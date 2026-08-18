#!/bin/bash
# إعداد Android SDK داخل Codespaces (JDK 17 مثبت مسبقاً عبر devcontainer features)
set -e

export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

if [ -d "$ANDROID_HOME/platforms" ]; then
    echo "Android SDK موجود مسبقاً"
    exit 0
fi

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
    "platform-tools"

# المحاكي فقط إذا كانت الافتراضية المتداخلة متاحة (تسريع KVM)
if [ -e /dev/kvm ]; then
    "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "emulator" "system-images;android-34;google_apis;x86_64" || true
fi

grep -q "ANDROID_HOME" ~/.bashrc || cat >> ~/.bashrc <<'EOF'
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
EOF

echo "✅ Android SDK جاهز في $ANDROID_HOME"