# -*- coding: utf-8 -*-
"""Заполнение базы приложения тегами и связями для съёмки скриншотов.

Использование:
    python seed_db.py ru      # теги по-русски
    python seed_db.py en      # те же теги по-английски (id не меняются)

Требует adb в PATH, подключённое устройство и `adb root`.

ВАЖНО про mediaId. Приложение хранит URI, выданный системным Photo Picker:

    content://media/picker/0/com.android.providers.media.photopicker/media/<MediaStore _id>

Читать файл по такому URI приложение может ТОЛЬКО при наличии гранта, а грант
выдаётся в момент выбора в пикере и становится постоянным лишь после того, как
пользователь нажал «Сохранить». Поэтому просто вписать строки в базу нельзя:
записи появятся, а картинки не загрузятся.

Порядок первого запуска на чистом устройстве:
  1. adb push фотографии в /sdcard/Pictures/<папка>
  2. медиа-скан:  adb shell content call --uri content://media --method scan_volume --arg external_primary
  3. узнать _id:  adb shell content query --uri content://media/external/images/media --projection _id:_display_name
  4. РУКАМИ в приложении: «Добавить» -> выбрать в пикере ВСЕ файлы -> любой тег -> «Сохранить».
     Это выдаёт постоянные гранты на все URI.
  5. дальше этот скрипт свободно перекладывает связи: гранты от содержимого таблицы не зависят.
"""
import subprocess
import sys

PKG = "com.dgopadakak.tagsgallery"
DB = f"/data/data/{PKG}/databases/tags_gallery_db"
URI = "content://media/picker/0/com.android.providers.media.photopicker/media/"

# id тега -> (русское имя, английское имя, цвет)
TAGS = {
    1:  ("Закат",       "Sunset",       "RED"),
    2:  ("Лес",         "Forest",       "GREEN"),
    3:  ("Горы",        "Mountains",    "BLUE"),
    4:  ("Город",       "City",         "YELLOW"),
    5:  ("Животные",    "Animals",      "NO_COLOR"),
    6:  ("Цветы",       "Flowers",      "RED"),
    7:  ("Вода",        "Water",        "BLUE"),
    8:  ("Небо",        "Sky",          "NO_COLOR"),
    9:  ("Архитектура", "Architecture", "YELLOW"),
    10: ("Лето",        "Summer",       "GREEN"),
}

# id тега -> MediaStore _id медиафайлов. Пересечения намеренные: без них
# карточка про режимы «Все / Любой / Исключить» показывала бы одинаковую выдачу.
LINKS = {
    1:  [172, 173, 174, 179, 180],
    2:  [185, 186, 187, 188, 189, 191],
    3:  [175, 180, 181, 183, 192],
    4:  [171, 176, 178],
    5:  [173, 174, 177, 182],
    6:  [171, 184, 190],
    7:  [175, 187, 188, 189, 191],
    8:  [172, 179, 180, 181],
    9:  [176, 178],
    10: [173, 174, 181, 182, 184, 185],
}

BASE_TIME = 1787900000000


def sql(locale):
    idx = 0 if locale == "ru" else 1
    tags = ",".join(f"({i},'{v[idx]}',{BASE_TIME - i * 7_200_000},'{v[2]}')"
                    for i, v in TAGS.items())
    links = ",".join(f"('{URI}{m}',{t})" for t, ms in LINKS.items() for m in ms)
    return (f"DELETE FROM MediaTagCrossRef;\nDELETE FROM Tag;\n"
            f"INSERT INTO Tag (id,name,lastModified,color) VALUES {tags};\n"
            f"INSERT INTO MediaTagCrossRef (mediaId,tagId) VALUES {links};\n")


def adb(*args, **kw):
    return subprocess.run(["adb", *args], capture_output=True, text=True, **kw)


def main():
    locale = sys.argv[1] if len(sys.argv) > 1 else "ru"
    if locale not in ("ru", "en"):
        sys.exit("укажите ru или en")

    covered = {m for ms in LINKS.values() for m in ms}
    print(f"тегов: {len(TAGS)}, медиа под тегами: {len(covered)}, связей: "
          f"{sum(len(v) for v in LINKS.values())}")

    with open("_seed.sql", "w", encoding="utf-8", newline="\n") as f:
        f.write(sql(locale))

    adb("root")
    adb("shell", f"am force-stop {PKG}")
    adb("push", "_seed.sql", "/data/local/tmp/seed.sql")
    r = adb("shell", f"sqlite3 {DB} < /data/local/tmp/seed.sql")
    if r.returncode or r.stderr.strip():
        sys.exit(f"sqlite3 вернул ошибку: {r.stderr.strip() or r.returncode}")

    check = adb("shell", f"sqlite3 {DB} "
                "'SELECT (SELECT COUNT(*) FROM Tag), (SELECT COUNT(*) FROM MediaTagCrossRef), "
                "(SELECT COUNT(DISTINCT mediaId) FROM MediaTagCrossRef);'")
    print("в базе (теги|связи|медиа):", check.stdout.strip())
    print(f"локаль приложения: adb shell cmd locale set-app-locales {PKG} --locales "
          f"{'ru-RU' if locale == 'ru' else 'en-US'}")


if __name__ == "__main__":
    main()
