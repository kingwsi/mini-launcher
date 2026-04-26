package com.mico.launcher

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected val inactivityHandler = Handler(Looper.getMainLooper())
    protected val INACTIVITY_TIMEOUT = 60000L
    
    private val inactivityRunnable = Runnable {
        onInactivityTimeout()
    }

    override fun onResume() {
        super.onResume()
        checkTimeoutOnResume()
        resetInactivityTimer()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetInactivityTimer()
    }

    private fun checkTimeoutOnResume() {
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("auto_return_clock", true)
        if (!isEnabled) return

        val now = System.currentTimeMillis()
        val lastInteraction = prefs.getLong("last_interaction_time", now)
        if (now - lastInteraction > INACTIVITY_TIMEOUT) {
            onInactivityTimeout()
        }
    }

    protected fun resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable)
        
        val prefs = getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("auto_return_clock", true)
        
        // 更新全局最后操作时间
        prefs.edit().putLong("last_interaction_time", System.currentTimeMillis()).apply()
        
        if (isEnabled) {
            inactivityHandler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT)
        }
    }

    override fun onPause() {
        super.onPause()
        inactivityHandler.removeCallbacks(inactivityRunnable)
    }

    /**
     * 子类需实现此方法以定义超时后的行为
     */
    abstract fun onInactivityTimeout()
}
