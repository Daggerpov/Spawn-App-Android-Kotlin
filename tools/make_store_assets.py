#!/usr/bin/env python3
"""Build Google Play listing graphics from raw emulator screenshots.

Usage:  python3 tools/make_store_assets.py <raw_screenshots_dir>

<raw_screenshots_dir> holds full-resolution `adb exec-out screencap -p` PNGs named
1_home.png, 2_map.png, 3_create.png, 4_friends.png, 5_profile.png, 6_activity.png.

Writes to Spawn-Android/store/:
  phone/<n>_<name>.png   1080x1920 (9:16) captioned screenshots, which Play accepts for phones
  feature_graphic.png    1024x500
  icon_512.png           512x512 app icon (copied from the launcher icon source)
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
APP = os.path.join(ROOT, "Spawn-Android")
FONTS = os.path.join(APP, "app/src/main/res/font")
OUT = os.path.join(APP, "store")

INDIGO_TOP = (122, 140, 255)
INDIGO_BOTTOM = (64, 84, 222)

CAPTIONS = {
    "1_home": "See what your friends\nare up to right now",
    "2_map": "Find spontaneous\nplans nearby",
    "3_create": "Spawn an activity\nin seconds",
    "4_friends": "Bring your\npeople along",
    "5_profile": "Every spawn,\nall in one place",
    "6_activity": "Jump in with\none tap",
}


def font(weight, size):
    return ImageFont.truetype(os.path.join(FONTS, f"onest_{weight}.ttf"), size)


def gradient(w, h, top=INDIGO_TOP, bottom=INDIGO_BOTTOM):
    img = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(1, h - 1)
        d.line([(0, y), (w, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    return img


def rounded(img, radius):
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, img.width - 1, img.height - 1], radius, fill=255)
    out = Image.new("RGBA", img.size)
    out.paste(img, (0, 0), mask)
    return out


def phone_shot(raw_path, caption):
    W, H = 1080, 1920
    canvas = gradient(W, H).convert("RGBA")
    d = ImageDraw.Draw(canvas)
    f = font("bold", 76)
    y = 110
    for line in caption.split("\n"):
        tw = d.textlength(line, font=f)
        d.text(((W - tw) / 2, y), line, font=f, fill="white")
        y += 92

    shot = Image.open(raw_path).convert("RGB")
    target_h = H - y - 40
    scale = target_h / shot.height
    shot = shot.resize((int(shot.width * scale), target_h), Image.LANCZOS)
    shot = rounded(shot, 48)
    x = (W - shot.width) // 2
    top = y + 60
    # soft shadow
    shadow = Image.new("RGBA", (shot.width + 80, shot.height + 80), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle([40, 40, shot.width + 40, shot.height + 40], 48, fill=(20, 24, 80, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(24))
    canvas.alpha_composite(shadow, (x - 40, top - 28))
    canvas.alpha_composite(shot, (x, top))
    return canvas.convert("RGB")


def feature_graphic(icon_path):
    W, H = 1024, 500
    g = gradient(W, H).convert("RGBA")
    d = ImageDraw.Draw(g)
    icon = Image.open(icon_path).convert("RGBA").resize((220, 220), Image.LANCZOS)
    icon = rounded(icon, 48)
    g.alpha_composite(icon, (96, (H - 220) // 2))
    d.text((360, 150), "Spawn", font=font("bold", 110), fill="white")
    d.text((364, 290), "Spontaneity made easy.", font=font("medium", 44), fill=(235, 238, 253))
    return g.convert("RGB")


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    raw = sys.argv[1]
    os.makedirs(os.path.join(OUT, "phone"), exist_ok=True)
    for name, caption in CAPTIONS.items():
        src = os.path.join(raw, name + ".png")
        if not os.path.exists(src):
            print("skip (missing):", src)
            continue
        phone_shot(src, caption).save(os.path.join(OUT, "phone", name + ".png"), optimize=True)
        print("wrote phone/" + name + ".png")
    icon_src = os.path.join(APP, "store", "icon_512.png")
    if os.path.exists(icon_src):
        feature_graphic(icon_src).save(os.path.join(OUT, "feature_graphic.png"), optimize=True)
        print("wrote feature_graphic.png")
    else:
        print("put the 512x512 icon at", icon_src, "to build the feature graphic")


if __name__ == "__main__":
    main()
