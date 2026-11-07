#!/usr/bin/env python3
"""Create a URL-only inventory from a user-provided Biz sitemap XML.

No network access, product guesses, pricing or automatic import into the app.
"""
import argparse
import csv
import re
from pathlib import Path
from urllib.parse import urlsplit
import xml.etree.ElementTree as ET

MAX_BYTES = 25_000_000
PRODUCT_PATH = re.compile(r'^/products/(\d+)/(\d+)/(\d+)/?$')


def parse_sitemap(path):
    raw = Path(path).read_bytes()
    if len(raw) > MAX_BYTES:
        raise ValueError('Sitemap exceeds size limit')
    if b'<!DOCTYPE' in raw.upper() or b'<!ENTITY' in raw.upper():
        raise ValueError('DTD and entities are not supported')
    try:
        root = ET.fromstring(raw)
    except ET.ParseError as e:
        raise ValueError('Invalid sitemap XML') from e
    kind = root.tag.rsplit('}', 1)[-1]
    if kind != 'urlset':
        raise ValueError('Expected urlset; download each child sitemap separately if this is a sitemap index')
    found = {}
    for entry in root:
        if entry.tag.rsplit('}', 1)[-1] != 'url':
            continue
        locs = [child.text for child in entry if child.tag.rsplit('}', 1)[-1] == 'loc']
        if len(locs) != 1 or not locs[0]:
            continue
        url = urlsplit(locs[0].strip())
        if url.scheme != 'https' or url.hostname not in ('bizmlm.ir', 'www.bizmlm.ir') or url.username or url.password or url.port not in (None, 443) or url.query or url.fragment:
            continue
        match = PRODUCT_PATH.fullmatch(url.path)
        if not match:
            continue
        category, group, product = match.groups()
        canonical = f'https://bizmlm.ir/products/{category}/{group}/{product}'
        found[canonical] = (product, category, group, canonical)
    return sorted(found.values(), key=lambda item: (int(item[0]), item[3]))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('sitemap', help='locally saved XML file; no network request is made')
    parser.add_argument('output', help='new CSV output path; existing files are never overwritten')
    args = parser.parse_args()
    rows = parse_sitemap(args.sitemap)
    if not rows:
        raise SystemExit('No product URLs found; no output created.')
    with open(args.output, 'x', encoding='utf-8-sig', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(['product_id', 'category_id', 'group_id', 'source_url', 'name', 'price_irr', 'status'])
        for row in rows:
            writer.writerow([*row, '', '', 'needs_product_and_price_verification'])
    print(f'{len(rows)} product URLs saved; titles, prices, images and availability are not determined by the sitemap.')

if __name__ == '__main__':
    main()
