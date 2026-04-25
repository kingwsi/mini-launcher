#!/bin/bash

# Configuration
DEVICE_IP="192.168.123.46"
PACKAGE_NAME="com.mico.launcher"
ACTIVITY_NAME=".LauncherActivity"
APK_PATH="app/build/outputs/apk/debug/minLauncher-debug.apk"

echo "--- Building APK ---"
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "--- Connecting to Device ($DEVICE_IP) ---"
adb connect $DEVICE_IP

echo "--- Installing APK ---"
adb -s $DEVICE_IP:5555 install -r $APK_PATH

if [ $? -ne 0 ]; then
    echo "Install failed!"
    exit 1
fi

echo "--- Starting Activity ---"
adb -s $DEVICE_IP:5555 shell am start -n $PACKAGE_NAME/$ACTIVITY_NAME

echo "--- Done ---"
