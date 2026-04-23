package com.mico.launcher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import java.util.*

class LauncherActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    
    // Page 1 views (Clock)
    private lateinit var clockTime: TextView
    private lateinit var dateText: TextView
    private lateinit var weatherIcon: TextView
    private lateinit var weatherTemp: TextView
    private lateinit var weatherGroup: LinearLayout

    // Page 2 views (Apps)
    private lateinit var appsRecyclerView: RecyclerView

    private val handler = Handler(Looper.getMainLooper())
    private var lastMinute = -1

    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1000)
        }
    }

    private val weatherRunnable = object : Runnable {
        override fun run() {
            fetchWeather()
            handler.postDelayed(this, 30 * 60 * 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive fullscreen
        setupImmersiveMode()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_launcher)

        viewPager = findViewById(R.id.viewPager)

        val inflater = LayoutInflater.from(this)
        val pageClock = inflater.inflate(R.layout.page_clock, null)
        val pageApps = inflater.inflate(R.layout.page_apps, null)

        // Initialize Clock Page Views
        clockTime = pageClock.findViewById(R.id.clockTime)
        dateText = pageClock.findViewById(R.id.dateText)
        weatherIcon = pageClock.findViewById(R.id.weatherIcon)
        weatherTemp = pageClock.findViewById(R.id.weatherTemp)
        weatherGroup = pageClock.findViewById(R.id.weatherGroup)

        // Initialize App Page Views
        appsRecyclerView = pageApps.findViewById(R.id.appsRecyclerView)
        setupAppsList()

        val pages = listOf(pageClock, pageApps)
        viewPager.adapter = MainPagerAdapter(pages)

        handler.post(clockRunnable)
        handler.post(weatherRunnable)
    }

    private fun setupAppsList() {
        val apps = getInstalledApps()
        appsRecyclerView.layoutManager = GridLayoutManager(this, 5) // 5 columns for landscape
        appsRecyclerView.adapter = AppAdapter(apps) { app ->
            val intent = Intent().apply {
                component = ComponentName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val appList = mutableListOf<AppInfo>()

        for (info in resolveInfos) {
            appList.add(
                AppInfo(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(pm),
                    className = info.activityInfo.name
                )
            )
        }
        
        // Sort alphabetically
        appList.sortBy { it.label.lowercase(Locale.ROOT) }
        return appList
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupImmersiveMode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clockRunnable)
        handler.removeCallbacks(weatherRunnable)
    }

    override fun onBackPressed() {
        // Return to first page if on apps page
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = 0
        }
    }

    private fun setupImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    private fun updateClock() {
        val now = Calendar.getInstance()
        val hours = now.get(Calendar.HOUR_OF_DAY)
        val minutes = now.get(Calendar.MINUTE)

        clockTime.text = String.format("%02d:%02d", hours, minutes)

        if (minutes != lastMinute) {
            lastMinute = minutes
            updateDate(now)
        }
    }

    private fun updateDate(calendar: Calendar) {
        val weekDays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        val dayOfWeek = weekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        dateText.text = "$dayOfWeek  ${month}月${day}日"
    }

    private fun fetchWeather() {
        WeatherManager.fetch { weatherInfo ->
            handler.post {
                if (weatherInfo != null) {
                    weatherIcon.text = weatherInfo.icon
                    weatherTemp.text = "${weatherInfo.tempC}°"
                    weatherGroup.visibility = View.VISIBLE
                }
            }
        }
    }
}
