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
        for provider in ['gemini','openai','groq','gapgpt','deepseek','openrouter','cerebras','huggingface','cloudflare','maxrouter']:
            self.assertIn('"'+provider+'"', order)
            self.assertIn('"'+provider+'"', direct)
        for provider in ['fal','deepai','byteplus','stability']:
            self.assertIn('"'+provider+'"', media)
        for provider in ['kling','piapi']:
            self.assertIn('"'+provider+'"', video)
        self.assertIn('image != null && !AiProviderOrder.supportsImage(provider)', direct)
        self.assertIn('request(context, provider, key, facts, null, true)', direct)
        self.assertIn('تصویر تولیدشده ممکن است محصول واقعی را نشان ندهد', activity)
        self.assertIn('ثبت درخواست هزینه‌دار', activity)
        self.assertIn('setInstanceFollowRedirects(false)', video)
        self.assertNotIn('getErrorStream', media + video)

if __name__ == '__main__': unittest.main()
