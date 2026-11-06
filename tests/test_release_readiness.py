import importlib.util
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

root = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('release_readiness', root/'tools/release_readiness.py')
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)

class ReleaseGateTests(unittest.TestCase):
    def test_empty_workspace_fails_closed(self):
        with TemporaryDirectory() as d:
            issues = release.evaluate(d)
            self.assertTrue(any('Android application source is missing' in x for x in issues))
            self.assertTrue(any('Laravel application entry point' in x for x in issues))
    def test_old_source_and_test_manifest_are_rejected(self):
        with TemporaryDirectory() as d:
            p = Path(d)
            src = p/'android/app/src/main/java/com/asiyestore/admin/app/MainActivity.java'
            src.parent.mkdir(parents=True)
            src.write_text('class MainActivity {}')
            manifest = p/'android/app/src/main/AndroidManifest.xml'
            manifest.write_text('<manifest package="com.asiyestore.admin.screenshotpolishtest"/>')
            issues = release.evaluate(p)
            self.assertTrue(any('latest design' in x for x in issues))
            self.assertTrue(any('isolated test build' in x for x in issues))
            self.assertTrue(any('no internet permission' in x for x in issues))

if __name__ == '__main__': unittest.main()
