import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'android/app/src/main/java/com/asiyestore/admin/app/MainActivity.java'

def contrast(a, b):
    def luminance(h):
        rgb = [int(h[i:i+2], 16) / 255 for i in (0, 2, 4)]
        rgb = [v / 12.92 if v <= .04045 else ((v + .055) / 1.055) ** 2.4 for v in rgb]
        return .2126 * rgb[0] + .7152 * rgb[1] + .0722 * rgb[2]
    x, y = sorted((luminance(a), luminance(b)))
    return (y + .05) / (x + .05)

class SapphireThemeTest(unittest.TestCase):
    def test_palette_persistence_and_entry_points(self):
        s = SOURCE.read_text(encoding='utf8')
        self.assertIn('THEME_SAPPHIRE = 3', s)
        self.assertIn('themeMode == THEME_EMERALD ? THEME_SAPPHIRE', s)
        self.assertIn('THEME_EMERALD, THEME_SAPPHIRE, THEME_NIGHT', s)
        self.assertIn('.putInt("theme_mode", themeMode)', s)
        self.assertIn('getInt("theme_mode"', s)
        self.assertIn('PALETTE_SAPPHIRE', s)
    def test_sapphire_text_contrast(self):
        s = SOURCE.read_text(encoding='utf8')
        block = s.split('} else if (themeMode == THEME_SAPPHIRE) {', 1)[1].split('} else if (darkMode)', 1)[0]
        colors = dict(re.findall(r'(\w+)\s*=\s*0xFF([0-9A-F]{6})', block))
        for ink, background in [('COL_TEXT_MAIN','COL_WHITE'), ('COL_TEXT_MUTED','COL_WHITE'),
                                ('COL_TEXT_FAINT','COL_WHITE'), ('TEXT_ON_DARK','COL_OBSIDIAN'),
                                ('TEXT_ON_DARK_MUTED','COL_OBSIDIAN'), ('COL_GOLD','COL_WHITE')]:
            with self.subTest(ink=ink):
                self.assertGreaterEqual(contrast(colors[ink], colors[background]), 4.5)

if __name__ == '__main__':
    unittest.main()
