/**
 * ASIYE STORE — Bespoke 3D Obsidian-Crystal & 24K Champagne-Gold SVG Icon System
 * Crafted for Quiet Luxury UI: every icon features 3D squircle depth, metallic gold rim bevel,
 * top-left crystal specular highlight, and sculpted gold-foil inner vector geometry.
 */

export const OWNER_CONFIG = {
    ownerTitleFa: 'سرکار خانم زنگنه',
    ownerRoleFa: 'صاحب امتیاز و مدیریت مجموعه ASIYE STORE',
    phoneE164: '+989044200993',
    phoneDisplayLtr: '+98 904 420 0993',
    phonePersian: '۰۹۰۴ ۴۲۰ ۰۹۹۳',
    whatsappNumber: '989044200993',
    instagramUrl: 'https://www.instagram.com/asiyeh.store?psln=MnoxdzRzcXcxOGtz',
    instagramHandle: '@asiyeh.store',
    designerName: 'Milad Yaghoobi',
};

/**
 * Shared SVG <defs> injected once into document body so every 3D icon renders
 * rich metallic 24K gold gradients, smoked obsidian glass, and soft 3D levitation shadows.
 */
export function injectLuxury3DSvgDefs() {
    if (document.getElementById('asiye-3d-svg-defs')) return;
    const holder = document.createElement('div');
    holder.style.cssText = 'position:absolute;width:0;height:0;overflow:hidden;pointer-events:none;';
    holder.innerHTML = `
    <svg id="asiye-3d-svg-defs" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <!-- 3D Obsidian Crystal Body -->
        <radialGradient id="obs3dBody" cx="32%" cy="24%" r="78%">
          <stop offset="0%" stop-color="#2D2934"/>
          <stop offset="52%" stop-color="#141318"/>
          <stop offset="100%" stop-color="#070709"/>
        </radialGradient>

        <!-- 3D Warm Ivory Pearl Body (for dark backgrounds) -->
        <radialGradient id="pearl3dBody" cx="30%" cy="22%" r="78%">
          <stop offset="0%" stop-color="#FFFDF9"/>
          <stop offset="60%" stop-color="#F1E8D8"/>
          <stop offset="100%" stop-color="#DECBA8"/>
        </radialGradient>

        <!-- 24K Champagne Gold Metallic Bevel -->
        <linearGradient id="gold3dRim" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#F7E6C4"/>
          <stop offset="38%" stop-color="#D4B07A"/>
          <stop offset="72%" stop-color="#9E7944"/>
          <stop offset="100%" stop-color="#E7CA96"/>
        </linearGradient>

        <!-- Sculpted Inner Gold Foil Glyph -->
        <linearGradient id="gold3dGlyph" x1="15%" y1="5%" x2="85%" y2="95%">
          <stop offset="0%" stop-color="#FFF3D6"/>
          <stop offset="48%" stop-color="#DFBE88"/>
          <stop offset="100%" stop-color="#A67F46"/>
        </linearGradient>

        <!-- Subtle Emerald-Gold Aura for WhatsApp -->
        <radialGradient id="waAura" cx="50%" cy="50%" r="60%">
          <stop offset="0%" stop-color="#2E8B67" stop-opacity="0.34"/>
          <stop offset="100%" stop-color="#09090C" stop-opacity="0"/>
        </radialGradient>

        <!-- Subtle Rose-Amber Aura for Instagram -->
        <radialGradient id="igAura" cx="50%" cy="50%" r="60%">
          <stop offset="0%" stop-color="#C96A7E" stop-opacity="0.32"/>
          <stop offset="100%" stop-color="#09090C" stop-opacity="0"/>
        </radialGradient>

        <!-- Top-Left Glass Specular Sheen -->
        <linearGradient id="glassSheen" x1="0%" y1="0%" x2="80%" y2="80%">
          <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.32"/>
          <stop offset="55%" stop-color="#FFFFFF" stop-opacity="0.04"/>
          <stop offset="100%" stop-color="#FFFFFF" stop-opacity="0"/>
        </linearGradient>

        <!-- 3D Elevation Drop Shadow -->
        <filter id="lux3dShadow" x="-20%" y="-20%" width="140%" height="150%">
          <feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="#0A0806" flood-opacity="0.36"/>
          <feDropShadow dx="0" dy="1" stdDeviation="1" flood-color="#E5C691" flood-opacity="0.18"/>
        </filter>
      </defs>
    </svg>`;
    document.body.prepend(holder);
}

/**
 * Generates a 3D Obsidian & 24K Gold SVG icon string.
 * @param {'phone'|'sms'|'whatsapp'|'instagram'|'bag'|'shield'|'sparkle'|'crown'|'plus'|'heart'|'check'} name
 * @param {number} size
 */
export function icon3D(name, size = 44) {
    const aura = name === 'whatsapp'
        ? '<circle cx="32" cy="32" r="22" fill="url(#waAura)"/>'
        : name === 'instagram'
            ? '<circle cx="32" cy="32" r="22" fill="url(#igAura)"/>'
            : '';

    const glyphs = {
        phone: `
          <path d="M23.5 20.5c1.2-1.2 3.1-1.2 4.3 0l2.6 2.6c1 1 1.1 2.6.3 3.7l-1.6 2.1c1.8 3.1 4.4 5.7 7.5 7.5l2.1-1.6c1.1-.8 2.7-.7 3.7.3l2.6 2.6c1.2 1.2 1.2 3.1 0 4.3l-1.9 1.9c-2.3 2.3-5.8 2.9-8.8 1.5-7.1-3.3-12.8-9-16.1-16.1-1.4-3-.8-6.5 1.5-8.8l1.8-2z"
                fill="url(#gold3dGlyph)" stroke="#FFF3D6" stroke-width="0.7"/>`,
        sms: `
          <path d="M19 22h26c2.2 0 4 1.8 4 4v13c0 2.2-1.8 4-4 4H29l-7.5 5.2c-.8.5-1.9 0-1.9-1V43H19c-2.2 0-4-1.8-4-4V26c0-2.2 1.8-4 4-4z"
                fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.6" stroke-linejoin="round"/>
          <circle cx="26" cy="32.5" r="2" fill="url(#gold3dGlyph)"/>
          <circle cx="32" cy="32.5" r="2" fill="url(#gold3dGlyph)"/>
          <circle cx="38" cy="32.5" r="2" fill="url(#gold3dGlyph)"/>`,
        whatsapp: `
          <path d="M32 17.5c-8 0-14.5 6.3-14.5 14.1 0 2.7.8 5.3 2.2 7.5L17.8 46l7.2-1.9c2.1 1.1 4.5 1.7 7 1.7 8 0 14.5-6.3 14.5-14.1S40 17.5 32 17.5z"
                fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.5" stroke-linejoin="round"/>
          <path d="M27.4 25.4c.4-.8 1-.9 1.5-.9h1.1c.4 0 .9.2 1.1.8l1.3 3.1c.2.4.1.9-.2 1.3l-1.1 1.3c1.1 1.9 2.7 3.4 4.7 4.4l1.2-1.2c.4-.4.9-.5 1.4-.3l3.1 1.3c.6.2.8.7.7 1.2-.3 1.5-1.6 2.7-3.1 2.9-1.9.3-5.2-.5-8.6-3.7-3.2-3-4.3-6.2-4-8.1.1-.8.5-1.5.9-2.1z"
                fill="url(#gold3dGlyph)"/>`,
        instagram: `
          <rect x="18.5" y="18.5" width="27" height="27" rx="8" fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.5"/>
          <circle cx="32" cy="32" r="6.4" fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.5"/>
          <circle cx="40.3" cy="23.7" r="1.9" fill="url(#gold3dGlyph)"/>`,
        bag: `
          <path d="M21 26h22l-1.8 19.5c-.2 1.9-1.8 3.5-3.8 3.5H26.6c-2 0-3.6-1.6-3.8-3.5L21 26z"
                fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.5" stroke-linejoin="round"/>
          <path d="M26.5 26v-3.5a5.5 5.5 0 0 1 11 0V26" fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="27.5" cy="31" r="1.4" fill="url(#gold3dGlyph)"/>
          <circle cx="36.5" cy="31" r="1.4" fill="url(#gold3dGlyph)"/>`,
        shield: `
          <path d="M32 17l13 5.2v9.6c0 8.5-5.5 15.8-13 18.7-7.5-2.9-13-10.2-13-18.7v-9.6L32 17z"
                fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.4" stroke-linejoin="round"/>
          <path d="M26.5 32.2l3.8 3.8 7.5-7.8" fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round"/>`,
        sparkle: `
          <path d="M32 16l3.4 10.2L45.6 30l-10.2 3.4L32 43.6l-3.4-10.2L18.4 30l10.2-3.8L32 16z"
                fill="url(#gold3dGlyph)"/>
          <circle cx="44" cy="20" r="2" fill="#FFF3D6"/>`,
        crown: `
          <path d="M18 41l2.8-16 7.4 6.5L32 21l3.8 10.5 7.4-6.5L46 41H18z"
                fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.4" stroke-linejoin="round"/>
          <line x1="19" y1="45" x2="45" y2="45" stroke="url(#gold3dGlyph)" stroke-width="2.4" stroke-linecap="round"/>`,
        plus: `
          <line x1="32" y1="21" x2="32" y2="43" stroke="url(#gold3dGlyph)" stroke-width="2.8" stroke-linecap="round"/>
          <line x1="21" y1="32" x2="43" y2="32" stroke="url(#gold3dGlyph)" stroke-width="2.8" stroke-linecap="round"/>`,
        heart: `
          <path d="M32 44.5s-13-7.8-13-16.8c0-4.3 3.4-7.7 7.6-7.7 2.4 0 4.5 1.1 5.4 2.8.9-1.7 3-2.8 5.4-2.8 4.2 0 7.6 3.4 7.6 7.7 0 9-13 16.8-13 16.8z"
                fill="none" stroke="url(#gold3dGlyph)" stroke-width="2.4" stroke-linejoin="round"/>`,
        check: `
          <path d="M22.5 32.5l6.5 6.5 12.5-13" fill="none" stroke="url(#gold3dGlyph)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>`,
    };

    return `
    <svg class="lux-3d-svg" width="${size}" height="${size}" viewBox="0 0 64 64" aria-hidden="true">
      <g filter="url(#lux3dShadow)">
        <rect x="6" y="6" width="52" height="52" rx="17" fill="url(#obs3dBody)" stroke="url(#gold3dRim)" stroke-width="1.8"/>
        ${aura}
        <path d="M13 10h38c3.8 0 7 3.2 7 7v7C44 19 24 19 6 26v-9c0-3.8 3.2-7 7-7z" fill="url(#glassSheen)"/>
        ${glyphs[name] || glyphs.sparkle}
      </g>
    </svg>`;
}

/**
 * Generates an intelligent, traceable WhatsApp message so Ms. Zangeneh immediately knows
 * the inquiry originated from the official ASIYE STORE website, along with topic & bag items.
 */
export function buildSmartWhatsAppUrl({ topic = 'مشاوره تخصصی و ثبت سفارش', productName = '', cartItems = [] } = {}) {
    const refCode = 'AS-WEB-' + String(Date.now()).slice(-4);
    const lines = [
        'سلام و عرض ادب خدمت سرکار خانم زنگنه گرامی 🌹',
        'این پیام به صورت هوشمند از طریق «وب‌سایت رسمی ASIYE STORE» ارسال شده است.',
        `🔹 کد مرجع پورتال: ${refCode}`,
        `🔹 موضوع درخواست: ${topic}`,
    ];

    if (productName) {
        lines.push(`🔹 محصول انتخابی: ${productName}`);
    } else if (Array.isArray(cartItems) && cartItems.length > 0) {
        lines.push('🔹 اقلام موجود در سبد خرید من:');
        cartItems.forEach((it) => {
            lines.push(`   • ${it.name_fa} (${it.quantity} عدد)`);
        });
    }

    lines.push('ممنون می‌شوم جهت راهنمایی و هماهنگی ارسال، پاسخ بفرمایید ✨');
    return `https://wa.me/${OWNER_CONFIG.whatsappNumber}?text=${encodeURIComponent(lines.join('\n'))}`;
}

export function buildSmartSmsUrl(topic = 'مشاوره و ثبت سفارش از سایت ASIYE STORE') {
    const body = `سلام سرکار خانم زنگنه گرامی؛ از طریق وب‌سایت رسمی ASIYE STORE جهت «${topic}» پیام ارسال می‌کنم.`;
    return `sms:${OWNER_CONFIG.phoneE164}?body=${encodeURIComponent(body)}`;
}
