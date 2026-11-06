"""Guard the test fixture against accidental divergence from the tracked v9 schema."""
import ast
import re
import subprocess
import unittest
from pathlib import Path


class Version9SchemaFixtureTest(unittest.TestCase):
    def test_products_fixture_matches_v9_source(self):
        source = subprocess.check_output([
            'git', 'show',
            '8959abd05359633d449a59bcf47b2a994c651aee',
        ], text=True)
        self.assertIn('DB_VERSION = 9', source)
        fragment = source.split('db.execSQL("CREATE TABLE products (" +', 1)[1].split(');', 1)[0]
        parts = [ast.literal_eval(x) for x in re.findall(r'"(?:[^"\\]|\\.)*"', fragment)]
        expected = 'CREATE TABLE products (' + ''.join(parts) + ';\n'
        fixture = Path('reconstruction/android/test/sqlite/products_v9.sql').read_text()
        self.assertEqual(expected, fixture)
