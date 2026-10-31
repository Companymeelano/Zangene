/**
 * ASIYE STORE — Smart Caption & SEO Studio (browser edition, v6.0)
 *
 * Mirrors the on-device engine shipped in the Android app so the merchant gets
 * identical, brand-consistent copy on web and mobile. Three inputs only:
 * product name, brand and (optionally) a photo. Everything is generated offline.
 */

const KB = [
    {
        brand: ['گاتیو', 'gatieau', 'gatio'],
        name: ['ضدلک', 'روشن', 'whitening', 'لک', 'تیرگی', '۱۵۰', '150'],
        form: 'کرم',
        title: 'کرم روشن‌کننده و ضدلک تخصصی گاتیو (۱۵۰ میل)',
        nameEn: 'GATIEAU Skin Whitening Cream',
        category: 'مراقبت تخصصی پوست',
        ingredients: 'سرامید، لیپوویت، کمپلکس ضدلک Hentowhite AF و ویتامین‌های A، C و E',
        usage: 'روزانه یک تا دو مرتبه روی پوست تمیز و خشک موضع مورد نظر ماساژ داده و همراه ضدآفتاب استفاده شود.',
        volume: '۱۵۰ میلی‌لیتر',
        price: 1550000,
        oldPrice: 2380000,
        concern: 'لک و تیرگی',
        hook: 'روشنایی که از خودِ پوست می‌آید، نه از لایه‌های پوشاننده',
        benefits: [
            'کاهش محسوس لک‌های سطحی و تیرگی نواحی مختلف پوست',
            'یکدست‌سازی تناژ پوست و ایجاد درخشش طبیعی',
            'تقویت سد دفاعی پوست با سرامید و لیپوویت',
            'بافت سبک و جذب سریع، بدون چربی یا سنگینی'
        ]
    },
    {
        brand: ['گاتیو', 'gatieau', 'gatio'],
        name: ['ضدچروک', 'لیفت', 'پپتید', 'جوان', '۵۰', '50'],
        form: 'کرم',
        title: 'کرم ضدچروک و لیفتینگ تخصصی گاتیو (۵۰ میل)',
        nameEn: 'GATIEAU Anti-Wrinkle Cream',
        category: 'مراقبت تخصصی پوست',
        ingredients: 'کمپلکس پپتید جوان‌ساز، هیالورونیک اسید، ویتامین E و نیاسینامید',
        usage: 'هر شب و صبح با حرکات رو به بالا روی صورت و گردن ماساژ داده شود.',
        volume: '۵۰ میلی‌لیتر',
        price: 1390000,
        oldPrice: 1610000,
        concern: 'چروک و لیفتینگ',
        hook: 'زمان را نمی‌توان متوقف کرد، اما می‌توان آن را هوشمندانه‌تر جا گذاشت',
        benefits: [
            'کاهش هدفمند خطوط پنجه کلاغی و خط اخم',
            'تحریک کلاژن‌سازی طبیعی و بازیابی قوام پوست',
            'لیفت ملایم خط فک و گردن بدون تزریق',
            'بافت ابریشمی و مناسب پوست‌های حساس'
        ]
    },
    {
        brand: ['لاکچری کوین', 'luxury coin', 'coin', 'کوین'],
        name: ['پنکک', 'پودر', 'کامپکت', 'مخملی', 'spf', '۱۰۴', '104'],
        form: 'پنکک',
        title: 'پنکک ضدآب و مخملی لاکچری کوین SPF 25',
        nameEn: 'LUXURY COIN Waterproof Compact Powder',
        category: 'میکاپ لوکس',
        ingredients: 'پودر معدنی میکرونیزه، کلاژن هیدرولیزشده، عصاره آلوئه‌ورا و ویتامین E',
        usage: 'با پد خشک برای پوشش روزانه یا با پد مرطوب جهت کاور مجلسی استفاده شود.',
        volume: 'شماره ۱۰۴ بژ طبیعی',
        price: 920000,
        oldPrice: 1210000,
        concern: 'پوشش و ماندگاری',
        hook: 'کاوری که در عکس‌ها هم همان‌قدر بی‌نقص دیده می‌شود',
        benefits: [
            'پوشانندگی یکدست و مات مخملی بدون سنگینی',
            'فرمول واترپروف مقاوم به رطوبت و تعریق',
            'ضدآفتاب SPF 25 برای استفاده روزانه',
            'بدون اکسید شدن روی پوست در طول روز'
        ]
    },
    {
        brand: ['هیرفرند', 'hair friend', 'hairfriend'],
        name: ['تونیک', 'ریزش', 'رویش', 'مو', '۳۰۰', '300'],
        form: 'تونیک',
        title: 'تونیک گیاهی تقویت و رویش مجدد مو هیرفرند (۳۰۰ میل)',
        nameEn: 'Hair Friend Herbal Hair Tonic',
        category: 'تقویت و درمان مو',
        ingredients: 'عصاره چای سبز، شلتوک برنج، برگ زیتون، همیشه‌بهار و رزماری',
        usage: 'روزانه چند پاف روی پوست سر تمیز اسپری و ۲ دقیقه ماساژ داده شود؛ بدون نیاز به آبکشی.',
        volume: '۳۰۰ میلی‌لیتر',
        price: 1560000,
        oldPrice: 1950000,
        concern: 'ریزش و تقویت مو',
        hook: 'ریزش را متوقف کنید، پیش از آن‌که آینه خبرش را به شما بدهد',
        benefits: [
            'کاهش محسوس ریزش مو در مصرف منظم',
            'ضخیم‌تر شدن تارهای نازک و افزایش تراکم',
            'تحریک گردش خون فولیکول و رویش موی جدید',
            'بدون چربی، بدون رنگ و بدون نیاز به آبکشی'
        ]
    },
    {
        brand: ['اوردینری', 'ordinary', 'the ordinary'],
        name: ['تونر', 'گلیکولیک', 'glycolic', 'منافذ', '۲۴۰', '240'],
        form: 'تونر',
        title: 'تونر لایه‌بردار گلیکولیک اسید ۷٪ اوردینری (۲۴۰ میل)',
        nameEn: 'The Ordinary Glycolic Acid 7% Toner',
        category: 'مراقبت تخصصی پوست',
        ingredients: 'گلیکولیک اسید ۷٪، عصاره ریشه جینسینگ، فلفل تاسمانی و آلوئه‌ورا',
        usage: 'شب‌ها با پد پنبه‌ای روی پوست تمیز (به‌جز دور چشم) کشیده شود و روزها ضدآفتاب استفاده گردد.',
        volume: '۲۴۰ میلی‌لیتر',
        price: 2890000,
        oldPrice: 3450000,
        concern: 'منافذ و کدری',
        hook: 'پوست شفاف، نتیجه‌ی صبرِ هوشمندانه است',
        benefits: [
            'پاکسازی عمقی منافذ باز و کاهش نقطه‌های سیاه',
            'رفع کدری و جای جوش با لایه‌برداری ملایم',
            'درخشش آینه‌ای بدون الکل و پارابن',
            'بافت آبکی سبک و pH متعادل ۳٫۶'
        ]
    },
    {
        brand: ['مورینگا', 'moringa', 'ویتابلا', 'vitabella', 'emo'],
        name: ['شامپو', 'درخت چای', 'tea tree', 'شوره', '۴۰۰', '400'],
        form: 'شامپو',
        title: 'پک تخصصی شامپو درخت چای ویتابلا و مورینگا امو',
        nameEn: 'VitaBella Tea Tree & Moringa EMO Duo',
        category: 'تقویت و درمان مو',
        ingredients: 'روغن خالص برگ درخت چای، روغن هسته مورینگا، آمینواسید و ویتامین E',
        usage: 'روی موی مرطوب ۲ دقیقه ماساژ و با آب ولرم آبکشی شود.',
        volume: 'پک دوتایی کامل',
        price: 680000,
        oldPrice: 840000,
        concern: 'چربی و شوره سر',
        hook: 'موی سالم، از یک شامپوی درست شروع می‌شود',
        benefits: [
            'تنظیم چربی کف سر و رفع شوره با درخت چای',
            'تغذیه و احیای ساقه مو با روغن مورینگا',
            'فاقد سولفات خشن؛ مناسب موی رنگ‌شده',
            'نرمی، درخشش و کاهش وزش مو'
        ]
    }
];

const normalize = (value) => (value || '')
    .toString()
    .toLowerCase()
    .replace(/ي/g, 'ی')
    .replace(/ك/g, 'ک')
    .replace(/\u200c/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();

function scoreEntry(entry, name, brand) {
    const combined = `${name} ${brand}`;
    let score = 0;
    if (entry.brand.some((token) => brand.includes(normalize(token)) || combined.includes(normalize(token)))) score += 45;
    const hits = entry.name.filter((token) => combined.includes(normalize(token))).length;
    score += Math.min(38, hits * 11);
    if (combined.includes(normalize(entry.form))) score += 14;
    if (combined.includes(normalize(entry.concern))) score += 8;
    return score;
}

export function analyzeProduct(rawName, rawBrand) {
    const name = normalize(rawName);
    const brand = normalize(rawBrand);
    let best = null;
    let bestScore = -1;
    KB.forEach((entry) => {
        const score = scoreEntry(entry, name, brand);
        if (score > bestScore) {
            bestScore = score;
            best = entry;
        }
    });

    if (!best || bestScore < 25) {
        return {
            confidence: 55,
            matched: null,
            title: rawName || 'محصول جدید مجموعه',
            rationale: 'الگوی ورودی به خانواده شناخته‌شده‌ای نزدیک نبود؛ دسته «مراقبت تخصصی پوست» به‌عنوان نزدیک‌ترین گزینه انتخاب شد.'
        };
    }
    return {
        confidence: Math.min(97, 34 + bestScore),
        matched: best,
        title: best.title,
        rationale: `برند «${rawBrand || '—'}» و ساختار نام با خانواده ${best.form} مجموعه تطبیق داده شد و کلیدواژه‌های فرمول (${best.concern}) تأیید شد.`
    };
}

const trim = (text, max) => {
    if (!text) return '';
    return text.length > max ? `${text.slice(0, max - 2)}…` : text;
};

export function composeListing(analysis, rawName, rawBrand) {
    const entry = analysis.matched;
    const brand = (rawBrand || 'ASIYE STORE').trim();
    const title = (rawName && rawName.trim().length > 2) ? rawName.trim() : (entry ? entry.title : analysis.title);

    const category = entry ? entry.category : 'مراقبت تخصصی پوست';
    const concern = entry ? entry.concern : 'مراقبت روزانه';
    const ingredients = entry ? entry.ingredients : 'فرمولاسیون تخصصی استاندارد با مواد اولیه درجه یک';
    const usage = entry ? entry.usage : 'طبق دستور مصرف درج‌شده روی بسته‌بندی استفاده شود.';
    const volume = entry ? entry.volume : 'نسخه اصلی';
    const price = entry ? entry.price : 990000;
    const oldPrice = entry ? entry.oldPrice : 1180000;
    const hook = entry ? entry.hook : 'انتخابی که تفاوت را در جزئیات نشان می‌دهد';
    const benefits = entry ? entry.benefits : [
        'کیفیت اورجینال با ضمانت اصالت مجموعه',
        'فرمولاسیون متعادل و مناسب استفاده روزانه',
        'بسته‌بندی اختصاصی و ارسال ایمن VIP',
        'همراه با مشاوره تخصصی مدیریت مجموعه'
    ];

    const description = `«${title}» با ${ingredients}؛ انتخابی برای ${concern} با ضمانت اصالت و مشاوره تخصصی مجموعه.`;

    const longDescription = [
        `✨ ${hook}`,
        `${title} یکی از انتخاب‌های منتخب مجموعه است که برای مشتریانی طراحی شده که به دنبال نتیجه واقعی و ماندگار هستند. فرمولاسیون ${ingredients} باعث می‌شود در کنار روتین روزانه شما، تغییری پیوسته و قابل مشاهده شکل بگیرد.`,
        `چرا این محصول؟ ${benefits[0]}؛ ${benefits[1]}. بافت سبک و سازگار آن اجازه می‌دهد بدون حساسیت یا سنگینی، از همان هفته‌های نخست تفاوت را حس کنید.`,
        `روش استفاده: ${usage}`,
        'نکته مدیریت مجموعه: مصرف منظم را قطع نکنید و در صورت پوست حساس، ابتدا تست موضعی انجام دهید. همراه هر سفارش، گواهی اصالت و بسته‌بندی اختصاصی ارسال می‌شود.'
    ].join('\n\n');

    const keywords = [
        `خرید ${title}`, title, `${brand} اصل`, brand, category, concern,
        'خرید اورجینال', `قیمت ${title}`, 'مشاوره تخصصی پوست و مو',
        'فروشگاه اینترنتی لوکس', 'آسیه استور', 'ASIYE STORE', volume
    ].filter((item, index, arr) => item && arr.indexOf(item) === index).join('، ');

    const seoTitle = trim(title, 60);
    const seoDescription = trim(
        `خرید ${title} اورجینال با ضمانت اصالت از مجموعه ASIYE STORE. مناسب ${concern}، همراه مشاوره تخصصی مدیریت مجموعه، ارسال سریع VIP و پرداخت ۴ قسط بدون کارمزد.`,
        158
    );

    const instagramCaption = [
        `✨ ${title}`,
        '',
        `${hook} 🤍`,
        '',
        ...benefits.slice(0, 4).map((benefit) => `• ${benefit}`),
        '',
        `📌 حجم: ${volume}  |  اصالت: ۱۰۰٪ اورجینال`,
        '💬 برای مشاوره و انتخاب درست، دایرکت پیام بدهید یا به مدیریت مجموعه پیام دهید.',
        '',
        `#آسیه_استور #${category.replace(/\s/g, '_')} #${concern.replace(/\s/g, '_')} #${brand.replace(/\s/g, '_')}`,
        '#asiyestore #skincare #luxurybeauty #instabeauty'
    ].join('\n');

    return {
        title,
        nameEn: entry ? entry.nameEn : `${brand} • SELECTION`,
        category,
        concern,
        ingredients,
        usage,
        volume,
        description,
        longDescription,
        benefits,
        keywords,
        seoTitle,
        seoDescription,
        instagramCaption,
        price,
        oldPrice
    };
}
