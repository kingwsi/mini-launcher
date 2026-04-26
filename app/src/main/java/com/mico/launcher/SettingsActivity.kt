package com.mico.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_settings)

        setupDebloat()
        setupOtherSettings()
    }

    private fun setupDebloat() {
        // Removed
    }

    private fun setupOtherSettings() {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        
        // Weather switch
        val btnWeather = findViewById<View>(R.id.btnAutoWeather)
        val swWeather = findViewById<Switch>(R.id.switchAutoWeather)
        swWeather.isChecked = prefs.getBoolean("auto_weather", true)
        
        btnWeather.setOnClickListener {
            swWeather.isChecked = !swWeather.isChecked
            prefs.edit().putBoolean("auto_weather", swWeather.isChecked).apply()
        }

        // Auto return clock switch
        val btnClock = findViewById<View>(R.id.btnAutoReturnClock)
        val swClock = findViewById<Switch>(R.id.switchAutoReturnClock)
        swClock.isChecked = prefs.getBoolean("auto_return_clock", true)

        btnClock.setOnClickListener {
            swClock.isChecked = !swClock.isChecked
            prefs.edit().putBoolean("auto_return_clock", swClock.isChecked).apply()
        }
    }

    private fun isPackageDisabled(packageName: String): Boolean {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            !info.enabled
        } catch (e: Exception) {
            true // 没找到包则认为已禁用
        }
    }

    private fun checkProcessRunning(processName: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ps")
            val output = process.inputStream.bufferedReader().readText()
            output.contains(processName)
        } catch (e: Exception) {
            false
        }
    }
}
