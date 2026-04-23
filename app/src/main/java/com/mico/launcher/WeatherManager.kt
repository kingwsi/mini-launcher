package com.mico.launcher

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight weather manager using wttr.in free API (no API key required).
 * Caches weather data for 30 minutes to minimize network usage.
 */
object WeatherManager {

    data class WeatherInfo(
        val tempC: String,
        val condition: String,
        val icon: String
    )

    private var cachedWeather: WeatherInfo? = null
    private var lastFetchTime = 0L
    private const val CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes

    fun fetch(callback: (WeatherInfo?) -> Unit) {
        // Return cached data if still fresh
        val now = System.currentTimeMillis()
        if (cachedWeather != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
            callback(cachedWeather)
            return
        }

        Thread {
            try {
                val url = URL("https://wttr.in/?format=j1")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
                conn.setRequestProperty("User-Agent", "MicoLauncher/1.0")
                conn.setRequestProperty("Accept-Language", "zh-CN")

                if (conn.responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    val weather = parseWeather(json)
                    if (weather != null) {
                        cachedWeather = weather
                        lastFetchTime = System.currentTimeMillis()
                    }
                    callback(weather)
                } else {
                    callback(cachedWeather) // Fall back to cache
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                callback(cachedWeather) // Fall back to cache on error
            }
        }.start()
    }

    private fun parseWeather(jsonString: String): WeatherInfo? {
        return try {
            val json = JSONObject(jsonString)
            val current = json.getJSONArray("current_condition").getJSONObject(0)
            val tempC = current.getString("temp_C")
            val weatherCode = current.optString("weatherCode", "113")

            // Try to get Chinese description, fall back to English
            val condition = try {
                current.getJSONArray("lang_zh").getJSONObject(0).getString("value")
            } catch (_: Exception) {
                current.getJSONArray("weatherDesc").getJSONObject(0).getString("value")
            }

            val icon = mapWeatherCodeToIcon(weatherCode.toIntOrNull() ?: 113)
            WeatherInfo(tempC, condition, icon)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Maps wttr.in weather codes to Unicode weather symbols.
     * See: https://www.worldweatheronline.com/developer/api/docs/weather-icons.aspx
     */
    private fun mapWeatherCodeToIcon(code: Int): String {
        return when (code) {
            113 -> "☀"          // Sunny / Clear
            116 -> "⛅"         // Partly cloudy
            119, 122 -> "☁"    // Cloudy / Overcast
            143, 248, 260 -> "🌫" // Fog / Mist
            176, 263, 266, 293, 296 -> "🌦" // Light rain
            299, 302, 305, 308, 356, 359 -> "🌧" // Heavy rain
            200, 386, 389, 392, 395 -> "⛈" // Thunder
            179, 182, 185, 227, 230, 323, 326, 329, 332, 335, 338, 350, 368, 371, 374, 377 -> "❄" // Snow
            else -> "☁"        // Default to cloudy
        }
    }
}
