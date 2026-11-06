import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
from unittest import TestCase, mock
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('apkcheck', ROOT/'tools/check_release_apk.py')
apkcheck = importlib.util.module_from_spec(spec)
spec.loader.exec_module(apkcheck)
CERT = 'a' * 64
BADGING = "package: name='com.asiyestore.admin' versionCode='724'\napplication-label:'Asiye Store'\nuses-permission: name='android.permission.INTERNET'\n"
SIGNER = 'Signer #1 certificate SHA-256 digest: ' + CERT + '\n'

class PreflightTests(TestCase):
    def make_apk(self, root, contents=b'safe synthetic dex'):
        apk=Path(root)/'test.apk'
        with ZipFile(apk,'w') as z: z.writestr('classes.dex', contents)
        return apk
    def fake_run(self, args, **kwargs):
        out = BADGING if args[0] == 'aapt2' else SIGNER
        return mock.Mock(returncode=0,stdout=out)
    @mock.patch.object(apkcheck.subprocess,'run')
    def test_synthetic_good_and_demo_marker(self, run):
        run.side_effect=self.fake_run
        with TemporaryDirectory() as d:
            apk=self.make_apk(d)
            self.assertEqual(apkcheck.inspect(apk,'aapt2','apksigner',CERT),[])
            apk=self.make_apk(d, ('prefix '+apkcheck.TEST_MARKERS[0]).encode())
            self.assertTrue(any('demo' in x for x in apkcheck.inspect(apk,'aapt2','apksigner',CERT)))
    @mock.patch.object(apkcheck.subprocess,'run')
    def test_later_multidex_is_inspected(self, run):
        run.side_effect=self.fake_run
        with TemporaryDirectory() as d:
            apk=self.make_apk(d)
            with ZipFile(apk,'a') as z:
                z.writestr('classes10.dex', apkcheck.TEST_MARKERS[0].encode())
            self.assertTrue(any('demo' in x for x in apkcheck.inspect(apk,'aapt2','apksigner',CERT)))
    @mock.patch.object(apkcheck.subprocess,'run')
    def test_wrong_certificate_fails(self,run):
        run.side_effect=self.fake_run
        with TemporaryDirectory() as d:
            self.assertTrue(any('signer' in x.lower() for x in apkcheck.inspect(self.make_apk(d),'aapt2','apksigner','b'*64)))
    def test_tools_missing_fails_closed(self):
        with TemporaryDirectory() as d:
            issues=apkcheck.inspect(self.make_apk(d),'/no/aapt','/no/signer',CERT)
            self.assertTrue(any('manifest' in x for x in issues))
            self.assertTrue(any('Signature' in x for x in issues))
