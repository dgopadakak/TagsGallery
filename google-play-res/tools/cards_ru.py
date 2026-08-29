# -*- coding: utf-8 -*-
import render_cards as C
import card6
from PIL import Image, ImageDraw
import render_feature as F

CARDS = [
    ("ru_1_gallery.png",  "Поиск фото и видео простым выбором тегов",                   1660, 1010),
    ("ru_2_tags.png",     "Лёгкое создание тегов — придумай название и выбери цвет",    1450, 1010),
    ("ru_3_add.png",      "Удобное присвоение тегов — выбери общие и индивидуальные теги", 1650, 1010),
    ("ru_4_editor.png",   "Редактирование тегов прямо в режиме просмотра",              2340, 1010),
    ("ru_5_modes.png",    "Три режима поиска для быстрого доступа к нужным медиа",      1620, 1010),
    (None,                "Адаптивный дизайн под любой режим работы и диагональ",       None, 1010),
    ("ru_7_video.png",    "Просмотр фото и видео прямо в приложении",                   2340, 1010),
]

imgs = []
for i, (shot, cap, crop, w) in enumerate(CARDS, 1):
    out = f"../screenshots/ru/card_ru_{i:02d}.png"
    if shot is None:                      # карточка адаптивности собирается отдельно
        im = card6.build(caption=cap, out=out, tablet="ru_6_tablet.png", phone="ru_1_gallery.png")
    else:
        im = C.build(shot, cap, out, crop_to=crop, phone_w=w, center=True, fit=True)
    imgs.append(im)

th_w = 250
sh = Image.new("RGB", (len(imgs) * (th_w + 14) + 14, int(th_w * 1920 / 1080) + 46), (0x14, 0x11, 0x1E))
d = ImageDraw.Draw(sh)
for i, im in enumerate(imgs):
    sh.paste(im.resize((th_w, int(th_w * 1920 / 1080)), Image.LANCZOS), (14 + i * (th_w + 14), 34))
    d.text((16 + i * (th_w + 14), 10), f"карточка {i+1}", font=F.font(17), fill=(235, 230, 245))
sh.save("cards_ru_contact.png")
print("обзор:", sh.size)
