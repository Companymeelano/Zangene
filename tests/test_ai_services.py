from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app'

class ServiceCoverage(unittest.TestCase):
    def test_specialists_and_privacy(self):
        order = (JAVA/'AiProviderOrder.java').read_text()
        direct = (JAVA/'DirectAiStudio.java').read_text()
        media = (JAVA/'AiMedia.java').read_text()
        video = (JAVA/'AiVideo.java').read_text()
        activity = (JAVA/'MainActivity.java').read_text()
        self.assertIn('IDS = {"gemini", "openai", "groq"}', order)
        self.assertIn('if (!AiProviderOrder.isActive(provider))', direct)
        self.assertIn('if (!AiProviderOrder.isActive(id))', media)
        self.assertIn('StudioCredentials.get(context, id)', media)
        self.assertIn('image != null && !AiProviderOrder.supportsImage(provider)', direct)
        self.assertIn('request(context, provider, key, facts, null, true)', direct)
        menu = activity.split('private void showFinalMediaOptions()', 1)[1].split('private void cancelCloudAnalysis()', 1)[0]
        self.assertNotIn('chooseMediaProvider()', menu)
        self.assertNotIn('showVideoMenu()', menu)
        self.assertIn('generateStudioImage()', menu)
        self.assertIn('تصویر تولیدشده ممکن است محصول واقعی را نشان ندهد', activity)
        self.assertIn('ثبت درخواست هزینه‌دار', activity)
        self.assertIn('setInstanceFollowRedirects(false)', video)
        self.assertNotIn('getErrorStream', media + video)
        self.assertIn('CreationFallback.preferredFirst(provider, new String[]{"fal", "deepai", "byteplus", "stability"})', activity)
        self.assertIn('CreationFallback.preferredFirst(provider, new String[]{"kling", "piapi"})', activity)
        self.assertIn('شرح شما به هر دو ممکن است ارسال شود', activity)
        self.assertIn('سرویس بعدی خودکار اجرا نشد', activity)

if __name__ == '__main__': unittest.main()
