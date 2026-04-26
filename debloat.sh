#!/bin/bash

DEVICE_IP="192.168.123.46"
PORT="5555"
TARGET="$DEVICE_IP:$PORT"

# 待精简的包名列表
PACKAGES=(
    "com.xiaomi.micolauncher"
    "com.xiaomi.micovoip"
    "com.xiaomi.mico.romupdate"
    "com.xiaomi.mico.mqtt.service.application"
)

# 待停止的底层服务
SERVICES=(
    "mivpm"
)

echo "--- 正在连接设备 $TARGET ---"
adb connect $TARGET

function debloat() {
    echo "--- 开始执行深度精简 ---"
    
    # 1. 处理底层 Native 服务
    for svc in "${SERVICES[@]}"; do
        echo "停止底层服务: $svc"
        adb -s $TARGET shell "stop $svc"
        adb -s $TARGET shell "setprop ctl.stop $svc"
    done

    # 2. 处理 APK 应用
    for pkg in "${PACKAGES[@]}"; do
        echo "禁用应用并停止进程: $pkg"
        adb -s $TARGET shell "pm disable-user $pkg"
        adb -s $TARGET shell "am force-stop $pkg"
    done

    echo "--- 精简完成！设备现在处于极简模式 ---"
}

function restore() {
    echo "--- 正在恢复原厂服务 ---"
    
    for pkg in "${PACKAGES[@]}"; do
        echo "启用应用: $pkg"
        adb -s $TARGET shell "pm enable $pkg"
    done

    for svc in "${SERVICES[@]}"; do
        echo "启动底层服务: $svc"
        adb -s $TARGET shell "start $svc"
    done

    echo "--- 恢复完成！系统已回到原厂状态 ---"
}

function setup_launcher() {
    echo "--- 正在为 minLauncher 开启无障碍服务权限 ---"
    adb -s $TARGET shell settings put secure enabled_accessibility_services com.mico.launcher/.GlobalInactivityService
    adb -s $TARGET shell settings put secure accessibility_enabled 1
    echo "--- 权限设置完成 ---"
}

if [ "$1" == "restore" ]; then
    restore
else
    debloat
    setup_launcher
fi
