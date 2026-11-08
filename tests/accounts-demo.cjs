const { chromium } = require('playwright-core');
const AxeBuilder = require('@axe-core/playwright').default;
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

(async () => {
  const watchdog = setTimeout(() => { console.error('Account/admin test timed out'); process.exit(1); }, 300000);
  const launch = { headless: true };
  if (process.env.CHROMIUM_PATH) launch.executablePath = process.env.CHROMIUM_PATH;
  if (process.env.CHROMIUM_ARGS) launch.args = JSON.parse(process.env.CHROMIUM_ARGS);
  const browser = await chromium.launch(launch);
  const context = await browser.newContext({ viewport: { width: 393, height: 844 }, isMobile: true, hasTouch: true, reducedMotion: 'reduce' });
  const page = await context.newPage();
  const baseURL = process.env.BASE_URL || 'http://127.0.0.1:4173/';
  const screenshotDir = process.env.SCREENSHOT_DIR;
  if (screenshotDir) fs.mkdirSync(screenshotDir, { recursive: true });
  const errors = [], failed = [], external = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('response', response => { if (response.status() >= 400) failed.push(`${response.status()} ${response.url()}`); });
  page.on('request', request => { if (!request.url().startsWith(baseURL)) external.push(request.url()); });
  async function capture(name) {
    if (screenshotDir) await page.screenshot({ path: path.join(screenshotDir, `${name}.png`) });
  }
  async function fresh(width = 393, height = 844) {
    await page.setViewportSize({ width, height });
    await page.goto(baseURL, { waitUntil: 'networkidle' });
    await page.evaluate(() => localStorage.clear());
    await page.reload({ waitUntil: 'networkidle' });
    await page.evaluate(() => document.fonts.ready);
  }
  async function axe(label) {
    const result = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
    assert.deepEqual(result.violations.map(v => v.id), [], `${label}: ${JSON.stringify(result.violations)}`);
  }
  async function tab(name) { await page.locator(`#admin-tab-${name}`).tap(); }
  async function statValue(container, label) {
    return page.locator(`${container} .admin-stat`).filter({ hasText: label }).locator('strong').innerText();
  }

  // Account is local-only: no password, phone, SMS or real authentication.
  await fresh();
  assert.equal(await page.locator('.access-tile').count(), 2);
  await page.locator('.product-card [data-add-product="pistachio"]').tap();
  await page.locator('.product-card [data-favorite-product="walnut"]').tap();
  await page.locator('[data-open-account]').tap();
  assert(await page.locator('#account-drawer').isVisible());
  assert.match(await page.locator('#account-entry-view').innerText(), /ورود واقعی نیست/);
  assert.equal(await page.locator('#account-drawer input[type="password"], #account-drawer input[type="tel"], #account-drawer input[type="email"]').count(), 0);
  await axe('account entry');
  await page.locator('#account-form button[type="submit"]').tap();
  assert.equal(await page.locator('#account-name').getAttribute('aria-invalid'), 'true');
  assert.match(await page.locator('#account-name-error').innerText(), /نام نمایشی/);
  await page.locator('[data-account-guest]').tap();
  assert.match(await page.locator('#account-home-title').innerText(), /مهمان/);
  assert.equal(await page.locator('#account-cart-count').innerText(), '۱');
  assert.equal(await page.locator('#account-favorites-count').innerText(), '۱');
  await axe('account profile');
  await page.locator('[data-account-edit]').tap();
  const payload = '<svg onload=window.pwned=1>';
  await page.locator('#account-name').fill(payload);
  await page.locator('#account-form button[type="submit"]').tap();
  assert.match(await page.locator('#account-home-title').innerText(), /<svg/);
  assert.equal(await page.evaluate(() => window.pwned), undefined, 'profile must be rendered as text, not HTML');
  await page.reload({ waitUntil: 'networkidle' });
  await page.locator('[data-open-account]').tap();
  assert.match(await page.locator('#account-home-title').innerText(), /<svg/, 'local profile persists on this device');
  await page.locator('[data-account-edit]').tap();
  await page.locator('#account-name').fill('دوست درخشان');
  await page.locator('#account-form button[type="submit"]').tap();
  await page.locator('[data-account-orders]').tap();
  assert.match(await page.locator('#account-orders-view').innerText(), /هیچ سفارشی ارسال یا ثبت نمی‌شود/);
  assert.equal(await page.evaluate(() => window.NuteDemoBack()), true);
  assert(await page.locator('#account-home-view').isVisible());
  await page.locator('[data-account-cart]').tap();
  assert(await page.locator('#cart-drawer').isVisible());
  assert.equal(await page.locator('.quote-item').count(), 1);
  await page.locator('[data-close-cart]').tap();
  await page.locator('[data-open-account]').tap();
  await page.locator('[data-account-favorites]').tap();
  assert.equal(await page.locator('.product-card').count(), 1);
  assert.match(await page.locator('.product-card h3').innerText(), /گردو/);
  await page.locator('[data-dock-home]').tap();
  await page.locator('[data-open-account]').tap();
  await page.locator('[data-account-signout]').tap();
  assert.equal(await page.evaluate(() => localStorage.getItem('derakhshan-local-profile-demo-v1')), null);
  assert.equal(await page.locator('.mobile-dock [data-cart-count]').textContent(), '۱');
  assert.equal(await page.locator('.mobile-dock [data-favorites-count]').textContent(), '۱');
  await page.locator('[data-close-account]').tap();
  console.log('local profile, guest, edit, safe text, shortcuts, persistence and sign-out: PASS');

  const shopNames = await page.locator('.product-card h3').allTextContents();
  await page.locator('[data-open-admin]').tap();
  assert.match(await page.locator('#admin-gate').innerText(), /رمز و امنیت واقعی ندارد/);
  assert.equal(await page.locator('#admin-drawer input[type="password"]').count(), 0);
  await axe('admin demo gate');
  await page.locator('[data-admin-enter]').tap();
  assert.equal(await statValue('#admin-overview-stats', 'محصول نمونه'), '۵');
  assert.equal(await statValue('#admin-overview-stats', 'کاربر ساختگی'), '۰');
  assert.equal(await statValue('#admin-overview-stats', 'سفارش ساختگی'), '۰');
  await axe('admin dashboard');
  await capture('393-admin-dashboard');

  // Product CRUD is isolated from the real shop and accepts optional test-only numbers.
  await tab('products');
  assert.equal(await page.locator('#admin-products-list .admin-list-row').count(), 5);
  await page.locator('#admin-product-search').fill('پسته');
  assert.equal(await page.locator('#admin-products-list .admin-list-row').count(), 1);
  await page.locator('#admin-product-search').fill('');
  await page.locator('[data-admin-add-product]').tap();
  await axe('admin product form');
  await page.locator('#admin-product-form button[type="submit"]').tap();
  assert.equal(await page.locator('#admin-product-name').getAttribute('aria-invalid'), 'true');
  await page.locator('#admin-product-name').fill('محصول نمونه تست');
  await page.locator('#admin-product-price').fill('۱۲x');
  await page.locator('#admin-product-form button[type="submit"]').tap();
  assert.equal(await page.locator('#admin-product-price').getAttribute('aria-invalid'), 'true');
  await page.locator('#admin-product-price').fill('۱۲٬۳۴۵');
  await page.locator('#admin-product-stock').fill('۰');
  await page.locator('#admin-product-form button[type="submit"]').tap();
  let product = page.locator('#admin-products-list .admin-list-row').filter({ hasText: 'محصول نمونه تست' });
  assert.match(await product.innerText(), /۱۲٬۳۴۵ تومان/);
  assert.match(await product.innerText(), /موجودی نمونه: صفر/);
  await product.locator('[data-admin-edit-product]').tap();
  await page.locator('#admin-product-name').fill('محصول ویرایش‌شده');
  await page.locator('#admin-product-price').fill('');
  await page.locator('#admin-product-stock').fill('۴');
  await page.locator('#admin-product-form button[type="submit"]').tap();
  product = page.locator('#admin-products-list .admin-list-row').filter({ hasText: 'محصول ویرایش‌شده' });
  assert.match(await product.innerText(), /قیمت نمونه: نامشخص/);
  assert.match(await product.innerText(), /۴ کیلوگرم/);
  await product.locator('[data-admin-delete-product]').tap();
  await product.locator('[data-admin-cancel-delete-product]').tap();
  assert.equal(await product.count(), 1, 'cancel must not delete the product');

  // Sample users never read or alter the device profile.
  await tab('users');
  await page.locator('[data-admin-add-user]').tap();
  await page.locator('#admin-user-name').fill('<b>ساختگی</b>');
  await page.locator('#admin-user-form button[type="submit"]').tap();
  let user = page.locator('#admin-users-list .admin-list-row').first();
  assert.match(await user.innerText(), /<b>ساختگی<\/b>/);
  assert.equal(await user.locator('b').count(), 0, 'sample alias must not be interpreted as markup');
  await user.locator('[data-admin-toggle-user]').tap();
  user = page.locator('#admin-users-list .admin-list-row').first();
  assert.match(await user.innerText(), /غیرفعال نمونه/);
  await user.locator('[data-admin-delete-user]').tap();
  await user.locator('[data-admin-cancel-delete-user]').tap();
  assert.equal(await user.count(), 1);

  // Explicitly created fake orders feed the live local reports, never real sales.
  await tab('orders');
  assert.match(await page.locator('#admin-orders-list').innerText(), /هنوز سفارش ساختگی ایجاد نشده/);
  await page.locator('[data-admin-add-order]').tap();
  await page.locator('#admin-order-product').selectOption({ label: 'محصول ویرایش‌شده' });
  await page.locator('#admin-order-quantity').fill('۰');
  await page.locator('#admin-order-form button[type="submit"]').tap();
  assert.equal(await page.locator('#admin-order-quantity').getAttribute('aria-invalid'), 'true');
  await page.locator('#admin-order-quantity').fill('۳');
  await page.locator('#admin-order-form button[type="submit"]').tap();
  assert.equal(await page.locator('#admin-orders-list .admin-list-row').count(), 1);
  await page.locator('[data-admin-order-status]').selectOption('completed');
  await tab('reports');
  assert.equal(await statValue('#admin-report-stats', 'سفارش ساختگی'), '۱');
  assert.equal(await statValue('#admin-report-stats', 'تکمیل‌شده نمونه'), '۱');
  assert.equal(await statValue('#admin-report-stats', 'کیلوگرم آزمایشی'), '۳');
  assert.equal(await statValue('#admin-report-stats', 'کاربر ساختگی فعلی'), '۱');
  assert.match(await page.locator('#admin-panel-reports').innerText(), /فروش یا درآمد واقعی نیستند/);
  await axe('admin reports');
  await tab('users');
  user = page.locator('#admin-users-list .admin-list-row').first();
  await user.locator('[data-admin-delete-user]').tap();
  await user.locator('[data-admin-confirm-delete-user]').tap();
  assert.equal(await page.locator('#admin-users-list .admin-list-row').count(), 0);
  await tab('products');
  product = page.locator('#admin-products-list .admin-list-row').filter({ hasText: 'محصول ویرایش‌شده' });
  await product.locator('[data-admin-delete-product]').tap();
  await product.locator('[data-admin-confirm-delete-product]').tap();
  assert.equal(await product.count(), 0);
  await tab('orders');
  assert.match(await page.locator('#admin-orders-list').innerText(), /محصول ویرایش‌شده/, 'existing demo orders retain their product snapshot');
  await page.locator('[data-admin-add-order]').tap();
  await page.locator('#admin-order-form button[type="submit"]').tap();
  assert.equal(await page.locator('#admin-orders-list .admin-list-row').count(), 2);
  const extraOrder = page.locator('#admin-orders-list .admin-list-row').first();
  await extraOrder.locator('[data-admin-delete-order]').tap();
  await extraOrder.locator('[data-admin-confirm-delete-order]').tap();
  assert.equal(await page.locator('#admin-orders-list .admin-list-row').count(), 1);

  // Test date filtering, persistence and native Back-like hierarchy.
  await page.evaluate(() => {
    const key = 'derakhshan-admin-sandbox-v1';
    const state = JSON.parse(localStorage.getItem(key));
    state.orders.push({ id: 'older-demo-order', productId: 'base-pistachio', productName: 'سفارش نمونه قدیمی', quantity: 2, status: 'pending', createdAt: Date.now() - 10 * 86400000 });
    localStorage.setItem(key, JSON.stringify(state));
  });
  await page.reload({ waitUntil: 'networkidle' });
  await page.locator('[data-open-admin]').tap();
  assert(await page.locator('#admin-gate').isVisible(), 'admin session must not be mistaken for real authentication');
  await page.locator('[data-admin-enter]').tap();
  await tab('reports');
  assert.equal(await statValue('#admin-report-stats', 'سفارش ساختگی'), '۲');
  await page.locator('#admin-report-range').selectOption('week');
  assert.equal(await statValue('#admin-report-stats', 'سفارش ساختگی'), '۱');
  await page.locator('#admin-report-range').selectOption('month');
  assert.equal(await statValue('#admin-report-stats', 'سفارش ساختگی'), '۲');
  await tab('products');
  await page.locator('[data-admin-add-product]').tap();
  assert.equal(await page.evaluate(() => window.NuteDemoBack()), true);
  assert(await page.locator('#admin-products-browser').isVisible());
  assert.equal(await page.evaluate(() => window.NuteDemoBack()), true);
  assert(await page.locator('#admin-panel-overview').isVisible());
  assert.equal(await page.evaluate(() => window.NuteDemoBack()), true);
  assert(await page.locator('#admin-gate').isVisible());
  assert.equal(await page.evaluate(() => window.NuteDemoBack()), true);
  assert.equal(await page.locator('#admin-drawer').isVisible(), false);
  assert.deepEqual(await page.locator('.product-card h3').allTextContents(), shopNames, 'sandbox must not alter the customer catalog');
  assert.deepEqual(await page.locator('.shop-price strong').allTextContents(), Array(5).fill('قیمت ثبت نشده'), 'test prices must never leak into the shop');

  // Reset is explicitly confirmed and leaves the local profile, cart and favorites intact.
  await page.locator('[data-open-account]').tap();
  await page.locator('#account-name').fill('نمایه نمونه');
  await page.locator('#account-form button[type="submit"]').tap();
  await page.locator('[data-close-account]').tap();
  await page.locator('[data-open-admin]').tap();
  await page.locator('[data-admin-enter]').tap();
  await tab('settings');
  await page.locator('[data-admin-reset]').tap();
  await page.locator('[data-admin-reset-cancel]').tap();
  assert.equal(await page.evaluate(() => JSON.parse(localStorage.getItem('derakhshan-admin-sandbox-v1')).orders.length), 2);
  await page.locator('[data-admin-reset]').tap();
  await page.locator('[data-admin-reset-confirm]').tap();
  assert.deepEqual(await page.evaluate(() => {
    const state = JSON.parse(localStorage.getItem('derakhshan-admin-sandbox-v1'));
    return [state.products.length, state.orders.length, state.users.length];
  }), [5, 0, 0]);
  assert.equal(await page.locator('.mobile-dock [data-cart-count]').textContent(), '۱');
  assert.equal(await page.locator('.mobile-dock [data-favorites-count]').textContent(), '۱');
  assert.equal(await page.evaluate(() => JSON.parse(localStorage.getItem('derakhshan-local-profile-demo-v1')).name), 'نمایه نمونه');
  await page.locator('[data-close-admin]').tap();
  console.log('admin sample products, users, orders, reports, persistence, reset and isolation: PASS');

  // Storage can be blocked or full on real devices: keep edits in memory and
  // warn instead of silently losing them when the panel is reopened.
  await fresh();
  await page.evaluate(() => {
    window.originalSetItem = Storage.prototype.setItem;
    Storage.prototype.setItem = () => { throw new DOMException('Storage blocked', 'QuotaExceededError'); };
  });
  await page.locator('[data-open-admin]').tap();
  await page.locator('[data-admin-enter]').tap();
  await tab('products');
  await page.locator('[data-admin-add-product]').tap();
  await page.locator('#admin-product-name').fill('نمونه فقط در حافظه');
  await page.locator('#admin-product-form button[type="submit"]').tap();
  assert.match(await page.locator('#admin-feedback').innerText(), /ذخیره محلی در دسترس نیست/);
  await page.locator('[data-close-admin]').tap();
  await page.locator('[data-open-admin]').tap();
  await page.locator('[data-admin-enter]').tap();
  await tab('products');
  assert.equal(await page.locator('#admin-products-list .admin-list-row').filter({ hasText: 'نمونه فقط در حافظه' }).count(), 1);
  await page.evaluate(() => { Storage.prototype.setItem = window.originalSetItem; });
  await page.locator('[data-close-admin]').tap();
  console.log('storage-unavailable warning and session-only fallback: PASS');

  // Responsive entry points, navigation and scrollable dialogs through 200% text.
  for (const width of [320, 360, 393, 680]) {
    for (const scale of [100, 140, 200]) {
      await fresh(width);
      await page.addStyleTag({ content: `html{-webkit-text-size-adjust:${scale}%!important}` });
      const entry = await page.evaluate(() => {
        const rect = el => { const r = el.getBoundingClientRect(); return { x: r.x, right: r.right, y: r.y, bottom: r.bottom, width: r.width, height: r.height }; };
        return { width: innerWidth, docWidth: document.documentElement.scrollWidth, buttons: [...document.querySelectorAll('.access-tile')].map(el => ({ box: rect(el), text: rect(el.querySelector('.access-copy')) })) };
      });
      assert.equal(entry.docWidth, width, `${width}/${scale}%: document overflow`);
      for (const { box, text } of entry.buttons) {
        assert(box.x >= 0 && box.right <= width && box.height >= 44, `${width}/${scale}%: access button clipped`);
        assert(text.x >= box.x - 1 && text.right <= box.right + 1 && text.y >= box.y - 1 && text.bottom <= box.bottom + 1, `${width}/${scale}%: access text clipped`);
      }
      await page.locator('[data-open-account]').tap();
      let dimensions = await page.locator('#account-drawer').evaluate(el => ({ width: el.clientWidth, scrollWidth: el.scrollWidth, height: el.clientHeight, viewport: innerHeight, innerWidth }));
      assert(dimensions.width <= width && dimensions.scrollWidth <= dimensions.width + 1 && dimensions.height <= dimensions.viewport + 1, `${width}/${scale}%: account drawer overflow ${JSON.stringify(dimensions)}`);
      await page.locator('[data-account-guest]').tap();
      await page.locator('[data-account-orders]').tap();
      assert(await page.locator('#account-orders-title').isVisible());
      await page.locator('[data-close-account]').tap();
      await page.locator('[data-open-admin]').tap();
      await page.locator('[data-admin-enter]').tap();
      dimensions = await page.locator('#admin-drawer').evaluate(el => ({ width: el.clientWidth, scrollWidth: el.scrollWidth, height: el.clientHeight, viewport: innerHeight }));
      assert(dimensions.width <= width && dimensions.scrollWidth <= dimensions.width + 1 && dimensions.height <= dimensions.viewport + 1, `${width}/${scale}%: admin drawer overflow ${JSON.stringify(dimensions)}`);
      const navLabelsFit = await page.locator('#admin-tabs button').evaluateAll(buttons => buttons.every(button => {
        const label = button.lastElementChild, item = button.getBoundingClientRect(), r = label.getBoundingClientRect();
        return r.left >= item.left - 1 && r.right <= item.right + 1 && r.height <= parseFloat(getComputedStyle(label).lineHeight) + 1;
      }));
      assert(navLabelsFit, `${width}/${scale}%: admin tab labels must not clip or split`);
      await tab('users');
      await page.locator('[data-admin-add-user]').tap();
      await page.locator('#admin-user-name').fill('کاربر نمونه');
      await page.locator('#admin-user-form button[type="submit"]').tap();
      assert.equal(await page.locator('#admin-users-list .admin-list-row').count(), 1);
      await page.locator('[data-close-admin]').tap();
    }
    console.log(`width ${width}: account/admin access and 100–200% text PASS`);
  }
  await fresh(568, 320);
  await page.locator('[data-open-admin]').tap();
  await page.locator('[data-admin-enter]').tap();
  await tab('products');
  await page.locator('[data-admin-add-product]').tap();
  await page.locator('#admin-product-name').fill('نمونه افقی');
  await page.locator('#admin-product-form button[type="submit"]').tap();
  assert.equal(await page.locator('#admin-products-list .admin-list-row').filter({ hasText: 'نمونه افقی' }).count(), 1);
  assert.deepEqual(errors, []); assert.deepEqual(failed, []); assert.deepEqual(external, []);
  console.log('landscape, offline-only resources and console: PASS');

  await browser.close();
  // A separate browser avoids single-process Chromium tearing down a second context unexpectedly.
  const desktopBrowser = await chromium.launch(launch);
  const desktop = await desktopBrowser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
  const desktopPage = await desktop.newPage();
  const desktopErrors = [], desktopExternal = [];
  desktopPage.on('pageerror', error => desktopErrors.push(error.message));
  desktopPage.on('request', request => { if (!request.url().startsWith(baseURL)) desktopExternal.push(request.url()); });
  await desktopPage.goto(baseURL, { waitUntil: 'networkidle' });
  await desktopPage.locator('[data-open-account]').click();
  assert(await desktopPage.locator('#account-drawer').isVisible());
  assert((await desktopPage.locator('#account-drawer').boundingBox()).width <= 500);
  let audit = await new AxeBuilder({ page: desktopPage }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
  assert.deepEqual(audit.violations.map(v => v.id), [], `desktop account: ${JSON.stringify(audit.violations)}`);
  await desktopPage.locator('[data-close-account]').click();
  await desktopPage.locator('[data-open-admin]').click();
  await desktopPage.locator('[data-admin-enter]').click();
  assert((await desktopPage.locator('#admin-drawer').boundingBox()).width <= 700);
  await desktopPage.locator('#admin-tab-overview').focus();
  await desktopPage.keyboard.press('ArrowLeft');
  assert.equal(await desktopPage.locator('#admin-tab-products').getAttribute('aria-selected'), 'true');
  assert(await desktopPage.locator('#admin-panel-products').isVisible());
  audit = await new AxeBuilder({ page: desktopPage }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
  assert.deepEqual(audit.violations.map(v => v.id), [], `desktop admin: ${JSON.stringify(audit.violations)}`);
  if (screenshotDir) await desktopPage.screenshot({ path: path.join(screenshotDir, 'desktop-admin-products.png') });
  assert.deepEqual(desktopErrors, []); assert.deepEqual(desktopExternal, []);
  console.log('desktop drawers, tab keyboard navigation and accessibility: PASS');
  await desktopBrowser.close();
  clearTimeout(watchdog);
})().catch(error => { console.error(error); process.exit(1); });
