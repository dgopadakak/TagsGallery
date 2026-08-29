"""Карточки скриншотов 1080x1920 для Google Play в фирменном стиле витрины."""
import os

from PIL import Image, ImageDraw, ImageFilter

import render_feature as F

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.join(HERE, "raw")      # исходные скриншоты с устройства
OUT = HERE                            # совместимость с остальными скриптами


def raw(path):
    """Имя без каталога ищется в raw/."""
    return path if os.path.isabs(path) or os.path.dirname(path) else os.path.join(RAW, path)


def out_path(name):
    """Путь назначения считается от каталога скрипта; каталоги создаются."""
    q = os.path.normpath(os.path.join(HERE, name))
    os.makedirs(os.path.dirname(q), exist_ok=True)
    return q
W, H = 1080, 1920
BG_TOP = (0x5B, 0x3F, 0xA8)
BG_BOT = (0x2E, 0x1B, 0x58)

MARGIN = 72
CAP_TOP = 80            # базовая линия первой строки = CAP_TOP + ascent; верх прописных попадает на 104
CAP_SIZE = 62
PHONE_W = 812           # ширина панели со скриншотом
PHONE_TOP_GAP = 58      # зазор от подписи до панели
RADIUS = 46


def gradient(w, h):
    g = Image.new("RGB", (256, 256))
    px = g.load()
    for y in range(256):
        for x in range(256):
            t = (x / 255 * 0.3 + y / 255 * 0.7)
            px[x, y] = tuple(round(BG_TOP[i] + (BG_BOT[i] - BG_TOP[i]) * t) for i in range(3))
    return g.resize((w, h), Image.BICUBIC).convert("RGBA")


def wrap(draw, text, font, max_w):
    words, lines, cur = text.split(), [], ""
    for w_ in words:
        t = (cur + " " + w_).strip()
        if draw.textlength(t, font=font) <= max_w or not cur:
            cur = t
        else:
            lines.append(cur)
            cur = w_
    if cur:
        lines.append(cur)
    return lines


def rounded_mask(size, radius):
    ss = 4
    m = Image.new("L", (size[0] * ss, size[1] * ss), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0] * ss - 1, size[1] * ss - 1],
                                        radius=radius * ss, fill=255)
    return m.resize(size, Image.LANCZOS)


LEADING = 14        # добавка к высоте строки, интерлиньяж


def draw_caption(d, caption, font, colour=(255, 255, 255, 255)):
    """Строки ставятся по базовым линиям с постоянным шагом.

    Раньше шаг считался от низа чернил предыдущей строки до верха чернил
    следующей, поэтому просвет гулял: «ё» поднимает верх, «р», «у», «д»
    опускают низ. Базовая линия от состава букв не зависит.
    """
    lines = wrap(d, caption, font, W - 2 * MARGIN)
    asc, desc = font.getmetrics()
    step = asc + desc + LEADING
    base = CAP_TOP + asc
    for ln in lines:
        d.text((W // 2, base), ln, font=font, fill=colour, anchor="ms")
        base += step
    return lines, base - step + desc


def build(shot_path, caption, out_name, crop_to=None, phone_w=PHONE_W, center=False, fit=False):
    canvas = gradient(W, H)
    d = ImageDraw.Draw(canvas)
    fcap = F.font(CAP_SIZE)

    lines, bottom = draw_caption(d, caption, fcap)

    # --- панель со скриншотом, свисающая за нижний край ---
    shot = Image.open(raw(shot_path)).convert("RGB")
    if crop_to:
        shot = shot.crop((0, 0, shot.width, crop_to))
    if fit:
        # ширина подбирается так, чтобы весь кадр поместился по высоте
        avail = (H - MARGIN) - (bottom + PHONE_TOP_GAP)
        phone_w = int(min(phone_w, avail * shot.width / shot.height))
    ph = round(phone_w * shot.height / shot.width)
    shot = shot.resize((phone_w, ph), Image.LANCZOS)
    px = (W - phone_w) // 2
    py = bottom + PHONE_TOP_GAP
    if center:
        free_top, free_bot = bottom + PHONE_TOP_GAP, H - MARGIN
        py = free_top + max(0, (free_bot - free_top - ph) // 2)

    shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [px, py + 14, px + phone_w, min(py + ph, H) + 14], radius=RADIUS, fill=(0, 0, 0, 150))
    canvas = Image.alpha_composite(canvas, shadow.filter(ImageFilter.GaussianBlur(26)))

    panel = shot.convert("RGBA")
    panel.putalpha(rounded_mask((phone_w, ph), RADIUS))
    canvas.alpha_composite(panel, (px, py))

    # тонкая светлая обводка панели
    ring = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(ring).rounded_rectangle([px, py, px + phone_w - 1, py + ph - 1],
                                           radius=RADIUS, outline=(255, 255, 255, 64), width=3)
    canvas = Image.alpha_composite(canvas, ring)

    img = canvas.convert("RGB")
    p = out_path(out_name)
    img.save(p)
    print(f"{out_name}: {img.size} {img.mode} | подпись в {len(lines)} стр., низ на y={bottom}, панель с y={py}")
    return img


if __name__ == "__main__":
    cap = "Выбираете тег — остаётся только нужное"
    a = build("shot_gallery_sunset.png", cap, "card_A.png")
    b = build("shot_gallery_sunset.png", cap, "card_B.png", crop_to=1560, phone_w=860, center=True)
    c = build("shot_gallery_sunset.png", cap, "card_C.png", crop_to=1560, phone_w=1010, center=True)
    from PIL import Image as I
    sh = I.new("RGB", (3 * 360 + 40, 640 + 40), (0x14, 0x11, 0x1E))
    d2 = ImageDraw.Draw(sh)
    for i, (nm, im) in enumerate((("A - как было", a), ("B - обрезка 860", b), ("C - обрезка 1010", c))):
        t = im.resize((360, 640), I.LANCZOS)
        sh.paste(t, (10 + i * 360, 34))
        d2.text((12 + i * 360, 8), nm, font=F.font(20), fill=(255, 255, 255))
    sh.save("cards_compare.png"); print("сравнение:", sh.size)
