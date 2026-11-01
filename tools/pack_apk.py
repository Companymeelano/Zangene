#!/usr/bin/env python3
"""Merge a linked resource APK with a classes.dex into a 4-byte aligned APK.

Usage: pack_apk.py <base.apk> <classes.dex> <out.apk>
"""
import sys
import zipfile


def pack(base_apk: str, dex_path: str, out_apk: str) -> None:
    dex = open(dex_path, 'rb').read()
    with zipfile.ZipFile(base_apk) as zin, zipfile.ZipFile(out_apk, 'w') as zout:
        for item in (i for i in zin.infolist() if i.filename != 'classes.dex'):
            data = zin.read(item.filename)
            info = zipfile.ZipInfo(item.filename, item.date_time)
            info.compress_type = zipfile.ZIP_STORED
            info.external_attr = item.external_attr
            info.internal_attr = item.internal_attr
            info.create_system = item.create_system
            offset = zout.fp.tell() + 30 + len(item.filename.encode())
            alignment = 4096 if item.filename.endswith('.so') else 4
            pad = (-offset) % alignment
            info.extra = b'\x00' * pad if pad else b''
            zout.writestr(info, data)

        dex_info = zipfile.ZipInfo('classes.dex')
        dex_info.compress_type = zipfile.ZIP_DEFLATED
        zout.writestr(dex_info, dex)

    with zipfile.ZipFile(out_apk) as check:
        bad = [i.filename for i in check.infolist()
               if i.compress_type == zipfile.ZIP_STORED
               and (i.header_offset + 30 + len(i.filename.encode()) + len(i.extra)) % 4]
    assert not bad, f'misaligned entries: {bad}'
    print(f'packed {out_apk} with {len(check.infolist())} entries')


if __name__ == '__main__':
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    pack(sys.argv[1], sys.argv[2], sys.argv[3])
