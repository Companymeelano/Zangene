#!/usr/bin/env python3
"""Patch the isolated offline test APK after apktool decode, not a production build.
Run with a decoded APK directory and a source folder of transparent icon assets.
The original source was not present; this limits changes to the icon loader.
"""
from pathlib import Path
import shutil, sys

root, icons = map(Path, sys.argv[1:])
smali = root/'smali/com/asiyestore/admin/app/Luxury3DIconView.smali'
s = smali.read_text()
needle = '.method private jewelBitmap(I)Landroid/graphics/Bitmap;\n    .locals 3\n'
assert s.count(needle)==1
s=s.replace(needle, needle+'''
    invoke-direct {p0, p1}, Lcom/asiyestore/admin/app/Luxury3DIconView;->additionalJewelBitmap(I)Landroid/graphics/Bitmap;
    move-result-object v0
    if-eqz v0, :jewel_original
    return-object v0
    :jewel_original
''',1)
# Use the same 256px transparent source size and in-memory cache as existing icons.
items=[(4,'crown'),(5,'phone'),(7,'whatsapp'),(8,'instagram'),(11,'heart'),(13,'crown'),(17,'gift'),(20,'chart'),(25,'ai'),(26,'camera'),(34,'box')]
helper='''.method private additionalJewelBitmap(I)Landroid/graphics/Bitmap;
    .locals 5
    sget-object v0, Lcom/asiyestore/admin/app/Luxury3DIconView;->JEWEL_CACHE:[Landroid/graphics/Bitmap;
    if-ltz p1, :jewel_none
    array-length v1, v0
    if-ge p1, v1, :jewel_none
    # An unselected heart stays outlined; do not make favorites look selected.
    const/16 v2, 0xb
    if-ne p1, v2, :jewel_cache_lookup
    iget-boolean v2, p0, Lcom/asiyestore/admin/app/Luxury3DIconView;->goldPedestal:Z
    if-eqz v2, :jewel_none
    :jewel_cache_lookup
    aget-object v1, v0, p1
    if-nez v1, :jewel_return
    sparse-switch p1, :jewel_switch
    :jewel_none
    const/4 v1, 0x0
    return-object v1
'''
for name in sorted({name for _,name in items}):
    helper += f'''    :jewel_{name}
    const-string v2, "jewel_extra/jewel_{name}.png"
    goto :jewel_load
'''
helper+='''    :jewel_load
    :try_start_jewel
    invoke-virtual {p0}, Lcom/asiyestore/admin/app/Luxury3DIconView;->getContext()Landroid/content/Context;
    move-result-object v3
    invoke-virtual {v3}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v3
    invoke-virtual {v3, v2}, Landroid/content/res/AssetManager;->open(Ljava/lang/String;)Ljava/io/InputStream;
    move-result-object v3
    invoke-static {v3}, Landroid/graphics/BitmapFactory;->decodeStream(Ljava/io/InputStream;)Landroid/graphics/Bitmap;
    move-result-object v1
    invoke-virtual {v3}, Ljava/io/InputStream;->close()V
    aput-object v1, v0, p1
    :try_end_jewel
    .catch Ljava/io/IOException; {:try_start_jewel .. :try_end_jewel} :jewel_failed
    :jewel_return
    return-object v1
    :jewel_failed
    move-exception v4
    const/4 v1, 0x0
    return-object v1
    :jewel_switch
    .sparse-switch
'''
for num,name in items: helper += f'        0x{num:x} -> :jewel_{name}\n'
helper+='''    .end sparse-switch
.end method

'''
s=s.replace(needle,helper+needle,1)
smali.write_text(s)
assets=root/'assets/jewel_extra';assets.mkdir(parents=True,exist_ok=True)
for name in {name for _,name in items}: shutil.copy2(icons/f'jewel_{name}.png',assets/f'jewel_{name}.png')
manifest=root/'AndroidManifest.xml'
m=manifest.read_text();assert m.count('com.asiyestore.admin.pearlicontest')==1
manifest.write_text(m.replace('com.asiyestore.admin.pearlicontest','com.asiyestore.admin.jewelcollectiontest'))
