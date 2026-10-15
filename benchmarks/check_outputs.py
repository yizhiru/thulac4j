#!/usr/bin/env python3
"""Compare 117 corpus predictions in each mode using the two already built artifacts."""
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'target/performance'
JAVA = json.loads((OUT / 'environment.json').read_text())['java']
JAVAC = str(Path(JAVA).with_name('javac'))
HELPERS = OUT / 'helpers'
HELPERS.mkdir(parents=True, exist_ok=True)
subprocess.run([JAVAC, '--release', '8', '-cp', str(ROOT / 'target/benchmarks/classes'), '-d', str(HELPERS),
                str(ROOT / 'benchmarks/CorpusSnapshot.java'), str(ROOT / 'benchmarks/ModelLoadProbe.java')], check=True)
results = {}
for mode in ['segment', 'pos']:
    files = []
    for version, directory in [('baseline', OUT / 'baseline'), ('optimized', ROOT)]:
        path = OUT / '{}-{}-outputs.txt'.format(version, mode)
        classes = directory / 'target/benchmarks/classes'
        subprocess.run([JAVA, '-Xms2g', '-Xmx2g', '-XX:+UseG1GC', '-cp', str(HELPERS) + ':' + str(classes),
                        'io.github.yizhiru.thulac4j.CorpusSnapshot', mode, str(path)], cwd=ROOT, check=True)
        files.append(path.read_text().splitlines())
    if len(files[0]) != 117 or files[0] != files[1]:
        differences = [(i, a, b) for i, (a, b) in enumerate(zip(*files)) if a != b]
        raise SystemExit('Prediction mismatch in {}: {}'.format(mode, differences))
    results[mode] = {'sentences': 117, 'identical': True}
    print(mode, '117/117 predictions identical')
(OUT / 'output-equivalence.json').write_text(json.dumps(results, indent=2) + '\n')
