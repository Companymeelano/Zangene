import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

class ScreenshotProductsTests(unittest.TestCase):
    def test_all_are_unpublished_and_require_confirmation(self):
        doc = json.loads((ROOT/'reconstruction/catalog/screenshot_products.json').read_text(encoding='utf-8'))
        self.assertIs(doc['auto_import'], False)
        self.assertIs(doc['auto_publish'], False)
        self.assertEqual(len(doc['products']), 5)
        ids = set()
        for item in doc['products']:
            self.assertNotIn(item['draft_id'], ids); ids.add(item['draft_id'])
            self.assertIs(item['published'], False)
            self.assertIsNone(item['price_toman'])
            self.assertIsNone(item['stock'])
            self.assertTrue(item['needs_confirmation'])
            if item['image_path'] is not None:
                self.assertTrue((ROOT/item['image_path']).is_file())
        moringa = next(x for x in doc['products'] if x['draft_id'] == 'AS-REF-MORINGA')
        self.assertNotIn('شامپو', moringa['provisional_name'])
        self.assertIn('نوع', moringa['needs_confirmation'][0])
        purple = next(x for x in doc['products'] if x['draft_id'] == 'AS-REF-PURPLE')
        self.assertIsNone(purple['image_path'])

    def test_migration_does_not_assign_shampoo_price_to_moringa(self):
        source = (ROOT/'reconstruction/android/src/com/asiyestore/admin/app/ReferenceDraftMigration.java').read_text(encoding='utf-8')
        record = source.split('new Draft("AS-REF-MORINGA"', 1)[1].split('new Draft("AS-REF-PURPLE"', 1)[0]
        self.assertIn('"reference_moringa", 0L', record)
        self.assertNotIn('"شامپو مورینگا', record)
        self.assertIn('نوع محصول در تصویر قطعی نیست', record)
