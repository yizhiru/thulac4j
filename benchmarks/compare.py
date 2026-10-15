#!/usr/bin/env python3
"""Build an untouched Git baseline and run the same JMH workloads on both versions."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_REF = 'a9d5b8405e71ff900be08f36b876c225daec63c7'

def benchmark_jar(directory):
    pom = ET.parse(directory / 'pom.xml').getroot()
    ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
    artifact = pom.findtext('m:artifactId', namespaces=ns)
    version = pom.findtext('m:version', namespaces=ns)
    if not artifact or not version or '${' in version:
        raise ValueError('Expected explicit artifactId and version in ' + str(directory / 'pom.xml'))
    return directory / 'target/benchmarks/{}-{}-benchmarks.jar'.format(artifact, version)


def run(command, cwd, log):
    with log.open('w') as stream:
        subprocess.run(command, cwd=cwd, stdout=stream, stderr=subprocess.STDOUT, check=True)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline-ref', default=DEFAULT_REF)
    parser.add_argument('--java', default=shutil.which('java'))
    parser.add_argument('--output', type=Path, default=ROOT / 'target/performance')
    parser.add_argument('--skip-build', action='store_true')
    parser.add_argument('--build-only', action='store_true', help='Build/validate both jars without running measurements')
    args = parser.parse_args()
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    baseline = output / 'baseline'
    ref = subprocess.check_output(['git', 'rev-parse', args.baseline_ref], cwd=ROOT, text=True).strip()
    if not args.skip_build:
        paths = subprocess.check_output(['git', 'ls-tree', '-r', '--name-only', ref, 'src/main/java'], cwd=ROOT, text=True).splitlines()
        for name in paths:
            path = baseline / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(subprocess.check_output(['git', 'show', ref + ':' + name], cwd=ROOT))
        shutil.copyfile(ROOT / 'pom.xml', baseline / 'pom.xml')
        for name in ['TokenizerBenchmark.java', 'CorpusBenchmark.java']:
            relative = Path('src/jmh/java/io/github/yizhiru/thulac4j') / name
            (baseline / relative).parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / relative, baseline / relative)
        for name in ['models', 'dicts', 'benchmarks']:
            link = baseline / name
            if not link.exists():
                link.symlink_to(ROOT / name, target_is_directory=True)
        for version, path in [('baseline', baseline), ('optimized', ROOT)]:
            print('Building', version, flush=True)
            run(['mvn', '-B', '-ntp', '-Pbenchmarks', '-Dmaven.test.skip=true', 'package'], path, output / ('build-' + version + '.log'))
    for directory in [baseline, ROOT]:
        if not benchmark_jar(directory).is_file():
            raise FileNotFoundError(benchmark_jar(directory))
    if args.build_only:
        print('Both benchmark jars are ready.')
        return
    environment = {
        'baseline_ref': ref,
        'java': args.java,
        'java_version': subprocess.check_output([args.java, '-version'], stderr=subprocess.STDOUT, text=True),
        'platform': dict(zip(['system', 'node', 'release', 'version', 'machine'], os.uname())),
        'jvm_args': '-Xms2g -Xmx2g -XX:+UseG1GC',
        'forks': 2, 'threads': 1, 'warmup_iterations': 3, 'measurement_iterations': 5, 'iteration_seconds': 1,
        'model_manifest': json.loads((ROOT / 'models/manifest.json').read_text()),
        'corpus_sha256': hashlib.sha256((ROOT / 'benchmarks/corpus.txt').read_bytes()).hexdigest(),
        'started_at': time.strftime('%Y-%m-%dT%H:%M:%S%z'),
        'run_order': [],
    }
    # Retain exact production sources used for the optimized run, as they are uncommitted.
    snapshot = output / 'optimized-source'
    shutil.copytree(ROOT / 'src/main/java', snapshot, dirs_exist_ok=True)
    source_hashes = {str(p.relative_to(snapshot)): hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(snapshot.rglob('*.java'))}
    environment['optimized_source_sha256'] = source_hashes
    workloads = [(mode, kind) for mode in ['segment', 'pos'] for kind in ['corpus', 'short', 'mixed', 'long']]
    for index, (mode, kind) in enumerate(workloads):
        order = [('baseline', baseline), ('optimized', ROOT)]
        if index % 2:
            order.reverse()
        for version, directory in order:
            label = '{}-{}-{}'.format(version, mode, kind)
            environment['run_order'].append(label)
            (output / 'environment.json').write_text(json.dumps(environment, ensure_ascii=False, indent=2) + '\n')
            jar = benchmark_jar(directory)
            benchmark = 'CorpusBenchmark.tokenizeCorpus' if kind == 'corpus' else 'TokenizerBenchmark.tokenize'
            command = [args.java, '-jar', str(jar), benchmark + '$', '-p', 'mode=' + mode,
                       '-wi', '3', '-w', '1s', '-i', '5', '-r', '1s', '-f', '2', '-t', '1',
                       '-jvmArgs', environment['jvm_args'], '-prof', 'gc', '-foe', 'true',
                       '-rf', 'json', '-rff', str(output / (label + '.json'))]
            if kind != 'corpus':
                command += ['-p', 'textKind=' + kind]
            print('Measuring', label, flush=True)
            run(command, directory, output / (label + '.log'))
            result = json.loads((output / (label + '.json')).read_text())
            if len(result) != 1:
                raise RuntimeError('Unexpected benchmark result: ' + label)
            metric = result[0]['primaryMetric']
            print('Finished {}: {:.2f} {}'.format(label, metric['score'], metric['scoreUnit']), flush=True)
    environment['finished_at'] = time.strftime('%Y-%m-%dT%H:%M:%S%z')
    (output / 'environment.json').write_text(json.dumps(environment, ensure_ascii=False, indent=2) + '\n')

if __name__ == '__main__':
    main()
