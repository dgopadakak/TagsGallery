# -*- coding: utf-8 -*-
"""Карточка 6: планшет в горизонте и телефон внахлёст, с лёгким разворотом."""
import render_cards as C
import render_feature as F
from PIL import Image, ImageDraw, ImageFilter


def make_panel(path, w, crop=None, radius=34, rot=0.0):
    im = Image.open(C.raw(path)).convert("RGB")
    if crop:
        im = im.crop((0, 0, im.width, crop))
    h = round(w * im.height / im.width)
    im = im.resize((w, h), Image.LANCZOS).convert("RGBA")
    im.putalpha(C.rounded_mask((w, h), radius))
    ring = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ImageDraw.Draw(ring).rounded_rectangle([0, 0, w - 1, h - 1], radius=radius,
                                           outline=(255, 255, 255, 78), width=3)
    im = Image.alpha_composite(im, ring)
    if rot:
        im = im.rotate(rot, resample=Image.BICUBIC, expand=True)
    return im


def place(canvas, panel, x, y, blur=30, dy=20, opacity=0.62):
    dark = Image.new("RGBA", panel.size, (0, 0, 0, 0))
    dark.putalpha(panel.getchannel("A").point(lambda v: int(v * opacity)))
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    sh.alpha_composite(dark, (x, y + dy))
    canvas = Image.alpha_composite(canvas, sh.filter(ImageFilter.GaussianBlur(blur)))
    canvas.alpha_composite(panel, (x, y))
    return canvas


def build(caption="Адаптивный дизайн под любой режим работы и диагональ",
          out="card_ru_06.png", tablet="ru_6_tablet.png", phone="ru_1_gallery.png"):
    canvas = C.gradient(C.W, C.H)
    d = ImageDraw.Draw(canvas)
    f = F.font(C.CAP_SIZE)
    _, bottom = C.draw_caption(d, caption, f)

    tab = make_panel(tablet, 1010, rot=-4.0)
    ph = make_panel(phone, 486, crop=1700, rot=6.0)

    ty = bottom + 64
    canvas = place(canvas, tab, (C.W - tab.size[0]) // 2, ty)
    py = ty + tab.size[1] - 120                      # нахлёст на нижний край планшета
    canvas = place(canvas, ph, C.W - ph.size[0] - 36, py, blur=34, dy=24, opacity=0.7)

    img = canvas.convert("RGB")
    img.save(C.out_path(out))
    print(f"{out}: планшет {tab.size} с y={ty}, телефон {ph.size} с y={py}, "
          f"низ телефона {py + ph.size[1]} из {C.H}")
    return img


if __name__ == "__main__":
    build()
