#!/usr/bin/env python3
"""Summarize complete JMH comparisons without discarding forks or iterations."""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--input', type=Path, default=ROOT / 'target/performance')
    args = parser.parse_args()
    rows = []
    for mode in ['segment', 'pos']:
        for workload in ['corpus', 'short', 'mixed', 'long']:
            results = []
            for version in ['baseline', 'optimized']:
                path = args.input / '{}-{}-{}.json'.format(version, mode, workload)
                values = json.loads(path.read_text())
                if len(values) != 1:
                    raise ValueError('Expected one benchmark result in ' + str(path))
                result = values[0]
                if result['primaryMetric']['scoreUnit'] != 'ops/s':
                    raise ValueError('Unexpected score unit')
                results.append(result)
            old, new = results
            a, b = old['primaryMetric'], new['primaryMetric']
            old_bytes = old['secondaryMetrics']['gc.alloc.rate.norm']['score']
            new_bytes = new['secondaryMetrics']['gc.alloc.rate.norm']['score']
            intervals_overlap = max(a['scoreConfidence'][0], b['scoreConfidence'][0]) <= min(a['scoreConfidence'][1], b['scoreConfidence'][1])
            row = {
                'mode': mode, 'workload': workload,
                'baseline_ops_per_second': a['score'], 'optimized_ops_per_second': b['score'],
                'baseline_error_999': a['scoreError'], 'optimized_error_999': b['scoreError'],
                'throughput_change_percent': (b['score'] / a['score'] - 1) * 100,
                'baseline_bytes_per_op': old_bytes, 'optimized_bytes_per_op': new_bytes,
                'allocation_reduction_percent': (1 - new_bytes / old_bytes) * 100,
                'confidence_intervals_overlap': intervals_overlap,
            }
            rows.append(row)
            print('{mode:7} {workload:6} {baseline_ops_per_second:12.1f} -> {optimized_ops_per_second:12.1f} ops/s '
                  '{throughput_change_percent:+6.1f}%  allocation -{allocation_reduction_percent:5.1f}%  '
                  'CI overlap={confidence_intervals_overlap}'.format(**row))
    (args.input / 'summary.json').write_text(json.dumps(rows, indent=2) + '\n')

if __name__ == '__main__':
    main()
