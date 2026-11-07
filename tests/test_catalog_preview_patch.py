import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
SCRIPT=ROOT/'tools/prepare_catalog_preview.py'
APK=ROOT/'ASIYE_STORE_SCREENSHOT_POLISH_TEST.apk'

class CatalogPreviewPatchTest(unittest.TestCase):
    def test_wrong_apk_fails_before_mutating(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); fake=root/'bad.apk'; fake.write_bytes(b'wrong')
            result=subprocess.run(['python3',str(SCRIPT),str(root/'unpacked'),str(fake)],capture_output=True)
            self.assertNotEqual(result.returncode,0)
            self.assertFalse((root/'unpacked').exists())
    def test_inserts_hooks_and_disables_login(self):
        if not APK.is_file(): self.skipTest('Pinned demo APK unavailable')
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); (root/'smali/com/asiyestore/admin/app').mkdir(parents=True)
            (root/'res/drawable-nodpi').mkdir(parents=True)
            (root/'AndroidManifest.xml').write_text('<manifest package="com.asiyestore.admin.screenshotpolishtest"/>')
            (root/'apktool.yml').write_text('renameManifestPackage: null')
            (root/'smali/com/asiyestore/admin/app/AsiyeDatabaseHelper.smali').write_text('''.method public onCreate(Landroid/database/sqlite/SQLiteDatabase;)V
    invoke-direct {p0, p1}, Lcom/asiyestore/admin/app/AsiyeDatabaseHelper;->seedSignatureCatalog(Landroid/database/sqlite/SQLiteDatabase;)V
    invoke-direct {p0, p1}, Lcom/asiyestore/admin/app/AsiyeDatabaseHelper;->seedReferenceDrafts(Landroid/database/sqlite/SQLiteDatabase;)V
.end method''')
            (root/'smali/com/asiyestore/admin/app/MainActivity.smali').write_text('''.method private resolveProductImage(Ljava/lang/String;)I
    .locals 2
.end method
.method private showEditorialProductDialog(Lcom/asiyestore/admin/app/Models$Product;)V
    iget-object v6, v1, Lcom/asiyestore/admin/app/Models$Product;->benefits:Ljava/lang/String;
.end method
.method private showAdminLoginDialog()V
    bypass
.end method''')
            subprocess.run(['python3',str(SCRIPT),str(root),str(APK)],check=True)
            s=(root/'smali/com/asiyestore/admin/app/MainActivity.smali').read_text()
            self.assertIn('OfflineContentPreview;->addContentButton', s); self.assertNotIn('bypass',s); self.assertIn('OfflineCatalogPreview;->imageResource',s)
            db=(root/'smali/com/asiyestore/admin/app/AsiyeDatabaseHelper.smali').read_text()
            self.assertIn('OfflineCatalogPreview;->seedPreview',db)
            self.assertNotIn('seedSignatureCatalog',db); self.assertNotIn('seedReferenceDrafts',db)
            self.assertEqual(len(list((root/'res/drawable-nodpi').glob('lc155_*.jpg'))),30)
            self.assertIn('com.asiyestore.admin.catalog30preview',(root/'AndroidManifest.xml').read_text())
