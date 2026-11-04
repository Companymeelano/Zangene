from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app'

class DirectStudioTests(unittest.TestCase):
    def test_direct_provider_wiring_and_consent(self):
        direct = (JAVA / 'DirectAiStudio.java').read_text(encoding='utf8')
        activity = (JAVA / 'MainActivity.java').read_text(encoding='utf8')
        self.assertIn('AiProviderOrder.forPreference(preferred)', direct)
        self.assertIn('StudioCredentials.get(context, "openrouter_free".equals(provider) ? "openrouter" : provider)', direct)
        self.assertIn('inline_data', direct)
        self.assertIn('data:image/jpeg;base64,', direct)
        self.assertIn('DirectAiStudio.generate(MainActivity.this, name, brand, photo, preferred, new DirectAiStudio.Cancellation()', activity)
        self.assertIn('primaryButton("ارسال و تحلیل"', activity)
        self.assertIn('ProvidersFailedException', direct)
        self.assertIn('gemini-3.5-flash-lite', direct)
        self.assertIn('qwen/qwen3.8-27b', direct)
        self.assertNotIn('setRequestProperty("Authorization", key)', direct)

    def test_health_probe_and_service_selection(self):
        direct = (JAVA / 'DirectAiStudio.java').read_text(encoding='utf8')
        activity = (JAVA / 'MainActivity.java').read_text(encoding='utf8')
        probe = direct.split('public static String probe(', 1)[1].split('private static String gemini(', 1)[0]
        self.assertIn('request(context, provider, key, facts, null, true)', probe)
        self.assertIn('validateOutput(result)', probe)
        self.assertIn('validateOutput(result)', probe)
        self.assertNotIn('aiSourceBitmap', probe)
        self.assertIn('DirectAiStudio.probe(app, provider)', activity)
        self.assertIn('startAiProbes(AiProviderOrder.IDS)', activity)
        self.assertIn('putString("preferred", ids[which])', activity)
        self.assertIn('getString("preferred", "gemini")', activity)
        self.assertIn('Gemini، OpenAI یا Groq', activity)
        self.assertIn('if (!AiProviderOrder.isActive(provider))', direct)

if __name__ == '__main__': unittest.main()
