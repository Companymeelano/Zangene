/* ASIYE STORE — FRONTEND ECOSYSTEM CLIENT v2.0 (XSS-Safe, RTL Persian, Offline-Resilient) */

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

// Resolve image paths cleanly whether index.html is opened from /frontend/ or domain root
function resolveAssetPath(rawPath) {
    if (!rawPath) return '../images/app_icon_dark.png';
    if (rawPath.startsWith('http://') || rawPath.startsWith('https://')) return rawPath;
    const clean = rawPath.replace(/^\/+/, '');
    const inFrontendSubdir = window.location.pathname.includes('/frontend/');
    return inFrontendSubdir ? `../${clean}` : `/${clean}`;
}

const FALLBACK_LUXURY_PRODUCTS = [
    {
        id: 1,
        sku: 'AS-SRM-001',
        name_fa: 'سرم طلایی احیاکننده پوست ۲۴ عیار',
        name_en: 'Golden Revitalizing Facial Serum',
        category_id: 1,
        category: { name_fa: 'مراقبت از پوست' },
        brand: { name_fa: 'ASIYE SIGNATURE' },
        original_price: 2300000,
        sale_price: 1850000,
        discount_percent: 20,
        description: 'سرم لوکس حاوی عصاره طلای خالص ۲۴ عیار و هیالورونیک اسید چندمولکولی برای بازسازی سلولی و درخشش ابریشمی پوست.',
        images: [{ path: 'images/product_serum_luxury.png' }],
        variants: [
            { id: 101, size: '۳۰ میلی‌لیتر', price_offset: 0, inventory: { quantity: 28 } },
            { id: 102, size: '۵۰ میلی‌لیتر', price_offset: 650000, inventory: { quantity: 12 } }
        ]
    },
    {
        id: 2,
        sku: 'AS-LIP-042',
        name_fa: 'رژ لب مخملی مات اختصاصی ASIYE',
        name_en: 'Exclusive Velvet Matte Lipstick',
        category_id: 2,
        category: { name_fa: 'آرایش لب و صورت' },
        brand: { name_fa: 'ASIYE SIGNATURE' },
        original_price: 520000,
        sale_price: 450000,
        discount_percent: 13,
        description: 'رژ لب مات با بافت مخملی فوق‌سبک، ویتامین E و ماندگاری ۱۲ ساعته بدون ایجاد خشکی، الهام‌گرفته از پالت طلایی شامپاینی.',
        images: [{ path: 'images/product_lipstick_luxury.png' }],
        variants: [
            { id: 201, color: 'رز بژ مخملی', price_offset: 0, inventory: { quantity: 42 } },
            { id: 202, color: 'شامپاینی نود', price_offset: 0, inventory: { quantity: 4 } }
        ]
    },
    {
        id: 3,
        sku: 'AS-PRF-999',
        name_fa: 'ادوپرفیوم اختصاصی ASIYE Signature',
        name_en: 'ASIYE Signature Eau De Parfum',
        category_id: 3,
        category: { name_fa: 'عطر و ادکلن لوکس' },
        brand: { name_fa: 'ASIYE SIGNATURE' },
        original_price: 2450000,
        sale_price: 2100000,
        discount_percent: 14,
        description: 'سمفونی مسحورکننده‌ای از نت‌های رز دمشقی، زعفران سلطنتی، کهربا و چوب صندل گرم؛ ماندگاری ۲۴ ساعته.',
        images: [{ path: 'images/product_perfume_luxury.png' }],
        variants: [
            { id: 301, size: '۱۰۰ میلی‌لیتر', price_offset: 0, inventory: { quantity: 19 } }
        ]
    }
];

class AsiyeEcosystemClient {
    constructor() {
        this.baseUrl = '/api/v1';
        this.token = localStorage.getItem('asiye_token') || null;
        this.cart = JSON.parse(localStorage.getItem('asiye_cart_v2') || '[]');
        this.activeCoupon = localStorage.getItem('asiye_coupon') || '';
        this.productsCache = [...FALLBACK_LUXURY_PRODUCTS];
        this.selectedCategory = 'all';
        this.searchQuery = '';
        this.sortMode = 'newest';
    }

    showToast(message) {
        let toast = document.getElementById('asiye-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'asiye-toast';
            toast.className = 'luxury-toast';
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.classList.add('show');
        clearTimeout(this._toastTimer);
        this._toastTimer = setTimeout(() => toast.classList.remove('show'), 3200);
    }

    async fetchJson(endpoint, options = {}) {
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            ...(this.token ? { 'Authorization': `Bearer ${this.token}` } : {}),
            ...(options.headers || {})
        };
        const response = await fetch(`${this.baseUrl}${endpoint}`, { ...options, headers });
        const payload = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(payload.error || payload.message || `HTTP ${response.status}`);
        }
        return payload;
    }

    async loadProducts() {
        try {
            const res = await this.fetchJson('/products?sort_by=created_at&direction=desc');
            if (Array.isArray(res?.data) && res.data.length > 0) {
                this.productsCache = res.data;
            }
        } catch (_) {
            // Gracefully use built-in luxury catalog when running standalone without backend server
            this.productsCache = [...FALLBACK_LUXURY_PRODUCTS];
        }
        this.renderCatalog();
        this.updateCartBadge();
    }

    getFilteredProducts() {
        let list = [...this.productsCache];
        if (this.selectedCategory !== 'all') {
            list = list.filter((p) => String(p.category_id) === String(this.selectedCategory));
        }
        if (this.searchQuery.trim()) {
            const q = this.searchQuery.trim().toLowerCase();
            list = list.filter((p) =>
                String(p.name_fa || '').toLowerCase().includes(q) ||
                String(p.name_en || '').toLowerCase().includes(q) ||
                String(p.sku || '').toLowerCase().includes(q)
            );
        }
        if (this.sortMode === 'price_asc') {
            list.sort((a, b) => Number(a.sale_price) - Number(b.sale_price));
        } else if (this.sortMode === 'price_desc') {
            list.sort((a, b) => Number(b.sale_price) - Number(a.sale_price));
        } else if (this.sortMode === 'discount') {
            list.sort((a, b) => Number(b.discount_percent || 0) - Number(a.discount_percent || 0));
        }
        return list;
    }

    renderCatalog() {
        const grid = document.getElementById('product-grid');
        if (!grid) return;

        const products = this.getFilteredProducts();
        if (products.length === 0) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align:center; padding:3rem; background:#fff; border-radius:12px;">محصولی مطابق جستجوی شما یافت نشد.</div>`;
            return;
        }

        grid.innerHTML = products.map((product) => {
            const firstVariant = product.variants?.[0] || { id: product.id * 100 + 1, inventory: { quantity: 10 } };
            const stock = Number(firstVariant.inventory?.quantity ?? 10);
            const salePrice = Number(product.sale_price) || 0;
            const origPrice = Number(product.original_price) || salePrice;
            const discount = Number(product.discount_percent) || 0;
            const perInstallment = Math.round(salePrice / 4);
            const imgPath = resolveAssetPath(product.images?.[0]?.path);

            return `
                <article class="product-card">
                    ${discount > 0 ? `<span class="discount-badge">${toPersianDigits(discount)}٪ تخفیف ویژه</span>` : ''}
                    <img src="${escapeHtml(imgPath)}" alt="${escapeHtml(product.name_fa)}" class="product-image" loading="lazy">
                    <div class="product-info">
                        <div class="product-category-tag">${escapeHtml(product.category?.name_fa || 'کلکسیون اختصاصی')}</div>
                        <h3 class="product-title">${escapeHtml(product.name_fa)}</h3>
                        <p style="font-size:0.86rem; color:#57534E; margin:0 0 0.75rem;">${escapeHtml(product.description || '')}</p>
                        <div class="product-price-row">
                            <span class="product-price">${formatToman(salePrice)}</span>
                            ${origPrice > salePrice ? `<span class="product-discount">${formatToman(origPrice)}</span>` : ''}
                        </div>
                        <div class="installment-pill">
                            امکان خرید ۴ قسطه اسنپ‌پی: ماهی <strong>${formatToman(perInstallment)}</strong>
                        </div>
                        <button class="btn-luxury" style="width:100%; margin-top:1rem;"
                            ${stock <= 0 ? 'disabled' : ''}
                            data-action="add-cart"
                            data-product-id="${Number(product.id)}"
                            data-variant-id="${Number(firstVariant.id)}">
                            ${stock > 0 ? 'افزودن به سبد خرید لوکس' : 'اتمام موجودی'}
                        </button>
                    </div>
                </article>
            `;
        }).join('');
    }

    async addToCart(productId, variantId) {
        const product = this.productsCache.find((p) => Number(p.id) === Number(productId));
        if (!product) return;
        const variant = product.variants?.find((v) => Number(v.id) === Number(variantId)) || product.variants?.[0] || { id: variantId, price_offset: 0, inventory: { quantity: 10 } };
        const maxStock = Number(variant.inventory?.quantity ?? 10);

        const existing = this.cart.find((item) => Number(item.variant_id) === Number(variantId));
        if (existing) {
            if (existing.quantity + 1 > maxStock) {
                this.showToast(`حداکثر موجودی قابل سفارش ${toPersianDigits(maxStock)} عدد است.`);
                return;
            }
            existing.quantity += 1;
        } else {
            this.cart.push({
                product_id: Number(product.id),
                variant_id: Number(variantId),
                name_fa: product.name_fa,
                variant_label: variant.size || variant.color || 'نسخه اصلی',
                unit_price: Number(product.sale_price) + Number(variant.price_offset || 0),
                image: resolveAssetPath(product.images?.[0]?.path),
                quantity: 1,
                max_stock: maxStock
            });
        }

        this.persistCart();
        this.showToast(`«${product.name_fa}» به سبد خرید اضافه شد.`);

        if (this.token) {
            this.fetchJson('/cart/add', {
                method: 'POST',
                body: JSON.stringify({ variant_id: Number(variantId), quantity: 1 })
            }).catch(() => {});
        }
    }

    changeCartQty(variantId, delta) {
        const idx = this.cart.findIndex((i) => Number(i.variant_id) === Number(variantId));
        if (idx === -1) return;
        const next = this.cart[idx].quantity + delta;
        if (next <= 0) {
            this.cart.splice(idx, 1);
        } else if (next <= this.cart[idx].max_stock) {
            this.cart[idx].quantity = next;
        } else {
            this.showToast('موجودی انبار بیشتر از این تعداد نیست.');
        }
        this.persistCart();
        this.renderCartDrawer();
    }

    persistCart() {
        localStorage.setItem('asiye_cart_v2', JSON.stringify(this.cart));
        this.updateCartBadge();
    }

    updateCartBadge() {
        const badge = document.getElementById('cart-count-badge');
        if (!badge) return;
        const count = this.cart.reduce((sum, item) => sum + item.quantity, 0);
        badge.textContent = toPersianDigits(count);
    }

    calculateTotals() {
        const subtotal = this.cart.reduce((sum, item) => sum + item.unit_price * item.quantity, 0);
        let discount = 0;
        const code = this.activeCoupon.trim().toUpperCase();
        if (code === 'ASIYE20' && subtotal >= 500000) {
            discount = Math.min(Math.round(subtotal * 0.2), 600000);
        } else if (code === 'VIPGOLD' && subtotal >= 1500000) {
            discount = 350000;
        }
        const afterDiscount = Math.max(0, subtotal - discount);
        const shipping = afterDiscount === 0 || afterDiscount >= 3000000 ? 0 : 95000;
        const total = afterDiscount + shipping;
        return { subtotal, discount, shipping, total };
    }

    toggleCartDrawer(forceOpen) {
        const drawer = document.getElementById('cart-drawer');
        const backdrop = document.getElementById('drawer-backdrop');
        if (!drawer || !backdrop) return;
        const willOpen = typeof forceOpen === 'boolean' ? forceOpen : !drawer.classList.contains('open');
        drawer.classList.toggle('open', willOpen);
        backdrop.classList.toggle('open', willOpen);
        if (willOpen) this.renderCartDrawer();
    }

    renderCartDrawer() {
        const body = document.getElementById('cart-drawer-body');
        const summary = document.getElementById('cart-drawer-summary');
        if (!body || !summary) return;

        if (this.cart.length === 0) {
            body.innerHTML = `<div style="text-align:center; padding:3rem 1rem; color:#78716C;">سبد خرید لوکس شما خالی است.</div>`;
            summary.innerHTML = '';
            return;
        }

        body.innerHTML = this.cart.map((item) => `
            <div style="display:flex; gap:0.85rem; align-items:center; padding:0.85rem 0; border-bottom:1px solid rgba(198,161,108,0.2);">
                <img src="${escapeHtml(item.image)}" alt="${escapeHtml(item.name_fa)}" style="width:64px; height:64px; border-radius:8px; object-fit:cover;">
                <div style="flex:1;">
                    <div style="font-weight:700; font-size:0.92rem;">${escapeHtml(item.name_fa)}</div>
                    <div style="font-size:0.78rem; color:#78716C;">${escapeHtml(item.variant_label)} — ${formatToman(item.unit_price)}</div>
                    <div style="display:flex; align-items:center; gap:0.5rem; margin-top:0.45rem;">
                        <button class="chip" data-action="qty-inc" data-variant-id="${Number(item.variant_id)}">+</button>
                        <span style="font-weight:700;">${toPersianDigits(item.quantity)}</span>
                        <button class="chip" data-action="qty-dec" data-variant-id="${Number(item.variant_id)}">−</button>
                    </div>
                </div>
            </div>
        `).join('');

        const { subtotal, discount, shipping, total } = this.calculateTotals();
        summary.innerHTML = `
            <div style="display:flex; gap:0.5rem; margin-bottom:0.85rem;">
                <input id="coupon-input" class="search-input" style="flex:1;" placeholder="کد تخفیف (مثلا ASIYE20)" value="${escapeHtml(this.activeCoupon)}">
                <button class="btn-dark" id="apply-coupon-btn" style="padding:0.5rem 1rem;">اعمال</button>
            </div>
            <div style="font-size:0.88rem; display:flex; justify-content:space-between;"><span>جمع کل کالاها:</span><span>${formatToman(subtotal)}</span></div>
            ${discount > 0 ? `<div style="font-size:0.88rem; color:#2D6A4F; display:flex; justify-content:space-between;"><span>سود شما از تخفیف:</span><span>${formatToman(discount)}−</span></div>` : ''}
            <div style="font-size:0.88rem; display:flex; justify-content:space-between;"><span>ارسال VIP:</span><span>${shipping === 0 ? 'رایگان' : formatToman(shipping)}</span></div>
            <div style="font-size:1.1rem; font-weight:800; border-top:1px solid rgba(198,161,108,0.35); margin-top:0.6rem; padding-top:0.6rem; display:flex; justify-content:space-between;">
                <span>مبلغ قابل پرداخت:</span><span style="color:#A6824D;">${formatToman(total)}</span>
            </div>
            <div style="font-size:0.8rem; color:#57534E; margin:0.4rem 0 0.9rem;">یا ۴ قسط ماهانه <strong>${formatToman(Math.round(total / 4))}</strong> با اسنپ‌پی / تارا</div>
            <button class="btn-luxury" id="complete-checkout-btn" style="width:100%;">تکمیل سفارش و پرداخت امن</button>
        `;
    }

    completeOrderDemo() {
        const { total } = this.calculateTotals();
        if (total <= 0) return;
        const orderCode = 'AS-' + toPersianDigits(Math.floor(100000 + Math.random() * 900000));
        this.cart = [];
        this.activeCoupon = '';
        localStorage.removeItem('asiye_coupon');
        this.persistCart();
        this.toggleCartDrawer(false);
        this.showToast(`سفارش شما با کد رهگیری ${orderCode} با موفقیت ثبت شد!`);
    }
}

const asiyeApp = new AsiyeEcosystemClient();

document.addEventListener('DOMContentLoaded', () => {
    asiyeApp.loadProducts();

    document.body.addEventListener('click', (e) => {
        const addBtn = e.target.closest('[data-action="add-cart"]');
        if (addBtn) {
            asiyeApp.addToCart(addBtn.dataset.productId, addBtn.dataset.variantId);
            return;
        }
        const incBtn = e.target.closest('[data-action="qty-inc"]');
        if (incBtn) {
            asiyeApp.changeCartQty(incBtn.dataset.variantId, 1);
            return;
        }
        const decBtn = e.target.closest('[data-action="qty-dec"]');
        if (decBtn) {
            asiyeApp.changeCartQty(decBtn.dataset.variantId, -1);
            return;
        }
        const catBtn = e.target.closest('[data-category-filter]');
        if (catBtn) {
            document.querySelectorAll('[data-category-filter]').forEach((el) => el.classList.remove('active'));
            catBtn.classList.add('active');
            asiyeApp.selectedCategory = catBtn.dataset.categoryFilter;
            asiyeApp.renderCatalog();
            return;
        }
        if (e.target.closest('#open-cart-btn')) {
            asiyeApp.toggleCartDrawer(true);
            return;
        }
        if (e.target.closest('#close-cart-btn') || e.target.id === 'drawer-backdrop') {
            asiyeApp.toggleCartDrawer(false);
            return;
        }
        if (e.target.closest('#apply-coupon-btn')) {
            const input = document.getElementById('coupon-input');
            asiyeApp.activeCoupon = (input?.value || '').trim().toUpperCase();
            localStorage.setItem('asiye_coupon', asiyeApp.activeCoupon);
            asiyeApp.renderCartDrawer();
            asiyeApp.showToast('کد تخفیف بررسی و اعمال شد.');
            return;
        }
        if (e.target.closest('#complete-checkout-btn')) {
            asiyeApp.completeOrderDemo();
            return;
        }
        if (e.target.closest('#mobile-menu-btn')) {
            document.querySelector('.nav-menu')?.classList.toggle('mobile-open');
        }
    });

    document.getElementById('catalog-search')?.addEventListener('input', (e) => {
        asiyeApp.searchQuery = e.target.value;
        asiyeApp.renderCatalog();
    });

    document.getElementById('catalog-sort')?.addEventListener('change', (e) => {
        asiyeApp.sortMode = e.target.value;
        asiyeApp.renderCatalog();
    });
});
