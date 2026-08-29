"""Feature graphic 1024x500 для Google Play — три варианта компоновки."""
import math
import os
import random

from PIL import Image, ImageChops, ImageDraw, ImageFont

import render_icons as R

OUT = os.path.dirname(os.path.abspath(__file__))
W, H = 1024, 500
SS = 3                      # супersampling
CW, CH = W * SS, H * SS

BG_TOP = (0x5B, 0x3F, 0xA8)
BG_BOT = (0x2E, 0x1B, 0x58)

# Цвета тегов как их рисует приложение: lerp(Color.Gray, чистый цвет, 0.6)
GRAY = (0x88, 0x88, 0x88)


def app_tag_color(pure):
    return tuple(round(GRAY[i] + (pure[i] - GRAY[i]) * 0.6) for i in range(3))


TAG_RED = app_tag_color((255, 0, 0))
TAG_YELLOW = app_tag_color((255, 255, 0))
TAG_GREEN = app_tag_color((0, 255, 0))
TAG_BLUE = app_tag_color((0, 0, 255))
TAG_COLORS = [TAG_RED, TAG_YELLOW, TAG_GREEN, TAG_BLUE]

SKIES = [(0xF2, 0xE6, 0xD8), (0xDCE6F2 >> 16, (0xDCE6F2 >> 8) & 0xFF, 0xDCE6F2 & 0xFF),
         (0xF7, 0xEC, 0xF2), (0xE4, 0xF0, 0xE8), (0xF5, 0xF0, 0xE2)]
RIDGES = [(0x66, 0x50, 0xA4), (0x7A, 0x63, 0xB8), (0x53, 0x41, 0x8C),
          (0x8A, 0x74, 0xC4), (0x60, 0x4B, 0x9C)]
SUN = (0xFF, 0xB0, 0x20)


def font(sz, bold=True):
    p = r"C:\Windows\Fonts\segoeuib.ttf" if bold else r"C:\Windows\Fonts\segoeui.ttf"
    return ImageFont.truetype(p, sz)


def gradient():
    g = Image.new("RGB", (256, 256))
    px = g.load()
    for y in range(256):
        for x in range(256):
            t = (x / 255 * 0.35 + y / 255 * 0.65)
            px[x, y] = tuple(round(BG_TOP[i] + (BG_BOT[i] - BG_TOP[i]) * t) for i in range(3))
    return g.resize((CW, CH), Image.BICUBIC).convert("RGBA")


def tag_shape(w, h):
    """Ярлык из иконки, вписанный в прямоугольник w x h, остриём влево."""
    pts = R.tag_outline()                       # координаты 54..78 x 57..75
    x0 = min(p[0] for p in pts); x1 = max(p[0] for p in pts)
    y0 = min(p[1] for p in pts); y1 = max(p[1] for p in pts)
    sx, sy = w / (x1 - x0), h / (y1 - y0)
    return [((x - x0) * sx, (y - y0) * sy) for x, y in pts]


def draw_tag(layer, x, y, w, h, color, rot=180, hole=True):
    pts = tag_shape(w, h)
    cx, cy = w / 2, h / 2
    pts = R.rot(pts, cx, cy, rot)
    pts = [(px + x - cx + w / 2, py + y - cy + h / 2) for px, py in pts]
    tl = Image.new("RGBA", layer.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(tl)
    d.polygon(pts, fill=color + (255,))
    if hole and w > 30:
        hx, hy = R.rot([(70.8, 66.0)], 66, 66, 0)[0]
        # положение дырки в долях исходного бокса
        fx, fy = (70.8 - 54) / 24.0, (66 - 57) / 18.0
        hp = R.rot([(fx * w, fy * h)], cx, cy, rot)[0]
        r = min(w, h) * 0.11
        d.ellipse([hp[0] + x - cx + w / 2 - r, hp[1] + y - cy + h / 2 - r,
                   hp[0] + x - cx + w / 2 + r, hp[1] + y - cy + h / 2 + r], fill=(0, 0, 0, 0))
    return Image.alpha_composite(layer, tl)


def photo_card(w, h, rng, tag_color=None, pad=None):
    """Карточка-фото со случайным пейзажем; возвращает RGBA слой размера w x h."""
    m = max(w, h)
    im = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    r = max(3, int(m * 0.07))
    d.rounded_rectangle([0, 0, w - 1, h - 1], radius=r, fill=(255, 255, 255, 255))

    inner = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    di = ImageDraw.Draw(inner)
    b = max(2, int(m * 0.05))
    di.rectangle([b, b, w - b, h - b], fill=SKIES[rng.randrange(len(SKIES))] + (255,))
    sr = rng.uniform(0.07, 0.11) * m
    sx = rng.uniform(0.6, 0.85) * w
    sy = rng.uniform(0.2, 0.34) * h
    di.ellipse([sx - sr, sy - sr, sx + sr, sy + sr], fill=SUN + (255,))
    for k in range(rng.randrange(1, 3)):
        base = h - b
        peak_x = rng.uniform(0.15, 0.8) * w
        peak_y = rng.uniform(0.32, 0.6) * h
        half = rng.uniform(0.3, 0.55) * w
        di.polygon([(peak_x - half, base), (peak_x, peak_y), (peak_x + half, base)],
                   fill=RIDGES[rng.randrange(len(RIDGES))] + (255,))
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).rounded_rectangle([b, b, w - b - 1, h - b - 1], radius=max(2, r - b), fill=255)
    inner.putalpha(ImageChops.multiply(inner.getchannel("A"), mask))
    im = Image.alpha_composite(im, inner)

    if tag_color:
        tw = int(m * 0.42)
        th = int(tw * 18 / 24)
        im = draw_tag(im, w - tw * 0.62, h - th * 0.62, tw, th, tag_color, rot=180, hole=False)
    return im


def place(canvas, layer, cx, cy, angle=0.0, alpha=1.0):
    if angle:
        layer = layer.rotate(angle, resample=Image.BICUBIC, expand=True)
    if alpha < 1.0:
        a = layer.getchannel("A").point(lambda v: int(v * alpha))
        layer.putalpha(a)
    canvas.alpha_composite(layer, (int(cx - layer.size[0] / 2), int(cy - layer.size[1] / 2)))


def icon_tile(size):
    img, _ = R.render(dict(recenter=True, simple_landscape=False, hole="small",
                           fan=(-20, -10), back_dark=True, scale=1.0), 1080)
    tile = R.safezone(img).resize((size, size), Image.LANCZOS).convert("RGBA")
    mask = Image.new("L", (size * 4, size * 4), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size * 4 - 1, size * 4 - 1],
                                           radius=int(size * 4 * 0.22), fill=255)
    tile.putalpha(mask.resize((size, size), Image.LANCZOS))
    return tile


# ---------------- варианты ----------------

def variant_wall():
    """A — стена с тегами: выбран синий, совпавшие карточки яркие, остальные притушены."""
    c = gradient()
    rng = random.Random(7)
    s = SS
    tile = icon_tile(168 * s)
    place(c, tile, 168 * s, 196 * s)
    d = ImageDraw.Draw(c)
    d.text((84 * s, 300 * s), "TagsGallery", font=font(58 * s), fill=(255, 255, 255, 255))
    d.text((86 * s, 364 * s), "Найдите нужное по тегам",
           font=font(27 * s, bold=False), fill=(0xC9, 0xBC, 0xE8, 255))

    chosen = TAG_BLUE
    for i, col in enumerate(TAG_COLORS):
        x = (88 + i * 62) * s
        sel = col == chosen
        c2 = c
        c.alpha_composite(Image.new("RGBA", (1, 1), (0, 0, 0, 0)))
        tmp = Image.new("RGBA", c.size, (0, 0, 0, 0))
        tmp = draw_tag(tmp, x, 424 * s, 46 * s, 34 * s, col, rot=180, hole=False)
        if not sel:
            tmp.putalpha(tmp.getchannel("A").point(lambda v: int(v * 0.38)))
        c.alpha_composite(tmp)

    cw, ch = 132 * s, 98 * s
    cols, rows = 4, 3
    gx, gy = 26 * s, 22 * s
    x0 = 452 * s
    y0 = 78 * s
    for r_ in range(rows):
        for q in range(cols):
            hit = (r_ * cols + q) in (1, 3, 4, 9, 10)
            col = chosen if hit else TAG_COLORS[rng.randrange(3)]
            card = photo_card(cw, ch, rng, tag_color=col)
            cx = x0 + q * (cw + gx) + cw / 2 + rng.randint(-6 * s, 6 * s)
            cy = y0 + r_ * (ch + gy) + ch / 2 + rng.randint(-6 * s, 6 * s)
            place(c, card, cx, cy, angle=rng.uniform(-5, 5), alpha=1.0 if hit else 0.28)
    return c


def variant_funnel():
    """B — воронка: много неразобранного слева, тег посередине, найденное справа."""
    c = gradient()
    rng = random.Random(21)
    s = SS
    tile = icon_tile(84 * s)
    place(c, tile, 96 * s, 74 * s)
    d = ImageDraw.Draw(c)
    d.text((148 * s, 48 * s), "TagsGallery", font=font(44 * s), fill=(255, 255, 255, 255))

    for i in range(11):
        w_ = rng.randint(58, 74) * s
        h_ = int(w_ * 0.74)
        card = photo_card(w_, h_, rng, tag_color=TAG_COLORS[rng.randrange(4)])
        place(c, card, rng.randint(70, 300) * s, rng.randint(190, 430) * s,
              angle=rng.uniform(-16, 16), alpha=0.34)

    tmp = Image.new("RGBA", c.size, (0, 0, 0, 0))
    tmp = draw_tag(tmp, 430 * s, 268 * s, 132 * s, 99 * s, TAG_BLUE, rot=180, hole=True)
    c.alpha_composite(tmp)
    d.text((398 * s, 386 * s), "один тег", font=font(30 * s, bold=False),
           fill=(0xC9, 0xBC, 0xE8, 255))

    for i in range(3):
        card = photo_card(150 * s, 112 * s, rng, tag_color=TAG_BLUE)
        place(c, card, (660 + i * 130) * s, (250 + (i % 2) * 26) * s,
              angle=rng.uniform(-4, 4), alpha=1.0)
    return c


def variant_minimal():
    """C — минимализм: иконка, вордмарк, лёгкий фон из карточек."""
    c = gradient()
    rng = random.Random(3)
    s = SS
    for i in range(7):
        w_ = rng.randint(120, 190) * s
        h_ = int(w_ * 0.74)
        card = photo_card(w_, h_, rng)
        place(c, card, rng.randint(560, 1000) * s, rng.randint(60, 440) * s,
              angle=rng.uniform(-14, 14), alpha=0.17)

    tile = icon_tile(208 * s)
    place(c, tile, 190 * s, 250 * s)
    d = ImageDraw.Draw(c)
    d.text((330 * s, 196 * s), "TagsGallery", font=font(66 * s), fill=(255, 255, 255, 255))
    d.text((334 * s, 278 * s), "Теги для фото и видео на вашем устройстве",
           font=font(28 * s, bold=False), fill=(0xC9, 0xBC, 0xE8, 255))
    tmp = Image.new("RGBA", c.size, (0, 0, 0, 0))
    tmp = draw_tag(tmp, 336 * s, 330 * s, 58 * s, 43 * s, TAG_BLUE, rot=180, hole=False)
    c.alpha_composite(tmp)
    return c


VARIANTS = [("A — стена с тегами", "feature_A_wall", variant_wall),
            ("B — воронка", "feature_B_funnel", variant_funnel),
            ("C — минимализм", "feature_C_minimal", variant_minimal)]


def main():
    made = []
    for title, key, fn in VARIANTS:
        img = fn().convert("RGB").resize((W, H), Image.LANCZOS)
        p = os.path.join(OUT, f"{key}_1024x500.png")
        img.save(p)
        made.append((title, img))
        print(f"{title}: {p}  {img.size}  {img.mode}")

    pad, lab = 24, 40
    sheet = Image.new("RGB", (W + pad * 2, (H + lab) * len(made) + pad), (0x14, 0x11, 0x1E))
    d = ImageDraw.Draw(sheet)
    for i, (title, img) in enumerate(made):
        y = pad // 2 + i * (H + lab)
        d.text((pad, y), title, font=font(24), fill=(0xFF, 0xFF, 0xFF))
        sheet.paste(img, (pad, y + lab - 8))
    sp = os.path.join(OUT, "feature_variants_sheet.png")
    sheet.save(sp)
    print("лист:", sp, sheet.size)


if __name__ == "__main__":
    main()
