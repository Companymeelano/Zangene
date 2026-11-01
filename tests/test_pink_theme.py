import re
import unittest
from test_sapphire_theme import SOURCE, contrast, ROOT

class PinkThemeTest(unittest.TestCase):
    def test_direct_picker_and_preservation(self):
        s = SOURCE.read_text(encoding='utf8')
        self.assertIn('THEME_ROSE = 4', s)
        self.assertIn('THEME_PINK = 5', s)
        self.assertIn('THEME_ROSE, THEME_PINK, THEME_NIGHT', s)
        self.assertIn('showThemePicker();', s)
        self.assertIn('scroll.addView(themeTiles(dialog))', s)
        self.assertIn('look.addView(themeTiles(null))', s)
        self.assertIn('if (dialog != null) dialog.dismiss();', s)
        self.assertIn('.putInt("theme_mode", themeMode)', s)
        self.assertIn('PALETTE_PINK', s)
        for name in ('Luxury3DIconView', 'LuxuryCanvas', 'SalesChartView'):
            text = (ROOT / ('android/app/src/main/java/com/asiyestore/admin/app/' + name + '.java')).read_text(encoding='utf8')
            self.assertIn('PINK', text)

    def test_pink_ink_and_contrasts(self):
        s = SOURCE.read_text(encoding='utf8')
        block = s.split('} else if (themeMode == THEME_PINK) {', 1)[1].split('} else if (', 1)[0]
        colors = dict(re.findall(r'(\w+)\s*=\s*0xFF([0-9A-F]{6})', block))
        # Pink surfaces have red dominant, with appreciably more blue than green.
        bg = colors['COL_IVORY_BG']
        r,g,b = [int(bg[i:i+2],16) for i in (0,2,4)]
        self.assertGreater(r,g); self.assertGreater(b,g)
        for ink, background in [('COL_TEXT_MAIN','COL_WHITE'), ('COL_TEXT_MUTED','COL_WHITE'),
                ('COL_TEXT_FAINT','COL_IVORY_BG'), ('COL_GOLD','COL_WHITE'),
                ('TEXT_ON_DARK','COL_OBSIDIAN'), ('TEXT_ON_DARK_MUTED','COL_OBSIDIAN'),
                ('COL_GOLD_LIGHT','COL_GOLD')]:
            with self.subTest(ink=ink, background=background):
                self.assertGreaterEqual(contrast(colors[ink], colors[background]), 4.5)

if __name__ == '__main__': unittest.main()
