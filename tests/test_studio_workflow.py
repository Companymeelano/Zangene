from pathlib import Path
import unittest
ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app'

class WorkflowTest(unittest.TestCase):
    def test_ai_first_and_media_last(self):
        activity = (JAVA / 'MainActivity.java').read_text(encoding='utf8')
        inputs = activity.split('private void renderAiInputs()',1)[1].split('// ── STEP 2',1)[0]
        draft = activity.split('private void renderAiDraft()',1)[1].split('private View aiEditableField(',1)[0]
        self.assertLess(inputs.index('تحلیل و تکمیل مشخصات با هوش مصنوعی'), inputs.index('پیش‌نویس محلی بدون تحلیل عکس'))
        self.assertLess(draft.index('aiEditableField("کپشن اینستاگرام"'), draft.index('مرحلهٔ پایانی · تصویرسازی اختیاری'))
        self.assertLess(draft.index('مرحلهٔ پایانی · تصویرسازی اختیاری'), draft.index('ذخیره کالا در ویترین فروشگاه'))
        self.assertNotIn('chooseMediaProvider()', draft)
        self.assertIn('showFinalMediaOptions()', draft)
        self.assertIn('انتخاب یا تغییر عکس از گالری', draft)
        self.assertIn('if (aiBusy)', inputs)
        self.assertIn('cancelCloudAnalysis()', inputs)
        self.assertIn('درخواست آغازشده ممکن است همچنان هزینه داشته باشد', activity)
        self.assertIn('generation != aiGeneration', activity)

if __name__ == '__main__': unittest.main()
