#!/bin/bash
# إعداد Android SDK + المحاكي + noVNC داخل Codespaces (JDK 17 مثبت عبر devcontainer features)
set -e

export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

if [ -d "$ANDROID_HOME/platforms" ]; then
    echo "Android SDK موجود مسبقاً"
else
    mkdir -p "$ANDROID_HOME/cmdline-tools"
    cd /tmp

    for URL in \
        "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" \
        "https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"; do
        echo "تحميل أدوات سطر الأوامر: $URL"
        if curl -fsSL -o cmdtools.zip "$URL"; then
            break
        fi
    done

    unzip -q cmdtools.zip -d "$ANDROID_HOME/cmdline-tools"
    mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"

    yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null || true
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" \
        "platforms;android-34" \
        "build-tools;34.0.0" \
        "platform-tools" \
        "emulator" \
        "system-images;android-34;google_apis;x86_64"
fi

# أدوات العرض: شاشة افتراضية + VNC + noVNC (لرؤية المحاكي في المتصفح)
if ! command -v Xvfb > /dev/null; then
    sudo apt-get update -qq
    sudo apt-get install -y -qq xvfb x11vnc novnc websockify
fi

# إنشاء جهاز افتراضي (AVD) إن لم يوجد
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
if ! avdmanager list avd 2>/dev/null | grep -q "Name: test"; then
    echo no | avdmanager create avd -n test -k "system-images;android-34;google_apis;x86_64" -d pixel_5 > /dev/null
    echo "✅ تم إنشاء الجهاز الافتراضي 'test'"
fi

grep -q "ANDROID_HOME" ~/.bashrc || cat >> ~/.bashrc <<'EOF'
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
EOF

echo "✅ جاهز: SDK + محاكي + noVNC في $ANDROID_HOME"