(() => {
  'use strict';

  // Both experiences are deliberately local demos. Nothing here authenticates a
  // user, protects an admin panel, transmits data, or modifies the shop catalog.
  const $ = selector => document.querySelector(selector);
  const $$ = selector => [...document.querySelectorAll(selector)];
  const profileKey = 'derakhshan-local-profile-demo-v1';
  const adminKey = 'derakhshan-admin-sandbox-v1';
  const accountDialog = $('#account-drawer');
  const adminDialog = $('#admin-drawer');
  const accountForm = $('#account-form');
  const productForm = $('#admin-product-form');
  const orderForm = $('#admin-order-form');
  const userForm = $('#admin-user-form');
  const accountFeedback = $('#account-feedback');
  const adminFeedback = $('#admin-feedback');
  const numbers = new Intl.NumberFormat('fa-IR');
  const categories = ['پسته', 'بادام', 'مغزها', 'خشکبار', 'آجیل مخلوط', 'سایر'];
  const statuses = { pending: 'در انتظار', preparing: 'در حال آماده‌سازی', completed: 'تکمیل نمونه', cancelled: 'لغو نمونه' };
  const maxPrice = 9999999999;
  const maxStock = 1000000;
  const maxQuantity = 100000;
  const escapeHTML = value => String(value).replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
  const format = value => numbers.format(value);
  const toEnglishDigits = value => String(value).replace(/[۰-۹]/g, digit => String(digit.charCodeAt(0) - 0x06f0)).replace(/[٠-٩]/g, digit => String(digit.charCodeAt(0) - 0x0660));
  const normalize = value => toEnglishDigits(value).trim().toLowerCase().replace(/ي|ى/g, 'ی').replace(/ك/g, 'ک').replace(/[\u200c\u200f]/g, ' ').replace(/\s+/g, ' ');
  const identifier = prefix => `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 9)}`;

  // app.js has rendered the original sample catalog before this deferred script.
  // Take a separate snapshot so admin experiments never alter customer-facing data.
  const initialProducts = $$('#product-grid .product-card').map((card, index) => {
    const id = card.querySelector('[data-add-product]')?.dataset.addProduct || `sample-${index}`;
    const name = card.querySelector('h3')?.textContent.trim() || 'محصول نمونه';
    const categoryText = card.querySelector('.product-category')?.textContent.trim() || name;
    return { id: `base-${id}`, name, category: categories.includes(categoryText) ? categoryText : 'سایر', price: null, stock: null };
  });
  const freshSandbox = () => ({ products: initialProducts.map(product => ({ ...product })), orders: [], users: [] });

  function cleanProduct(value) {
    if (!value || typeof value !== 'object') return null;
    const id = String(value.id || '').slice(0, 80);
    const name = String(value.name || '').trim().slice(0, 80);
    if (!id || !name) return null;
    const price = Number.isSafeInteger(value.price) && value.price > 0 && value.price <= maxPrice ? value.price : null;
    const stock = Number.isSafeInteger(value.stock) && value.stock >= 0 && value.stock <= maxStock ? value.stock : null;
    return { id, name, category: categories.includes(value.category) ? value.category : 'سایر', price, stock };
  }

  function cleanOrder(value) {
    if (!value || typeof value !== 'object') return null;
    const id = String(value.id || '').slice(0, 80);
    const productName = String(value.productName || '').trim().slice(0, 80);
    const productId = String(value.productId || '').slice(0, 80);
    const quantity = Number(value.quantity);
    const createdAt = Number(value.createdAt);
    if (!id || !productName || !Number.isSafeInteger(quantity) || quantity < 1 || quantity > maxQuantity || !Number.isFinite(createdAt)) return null;
    return { id, productId, productName, quantity, createdAt, status: Object.prototype.hasOwnProperty.call(statuses, value.status) ? value.status : 'pending' };
  }

  function cleanUser(value) {
    if (!value || typeof value !== 'object') return null;
    const id = String(value.id || '').slice(0, 80);
    const alias = String(value.alias || '').trim().slice(0, 40);
    return id && alias ? { id, alias, active: value.active === true } : null;
  }

  function loadSandbox() {
    try {
      const raw = localStorage.getItem(adminKey);
      if (raw === null) return freshSandbox();
      const saved = JSON.parse(raw);
      if (!Array.isArray(saved?.products) || !Array.isArray(saved?.orders)) return freshSandbox();
      const products = saved.products.slice(0, 250).map(cleanProduct).filter(Boolean);
      const orders = saved.orders.slice(0, 500).map(cleanOrder).filter(Boolean);
      const users = Array.isArray(saved.users) ? saved.users.slice(0, 250).map(cleanUser).filter(Boolean) : [];
      return { products, orders, users };
    } catch { return freshSandbox(); }
  }

  function loadProfile() {
    try {
      const value = JSON.parse(localStorage.getItem(profileKey) || 'null');
      const name = typeof value?.name === 'string' ? value.name.trim().slice(0, 40) : '';
      return name ? { name } : null;
    } catch { return null; }
  }

  let profile = loadProfile();
  let sandbox = loadSandbox();
  let sandboxUnsaved = false;
  let accountView = 'entry';
  let editingProfile = false;
  let adminActive = false;
  let adminTab = 'overview';
  let deletingProduct = null;
  let deletingOrder = null;
  let deletingUser = null;

  function syncModalState() {
    document.body.classList.toggle('is-modal-open', Boolean(document.querySelector('dialog[open]')));
  }
  function openDialog(dialog) {
    if (!dialog.open) dialog.showModal();
    syncModalState();
  }
  function closeDialog(dialog) {
    if (dialog.open) dialog.close();
    syncModalState();
  }
  function focusHeading(selector) {
    $(selector)?.focus({ preventScroll: true });
  }
  function accountMessage(message) { accountFeedback.textContent = message; }
  function adminMessage(message) { adminFeedback.textContent = message; }
  function clearFieldError(input) {
    if (!input?.id) return;
    document.getElementById(`${input.id}-error`)?.remove();
    input.removeAttribute('aria-invalid');
    input.removeAttribute('aria-errormessage');
  }
  function showFieldError(input, message) {
    clearFieldError(input);
    const error = document.createElement('p');
    error.id = `${input.id}-error`;
    error.className = 'demo-error';
    error.setAttribute('role', 'alert');
    error.textContent = message;
    input.insertAdjacentElement('afterend', error);
    input.setAttribute('aria-invalid', 'true');
    input.setAttribute('aria-errormessage', error.id);
    input.focus();
  }
  [accountForm, productForm, orderForm, userForm].forEach(form => {
    form.addEventListener('input', event => clearFieldError(event.target));
    form.addEventListener('change', event => clearFieldError(event.target));
  });

  function persistSandbox(message) {
    try {
      localStorage.setItem(adminKey, JSON.stringify(sandbox));
      sandboxUnsaved = false;
      adminMessage(message);
      return true;
    } catch {
      sandboxUnsaved = true;
      adminMessage('ذخیره محلی در دسترس نیست؛ تغییرات فقط تا پایان این اجرا باقی می‌مانند.');
      return false;
    }
  }

  function renderAccountShortcut() {
    $('#account-shortcut-label').textContent = profile ? 'نمایه محلی فعال' : 'نمایه روی دستگاه';
  }
  function renderProfile() {
    if (!profile) return;
    $('#account-home-title').textContent = `سلام، ${profile.name}`;
    $('#account-cart-count').textContent = $('[data-cart-count]')?.textContent || '۰';
    $('#account-favorites-count').textContent = $('[data-favorites-count]')?.textContent || '۰';
  }
  function showAccountView(view, options = {}) {
    accountView = view;
    $('#account-entry-view').hidden = view !== 'entry';
    $('#account-home-view').hidden = view !== 'home';
    $('#account-orders-view').hidden = view !== 'orders';
    if (view === 'entry') {
      editingProfile = Boolean(options.edit && profile);
      $('#account-entry-title').textContent = editingProfile ? 'ویرایش نمایه' : 'خوش آمدید';
      $('#account-submit-label').textContent = editingProfile ? 'ذخیره نام نمایشی' : 'ادامه با نام نمایشی';
      $('#account-name').value = editingProfile ? profile.name : '';
      clearFieldError($('#account-name'));
      $('[data-account-guest]').hidden = editingProfile;
      $('[data-account-cancel-edit]').hidden = !editingProfile;
    }
    if (view === 'home') renderProfile();
    if (options.focus) focusHeading(view === 'home' ? '#account-home-title' : view === 'orders' ? '#account-orders-title' : '#account-entry-title');
    $('.account-drawer .drawer-scroll').scrollTop = 0;
  }
  function openAccount() {
    accountMessage('');
    showAccountView(profile ? 'home' : 'entry');
    openDialog(accountDialog);
  }
  function saveProfile(name) {
    profile = { name };
    let saved = true;
    try { localStorage.setItem(profileKey, JSON.stringify(profile)); } catch { saved = false; }
    renderAccountShortcut();
    showAccountView('home', { focus: true });
    accountMessage(saved ? 'نمایه نمایشی در همین دستگاه ذخیره شد.' : 'ذخیره محلی در دسترس نیست؛ نمایه فقط تا پایان این اجرا باقی می‌ماند.');
  }
  function signOutProfile() {
    profile = null;
    let removed = true;
    try { localStorage.removeItem(profileKey); } catch { removed = false; }
    renderAccountShortcut();
    showAccountView('entry', { focus: true });
    accountMessage(removed ? 'نام نمایشی حذف شد؛ سبد و منتخب‌ها حفظ شدند.' : 'ذخیره محلی در دسترس نیست؛ پاک شدن نام پس از خروج قابل تأیید نیست.');
  }
  accountForm.addEventListener('submit', event => {
    event.preventDefault();
    const input = $('#account-name');
    const name = input.value.trim().slice(0, 40);
    if (!name) {
      showFieldError(input, 'لطفاً یک نام نمایشی وارد کنید یا مهمان ادامه دهید.');
      return;
    }
    clearFieldError(input);
    saveProfile(name);
  });

  function rowActionArt(name) {
    return `<span class="row-action-art" aria-hidden="true"><img src="assets/ui-${name}-3d.webp" alt=""></span>`;
  }
  function stat(value, label) {
    return `<div class="admin-stat"><strong>${escapeHTML(format(value))}</strong><span>${escapeHTML(label)}</span></div>`;
  }
  function renderOverview() {
    const missingPrices = sandbox.products.filter(product => product.price === null).length;
    const completed = sandbox.orders.filter(order => order.status === 'completed').length;
    $('#admin-overview-stats').innerHTML = [
      stat(sandbox.products.length, 'محصول نمونه'),
      stat(sandbox.orders.length, 'سفارش ساختگی'),
      stat(sandbox.users.length, 'کاربر ساختگی'),
      stat(missingPrices, 'قیمت نمونه نامشخص'),
      stat(completed, 'سفارش نمونه تکمیل‌شده'),
      stat(sandbox.users.filter(user => user.active).length, 'کاربر نمونه فعال')
    ].join('');
  }
  function priceText(product) {
    return product.price === null ? 'قیمت نمونه: نامشخص' : `قیمت آزمایشی: ${format(product.price)} تومان`;
  }
  function stockText(product) {
    return product.stock === null ? 'موجودی نمونه: نامشخص' : product.stock === 0 ? 'موجودی نمونه: صفر' : `موجودی آزمایشی: ${format(product.stock)} کیلوگرم`;
  }
  function renderProductsList() {
    const query = normalize($('#admin-product-search').value);
    const products = sandbox.products.filter(product => !query || normalize(`${product.name} ${product.category}`).includes(query));
    $('#admin-products-list').innerHTML = products.length ? products.map(product => {
      const id = escapeHTML(product.id), name = escapeHTML(product.name);
      const confirm = deletingProduct === product.id ? `<div class="demo-confirm"><span>محصول نمونه «${name}» حذف شود؟</span><div><button type="button" data-admin-confirm-delete-product="${id}">بله، حذف نمونه</button><button type="button" data-admin-cancel-delete-product>انصراف</button></div></div>` : '';
      return `<article class="admin-list-row"><div class="admin-row-head"><div><strong class="admin-row-title">${name}</strong><p class="admin-row-meta">${escapeHTML(product.category)} · ${escapeHTML(priceText(product))}<br>${escapeHTML(stockText(product))}</p></div><span class="admin-row-badge">نمونه</span></div><div class="admin-row-actions"><button type="button" data-admin-edit-product="${id}" aria-label="ویرایش محصول نمونه: ${name}">${rowActionArt('edit')}<span>ویرایش</span></button><button class="demo-danger" type="button" data-admin-delete-product="${id}" aria-label="حذف محصول نمونه: ${name}">${rowActionArt('delete')}<span>حذف</span></button></div>${confirm}</article>`;
    }).join('') : `<div class="admin-list-empty">${query ? 'محصول نمونه‌ای با این نام پیدا نشد.' : 'هنوز محصول نمونه‌ای در پنل نیست.'}</div>`;
  }
  function orderDate(timestamp) {
    try { return new Intl.DateTimeFormat('fa-IR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(timestamp)); }
    catch { return 'تاریخ نامشخص'; }
  }
  function renderOrdersList() {
    const orders = [...sandbox.orders].sort((a, b) => b.createdAt - a.createdAt);
    $('#admin-orders-list').innerHTML = orders.length ? orders.map(order => {
      const id = escapeHTML(order.id), name = escapeHTML(order.productName);
      const options = Object.entries(statuses).map(([key, label]) => `<option value="${key}" ${order.status === key ? 'selected' : ''}>${escapeHTML(label)}</option>`).join('');
      const confirm = deletingOrder === order.id ? `<div class="demo-confirm"><span>این سفارش ساختگی حذف شود؟</span><div><button type="button" data-admin-confirm-delete-order="${id}">بله، حذف نمونه</button><button type="button" data-admin-cancel-delete-order>انصراف</button></div></div>` : '';
      return `<article class="admin-list-row"><div class="admin-row-head"><div><strong class="admin-row-title">${name}</strong><p class="admin-row-meta">${escapeHTML(format(order.quantity))} کیلوگرم نمونه · ${escapeHTML(orderDate(order.createdAt))}</p></div><span class="admin-row-badge">ساختگی</span></div><label class="demo-search-label" for="order-status-${id}">وضعیت سفارش نمونه</label><select id="order-status-${id}" class="admin-order-status" data-admin-order-status="${id}" aria-label="وضعیت سفارش نمونه: ${name}">${options}</select><div class="admin-row-actions"><button class="demo-danger" type="button" data-admin-delete-order="${id}" aria-label="حذف سفارش نمونه: ${name}">${rowActionArt('delete')}<span>حذف نمونه</span></button></div>${confirm}</article>`;
    }).join('') : '<div class="admin-list-empty">هنوز سفارش ساختگی ایجاد نشده است. این بخش سفارش‌های واقعی را نشان نمی‌دهد.</div>';
  }
  function renderUsersList() {
    $('#admin-users-list').innerHTML = sandbox.users.length ? sandbox.users.map(user => {
      const id = escapeHTML(user.id), alias = escapeHTML(user.alias);
      const action = user.active ? 'غیرفعال کردن' : 'فعال کردن';
      const confirm = deletingUser === user.id ? `<div class="demo-confirm"><span>کاربر ساختگی «${alias}» حذف شود؟</span><div><button type="button" data-admin-confirm-delete-user="${id}">بله، حذف نمونه</button><button type="button" data-admin-cancel-delete-user>انصراف</button></div></div>` : '';
      return `<article class="admin-list-row"><div class="admin-row-head"><div><strong class="admin-row-title">${alias}</strong><p class="admin-row-meta">حساب ساختگی و مستقل از کاربران واقعی</p></div><span class="admin-row-badge">${user.active ? 'فعال نمونه' : 'غیرفعال نمونه'}</span></div><div class="admin-row-actions"><button type="button" data-admin-toggle-user="${id}" aria-label="${action} کاربر نمونه: ${alias}">${rowActionArt('edit')}<span>${action}</span></button><button class="demo-danger" type="button" data-admin-delete-user="${id}" aria-label="حذف کاربر نمونه: ${alias}">${rowActionArt('delete')}<span>حذف</span></button></div>${confirm}</article>`;
    }).join('') : '<div class="admin-list-empty">هنوز کاربر ساختگی ایجاد نشده است. اطلاعات کاربران واقعی در این پنل وجود ندارد.</div>';
  }
  function filteredReportOrders() {
    const range = $('#admin-report-range').value;
    const now = new Date();
    let start = -Infinity;
    if (range === 'today') start = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    if (range === 'week') start = now.getTime() - 7 * 86400000;
    if (range === 'month') start = now.getTime() - 30 * 86400000;
    return sandbox.orders.filter(order => order.createdAt >= start && order.createdAt <= now.getTime());
  }
  function renderReports() {
    const orders = filteredReportOrders();
    const completed = orders.filter(order => order.status === 'completed').length;
    const preparing = orders.filter(order => order.status === 'preparing').length;
    const quantity = orders.reduce((sum, order) => sum + order.quantity, 0);
    $('#admin-report-stats').innerHTML = [
      stat(orders.length, 'سفارش ساختگی'),
      stat(preparing, 'در حال آماده‌سازی نمونه'),
      stat(completed, 'تکمیل‌شده نمونه'),
      stat(quantity, 'کیلوگرم آزمایشی'),
      stat(sandbox.users.length, 'کاربر ساختگی فعلی'),
      stat(sandbox.users.filter(user => user.active).length, 'کاربر نمونه فعال')
    ].join('');
    $('#admin-report-bars').innerHTML = Object.entries(statuses).map(([key, label]) => {
      const count = orders.filter(order => order.status === key).length;
      const percent = orders.length ? Math.round(count * 100 / orders.length) : 0;
      return `<div class="report-bar"><div class="report-bar-label"><span>${escapeHTML(label)}</span><strong>${escapeHTML(format(count))}</strong></div><div class="report-track" aria-hidden="true"><div class="report-fill" style="width:${percent}%"></div></div></div>`;
    }).join('');
  }
  function renderAdmin() {
    renderOverview();
    renderProductsList();
    renderOrdersList();
    renderUsersList();
    renderReports();
  }
  function activateTab(tab, options = {}) {
    if (!['overview', 'products', 'orders', 'users', 'reports', 'settings'].includes(tab)) return;
    adminTab = tab;
    $$('[role="tab"][data-admin-tab]').forEach(button => {
      const selected = button.dataset.adminTab === tab;
      button.setAttribute('aria-selected', String(selected));
      button.tabIndex = selected ? 0 : -1;
    });
    $$('[data-admin-panel]').forEach(panel => { panel.hidden = panel.dataset.adminPanel !== tab; });
    if (options.focus) $(`#admin-tab-${tab}`).focus({ preventScroll: true });
    $('.admin-scroll').scrollTop = 0;
    renderAdmin();
  }
  function showAdminGate() {
    adminActive = false;
    $('#admin-gate').hidden = false;
    $('#admin-workspace').hidden = true;
    $('#admin-tabs').hidden = true;
    $('#admin-reset-confirm').hidden = true;
    closeProductForm();
    closeOrderForm();
    closeUserForm();
    deletingProduct = null;
    deletingOrder = null;
    deletingUser = null;
    $('.admin-scroll').scrollTop = 0;
  }
  function openAdmin() {
    // Do not discard session-only edits when storage is blocked or full.
    if (!sandboxUnsaved) sandbox = loadSandbox();
    adminMessage(sandboxUnsaved ? 'ذخیره محلی در دسترس نیست؛ تغییرات فقط تا پایان این اجرا باقی می‌مانند.' : '');
    showAdminGate();
    openDialog(adminDialog);
  }
  function enterAdmin() {
    adminActive = true;
    $('#admin-gate').hidden = true;
    $('#admin-workspace').hidden = false;
    $('#admin-tabs').hidden = false;
    activateTab('overview', { focus: true });
  }

  function closeProductForm() {
    productForm.hidden = true;
    $('#admin-products-browser').hidden = false;
    delete productForm.dataset.editId;
  }
  function openProductForm(id) {
    const product = id ? sandbox.products.find(item => item.id === id) : null;
    if (id && !product) return;
    if (!product && sandbox.products.length >= 250) { adminMessage('حداکثر ۲۵۰ محصول نمونه قابل نگهداری است.'); return; }
    productForm.reset();
    productForm.querySelectorAll('input,select').forEach(clearFieldError);
    if (product) productForm.dataset.editId = product.id; else delete productForm.dataset.editId;
    $('#admin-product-form-title').textContent = product ? 'ویرایش محصول نمونه' : 'محصول نمونه جدید';
    $('#admin-product-name').value = product?.name || '';
    $('#admin-product-category').value = product?.category || categories[0];
    $('#admin-product-price').value = product?.price === null || !product ? '' : String(product.price);
    $('#admin-product-stock').value = product?.stock === null || !product ? '' : String(product.stock);
    productForm.hidden = false;
    $('#admin-products-browser').hidden = true;
    adminMessage('');
    focusHeading('#admin-product-form-title');
    $('.admin-scroll').scrollTop = 0;
  }
  function optionalNumber(value, maximum, allowZero) {
    const text = toEnglishDigits(value).trim().replace(/[,\u066c\s]/g, '');
    if (!text) return { valid: true, value: null };
    if (!/^\d+$/.test(text)) return { valid: false };
    const parsed = Number(text);
    return Number.isSafeInteger(parsed) && parsed <= maximum && (allowZero ? parsed >= 0 : parsed > 0) ? { valid: true, value: parsed } : { valid: false };
  }
  productForm.addEventListener('submit', event => {
    event.preventDefault();
    const name = $('#admin-product-name').value.trim().slice(0, 80);
    if (!name) { showFieldError($('#admin-product-name'), 'نام محصول نمونه را وارد کنید.'); return; }
    const price = optionalNumber($('#admin-product-price').value, maxPrice, false);
    if (!price.valid) { showFieldError($('#admin-product-price'), 'قیمت آزمایشی باید یک عدد صحیح مثبت و حداکثر ۹٬۹۹۹٬۹۹۹٬۹۹۹ تومان باشد.'); return; }
    const stock = optionalNumber($('#admin-product-stock').value, maxStock, true);
    if (!stock.valid) { showFieldError($('#admin-product-stock'), 'موجودی آزمایشی باید عددی بین صفر و ۱٬۰۰۰٬۰۰۰ کیلوگرم باشد.'); return; }
    const existing = productForm.dataset.editId;
    const product = { id: existing || identifier('product'), name, category: categories.includes($('#admin-product-category').value) ? $('#admin-product-category').value : 'سایر', price: price.value, stock: stock.value };
    const index = sandbox.products.findIndex(item => item.id === existing);
    if (existing && index < 0) { adminMessage('محصول نمونه دیگر وجود ندارد.'); return; }
    if (index >= 0) sandbox.products[index] = product;
    else if (sandbox.products.length < 250) sandbox.products.push(product);
    else { adminMessage('حداکثر ۲۵۰ محصول نمونه قابل نگهداری است.'); return; }
    closeProductForm();
    $('#admin-product-search').value = '';
    persistSandbox('محصول در پنل نمایشی ذخیره شد؛ فروشگاه اصلی تغییری نکرد.');
    renderAdmin();
    $('[data-admin-add-product]').focus({ preventScroll: true });
  });

  function closeOrderForm() {
    orderForm.hidden = true;
    $('#admin-orders-browser').hidden = false;
  }
  function openOrderForm() {
    if (!sandbox.products.length) { adminMessage('ابتدا یک محصول نمونه اضافه کنید.'); return; }
    if (sandbox.orders.length >= 500) { adminMessage('حداکثر ۵۰۰ سفارش ساختگی قابل نگهداری است.'); return; }
    const select = $('#admin-order-product');
    select.textContent = '';
    sandbox.products.forEach(product => {
      const option = document.createElement('option');
      option.value = product.id;
      option.textContent = product.name;
      select.appendChild(option);
    });
    orderForm.querySelectorAll('input,select').forEach(clearFieldError);
    $('#admin-order-quantity').value = '۱';
    orderForm.hidden = false;
    $('#admin-orders-browser').hidden = true;
    adminMessage('');
    orderForm.querySelector('h4').focus({ preventScroll: true });
    $('.admin-scroll').scrollTop = 0;
  }
  orderForm.addEventListener('submit', event => {
    event.preventDefault();
    const product = sandbox.products.find(item => item.id === $('#admin-order-product').value);
    if (!product) { showFieldError($('#admin-order-product'), 'یک محصول نمونه انتخاب کنید.'); return; }
    const quantity = optionalNumber($('#admin-order-quantity').value, maxQuantity, false);
    if (!quantity.valid || quantity.value === null) { showFieldError($('#admin-order-quantity'), 'مقدار نمونه باید یک عدد صحیح از ۱ تا ۱۰۰٬۰۰۰ باشد.'); return; }
    if (sandbox.orders.length >= 500) { adminMessage('حداکثر ۵۰۰ سفارش ساختگی قابل نگهداری است.'); return; }
    sandbox.orders.push({ id: identifier('order'), productId: product.id, productName: product.name, quantity: quantity.value, status: 'pending', createdAt: Date.now() });
    closeOrderForm();
    persistSandbox('سفارش ساختگی فقط در پنل نمایشی ایجاد شد؛ چیزی ارسال نشده است.');
    renderAdmin();
    $('[data-admin-add-order]').focus({ preventScroll: true });
  });

  function closeUserForm() {
    userForm.hidden = true;
    $('#admin-users-browser').hidden = false;
  }
  function openUserForm() {
    if (sandbox.users.length >= 250) { adminMessage('حداکثر ۲۵۰ کاربر نمونه قابل نگهداری است.'); return; }
    userForm.reset();
    userForm.querySelectorAll('input,select').forEach(clearFieldError);
    userForm.hidden = false;
    $('#admin-users-browser').hidden = true;
    adminMessage('');
    focusHeading('#admin-user-form-title');
    $('.admin-scroll').scrollTop = 0;
  }
  userForm.addEventListener('submit', event => {
    event.preventDefault();
    const alias = $('#admin-user-name').value.trim().slice(0, 40);
    if (!alias) { showFieldError($('#admin-user-name'), 'یک نام مستعار نمونه وارد کنید.'); return; }
    if (sandbox.users.length >= 250) { adminMessage('حداکثر ۲۵۰ کاربر نمونه قابل نگهداری است.'); return; }
    sandbox.users.push({ id: identifier('user'), alias, active: true });
    closeUserForm();
    persistSandbox('کاربر ساختگی فقط در پنل نمایشی ایجاد شد.');
    renderAdmin();
    $('[data-admin-add-user]').focus({ preventScroll: true });
  });

  $('#admin-product-search').addEventListener('input', renderProductsList);
  $('#admin-report-range').addEventListener('change', renderReports);
  $('#admin-orders-list').addEventListener('change', event => {
    const select = event.target.closest('[data-admin-order-status]');
    if (!select) return;
    const order = sandbox.orders.find(item => item.id === select.dataset.adminOrderStatus);
    if (!order || !Object.prototype.hasOwnProperty.call(statuses, select.value)) return;
    order.status = select.value;
    const id = order.id;
    persistSandbox('وضعیت سفارش ساختگی تغییر کرد.');
    renderAdmin();
    $$('[data-admin-order-status]').find(element => element.dataset.adminOrderStatus === id)?.focus({ preventScroll: true });
  });
  $('#admin-tabs').addEventListener('keydown', event => {
    if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return;
    const tabs = $$('[role="tab"][data-admin-tab]');
    const index = tabs.findIndex(tab => tab.dataset.adminTab === adminTab);
    let next = index;
    if (event.key === 'Home') next = 0;
    else if (event.key === 'End') next = tabs.length - 1;
    else if (event.key === 'ArrowLeft') next = (index + 1) % tabs.length;
    else next = (index - 1 + tabs.length) % tabs.length;
    event.preventDefault();
    activateTab(tabs[next].dataset.adminTab, { focus: true });
  });

  document.addEventListener('click', event => {
    const button = event.target.closest('button');
    if (!button) return;
    if (button.matches('[data-open-account]')) { openAccount(); return; }
    if (button.matches('[data-close-account]')) { closeDialog(accountDialog); return; }
    if (button.matches('[data-account-guest]')) { saveProfile('مهمان'); return; }
    if (button.matches('[data-account-edit]')) { accountMessage(''); showAccountView('entry', { edit: true, focus: true }); return; }
    if (button.matches('[data-account-cancel-edit], [data-account-back]')) { accountMessage(''); showAccountView('home', { focus: true }); return; }
    if (button.matches('[data-account-orders]')) { accountMessage(''); showAccountView('orders', { focus: true }); return; }
    if (button.matches('[data-account-signout]')) { signOutProfile(); return; }
    if (button.matches('[data-account-cart]')) { closeDialog(accountDialog); $('.dock-cart').click(); return; }
    if (button.matches('[data-account-favorites]')) { closeDialog(accountDialog); $('[data-dock-favorites]').click(); return; }
    if (button.matches('[data-account-browse]')) { closeDialog(accountDialog); $('[data-dock-products]').click(); return; }
    if (button.matches('[data-open-admin]')) { openAdmin(); return; }
    if (button.matches('[data-close-admin]')) { closeDialog(adminDialog); return; }
    if (button.matches('[data-admin-enter]')) { enterAdmin(); return; }
    if (button.matches('[data-admin-tab]')) { activateTab(button.dataset.adminTab); return; }
    if (button.matches('[data-admin-add-product]')) { openProductForm(); return; }
    if (button.matches('[data-admin-edit-product]')) { openProductForm(button.dataset.adminEditProduct); return; }
    if (button.matches('[data-admin-cancel-product]')) { closeProductForm(); $('[data-admin-add-product]').focus({ preventScroll: true }); return; }
    if (button.matches('[data-admin-delete-product]')) { deletingProduct = button.dataset.adminDeleteProduct; renderProductsList(); return; }
    if (button.matches('[data-admin-cancel-delete-product]')) { deletingProduct = null; renderProductsList(); return; }
    if (button.matches('[data-admin-confirm-delete-product]')) {
      const id = button.dataset.adminConfirmDeleteProduct;
      sandbox.products = sandbox.products.filter(product => product.id !== id);
      deletingProduct = null;
      persistSandbox('محصول نمونه حذف شد؛ فروشگاه اصلی تغییری نکرد.');
      renderAdmin();
      return;
    }
    if (button.matches('[data-admin-add-order]')) { openOrderForm(); return; }
    if (button.matches('[data-admin-cancel-order]')) { closeOrderForm(); $('[data-admin-add-order]').focus({ preventScroll: true }); return; }
    if (button.matches('[data-admin-delete-order]')) { deletingOrder = button.dataset.adminDeleteOrder; renderOrdersList(); return; }
    if (button.matches('[data-admin-cancel-delete-order]')) { deletingOrder = null; renderOrdersList(); return; }
    if (button.matches('[data-admin-confirm-delete-order]')) {
      const id = button.dataset.adminConfirmDeleteOrder;
      sandbox.orders = sandbox.orders.filter(order => order.id !== id);
      deletingOrder = null;
      persistSandbox('سفارش ساختگی حذف شد.');
      renderAdmin();
      return;
    }
    if (button.matches('[data-admin-add-user]')) { openUserForm(); return; }
    if (button.matches('[data-admin-cancel-user]')) { closeUserForm(); $('[data-admin-add-user]').focus({ preventScroll: true }); return; }
    if (button.matches('[data-admin-toggle-user]')) {
      const user = sandbox.users.find(item => item.id === button.dataset.adminToggleUser);
      if (!user) return;
      user.active = !user.active;
      persistSandbox('وضعیت کاربر ساختگی تغییر کرد.');
      renderAdmin();
      return;
    }
    if (button.matches('[data-admin-delete-user]')) { deletingUser = button.dataset.adminDeleteUser; renderUsersList(); return; }
    if (button.matches('[data-admin-cancel-delete-user]')) { deletingUser = null; renderUsersList(); return; }
    if (button.matches('[data-admin-confirm-delete-user]')) {
      const id = button.dataset.adminConfirmDeleteUser;
      sandbox.users = sandbox.users.filter(user => user.id !== id);
      deletingUser = null;
      persistSandbox('کاربر ساختگی حذف شد.');
      renderAdmin();
      return;
    }
    if (button.matches('[data-admin-reset]')) { $('#admin-reset-confirm').hidden = false; return; }
    if (button.matches('[data-admin-reset-cancel]')) { $('#admin-reset-confirm').hidden = true; return; }
    if (button.matches('[data-admin-reset-confirm]')) {
      sandbox = freshSandbox();
      deletingProduct = null; deletingOrder = null; deletingUser = null;
      $('#admin-reset-confirm').hidden = true;
      $('#admin-product-search').value = '';
      persistSandbox('داده‌های پنل نمایشی بازنشانی شدند.');
      renderAdmin();
      return;
    }
    if (button.matches('[data-admin-exit]')) { showAdminGate(); focusHeading('#admin-gate-title'); }
  });

  // Android calls this before its existing product/cart/checkout Back handling.
  // Escape uses the same hierarchy so desktop and mobile behave consistently.
  window.NuteDemoBack = () => {
    if (adminDialog.open) {
      if (!adminActive) { closeDialog(adminDialog); return true; }
      if (!$('#admin-reset-confirm').hidden) { $('#admin-reset-confirm').hidden = true; return true; }
      if (adminTab === 'products' && !productForm.hidden) { closeProductForm(); $('[data-admin-add-product]').focus({ preventScroll: true }); return true; }
      if (adminTab === 'orders' && !orderForm.hidden) { closeOrderForm(); $('[data-admin-add-order]').focus({ preventScroll: true }); return true; }
      if (adminTab === 'users' && !userForm.hidden) { closeUserForm(); $('[data-admin-add-user]').focus({ preventScroll: true }); return true; }
      if (deletingProduct || deletingOrder || deletingUser) { deletingProduct = null; deletingOrder = null; deletingUser = null; renderAdmin(); return true; }
      if (adminTab !== 'overview') { activateTab('overview', { focus: true }); return true; }
      showAdminGate(); focusHeading('#admin-gate-title'); return true;
    }
    if (accountDialog.open) {
      if (accountView === 'orders' || (accountView === 'entry' && editingProfile && profile)) { showAccountView('home', { focus: true }); return true; }
      closeDialog(accountDialog); return true;
    }
    return false;
  };
  [accountDialog, adminDialog].forEach(dialog => {
    dialog.addEventListener('cancel', event => { event.preventDefault(); window.NuteDemoBack(); });
    dialog.addEventListener('click', event => { if (event.target === dialog) closeDialog(dialog); });
    dialog.addEventListener('close', syncModalState);
  });
  window.addEventListener('storage', event => {
    if (event.key === profileKey) { profile = loadProfile(); renderAccountShortcut(); if (accountDialog.open) showAccountView(profile ? 'home' : 'entry'); }
    if (event.key === adminKey && !sandboxUnsaved) { sandbox = loadSandbox(); if (adminDialog.open && adminActive) renderAdmin(); }
  });
  renderAccountShortcut();
})();
