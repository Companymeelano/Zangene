from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app'

class CredentialRecoveryTests(unittest.TestCase):
    def test_override_is_shared_and_failure_is_not_silently_ignored(self):
        store = (JAVA / 'StudioCredentials.java').read_text()
        cipher = (JAVA / 'CredentialCipher.java').read_text()
        direct = (JAVA / 'DirectAiStudio.java').read_text()
        media = (JAVA / 'AiMedia.java').read_text()
        activity = (JAVA / 'MainActivity.java').read_text()
        self.assertIn('AndroidKeyStore', store)
        self.assertIn('if (isOverrideActive(ctx, id))', store)
        self.assertIn('putInt("revision_" + id, CredentialRevision.CURRENT)', store)
        self.assertIn('کلید محلی قبلی حفظ شده ولی کلید جدید بسته فعال است', activity)
        self.assertIn('CredentialCipher.open(getKey(false), id, payload)', store)
        self.assertIn('setOverrideEnabled(Context ctx, String id, boolean enabled)', store)
        self.assertIn('putBoolean("disabled_" + id, !enabled)', store)
        self.assertIn('StudioCredentials.setOverrideEnabled(MainActivity.this, id, !localActive)', activity)
        self.assertNotIn('StudioCredentials.clear(MainActivity.this, id)', activity)
        self.assertIn('AES/GCM/NoPadding', cipher)
        self.assertIn('cipher.updateAAD', cipher)
        self.assertIn('StudioCredentials.get(context, "openrouter_free".equals(provider) ? "openrouter" : provider)', direct)
        self.assertIn('StudioCredentials.get(context, "cloudflare_account")', direct)
        self.assertIn('StudioCredentials.get(context, id)', media)
        self.assertIn('StudioCredentials.save(MainActivity.this, id, value)', activity)
        self.assertIn('input.setSaveEnabled(false)', activity)
        self.assertIn('افزودن یا تغییر کلید هوش مصنوعی', activity)
        self.assertIn('keyAction.setOnClickListener', activity)
        self.assertIn('کلید API این سرویس را اینجا وارد کنید', activity)
        self.assertIn('SOFT_INPUT_ADJUST_RESIZE', activity)
        self.assertIn('کلید قبلی همچنان داخل APK قابل استخراج است', activity)
        self.assertIn('Gemini، OpenAI یا Groq', activity)

if __name__ == '__main__': unittest.main()
