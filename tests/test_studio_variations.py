from pathlib import Path
import unittest
ROOT = Path(__file__).resolve().parents[1] / 'android/app/src/main/java/com/asiyestore/admin/app'
class StudioVariations(unittest.TestCase):
    def test_theme_photo_and_explicit_selection(self):
        design = (ROOT/'LuxuryImageStudio.java').read_text()
        activity = (ROOT/'MainActivity.java').read_text()
        self.assertIn('int dark, int surface, int metal, int variant', design)
        self.assertIn('Math.floorMod(variant, 3)', design)
        self.assertIn('canvas.drawBitmap(source,null,dest,p)', design)
        self.assertNotIn('removeFlatBackground', design)
        self.assertIn('LuxuryImageStudio.render(photo,dark,surface,metal,variant)', activity)
        self.assertIn('aiStudioVariants.add(result)', activity)
        self.assertIn('aiStudioPath=path; persistStudioDraft()', activity)
        self.assertIn('studio_variants', activity)
        self.assertIn('!sourcePath.equals(aiSourcePath)', activity)
        self.assertIn('ساخت طرح دیگر', activity)
        self.assertIn('مشاهده و انتخاب طرح‌های ساخته‌شده', activity)
