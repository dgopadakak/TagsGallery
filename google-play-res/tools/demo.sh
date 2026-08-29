# Сброс и настройка demo mode. exit+enter обязателен: без него команда network
# добавляет ещё одну иконку Wi-Fi при каждом вызове, и они накапливаются.
demo() {
  D="am broadcast -a com.android.systemui.demo"
  adb shell "$D -e command exit" >/dev/null
  adb shell "$D -e command enter" >/dev/null
  adb shell "$D -e command clock -e hhmm 0941" >/dev/null
  adb shell "$D -e command battery -e level 100 -e plugged false" >/dev/null
  adb shell "$D -e command network -e wifi show -e level 4 -e fully true" >/dev/null
  adb shell "$D -e command network -e mobile hide" >/dev/null
  adb shell "$D -e command notifications -e visible false" >/dev/null
}
shoot() { adb shell screencap -p /data/local/tmp/s.png; adb pull /data/local/tmp/s.png "$1" >/dev/null 2>&1; }
prev() { "$(dirname "$0")/venv/Scripts/python.exe" -c "
from PIL import Image; import sys
Image.open(sys.argv[1]).resize((360,800),Image.LANCZOS).save('_p.png')" "$1"; }
