package com.mico.launcher

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class GlobalInactivityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private val INACTIVITY_TIMEOUT = 60000L // 60秒
    
    private val returnToClockRunnable = Runnable {
        returnToHome()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 任何窗口变化或交互事件都重置计时器
        resetInactivityTimer()
    }

    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        resetInactivityTimer()
    }

    private fun resetInactivityTimer() {
        handler.removeCallbacks(returnToClockRunnable)
        
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("auto_return_clock", true)
        
        if (isEnabled) {
            handler.postDelayed(returnToClockRunnable, INACTIVITY_TIMEOUT)
        }
    }

    private fun returnToHome() {
        // 发送回到桌面的 Intent
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        
        // 发送广播或者其他方式通知 LauncherActivity 切换到时钟页
        val updateIntent = Intent("com.mico.launcher.ACTION_RETURN_CLOCK")
        sendBroadcast(updateIntent)
    }
}
