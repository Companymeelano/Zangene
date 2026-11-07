import json,sqlite3,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
class SimpleCategoryTests(unittest.TestCase):
 def test_categories_and_brand_filters(self):
  items=[]
  for name in ('luxury_coin_155_drafts.json','luxury_coin_extra_drafts.json','new_screenshot_drafts.json'):
   items+=json.loads((ROOT/'reconstruction/catalog'/name).read_text(encoding='utf8'))['products']
  self.assertEqual(len(items),30)
  valid={'مراقبت پوست و بدن','مراقبت مو','آرایش','سایر'}
  self.assertEqual({p['broad_category'] for p in items},valid)
  with sqlite3.connect(':memory:') as db:
   db.execute('CREATE TABLE products (sku TEXT,category TEXT,brand TEXT,published INTEGER)')
   db.executemany('INSERT INTO products VALUES (?,?,?,?)',[(p['draft_id'],p['broad_category'],p['brand'],1) for p in items])
   self.assertEqual(db.execute('SELECT count(*) FROM products WHERE published=1 AND brand = ?',('گاتیو',)).fetchone()[0],3)
   self.assertEqual(db.execute('SELECT count(*) FROM products WHERE published=1 AND brand = ?',('لاکچری کوین',)).fetchone()[0],27)
   rows=db.execute('SELECT sku FROM products WHERE published=1 AND category = ?',('مراقبت مو',)).fetchall()
   self.assertEqual({r[0] for r in rows},{'LC155-ARGAN','LC155-ROSEMARY'})
   rows=db.execute('SELECT sku FROM products WHERE category = ?',('سایر',)).fetchall()
   self.assertEqual({r[0] for r in rows},{'LC155-POWDER4','LC155-BLACKGARLIC'})
   self.assertEqual(db.execute('SELECT count(*) FROM products WHERE brand = ?',("گاتیو' OR 1=1 --",)).fetchone()[0],0)
