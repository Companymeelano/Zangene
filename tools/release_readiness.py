#!/usr/bin/env python3
"""Fail closed on prerequisites for a real, source-built Asiye Store release.

Read-only: does not read, display, alter, or attempt to validate secret values.
Passing this check does not certify security, services, or device behaviour.
"""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def evaluate(root):
    root = Path(root)
    blockers = []
    activity = root / 'android/app/src/main/java/com/asiyestore/admin/app/MainActivity.java'
    manifest = root / 'android/app/src/main/AndroidManifest.xml'
    if not activity.is_file():
        blockers.append('Current Android application source is missing from the workspace.')
    else:
        src = activity.read_text(encoding='utf-8')
        for label, token in [('current pastel design', 'THEME_PASTEL_ROSE'),
                             ('simplified navigation', 'SimpleNavigation'),
                             ('current AS mark', 'img_launcher_pink')]:
            if token not in src:
                blockers.append(f'Android source does not include {label}; it is not the latest design.')
        if 'adminAuthenticated = true; adminMode = true; adminTab = ADMIN_REPORT;' in src:
            blockers.append('An offline administrator bypass appears in the application source.')
    if not manifest.is_file():
        blockers.append('Production Android manifest is missing.')
    else:
        m = manifest.read_text(encoding='utf-8')
        if 'android.permission.INTERNET' not in m:
            blockers.append('Manifest has no internet permission; this is not the connected release.')
        if 'android:debuggable="true"' in m or 'pearlicontest' in m or 'screenshotpolishtest' in m:
            blockers.append('Manifest appears to be a debug or isolated test build.')
    for rel, desc in [('backend/artisan', 'Laravel application entry point'),
                      ('backend/bootstrap/app.php', 'Laravel bootstrap'),
                      ('backend/app/Services/AiRouter', 'configured AI routing implementation')]:
        if not (root / rel).exists(): blockers.append(f'Missing {desc} ({rel}).')
    if (root / 'android/keystore/asiye-release.jks').exists():
        blockers.append('A historical release keystore exists in the working tree; do not use it.')
    # Never inspect keystore contents; a tracked key requires independent rotation and history remediation.
    try:
        tracked = subprocess.run(['git', '-C', str(root), 'ls-files', '--error-unmatch',
                                  'android/keystore/asiye-release.jks'],
                                 stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=False)
        if tracked.returncode == 0:
            blockers.append('A historical release keystore is tracked in Git; rotate it and address history exposure.')
    except OSError:
        blockers.append('Could not verify whether historical signing material is tracked.')
    return blockers


def main():
    issues = evaluate(ROOT)
    if issues:
        print('NOT READY FOR PRODUCTION:')
        for issue in issues: print(' - ' + issue)
        print('A clean result is not a security, integration, payment or real-device certification.')
        return 1
    print('Static prerequisites found. Independent integration, security and device testing still required.')
    return 0

if __name__ == '__main__':
    sys.exit(main())
