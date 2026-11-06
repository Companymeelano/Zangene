#!/usr/bin/env python3
"""Recover only the known, public UI icons from the pinned offline demo APK.

This does not recover source, product/customer data, or production credentials.
It rejects any other APK and never overwrites an existing differing file.
"""
from hashlib import sha256
from pathlib import Path
import struct
import sys
import zipfile

EXPECTED_APK = 'e55ea9c58603a88fefd03bcb127edbb5ac420c89ac35ab21c94aa99c67e8031b'
NAMES = frozenset('ai bag bell box camera chart crown diamond doc gift heart home instagram lock phone search settings share shield time truck users whatsapp'.split())


def recover(apk, target):
    apk, target = Path(apk), Path(target)
    if sha256(apk.read_bytes()).hexdigest() != EXPECTED_APK:
        raise ValueError('APK checksum does not match the pinned offline demo.')
    found = {}
    with zipfile.ZipFile(apk) as archive:
        for entry in archive.infolist():
            prefixes = ('res/drawable-nodpi/jewel_', 'assets/jewel_extra/jewel_')
            if not entry.filename.startswith(prefixes) or not entry.filename.endswith('.png'):
                continue
            name = Path(entry.filename).name
            if name[6:-4] not in NAMES or entry.file_size > 2_000_000:
                raise ValueError('Unexpected icon entry.')
            data = archive.read(entry)
            if len(data) < 26 or data[:8] != b'\x89PNG\r\n\x1a\n':
                raise ValueError('Invalid PNG.')
            width, height, depth, kind = struct.unpack('>IIBB', data[16:26])
            if (width, height, depth, kind) != (256, 256, 8, 6):
                raise ValueError('Icon must be 256px RGBA PNG.')
            if name in found:
                raise ValueError('Duplicate icon entry.')
            found[name] = data
    if set(found) != {'jewel_' + n + '.png' for n in NAMES}:
        raise ValueError('Unexpected icon inventory.')
    target.mkdir(parents=True, exist_ok=True)
    # Check all conflicts before writing anything.
    for name, data in found.items():
        f = target / name
        if f.exists() and f.read_bytes() != data:
            raise FileExistsError('Existing icon differs: ' + name)
    for name, data in found.items():
        (target / name).write_bytes(data)
    return len(found)

if __name__ == '__main__':
    if len(sys.argv) != 3:
        raise SystemExit('Usage: recover_demo_icons.py <offline-demo.apk> <output-directory>')
    print('Recovered', recover(sys.argv[1], sys.argv[2]), 'public icon assets.')
