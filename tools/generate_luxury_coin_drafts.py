#!/usr/bin/env python3
"""Generate the isolated Android draft catalog class from the reviewed JSON file."""
import argparse
import json
import re
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'reconstruction/catalog/luxury_coin_155_drafts.json'
OUTPUT=ROOT/'reconstruction/android/src/com/asiyestore/admin/app/LuxuryCoinDraftCatalog.java'


def java_string(value):
    return json.dumps(value, ensure_ascii=True)


def render(doc):
    if doc['auto_import'] or doc['auto_publish'] or len(doc['products'])!=10:
        raise ValueError('Expected ten opt-in unpublished drafts')
    rows=[]; seen=set()
    for item in doc['products']:
        key=item['draft_id']
        if not re.fullmatch(r'LC155-[A-Z0-9]+',key) or key in seen:
            raise ValueError('Invalid draft ID')
        seen.add(key)
        if item['published'] or item['stock_verified'] or item['approved_sale_price_toman'] is not None:
            raise ValueError('Only unverified draft input is allowed')
        if item['source_price_irr']!=10*item['reference_price_toman'] or item['default_stock_requested']!=5:
            raise ValueError('Invalid price conversion or initial stock')
        asset='catalog/luxury_coin/'+key[6:].lower()+'.jpg'
        if item['image_path']!='reconstruction/android/assets/'+asset or not (ROOT/item['image_path']).is_file():
            raise ValueError('Missing or unexpected image path')
        fields=['draft_id','product_name','english_name','brand','category','size','short_description','description','usage','seo_title','seo_description']
        values=[java_string(item[k]) for k in fields]
        values += [java_string('، '.join(item['keywords']))]
        values += [java_string(item[k]) for k in ['instagram_caption','youtube_caption','telegram_caption','whatsapp_caption']]
        values += [java_string(item['price_note'] + ' منبع: ' + item['source_category_url'] + '؛ تصویر: ' + item['source_screenshot'])]
        values += [java_string(asset), str(item['reference_price_toman'])+'L']
        rows.append('        new Draft('+', '.join(values)+')')
    return '''package com.asiyestore.admin.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Generated from reviewed screenshot drafts; local IDs are not official SKUs. */
public final class LuxuryCoinDraftCatalog {
    public static final class Draft {
        public final String sku, nameFa, nameEn, brand, category, size, shortDescription, description, usage,
                seoTitle, seoDescription, keywords, instagram, youtube, telegram, whatsapp, priceNote, assetPath;
        public final long referencePriceToman;
        private Draft(String sku, String nameFa, String nameEn, String brand, String category, String size,
                String shortDescription, String description, String usage, String seoTitle, String seoDescription,
                String keywords, String instagram, String youtube, String telegram, String whatsapp,
                String priceNote, String assetPath, long referencePriceToman) {
            this.sku=sku; this.nameFa=nameFa; this.nameEn=nameEn; this.brand=brand; this.category=category;
            this.size=size; this.shortDescription=shortDescription; this.description=description; this.usage=usage;
            this.seoTitle=seoTitle; this.seoDescription=seoDescription; this.keywords=keywords;
            this.instagram=instagram; this.youtube=youtube; this.telegram=telegram; this.whatsapp=whatsapp;
            this.priceNote=priceNote; this.assetPath=assetPath; this.referencePriceToman=referencePriceToman;
        }
    }
    private static final List<Draft> DRAFTS=Collections.unmodifiableList(Arrays.asList(
''' + ',\n'.join(rows) + '''
    ));
    private LuxuryCoinDraftCatalog() { }
    public static List<Draft> drafts() { return DRAFTS; }
    public static String assetPathForSku(String sku) {
        if (sku == null) return null;
        for (Draft d : DRAFTS) if (d.sku.equals(sku)) return d.assetPath;
        return null;
    }
}
'''


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--check',action='store_true')
    args=parser.parse_args()
    content=render(json.loads(SOURCE.read_text(encoding='utf-8')))
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding='utf-8')!=content:
            raise SystemExit('Generated catalog is out of date')
    else:
        OUTPUT.write_text(content,encoding='utf-8')

if __name__=='__main__': main()
