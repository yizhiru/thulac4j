#!/usr/bin/env python3
"""Download and verify the pinned THUNLP distribution, installing only missing models."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]

def sha256(path):
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--archive', type=Path, help='Use an already downloaded wheel')
    args = parser.parse_args()
    manifest = json.loads((ROOT / 'models/manifest.json').read_text())
    models = ROOT / 'models'
    missing = []
    for name, expected in manifest['files'].items():
        path = models / name
        if path.exists():
            if path.stat().st_size != expected['size'] or sha256(path) != expected['sha256']:
                raise SystemExit('Existing model does not match manifest; left unchanged: ' + str(path))
        else:
            missing.append(name)
    if not missing:
        print('All model files verified; no download needed.')
        return
    archive = args.archive or ROOT / 'target/model-downloads/thulac-0.2.2.verified.whl'
    archive.parent.mkdir(parents=True, exist_ok=True)
    if not archive.exists():
        if args.archive:
            raise SystemExit('Archive not found: ' + str(archive))
        partial = archive.with_suffix('.download')
        subprocess.run(['curl', '-fL', '--retry', '2', '--connect-timeout', '30', '--max-time', '600',
                        '-o', str(partial), manifest['download_url']], check=True)
        if sha256(partial) != manifest['archive_sha256']:
            raise SystemExit('Archive checksum mismatch; models were not installed.')
        partial.replace(archive)
    if sha256(archive) != manifest['archive_sha256']:
        raise SystemExit('Archive checksum mismatch; models were not installed.')
    with zipfile.ZipFile(archive) as package:
        for name in missing:
            expected = manifest['files'][name]
            path = models / name
            temporary = path.with_suffix(path.suffix + '.part')
            try:
                with package.open('thulac/models/' + name) as source, temporary.open('wb') as out:
                    for block in iter(lambda: source.read(1024 * 1024), b''):
                        out.write(block)
                if temporary.stat().st_size != expected['size'] or sha256(temporary) != expected['sha256']:
                    raise SystemExit('Model checksum mismatch: ' + name)
                temporary.replace(path)
                print('Installed and verified:', name)
            finally:
                temporary.unlink(missing_ok=True)

if __name__ == '__main__':
    main()
