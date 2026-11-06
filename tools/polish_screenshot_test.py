#!/usr/bin/env python3
"""Narrow reversible changes to the isolated OFFLINE demo, applied to an apktool decode.
This is not the missing Android source or a release build.
"""
from pathlib import Path
import sys, shutil
root, assets = map(Path, sys.argv[1:])
main=root/'smali/com/asiyestore/admin/app/MainActivity.smali'
s=main.read_text()
a=s.index('.method private welcomeCard()Landroid/view/View;'); b=s.index('.end method',a)+len('.end method')
w=s[a:b]
# Theme-local substitutions, leaving the pre-existing dark themes unchanged.
for register,original,replacement in [('v2','COL_OBSIDIAN','COL_WHITE'),('v3','TEXT_ON_DARK','COL_TEXT_MAIN'),('v1','TEXT_ON_DARK_MUTED','COL_TEXT_MUTED')]:
    needle=f'    sget {register}, Lcom/asiyestore/admin/app/MainActivity;->{original}:I\n'
    assert w.count(needle)==1, original
    w=w.replace(needle,needle+f'''    sget v7, Lcom/asiyestore/admin/app/MainActivity;->{replacement}:I
    invoke-static {{{register}, v7}}, Lcom/asiyestore/admin/app/MainActivity;->polishColor(II)I
    move-result {register}
''')
old='''    invoke-direct {p0, v3, v1}, Lcom/asiyestore/admin/app/MainActivity;->ghostDarkButton(Ljava/lang/String;Ljava/lang/Runnable;)Landroid/view/View;

    move-result-object v1'''
new='''    sget v7, Lcom/asiyestore/admin/app/MainActivity;->themeMode:I
    const/4 v4, 0x6
    if-ne v7, v4, :welcome_dark_link
    invoke-direct {p0, v3, v1}, Lcom/asiyestore/admin/app/MainActivity;->ghostGoldButton(Ljava/lang/String;Ljava/lang/Runnable;)Landroid/view/View;
    move-result-object v1
    goto :welcome_link_done
    :welcome_dark_link
    invoke-direct {p0, v3, v1}, Lcom/asiyestore/admin/app/MainActivity;->ghostDarkButton(Ljava/lang/String;Ljava/lang/Runnable;)Landroid/view/View;
    move-result-object v1
    :welcome_link_done'''
assert w.count(old)==1;w=w.replace(old,new)
def esc(text):
    import re
    return re.sub(r'\\x([0-9a-f]{2})', lambda m: r'\u00'+m.group(1), text.encode('unicode_escape').decode('ascii'))
old_claim=esc('ورود با کد پیامکی انجام می‌شود؛ رمز شما هرگز روی سرور نگه‌داری نمی‌شود.')
assert old_claim in w
w=w.replace(old_claim,esc('نسخه آزمایشی آفلاین است؛ پیامک و ورود واقعی فعال نیستند.'))
s=s[:a]+w+s[b:]
helper='''.method private static polishColor(II)I
    .locals 2
    sget v0, Lcom/asiyestore/admin/app/MainActivity;->themeMode:I
    const/4 v1, 0x6
    if-ne v0, v1, :use_old_color
    return p1
    :use_old_color
    return p0
.end method

'''
s=s[:a]+helper+s[a:]
old_header=esc('نسخه آزمایشی آفلاین');new_header=esc('آفلاین · قیمت و تصویر نمونه')
assert s.count('\"'+old_header+'\"')==2
s=s.replace('\"'+old_header+'\"','\"'+new_header+'\"')
main.write_text(s)
# Never insert fabricated seed reviews into this isolated test; existing records are not deleted.
db=root/'smali/com/asiyestore/admin/app/AsiyeDatabaseHelper.smali'
s=db.read_text();a=s.index('.method public static seedSignatureReviews(Landroid/database/sqlite/SQLiteDatabase;)V');b=s.index('.end method',a)+len('.end method')
s=s[:a]+'.method public static seedSignatureReviews(Landroid/database/sqlite/SQLiteDatabase;)V\n    .locals 0\n    return-void\n.end method'+s[b:];db.write_text(s)
# Add carefully matched 3D assets, leaving unspecified icon types untouched.
ic=root/'smali/com/asiyestore/admin/app/Luxury3DIconView.smali'
s=ic.read_text();a=s.index('.method private additionalJewelBitmap(I)Landroid/graphics/Bitmap;');b=s.index('.end method',a)+len('.end method')
h=s[a:b];assert ':goto_0\n' in h and '.sparse-switch\n' in h
for name in ['share','bell','doc']:
    h=h.replace('    :goto_0\n',f'''    :new_{name}
    const-string v2, "jewel_extra/jewel_{name}.png"
    goto :goto_0

    :goto_0
''',1)
for old,new in [('        0x14 ->', '        0x12 -> :new_share\n        0x14 ->'),('    .end sparse-switch','        0x27 -> :new_bell\n        0x29 -> :new_doc\n    .end sparse-switch')]:
    assert h.count(old)==1; h=h.replace(old,new)
s=s[:a]+h+s[b:];ic.write_text(s)
dest=root/'assets/jewel_extra';dest.mkdir(parents=True,exist_ok=True)
for name in ['share','bell','doc']:shutil.copy2(assets/f'jewel_{name}.png',dest/f'jewel_{name}.png')
manifest=root/'AndroidManifest.xml';s=manifest.read_text();assert s.count('com.asiyestore.admin.jewelcollectiontest')==1
manifest.write_text(s.replace('com.asiyestore.admin.jewelcollectiontest','com.asiyestore.admin.screenshotpolishtest'))
