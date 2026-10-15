#!/usr/bin/env python3
"""Enter Central Portal token credentials locally, without echoing them or uploading."""
import argparse
import getpass
import os
from pathlib import Path
import sys
import tempfile
import xml.etree.ElementTree as ET

SETTINGS = Path.home() / '.m2/settings.xml'
NAMESPACE = 'http://maven.apache.org/SETTINGS/1.2.0'


def normalize_token(value, field):
    value = value.strip()
    if value.startswith('<'):
        try:
            wrapper = ET.fromstring(value)
        except ET.ParseError:
            raise SystemExit('Paste the token value, or one complete <' + field + '> element.')
        if wrapper.tag != field or len(wrapper):
            raise SystemExit('Expected only the token ' + field + ' field, not a whole settings snippet.')
        value = (wrapper.text or '').strip()
    return value


def load_settings(path):
    if path.is_symlink():
        raise SystemExit('Refusing to modify symlinked settings.xml.')
    if path.exists():
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            raise SystemExit('Existing settings.xml is not valid XML; left unchanged.')
        if root.tag.rsplit('}', 1)[-1] != 'settings':
            raise SystemExit('Unexpected settings.xml root; left unchanged.')
    else:
        root = ET.Element('{' + NAMESPACE + '}settings')
    namespace = root.tag[1:].split('}')[0] if root.tag.startswith('{') else ''
    if namespace:
        ET.register_namespace('', namespace)
    prefix = '{' + namespace + '}' if namespace else ''
    servers = root.find(prefix + 'servers')
    server = None
    if servers is not None:
        matches = [s for s in servers.findall(prefix + 'server') if s.findtext(prefix + 'id') == 'central']
        if len(matches) > 1:
            raise SystemExit('Multiple central server entries; resolve them before continuing.')
        server = matches[0] if matches else None
    return root, prefix, servers, server


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Report configuration presence only; never print secrets')
    parser.add_argument('--replace', action='store_true', help='Explicitly replace an existing central token')
    args = parser.parse_args()
    root, prefix, servers, server = load_settings(SETTINGS)
    if args.check:
        ready = server is not None and all(server.findtext(prefix + name, '').strip() for name in ['username', 'password'])
        print('Central token fields configured:', bool(ready))
        print('This checks local configuration only, not account permissions or token validity.')
        return
    if not sys.stdin.isatty():
        raise SystemExit('Run this script in your local terminal; token input must not be piped or sent in chat.')
    if server is not None and not args.replace:
        raise SystemExit('A central entry already exists; left unchanged. Use --replace to rotate its token.')
    username = normalize_token(getpass.getpass('Central Portal token username (hidden): '), 'username')
    password = normalize_token(getpass.getpass('Central Portal token password (hidden): '), 'password')
    if not username or not password:
        raise SystemExit('Both token fields are required; settings were not changed.')
    if servers is None:
        servers = ET.SubElement(root, prefix + 'servers')
    if server is None:
        server = ET.SubElement(servers, prefix + 'server')
        ET.SubElement(server, prefix + 'id').text = 'central'
    for name, value in [('username', username), ('password', password)]:
        element = server.find(prefix + name)
        if element is None:
            element = ET.SubElement(server, prefix + name)
        element.text = value
    SETTINGS.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    fd, temporary = tempfile.mkstemp(prefix='.central-settings-', dir=SETTINGS.parent)
    try:
        with os.fdopen(fd, 'wb') as stream:
            os.fchmod(stream.fileno(), 0o600)
            ET.ElementTree(root).write(stream, encoding='utf-8', xml_declaration=True)
        os.replace(temporary, SETTINGS)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)
    print('Central token saved to ~/.m2/settings.xml (owner read/write only). No upload performed.')


if __name__ == '__main__':
    main()
