(() => {
  'use strict';

  // Illustrative catalog only. Real prices and inventory have not been provided.
  // priceToman is an integer per kilogram or null when no real price is known.
  const products = [
    { id: 'pistachio', category: 'pistachio', categoryLabel: 'پسته', name: 'پسته اکبری', detail: 'انتخابی خوش‌طعم از پسته', image: 'assets/product-pistachio.webp', keywords: 'آجیل پسته ایرانی خام', priceToman: null, inStock: null },
    { id: 'almond', category: 'almond', categoryLabel: 'بادام', name: 'مغز بادام درختی', detail: 'انتخابی از مغزهای محبوب', image: 'assets/product-almond.webp', keywords: 'آجیل مغز بادام خام', priceToman: null, inStock: null },
    { id: 'walnut', category: 'walnut', categoryLabel: 'مغزها', name: 'مغز گردو', detail: 'مغز گردو برای انتخاب شما', image: 'assets/product-walnut.webp', keywords: 'گردو آجیل مغز', priceToman: null, inStock: null },
    { id: 'fig', category: 'driedfruit', categoryLabel: 'خشکبار', name: 'انجیر خشک', detail: 'انتخابی از دنیای خشکبار', image: 'assets/product-fig.webp', keywords: 'میوه خشک خشکبار انجیر', priceToman: null, inStock: null },
    { id: 'mixed', category: 'mixed', categoryLabel: 'آجیل مخلوط', name: 'آجیل مخلوط', detail: 'ترکیبی از طعم‌های متنوع', image: 'assets/hero-nuts.webp', keywords: 'آجیل مخلوط پسته بادام گردو', priceToman: null, inStock: null }
  ];
  const productById = new Map(products.map(product => [product.id, product]));
  const cartKey = 'derakhshan-shopping-cart-v1';
  const legacyQuoteKey = 'derakhshan-wholesale-quote-v1';
  const favoritesKey = 'derakhshan-wholesale-favorites-v1';
  const maxQuantity = 100000;
  const numberFormat = new Intl.NumberFormat('fa-IR');
  const quantityFormat = new Intl.NumberFormat('fa-IR', { useGrouping: false });
  const $ = selector => document.querySelector(selector);
  const $$ = selector => [...document.querySelectorAll(selector)];
  const appScroll = $('#app-scroll');
  const grid = $('#product-grid');
  const search = $('#product-search');
  const emptyResults = $('#empty-results');
  const sortControl = $('#catalog-sort');
  const productModal = $('#product-modal');
  const cartDrawer = $('#cart-drawer');
  const checkoutDrawer = $('#checkout-drawer');
  const checkoutForm = $('#checkout-form');
  const checkoutFormView = $('#checkout-form-view');
  const checkoutSuccess = $('#checkout-success');
  const toast = $('#toast');
  let activeCategory = 'all';
  let showFavorites = false;
  let dockSection = 'home';
  let toastTimer;
  const mobileLayout = window.matchMedia('(max-width: 680px)');
  let lastScrollPosition = mobileLayout.matches ? appScroll.scrollTop : window.scrollY;

  const toEnglishDigits = value => String(value)
    .replace(/[۰-۹]/g, digit => String(digit.charCodeAt(0) - 0x06f0))
    .replace(/[٠-٩]/g, digit => String(digit.charCodeAt(0) - 0x0660));
  const normalize = value => toEnglishDigits(value).trim().toLowerCase()
    .replace(/ي|ى/g, 'ی').replace(/ك/g, 'ک').replace(/[\u064b-\u065f\u0670]/g, '')
    .replace(/[\u200c\u200f]/g, ' ').replace(/\s+/g, ' ');
  const escapeHTML = value => String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
  const formatNumber = value => numberFormat.format(value);
  const scrollBehavior = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth';
  const icon = name => `<svg class="icon" aria-hidden="true"><use href="#i-${name}"></use></svg>`;
  const hasPrice = product => Number.isSafeInteger(product.priceToman) && product.priceToman > 0;
  const priceLabel = product => hasPrice(product) ? `${formatNumber(product.priceToman)} تومان` : 'قیمت ثبت نشده';

  function cleanCart(value) {
    if (!Array.isArray(value)) return [];
    const seen = new Set();
    return value.reduce((items, item) => {
      const quantity = Number(item?.quantity);
      if (productById.has(item?.id) && !seen.has(item.id) && Number.isFinite(quantity) && quantity >= 1) {
        seen.add(item.id);
        items.push({ id: item.id, quantity: Math.min(maxQuantity, Math.floor(quantity)) });
      }
      return items;
    }, []);
  }

  function readCart() {
    try {
      const saved = localStorage.getItem(cartKey);
      if (saved !== null) return cleanCart(JSON.parse(saved));
      // Preserve selections made in the previous inquiry-based version on upgrade.
      const legacy = localStorage.getItem(legacyQuoteKey);
      const migrated = legacy === null ? [] : cleanCart(JSON.parse(legacy));
      if (legacy !== null) localStorage.setItem(cartKey, JSON.stringify(migrated));
      return migrated;
    } catch { return []; }
  }

  function readFavorites() {
    try {
      const stored = JSON.parse(localStorage.getItem(favoritesKey) || '[]');
      return new Set(Array.isArray(stored) ? stored.filter(id => productById.has(id)) : []);
    } catch { return new Set(); }
  }

  let cart = readCart();
  let favorites = readFavorites();
  const saveCart = () => { try { localStorage.setItem(cartKey, JSON.stringify(cart)); } catch { /* The cart remains available in this session. */ } };
  const itemCount = () => cart.reduce((total, item) => total + item.quantity, 0);

  function cartTotal() {
    if (!cart.length || cart.some(item => !hasPrice(productById.get(item.id)))) return null;
    const total = cart.reduce((sum, item) => sum + productById.get(item.id).priceToman * item.quantity, 0);
    return Number.isSafeInteger(total) ? total : null;
  }

  function syncDock() {
    const active = dockSection === 'contact' ? null : (dockSection === 'catalog' ? (showFavorites ? '[data-dock-favorites]' : '[data-dock-products]') : '[data-dock-home]');
    $$('.dock-item').forEach(item => {
      const selected = Boolean(active && item.matches(active));
      item.classList.toggle('is-current', selected);
      if (selected) item.setAttribute('aria-current', 'location');
      else item.removeAttribute('aria-current');
    });
  }

  function setDockSection(section) {
    dockSection = section;
    // An explicit action wins over a previously queued scroll frame.
    lastScrollPosition = mobileLayout.matches ? appScroll.scrollTop : window.scrollY;
    syncDock();
  }

  function syncFavorites() {
    $$('[data-favorites-count]').forEach(el => {
      el.textContent = formatNumber(favorites.size);
      if (el.classList.contains('dock-count')) el.hidden = favorites.size === 0;
    });
    $$('[data-toggle-favorites]').forEach(button => {
      button.setAttribute('aria-pressed', String(showFavorites));
      button.classList.toggle('is-active', showFavorites);
    });
    $$('[data-favorite-product]').forEach(button => {
      const product = productById.get(button.dataset.favoriteProduct);
      if (!product) return;
      const selected = favorites.has(product.id);
      const action = selected ? 'حذف از منتخب‌ها' : 'افزودن به منتخب‌ها';
      button.setAttribute('aria-pressed', String(selected));
      button.setAttribute('aria-label', `${action}: ${product.name}`);
      button.classList.toggle('is-selected', selected);
      const label = button.querySelector('.modal-favorite-label');
      if (label) label.textContent = action;
    });
    syncDock();
  }

  function notify(message, kind = 'basket') {
    clearTimeout(toastTimer);
    toast.dataset.kind = kind;
    toast.textContent = message;
    toast.classList.add('visible');
    toastTimer = setTimeout(() => toast.classList.remove('visible'), 2500);
  }

  function toggleFavorite(id) {
    const product = productById.get(id);
    if (!product) return;
    const selected = !favorites.has(id);
    if (selected) favorites.add(id); else favorites.delete(id);
    try { localStorage.setItem(favoritesKey, JSON.stringify([...favorites])); } catch { /* Retained for this session. */ }
    if (showFavorites) renderProducts(); else syncFavorites();
    if (!productModal.open) notify(selected ? `${product.name} به منتخب‌ها اضافه شد.` : `${product.name} از منتخب‌ها حذف شد.`, 'favorite');
  }

  function renderProducts() {
    const query = normalize(search.value);
    let filtered = products.filter(product => {
      const matchesCategory = activeCategory === 'all' || product.category === activeCategory;
      const text = normalize([product.name, product.categoryLabel, product.detail, product.keywords].join(' '));
      return matchesCategory && (!showFavorites || favorites.has(product.id)) && (!query || text.includes(query));
    });
    if (sortControl.value !== 'featured') {
      const direction = sortControl.value === 'name-desc' ? -1 : 1;
      filtered = [...filtered].sort((a, b) => direction * a.name.localeCompare(b.name, 'fa'));
    }
    grid.innerHTML = filtered.map(product => {
      const id = escapeHTML(product.id), name = escapeHTML(product.name), unavailable = product.inStock === false;
      return `<article class="product-card">
        <div class="product-image-wrap">
          <img src="${escapeHTML(product.image)}" alt="تصویر نمونه ${name}">
          <span class="product-badge">نمونه</span>
          <button class="product-favorite" type="button" data-favorite-product="${id}" aria-label="افزودن به منتخب‌ها: ${name}" aria-pressed="false"><img src="assets/ui-favorites-3d.webp" alt=""><span aria-hidden="true">♥</span></button>
        </div>
        <div class="product-info">
          ${product.categoryLabel === product.name ? '' : `<p class="product-category">${escapeHTML(product.categoryLabel)}</p>`}
          <h3>${name}</h3>
          <p class="shop-price"><strong>${escapeHTML(priceLabel(product))}</strong><small>هر کیلوگرم</small></p>
          <button class="shop-add" type="button" data-add-product="${id}" ${unavailable ? 'disabled' : ''}><span class="shop-action-art" aria-hidden="true"><img src="assets/ui-basket-3d.webp" alt=""></span><span>${unavailable ? 'ناموجود' : 'افزودن به سبد'}</span></button>
          <button class="product-view" type="button" data-open-product="${id}" aria-label="جزئیات محصول: ${name}"><span class="product-view-art" aria-hidden="true"><img src="assets/ui-details-3d.webp" alt=""></span><span>جزئیات محصول</span>${icon('arrow-left')}</button>
        </div>
      </article>`;
    }).join('');
    grid.classList.toggle('is-single', filtered.length === 1);
    emptyResults.hidden = filtered.length !== 0;
    const noFavorites = showFavorites && favorites.size === 0;
    $('#empty-results-title').textContent = noFavorites ? 'منتخب‌های شما هنوز خالی است' : (showFavorites ? 'محصول منتخبی پیدا نشد' : 'محصولی پیدا نشد');
    $('#empty-results-description').textContent = noFavorites ? 'نشان قلب روی هر محصول را لمس کنید.' : 'جست‌وجو یا دسته‌بندی را تغییر دهید.';
    const art = $('#empty-results-art');
    const artPath = showFavorites ? 'assets/ui-favorites-3d.webp' : 'assets/ui-search-3d.webp';
    if (art.getAttribute('src') !== artPath) art.setAttribute('src', artPath);
    $('#catalog-count').textContent = `${formatNumber(filtered.length)} محصول${showFavorites ? ' منتخب' : ''}`;
    $$('.filter-chip').forEach(button => {
      const selected = button.dataset.filter === activeCategory;
      button.classList.toggle('active', selected);
      button.setAttribute('aria-pressed', String(selected));
    });
    syncFavorites();
  }

  function updateCartTotals() {
    const count = itemCount();
    $$('[data-cart-count]').forEach(el => {
      el.textContent = formatNumber(count);
      if (el.classList.contains('dock-count') || el.classList.contains('quote-count')) el.hidden = count === 0;
    });
    $$('.cart-trigger, .dock-cart').forEach(el => el.setAttribute('aria-label', `سبد خرید، ${formatNumber(count)} کیلوگرم`));
    const total = cartTotal();
    $('#cart-total').textContent = total === null ? 'نامشخص' : `${formatNumber(total)} تومان`;
    $('#checkout-total').textContent = total === null ? 'مبلغ: نامشخص' : `مبلغ: ${formatNumber(total)} تومان`;
    $('#checkout-count').textContent = `${formatNumber(count)} کیلوگرم محصول`;
    $('#cart-price-note').textContent = total === null ? 'قیمت محصولات هنوز ثبت نشده است.' : 'هزینه ارسال هنوز محاسبه نشده است.';
    $('#cart-empty').hidden = count > 0;
    $('#cart-summary').hidden = count === 0;
    $('[data-checkout]').hidden = count === 0;
  }

  function renderCart() {
    $('#cart-items').innerHTML = cart.map(item => {
      const product = productById.get(item.id);
      const id = escapeHTML(product.id), name = escapeHTML(product.name);
      return `<li class="quote-item">
        <img src="${escapeHTML(product.image)}" alt="">
        <div class="quote-item-info"><strong>${name}</strong><small>${escapeHTML(priceLabel(product))} / کیلوگرم</small>
          <div class="quantity-control">
            <button type="button" data-decrease="${id}" aria-label="کاهش مقدار ${name}">${icon('minus')}</button>
            <input type="text" inputmode="numeric" maxlength="6" value="${quantityFormat.format(item.quantity)}" data-quantity="${id}" aria-label="مقدار ${name} به کیلوگرم">
            <button type="button" data-increase="${id}" aria-label="افزایش مقدار ${name}">${icon('plus')}</button><span>کیلوگرم</span>
          </div>
        </div>
        <button class="quote-item-remove" type="button" data-remove="${id}" aria-label="حذف ${name} از سبد خرید">${icon('trash')}</button>
      </li>`;
    }).join('');
    updateCartTotals();
  }

  function addProduct(id) {
    const product = productById.get(id);
    if (!product || product.inStock === false) return;
    const item = cart.find(entry => entry.id === id);
    if (item) item.quantity = Math.min(maxQuantity, item.quantity + 1);
    else cart.push({ id, quantity: 1 });
    saveCart();
    renderCart();
    if (productModal.open) closeProduct();
    notify(`${product.name} به سبد خرید اضافه شد.`);
  }

  function resetFilters() {
    activeCategory = 'all';
    showFavorites = false;
    search.value = '';
    renderProducts();
  }

  function scrollToProducts(target = $('#products'), block = 'start') {
    target.scrollIntoView({ behavior: scrollBehavior(), block });
    setDockSection('catalog');
  }

  function goHome() {
    resetFilters();
    if (mobileLayout.matches) appScroll.scrollTo({ top: 0, behavior: scrollBehavior() });
    else window.scrollTo({ top: 0, behavior: scrollBehavior() });
    setDockSection('home');
  }

  function syncModalState() {
    // New account and admin dialogs share the same mobile shell.
    document.body.classList.toggle('is-modal-open', Boolean(document.querySelector('dialog[open]')));
  }

  function closeProduct() {
    if (productModal.open) productModal.close();
    syncModalState();
  }

  function openProduct(id) {
    const product = productById.get(id);
    if (!product) return;
    if (cartDrawer.open) cartDrawer.close();
    const image = $('#product-modal-image');
    image.src = product.image;
    image.alt = `تصویر نمونه ${product.name}`;
    $('#product-modal-category').textContent = product.categoryLabel;
    $('#product-modal-title').textContent = product.name;
    $('#product-modal-description').textContent = product.detail;
    $('#product-modal-price').textContent = priceLabel(product);
    const missing = [];
    if (!hasPrice(product)) missing.push('قیمت');
    if (product.inStock !== true && product.inStock !== false) missing.push('موجودی');
    $('#product-modal-note').textContent = product.inStock === false
      ? 'محصول نمونه است؛ این محصول در حال حاضر ناموجود است.'
      : missing.length ? `محصول نمونه است؛ ${missing.join(' و ')} آن هنوز ثبت نشده است.` : 'محصول و تصویر نمایش‌داده‌شده نمونه‌اند.';
    $('#product-modal-add').dataset.addProduct = id;
    $('#product-modal-add').disabled = product.inStock === false;
    $('#product-modal-add-label').textContent = product.inStock === false ? 'ناموجود' : 'افزودن به سبد';
    $('#product-modal-favorite').dataset.favoriteProduct = id;
    syncFavorites();
    if (!productModal.open) productModal.showModal();
    syncModalState();
    productModal.scrollTop = 0;
  }

  function closeCart() {
    if (cartDrawer.open) cartDrawer.close();
    syncModalState();
  }

  function openCart() {
    if (productModal.open) productModal.close();
    if (checkoutDrawer.open) checkoutDrawer.close();
    checkoutForm.reset();
    $('#cart-feedback').textContent = '';
    renderCart();
    if (!cartDrawer.open) cartDrawer.showModal();
    syncModalState();
    $('.cart-drawer .drawer-scroll').scrollTop = 0;
  }

  function openCheckout() {
    if (!cart.length) return;
    closeCart();
    checkoutForm.reset();
    [...checkoutForm.elements].forEach(el => { if (typeof el.setCustomValidity === 'function') el.setCustomValidity(''); });
    checkoutFormView.hidden = false;
    checkoutSuccess.hidden = true;
    checkoutDrawer.setAttribute('aria-describedby', 'checkout-disclaimer');
    updateCartTotals();
    if (!checkoutDrawer.open) checkoutDrawer.showModal();
    syncModalState();
    $('.checkout-drawer .drawer-scroll').scrollTop = 0;
  }

  function closeCheckout() {
    const wasComplete = !checkoutSuccess.hidden;
    if (checkoutDrawer.open) checkoutDrawer.close();
    checkoutForm.reset();
    syncModalState();
    if (!wasComplete) openCart();
    else scrollToProducts();
  }

  document.addEventListener('click', event => {
    const target = event.target.closest('button, a');
    if (!target) return;
    if (target.matches('[data-home]')) { event.preventDefault(); goHome(); return; }
    if (target.matches('[data-show-contact]')) { event.preventDefault(); $('#contact').scrollIntoView({ behavior: scrollBehavior(), block: 'start' }); setDockSection('contact'); return; }
    if (target.matches('[data-browse], [data-browse-products]')) { event.preventDefault(); closeCart(); resetFilters(); scrollToProducts(); return; }
    if (target.matches('[data-focus-search]')) { resetFilters(); scrollToProducts(); search.focus({ preventScroll: true }); return; }
    if (target.matches('[data-filter]')) { activeCategory = target.dataset.filter; renderProducts(); setDockSection('catalog'); return; }
    if (target.matches('[data-reset-filters]')) { resetFilters(); scrollToProducts(); return; }
    if (target.matches('[data-toggle-favorites]')) { showFavorites = !showFavorites; renderProducts(); scrollToProducts(showFavorites && !favorites.size ? emptyResults : grid, showFavorites && !favorites.size ? 'center' : 'start'); return; }
    if (target.matches('[data-dock-favorites]')) { showFavorites = true; activeCategory = 'all'; search.value = ''; renderProducts(); scrollToProducts(favorites.size ? grid : emptyResults, favorites.size ? 'start' : 'center'); return; }
    if (target.matches('[data-open-product]')) { openProduct(target.dataset.openProduct); return; }
    if (target.matches('[data-favorite-product]')) { toggleFavorite(target.dataset.favoriteProduct); return; }
    if (target.matches('[data-add-product]')) { addProduct(target.dataset.addProduct); return; }
    if (target.matches('[data-close-product]')) { closeProduct(); return; }
    if (target.matches('[data-open-cart]')) { openCart(); return; }
    if (target.matches('[data-close-cart]')) { closeCart(); return; }
    if (target.matches('[data-checkout]')) { openCheckout(); return; }
    if (target.matches('[data-close-checkout], [data-finish-checkout]')) { closeCheckout(); return; }
    if (target.matches('[data-increase], [data-decrease]')) {
      const increase = target.hasAttribute('data-increase');
      const id = increase ? target.dataset.increase : target.dataset.decrease;
      const item = cart.find(entry => entry.id === id);
      if (!item) return;
      item.quantity = Math.min(maxQuantity, Math.max(1, item.quantity + (increase ? 1 : -1)));
      saveCart(); renderCart();
      if (event.detail === 0) $(`[data-${increase ? 'increase' : 'decrease'}="${id}"]`)?.focus({ preventScroll: true });
      return;
    }
    if (target.matches('[data-remove]')) {
      const id = target.dataset.remove;
      const product = productById.get(id);
      cart = cart.filter(item => item.id !== id);
      saveCart(); renderCart();
      if (event.detail === 0) (cart.length ? $('#cart-items button') : $('[data-browse-products]')).focus({ preventScroll: true });
      if (product) $('#cart-feedback').textContent = `${product.name} از سبد خرید حذف شد.`;
    }
  });

  search.addEventListener('input', () => { renderProducts(); setDockSection('catalog'); });
  sortControl.addEventListener('change', () => { renderProducts(); setDockSection('catalog'); });
  $('#cart-items').addEventListener('change', event => {
    const input = event.target.closest('[data-quantity]');
    if (!input) return;
    const item = cart.find(entry => entry.id === input.dataset.quantity);
    if (!item) return;
    const digits = toEnglishDigits(input.value).replace(/[^0-9]/g, '');
    const quantity = Number(digits);
    item.quantity = Number.isFinite(quantity) && quantity > 0 ? Math.min(maxQuantity, Math.floor(quantity)) : 1;
    input.value = quantityFormat.format(item.quantity);
    saveCart(); updateCartTotals();
  });

  const isTextEditor = element => Boolean(element && element.matches('input:not([type=checkbox]):not([type=radio]):not([type=button]):not([type=submit]), textarea'));
  document.addEventListener('focusin', event => { if (isTextEditor(event.target)) document.body.classList.add('is-editing'); });
  document.addEventListener('focusout', () => { setTimeout(() => document.body.classList.toggle('is-editing', isTextEditor(document.activeElement)), 0); });
  productModal.addEventListener('click', event => { if (event.target === productModal) closeProduct(); });
  cartDrawer.addEventListener('click', event => { if (event.target === cartDrawer) closeCart(); });
  checkoutDrawer.addEventListener('click', event => { if (event.target === checkoutDrawer) closeCheckout(); });
  productModal.addEventListener('cancel', event => { event.preventDefault(); closeProduct(); });
  cartDrawer.addEventListener('cancel', event => { event.preventDefault(); closeCart(); });
  checkoutDrawer.addEventListener('cancel', event => { event.preventDefault(); closeCheckout(); });
  [productModal, cartDrawer, checkoutDrawer].forEach(dialog => dialog.addEventListener('close', syncModalState));
  checkoutForm.addEventListener('input', event => { if (typeof event.target.setCustomValidity === 'function') event.target.setCustomValidity(''); });
  checkoutForm.addEventListener('submit', event => {
    event.preventDefault();
    if (!cart.length) { closeCheckout(); return; }
    const name = checkoutForm.elements.name;
    const phone = checkoutForm.elements.phone;
    const address = checkoutForm.elements.address;
    const normalizedPhone = toEnglishDigits(phone.value.trim());
    const digits = normalizedPhone.replace(/\D/g, '');
    if (!name.value.trim()) { name.setCustomValidity('نام گیرنده را وارد کنید.'); name.reportValidity(); return; }
    name.setCustomValidity('');
    if (!/^[+()\d\s-]+$/.test(normalizedPhone) || digits.length < 8 || digits.length > 15) {
      phone.setCustomValidity('شماره تماس معتبر وارد کنید.'); phone.reportValidity(); return;
    }
    phone.setCustomValidity('');
    if (!address.value.trim()) { address.setCustomValidity('نشانی را وارد کنید.'); address.reportValidity(); return; }
    address.setCustomValidity('');
    // A deliberate local simulation: no card data is collected, no order or money is sent.
    checkoutForm.reset();
    checkoutFormView.hidden = true;
    checkoutSuccess.hidden = false;
    checkoutDrawer.setAttribute('aria-describedby', 'checkout-success');
    $('.checkout-drawer .drawer-scroll').scrollTop = 0;
    $('[data-finish-checkout]').focus({ preventScroll: true });
  });

  window.addEventListener('storage', event => {
    if (event.key === cartKey) { cart = readCart(); renderCart(); }
    if (event.key === favoritesKey) { favorites = readFavorites(); renderProducts(); }
  });

  // Update the dock on actual scrolling, not just intersection transitions: a
  // section can stay intersecting while the hero scrolls away or cards resize.
  let scrollFrame = 0;
  function updateDockForScroll() {
    scrollFrame = 0;
    const mobile = mobileLayout.matches;
    const scrollPosition = mobile ? appScroll.scrollTop : window.scrollY;
    const wasScrollingUp = scrollPosition < lastScrollPosition;
    lastScrollPosition = scrollPosition;
    if (scrollPosition < 10) {
      // Keep an explicit filter/search selection at the very top; returning
      // upward to the top still restores the Home state.
      if (wasScrollingUp) dockSection = 'home';
    } else {
      const scrollBounds = mobile ? appScroll.getBoundingClientRect() : { top: 0, bottom: window.innerHeight, height: window.innerHeight };
      const probe = scrollBounds.top + scrollBounds.height * 0.25;
      const contactBounds = $('#contact').getBoundingClientRect();
      const productsTop = $('#products').getBoundingClientRect().top;
      const maxScroll = mobile ? appScroll.scrollHeight - appScroll.clientHeight : document.documentElement.scrollHeight - window.innerHeight;
      // Short result lists can reach the end before the contact section ever
      // crosses the probe. At the end, honor contact if it is visible.
      const contactAtEnd = scrollPosition >= maxScroll - 2 && contactBounds.top < scrollBounds.bottom && contactBounds.bottom > scrollBounds.top;
      dockSection = contactBounds.top <= probe || contactAtEnd ? 'contact' : (productsTop <= probe ? 'catalog' : 'home');
    }
    syncDock();
  }
  function scheduleDockUpdate() {
    if (!scrollFrame) scrollFrame = requestAnimationFrame(updateDockForScroll);
  }
  appScroll.addEventListener('scroll', scheduleDockUpdate, { passive: true });
  window.addEventListener('scroll', scheduleDockUpdate, { passive: true });
  window.addEventListener('resize', scheduleDockUpdate, { passive: true });
  if (mobileLayout.addEventListener) mobileLayout.addEventListener('change', scheduleDockUpdate);
  else if (mobileLayout.addListener) mobileLayout.addListener(scheduleDockUpdate);

  renderProducts();
  renderCart();
})();
