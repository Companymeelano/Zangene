import json,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class NewScreenshotCatalogTest(unittest.TestCase):
 def test_items_and_screenshot_prices(self):
  doc=json.loads((ROOT/'reconstruction/catalog/new_screenshot_drafts.json').read_text(encoding='utf8'))
  expected={'VITAMINC':52250000,'ALLINONE':52250000,'CAFFEINE':52250000,'BLACKGARLIC':52250000,'MICELLARDRY':24750000,'MICELLAROILY':35750000,'MICELLARNORMAL':35750000,'GATIEREPAIR':18117000,'GATIEANTIWRINKLE':19057000,'GATIEBODY':29887000}
  self.assertFalse(doc['auto_import']);self.assertFalse(doc['auto_publish']);self.assertEqual(len(doc['products']),10)
  seen=set()
  for p in doc['products']:
   key=p['draft_id'][6:];self.assertNotIn(key,seen);seen.add(key);self.assertEqual(expected[key],p['source_price_irr'])
   self.assertEqual(p['reference_price_toman']*10,p['source_price_irr']);self.assertEqual(p['default_stock_requested'],5)
   self.assertIsNone(p['approved_sale_price_toman']);self.assertFalse(p['published']);self.assertFalse(p['stock_verified'])
   self.assertTrue((ROOT/p['image_path']).is_file())
   for field in ('category','description','usage','seo_title','seo_description','keywords','instagram_caption','telegram_caption','whatsapp_caption','youtube_caption','image_alt_text'):self.assertTrue(p[field])
  self.assertEqual(seen,set(expected))
  self.assertIn('قیمت متفاوت',next(p for p in doc['products'] if p['draft_id'].endswith('GATIEBODY'))['price_note'])
