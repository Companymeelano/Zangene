#!/usr/bin/env python3
"""Make draft cards by compositing unaltered product crops from user screenshots.

Requires ImageMagick, the user-provided screenshots, and the tracked backdrop.
It does not create packaging or write to the app database.
"""
from pathlib import Path
import argparse
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
BACKDROP = ROOT / 'reconstruction/android/assets/catalog/biz_pink_backdrop.png'
# Crop rectangles deliberately include the entire product and surrounding white space.
ITEMS = {
    'day': ('171818', 600), 'night': ('171827', 250), 'eye': ('171832', 190),
    'sunscreen30': ('171837', 250), 'sunscreen50': ('171842', 250),
    'lifting': ('171846', 250), 'hydration': ('171850', 190),
    'antispot': ('171854', 250), 'pocket50': ('171900', 280), 'primer': ('171904', 260),
}


def create(screenshots, output):
    screenshots, output = Path(screenshots), Path(output)
    paths = [screenshots / f'Screenshot_20261107_{stamp}_Samsung Browser.jpg' for stamp, _ in ITEMS.values()]
    if any(not p.is_file() for p in paths):
        raise ValueError('All ten screenshots are required')
    output.mkdir(parents=True, exist_ok=True)
    if any((output / (key + '.jpg')).exists() for key in ITEMS):
        raise FileExistsError('Output image already exists; refusing to overwrite')
    with tempfile.TemporaryDirectory() as tmp:
        tmp = Path(tmp)
        # A feathered mask merges the photo background with the decorative stage; the central
        # product area uses the screenshot pixels, not AI-reconstructed packaging.
        mask = tmp/'mask.png'
        subprocess.run(['convert', '-size', '760x718', 'xc:black', '-fill', 'white',
                        '-draw', 'roundrectangle 40,40 720,678 30,30', '-blur', '0x16', str(mask)], check=True)
        for key, (stamp, y) in ITEMS.items():
            src = screenshots / f'Screenshot_20261107_{stamp}_Samsung Browser.jpg'
            crop = tmp/(key+'.png'); layer = tmp/(key+'-layer.png')
            subprocess.run(['convert', str(src), '-crop', f'900x850+90+{y}', '+repage', '-bordercolor', 'white', '-border', '100',
                            '-resize', '760x718', '-gravity', 'center', '-background', 'white', '-extent', '760x718', str(crop)], check=True)
            subprocess.run(['convert', str(crop), str(mask), '-alpha', 'off', '-compose', 'CopyOpacity',
                            '-composite', str(layer)], check=True)
            subprocess.run(['convert', str(BACKDROP), str(layer), '-gravity', 'center',
                            '-geometry', '+0-35', '-compose', 'Over', '-composite',
                            '-strip', '-quality', '91', str(output/(key+'.jpg'))], check=True)
    return len(ITEMS)

if __name__ == '__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('screenshots')
    parser.add_argument('output')
    args=parser.parse_args()
    print('Created', create(args.screenshots,args.output), 'draft product images.')
