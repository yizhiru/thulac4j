#!/usr/bin/env python3
"""Check release archives and run examples against the packaged jar, never upload."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import struct
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}


def require(condition, message):
    if not condition:
        raise SystemExit(message)


def digest(stream):
    value = hashlib.sha256()
    for block in iter(lambda: stream.read(1024 * 1024), b''):
        value.update(block)
    return value.hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java', help='JDK 9+ java executable used for smoke tests')
    parser.add_argument('--signed', action='store_true', help='Also require and verify all GPG signatures')
    args = parser.parse_args()
    pom = ET.parse(ROOT / 'pom.xml').getroot()
    artifact = pom.findtext('m:artifactId', namespaces=NS)
    version = pom.findtext('m:version', namespaces=NS)
    group = pom.findtext('m:groupId', namespaces=NS)
    require(version == '4.0.0', 'This release checklist expects version 4.0.0')
    out = ROOT / 'target/release-checks'
    out.mkdir(parents=True, exist_ok=True)
    # Never leave a stale success report after a failed recheck.
    (out / 'summary.json').unlink(missing_ok=True)
    archives = [ROOT / 'target' / (artifact + '-' + version + suffix + '.jar')
                for suffix in ['', '-sources', '-javadoc']]
    for archive in archives:
        require(archive.is_file(), 'Missing release archive: ' + str(archive))
    manifest = json.loads((ROOT / 'models/manifest.json').read_text())
    with zipfile.ZipFile(archives[0]) as jar:
        entries = set(jar.namelist())
        for name in ['cws_model.bin', 'cws_dat.bin', 'cws_label.txt', 'model_c_label.txt']:
            with jar.open('models/' + name) as stream:
                require(digest(stream) == manifest['files'][name]['sha256'], 'Wrong bundled model: ' + name)
        for name in ['ns_dat.bin', 'idiom_dat.bin', 'stop_dat.bin', 't2s.dat']:
            require('models/' + name in entries, 'Missing dictionary: ' + name)
        require('dicts/core_char.dict' in entries, 'Missing character dictionary')
        for name in ['model_c_model.bin', 'model_c_dat.bin']:
            require('models/' + name not in entries, 'POS weights/features should remain external: ' + name)
        classes = [name for name in entries if name.endswith('.class')]
        require(bool(classes), 'Jar contains no classes')
        for name in classes:
            major = struct.unpack('>H', jar.read(name)[6:8])[0]
            require(major == 52, 'Not Java 8 bytecode: ' + name)
        require(not any(name.startswith(('org/openjdk/jmh/', 'org/junit/')) for name in entries),
                'Test/benchmark dependencies leaked into the release jar')
        embedded = ET.fromstring(jar.read('META-INF/maven/' + group + '/' + artifact + '/pom.xml'))
        require(embedded.findtext('m:version', namespaces=NS) == version, 'Wrong embedded POM version')
    with zipfile.ZipFile(archives[1]) as jar:
        require('io/github/yizhiru/thulac4j/TokenizerConfig.java' in jar.namelist(), 'Missing new API source')
        require(not any(n.endswith(('.bin', '.dat')) for n in jar.namelist()), 'Models duplicated in sources jar')
    with zipfile.ZipFile(archives[2]) as jar:
        require('index.html' in jar.namelist(), 'Missing Javadoc index')
        require('io/github/yizhiru/thulac4j/TokenizerConfig.html' in jar.namelist(), 'Missing new API Javadoc')
    java = args.java or (str(Path(os.environ['JAVA_HOME']) / 'bin/java')
                        if os.environ.get('JAVA_HOME') else shutil.which('java'))
    require(java, 'Java executable not found')
    java = str(Path(java).resolve())
    javac = str(Path(java).with_name('javac'))
    examples = []
    for block in re.findall(r'```java\n(.*?)```', (ROOT / 'README.md').read_text(), re.S):
        match = re.search(r'public class (\w+)', block)
        if not match:
            continue
        name = match.group(1)
        source = out / (name + '.java')
        source.write_text(block, encoding='utf-8')
        subprocess.run([javac, '--release', '8', '-encoding', 'UTF-8', '-cp', str(archives[0]),
                        '-d', str(out), str(source)], check=True)
        result = subprocess.check_output([java, '-cp', str(out) + os.pathsep + str(archives[0]), name],
                                         cwd=ROOT, text=True, encoding='utf-8')
        expected = re.search(r'// (\[.*\])', block)
        if expected:
            require(result.strip() == expected.group(1), 'README output mismatch: ' + name)
        examples.append(name)
        print('Packaged-jar example passed:', name, flush=True)
    require(len(examples) == 4, 'Expected four complete README examples')
    subprocess.run([javac, '--release', '8', '-encoding', 'UTF-8', '-cp', str(archives[0]),
                    '-d', str(out), str(ROOT / 'benchmarks/CorpusSnapshot.java')], check=True)
    for mode in ['segment', 'pos']:
        prediction = out / (mode + '-outputs.txt')
        subprocess.run([java, '-cp', str(out) + os.pathsep + str(archives[0]),
                        'io.github.yizhiru.thulac4j.CorpusSnapshot', mode, str(prediction)], cwd=ROOT, check=True)
        reference = ROOT / 'benchmarks/results/2026-10-15' / ('optimized-' + mode + '-outputs.txt')
        lines = prediction.read_text(encoding='utf-8').splitlines()
        require(len(lines) == 117 and lines == reference.read_text(encoding='utf-8').splitlines(),
                'Packaged-jar regression mismatch: ' + mode)
        print('Packaged-jar corpus passed:', mode, '117/117', flush=True)
    if args.signed:
        require(shutil.which('gpg'), 'GPG is required for --signed checks')
        # maven-gpg-plugin 3.2.7 copies the project POM to target before signing it.
        signed_pom = ROOT / 'target' / (artifact + '-' + version + '.pom')
        require(signed_pom.is_file(), 'Missing POM copy produced by the GPG plugin')
        require(signed_pom.read_bytes() == (ROOT / 'pom.xml').read_bytes(), 'Signed POM copy is stale')
        for path in archives + [signed_pom]:
            signature = Path(str(path) + '.asc')
            require(signature.is_file(), 'Missing signature: ' + str(signature))
            subprocess.run(['gpg', '--batch', '--verify', str(signature), str(path)], check=True)
    files = []
    for path in archives:
        with path.open('rb') as stream:
            files.append({'file': path.name, 'bytes': path.stat().st_size, 'sha256': digest(stream)})
    report = {'version': version, 'java': java, 'bytecode_major': 52, 'classes': len(classes),
              'examples': examples, 'corpus_predictions': 234, 'signatures_verified': args.signed,
              'remote_upload_performed': False, 'artifacts': files}
    (out / 'summary.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
    print('Release archive checks passed. Signatures verified:', args.signed)


if __name__ == '__main__':
    main()
