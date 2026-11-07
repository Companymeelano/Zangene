#!/usr/bin/env python3
"""Patch only a decoded offline demo to preview ten screenshot-based products.

Not production source. Disables the demo administrator entry and uses a separate package.
Call only against a temporary apktool decode of the pinned offline APK. No existing DB is touched.
"""
from pathlib import Path
from hashlib import sha256
import shutil
import sys

if len(sys.argv) != 3:
    raise SystemExit('Usage: prepare_catalog_preview.py <apktool-decoded-dir> <pinned-demo.apk>')
root=Path(sys.argv[1])
original=Path(sys.argv[2])
if sha256(original.read_bytes()).hexdigest() != 'e55ea9c58603a88fefd03bcb127edbb5ac420c89ac35ab21c94aa99c67e8031b':
    raise ValueError('Incorrect demo APK checksum')
repo=Path(__file__).resolve().parents[1]
manifest=root/'AndroidManifest.xml'
s=manifest.read_text()
old='package="com.asiyestore.admin.screenshotpolishtest"'
assert s.count(old)==1
manifest.write_text(s.replace(old,'package="com.asiyestore.admin.catalogpreview"'))
yml=root/'apktool.yml'; s=yml.read_text(); assert 'renameManifestPackage: null' in s
yml.write_text(s.replace('renameManifestPackage: null','renameManifestPackage: com.asiyestore.admin.catalogpreview'))
db=root/'smali/com/asiyestore/admin/app/AsiyeDatabaseHelper.smali'; s=db.read_text()
a=s.index('.method public onCreate(Landroid/database/sqlite/SQLiteDatabase;)V'); b=s.index('.end method',a)
m=s[a:b]
seed='    invoke-direct {p0, p1}, Lcom/asiyestore/admin/app/AsiyeDatabaseHelper;->seedSignatureCatalog(Landroid/database/sqlite/SQLiteDatabase;)V'
refs='    invoke-direct {p0, p1}, Lcom/asiyestore/admin/app/AsiyeDatabaseHelper;->seedReferenceDrafts(Landroid/database/sqlite/SQLiteDatabase;)V'
assert m.count(seed)==m.count(refs)==1
# Only the ten review products in this separate preview database.
m=m.replace(seed,'').replace(refs,'    invoke-static {p1}, Lcom/asiyestore/admin/app/OfflineCatalogPreview;->seedPreview(Landroid/database/sqlite/SQLiteDatabase;)V')
s=s[:a]+m+s[b:]; db.write_text(s)
activity=root/'smali/com/asiyestore/admin/app/MainActivity.smali'; s=activity.read_text()
a=s.index('.method private resolveProductImage(Ljava/lang/String;)I'); b=s.index('.end method',a)
m=s[a:b]; needle='    .locals 2\n'; assert m.count(needle)==1
m=m.replace(needle,needle+'''\n    invoke-static {p0, p1}, Lcom/asiyestore/admin/app/OfflineCatalogPreview;->imageResource(Landroid/content/Context;Ljava/lang/String;)I
    move-result v0
    if-eqz v0, :catalog_fallback
    return v0
    :catalog_fallback
''');s=s[:a]+m+s[b:]
a=s.index('.method private showAdminLoginDialog()V');b=s.index('.end method',a)+len('.end method')
# Unlike the source demo, this preview cannot log in as administrator.
message='ورود مدیریت در پیش‌نمایش غیرفعال است؛ قیمت و موجودی صرفاً نمونه‌اند.'
def esc(t): return t.encode('unicode_escape').decode('ascii')
s=s[:a]+'''.method private showAdminLoginDialog()V
    .locals 2
    const-string v0, "'''+esc(message)+'''"
    const/4 v1, 0x1
    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
    move-result-object v0
    invoke-virtual {v0}, Landroid/widget/Toast;->show()V
    return-void
.end method'''+s[b:]
activity.write_text(s)
target=root/'res/drawable-nodpi'
for p in sorted((repo/'reconstruction/android/assets/catalog/luxury_coin').glob('*.jpg')):
    shutil.copyfile(p,target/('lc155_'+p.name))
