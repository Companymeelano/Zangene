import json,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class ExtraCatalogTests(unittest.TestCase):
 def test_extra_screenshot_drafts(self):
  d=json.loads((ROOT/'reconstruction/catalog/luxury_coin_extra_drafts.json').read_text(encoding='utf8'))
  self.assertFalse(d['auto_import']); self.assertFalse(d['auto_publish'])
  expected={'GEL4':93500000,'POWDER4':93500000,'ROSEMARY':52250000,'ARGAN':52250000,'VITAMINA':35800000,'COLLAGEN':35800000,'ANTIACNE':52250000,'HYALURONIC':52250000,'EXFOLIANT':52250000,'PORES':52250000}
  products=d['products'];self.assertEqual(len(products),10);self.assertEqual(len({p['draft_id'] for p in products}),10)
  for p in products:
   self.assertEqual(expected[p['draft_id'][6:]],p['source_price_irr'])
   self.assertEqual(p['reference_price_toman']*10,p['source_price_irr'])
   self.assertEqual(p['default_stock_requested'],5); self.assertFalse(p['stock_verified']);self.assertFalse(p['published']);self.assertIsNone(p['approved_sale_price_toman'])
   self.assertTrue((ROOT/p['image_path']).is_file())
   for key in ('category','description','usage','keywords','seo_title','seo_description','instagram_caption','telegram_caption','whatsapp_caption','youtube_caption','image_alt_text'):self.assertTrue(p[key])
  powder=next(p for p in products if p['draft_id'].endswith('POWDER4'))
  self.assertIn('خوراکی یا موضعی',powder['description'])
  for sku in ('ROSEMARY','ANTIACNE'):
   self.assertIn('قیمت متفاوت',next(p for p in products if p['draft_id'].endswith(sku))['price_note'])
