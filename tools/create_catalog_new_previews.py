#!/usr/bin/env python3
"""Compose screenshot product crops on the tracked backdrop, without inventing labels."""
import argparse
from pathlib import Path
import subprocess, tempfile
ROOT=Path(__file__).resolve().parents[1]
ITEMS={
 'vitaminc':('195558',170,480,360,550), 'allinone':('195553',170,645,360,520),
 'caffeine':('195547',170,485,360,525), 'blackgarlic':('195543',170,550,360,530),
 'micellardry':('202802',135,385,430,520), 'micellaroily':('202808',135,315,430,560),
 'micellarnormal':('202813',135,375,430,510), 'gatierepair':('202832',175,460,350,475),
 'gatieantiwrinkle':('202838',175,500,350,460), 'gatiebody':('202845',175,360,350,480),
}
def create(screenshots, output):
 screenshots,output=Path(screenshots),Path(output)
 sources=[screenshots/f'Screenshot_20261107_{x[0]}_Samsung Browser.jpg' for x in ITEMS.values()]
 if any(not p.is_file() for p in sources): raise ValueError('Missing screenshot')
 output.mkdir(parents=True,exist_ok=True)
 if any((output/f'{key}.jpg').exists() for key in ITEMS): raise FileExistsError('Refusing to overwrite')
 with tempfile.TemporaryDirectory() as t:
  t=Path(t); mask=t/'mask.png'
  subprocess.run(['convert','-size','760x718','xc:black','-fill','white','-draw','roundrectangle 40,40 720,678 30,30','-blur','0x16',str(mask)],check=True)
  for key,(stamp,x,y,w,h) in ITEMS.items():
   x,y,w,h=(round(v*1080/703) for v in (x,y,w,h))
   crop=t/'crop.png'; layer=t/'layer.png'; source=screenshots/f'Screenshot_20261107_{stamp}_Samsung Browser.jpg'
   subprocess.run(['convert',str(source),'-crop',f'{w}x{h}+{x}+{y}','+repage','-bordercolor','white','-border','70','-resize','760x718','-gravity','center','-background','white','-extent','760x718',str(crop)],check=True)
   subprocess.run(['convert',str(crop),str(mask),'-alpha','off','-compose','CopyOpacity','-composite',str(layer)],check=True)
   subprocess.run(['convert',str(ROOT/'reconstruction/android/assets/catalog/biz_pink_backdrop.png'),str(layer),'-gravity','center','-geometry','+0-35','-compose','Over','-composite','-strip','-quality','91',str(output/f'{key}.jpg')],check=True)
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('screenshots');p.add_argument('output');a=p.parse_args();create(a.screenshots,a.output)
