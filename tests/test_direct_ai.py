from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app'

class DirectStudioTests(unittest.TestCase):
    def test_direct_provider_wiring_and_consent(self):
        direct = (JAVA / 'DirectAiStudio.java').read_text(encoding='utf8')
        activity = (JAVA / 'MainActivity.java').read_text(encoding='utf8')
        self.assertIn('String[] providers = {"gemini", "openai", "groq"}', direct)
        self.assertIn('studio_provider_keys.json', direct)
        self.assertIn('inline_data', direct)
        self.assertIn('data:image/jpeg;base64,', direct)
        self.assertIn('DirectAiStudio.generate(MainActivity.this, name, brand, photo)', activity)
        self.assertIn('setPositiveButton("ارسال و تحلیل"', activity)
        self.assertIn('ProvidersFailedException', direct)
        self.assertIn('gemini-3.5-flash-lite', direct)
        self.assertIn('qwen/qwen3.8-27b', direct)
        self.assertNotIn('setRequestProperty("Authorization", key)', direct)

if __name__ == '__main__': unittest.main()
