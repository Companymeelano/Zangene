(() => {
  'use strict';

  // The catalog is deliberately local and illustrative; prices and stock require confirmation.
  const products = [
    { id: 'pistachio', category: 'pistachio', categoryLabel: 'پسته', name: 'پسته اکبری', detail: 'انتخابی اصیل برای خرید عمده', image: 'assets/product-pistachio.webp', badge: 'انتخاب درخشان', keywords: 'آجیل پسته ایرانی خام' },
    { id: 'almond', category: 'almond', categoryLabel: 'بادام', name: 'مغز بادام درختی', detail: 'طبیعی و خوش‌طعم', image: 'assets/product-almond.webp', badge: 'طعم طبیعی', keywords: 'آجیل مغز بادام خام' },
    { id: 'walnut', category: 'walnut', categoryLabel: 'مغزها', name: 'مغز گردو', detail: 'گزینه‌ای دلنشین برای فروشگاه‌ها', image: 'assets/product-walnut.webp', badge: 'انتخاب درخشان', keywords: 'گردو آجیل مغز' },
    { id: 'fig', category: 'driedfruit', categoryLabel: 'خشکبار', name: 'انجیر خشک', detail: 'شیرینیِ دوست‌داشتنی طبیعت', image: 'assets/product-fig.webp', badge: 'طعم طبیعی', keywords: 'میوه خشک خشکبار انجیر' },
    { id: 'mixed', category: 'mixed', categoryLabel: 'آجیل مخلوط', name: 'آجیل مخلوط', detail: 'ترکیبی خوش‌طعم از مغزها', image: 'assets/hero-nuts.webp', badge: 'انتخاب درخشان', keywords: 'آجیل مخلوط پسته بادام گردو' }
  ];
  const productById = new Map(products.map(product => [product.id, product]));
  const storageKey = 'derakhshan-wholesale-quote-v1';
  const favoritesKey = 'derakhshan-wholesale-favorites-v1';
  const productDescriptions = {
    pistachio: 'پسته اکبری، انتخابی خوش‌نام برای فروشگاه‌ها و سفارش‌های عمده است. جزئیات محصول و شرایط همکاری را از فروشنده استعلام کنید.',
    almond: 'مغز بادام درختی انتخابی کاربردی برای فروشگاه، شیرینی‌پزی و سفارش عمده است. برای اطلاع از جزئیات و شرایط تهیه، استعلام بگیرید.',
    walnut: 'مغز گردو را برای انتخابی متناسب با نیاز کسب‌وکارتان بررسی کنید. جزئیات، بسته‌بندی و شرایط سفارش با فروشنده هماهنگ می‌شود.',
    fig: 'انجیر خشک، انتخابی دلنشین در مجموعه خشکبار است. مشخصات و شرایط تأمین این محصول پس از استعلام تأیید می‌شود.',
    mixed: 'ترکیبی از طعم‌های متنوع برای انتخاب عمده. اجزای محصول و شرایط بسته‌بندی پیش از سفارش با فروشنده هماهنگ می‌شود.'
  };
  const numberFormat = new Intl.NumberFormat('fa-IR');
  const $ = selector => document.querySelector(selector);
  const $$ = selector => [...document.querySelectorAll(selector)];
  const appScroll = $('#app-scroll');
  const grid = $('#product-grid');
  const search = $('#product-search');
  const emptyResults = $('#empty-results');
  const sortControl = $('#catalog-sort');
  const favoritesFilter = $('#favorites-filter');
  const productModal = $('#product-modal');
  const drawer = $('#quote-drawer');
  const quoteItems = $('#quote-items');
  const quoteEmpty = $('#quote-empty');
  const quoteSummary = $('#quote-summary');
  const quoteForm = $('#quote-form');
  const quoteResult = $('#quote-result');
  const quoteText = $('#quote-text');
  const feedback = $('#copy-feedback');
  const toast = $('#toast');
  const mobileNav = $('#mobile-nav');
  const mobileToggle = $('.mobile-menu-toggle');
  let activeCategory = 'all';
  let showFavorites = false;
  let dockSection = 'home';
  let toastTimer;

  const toEnglishDigits = value => String(value)
    .replace(/[۰-۹]/g, digit => String(digit.charCodeAt(0) - 0x06f0))
    .replace(/[٠-٩]/g, digit => String(digit.charCodeAt(0) - 0x0660));
  const normalize = value => toEnglishDigits(value).trim().toLowerCase()
    .replace(/ي|ى/g, 'ی').replace(/ك/g, 'ک').replace(/[\u064b-\u065f\u0670]/g, '')
    .replace(/[\u200c\u200f]/g, ' ').replace(/\s+/g, ' ');
  const formatNumber = number => numberFormat.format(number);
  const scrollBehavior = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth';
  const icon = name => `<svg class="icon" aria-hidden="true"><use href="#i-${name}"></use></svg>`;

  function readQuote() {
    try {
      const stored = JSON.parse(localStorage.getItem(storageKey) || '[]');
      if (!Array.isArray(stored)) return [];
      const seen = new Set();
      return stored.reduce((items, item) => {
        const quantity = Number(item?.quantity);
        if (productById.has(item?.id) && !seen.has(item.id) && Number.isFinite(quantity) && quantity >= 1) {
          seen.add(item.id);
          items.push({ id: item.id, quantity: Math.min(100000, Math.floor(quantity)) });
        }
        return items;
      }, []);
    } catch {
      return [];
    }
  }

  function readFavorites() {
    try {
      const stored = JSON.parse(localStorage.getItem(favoritesKey) || '[]');
      return new Set(Array.isArray(stored) ? stored.filter(id => productById.has(id)) : []);
    } catch {
      return new Set();
    }
  }

  let favorites = readFavorites();
  let quote = readQuote();
  function saveQuote() {
    try { localStorage.setItem(storageKey, JSON.stringify(quote)); } catch { /* The list remains available for this page session. */ }
  }

  function syncDock() {
    const active = dockSection === 'catalog' ? (showFavorites ? '[data-dock-favorites]' : '[data-dock-products]') : '[data-dock-home]';
    $$('.dock-item').forEach(item => item.classList.toggle('is-current', item.matches(active)));
  }

  function syncFavoriteControls() {
    $$('[data-favorites-count]').forEach(el => {
      el.textContent = formatNumber(favorites.size);
      if (el.classList.contains('dock-count')) el.hidden = favorites.size === 0;
    });
    favoritesFilter.setAttribute('aria-pressed', String(showFavorites));
    favoritesFilter.classList.toggle('is-active', showFavorites);
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

  function toggleFavorite(id) {
    const product = productById.get(id);
    if (!product) return;
    const selected = !favorites.has(id);
    if (selected) favorites.add(id); else favorites.delete(id);
    try { localStorage.setItem(favoritesKey, JSON.stringify([...favorites])); } catch { /* Retained for this session. */ }
    if (showFavorites) renderProducts(); else syncFavoriteControls();
    notify(selected ? `${product.name} به منتخب‌ها اضافه شد.` : `${product.name} از منتخب‌ها حذف شد.`, 'favorite');
  }

  function renderProducts() {
    const query = normalize(search.value);
    let filtered = products.filter(product => {
      const matchesCategory = activeCategory === 'all' || product.category === activeCategory;
      const searchable = normalize([product.name, product.categoryLabel, product.detail, product.keywords].join(' '));
      return matchesCategory && (!showFavorites || favorites.has(product.id)) && (!query || searchable.includes(query));
    });
    if (sortControl.value !== 'featured') {
      const direction = sortControl.value === 'name-desc' ? -1 : 1;
      filtered = [...filtered].sort((a, b) => direction * a.name.localeCompare(b.name, 'fa'));
    }
    grid.innerHTML = filtered.map(product => `
      <article class="product-card">
        <div class="product-image-wrap">
          <img src="${product.image}" alt="تصویر مفهومی ${product.name}" loading="lazy">
          <span class="product-badge">${product.badge}</span>
          <button class="product-favorite" type="button" data-favorite-product="${product.id}" aria-label="افزودن ${product.name} به منتخب‌ها" aria-pressed="false"><img src="assets/ui-favorites-3d.webp" alt=""><span aria-hidden="true">♥</span></button>
          <button class="product-add" type="button" data-add-product="${product.id}" aria-label="افزودن ${product.name} به لیست استعلام"><img src="assets/ui-basket-3d.webp" alt=""><span aria-hidden="true">+</span></button>
        </div>
        <div class="product-info">
          <p class="product-category">${product.categoryLabel} <span aria-hidden="true"> / </span> فروش عمده</p>
          <h3>${product.name}</h3>
          <p class="product-detail">${product.detail}</p>
          <button class="product-view" type="button" data-open-product="${product.id}" aria-label="جزئیات محصول: ${product.name}"><span class="product-view-art" aria-hidden="true"><img src="assets/ui-details-3d.webp" alt=""></span><span>جزئیات محصول</span><svg class="icon"><use href="#i-arrow-left"></use></svg></button>
          <div class="product-card-footer"><span>قیمت همکاری</span><button class="product-quote" type="button" data-quote-product="${product.id}"><span class="product-quote-art" aria-hidden="true"><img src="assets/ui-inquiry-3d.webp" alt=""></span>استعلام قیمت</button></div>
        </div>
      </article>`).join('');
    emptyResults.hidden = filtered.length !== 0;
    const noFavorites = showFavorites && favorites.size === 0;
    $('#empty-results-title').textContent = noFavorites ? 'منتخب‌های شما هنوز خالی است' : (showFavorites ? 'محصول منتخبی پیدا نشد' : 'محصولی پیدا نشد');
    $('#empty-results-description').textContent = noFavorites
      ? 'با لمس نشان قلب روی هر محصول، آن را به فهرست منتخب‌های خود اضافه کنید.'
      : 'عبارت جست‌وجو یا دسته‌بندی را تغییر دهید، یا همه محصولات را ببینید.';
    const emptyArt = $('#empty-results-art');
    const emptyArtPath = showFavorites ? 'assets/ui-favorites-3d.webp' : 'assets/ui-search-3d.webp';
    if (emptyArt.getAttribute('src') !== emptyArtPath) emptyArt.setAttribute('src', emptyArtPath);
    $('#catalog-count').textContent = `نمایش ${formatNumber(filtered.length)} محصول${showFavorites ? ' منتخب' : ''}`;
    $$('.filter-chip').forEach(button => {
      const selected = button.dataset.filter === activeCategory;
      button.classList.toggle('active', selected);
      button.setAttribute('aria-pressed', String(selected));
    });
    syncFavoriteControls();
  }

  function invalidateResult() {
    quoteResult.hidden = true;
    feedback.textContent = '';
  }

  function renderQuote() {
    const count = quote.length;
    $$('[data-quote-count]').forEach(el => {
      el.textContent = formatNumber(count);
      if (el.classList.contains('dock-count') || el.classList.contains('quote-count')) el.hidden = count === 0;
    });
    $('.quote-trigger').setAttribute('aria-label', `لیست استعلام، ${formatNumber(count)} محصول`);
    $('.dock-quote').setAttribute('aria-label', `لیست استعلام، ${formatNumber(count)} محصول`);
    quoteEmpty.hidden = count > 0;
    quoteSummary.hidden = count === 0;
    quoteItems.innerHTML = quote.map(item => {
      const product = productById.get(item.id);
      return `<li class="quote-item">
        <img src="${product.image}" alt="">
        <div class="quote-item-info"><strong>${product.name}</strong><small>مقدار مورد نیاز</small>
          <div class="quantity-control">
            <button type="button" data-decrease="${item.id}" aria-label="کاهش مقدار ${product.name}">${icon('minus')}</button>
            <input type="text" inputmode="numeric" value="${formatNumber(item.quantity)}" data-quantity="${item.id}" aria-label="مقدار ${product.name} به کیلوگرم">
            <button type="button" data-increase="${item.id}" aria-label="افزایش مقدار ${product.name}">${icon('plus')}</button>
            <span>کیلوگرم</span>
          </div>
        </div>
        <button class="quote-item-remove" type="button" data-remove="${item.id}" aria-label="حذف ${product.name} از لیست">${icon('trash')}</button>
      </li>`;
    }).join('');
  }

  function updateQuote() {
    saveQuote();
    renderQuote();
    invalidateResult();
  }

  function notify(message, kind = 'basket') {
    clearTimeout(toastTimer);
    toast.dataset.kind = kind;
    toast.textContent = message;
    toast.classList.add('visible');
    toastTimer = setTimeout(() => toast.classList.remove('visible'), 2600);
  }

  function addProduct(id, increment = true) {
    const product = productById.get(id);
    if (!product) return;
    const item = quote.find(entry => entry.id === id);
    if (item && increment) item.quantity = Math.min(100000, item.quantity + 1);
    else if (!item) quote.push({ id, quantity: 1 });
    updateQuote();
    if (increment) notify(`${product.name} به لیست استعلام اضافه شد.`);
  }

  function setCategory(category, shouldScroll = true, keepFavorites = false) {
    const valid = category === 'all' || products.some(product => product.category === category);
    if (!keepFavorites) showFavorites = false;
    activeCategory = valid ? category : 'all';
    search.value = '';
    renderProducts();
    const filterList = $('.filter-list');
    const selectedFilter = filterList.querySelector('.active');
    if (selectedFilter && filterList.scrollWidth > filterList.clientWidth) {
      const listBounds = filterList.getBoundingClientRect();
      const chipBounds = selectedFilter.getBoundingClientRect();
      const offset = chipBounds.left < listBounds.left ? chipBounds.left - listBounds.left :
        chipBounds.right > listBounds.right ? chipBounds.right - listBounds.right : 0;
      if (offset) filterList.scrollBy({ left: offset, behavior: scrollBehavior() });
    }
    if (shouldScroll) $('#products').scrollIntoView({ behavior: scrollBehavior(), block: 'start' });
  }

  function closeMobileMenu() {
    mobileNav.hidden = true;
    mobileToggle.setAttribute('aria-expanded', 'false');
    mobileToggle.setAttribute('aria-label', 'باز کردن منو');
    mobileToggle.querySelector('use').setAttribute('href', '#i-menu');
  }

  function syncModalState() {
    document.body.classList.toggle('is-modal-open', drawer.open || productModal.open);
  }

  function closeProduct() {
    if (productModal.open) productModal.close();
    syncModalState();
  }

  function openProduct(id) {
    const product = productById.get(id);
    if (!product) return;
    if (drawer.open) closeDrawer();
    closeMobileMenu();
    const image = $('#product-modal-image');
    image.src = product.image;
    image.alt = `تصویر مفهومی ${product.name}`;
    $('#product-modal-category').textContent = `${product.categoryLabel} / فروش عمده`;
    $('#product-modal-title').textContent = product.name;
    $('#product-modal-description').textContent = productDescriptions[id] || product.detail;
    $('#product-modal-quote').dataset.quoteProduct = id;
    $('#product-modal-favorite').dataset.favoriteProduct = id;
    syncFavoriteControls();
    if (!productModal.open) productModal.showModal();
    syncModalState();
    productModal.scrollTop = 0;
  }

  function openDrawer(focusForm = false) {
    if (productModal.open) closeProduct();
    closeMobileMenu();
    renderQuote();
    if (!drawer.open) drawer.showModal();
    syncModalState();
    $('.drawer-scroll').scrollTop = 0;
    if (focusForm) requestAnimationFrame(() => {
      if (drawer.open) $('#request-form-wrap').scrollIntoView({ behavior: scrollBehavior(), block: 'start' });
    });
  }

  function closeDrawer() {
    if (drawer.open) drawer.close();
    syncModalState();
  }

  document.addEventListener('click', event => {
    const category = event.target.closest('[data-category]');
    const filter = event.target.closest('[data-filter]');
    const footerCategory = event.target.closest('[data-footer-category]');
    const add = event.target.closest('[data-add-product]');
    const quoteProduct = event.target.closest('[data-quote-product]');
    const productView = event.target.closest('[data-open-product]');
    const favorite = event.target.closest('[data-favorite-product]');
    const increase = event.target.closest('[data-increase]');
    const decrease = event.target.closest('[data-decrease]');
    const remove = event.target.closest('[data-remove]');
    const open = event.target.closest('[data-open-quote]');

    // The header is outside the independently scrolling content on phones.
    // A fragment targeting that header would otherwise leave the content at its old position.
    const homeLink = event.target.closest('a[href="#top"], a[href="#home"]');
    if (homeLink && window.matchMedia('(max-width: 680px)').matches) {
      event.preventDefault();
      appScroll.scrollTo({ top: 0, behavior: scrollBehavior() });
      closeMobileMenu();
      dockSection = 'home';
      syncDock();
    }

    if (category) setCategory(category.dataset.category);
    if (filter) setCategory(filter.dataset.filter, false, true);
    if (footerCategory) { event.preventDefault(); setCategory(footerCategory.dataset.footerCategory); }
    if (event.target.closest('[data-show-all]')) setCategory('all', false);
    if (event.target.closest('[data-reset-filters]')) { showFavorites = false; sortControl.value = 'featured'; setCategory('all', false); }
    if (event.target.closest('[data-favorites-filter]')) { showFavorites = !showFavorites; renderProducts(); }
    if (event.target.closest('[data-dock-products]')) { event.preventDefault(); setCategory('all'); dockSection = 'catalog'; syncDock(); }
    if (event.target.closest('[data-dock-favorites]')) {
      showFavorites = true; activeCategory = 'all'; search.value = ''; renderProducts();
      dockSection = 'catalog'; syncDock();
      const destination = favorites.size === 0 ? emptyResults : $('#product-grid');
      destination.scrollIntoView({ behavior: scrollBehavior(), block: favorites.size === 0 ? 'center' : 'start' });
    }
    if (event.target.closest('[data-dock-home]')) { closeMobileMenu(); dockSection = 'home'; syncDock(); }
    if (productView) openProduct(productView.dataset.openProduct);
    if (favorite) toggleFavorite(favorite.dataset.favoriteProduct);
    if (event.target.closest('[data-close-product]')) closeProduct();
    if (add) addProduct(add.dataset.addProduct);
    if (quoteProduct) { addProduct(quoteProduct.dataset.quoteProduct, false); openDrawer(); }
    if (open) openDrawer(open.dataset.openQuote === 'form');
    if (event.target.closest('[data-close-quote]')) closeDrawer();
    if (event.target.closest('[data-browse-products]')) { closeDrawer(); setCategory('all'); }
    if (event.target.closest('[data-focus-search]')) {
      setCategory('all');
      search.focus({ preventScroll: true });
    }
    if (increase || decrease) {
      const id = increase ? increase.dataset.increase : decrease.dataset.decrease;
      const item = quote.find(entry => entry.id === id);
      if (item) {
        item.quantity = Math.min(100000, Math.max(1, item.quantity + (increase ? 1 : -1)));
        const input = quoteItems.querySelector(`[data-quantity="${id}"]`);
        if (input) input.value = formatNumber(item.quantity);
        saveQuote();
        invalidateResult();
      }
    }
    if (remove) {
      quote = quote.filter(item => item.id !== remove.dataset.remove);
      updateQuote();
      notify('محصول از لیست استعلام حذف شد.');
    }
  });

  search.addEventListener('input', renderProducts);
  sortControl.addEventListener('change', renderProducts);
  const isTextEditor = element => Boolean(element && element.matches('input:not([type=checkbox]):not([type=radio]):not([type=button]):not([type=submit]), textarea'));
  document.addEventListener('focusin', event => {
    if (isTextEditor(event.target)) document.body.classList.add('is-editing');
  });
  document.addEventListener('focusout', () => {
    setTimeout(() => document.body.classList.toggle('is-editing', isTextEditor(document.activeElement)), 0);
  });
  quoteItems.addEventListener('input', event => {
    if (event.target.matches('[data-quantity]')) invalidateResult();
  });
  quoteItems.addEventListener('change', event => {
    const input = event.target.closest('[data-quantity]');
    if (!input) return;
    const item = quote.find(entry => entry.id === input.dataset.quantity);
    if (!item) return;
    const digits = toEnglishDigits(input.value).replace(/[^0-9]/g, '');
    const quantity = Number(digits);
    item.quantity = Number.isFinite(quantity) && quantity > 0 ? Math.min(100000, Math.floor(quantity)) : 1;
    input.value = formatNumber(item.quantity);
    saveQuote();
    invalidateResult();
  });

  mobileToggle.addEventListener('click', () => {
    const isOpening = mobileNav.hidden;
    mobileNav.hidden = !isOpening;
    mobileToggle.setAttribute('aria-expanded', String(isOpening));
    mobileToggle.setAttribute('aria-label', isOpening ? 'بستن منو' : 'باز کردن منو');
    mobileToggle.querySelector('use').setAttribute('href', isOpening ? '#i-close' : '#i-menu');
  });
  mobileNav.addEventListener('click', event => { if (event.target.closest('a')) closeMobileMenu(); });
  drawer.addEventListener('click', event => { if (event.target === drawer) closeDrawer(); });
  drawer.addEventListener('cancel', event => { event.preventDefault(); closeDrawer(); });
  productModal.addEventListener('cancel', event => { event.preventDefault(); closeProduct(); });
  drawer.addEventListener('close', syncModalState);
  productModal.addEventListener('close', syncModalState);
  productModal.addEventListener('click', event => { if (event.target === productModal) closeProduct(); });
  quoteForm.addEventListener('input', event => {
    if (event.target.name === 'phone' || event.target.name === 'name') event.target.setCustomValidity('');
    invalidateResult();
  });

  quoteForm.addEventListener('submit', event => {
    event.preventDefault();
    const data = new FormData(quoteForm);
    const name = String(data.get('name') || '').trim();
    const phone = String(data.get('phone') || '').trim();
    const message = String(data.get('message') || '').trim();
    const nameInput = quoteForm.elements.name;
    const phoneInput = quoteForm.elements.phone;
    const normalizedPhone = toEnglishDigits(phone);
    const phoneDigits = normalizedPhone.replace(/\D/g, '');
    if (!name) {
      nameInput.setCustomValidity('لطفاً نام خود یا کسب‌وکارتان را وارد کنید.');
      nameInput.reportValidity();
      return;
    }
    nameInput.setCustomValidity('');
    if (!/^[+()\d\s-]+$/.test(normalizedPhone) || phoneDigits.length < 8 || phoneDigits.length > 15) {
      phoneInput.setCustomValidity('لطفاً شماره تماس معتبر وارد کنید.');
      phoneInput.reportValidity();
      return;
    }
    phoneInput.setCustomValidity('');
    const lines = [
      'سلام، برای همکاری با عمده‌فروشی آجیل و خشکبار درخشان درخواست استعلام دارم.',
      '',
      `نام / کسب‌وکار: ${name}`,
      `شماره تماس: ${phone}`,
      ''
    ];
    if (quote.length) {
      lines.push('محصولات مورد نظر:');
      quote.forEach(item => lines.push(`• ${productById.get(item.id).name}: ${formatNumber(item.quantity)} کیلوگرم`));
    } else {
      lines.push('موضوع: درخواست همکاری و استعلام محصولات');
    }
    if (message) lines.push('', `توضیحات: ${message}`);
    lines.push('', 'لطفاً قیمت، موجودی و شرایط سفارش را اعلام کنید.');
    quoteText.value = lines.join('\n');
    quoteResult.hidden = false;
    feedback.textContent = '';
    quoteResult.scrollIntoView({ behavior: scrollBehavior(), block: 'nearest' });
  });

  $('#copy-quote').addEventListener('click', async () => {
    try {
      if (window.DerakhshanAndroid && typeof window.DerakhshanAndroid.copyText === 'function') {
        // The Android wrapper provides clipboard access without any network permission.
        window.DerakhshanAndroid.copyText(quoteText.value);
      } else if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(quoteText.value);
      } else {
        quoteText.focus();
        quoteText.select();
        if (!document.execCommand('copy')) throw new Error('Copy unavailable');
      }
      feedback.textContent = 'متن کپی شد. حالا می‌توانید آن را برای فروشنده ارسال کنید.';
    } catch {
      quoteText.focus();
      quoteText.select();
      feedback.textContent = 'لطفاً متن انتخاب‌شده را به‌صورت دستی کپی کنید.';
    }
  });

  $('#download-quote').addEventListener('click', () => {
    if (window.DerakhshanAndroid && typeof window.DerakhshanAndroid.saveText === 'function') {
      try {
        window.DerakhshanAndroid.saveText(quoteText.value);
        feedback.textContent = 'برای ذخیره فایل، محل مورد نظر را انتخاب کنید.';
      } catch {
        feedback.textContent = 'ذخیره فایل ممکن نشد؛ می‌توانید متن را کپی کنید.';
      }
      return;
    }
    const blob = new Blob(['\ufeff', quoteText.value], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'derakhshan-inquiry.txt';
    document.body.append(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
    feedback.textContent = 'فایل متن درخواست آماده شد.';
  });

  window.addEventListener('storage', event => {
    if (event.key === storageKey) { quote = readQuote(); renderQuote(); invalidateResult(); }
    if (event.key === favoritesKey) { favorites = readFavorites(); renderProducts(); }
  });

  if ('IntersectionObserver' in window) {
    const mobileLayout = window.matchMedia('(max-width: 680px)');
    let observer;
    const observeSections = () => {
      if (observer) observer.disconnect();
      observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) { dockSection = entry.target.id === 'products' ? 'catalog' : 'home'; syncDock(); }
        });
      }, { root: mobileLayout.matches ? appScroll : null, rootMargin: '-25% 0px -55% 0px', threshold: 0 });
      ['home', 'categories', 'products', 'about', 'contact'].forEach(id => observer.observe(document.getElementById(id)));
    };
    observeSections();
    if (mobileLayout.addEventListener) mobileLayout.addEventListener('change', observeSections);
    else if (mobileLayout.addListener) mobileLayout.addListener(observeSections);
  }

  renderProducts();
  renderQuote();
})();
