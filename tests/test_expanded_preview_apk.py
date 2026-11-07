import json,struct,unittest,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
APK=ROOT/'ASIYE_STORE_20_PRODUCT_PREVIEW.apk'
def mutf8(s):
 raw=s.encode('utf-16-le','surrogatepass')
 return ''.join(chr(int.from_bytes(raw[i:i+2],'little')) for i in range(0,len(raw),2)).encode('utf8','surrogatepass').replace(b'\0',b'\xc0\x80')
class ExpandedPreviewTests(unittest.TestCase):
 def test_apk_contains_twenty_complete_catalog_items(self):
  if not APK.exists():self.skipTest('APK unavailable')
  products=[]
  for name in ('luxury_coin_155_drafts.json','luxury_coin_extra_drafts.json'):
   products += json.loads((ROOT/'reconstruction/catalog'/name).read_text(encoding='utf8'))['products']
  self.assertEqual(len(products),20)
  with zipfile.ZipFile(APK) as apk,APK.open('rb') as raw:
   dex=apk.read('classes2.dex')
   for p in products:
    for field in ('draft_id','product_name','category','description','short_description','usage','seo_title','seo_description','post_hook','image_alt_text','instagram_caption','telegram_caption','youtube_caption','whatsapp_caption'):
     self.assertIn(mutf8(p[field]),dex,(p['draft_id'],field))
    for word in p['keywords']:self.assertIn(mutf8(word),dex)
    self.assertEqual(apk.read('res/drawable-nodpi/lc155_'+p['draft_id'][6:].lower()+'.jpg'),(ROOT/p['image_path']).read_bytes())
   for i in apk.infolist():
    if i.compress_type!=zipfile.ZIP_STORED:continue
    raw.seek(i.header_offset); h=raw.read(30); a,b=struct.unpack('<HH',h[26:30]);self.assertEqual((i.header_offset+30+a+b)%4,0,i.filename)
