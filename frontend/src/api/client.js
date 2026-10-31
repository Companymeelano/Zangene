/* ASIYE STORE — IMPERIAL ECOSYSTEM CLIENT v3.0
   Implements:
   1. AI Skin Diagnostic & Personalized AM/PM Ritual Bundler
   2. Virtual Couture Lip Shade Try-On Studio (Live Canvas)
   3. Digital Fragrance Sommelier & Olfactory Pyramid
   4. Anti-Counterfeit TTAC & Hologram Certificate Verifier
   5. 4-Provider BNPL Hub (Snapp Pay, Tara, Digipay, AzkiVam) + Gold Cashback Wallet
   6. White-Glove Velvet Gift Concierge + Scented Tissue + Cold-Chain Thermal Delivery
   7. Smart Replenishment Depletion Tracker
*/

const PERSIAN_DIGITS = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

function toPersianDigits(value) {
    return String(value ?? '').replace(/\d/g, (d) => PERSIAN_DIGITS[Number(d)]);
}

function formatToman(amount) {
    const num = Math.max(0, Math.round(Number(amount) || 0));
    return toPersianDigits(num.toLocaleString('en-US')) + ' تومان';
}

function escapeHtml(unsafe) {
    return String(unsafe ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function resolveAssetPath(rawPath) {
    if (!rawPath) return '../images/app_icon_dark.png';
    if (rawPath.startsWith('http://') || rawPath.startsWith('https://')) return rawPath;
    const clean = rawPath.replace(/^\/+/, '');
    const inFrontendSubdir = window.location.pathname.includes('/frontend/');
    return inFrontendSubdir ? `../${clean}` : `/${clean}`;
}

const IMPERIAL_CATALOG = [
    {
        id: 1,
        sku: 'AS-SRM-001',
        ttac_serial: 'AS-AUTH-2026-9901',
        batch_code: 'LOT-24K-881',
        name_fa: 'سرم طلایی احیاکننده پوست ۲۴ عیار',
        name_en: '24K Golden Revitalizing Cellular Serum',
        category_id: 1,
        category: { name_fa: 'مراقبت تخصصی پوست' },
        original_price: 2300000,
        sale_price: 1850000,
        discount_percent: 20,
        cold_chain: true,
        description: 'سرم سلولی حاوی ورقه‌های طلای کلوئیدی ۲۴ عیار، هیالورونیک اسید چندمولکولی و عصاره رز دمشقی برای لیفتینگ و درخشش کریستالی.',
        images: [{ path: 'images/product_serum_luxury.png' }],
        variants: [{ id: 101, size: '۳۰ میلی‌لیتر', price_offset: 0, inventory: { quantity: 28 } }]
    },
    {
        id: 2,
        sku: 'AS-CRM-108',
        ttac_serial: 'AS-AUTH-2026-9902',
        batch_code: 'LOT-CAV-502',
        name_fa: 'کرم شب خاویار سیاه و پپتید طلایی',
        name_en: 'Imperial Caviar & Gold Peptide Night Crème',
        category_id: 1,
        category: { name_fa: 'مراقبت تخصصی پوست' },
        original_price: 3100000,
        sale_price: 2690000,
        discount_percent: 13,
        cold_chain: true,
        description: 'کرم بازسازی‌کننده عمیق شبانه با عصاره خاویار و کمپلکس مس-پپتید برای استحکام سد دفاعی و جوانسازی ابریشمی پوست.',
        images: [{ path: 'images/product_caviar_cream_luxury.jpg' }],
        variants: [{ id: 201, size: '۵۰ میلی‌لیتر', price_offset: 0, inventory: { quantity: 14 } }]
    },
    {
        id: 3,
        sku: 'AS-LIP-042',
        ttac_serial: 'AS-AUTH-2026-9903',
        batch_code: 'LOT-VLV-319',
        name_fa: 'رژ لب مخملی مات اختصاصی ASIYE Couture',
        name_en: 'Haute Couture Velvet Matte Lipstick',
        category_id: 2,
        category: { name_fa: 'آرایش کوتور لب و چهره' },
        original_price: 520000,
        sale_price: 450000,
        discount_percent: 13,
        cold_chain: false,
        description: 'فرمولاسیون مخملی ۱۲ ساعته غنی‌شده با شی‌باتر ارگانیک و رنگدانه‌های معدنی خالص بدون ایجاد خشکی.',
        images: [{ path: 'images/product_lipstick_luxury.png' }],
        variants: [{ id: 301, color: 'شامپاینی نود', price_offset: 0, inventory: { quantity: 24 } }]
    },
    {
        id: 4,
        sku: 'AS-PRF-999',
        ttac_serial: 'TTAC-8849201140',
        batch_code: 'LOT-PAR-999',
        name_fa: 'ادوپرفیوم سلطنتی ASIYE Signature',
        name_en: 'ASIYE Signature Eau De Parfum Intense',
        category_id: 3,
        category: { name_fa: 'عطر و نیش پرفیوم' },
        original_price: 2450000,
        sale_price: 2100000,
        discount_percent: 14,
        cold_chain: true,
        description: 'سمفونی مسحورکننده‌ای از رز دمشقی، زعفران سلطنتی، کهربای طلایی و چوب صندل گرم با ماندگاری ۲۴ ساعته.',
        images: [{ path: 'images/product_perfume_luxury.png' }],
        variants: [{ id: 401, size: '۱۰۰ میلی‌لیتر', price_offset: 0, inventory: { quantity: 19 } }]
    },
    {
        id: 5,
        sku: 'AS-ELX-305',
        ttac_serial: 'AS-AUTH-2026-9905',
        batch_code: 'LOT-ELX-712',
        name_fa: 'الکسیر ابریشم و طلای ۲۴ عیار مو',
        name_en: 'L’Elixir D’Exception 24K Gold Hair Oil',
        category_id: 4,
        category: { name_fa: 'مراقبت تخصصی مو' },
        original_price: 1580000,
        sale_price: 1290000,
        discount_percent: 18,
        cold_chain: false,
        description: 'الکسیر کریستالی حاوی ذرات معلق طلای ۲۴ عیار و روغن آرگان مراکشی برای احیای فوری و درخشش آینه‌ای ساقه مو.',
        images: [{ path: 'images/product_hair_elixir_luxury.jpg' }],
        variants: [{ id: 501, size: '۱۰۰ میلی‌لیتر', price_offset: 0, inventory: { quantity: 22 } }]
    },
    {
        id: 6,
        sku: 'AS-DSC-777',
        ttac_serial: 'AS-AUTH-2026-9906',
        batch_code: 'LOT-VIP-777',
        name_fa: 'کافره مخمل هدیه VIP و پک تستر عطر Discovery',
        name_en: 'VIP Velvet Coffret & 3x5ml Fragrance Discovery Set',
        category_id: 3,
        category: { name_fa: 'عطر و نیش پرفیوم' },
        original_price: 980000,
        sale_price: 790000,
        discount_percent: 19,
        cold_chain: true,
        description: 'شامل ۳ ویال کریستالی عطرهای اختصاصی در جعبه مخمل مشکی با مهر موم طلایی AS + کارت هدیه ۱۰۰٪ بازگشت وجه برای خرید نسخه ۱۰۰ میل.',
        images: [{ path: 'images/vip_gift_box_luxury.jpg' }],
        variants: [{ id: 601, size: '۳ × ۵ میلی‌لیتر + جعبه مخمل', price_offset: 0, inventory: { quantity: 16 } }]
    }
];

const COUTURE_LIP_SHADES = [
    { name: 'شامپاینی نود (Champagne Nude)', hex: '#C68B73', code: 'NUDE-01' },
    { name: 'رز بژ مخملی (Velvet Rose Beige)', hex: '#B96A6C', code: 'ROSE-02' },
    { name: 'یاقوت پاریسی (Parisian Crimson)', hex: '#8E1E2B', code: 'CRIMSON-03' },
    { name: 'توت سلطنتی (Imperial Berry)', hex: '#631F36', code: 'BERRY-04' },
    { name: 'تراکوتا ابریشمی (Silk Terracotta)', hex: '#A94B39', code: 'TERRA-05' },
    { name: 'هلویی طلایی (Soft Peach Gold)', hex: '#CE7F6A', code: 'PEACH-06' }
];

const SKIN_TONES = [
    { label: 'بلوری روشن (Fair Ivory)', hex: '#F3DDD0' },
    { label: 'گندمی طبیعی (Warm Beige)', hex: '#DFB99B' },
    { label: 'برنزه طلایی (Golden Bronze)', hex: '#B88463' }
];

class AsiyeImperialClient {
    constructor() {
        this.cart = JSON.parse(localStorage.getItem('asiye_imperial_cart') || '[]');
        this.walletBalance = Number(localStorage.getItem('asiye_gold_wallet') ?? 250000);
        this.loyaltyPoints = Number(localStorage.getItem('asiye_vip_points') ?? 1480);
        this.activeCoupon = localStorage.getItem('asiye_coupon') || '';
        this.useWallet = true;
        this.vipGiftBox = true;
        this.coldChain = true;
        this.scentedTissue = 'رز دمشقی و عود سلطنتی';
        this.giftMessage = '';
        this.bnplProvider = 'snapp_pay';
        this.products = [...IMPERIAL_CATALOG];
        this.selectedCategory = 'all';
        this.searchQuery = '';
        this.sortMode = 'newest';
        this.selectedShadeIndex = 0;
        this.selectedSkinToneIndex = 0;
    }

    showToast(msg) {
        let el = document.getElementById('asiye-toast');
        if (!el) {
            el = document.createElement('div');
            el.id = 'asiye-toast';
            el.className = 'luxury-toast';
            document.body.appendChild(el);
        }
        el.textContent = msg;
        el.classList.add('show');
        clearTimeout(this._t);
        this._t = setTimeout(() => el.classList.remove('show'), 3600);
    }

    init() {
        this.renderCatalog();
        this.updateHeaderBadges();
        this.renderVirtualLipCanvas();
        this.renderShadeControls();
        this.runSkinAnalysis();
        this.runFragranceMatcher();
    }

    updateHeaderBadges() {
        const countEl = document.getElementById('cart-count-badge');
        if (countEl) {
            const totalQty = this.cart.reduce((s, i) => s + i.quantity, 0);
            countEl.textContent = toPersianDigits(totalQty);
        }
        const walletEl = document.getElementById('header-wallet-balance');
        if (walletEl) {
            walletEl.textContent = formatToman(this.walletBalance);
        }
        const clubWalletEl = document.getElementById('club-wallet-balance');
        if (clubWalletEl) {
            clubWalletEl.textContent = formatToman(this.walletBalance);
        }
        const clubPtsEl = document.getElementById('club-loyalty-points');
        if (clubPtsEl) {
            clubPtsEl.textContent = toPersianDigits(this.loyaltyPoints.toLocaleString('en-US')) + ' امتیاز الماس سیاه';
        }
    }

    getFilteredProducts() {
        let list = [...this.products];
        if (this.selectedCategory !== 'all') {
            list = list.filter((p) => String(p.category_id) === String(this.selectedCategory));
        }
        if (this.searchQuery.trim()) {
            const q = this.searchQuery.trim().toLowerCase();
            list = list.filter((p) =>
                p.name_fa.toLowerCase().includes(q) ||
                p.name_en.toLowerCase().includes(q) ||
                p.sku.toLowerCase().includes(q)
            );
        }
        if (this.sortMode === 'price_asc') list.sort((a, b) => a.sale_price - b.sale_price);
        else if (this.sortMode === 'price_desc') list.sort((a, b) => b.sale_price - a.sale_price);
        else if (this.sortMode === 'discount') list.sort((a, b) => b.discount_percent - a.discount_percent);
        return list;
    }

    renderCatalog() {
        const grid = document.getElementById('product-grid');
        if (!grid) return;
        const items = this.getFilteredProducts();
        if (!items.length) {
            grid.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:3rem; background:#fff; border-radius:16px;">محصولی مطابق فیلتر انتخابی یافت نشد.</div>`;
            return;
        }
        grid.innerHTML = items.map((p) => {
            const img = resolveAssetPath(p.images[0]?.path);
            const perMonth = Math.round(p.sale_price / 4);
            const cashback = Math.round(p.sale_price * 0.05);
            return `
                <article class="product-card">
                    ${p.discount_percent > 0 ? `<span class="discount-badge">${toPersianDigits(p.discount_percent)}٪ امتیاز ویژه</span>` : ''}
                    <span class="ttac-badge">✓ اصالت TTAC ${p.cold_chain ? '• زنجیره سرد' : ''}</span>
                    <div style="overflow:hidden;">
                        <img src="${escapeHtml(img)}" alt="${escapeHtml(p.name_fa)}" class="product-image" loading="lazy">
                    </div>
                    <div class="product-info">
                        <div class="product-category-tag">${escapeHtml(p.category.name_fa)} | کد: ${escapeHtml(p.sku)}</div>
                        <h3 class="product-title">${escapeHtml(p.name_fa)}</h3>
                        <p style="font-size:0.85rem; color:#5C564F; margin:0 0 0.7rem;">${escapeHtml(p.description)}</p>
                        <div class="product-price-row">
                            <span class="product-price">${formatToman(p.sale_price)}</span>
                            ${p.original_price > p.sale_price ? `<span class="product-discount">${formatToman(p.original_price)}</span>` : ''}
                        </div>
                        <div class="installment-pill">
                            ۴ قسط بدون کارمزد (اسنپ‌پی / تارا / دیجی‌پی): <strong>${formatToman(perMonth)}</strong><br>
                            <span style="color:#1F6F50; font-weight:700;">+ ${formatToman(cashback)} کش‌بک آنی کیف پول طلایی</span>
                        </div>
                        <button class="btn-luxury" style="width:100%; margin-top:1rem;" data-add-product="${p.id}">
                            افزودن به سبد تشریفاتی
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    addProduct(productId, customLabel = null) {
        const product = this.products.find((p) => p.id === Number(productId));
        if (!product) return;
        const key = `${product.id}:${customLabel || 'standard'}`;
        const existing = this.cart.find((c) => c.key === key);
        if (existing) {
            existing.quantity += 1;
        } else {
            this.cart.push({
                key,
                product_id: product.id,
                name_fa: product.name_fa,
                variant_label: customLabel || product.variants[0]?.size || product.variants[0]?.color || 'نسخه بوتیک',
                unit_price: product.sale_price,
                image: resolveAssetPath(product.images[0]?.path),
                quantity: 1
            });
        }
        this.saveState();
        this.showToast(`«${product.name_fa}» به سبد تشریفاتی شما افزوده شد.`);
    }

    saveState() {
        localStorage.setItem('asiye_imperial_cart', JSON.stringify(this.cart));
        localStorage.setItem('asiye_gold_wallet', String(this.walletBalance));
        localStorage.setItem('asiye_vip_points', String(this.loyaltyPoints));
        this.updateHeaderBadges();
    }

    /* === 1. AI SKIN ANALYZER === */
    runSkinAnalysis() {
        const skinType = document.getElementById('ai-skin-type')?.value || 'dry';
        const concern = document.getElementById('ai-skin-concern')?.value || 'aging';
        const out = document.getElementById('ai-skin-result');
        if (!out) return;

        const hydration = skinType === 'dry' ? 61 : skinType === 'combination' ? 75 : 84;
        const firmness = concern === 'aging' ? 64 : concern === 'dullness' ? 73 : 82;
        const radiance = concern === 'dullness' ? 63 : 87;

        const bundleTotal = Math.round((1850000 + 2690000) * 0.88);

        out.innerHTML = `
            <div style="font-weight:800; color:#E2C28E; font-size:1.05rem; margin-bottom:0.8rem;">
                نتیجه اسکن هوشمند درماتولوژی ASIYE AI:
            </div>
            <div style="margin-bottom:0.65rem;">
                <div style="display:flex; justify-content:space-between; font-size:0.84rem;"><span>تراز رطوبت بین‌سلولی</span><span>${toPersianDigits(hydration)}٪</span></div>
                <div class="score-bar-track"><div class="score-bar-fill" style="width:${hydration}%"></div></div>
            </div>
            <div style="margin-bottom:0.65rem;">
                <div style="display:flex; justify-content:space-between; font-size:0.84rem;"><span>تراکم کلاژن و استحکام</span><span>${toPersianDigits(firmness)}٪</span></div>
                <div class="score-bar-track"><div class="score-bar-fill" style="width:${firmness}%"></div></div>
            </div>
            <div style="margin-bottom:1rem;">
                <div style="display:flex; justify-content:space-between; font-size:0.84rem;"><span>شفافیت و سد دفاعی پوست</span><span>${toPersianDigits(radiance)}٪</span></div>
                <div class="score-bar-track"><div class="score-bar-fill" style="width:${radiance}%"></div></div>
            </div>
            <div style="background:rgba(198,161,108,0.12); border:1px solid rgba(198,161,108,0.4); border-radius:10px; padding:0.9rem; font-size:0.86rem; margin-bottom:1rem;">
                <div>☀️ <strong>روتین صبح:</strong> سرم طلایی احیاکننده ۲۴ عیار (لیفت و آبرسانی عمقی)</div>
                <div style="margin-top:0.35rem;">🌙 <strong>روتین شب:</strong> کرم شب خاویار سیاه و پپتید طلایی (بازسازی سلولی)</div>
            </div>
            <button class="btn-luxury" style="width:100%;" id="add-ai-ritual-btn">
                افزودن پک کامل روتین صبح و شب با ۱۲٪ تخفیف (${formatToman(bundleTotal)})
            </button>
        `;
    }

    /* === 2. VIRTUAL COUTURE SHADE TRY-ON STUDIO === */
    renderShadeControls() {
        const swatchBox = document.getElementById('shade-swatches');
        const toneBox = document.getElementById('skin-tone-buttons');
        if (!swatchBox || !toneBox) return;

        swatchBox.innerHTML = COUTURE_LIP_SHADES.map((s, idx) => `
            <button class="shade-swatch ${idx === this.selectedShadeIndex ? 'active' : ''}"
                style="background:${s.hex};"
                title="${escapeHtml(s.name)}"
                data-shade-idx="${idx}"></button>
        `).join('');

        toneBox.innerHTML = SKIN_TONES.map((t, idx) => `
            <button class="chip ${idx === this.selectedSkinToneIndex ? 'active' : ''}"
                data-tone-idx="${idx}">${escapeHtml(t.label)}</button>
        `).join('');

        const label = document.getElementById('selected-shade-label');
        if (label) {
            const s = COUTURE_LIP_SHADES[this.selectedShadeIndex];
            label.textContent = `شید انتخابی: ${s.name} — کد ${s.code}`;
        }
    }

    renderVirtualLipCanvas() {
        const canvas = document.getElementById('virtual-lip-canvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const w = canvas.width;
        const h = canvas.height;

        const skin = SKIN_TONES[this.selectedSkinToneIndex];
        const shade = COUTURE_LIP_SHADES[this.selectedShadeIndex];

        // Soft studio skin complexion backdrop
        const bgGrad = ctx.createRadialGradient(w / 2, h / 2, 20, w / 2, h / 2, w * 0.65);
        bgGrad.addColorStop(0, skin.hex);
        bgGrad.addColorStop(1, '#A97E63');
        ctx.fillStyle = bgGrad;
        ctx.fillRect(0, 0, w, h);

        // Subtle philtrum glow above cupid's bow
        ctx.strokeStyle = 'rgba(255,255,255,0.28)';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(w * 0.47, h * 0.16);
        ctx.quadraticCurveTo(w * 0.48, h * 0.34, w * 0.45, h * 0.38);
        ctx.moveTo(w * 0.53, h * 0.16);
        ctx.quadraticCurveTo(w * 0.52, h * 0.34, w * 0.55, h * 0.38);
        ctx.stroke();

        // Upper Couture Lip (Cupid's Bow)
        ctx.save();
        ctx.shadowColor = 'rgba(0,0,0,0.22)';
        ctx.shadowBlur = 14;
        ctx.shadowOffsetY = 6;

        ctx.fillStyle = shade.hex;
        ctx.beginPath();
        ctx.moveTo(w * 0.16, h * 0.52);
        ctx.bezierCurveTo(w * 0.28, h * 0.34, w * 0.42, h * 0.32, w * 0.50, h * 0.40);
        ctx.bezierCurveTo(w * 0.58, h * 0.32, w * 0.72, h * 0.34, w * 0.84, h * 0.52);
        ctx.quadraticCurveTo(w * 0.50, h * 0.57, w * 0.16, h * 0.52);
        ctx.closePath();
        ctx.fill();

        // Lower Plush Velvet Lip
        ctx.beginPath();
        ctx.moveTo(w * 0.16, h * 0.52);
        ctx.quadraticCurveTo(w * 0.50, h * 0.57, w * 0.84, h * 0.52);
        ctx.bezierCurveTo(w * 0.75, h * 0.79, w * 0.25, h * 0.79, w * 0.16, h * 0.52);
        ctx.closePath();
        ctx.fill();
        ctx.restore();

        // Velvet Satin Specular Sheen on Lower Lip
        const sheen = ctx.createLinearGradient(w * 0.35, h * 0.58, w * 0.65, h * 0.68);
        sheen.addColorStop(0, 'rgba(255,255,255,0)');
        sheen.addColorStop(0.5, 'rgba(255,245,238,0.32)');
        sheen.addColorStop(1, 'rgba(255,255,255,0)');
        ctx.fillStyle = sheen;
        ctx.beginPath();
        ctx.ellipse(w * 0.5, h * 0.63, w * 0.18, h * 0.045, 0, 0, Math.PI * 2);
        ctx.fill();

        // Center Lip Parting Line
        ctx.strokeStyle = 'rgba(25, 8, 12, 0.38)';
        ctx.lineWidth = 2.2;
        ctx.beginPath();
        ctx.moveTo(w * 0.17, h * 0.52);
        ctx.quadraticCurveTo(w * 0.50, h * 0.565, w * 0.83, h * 0.52);
        ctx.stroke();

        // Gold Atelier Watermark Frame
        ctx.strokeStyle = 'rgba(198, 161, 108, 0.65)';
        ctx.lineWidth = 2;
        ctx.strokeRect(8, 8, w - 16, h - 16);
    }

    /* === 3. DIGITAL FRAGRANCE SOMMELIER === */
    runFragranceMatcher() {
        const mood = document.getElementById('frag-mood')?.value || 'imperial_night';
        const note = document.getElementById('frag-note')?.value || 'oud_saffron';
        const out = document.getElementById('frag-result');
        if (!out) return;

        const topNotes = note === 'white_bergamot' ? 'ترنج کالابریا، شکوفه پرتقال و گلابی سفید' : 'زعفران سلطنتی، ترنج و فلفل صورتی';
        const heartNotes = mood === 'velvet_romance' ? 'رز دمشقی مخملی، صدتومانی و یاسمن' : 'رز سیاه پاریسی، زنبق فلورانس و شمعدانی';
        const baseNotes = 'چوب صندل گرم، کهربای طلایی ۲۴ ساعته، عود ملایم و مشک سفید';

        out.innerHTML = `
            <div style="font-weight:800; color:#E2C28E; font-size:1.05rem; margin-bottom:0.75rem;">
                امضای بویایی منطبق با شخصیت شما (۹۸٪ تطابق):
            </div>
            <div style="display:grid; gap:0.55rem; margin-bottom:1rem; font-size:0.86rem;">
                <div style="background:rgba(255,255,255,0.06); padding:0.65rem 0.9rem; border-radius:8px; border-right:3px solid #E2C28E;">
                    <strong>△ نت آغازین (۵ دقیقه اول):</strong> ${topNotes}
                </div>
                <div style="background:rgba(255,255,255,0.06); padding:0.65rem 0.9rem; border-radius:8px; border-right:3px solid #C6A16C;">
                    <strong>◇ نت میانی / قلب عطر:</strong> ${heartNotes}
                </div>
                <div style="background:rgba(255,255,255,0.06); padding:0.65rem 0.9rem; border-radius:8px; border-right:3px solid #9B7844;">
                    <strong>▽ نت پایانی و خط بو:</strong> ${baseNotes}
                </div>
            </div>
            <div style="display:flex; flex-wrap:wrap; gap:0.65rem;">
                <button class="btn-luxury" style="flex:1;" data-add-product="4">سفارش شیشه ۱۰۰ میل (${formatToman(2100000)})</button>
                <button class="btn-dark" style="flex:1;" data-add-product="6">سفارش پک تستر Discovery (${formatToman(790000)})</button>
            </div>
        `;
    }

    /* === 4. TTAC & HOLOGRAM AUTHENTICITY VERIFIER === */
    verifySerial(rawCode) {
        const box = document.getElementById('ttac-result');
        if (!box) return;
        const clean = String(rawCode || '').trim().toUpperCase();
        const match = this.products.find((p) => p.ttac_serial === clean || p.sku === clean);

        if (!match) {
            box.innerHTML = `
                <div style="background:rgba(158,42,43,0.22); border:1px solid #E05A5B; border-radius:12px; padding:1.2rem; color:#FFD7D7;">
                    <strong>⚠ هشدار امنیتی اصالت:</strong> کد واردشده در سامانه اصالت‌سنجی رسمی ASIYE STORE یافت نشد. لطفا از کدهای روی لیبل هولوگرام طلایی استفاده نمایید.
                </div>
            `;
            return;
        }

        box.innerHTML = `
            <div style="background:linear-gradient(135deg, rgba(198,161,108,0.22), rgba(31,111,80,0.22)); border:1.5px solid #C6A16C; border-radius:14px; padding:1.3rem;">
                <div style="display:flex; justify-content:space-between; align-items:center; border-bottom:1px solid rgba(198,161,108,0.35); padding-bottom:0.6rem; margin-bottom:0.75rem;">
                    <strong style="color:#E2C28E; font-size:1rem;">✓ گواهی رسمی اصالت هولوگرافیک ASIYE</strong>
                    <span style="background:#1F6F50; color:#fff; padding:0.2rem 0.65rem; border-radius:999px; font-size:0.75rem;">اورجینال تاییدشده</span>
                </div>
                <div style="font-size:0.86rem; line-height:1.9;">
                    • <strong>نام فرآورده:</strong> ${escapeHtml(match.name_fa)}<br>
                    • <strong>شناسه یکتای TTAC / هولوگرام:</strong> <code>${escapeHtml(match.ttac_serial)}</code><br>
                    • <strong>بچ‌کد آزمایشگاه (Batch):</strong> <code>${escapeHtml(match.batch_code)}</code> (انقضا: ۱۴۰۷/۰۸)<br>
                    • <strong>زنجیره نگهداری دمایی:</strong> تاییدشده در دمای کنترل‌شده ۱۸ درجه سانتی‌گراد
                </div>
            </div>
        `;
    }

    /* === 5 & 6. CONCIERGE CART, WALLET, BNPL & WHITE-GLOVE GIFTING === */
    calculateInvoice() {
        const subtotal = this.cart.reduce((s, i) => s + i.unit_price * i.quantity, 0);
        let couponDiscount = 0;
        const code = this.activeCoupon.trim().toUpperCase();
        if (code === 'ASIYE20' && subtotal >= 500000) couponDiscount = Math.min(Math.round(subtotal * 0.2), 700000);
        else if (code === 'VIPGOLD' && subtotal >= 1500000) couponDiscount = 350000;
        else if (code === 'REFILL10' && subtotal > 0) couponDiscount = Math.round(subtotal * 0.1);

        const afterCoupon = Math.max(0, subtotal - couponDiscount);
        const walletUsed = this.useWallet ? Math.min(this.walletBalance, afterCoupon) : 0;
        const afterWallet = Math.max(0, afterCoupon - walletUsed);
        const shipping = afterWallet === 0 || afterWallet >= 3000000 ? 0 : 95000;
        const coldChainFee = this.coldChain && afterWallet > 0 ? 0 : 0; // Complimentary VIP Cold-Chain!
        const total = afterWallet + shipping + coldChainFee;
        const cashbackEarned = Math.round(total * 0.05);

        return { subtotal, couponDiscount, walletUsed, shipping, total, cashbackEarned };
    }

    toggleCartDrawer(open) {
        const dr = document.getElementById('cart-drawer');
        const bd = document.getElementById('drawer-backdrop');
        if (!dr || !bd) return;
        const show = typeof open === 'boolean' ? open : !dr.classList.contains('open');
        dr.classList.toggle('open', show);
        bd.classList.toggle('open', show);
        if (show) this.renderCartDrawer();
    }

    renderCartDrawer() {
        const body = document.getElementById('cart-drawer-body');
        const footer = document.getElementById('cart-drawer-summary');
        if (!body || !footer) return;

        if (!this.cart.length) {
            body.innerHTML = `<div style="text-align:center; padding:3.5rem 1rem; color:#6E675F;">سبد تشریفاتی شما خالی است.</div>`;
            footer.innerHTML = '';
            return;
        }

        body.innerHTML = this.cart.map((item, idx) => `
            <div style="display:flex; gap:0.85rem; align-items:center; padding:0.85rem 0; border-bottom:1px solid rgba(198,161,108,0.22);">
                <img src="${escapeHtml(item.image)}" style="width:64px; height:64px; border-radius:10px; object-fit:cover; border:1px solid rgba(198,161,108,0.4);">
                <div style="flex:1;">
                    <div style="font-weight:800; font-size:0.9rem;">${escapeHtml(item.name_fa)}</div>
                    <div style="font-size:0.78rem; color:#6E675F;">${escapeHtml(item.variant_label)} • ${formatToman(item.unit_price)}</div>
                    <div style="display:flex; align-items:center; gap:0.45rem; margin-top:0.4rem;">
                        <button class="chip" style="padding:0.2rem 0.65rem;" data-cart-inc="${idx}">+</button>
                        <strong>${toPersianDigits(item.quantity)}</strong>
                        <button class="chip" style="padding:0.2rem 0.65rem;" data-cart-dec="${idx}">−</button>
                    </div>
                </div>
            </div>
        `).join('');

        const { subtotal, couponDiscount, walletUsed, shipping, total, cashbackEarned } = this.calculateInvoice();
        const perInstallment = Math.round(total / 4);

        footer.innerHTML = `
            <div class="concierge-box">
                <strong style="display:block; margin-bottom:0.4rem; color:#9B7844;">🎁 تشریفات بسته‌بندی هدیه سلطنتی و زنجیره سرد (رایگان VIP):</strong>
                <label style="display:block; cursor:pointer; margin-bottom:0.3rem;">
                    <input type="checkbox" id="chk-vip-box" ${this.vipGiftBox ? 'checked' : ''}>
                    جعبه مخمل مشکی با مهر موم طلایی AS + ۲ سمپل عطر رایگان
                </label>
                <label style="display:block; cursor:pointer; margin-bottom:0.45rem;">
                    <input type="checkbox" id="chk-cold-chain" ${this.coldChain ? 'checked' : ''}>
                    کیف عایق دمایی Cold-Chain (محافظت ترکیبات فعال در برابر گرما)
                </label>
                <select id="sel-tissue-scent" class="luxury-select" style="width:100%; padding:0.45rem; font-size:0.82rem; margin-bottom:0.4rem;">
                    <option value="رز دمشقی و عود سلطنتی" ${this.scentedTissue.includes('رز') ? 'selected' : ''}>رایحه کاغذ ابریشمی: رز دمشقی و عود سلطنتی</option>
                    <option value="وانیل ماداگاسکار و کهربا" ${this.scentedTissue.includes('وانیل') ? 'selected' : ''}>رایحه کاغذ ابریشمی: وانیل ماداگاسکار و کهربا</option>
                    <option value="ترنج سفید پاریسی" ${this.scentedTissue.includes('ترنج') ? 'selected' : ''}>رایحه کاغذ ابریشمی: ترنج سفید پاریسی</option>
                </select>
                <input id="inp-gift-msg" class="search-input" style="width:100%; padding:0.45rem; font-size:0.82rem;" placeholder="متن دلخواه روی کارت پستال طلاکوب هدیه..." value="${escapeHtml(this.giftMessage)}">
            </div>

            <div class="concierge-box">
                <label style="display:flex; justify-content:space-between; align-items:center; cursor:pointer; font-weight:700;">
                    <span><input type="checkbox" id="chk-use-wallet" ${this.useWallet ? 'checked' : ''}> کسر از کیف پول طلایی شما</span>
                    <span style="color:#1F6F50;">موجودی: ${formatToman(this.walletBalance)}</span>
                </label>
                <div style="display:flex; gap:0.45rem; margin-top:0.55rem;">
                    <input id="coupon-input" class="search-input" style="flex:1; padding:0.45rem;" placeholder="کد تخفیف (ASIYE20 / VIPGOLD / REFILL10)" value="${escapeHtml(this.activeCoupon)}">
                    <button class="btn-dark" id="apply-coupon-btn" style="padding:0.45rem 0.9rem;">اعمال</button>
                </div>
            </div>

            <div style="margin-bottom:0.7rem;">
                <select id="sel-bnpl-provider" class="luxury-select" style="width:100%; padding:0.55rem; font-weight:700;">
                    <option value="snapp_pay" ${this.bnplProvider === 'snapp_pay' ? 'selected' : ''}>اسنپ‌پی (Snapp Pay — ۴ قسط بدون کارمزد)</option>
                    <option value="tara" ${this.bnplProvider === 'tara' ? 'selected' : ''}>اعتبار تارا (Tara — ۴ قسط بدون سود)</option>
                    <option value="digipay" ${this.bnplProvider === 'digipay' ? 'selected' : ''}>دیجی‌پی (Digipay — اعتبار هوشمند)</option>
                    <option value="azkivam" ${this.bnplProvider === 'azkivam' ? 'selected' : ''}>ازکی‌وام (AzkiVam — سبدهای کلکسیونی)</option>
                    <option value="zarinpal" ${this.bnplProvider === 'zarinpal' ? 'selected' : ''}>درگاه آنلاین زرین‌پال (پرداخت نقدی شتاب)</option>
                </select>
            </div>

            <div style="font-size:0.85rem; line-height:1.8;">
                <div style="display:flex; justify-content:space-between;"><span>جمع کل کلکسیون:</span><span>${formatToman(subtotal)}</span></div>
                ${couponDiscount > 0 ? `<div style="display:flex; justify-content:space-between; color:#1F6F50;"><span>تخفیف کوپن:</span><span>${formatToman(couponDiscount)}−</span></div>` : ''}
                ${walletUsed > 0 ? `<div style="display:flex; justify-content:space-between; color:#9B7844;"><span>کسر از کیف پول طلایی:</span><span>${formatToman(walletUsed)}−</span></div>` : ''}
                <div style="display:flex; justify-content:space-between;"><span>ارسال VIP + زنجیره سرد:</span><span>${shipping === 0 ? 'رایگان' : formatToman(shipping)}</span></div>
                <div style="display:flex; justify-content:space-between; font-size:1.06rem; font-weight:900; border-top:1px solid rgba(198,161,108,0.4); margin-top:0.4rem; padding-top:0.4rem;">
                    <span>مبلغ قابل پرداخت:</span><span style="color:#9B7844;">${formatToman(total)}</span>
                </div>
                ${this.bnplProvider !== 'zarinpal' ? `<div style="font-size:0.8rem; color:#1F6F50; font-weight:700;">قسط اول امروز: ${formatToman(perInstallment)} (۳ قسط بعدی هر ۳۰ روز)</div>` : ''}
                <div style="font-size:0.78rem; color:#6E675F;">✨ پس از این خرید <strong>${formatToman(cashbackEarned)}</strong> کش‌بک طلایی آنی به کیف پول شما واریز می‌شود!</div>
            </div>

            <button class="btn-luxury" id="complete-imperial-order" style="width:100%; margin-top:0.85rem;">
                ثبت نهایی سفارش تشریفاتی و صدور گواهی اصالت
            </button>
        `;
    }

    completeOrder() {
        const { walletUsed, total, cashbackEarned } = this.calculateInvoice();
        this.walletBalance = Math.max(0, this.walletBalance - walletUsed) + cashbackEarned;
        this.loyaltyPoints += Math.max(25, Math.round(total / 10000));
        const certCode = 'AS-CERT-' + Math.floor(100000 + Math.random() * 900000);
        this.cart = [];
        this.activeCoupon = '';
        localStorage.removeItem('asiye_coupon');
        this.saveState();
        this.toggleCartDrawer(false);
        this.showToast(`سفارش VIP ثبت شد! گواهی اصالت ${toPersianDigits(certCode)} صادر و ${formatToman(cashbackEarned)} کش‌بک به کیف پول شما واریز گردید.`);
    }
}

const asiyeImperial = new AsiyeImperialClient();

document.addEventListener('DOMContentLoaded', () => {
    asiyeImperial.init();

    document.body.addEventListener('click', (e) => {
        const addBtn = e.target.closest('[data-add-product]');
        if (addBtn) {
            asiyeImperial.addProduct(addBtn.dataset.addProduct);
            return;
        }
        if (e.target.closest('#add-ai-ritual-btn')) {
            asiyeImperial.addProduct(1, 'روتین صبح AI');
            asiyeImperial.addProduct(2, 'روتین شب AI');
            asiyeImperial.activeCoupon = 'ASIYE20';
            asiyeImperial.toggleCartDrawer(true);
            return;
        }
        const swatch = e.target.closest('[data-shade-idx]');
        if (swatch) {
            asiyeImperial.selectedShadeIndex = Number(swatch.dataset.shadeIdx);
            asiyeImperial.renderShadeControls();
            asiyeImperial.renderVirtualLipCanvas();
            return;
        }
        const tone = e.target.closest('[data-tone-idx]');
        if (tone) {
            asiyeImperial.selectedSkinToneIndex = Number(tone.dataset.toneIdx);
            asiyeImperial.renderShadeControls();
            asiyeImperial.renderVirtualLipCanvas();
            return;
        }
        if (e.target.closest('#add-selected-shade-btn')) {
            const s = COUTURE_LIP_SHADES[asiyeImperial.selectedShadeIndex];
            asiyeImperial.addProduct(3, s.name);
            return;
        }
        const tabBtn = e.target.closest('[data-atelier-tab]');
        if (tabBtn) {
            document.querySelectorAll('[data-atelier-tab]').forEach((b) => b.classList.remove('active'));
            document.querySelectorAll('.atelier-panel').forEach((p) => p.classList.remove('active'));
            tabBtn.classList.add('active');
            document.getElementById(tabBtn.dataset.atelierTab)?.classList.add('active');
            if (tabBtn.dataset.atelierTab === 'panel-lip') asiyeImperial.renderVirtualLipCanvas();
            return;
        }
        const sampleSerial = e.target.closest('[data-sample-serial]');
        if (sampleSerial) {
            const inp = document.getElementById('ttac-serial-input');
            if (inp) inp.value = sampleSerial.dataset.sampleSerial;
            asiyeImperial.verifySerial(sampleSerial.dataset.sampleSerial);
            return;
        }
        if (e.target.closest('#ttac-verify-btn')) {
            asiyeImperial.verifySerial(document.getElementById('ttac-serial-input')?.value);
            return;
        }
        if (e.target.closest('#one-click-refill-btn')) {
            asiyeImperial.addProduct(1, 'شارژ مجدد هوشمند');
            asiyeImperial.activeCoupon = 'REFILL10';
            asiyeImperial.toggleCartDrawer(true);
            return;
        }
        const catBtn = e.target.closest('[data-category-filter]');
        if (catBtn) {
            document.querySelectorAll('[data-category-filter]').forEach((el) => el.classList.remove('active'));
            catBtn.classList.add('active');
            asiyeImperial.selectedCategory = catBtn.dataset.categoryFilter;
            asiyeImperial.renderCatalog();
            return;
        }
        if (e.target.closest('#open-cart-btn') || e.target.closest('#wallet-header-pill')) {
            asiyeImperial.toggleCartDrawer(true);
            return;
        }
        if (e.target.closest('#close-cart-btn') || e.target.id === 'drawer-backdrop') {
            asiyeImperial.toggleCartDrawer(false);
            return;
        }
        const inc = e.target.closest('[data-cart-inc]');
        if (inc) {
            asiyeImperial.cart[Number(inc.dataset.cartInc)].quantity += 1;
            asiyeImperial.saveState();
            asiyeImperial.renderCartDrawer();
            return;
        }
        const dec = e.target.closest('[data-cart-dec]');
        if (dec) {
            const i = Number(dec.dataset.cartDec);
            if (asiyeImperial.cart[i].quantity > 1) asiyeImperial.cart[i].quantity -= 1;
            else asiyeImperial.cart.splice(i, 1);
            asiyeImperial.saveState();
            asiyeImperial.renderCartDrawer();
            return;
        }
        if (e.target.closest('#apply-coupon-btn')) {
            asiyeImperial.activeCoupon = (document.getElementById('coupon-input')?.value || '').trim().toUpperCase();
            localStorage.setItem('asiye_coupon', asiyeImperial.activeCoupon);
            asiyeImperial.renderCartDrawer();
            asiyeImperial.showToast('کد تخفیف اعمال شد.');
            return;
        }
        if (e.target.closest('#complete-imperial-order')) {
            asiyeImperial.completeOrder();
            return;
        }
        if (e.target.closest('#mobile-menu-btn')) {
            document.querySelector('.nav-menu')?.classList.toggle('mobile-open');
        }
    });

    document.body.addEventListener('change', (e) => {
        if (e.target.id === 'ai-skin-type' || e.target.id === 'ai-skin-concern') asiyeImperial.runSkinAnalysis();
        if (e.target.id === 'frag-mood' || e.target.id === 'frag-note') asiyeImperial.runFragranceMatcher();
        if (e.target.id === 'chk-use-wallet') {
            asiyeImperial.useWallet = e.target.checked;
            asiyeImperial.renderCartDrawer();
        }
        if (e.target.id === 'chk-vip-box') asiyeImperial.vipGiftBox = e.target.checked;
        if (e.target.id === 'chk-cold-chain') asiyeImperial.coldChain = e.target.checked;
        if (e.target.id === 'sel-tissue-scent') asiyeImperial.scentedTissue = e.target.value;
        if (e.target.id === 'sel-bnpl-provider') {
            asiyeImperial.bnplProvider = e.target.value;
            asiyeImperial.renderCartDrawer();
        }
        if (e.target.id === 'catalog-sort') {
            asiyeImperial.sortMode = e.target.value;
            asiyeImperial.renderCatalog();
        }
    });

    document.body.addEventListener('input', (e) => {
        if (e.target.id === 'catalog-search') {
            asiyeImperial.searchQuery = e.target.value;
            asiyeImperial.renderCatalog();
        }
        if (e.target.id === 'inp-gift-msg') {
            asiyeImperial.giftMessage = e.target.value;
        }
    });
});
