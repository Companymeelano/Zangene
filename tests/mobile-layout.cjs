const {chromium}=require('playwright-core');
const AxeBuilder=require('@axe-core/playwright').default;
const assert=require('node:assert/strict');
const fs=require('node:fs');
(async()=>{
 // Use a locally installed Playwright Chromium by default. CHROMIUM_PATH and
 // CHROMIUM_ARGS allow using a system or sandbox browser without committing one.
 const launchOptions={headless:true};
 if(process.env.CHROMIUM_PATH)launchOptions.executablePath=process.env.CHROMIUM_PATH;
 if(process.env.CHROMIUM_ARGS)launchOptions.args=JSON.parse(process.env.CHROMIUM_ARGS);
 const browser=await chromium.launch(launchOptions);
 const baseURL=process.env.BASE_URL||'http://127.0.0.1:4173/';
 const context=await browser.newContext({viewport:{width:360,height:760},deviceScaleFactor:1,isMobile:true,hasTouch:true,reducedMotion:'reduce'});
 const page=await context.newPage();
 const errors=[], badRequests=[];
 page.on('pageerror',e=>errors.push(e.message));
 page.on('response',r=>{if(r.status()>=400)badRequests.push(`${r.status()} ${r.url()}`)});
 const screenshotDir=process.env.SCREENSHOT_DIR;
 if(screenshotDir)fs.mkdirSync(screenshotDir,{recursive:true});
 const capture=async(name)=>{if(screenshotDir)await page.screenshot({path:`${screenshotDir}/${name}.png`});};
 const layout=async()=>page.evaluate(()=>{
   let rect=(sel)=>{const r=document.querySelector(sel).getBoundingClientRect();return {top:r.top,bottom:r.bottom,left:r.left,right:r.right,width:r.width,height:r.height}};
   const s=document.querySelector('#app-scroll');
   return {head:rect('.site-header'),scroll:rect('#app-scroll'),dock:rect('.mobile-dock'),width:innerWidth,height:innerHeight,htmlWidth:document.documentElement.scrollWidth,rootScroll:scrollY,scrollTop:s.scrollTop,scrollHeight:s.scrollHeight,scrollClient:s.clientHeight};
 });
 async function validShell(label){
   const l=await layout();
   assert(l.head.top>=-0.5,`${label}: header scrolled out: ${JSON.stringify(l)}`);
   assert(l.head.bottom<=l.scroll.top+0.5,`${label}: header covers scroll ${JSON.stringify(l)}`);
   assert(l.scroll.bottom<=l.dock.top+0.5,`${label}: dock covers scroll ${JSON.stringify(l)}`);
   assert(l.dock.bottom<=l.height+0.5,`${label}: dock outside viewport ${JSON.stringify(l)}`);
   assert.equal(l.htmlWidth,l.width,`${label}: horizontal document overflow`);
   assert.equal(l.rootScroll,0,`${label}: unexpectedly scrolled root`);
 }
 async function inScroll(selector,label){
   const result=await page.$eval(selector,el=>{
     const r=el.getBoundingClientRect(),s=document.querySelector('#app-scroll').getBoundingClientRect();
     const x=(r.left+r.right)/2,y=(r.top+r.bottom)/2,hit=document.elementFromPoint(x,y);
     return {top:r.top,bottom:r.bottom,left:r.left,right:r.right,scrollTop:s.top,scrollBottom:s.bottom,screenWidth:innerWidth,hit:!!hit&&(hit===el||el.contains(hit))};
   });
   assert(result.top>=result.scrollTop-1 && result.bottom<=result.scrollBottom+1,`${label} not fully within scroll: ${JSON.stringify(result)}`);
   assert(result.left>=-0.5 && result.right<=result.screenWidth+0.5,`${label} clipped horizontally: ${JSON.stringify(result)}`);
   assert(result.hit,`${label} center covered by other UI: ${JSON.stringify(result)}`);
 }
 const selectTarget=(sel,block='start')=>page.$eval(sel,(el,block)=>el.scrollIntoView({behavior:'auto',block}),block);
 for(const width of [320,360,390,480,680]){
   await page.setViewportSize({width,height:760});
   await page.goto(baseURL,{waitUntil:'networkidle'});await page.evaluate(()=>localStorage.clear());await page.reload({waitUntil:'networkidle'});await page.evaluate(()=>document.fonts.ready);
   await validShell(`${width}/home`);
   assert.equal(await page.locator('.dock-count:visible').count(),0,`${width}/empty badges`);
   assert.equal(await page.locator('.quote-count:visible').count(),0,`${width}/empty header badge`);
   assert.equal(await page.locator('#favorites-filter [data-favorites-count]').innerText(),'۰');
   if([320,480,680].includes(width))await capture(`${width}-home`);
   await selectTarget('#categories');await validShell(`${width}/categories`);
   const cards=await page.locator('.category-card').evaluateAll(els=>els.map(el=>{let r=el.getBoundingClientRect(),g=el.parentElement.getBoundingClientRect();return {left:r.left,right:r.right,gridLeft:g.left,gridRight:g.right,width:r.width}}));
   assert.equal(cards.length,5);for(const c of cards)assert(c.left>=c.gridLeft-0.5&&c.right<=c.gridRight+0.5,`${width} category clipped: ${JSON.stringify(c)}`);
   if([320,680].includes(width))await capture(`${width}-categories`);
   await selectTarget('[data-category="mixed"]','center');await inScroll('[data-category="mixed"]',`${width}/mixed category`);
   await page.locator('[data-category="mixed"]').tap();assert.equal(await page.locator('.product-card').count(),1);assert.match(await page.locator('.product-card h3').innerText(),/مخلوط/);await validShell(`${width}/category tap`);
   const chips=await page.locator('.filter-chip').evaluateAll(els=>els.map(el=>{let r=el.getBoundingClientRect(),g=el.parentElement.getBoundingClientRect();return {left:r.left,right:r.right,gridLeft:g.left,gridRight:g.right,height:r.height}}));
   assert.equal(chips.length,6);for(const c of chips)assert(c.left>=c.gridLeft-0.5 && c.right<=c.gridRight+0.5&&c.height>=40,`${width} filter clipped/small ${JSON.stringify(c)}`);
   await selectTarget('[data-filter="walnut"]','center');await inScroll('[data-filter="walnut"]',`${width}/filter`);await page.locator('[data-filter="walnut"]').tap();assert.match(await page.locator('.product-card h3').innerText(),/گردو/);
   await page.locator('[data-dock-favorites]').tap();await validShell(`${width}/favorites`);
   assert.equal(await page.locator('.product-card').count(),0);assert.equal(await page.locator('#empty-results-art').getAttribute('src'),'assets/ui-favorites-3d.webp');
   await inScroll('[data-reset-filters]',`${width}/empty-state button`);
   if([320,480].includes(width))await capture(`${width}-favorites`);
   await page.locator('[data-reset-filters]').tap();assert.equal(await page.locator('.product-card').count(),5);
   await page.locator('[data-favorite-product="pistachio"]').first().tap();assert.equal(await page.locator('.mobile-dock [data-favorites-count]').innerText(),'۱');assert(await page.locator('.mobile-dock [data-favorites-count]').isVisible());
   await page.locator('[data-add-product="pistachio"]').tap();assert.equal(await page.locator('.mobile-dock [data-quote-count]').innerText(),'۱');assert(await page.locator('.quote-count').isVisible());
   await page.locator('[data-dock-favorites]').tap();assert.equal(await page.locator('.product-card').count(),1);assert.match(await page.locator('.product-card h3').innerText(),/پسته/);
   await page.locator('[data-dock-products]').tap();await validShell(`${width}/products`);
   const productControls=await page.locator('.product-card').evaluateAll(cards=>cards.map(card=>{
     const a=card.querySelector('.product-favorite').getBoundingClientRect();
     const b=card.querySelector('.product-badge').getBoundingClientRect();
     return {size:a.width,overlap:a.right>b.left && a.left<b.right && a.bottom>b.top && a.top<b.bottom};
   }));
   assert(productControls.every(c=>c.size>=44&&!c.overlap),`${width}/product favorite tap area or badge collision`);
   if([320,480,680].includes(width))await capture(`${width}-products`);
   // A select should not make the navigation disappear; text entry should, allowing room for a keyboard.
   await page.locator('#catalog-sort').focus();assert(await page.locator('.mobile-dock').isVisible(),`${width}/select hides dock`);
   await page.locator('#product-search').focus();assert.equal(await page.locator('.mobile-dock').isVisible(),false,`${width}/typing doesn't hide dock`);
   await page.setViewportSize({width,height:480});await page.waitForTimeout(120);await page.evaluate(()=>document.querySelector('#product-search').scrollIntoView({block:'center'}));
   let editing=await page.evaluate(()=>({h:document.querySelector('.site-header').getBoundingClientRect().bottom,s:document.querySelector('#app-scroll').getBoundingClientRect(),root:scrollY}));
   assert(editing.h<=editing.s.top+1 && editing.s.bottom<=480+1 && editing.root===0,`${width}/resized keyboard geometry ${JSON.stringify(editing)}`);
   await page.locator('#product-search').blur();await page.waitForTimeout(20);assert(await page.locator('.mobile-dock').isVisible());
   await page.setViewportSize({width,height:760});await page.waitForTimeout(120);await validShell(`${width}/keyboard restored`);
   // Eager, unblended decorative images and no collision with the caption.
   await selectTarget('#contact');await validShell(`${width}/partner`);
   const info=await page.locator('.partner-floating').evaluateAll(els=>els.map(e=>{let img=e.querySelector('img'),r=e.getBoundingClientRect(),caption=document.querySelector('.partner-art-caption').getBoundingClientRect();return {loaded:img.complete&&img.naturalWidth>0,loading:img.loading,blend:getComputedStyle(img).mixBlendMode,r:{l:r.left,r:r.right,t:r.top,b:r.bottom},caption:{l:caption.left,r:caption.right,t:caption.top,b:caption.bottom}}}));
   for(const i of info){assert(i.loaded&&i.loading!=='lazy'&&i.blend==='normal',`${width}/partner art loading ${JSON.stringify(i)}`);assert(i.r.r<=i.caption.l||i.r.l>=i.caption.r||i.r.b<=i.caption.t||i.r.t>=i.caption.b,`${width}/partner art overlaps caption`);}
   if([320,680].includes(width))await capture(`${width}-partner`);
   await selectTarget('.footer-bottom','end');await validShell(`${width}/footer bottom`);
   await selectTarget('.footer-contact button','center');await inScroll('.footer-contact button',`${width}/footer inquiry`);
   if([320,680].includes(width))await capture(`${width}-footer`);
   await page.locator('.footer-contact button').tap();assert(await page.locator('#quote-drawer').isVisible());assert.equal(await page.locator('.mobile-dock').isVisible(),false);
   await page.locator('[data-close-quote]').tap();await validShell(`${width}/close inquiry`);
   await selectTarget('[data-footer-category="walnut"]','center');await page.locator('[data-footer-category="walnut"]').tap();assert.match(await page.locator('.product-card h3').innerText(),/گردو/);
   await page.locator('.site-header .brand').tap();await page.waitForTimeout(80);assert.equal((await layout()).scrollTop,0,`${width}/brand did not return to top`);await validShell(`${width}/home after footer`);
   await page.locator('.mobile-menu-toggle').tap();assert(await page.locator('#mobile-nav').isVisible());await page.locator('#mobile-nav a[href="#about"]').tap();assert.equal(await page.locator('#mobile-nav').isVisible(),false);await validShell(`${width}/menu navigation`);
   console.log(`mobile width ${width}: shell geometry, taps, categories, filters, favorites, keyboard, partner, footer & menu PASS`);
 }
 // Keyboard navigation, accessibility and an actual touch swipe at a representative size.
 await page.setViewportSize({width:390,height:844});await page.goto(baseURL,{waitUntil:'networkidle'});await page.evaluate(()=>localStorage.clear());await page.reload({waitUntil:'networkidle'});
 let axe=await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21a','wcag21aa']).analyze();assert.deepEqual(axe.violations.map(v=>v.id),[],`home axe ${JSON.stringify(axe.violations)}`);
 const cdp=await context.newCDPSession(page);await cdp.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:200,y:570}]});
 for(let y=530;y>=300;y-=45)await cdp.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:200,y}]});
 await cdp.send('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});await page.waitForTimeout(400);
 const touchLayout=await layout();assert(touchLayout.scrollTop>0,`touch swipe did not scroll: ${JSON.stringify(touchLayout)}`);await validShell('touch swipe');
 await page.locator('[data-dock-favorites]').tap();axe=await new AxeBuilder({page}).withTags(['wcag2a','wcag2aa','wcag21a','wcag21aa']).analyze();assert.deepEqual(axe.violations.map(v=>v.id),[],`favorites axe ${JSON.stringify(axe.violations)}`);
 console.log('touch swipe and axe home/favorites: PASS');
 assert.deepEqual(errors,[]);assert.deepEqual(badRequests,[]);
 await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
