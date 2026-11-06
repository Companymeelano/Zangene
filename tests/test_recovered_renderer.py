import subprocess
import unittest
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
FILE=ROOT/'reconstruction/android/src/com/asiyestore/admin/app/Luxury3DIconView.java'

class RecoveredRendererTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls): cls.src=FILE.read_text(encoding='utf-8')

    def test_pastel_uses_assets_and_safe_vector_fallback(self):
        s=self.src
        branch=s.split('if (palette == PALETTE_PASTEL) {',1)[1].split('// 1. Soft 3D Ground Shadow',1)[0]
        self.assertIn('loadJewel(iconType, goldPedestal)',branch)
        self.assertIn('drawFloatingPinkGlyph(canvas, w, h, s)',branch)
        self.assertIn('return;',branch)
        loader=s.split('private Bitmap loadJewel',1)[1].split('private void drawFloatingPinkGlyph',1)[0]
        self.assertIn('JewelIconRegistry.assetName(type, selected)',loader)
        self.assertIn('getAssets().open("icons/" + name)',loader)
        self.assertIn('catch (IOException ex)',loader)
        self.assertIn('return null;',loader)

    def test_non_pastel_renderer_preserved(self):
        p='android/app/src/main/java/com/asiyestore/admin/app/Luxury3DIconView.java'
        original=subprocess.run(['git','show','afa2faf59988903220a71530e131d0349700ff30:'+p],cwd=ROOT,capture_output=True,text=True)
        if original.returncode: self.skipTest('Original source absent from Git')
        marker='// 1. Soft 3D Ground Shadow'
        old=original.stdout.split(marker,1)[1].split('    private float gx(',1)[0].strip()
        new=self.src.split(marker,1)[1].split('    /** Load exact named assets',1)[0].strip()
        self.assertEqual(old,new)

    def test_all_assets_are_stored_in_intended_folder(self):
        assets=ROOT/'reconstruction/android/assets/icons'
        self.assertEqual(len(list(assets.glob('jewel_*.png'))),23)
        for f in assets.glob('jewel_*.png'):
            self.assertEqual(f.read_bytes()[:8],b'\x89PNG\r\n\x1a\n')
