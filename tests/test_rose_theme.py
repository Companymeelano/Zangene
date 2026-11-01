import re
import unittest
from test_sapphire_theme import SOURCE, contrast

class RoseThemeTest(unittest.TestCase):
    def test_theme_selection_and_persistence(self):
        s = SOURCE.read_text(encoding='utf8')
        self.assertIn('THEME_ROSE = 4', s)
        self.assertIn('showThemePicker()', s)
        self.assertIn('themeTiles(null)', s)
        self.assertIn('THEME_SAPPHIRE, THEME_ROSE, THEME_PINK, THEME_NIGHT', s)
        self.assertIn('.putInt("theme_mode", themeMode)', s)
        self.assertIn('PALETTE_ROSE', s)
        self.assertIn('METAL_ROSE', s)

    def test_readability(self):
        s = SOURCE.read_text(encoding='utf8')
        block = s.split('} else if (themeMode == THEME_ROSE) {', 1)[1].split('} else if (darkMode)', 1)[0]
        colors = dict(re.findall(r'(\w+)\s*=\s*0xFF([0-9A-F]{6})', block))
        for ink, background in [('COL_TEXT_MAIN','COL_WHITE'), ('COL_TEXT_MUTED','COL_WHITE'),
                                ('COL_TEXT_FAINT','COL_WHITE'), ('COL_GOLD','COL_WHITE'),
                                ('TEXT_ON_DARK','COL_OBSIDIAN'), ('TEXT_ON_DARK_MUTED','COL_OBSIDIAN')]:
            with self.subTest(ink=ink):
                self.assertGreaterEqual(contrast(colors[ink], colors[background]), 4.5)

if __name__ == '__main__': unittest.main()
