#!/bin/bash
# تشغيل المحاكي في Codespaces + عرضه في المتصفح (المنفذ 6080)
set -e

export ANDROID_HOME="$HOME/android-sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"

# إيقاف أي تشغيل سابق
pkill -f "emulator -avd" 2>/dev/null || true
pkill Xvfb 2>/dev/null || true
pkill x11vnc 2>/dev/null || true
pkill websockify 2>/dev/null || true
sleep 1

# تسريع فقط إذا توفر KVM
ACCEL=""
if [ -e /dev/kvm ]; then
    ACCEL="-accel on"
    echo "⚡ KVM متاح - تسريع عتادي"
else
    ACCEL="-no-accel"
    echo "🐢 بدون KVM - محاكاة برمجية (أبطأ لكن يعمل)"
fi

# شاشة افتراضية + VNC + noVNC على المنفذ 6080
Xvfb :1 -screen 0 1080x1920x24 &
sleep 2
x11vnc -display :1 -forever -shared -quiet -bg -rfbport 5900
websockify --web /usr/share/novnc 6080 localhost:5900 > /tmp/websockify.log 2>&1 &
sleep 2

# تشغيل المحاكي على الشاشة الافتراضية
DISPLAY=:1 emulator -avd test -no-audio -no-boot-anim -gpu swiftshader_indirect $ACCEL -memory 2048 > /tmp/emulator.log 2>&1 &
echo "📱 المحاكي يقلع (قد يستغرق دقائق)..."
adb wait-for-device
echo "✅ الجهاز جاهز!"

echo ""
echo "=============================================="
echo " افتح تبويب PORTS بالأسفل، ثم اضغط 🌐 على المنفذ 6080"
echo " نقر يسار = لمس | نقر يمين = ضغطة طويلة | سحب = تمرير"
echo "=============================================="
echo ""
echo "لتثبيت التطبيق: cd /workspaces && ./gradlew assembleDebug"
echo "ثم: adb install app/build/outputs/apk/debug/app-debug.apk"
echo "ثم: adb shell am start -n com.university.app/.MainActivity"