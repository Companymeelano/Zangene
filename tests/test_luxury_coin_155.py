import json
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
class LuxuryCoinDraftTests(unittest.TestCase):
    def test_drafts_price_units_images_and_inventory(self):
        d=json.loads((ROOT/'reconstruction/catalog/luxury_coin_155_drafts.json').read_text(encoding='utf-8'))
        self.assertIs(d['auto_import'],False); self.assertIs(d['auto_publish'],False)
        items=d['products']; self.assertEqual(len(items),10)
        ids=set(); images=set()
        expected={'DAY':66000000,'NIGHT':66000000,'EYE':49500000,'SUNSCREEN30':49500000,
                  'SUNSCREEN50':49500000,'LIFTING':66000000,'HYDRATION':49500000,
                  'ANTISPOT':49500000,'POCKET50':19800000,'PRIMER':28600000}
        for p in items:
            self.assertNotIn(p['draft_id'],ids); ids.add(p['draft_id'])
            self.assertIs(p['published'],False); self.assertIsNone(p['approved_sale_price_toman'])
            self.assertIs(p['stock_verified'],False); self.assertEqual(p['default_stock_requested'],5)
            self.assertEqual(p['source_price_irr'], expected[p['draft_id'].removeprefix('LC155-')])
            self.assertEqual(p['source_price_irr'],p['reference_price_toman']*10)
            self.assertEqual(p['source_category_url'],'https://bizmlm.ir/products/6/155')
            image=ROOT/p['image_path']; self.assertTrue(image.is_file()); self.assertNotIn(image,images); images.add(image)
            self.assertEqual(image.read_bytes()[:2],b'\xff\xd8')
            for key in ('short_description','description','usage','seo_title','seo_description','keywords','instagram_caption','telegram_caption','whatsapp_caption','youtube_caption'):
                self.assertTrue(p[key],key)
        night=next(p for p in items if p['draft_id']=='LC155-NIGHT')
        self.assertIn('قیمت متفاوت',night['price_note'])
