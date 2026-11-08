# درخشان | نسخه مستقل عمده‌فروشی

وب‌سایت مستقل آجیل و خشکبار در پوشه [`derakhshan-wholesale`](derakhshan-wholesale/) قرار دارد. فایل فشرده پروژه قبلی بدون تغییر نگه داشته شده است.

برای اجرای نسخه درخشان با Python 3:

```sh
npm run dev
```

راهنمای کامل و محدودیت‌های نسخه در [`derakhshan-wholesale/README.md`](derakhshan-wholesale/README.md) آمده است. کد پوسته و راهنمای بازسازی برنامه اندروید در [`derakhshan-android/`](derakhshan-android/) قرار دارد.

## آزمون رابط موبایل

برای اجرای آزمون‌های خودکار (عرض‌های مختلف، لمس، پیمایش، فرم و دسترس‌پذیری)، Node.js را نصب و از ریشه مخزن اجرا کنید:

```sh
npm ci
npx playwright-core install chromium
npm run dev        # در یک ترمینال جداگانه
npm run test:ui
```

می‌توان نشانی سرور را با متغیر `BASE_URL` و مسیر Chromium موجود را با `CHROMIUM_PATH` مشخص کرد. این آزمون‌ها مرورگری‌اند و جای آزمون نصب APK روی گوشی واقعی را نمی‌گیرند.
