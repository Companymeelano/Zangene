#!/usr/bin/env python3
"""Fail-closed APK preflight; never use this as a substitute for source/device audits.

Requires existing aapt2, apksigner and a public, independently approved release
certificate SHA-256 fingerprint. It does not inspect or print private secrets.
"""
import argparse
import re
import subprocess
import sys
import zipfile
from pathlib import Path

TEST_MARKERS = (
    'ورود آزمایشی بدون رمز',
    'نسخه آزمایشی آفلاین',
    'OFFLINE TEST · NOT FOR RELEASE',
    'نسخه آزمایشی آفلاین است؛ پیامک و ورود واقعی فعال نیستند.',
)


def inspect(apk, aapt2, apksigner, expected_cert):
    issues = []
    apk = Path(apk)
    if not apk.is_file():
        return ['APK file not found.']
    with zipfile.ZipFile(apk) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            issues.append('APK contains duplicate ZIP entries.')
        dex_names = [n for n in names if re.fullmatch(r'classes(?:[2-9]|[1-9][0-9]+)?\.dex', n)]
        if 'classes.dex' not in dex_names:
            issues.append('Primary DEX is missing.')
        for name in dex_names:
            if archive.getinfo(name).file_size > 128 * 1024 * 1024:
                issues.append('DEX is too large to inspect safely.')
                continue
            dex = archive.read(name)
            if any(marker.encode('utf-8') in dex for marker in TEST_MARKERS):
                issues.append('Offline demo or unauthenticated manager marker found in DEX.')
                break
    def run(args):
        try:
            result = subprocess.run(args, capture_output=True, text=True, timeout=30, check=False)
        except (OSError, subprocess.TimeoutExpired):
            return None
        return result.stdout if result.returncode == 0 else None

    badging = run([str(aapt2), 'dump', 'badging', str(apk)])
    if badging is None:
        issues.append('Could not inspect Android manifest using aapt2.')
    else:
        match = re.search(r"^package: name='([^']+)' versionCode='(\d+)'", badging, re.M)
        if not match or match.group(1) != 'com.asiyestore.admin':
            issues.append('Package name is not the intended production application ID.')
        if not match or int(match.group(2)) <= 1:
            issues.append('Version code is missing or belongs to the test series.')
        if "application-label:'Asiye Store'" not in badging:
            issues.append('Default launcher label is not Asiye Store.')
        if "uses-permission: name='android.permission.INTERNET'" not in badging:
            issues.append('INTERNET permission missing: this may be an offline demo.')
        if 'application-debuggable' in badging:
            issues.append('Application is debuggable.')

    cert = re.sub(r'[:\s]', '', expected_cert).lower()
    if not re.fullmatch(r'[0-9a-f]{64}', cert):
        issues.append('A valid approved release certificate SHA-256 fingerprint is required.')
    signed = run([str(apksigner), 'verify', '--min-sdk-version', '24', '--print-certs', str(apk)])
    if signed is None:
        issues.append('Signature verification failed or apksigner is unavailable.')
    else:
        signers = re.findall(r'^Signer #\d+ certificate SHA-256 digest:\s*([0-9a-fA-F:]+)', signed, re.M)
        if len(signers) != 1 or re.sub(':', '', signers[0]).lower() != cert:
            issues.append('APK signer does not match the one approved release certificate.')
    return issues


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('apk')
    p.add_argument('--aapt2', required=True)
    p.add_argument('--apksigner', required=True)
    p.add_argument('--certificate-sha256', required=True, help='public certificate fingerprint, not a private key')
    args = p.parse_args()
    try:
        issues = inspect(args.apk, args.aapt2, args.apksigner, args.certificate_sha256)
    except (OSError, zipfile.BadZipFile, RuntimeError):
        issues = ['APK could not be read safely.']
    if issues:
        print('NOT READY FOR RELEASE:')
        for issue in issues: print(' - '+issue)
        return 1
    print('APK preflight passed; production security, backend and device tests remain mandatory.')
    return 0

if __name__ == '__main__':
    sys.exit(main())
