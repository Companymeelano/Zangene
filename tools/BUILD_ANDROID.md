# ساخت و امضای APK — خط تولید تکرارپذیر (v6.3)

این سند دقیقاً همان زنجیره‌ای است که برای ساخت `ASIYE_STORE_LUXURY_FINAL.apk` اجرا می‌شود؛
بدون Gradle، بدون Android Studio و فقط با ابزارهای خط فرمان.

## ۱. ابزارهای لازم

| ابزار | منبع | نقش |
|---|---|---|
| JRE ۲۱+ (اینجا Temurin 25) | `pip install jdk4py` | اجرای ECJ، D8 و apksigner |
| `android.jar` (API 34) | مخزن `HashShin/H2APK` → `tools/android.jar` | کلاس‌های پلتفرم برای کامپایل |
| `d8.jar` | همان مخزن → `tools/d8.jar` | تبدیل class → dex |
| `apksigner.jar` | همان مخزن → `tools/apksigner.jar` | امضای v1/v2/v3 |
| `aapt2` | بستهٔ npm `aaptjs3` → `bin/x64/linux/aapt2` | کامپایل و لینک منابع |
| ECJ | بستهٔ npm `@vscjava/java-language-server` → `server/plugins/org.eclipse.jdt.core.compiler.batch_*.jar` | کامپایلر جاوا |

## ۲. مراحل

```bash
ROOT=android/app/src/main
B=/tmp/apk-build

# الف) منابع
aapt2 compile --dir $ROOT/res -o $B/compiled-res/res.zip
aapt2 link -I android.jar --manifest $ROOT/AndroidManifest.xml --java $B/gen \
  --min-sdk-version 24 --target-sdk-version 34 \
  --version-code 630 --version-name "6.3.0-atelier-icons" \
  -o $B/base.apk $B/compiled-res/res.zip

# ب) کامپایل (بدون lambda؛ کلاس‌های ناشناس استفاده می‌شود تا به core-lambda-stubs نیاز نباشد)
java -jar ecj.jar -source 8 -target 8 -encoding UTF-8 -proc:none \
  -bootclasspath android.jar -d $B/classes $(find $ROOT/java $B/gen -name "*.java")

# ج) dex
java -cp d8.jar com.android.tools.r8.D8 --release --min-api 24 \
  --lib android.jar --output $B/dex $(find $B/classes -name "*.class")

# د) ادغام ZIP با ترازبندی ۴ بایتی (classes.dex فشرده، بقیه STORED)
python3 tools/pack_apk.py $B/base.apk $B/dex/classes.dex $B/aligned.apk

# ه) امضا
apksigner sign --ks ~/.asiye/asiye-release.jks --ks-key-alias asiye_signature \
  --ks-pass pass:Asiye2026! --key-pass pass:Asiye2026! \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  ASIYE_STORE_LUXURY_FINAL.apk
rm -f ASIYE_STORE_LUXURY_FINAL.apk.idsig
```

## ۳. کلید امضا

- مسیر محلی نگهداری: `~/.asiye/asiye-release.jks` (بیرون از مخزن عمومی قرار می‌گیرد)
- alias: `asiye_signature` · الگوریتم: RSA 2048 · اعتبار: ۳۰ سال
- اثر انگشت گواهی نسخهٔ ۶.۳: `505b7a69d37eccb1651f099e8e35589855b215db3c9b2ff83f072a48b589bf57`
- **هشدار:** نسخه‌های ۴.۰ تا ۶.۲ با گواهی `68082324…2a146` امضا شده‌اند؛ آن کلید در بازنشانی محیط ساخت از بین رفت.
  بنابراین نصب ۶.۳ روی نسخه‌های قبلی نیاز به حذف یک‌بارهٔ اپ دارد. از این نسخه به بعد، کلید باید همیشه
  همراه داشته و پشتیبان‌گیری شود تا ارتقاهای آینده درجا انجام شوند.

## ۴. بررسی سلامت خروجی

```bash
apksigner verify --print-certs ASIYE_STORE_LUXURY_FINAL.apk
aapt2 dump badging ASIYE_STORE_LUXURY_FINAL.apk | head -1
```
