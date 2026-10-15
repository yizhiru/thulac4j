#!/usr/bin/env python3
"""Read one Central deployment status with normal TLS verification; never upload."""
import argparse
import base64
import json
from pathlib import Path
import urllib.error
import urllib.request
import uuid
import xml.etree.ElementTree as ET


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *args, **kwargs):
        return None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('deployment_id', type=uuid.UUID)
    parser.add_argument('--output', type=Path, default=Path('target/release-checks/central-deployment.json'))
    args = parser.parse_args()
    settings = Path.home() / '.m2/settings.xml'
    if not settings.exists():
        raise SystemExit('Configure the central server credentials locally first.')
    root = ET.parse(settings).getroot()
    fields = None
    for server in root.iter():
        if server.tag.rsplit('}', 1)[-1] != 'server':
            continue
        values = {n.tag.rsplit('}', 1)[-1]: n.text or '' for n in server}
        if values.get('id') == 'central':
            fields = values
            break
    if not fields or not fields.get('username') or not fields.get('password'):
        raise SystemExit('Central token fields are missing; no request made.')
    token = base64.b64encode((fields['username'] + ':' + fields['password']).encode()).decode()
    endpoint = 'https://central.sonatype.com/api/v1/publisher/status?id=' + str(args.deployment_id)
    request = urllib.request.Request(endpoint, data=b'', headers={'Authorization': 'Bearer ' + token}, method='POST')
    try:
        with urllib.request.build_opener(NoRedirect).open(request, timeout=30) as response:
            data = json.load(response)
    except urllib.error.HTTPError as error:
        raise SystemExit('Central status query returned HTTP ' + str(error.code) + '; no credentials displayed.')
    except (urllib.error.URLError, TimeoutError):
        raise SystemExit('Central status query failed due to a network/TLS error; certificate checking remains enabled.')
    if data.get('deploymentId') != str(args.deployment_id):
        raise SystemExit('Unexpected deployment ID in server response.')
    safe = {k: data[k] for k in ['deploymentId', 'deploymentName', 'deploymentState', 'purls', 'errors'] if k in data}
    rendered = json.dumps(safe, ensure_ascii=False, indent=2)
    for secret in [fields['username'], fields['password'], token]:
        rendered = rendered.replace(secret, '[REDACTED]')
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(rendered + '\n')
    print(rendered)


if __name__ == '__main__':
    main()
