import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase, mock
import zipfile

ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('recover_demo_icons',ROOT/'tools/recover_demo_icons.py')
recovery=importlib.util.module_from_spec(spec)
spec.loader.exec_module(recovery)

class RecoverIconsTests(TestCase):
    def test_rejects_wrong_checksum_before_writing(self):
        with TemporaryDirectory() as d:
            apk=Path(d)/'wrong.apk'; apk.write_bytes(b'not an apk')
            out=Path(d)/'out'
            with self.assertRaises(ValueError): recovery.recover(apk,out)
            self.assertFalse(out.exists())
    def test_rejects_incomplete_inventory(self):
        from hashlib import sha256
        with TemporaryDirectory() as d:
            apk=Path(d)/'sample.apk'
            with zipfile.ZipFile(apk,'w') as z: z.writestr('irrelevant',b'')
            with mock.patch.object(recovery,'EXPECTED_APK',sha256(apk.read_bytes()).hexdigest()):
                with self.assertRaises(ValueError): recovery.recover(apk,Path(d)/'out')
    def test_real_artifact_when_present(self):
        apk=ROOT/'ASIYE_STORE_SCREENSHOT_POLISH_TEST.apk'
        if not apk.exists(): self.skipTest('Pinned demo artifact not present')
        with TemporaryDirectory() as d:
            self.assertEqual(recovery.recover(apk,Path(d)/'icons'),23)
            f=Path(d)/'icons/jewel_home.png'; f.write_bytes(b'changed')
            with self.assertRaises(FileExistsError): recovery.recover(apk,Path(d)/'icons')
            self.assertEqual(f.read_bytes(),b'changed')
