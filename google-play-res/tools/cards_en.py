# -*- coding: utf-8 -*-
import render_cards as C
import card6
from PIL import Image, ImageDraw
import render_feature as F

CARDS = [
    ("en_1_gallery.png", "Find photos and videos by simply picking tags",          1660),
    ("en_2_tags.png",    "Easy tag creation — pick a name and a colour",           1450),
    ("en_3_add.png",     "Convenient tagging — choose shared and per-file tags",   1650),
    ("en_4_editor.png",  "Edit tags right in the viewer",                          2340),
    ("en_5_modes.png",   "Three search modes for quick access to the right media", 1620),
    (None,               "Adaptive design for any orientation and screen size",    None),
    ("en_7_video.png",   "View photos and videos right in the app",                2340),
]

imgs = []
for i, (shot, cap, crop) in enumerate(CARDS, 1):
    out = f"../screenshots/en/card_en_{i:02d}.png"
    if shot is None:
        im = card6.build(caption=cap, out=out, tablet="en_6_tablet.png", phone="en_1_gallery.png")
    else:
        im = C.build(shot, cap, out, crop_to=crop, phone_w=1010, center=True, fit=True)
    imgs.append(im)

th = 250
sh = Image.new("RGB", (7 * (th + 14) + 14, int(th * 1920 / 1080) + 46), (0x14, 0x11, 0x1E))
d = ImageDraw.Draw(sh)
for i, im in enumerate(imgs):
    sh.paste(im.resize((th, int(th * 1920 / 1080)), Image.LANCZOS), (14 + i * (th + 14), 34))
    d.text((16 + i * (th + 14), 10), f"card {i+1}", font=F.font(17), fill=(235, 230, 245))
sh.save("cards_en_contact.png")

ok = all(Image.open(f"../screenshots/en/card_en_{i:02d}.png").size == (1080, 1920) and
         Image.open(f"../screenshots/en/card_en_{i:02d}.png").mode == "RGB" for i in range(1, 8))
print("спецификация:", "все семь 1080x1920 RGB" if ok else "НАРУШЕНИЕ")
