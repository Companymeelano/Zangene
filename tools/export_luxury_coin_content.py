#!/usr/bin/env python3
"""Export the reviewed draft text for human review; not a release or an app update."""
import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = ROOT / 'reconstruction/catalog/luxury_coin_155_drafts.json'
output = ROOT / 'LUXURY_COIN_PRODUCT_CONTENT_FA.md'

def render(data):
    lines = ['# پیش‌نویس محتوای ۱۰ محصول لاکچری کوین', '',
             'این متن‌ها برای بازبینی هستند؛ قیمت و موجودی روز، مشخصات بسته، مجوز تصویر و ادعاها باید پیش از فروش یا انتشار تأیید شوند. ورود به اکسپلور تضمین نمی‌شود. این فایل به‌تنهایی برنامه یا محصولات موجود را به‌روزرسانی نمی‌کند.', '']
    for p in data['products']:
        lines += ['---', '', '## '+p['product_name'], '', '**شناسه پیش‌نویس:** '+p['draft_id'], '',
                  '**معرفی کوتاه:** '+p['short_description'], '', '**توضیحات:** '+p['description'], '',
                  '**روش استفاده:** '+p['usage'], '', '**حجم/وزن:** '+p['size'], '',
                  '**عنوان سئو:** '+p['seo_title'], '', '**توضیح سئو:** '+p['seo_description'], '',
                  '**کلیدواژه‌ها:** '+ '، '.join(p['keywords']), '', '**متن جایگزین تصویر:** '+p['image_alt_text'], '',
                  '**کپشن اینستاگرام**', '', p['instagram_caption'], '', '**کپشن تلگرام**', '', p['telegram_caption'], '',
                  '**پیام واتساپ**', '', p['whatsapp_caption'], '', '**متن یوتیوب**', '', p['youtube_caption'], '',
                  '**یادداشت بررسی:** '+p['editorial_note'], '', '**یادداشت قیمت:** '+p['price_note'], '']
    return '\n'.join(lines)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    content = render(json.loads(source.read_text(encoding='utf-8')))
    if args.check:
        if not output.exists() or output.read_text(encoding='utf-8') != content:
            raise SystemExit('Content export is out of date')
    else:
        output.write_text(content, encoding='utf-8')

if __name__ == '__main__':
    main()
