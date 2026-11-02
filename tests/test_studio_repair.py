import pathlib
import re
import sqlite3
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app'

class StudioRepairTest(unittest.TestCase):
    def test_product_schema_and_non_destructive_brand_migration(self):
        source = (JAVA / 'AsiyeDatabaseHelper.java').read_text(encoding='utf8')
        block = source.split('db.execSQL("CREATE TABLE products (" +', 1)[1].split(');', 1)[0]
        chunks = re.findall(r'"([^"\n]*)"', block)
        schema = 'CREATE TABLE products (' + ''.join(chunks)
        self.assertIn('brand TEXT NOT NULL DEFAULT', schema)
        db = sqlite3.connect(':memory:')
        db.execute(schema.replace("brand TEXT NOT NULL DEFAULT '',", ''))
        db.execute("INSERT INTO products (sku,ttac_serial,batch_code,expiry_label,name_fa,name_en,category,description,original_price,sale_price) VALUES (?,?,?,?,?,?,?,?,?,?)", ('old','', '', '', 'کالای قبلی','old','old','old',1,1))
        db.execute("ALTER TABLE products ADD COLUMN brand TEXT NOT NULL DEFAULT ''")
        self.assertEqual(db.execute('select name_fa, brand from products').fetchone(), ('کالای قبلی', ''))
        db.execute("update products set brand = ?", ('ساقدوش',))
        self.assertEqual(db.execute('select count(*) from products where brand like ?', ('%ساقدوش%',)).fetchone()[0], 1)

    def test_no_false_match_and_durable_image_path(self):
        studio = (JAVA / 'AiProductStudio.java').read_text(encoding='utf8')
        activity = (JAVA / 'MainActivity.java').read_text(encoding='utf8')
        analyze = studio.split('public static Analysis analyze(', 1)[1].split('public static Draft compose(', 1)[0]
        self.assertNotIn('KB', analyze)
        self.assertIn('سایر محصولات', analyze)
        self.assertIn('savedImagePath = !aiStudioPath.isEmpty() ? aiStudioPath : aiSourcePath', activity)
        self.assertIn('observeStudioInput(aiNameInput, true)', activity)
        self.assertIn('observeStudioInput(aiBrandInput, false)', activity)
        self.assertIn('restoreStudioDraft()', activity)
        self.assertIn('showOnlineImageDialog()', activity)
        self.assertIn('aiEditableField("کپشن اینستاگرام", "instagramCaption", true)', activity)

if __name__ == '__main__': unittest.main()
