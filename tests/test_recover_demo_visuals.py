import importlib.util
from hashlib import sha256
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase, mock
import zipfile

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('recover_demo_visuals', ROOT/'tools/recover_demo_visuals.py')
r = importlib.util.module_from_spec(spec)
spec.loader.exec_module(r)


class RecoverVisualsTests(TestCase):
    def test_wrong_apk_is_rejected_before_writing(self):
        with TemporaryDirectory() as d:
            p = Path(d)/'x.apk'; p.write_bytes(b'wrong')
            with self.assertRaises(ValueError): r.recover(p, Path(d)/'out')
            self.assertFalse((Path(d)/'out').exists())

    def test_incomplete_archive_is_rejected(self):
        with TemporaryDirectory() as d:
            p = Path(d)/'x.apk'
            with zipfile.ZipFile(p, 'w') as z: z.writestr('anything', b'anything')
            with mock.patch.object(r, 'EXPECTED_APK', sha256(p.read_bytes()).hexdigest()):
                with self.assertRaisesRegex(ValueError, 'Incomplete'): r.recover(p, Path(d)/'out')
            self.assertFalse((Path(d)/'out').exists())

    def test_recovery_matches_committed_files_and_conflicts_fail_closed(self):
        apk = ROOT/'ASIYE_STORE_SCREENSHOT_POLISH_TEST.apk'
        if not apk.exists(): self.skipTest('Pinned offline demo not present')
        with TemporaryDirectory() as d:
            out = Path(d)/'images'
            self.assertEqual(r.recover(apk, out), 5)
            committed = ROOT/'reconstruction/android/res/drawable-nodpi'
            for name in r.FILES:
                self.assertEqual((out/name).read_bytes(), (committed/name).read_bytes())
            victim = out/'img_pink_hero.webp'
            victim.write_bytes(b'user modification')
            (out/'img_reference_wheat.webp').unlink()
            with self.assertRaises(FileExistsError): r.recover(apk, out)
            self.assertEqual(victim.read_bytes(), b'user modification')
            self.assertFalse((out/'img_reference_wheat.webp').exists())

    def test_symlink_target_rejected(self):
        apk = ROOT/'ASIYE_STORE_SCREENSHOT_POLISH_TEST.apk'
        if not apk.exists(): self.skipTest('Pinned offline demo not present')
        with TemporaryDirectory() as d:
            out = Path(d)/'images'; out.mkdir()
            (out/'img_pink_hero.webp').symlink_to(Path(d)/'missing')
            with self.assertRaises(FileExistsError): r.recover(apk, out)
            self.assertEqual(list(out.iterdir()), [out/'img_pink_hero.webp'])
