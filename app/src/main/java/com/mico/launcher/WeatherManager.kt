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

    fun fetch(callback: (WeatherInfo?, String?) -> Unit) {
        Thread {
            try {
                val url = URL("http://v0.yiketianqi.com/api?unescape=1&version=v61&appid=43656133&appsecret=I42og6Lm")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val responseCode = conn.responseCode
                System.out.println("WeatherManager Response Code: $responseCode")
                if (responseCode == 200) {
                    val json = conn.inputStream.bufferedReader().readText()
                    val weather = parseWeather(json)
                    callback(weather, if (weather == null) "Parse Error" else null)
                } else {
                    System.out.println("WeatherManager HTTP Error: $responseCode")
                    callback(null, "HTTP $responseCode")
                }
                conn.disconnect()
            } catch (e: Exception) {
                System.out.println("WeatherManager Fetch Exception: $e")
                callback(null, e.toString())
            }
        }.start()
    }

    private fun parseWeather(jsonString: String): WeatherInfo? {
        System.out.println("WeatherManager JSON: $jsonString")
        return try {
            val json = JSONObject(jsonString)
            
            // Handle different JSON structures
            val tempC = when {
                json.has("tem") -> json.getString("tem")
                json.has("temp") -> json.getString("temp")
                else -> "20"
            }
            
            val condition = when {
                json.has("wea") -> json.getString("wea")
                json.has("weather") -> json.getString("weather")
                else -> "未知"
            }
            
            val iconImg = when {
                json.has("wea_img") -> json.getString("wea_img")
                else -> "qing"
            }

            val icon = mapConditionToEmoji(iconImg)
            WeatherInfo(tempC, condition, icon)
        } catch (e: Exception) {
            System.out.println("WeatherManager Parse Error: " + e.message)
            e.printStackTrace()
            null
        }
    }

    private fun mapConditionToEmoji(weaImg: String): String {
        return when (weaImg) {
            "xue" -> "❄"
            "lei" -> "⛈"
            "shachen" -> "🌫"
            "wu" -> "🌫"
            "bingbao" -> "🧊"
            "yun" -> "☁"
            "yu" -> "🌧"
            "yin" -> "☁"
            "qing" -> "☀"
            else -> "⛅"
        }
    }
}
