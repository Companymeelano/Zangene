import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('biz_sitemap_inventory',ROOT/'tools/biz_sitemap_inventory.py')
m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)

class SitemapTests(TestCase):
    def test_canonical_unique_local_urls_only(self):
        with TemporaryDirectory() as d:
            p=Path(d)/'sitemap.xml'
            p.write_text('''<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
              <url><loc>https://www.bizmlm.ir/products/6/213/18952</loc></url>
              <url><loc>https://bizmlm.ir/products/6/213/18952/</loc></url>
              <url><loc>https://bizmlm.ir/products/6/125/17749</loc></url>
              <url><loc>https://bizmlm.ir.evil.example/products/6/125/1</loc></url>
              <url><loc>https://bizmlm.ir/products/6/125/2?price=1</loc></url>
              <url><loc>http://bizmlm.ir/products/6/125/3</loc></url>
            </urlset>''')
            rows=m.parse_sitemap(p)
            self.assertEqual([r[0] for r in rows], ['17749','18952'])
            self.assertEqual(rows[1][3], 'https://bizmlm.ir/products/6/213/18952')
    def test_fail_closed_for_xml_entities_and_index(self):
        with TemporaryDirectory() as d:
            p=Path(d)/'sitemap.xml'
            for xml in ('<!DOCTYPE x [<!ENTITY z "a">]><urlset/>', '<sitemapindex/>', '<bad'):
                p.write_text(xml)
                with self.assertRaises(ValueError): m.parse_sitemap(p)
