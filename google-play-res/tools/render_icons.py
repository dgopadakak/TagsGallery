"""Рендер вариантов иконки TagsGallery из геометрии оригинальных vector drawable."""
import math
import os

from PIL import Image, ImageChops, ImageDraw, ImageFont

VP = 108           # viewport адаптивной иконки в dp
S = 16             # supersampling: рабочий холст 1728x1728
OUT = os.path.dirname(os.path.abspath(__file__))

BG_TOP = (0x5B, 0x3F, 0xA8)
BG_BOT = (0x3B, 0x24, 0x70)
CARD_BACK = (0xCF, 0xC4, 0xEC)
CARD_BACK_DARK = (0xA9, 0x99, 0xD4)   # затемнённая дальняя карточка для F4
CARD_MID = (0xE5, 0xDE, 0xF7)
CARD_FRONT = (0xFF, 0xFF, 0xFF)
MOUNTAIN = (0x66, 0x50, 0xA4)
ACCENT = (0xFF, 0xB0, 0x20)

CARD = (29.0, 32.0, 71.0, 64.0)   # x0, y0, x1, y1
CARD_R = 4.0
PIVOT = (50.0, 48.0)              # ось веера карточек
TAG_PIVOT = (66.0, 66.0)
TAG_ROT = 225.0


# ---------- геометрия ----------

def rot(pts, cx, cy, deg):
    """Поворот по часовой стрелке, как android:rotation (ось Y вниз)."""
    a = math.radians(deg)
    ca, sa = math.cos(a), math.sin(a)
    return [(cx + (x - cx) * ca - (y - cy) * sa,
             cy + (x - cx) * sa + (y - cy) * ca) for x, y in pts]


def translate(pts, dx, dy):
    return [(x + dx, y + dy) for x, y in pts]


def rrect(x0, y0, x1, y1, r, n=28):
    pts = []
    for cx, cy, a0, a1 in ((x0 + r, y0 + r, 180, 270), (x1 - r, y0 + r, 270, 360),
                           (x1 - r, y1 - r, 0, 90), (x0 + r, y1 - r, 90, 180)):
        for i in range(n + 1):
            a = math.radians(a0 + (a1 - a0) * i / n)
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return pts


def quad(p0, p1, p2, n=18):
    out = []
    for i in range(n + 1):
        t = i / n
        u = 1 - t
        out.append((u * u * p0[0] + 2 * u * t * p1[0] + t * t * p2[0],
                    u * u * p0[1] + 2 * u * t * p1[1] + t * t * p2[1]))
    return out


def circle_pts(cx, cy, r, n=64):
    return [(cx + r * math.cos(2 * math.pi * i / n),
             cy + r * math.sin(2 * math.pi * i / n)) for i in range(n)]


def tag_outline():
    """Ярлык из ic_launcher_foreground.xml, до поворота."""
    pts = [(56.4, 57.0), (69.2, 57.0), (78.0, 66.0), (69.2, 75.0), (56.4, 75.0)]
    pts += quad((56.4, 75.0), (54.0, 75.0), (54.0, 72.6))
    pts += [(54.0, 59.4)]
    pts += quad((54.0, 59.4), (54.0, 57.0), (56.4, 57.0))
    return pts


# ---------- сборка варианта ----------

def build(cfg):
    fan = cfg["fan"]
    back_col = CARD_BACK_DARK if cfg["back_dark"] else CARD_BACK

    card = rrect(*CARD, CARD_R)
    cards = [
        (rot(card, *PIVOT, fan[0]), back_col),
        (rot(card, *PIVOT, fan[1]), CARD_MID),
        (card, CARD_FRONT),
    ]

    if cfg["simple_landscape"]:
        mountains = [(29.0, 64.0), (46.0, 40.0), (71.0, 64.0)]
        sun = (62.0, 38.5, 5.0)
    else:
        mountains = [(29.0, 64.0), (43.0, 42.0), (53.0, 55.0), (60.0, 46.0), (71.0, 64.0)]
        sun = (62.0, 40.0, 3.8)

    tag = rot(tag_outline(), *TAG_PIVOT, TAG_ROT)
    if cfg["hole"] == "none":
        hole = None
    else:
        r = 3.4 if cfg["hole"] == "big" else 2.6
        hc = rot([(70.8, 66.0)], *TAG_PIVOT, TAG_ROT)[0]
        hole = (hc[0], hc[1], r)

    geom = dict(cards=cards, mountains=mountains, sun=sun, tag=tag, hole=hole,
                clip=card)

    if cfg["recenter"]:
        allp = [p for c, _ in cards for p in c] + list(tag)
        xs = [p[0] for p in allp]
        ys = [p[1] for p in allp]
        cx = (min(xs) + max(xs)) / 2
        cy = (min(ys) + max(ys)) / 2
        dx, dy = 54.0 - cx, 54.0 - cy
        geom["cards"] = [(translate(c, dx, dy), col) for c, col in cards]
        geom["mountains"] = translate(mountains, dx, dy)
        geom["sun"] = (sun[0] + dx, sun[1] + dy, sun[2])
        geom["tag"] = translate(tag, dx, dy)
        geom["clip"] = translate(card, dx, dy)
        if hole:
            geom["hole"] = (hole[0] + dx, hole[1] + dy, hole[2])
        geom["offset"] = (dx, dy)
    else:
        geom["offset"] = (0.0, 0.0)

    k = cfg.get("scale", 1.0)
    if k != 1.0:
        def z(pts):
            return [(54.0 + (x - 54.0) * k, 54.0 + (y - 54.0) * k) for x, y in pts]
        geom["cards"] = [(z(c), col) for c, col in geom["cards"]]
        geom["mountains"] = z(geom["mountains"])
        sx, sy, sr = geom["sun"]
        geom["sun"] = (54.0 + (sx - 54.0) * k, 54.0 + (sy - 54.0) * k, sr * k)
        geom["tag"] = z(geom["tag"])
        geom["clip"] = z(geom["clip"])
        if geom["hole"]:
            hx, hy, hr = geom["hole"]
            geom["hole"] = (54.0 + (hx - 54.0) * k, 54.0 + (hy - 54.0) * k, hr * k)
    return geom


def metrics(cfg):
    """Габариты содержимого и максимальный радиус от центра (54,54)."""
    g = build(cfg)
    pts = [p for c, _ in g["cards"] for p in c] + list(g["tag"])
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    rmax = max(math.hypot(x - 54.0, y - 54.0) for x, y in pts)
    return dict(x0=min(xs), x1=max(xs), y0=min(ys), y1=max(ys), rmax=rmax)


# ---------- растеризация ----------

def sc(pts):
    return [(x * S, y * S) for x, y in pts]


def gradient_bg(n):
    g = Image.new("RGB", (256, 256))
    p = g.load()
    for y in range(256):
        for x in range(256):
            t = (x + y) / (2 * 255)
            p[x, y] = (round(BG_TOP[0] + (BG_BOT[0] - BG_TOP[0]) * t),
                       round(BG_TOP[1] + (BG_BOT[1] - BG_TOP[1]) * t),
                       round(BG_TOP[2] + (BG_BOT[2] - BG_TOP[2]) * t))
    return g.resize((n, n), Image.BICUBIC)


def render(cfg, size):
    n = VP * S
    base = gradient_bg(n).convert("RGBA")
    g = build(cfg)

    d = ImageDraw.Draw(base)
    for pts, col in g["cards"][:2]:
        d.polygon(sc(pts), fill=col + (255,))

    # передняя карточка с пейзажем, обрезанным по её форме
    card_layer = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    cd = ImageDraw.Draw(card_layer)
    cd.polygon(sc(g["cards"][2][0]), fill=CARD_FRONT + (255,))
    cd.polygon(sc(g["mountains"]), fill=MOUNTAIN + (255,))
    sx, sy, sr = g["sun"]
    cd.polygon(sc(circle_pts(sx, sy, sr)), fill=ACCENT + (255,))
    mask = Image.new("L", (n, n), 0)
    ImageDraw.Draw(mask).polygon(sc(g["clip"]), fill=255)
    card_layer.putalpha(ImageChops.multiply(card_layer.getchannel("A"), mask))
    base = Image.alpha_composite(base, card_layer)

    # ярлык с прорезанной дыркой
    tag_layer = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    td = ImageDraw.Draw(tag_layer)
    td.polygon(sc(g["tag"]), fill=ACCENT + (255,))
    if g["hole"]:
        hx, hy, hr = g["hole"]
        td.polygon(sc(circle_pts(hx, hy, hr)), fill=(0, 0, 0, 0))
    base = Image.alpha_composite(base, tag_layer)

    return base.convert("RGB").resize((size, size), Image.LANCZOS), g["offset"]


def safezone(img):
    """Центральные 72 из 108 dp — всё, что лаунчер и Play вообще показывают."""
    k = img.size[0] / float(VP)
    return img.crop((round(18 * k), round(18 * k), round(90 * k), round(90 * k)))


def masked(img, size):
    """Как иконку обрежет лаунчер: круглая маска, наложенная на безопасную зону."""
    full = safezone(img).resize((size, size), Image.LANCZOS)
    m = Image.new("L", (size * 4, size * 4), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size * 4 - 1, size * 4 - 1], fill=255)
    m = m.resize((size, size), Image.LANCZOS)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(full, (0, 0), m)
    return out


VARIANTS = [
    dict(key="v0", title="V0 — как сейчас",
         note="базовый, ничего не менялось",
         recenter=False, simple_landscape=False, hole="small", fan=(-16, -8), back_dark=False),
    dict(key="v1", title="V1 — только центровка",
         note="F2",
         recenter=True, simple_landscape=False, hole="small", fan=(-16, -8), back_dark=False),
    dict(key="v2", title="V2 — центровка + пейзаж",
         note="F1+F2",
         recenter=True, simple_landscape=True, hole="small", fan=(-16, -8), back_dark=False),
    dict(key="v3", title="V3 — + дырка крупнее",
         note="F1+F2+F3",
         recenter=True, simple_landscape=True, hole="big", fan=(-16, -8), back_dark=False),
    dict(key="v4", title="V4 — все четыре",
         note="F1+F2+F3+F4",
         recenter=True, simple_landscape=True, hole="big", fan=(-20, -10), back_dark=True),
    dict(key="v5", title="V5 — все, дырка убрана",
         note="F1+F2+F4, без дырки",
         recenter=True, simple_landscape=True, hole="none", fan=(-20, -10), back_dark=True),
    dict(key="v6", title="V6 — центровка + веер",
         note="F2+F4, пейзаж не тронут",
         recenter=True, simple_landscape=False, hole="small", fan=(-20, -10), back_dark=True),
]


def font(sz):
    for p in (r"C:\Windows\Fonts\segoeui.ttf", r"C:\Windows\Fonts\arial.ttf"):
        if os.path.exists(p):
            return ImageFont.truetype(p, sz)
    return ImageFont.load_default()


def main():
    big, small_masked = {}, {}
    for cfg in VARIANTS:
        img, off = render(cfg, 1080)
        play = safezone(img).resize((512, 512), Image.LANCZOS)
        play.save(os.path.join(OUT, f"icon_{cfg['key']}_512.png"))
        big[cfg["key"]] = play.resize((240, 240), Image.LANCZOS)
        tiny = masked(img, 48)
        small_masked[cfg["key"]] = tiny.resize((144, 144), Image.NEAREST)
        print(f"{cfg['key']}: сдвиг центровки dx={off[0]:+.2f} dy={off[1]:+.2f}")

    cols = len(VARIANTS)
    cw, pad = 240, 28
    header, gap, labelh = 54, 20, 64
    W = pad + cols * (cw + pad)
    H = header + 240 + gap + 144 + labelh + pad
    sheet = Image.new("RGB", (W, H), (0x1A, 0x16, 0x24))
    d = ImageDraw.Draw(sheet)
    d.text((pad, 16), "TagsGallery — варианты иконки. Сверху 512 px как в Play (безопасная зона 72 dp), снизу 48 px под маской лаунчера, увеличено ×3 без сглаживания",
           font=font(19), fill=(0xE8, 0xE2, 0xF5))

    for i, cfg in enumerate(VARIANTS):
        x = pad + i * (cw + pad)
        sheet.paste(big[cfg["key"]], (x, header))
        tm = small_masked[cfg["key"]]
        sheet.paste(tm, (x + (cw - 144) // 2, header + 240 + gap), tm)
        ty = header + 240 + gap + 144 + 10
        d.text((x, ty), cfg["title"], font=font(18), fill=(0xFF, 0xFF, 0xFF))
        d.text((x, ty + 24), cfg["note"], font=font(16), fill=(0xB9, 0xAE, 0xD2))

    path = os.path.join(OUT, "icon_variants_sheet.png")
    sheet.save(path)
    print("контактный лист:", path, sheet.size)


if __name__ == "__main__":
    main()
