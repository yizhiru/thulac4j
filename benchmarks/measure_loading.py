#!/usr/bin/env python3
"""Measure initialization and macOS process peak RSS in five fresh JVMs per case."""
import json
import platform
import re
import statistics
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'target/performance'
JAVA = json.loads((OUT / 'environment.json').read_text())['java']
if platform.system() != 'Darwin':
    raise SystemExit('This probe uses macOS /usr/bin/time -l (peak RSS in bytes).')
samples = []
for mode in ['segment', 'pos']:
    for iteration in range(5):
        versions = [('baseline', OUT / 'baseline'), ('optimized', ROOT)]
        if iteration % 2:
            versions.reverse()
        for version, directory in versions:
            command = ['/usr/bin/time', '-l', JAVA, '-Xms2g', '-Xmx2g', '-XX:+UseG1GC', '-cp',
                       str(OUT / 'helpers') + ':' + str(directory / 'target/benchmarks/classes'),
                       'io.github.yizhiru.thulac4j.ModelLoadProbe', mode]
            result = subprocess.run(command, cwd=ROOT, text=True, capture_output=True)
            if result.returncode:
                raise SystemExit('Loading probe failed:\n' + result.stdout + result.stderr)
            output = json.loads(result.stdout)
            peak = re.search(r'(\d+)\s+maximum resident set size', result.stderr)
            if not peak:
                raise RuntimeError(result.stderr)
            output.update(mode=mode, version=version, iteration=iteration, peak_rss_bytes=int(peak.group(1)))
            samples.append(output)
            print(mode, version, iteration + 1, round(output['initialization_ms'], 1), 'ms', flush=True)
(OUT / 'loading-samples.json').write_text(json.dumps(samples, indent=2) + '\n')
summary = []
for mode in ['segment', 'pos']:
    for version in ['baseline', 'optimized']:
        values = [x for x in samples if x['mode'] == mode and x['version'] == version]
        summary.append({'mode': mode, 'version': version,
                        'median_initialization_ms': statistics.median(x['initialization_ms'] for x in values),
                        'median_peak_rss_bytes': statistics.median(x['peak_rss_bytes'] for x in values),
                        'samples': len(values)})
(OUT / 'loading-summary.json').write_text(json.dumps(summary, indent=2) + '\n')
print(json.dumps(summary, indent=2))
