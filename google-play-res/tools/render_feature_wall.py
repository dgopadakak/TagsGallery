"""Feature graphic 1024x500, вариант A «стена с тегами». Две локали: ru и en."""
import os
import random

from PIL import Image, ImageChops, ImageDraw

import render_feature as F
import render_icons as R

OUT = os.path.dirname(os.path.abspath(__file__))
W, H, SS = 1024, 500, 3
CW, CH = W * SS, H * SS

# Подсветка: Material Green 500. Зелёный отделяется от фиолетового фона, синий нет,
# а палитровый #36CF36 из приложения на этом фоне выходит кислотным.
SELECTED = (0x4C, 0xAF, 0x50)
OTHERS = [F.TAG_RED, F.TAG_YELLOW, F.TAG_BLUE]

LAYOUT = {}

TEXT = {
    "ru": ("TagsGallery", "Найдите нужное по тегам"),
    "en": ("TagsGallery", "Find what you need by tags"),
}


def photo_card(w, h, rng, tag_color=None):
    """Карточка с ярлыком, свисающим за угол. Слой с симметричным запасом,
    иначе свисающая часть обрезается границей слоя."""
    m = max(w, h)
    ov = int(m * 0.24)
    L = Image.new("RGBA", (w + 2 * ov, h + 2 * ov), (0, 0, 0, 0))
    d = ImageDraw.Draw(L)
    r = max(3, int(m * 0.07))
    d.rounded_rectangle([ov, ov, ov + w - 1, ov + h - 1], radius=r, fill=(255, 255, 255, 255))

    inner = Image.new("RGBA", L.size, (0, 0, 0, 0))
    di = ImageDraw.Draw(inner)
    b = max(2, int(m * 0.05))
    di.rectangle([ov + b, ov + b, ov + w - b, ov + h - b],
                 fill=F.SKIES[rng.randrange(len(F.SKIES))] + (255,))
    sr = rng.uniform(0.08, 0.12) * m
    sx = ov + rng.uniform(0.58, 0.84) * w
    sy = ov + rng.uniform(0.20, 0.32) * h
    di.ellipse([sx - sr, sy - sr, sx + sr, sy + sr], fill=F.SUN + (255,))
    for _ in range(rng.randrange(1, 3)):
        base = ov + h - b
        pxx = ov + rng.uniform(0.15, 0.8) * w
        pyy = ov + rng.uniform(0.30, 0.58) * h
        half = rng.uniform(0.30, 0.55) * w
        di.polygon([(pxx - half, base), (pxx, pyy), (pxx + half, base)],
                   fill=F.RIDGES[rng.randrange(len(F.RIDGES))] + (255,))
    mask = Image.new("L", L.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([ov + b, ov + b, ov + w - b - 1, ov + h - b - 1],
                                           radius=max(2, r - b), fill=255)
    inner.putalpha(ImageChops.multiply(inner.getchannel("A"), mask))
    L = Image.alpha_composite(L, inner)

    if tag_color:
        tw = int(m * 0.42)
        th = int(tw * 18 / 24)
        L = F.draw_tag(L, ov + w - tw * 0.58, ov + h - th * 0.58, tw, th,
                       tag_color, rot=180, hole=True)
    return L


def build(locale, selected=SELECTED):
    s = SS
    c = F.gradient()
    rng = random.Random(11)
    name, tagline = TEXT[locale]

    # --- левый блок: позиции считаются от измеренных чернил, а не заданы числами.
    # У «TagsGallery» есть хвосты g и y, поэтому фиксированные координаты давали наезд. ---
    d = ImageDraw.Draw(c)
    fw, ft = F.font(58 * s), F.font(27 * s, bold=False)
    x_text = 84 * s

    icon_sz, icon_cy = 168 * s, 178 * s
    F.place(c, F.icon_tile(icon_sz), 168 * s, icon_cy)

    def put(text, fnt, x, ink_top, fill):
        probe = d.textbbox((x, 0), text, font=fnt)
        y = ink_top - probe[1]
        d.text((x, y), text, font=fnt, fill=fill)
        return d.textbbox((x, y), text, font=fnt)

    nb = put(name, fw, x_text, icon_cy + icon_sz / 2 + 26 * s, (255, 255, 255, 255))
    tb = put(tagline, ft, x_text + 2 * s, nb[3] + 19 * s, (0xCB, 0xBF, 0xEA, 255))
    LAYOUT[locale] = dict(name=nb, tag=tb, gap=(tb[1] - nb[3]) / s)
    chips_top = tb[3] + 22 * s

    palette = [F.TAG_RED, F.TAG_YELLOW, selected, F.TAG_BLUE]
    for i, col in enumerate(palette):
        tmp = Image.new("RGBA", c.size, (0, 0, 0, 0))
        tmp = F.draw_tag(tmp, (90 + i * 78) * s, chips_top, 64 * s, 48 * s,
                         col, rot=180, hole=True)
        if col != selected:
            tmp.putalpha(tmp.getchannel("A").point(lambda v: int(v * 0.34)))
        c.alpha_composite(tmp)

    # --- сетка карточек: всё внутри 1024 с полем справа ---
    cw, ch = 118, 87
    gap, x0, y0 = 14, 444, 105
    hits = {1, 3, 4, 9, 10}
    for r_ in range(3):
        for q in range(4):
            i = r_ * 4 + q
            hit = i in hits
            col = selected if hit else OTHERS[rng.randrange(len(OTHERS))]
            card = photo_card(cw * s, ch * s, rng, tag_color=col)
            cx = (x0 + q * (cw + gap) + cw / 2 + rng.randint(-4, 4)) * s
            cy = (y0 + r_ * (ch + gap) + ch / 2 + rng.randint(-4, 4)) * s
            F.place(c, card, cx, cy, angle=rng.uniform(-5, 5),
                    alpha=1.0 if hit else 0.26)
    return c.convert("RGB").resize((W, H), Image.LANCZOS)


def main():
    for loc in ("ru", "en"):
        img = build(loc)
        p = os.path.join(OUT, "..", f"feature_graphic_{loc}_1024x500.png")
        img.save(p)
        chk = Image.open(p)
        print(f"{loc}: {p}  {chk.size}  {chk.mode}  альфа: {'A' in chk.getbands()}")

    a = Image.open(os.path.join(OUT, "..", "feature_graphic_ru_1024x500.png"))
    b = Image.open(os.path.join(OUT, "..", "feature_graphic_en_1024x500.png"))
    sheet = Image.new("RGB", (W + 32, H * 2 + 84), (0x14, 0x11, 0x1E))
    d = ImageDraw.Draw(sheet)
    d.text((16, 8), "RU", font=F.font(22), fill=(0xFF, 0xFF, 0xFF))
    sheet.paste(a, (16, 34))
    d.text((16, H + 46), "EN", font=F.font(22), fill=(0xFF, 0xFF, 0xFF))
    sheet.paste(b, (16, H + 72))
    sp = os.path.join(OUT, "feature_graphic_sheet.png")
    sheet.save(sp)
    print("лист:", sp, sheet.size)


if __name__ == "__main__":
    main()
