const { chromium } = require('playwright-core');
const AxeBuilder = require('@axe-core/playwright').default;
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

(async () => {
  const watchdog = setTimeout(() => { console.error('UI test timed out'); process.exit(1); }, 300000);
  const launch = { headless: true };
  if (process.env.CHROMIUM_PATH) launch.executablePath = process.env.CHROMIUM_PATH;
  if (process.env.CHROMIUM_ARGS) launch.args = JSON.parse(process.env.CHROMIUM_ARGS);
  const browser = await chromium.launch(launch);
  const baseURL = process.env.BASE_URL || 'http://127.0.0.1:4173/';
  const screenshotDir = process.env.SCREENSHOT_DIR;
  if (screenshotDir) fs.mkdirSync(screenshotDir, { recursive: true });
  const context = await browser.newContext({ viewport: { width: 360, height: 760 }, isMobile: true, hasTouch: true, reducedMotion: 'reduce' });
  const page = await context.newPage();
  const errors = [], failed = [], external = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('response', response => { if (response.status() >= 400) failed.push(`${response.status()} ${response.url()}`); });
  page.on('request', request => { if (!request.url().startsWith(baseURL)) external.push(request.url()); });

  async function capture(name) {
    if (screenshotDir) await page.screenshot({ path: path.join(screenshotDir, `${name}.png`) });
  }
  async function reset(width, height = 760) {
    await page.setViewportSize({ width, height });
    await page.goto(baseURL, { waitUntil: 'networkidle' });
    await page.evaluate(() => localStorage.clear());
    await page.reload({ waitUntil: 'networkidle' });
    await page.evaluate(() => document.fonts.ready);
  }
  async function shell(label) {
    const layout = await page.evaluate(() => {
      const rect = selector => {
        const r = document.querySelector(selector).getBoundingClientRect();
        return { top: r.top, bottom: r.bottom };
      };
      return { head: rect('.site-header'), scroll: rect('#app-scroll'), dock: rect('.mobile-dock'), height: innerHeight, width: innerWidth, doc: document.documentElement.scrollWidth, root: scrollY };
    });
    assert.equal(layout.doc, layout.width, `${label}: horizontal overflow`);
    if (layout.width <= 680) {
      assert(layout.head.top >= -0.5 && layout.head.bottom <= layout.scroll.top + 0.5, `${label}: header covers content ${JSON.stringify(layout)}`);
      assert(layout.scroll.bottom <= layout.dock.top + 0.5 && layout.dock.bottom <= layout.height + 0.5, `${label}: dock covers content ${JSON.stringify(layout)}`);
      assert.equal(layout.root, 0, `${label}: root scrolled`);
    }
  }
  async function axe(label) {
    const result = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
    assert.deepEqual(result.violations.map(violation => violation.id), [], `${label}: ${JSON.stringify(result.violations)}`);
  }

  for (const width of [320, 360, 390, 480, 680]) {
    await reset(width);
    await shell(`${width}/home`);
    assert.equal(await page.locator('.product-card').count(), 5);
    assert.equal(await page.locator('.dock-count:visible').count(), 0);
    assert.deepEqual(await page.locator('.shop-price strong').allTextContents(), Array(5).fill('قیمت ثبت نشده'));
    assert.equal(await page.locator('#about, #contact, #quote-form').count(), 0, 'legacy marketing/inquiry sections remain');
    const chips = await page.locator('.filter-chip').evaluateAll(elements => elements.map(element => {
      const r = element.getBoundingClientRect(), parent = element.parentElement.getBoundingClientRect();
      return r.left >= parent.left - .5 && r.right <= parent.right + .5 && r.height >= 40;
    }));
    assert.equal(chips.length, 6);
    assert(chips.every(Boolean), `${width}/category buttons clipped`);
    if ([320, 390, 680].includes(width)) await capture(`${width}-home`);
    await page.locator('[data-filter="walnut"]').tap();
    assert.equal(await page.locator('.product-card').count(), 1);
    assert.match(await page.locator('.product-card h3').innerText(), /گردو/);
    await page.locator('[data-dock-products]').tap();
    assert.equal(await page.locator('.product-card').count(), 5);
    await page.locator('.product-card [data-add-product="pistachio"]').tap();
    await page.locator('.product-card [data-add-product="almond"]').tap();
    assert.equal(await page.locator('.mobile-dock [data-cart-count]').innerText(), '۲');
    await page.locator('.dock-cart').tap();
    assert(await page.locator('#cart-drawer').isVisible());
    assert.equal(await page.locator('.quote-item').count(), 2);
    assert.equal(await page.locator('#cart-total').innerText(), 'نامشخص');
    assert.match(await page.locator('#cart-price-note').innerText(), /ثبت نشده/);
    await page.locator('[data-increase="pistachio"]').tap();
    assert.equal(await page.locator('[data-quantity="pistachio"]').inputValue(), '۲');
    await page.locator('[data-decrease="pistachio"]').tap();
    assert.equal(await page.locator('[data-quantity="pistachio"]').inputValue(), '۱');
    await page.locator('[data-quantity="pistachio"]').fill('۱۲');
    await page.locator('[data-quantity="pistachio"]').dispatchEvent('change');
    assert.equal(await page.locator('[data-quantity="pistachio"]').inputValue(), '۱۲');
    if (width === 320) await capture('320-cart');
    await page.locator('[data-checkout]').tap();
    assert(await page.locator('#checkout-drawer').isVisible());
    assert.match(await page.locator('#checkout-disclaimer').innerText(), /پولی کسر|سفارشی ارسال/);
    assert.equal(await page.locator('#checkout-total').innerText(), 'مبلغ: نامشخص');
    await page.locator('[name="name"]').fill('نام تست محرمانه');
    await page.locator('[name="phone"]').fill('۱۲');
    await page.locator('[name="address"]').fill('نشانی تست محرمانه');
    await page.locator('#checkout-form button[type="submit"]').tap();
    assert.equal(await page.locator('#checkout-success').isVisible(), false);
    assert.notEqual(await page.locator('[name="phone"]').evaluate(element => element.validationMessage), '');
    await page.locator('[name="phone"]').fill('۰۹۱۲۳۴۵۶۷۸۹');
    await page.locator('#checkout-form button[type="submit"]').tap();
    assert(await page.locator('#checkout-success').isVisible());
    assert.match(await page.locator('#checkout-success').innerText(), /هیچ پولی کسر/);
    assert(!await page.evaluate(() => JSON.stringify(localStorage).includes('محرمانه')));
    if (width === 320) await capture('320-payment-simulation');
    await page.locator('[data-finish-checkout]').tap();
    await shell(`${width}/after simulation`);
    await page.locator('.dock-cart').tap();
    assert.equal(await page.locator('[data-quantity="pistachio"]').inputValue(), '۱۲', 'demo does not erase the cart');
    await page.locator('[data-remove="pistachio"]').tap();
    await page.locator('[data-remove="almond"]').tap();
    assert(await page.locator('#cart-empty').isVisible());
    assert.equal(await page.locator('[data-checkout]').isVisible(), false);
    await page.locator('[data-browse-products]').tap();
    await page.locator('[data-dock-favorites]').tap();
    assert.match(await page.locator('#empty-results-title').innerText(), /منتخب/);
    assert.equal(await page.locator('#empty-results-art').getAttribute('src'), 'assets/ui-favorites-3d.webp');
    await page.locator('[data-reset-filters]').tap();
    await page.locator('.product-card [data-favorite-product="walnut"]').tap();
    await page.locator('[data-dock-favorites]').tap();
    assert.equal(await page.locator('.product-card').count(), 1);
    assert.match(await page.locator('.product-card h3').innerText(), /گردو/);
    await page.locator('[data-open-product="walnut"]').tap();
    assert(await page.locator('#product-modal').isVisible());
    await page.locator('[data-close-product]').tap();
    await page.locator('[data-dock-products]').tap();
    await page.locator('#catalog-sort').focus();
    assert(await page.locator('.mobile-dock').isVisible(), 'select focus should not hide the dock');
    await page.locator('#product-search').focus();
    assert.equal(await page.locator('.mobile-dock').isVisible(), false, 'text focus should free keyboard space');
    await page.setViewportSize({ width, height: 480 });
    await page.waitForTimeout(120);
    await page.locator('#product-search').scrollIntoViewIfNeeded();
    assert.equal(await page.evaluate(() => scrollY), 0, 'keyboard resizing scrolled root');
    await page.locator('#product-search').blur();
    await page.waitForTimeout(20);
    await page.setViewportSize({ width, height: 760 });
    await page.waitForTimeout(120);
    await shell(`${width}/keyboard restored`);
    console.log(`width ${width}: layout, filters, cart, quantities, demo checkout, favorites and keyboard PASS`);
  }

  // Preserve old inquiry selections/favorites on upgrade; no customer details are migrated.
  await reset(390, 844);
  await page.evaluate(() => {
    localStorage.setItem('derakhshan-wholesale-quote-v1', JSON.stringify([{ id: 'pistachio', quantity: 100000 }, { id: 'bad-id', quantity: 3 }]));
    localStorage.setItem('derakhshan-wholesale-favorites-v1', JSON.stringify(['walnut']));
  });
  await page.reload({ waitUntil: 'networkidle' });
  assert.equal(await page.locator('.mobile-dock [data-cart-count]').innerText(), '۱۰۰٬۰۰۰');
  assert.equal(await page.locator('.mobile-dock [data-favorites-count]').innerText(), '۱');
  assert.deepEqual(await page.evaluate(() => JSON.parse(localStorage.getItem('derakhshan-shopping-cart-v1'))), [{ id: 'pistachio', quantity: 100000 }]);
  await page.locator('.dock-cart').tap();
  assert.equal(await page.locator('[data-quantity="pistachio"]').inputValue(), '۱۰۰۰۰۰');
  await axe('cart');
  await page.locator('[data-checkout]').tap();
  await axe('checkout');
  await page.locator('[data-close-checkout]').tap();
  assert(await page.locator('#cart-drawer').isVisible(), 'back from checkout must return to cart');
  await page.keyboard.press('Escape');
  assert.equal(await page.locator('#cart-drawer').isVisible(), false);
  await page.locator('[data-dock-products]').tap();
  await page.locator('[data-open-product="pistachio"]').tap();
  await axe('product detail');
  await page.keyboard.press('Escape');
  assert.equal(await page.locator('#product-modal').isVisible(), false);
  await axe('shop home');
  console.log('legacy migration, dialog back behavior and accessibility: PASS');

  // Test the total calculator with fixture values injected into a test response only.
  await page.route('**/app.js', async route => {
    const response = await route.fetch();
    const source = (await response.text()).replaceAll('priceToman: null, inStock: null', 'priceToman: 100000, inStock: null');
    const headers = { ...response.headers(), 'cache-control': 'no-store' };
    delete headers.etag; delete headers['last-modified']; delete headers['content-length'];
    await route.fulfill({ response, headers, body: source });
  });
  await page.evaluate(() => localStorage.clear());
  await page.reload({ waitUntil: 'networkidle' });
  assert.match(await page.locator('.shop-price strong').first().innerText(), /۱۰۰٬۰۰۰ تومان/);
  await page.locator('.product-card [data-add-product="pistachio"]').tap();
  await page.locator('.product-card [data-add-product="pistachio"]').tap();
  await page.locator('.dock-cart').tap();
  assert.match(await page.locator('#cart-total').innerText(), /۲۰۰٬۰۰۰ تومان/);
  await page.unroute('**/app.js');
  await page.route('**/app.js', async route => {
    const response = await route.fetch();
    const source = (await response.text())
      .replace('priceToman: null, inStock: null', 'priceToman: 100000, inStock: true')
      .replace("keywords: 'میوه خشک خشکبار انجیر', priceToman: null, inStock: null", "keywords: 'میوه خشک خشکبار انجیر', priceToman: null, inStock: false");
    const headers = { ...response.headers(), 'cache-control': 'no-store' };
    delete headers.etag; delete headers['last-modified']; delete headers['content-length'];
    await route.fulfill({ response, headers, body: source });
  });
  await page.evaluate(() => localStorage.clear());
  await page.reload({ waitUntil: 'networkidle' });
  await page.locator('.product-card [data-add-product="pistachio"]').tap();
  await page.locator('.product-card [data-add-product="almond"]').tap();
  assert(await page.locator('.product-card [data-add-product="fig"]').isDisabled());
  await page.locator('[data-open-product="fig"]').tap();
  assert(await page.locator('#product-modal-add').isDisabled());
  assert.match(await page.locator('#product-modal-note').innerText(), /ناموجود/);
  assert.equal(await page.locator('#product-modal-add-label').innerText(), 'ناموجود');
  await page.locator('[data-close-product]').tap();
  await page.locator('.dock-cart').tap();
  assert.equal(await page.locator('#cart-total').innerText(), 'نامشخص', 'a partial price must never be presented as the full total');
  await page.unroute('**/app.js');
  // Discard intercepted data; subsequent screenshots and scenarios must use the real unpriced catalog.
  const cache = await context.newCDPSession(page);
  await cache.send('Network.clearBrowserCache');
  await cache.detach();
  await page.evaluate(() => localStorage.clear());
  await page.reload({ waitUntil: 'networkidle' });
  assert.deepEqual(await page.locator('.shop-price strong').allTextContents(), Array(5).fill('قیمت ثبت نشده'));
  console.log('partial totals, unavailable products and price calculator with temporary test-only data: PASS');

  for (const [width, height] of [[568, 320], [667, 375], [812, 375]]) {
    await reset(width, height);
    await shell(`${width}x${height}/landscape`);
    await page.locator('.product-card [data-add-product="pistachio"]').click();
    await page.locator('[data-open-cart]').first().click();
    await page.locator('[data-checkout]').click();
    assert(await page.locator('#checkout-drawer').isVisible());
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth), width, 'landscape dialog overflow');
    await page.locator('[data-close-checkout]').click();
    await page.locator('[data-close-cart]').click();
    await capture(`${width}x${height}-landscape`);
  }
  console.log('landscape layout and checkout: PASS');

  for (const width of [681, 768, 1024, 1440, 1920]) {
    await page.setViewportSize({ width, height: 900 });
    await page.waitForTimeout(80);
    await shell(`${width}/desktop`);
    assert.equal(await page.locator('.mobile-dock').isVisible(), false);
    assert.equal(await page.locator('#app-scroll').evaluate(el => getComputedStyle(el).display), 'contents');
  }
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(baseURL, { waitUntil: 'networkidle' });
  const cdp = await context.newCDPSession(page);
  await cdp.send('Input.dispatchTouchEvent', { type: 'touchStart', touchPoints: [{ x: 200, y: 620 }] });
  for (let y = 580; y >= 300; y -= 45) await cdp.send('Input.dispatchTouchEvent', { type: 'touchMove', touchPoints: [{ x: 200, y }] });
  await cdp.send('Input.dispatchTouchEvent', { type: 'touchEnd', touchPoints: [] });
  await page.waitForTimeout(200);
  assert(await page.evaluate(() => document.querySelector('#app-scroll').scrollTop > 0), 'touch swipe did not scroll content');
  await shell('touch swipe');
  assert.deepEqual(errors, []); assert.deepEqual(failed, []); assert.deepEqual(external, []);
  console.log('desktop widths, real touch swipe, offline assets and console: PASS');
  await browser.close();

  const desktopBrowser = await chromium.launch(launch);
  const desktop = await desktopBrowser.newContext({ viewport: { width: 1440, height: 900 }, reducedMotion: 'reduce' });
  const desktopPage = await desktop.newPage();
  await desktopPage.goto(baseURL, { waitUntil: 'networkidle' });
  await desktopPage.locator('.product-card [data-add-product="pistachio"]').click();
  await desktopPage.locator('.header-cta').click();
  for (const step of ['cart', 'checkout']) {
    if (step === 'checkout') await desktopPage.locator('[data-checkout]').click();
    const result = await new AxeBuilder({ page: desktopPage }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
    assert.deepEqual(result.violations.map(v => v.id), [], `desktop ${step}: ${JSON.stringify(result.violations)}`);
  }
  await desktop.close();
  console.log('desktop cart and checkout accessibility: PASS');
  await desktopBrowser.close();
  clearTimeout(watchdog);
})().catch(error => { console.error(error); process.exit(1); });
