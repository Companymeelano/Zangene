#!/usr/bin/env python3
"""Recover five specific public demo images; does not turn the demo into a release.

The reference product images are draft-only screenshots, not verified catalog photos.
"""
from hashlib import sha256
from pathlib import Path
import sys
import zipfile

EXPECTED_APK = 'e55ea9c58603a88fefd03bcb127edbb5ac420c89ac35ab21c94aa99c67e8031b'
FILES = {
    'img_launcher_pink.png': '62bf72e10284c19543163b41320c396837ad2cbf061a6ebb1a079f72661eaf98',
    'img_pink_hero.webp': '386bad1382aae6aa89b086830310cfbe5aa139d4e7a02ced1b4ee4c9de84a909',
    'img_reference_gatieau.webp': '825a546658c3f9876d9df7a6e425e28fe7fe2458e479b16ccf5e243828cec408',
    'img_reference_moringa.webp': 'bf064e1ad35f885ff411e84fa9f81096eb34739f047c4f524119212b195a9166',
    'img_reference_wheat.webp': 'b76f54e2cdc0a15108350f65f24c7ccb03c87af8fa81a46a3f762b1b2ba98f03',
}
PREFIX = 'res/drawable-nodpi/'


def recover(apk, target):
    apk, target = Path(apk), Path(target)
    if sha256(apk.read_bytes()).hexdigest() != EXPECTED_APK:
        raise ValueError('APK checksum does not match the pinned offline demo')
    found = {}
    with zipfile.ZipFile(apk) as archive:
        for entry in archive.infolist():
            if entry.filename not in {PREFIX + name for name in FILES}:
                continue
            name = entry.filename[len(PREFIX):]
            if name in found or entry.file_size > 2_000_000:
                raise ValueError('Duplicate or oversized image: ' + name)
            data = archive.read(entry)
            if sha256(data).hexdigest() != FILES[name]:
                raise ValueError('Image checksum mismatch: ' + name)
            found[name] = data
    if set(found) != set(FILES):
        raise ValueError('Incomplete image inventory')
    # Check the entire inventory before creating or changing output files.
    for name, data in found.items():
        path = target / name
        if path.is_symlink() or (path.exists() and (not path.is_file() or path.read_bytes() != data)):
            raise FileExistsError('Existing image differs or is a symlink: ' + name)
    target.mkdir(parents=True, exist_ok=True)
    for name, data in found.items():
        path = target / name
        if not path.exists():
            path.write_bytes(data)
    return len(found)


if __name__ == '__main__':
    if len(sys.argv) != 3:
        raise SystemExit('Usage: recover_demo_visuals.py <offline-demo.apk> <output-directory>')
    print('Recovered', recover(sys.argv[1], sys.argv[2]), 'demo images.')
