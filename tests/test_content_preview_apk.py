import json
import struct
import unittest
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
APK=ROOT/'ASIYE_STORE_CONTENT_PREVIEW.apk'

def mutf8(value):
    raw=value.encode('utf-16-le','surrogatepass')
    chars=''.join(chr(int.from_bytes(raw[i:i+2],'little')) for i in range(0,len(raw),2))
    return chars.encode('utf-8','surrogatepass').replace(b'\0',b'\xc0\x80')

class ContentPreviewApkTests(unittest.TestCase):
    def test_catalog_fields_and_images_embedded(self):
        if not APK.exists(): self.skipTest('Preview APK unavailable')
        data=json.loads((ROOT/'reconstruction/catalog/luxury_coin_155_drafts.json').read_text(encoding='utf-8'))
        with zipfile.ZipFile(APK) as apk, APK.open('rb') as raw:
            dex=apk.read('classes2.dex')
            for p in data['products']:
                for field in ('short_description','description','usage','seo_title','seo_description','post_hook','image_alt_text',
                              'instagram_caption','telegram_caption','youtube_caption','whatsapp_caption'):
                    self.assertIn(mutf8(p[field]),dex,(p['draft_id'],field))
                for word in p['keywords']: self.assertIn(mutf8(word),dex)
                image='res/drawable-nodpi/lc155_'+p['draft_id'][6:].lower()+'.jpg'
                self.assertEqual(apk.read(image),(ROOT/p['image_path']).read_bytes())
            for item in apk.infolist():
                if item.compress_type != zipfile.ZIP_STORED: continue
                raw.seek(item.header_offset); header=raw.read(30)
                name,extra=struct.unpack('<HH',header[26:30])
                self.assertEqual((item.header_offset+30+name+extra)%4,0,item.filename)
