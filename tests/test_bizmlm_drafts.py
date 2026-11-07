import json
import unittest
from pathlib import Path
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[1]

class BizDraftTests(unittest.TestCase):
    def test_drafts_remain_unpublished_traceable_and_not_imported(self):
        doc = json.loads((ROOT/'reconstruction/catalog/bizmlm_drafts.json').read_text(encoding='utf-8'))
        self.assertEqual(doc['status'], 'draft_only')
        self.assertIs(doc['auto_import'], False)
        self.assertIs(doc['auto_publish'], False)
        self.assertTrue((ROOT/doc['visual_background']).is_file())
        self.assertEqual(len(doc['products']), 5)
        skus=set()
        for p in doc['products']:
            self.assertNotIn(p['source_id'], skus); skus.add(p['source_id'])
            self.assertIs(p['published'], False)
            self.assertIsNone(p['stock'])
            self.assertIsNone(p['approved_sale_price_toman'])
            self.assertGreater(p['indexed_price_irr'], 0)
            self.assertEqual(p['indexed_price_irr'], p['reference_price_toman']*10)
            url=urlsplit(p['price_source_url'])
            self.assertEqual((url.scheme,url.hostname), ('https','bizmlm.ir'))
            self.assertTrue(url.path.endswith('/'+p['source_id']))
            self.assertIn('تأیید نشده', p['price_evidence'])
            self.assertIn(p['image_status'], ('third_party_reference_not_licensed_or_verified', 'reference_not_licensed_or_verified'))
            for field in ('name', 'short_description', 'description', 'caption', 'keywords'):
                self.assertTrue(p[field], field)
