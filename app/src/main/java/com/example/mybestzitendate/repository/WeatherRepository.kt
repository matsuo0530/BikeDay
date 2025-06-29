package com.example.mybestzitendate.repository

import android.util.Log
import com.example.mybestzitendate.data.BicycleWeatherInfo
import com.example.mybestzitendate.data.DailyData
import com.example.mybestzitendate.data.LocationSearchResult
import com.example.mybestzitendate.data.WeatherResponse
import com.example.mybestzitendate.network.NetworkModule
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository {
    private val weatherApiService = NetworkModule.weatherApiService
    private val geocodingApiService = NetworkModule.geocodingApiService
    
    suspend fun getBicycleWeatherForecast(lat: Double, lon: Double): List<BicycleWeatherInfo> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WeatherRepository", "天気データ取得開始: lat=$lat, lon=$lon")
                
                val response = weatherApiService.getWeatherForecast(
                    lat = lat,
                    lon = lon,
                    daily = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max,weathercode",
                    timezone = "auto"
                )
                
                Log.d("WeatherRepository", "API応答: $response")
                
                val bicycleWeatherList = mutableListOf<BicycleWeatherInfo>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                for (i in response.daily.time.indices) {
                    val date = response.daily.time[i]
                    val maxTemp = response.daily.temperature_2m_max[i]
                    val minTemp = response.daily.temperature_2m_min[i]
                    val precipitationProb = response.daily.precipitation_probability_max[i]
                    val windSpeed = response.daily.windspeed_10m_max[i]
                    val weatherCode = response.daily.weathercode[i]
                    
                    Log.d("WeatherRepository", "日付: $date, 最高気温: $maxTemp, 最低気温: $minTemp, 降水確率: $precipitationProb, 風速: $windSpeed, 天気コード: $weatherCode")
                    
                    val avgTemp = (maxTemp + minTemp) / 2.0
                    val isGoodForBicycle = isGoodForBicycle(
                        maxTemp = maxTemp,
                        minTemp = minTemp,
                        precipitationProb = precipitationProb,
                        windSpeed = windSpeed,
                        weatherCode = weatherCode
                    )
                    
                    val reason = getBicycleSuitabilityReason(
                        maxTemp = maxTemp,
                        minTemp = minTemp,
                        precipitationProb = precipitationProb,
                        windSpeed = windSpeed,
                        weatherCode = weatherCode
                    )
                    
                    val weatherDescription = getWeatherDescription(weatherCode)
                    
                    val bicycleWeatherInfo = BicycleWeatherInfo(
                        date = date,
                        temperature = avgTemp,
                        weatherDescription = weatherDescription,
                        humidity = 60, // Open-Meteoでは湿度が提供されないため、デフォルト値
                        windSpeed = windSpeed,
                        precipitationProbability = precipitationProb / 100.0,
                        isGoodForBicycle = isGoodForBicycle,
                        reason = reason
                    )
                    
                    bicycleWeatherList.add(bicycleWeatherInfo)
                    Log.d("WeatherRepository", "自転車適性: $isGoodForBicycle, 理由: $reason")
                }
                
                Log.d("WeatherRepository", "自転車天気情報作成完了: ${bicycleWeatherList.size}件")
                bicycleWeatherList
            } catch (e: Exception) {
                Log.e("WeatherRepository", "天気データ取得エラー", e)
                throw e
            }
        }
    }
    
    suspend fun searchLocations(query: String): Result<List<LocationSearchResult>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WeatherRepository", "Searching locations for query: $query")
                
                if (query.length < 2) {
                    Log.d("WeatherRepository", "Query too short, returning empty list")
                    return@withContext Result.success(emptyList())
                }
                
                val response = geocodingApiService.searchLocations(query = query)
                
                Log.d("WeatherRepository", "Geocoding API response: $response")
                
                val searchResults = response.results.map { result ->
                    LocationSearchResult(
                        name = result.name,
                        country = result.country,
                        state = result.state,
                        latitude = result.latitude,
                        longitude = result.longitude,
                        displayName = if (result.state != null) {
                            "${result.name}, ${result.state}, ${result.country}"
                        } else {
                            "${result.name}, ${result.country}"
                        }
                    )
                }
                
                Log.d("WeatherRepository", "Parsed search results: $searchResults")
                Result.success(searchResults)
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error searching locations", e)
                Result.failure(e)
            }
        }
    }
    
    private fun isGoodForBicycle(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): Boolean {
        // 気温条件: 最低気温が10度以上、最高気温が30度以下
        val tempCondition = minTemp >= 10 && maxTemp <= 30
        
        // 降水確率条件: 30%未満
        val precipitationCondition = precipitationProb < 30
        
        // 風速条件: 10 m/s未満
        val windCondition = windSpeed < 10
        
        // 天気条件: 晴れ、曇り、小雨のみ許可
        val weatherCondition = when (weatherCode) {
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> true
            else -> false
        }
        
        Log.d("WeatherRepository", "自転車適性判定: 気温=$tempCondition, 降水=$precipitationCondition, 風速=$windCondition, 天気=$weatherCondition")
        
        return tempCondition && precipitationCondition && windCondition && weatherCondition
    }
    
    private fun getBicycleSuitabilityReason(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): String {
        val reasons = mutableListOf<String>()
        
        if (minTemp < 10) {
            reasons.add("最低気温が低すぎます (${minTemp}°C)")
        }
        if (maxTemp > 30) {
            reasons.add("最高気温が高すぎます (${maxTemp}°C)")
        }
        if (precipitationProb >= 30) {
            reasons.add("降水確率が高いです (${precipitationProb}%)")
        }
        if (windSpeed >= 10) {
            reasons.add("風が強すぎます (${windSpeed} m/s)")
        }
        
        // 天気コードによる判定
        val badWeatherReasons = when (weatherCode) {
            95, 96, 99 -> "雷雨の可能性があります"
            71, 73, 75, 77, 85, 86 -> "雪の可能性があります"
            95, 96, 99 -> "雷雨の可能性があります"
            else -> null
        }
        
        badWeatherReasons?.let { reasons.add(it) }
        
        return if (reasons.isEmpty()) {
            "自転車に適した天気です"
        } else {
            reasons.joinToString(", ")
        }
    }
    
    private fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "晴天"
            1, 2, 3 -> "曇り"
            45, 48 -> "霧"
            51, 53, 55 -> "小雨"
            56, 57 -> "小雨（凍る）"
            61, 63, 65 -> "雨"
            66, 67 -> "雨（凍る）"
            71, 73, 75 -> "雪"
            77 -> "細かい雪"
            80, 81, 82 -> "にわか雨"
            85, 86 -> "にわか雪"
            95 -> "雷雨"
            96, 99 -> "雷雨（雹）"
            else -> "不明"
        }
    }
} 