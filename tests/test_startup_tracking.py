"""Regression check for the v6.9.4 launch-time recursive tracking helper."""
import pathlib
import re
import unittest

SOURCE = pathlib.Path(__file__).resolve().parents[1] / 'android/app/src/main/java/com/asiyestore/admin/app/MainActivity.java'

class StartupTrackingTest(unittest.TestCase):
    def test_tracking_applies_spacing_without_self_recursion(self):
        source = SOURCE.read_text(encoding='utf-8')
        match = re.search(r'private void tracking\(TextView t, float value\)\s*\{(.*?)\n    \}', source, re.S)
        self.assertIsNotNone(match)
        body = re.sub(r'//[^\n]*', '', match.group(1))
        self.assertNotRegex(body, r'(?<![\w.])tracking\s*\(')
        self.assertRegex(body, r't\.setLetterSpacing\(value\)')
        self.assertIn('hasPersian(', body)
        self.assertRegex(body, r'hasPersian\(.*?\)\)\s*return', re.S)

if __name__ == '__main__':
    unittest.main()
