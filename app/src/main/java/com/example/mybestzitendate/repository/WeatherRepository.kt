package com.example.mybestzitendate.repository

import android.util.Log
import com.example.mybestzitendate.data.BicycleWeatherInfo
import com.example.mybestzitendate.data.DailyData
import com.example.mybestzitendate.data.LocationSearchResult
import com.example.mybestzitendate.data.WeatherResponse
import com.example.mybestzitendate.data.WeatherAdviceInfo
import com.example.mybestzitendate.data.DailyAdvice
import com.example.mybestzitendate.data.AdviceType
import com.example.mybestzitendate.data.DailyWeatherAdvice
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
                    
                    bicycleWeatherList.add(
                        BicycleWeatherInfo(
                            date = date,
                            temperature = avgTemp,
                            weatherDescription = weatherDescription,
                            humidity = 60, // 仮の値（APIに湿度データがない場合）
                            windSpeed = windSpeed,
                            precipitationProbability = precipitationProb / 100.0,
                            isGoodForBicycle = isGoodForBicycle,
                            reason = reason
                        )
                    )
                }
                
                Log.d("WeatherRepository", "自転車天気データ生成完了: ${bicycleWeatherList.size}件")
                bicycleWeatherList
            } catch (e: Exception) {
                Log.e("WeatherRepository", "天気データ取得エラー", e)
                throw e
            }
        }
    }
    
    suspend fun getWeatherAdviceForecast(lat: Double, lon: Double): List<WeatherAdviceInfo> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WeatherRepository", "天気アドバイスデータ取得開始: lat=$lat, lon=$lon")
                
                val response = weatherApiService.getWeatherForecast(
                    lat = lat,
                    lon = lon,
                    daily = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max,weathercode",
                    timezone = "auto"
                )
                
                val weatherAdviceList = mutableListOf<WeatherAdviceInfo>()
                
                for (i in response.daily.time.indices) {
                    val date = response.daily.time[i]
                    val maxTemp = response.daily.temperature_2m_max[i]
                    val minTemp = response.daily.temperature_2m_min[i]
                    val precipitationProb = response.daily.precipitation_probability_max[i]
                    val windSpeed = response.daily.windspeed_10m_max[i]
                    val weatherCode = response.daily.weathercode[i]
                    
                    val avgTemp = (maxTemp + minTemp) / 2.0
                    val weatherDescription = getWeatherDescription(weatherCode)
                    
                    val advice = listOf(
                        getBicycleAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode),
                        getVentilationAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode),
                        getCarFrostAdvice(minTemp, weatherCode),
                        getWindowOpeningAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode)
                    )
                    
                    weatherAdviceList.add(
                        WeatherAdviceInfo(
                            date = date,
                            temperature = avgTemp,
                            weatherDescription = weatherDescription,
                            humidity = 60,
                            windSpeed = windSpeed,
                            precipitationProbability = precipitationProb / 100.0,
                            advice = advice
                        )
                    )
                }
                
                Log.d("WeatherRepository", "天気アドバイスデータ生成完了: ${weatherAdviceList.size}件")
                weatherAdviceList
            } catch (e: Exception) {
                Log.e("WeatherRepository", "天気アドバイスデータ取得エラー", e)
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
    
    private fun getBicycleAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        val isRecommended = isGoodForBicycle(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode)
        val reason = getBicycleSuitabilityReason(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode)
        return DailyAdvice(
            type = AdviceType.BICYCLE,
            isRecommended = isRecommended,
            reason = reason,
            icon = if (isRecommended) "🚴" else "❌"
        )
    }
    
    private fun getVentilationAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        // 換気に適している条件: 気温5-35度、降水確率50%未満、風速15m/s未満
        val tempCondition = minTemp >= 5 && maxTemp <= 35
        val precipitationCondition = precipitationProb < 50
        val windCondition = windSpeed < 15
        val weatherCondition = when (weatherCode) {
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> true
            else -> false
        }
        
        val isRecommended = tempCondition && precipitationCondition && windCondition && weatherCondition
        
        val reasons = mutableListOf<String>()
        if (!tempCondition) {
            reasons.add("気温が適切ではありません (${minTemp}°C〜${maxTemp}°C)")
        }
        if (!precipitationCondition) {
            reasons.add("降水確率が高いです (${precipitationProb}%)")
        }
        if (!windCondition) {
            reasons.add("風が強すぎます (${windSpeed} m/s)")
        }
        if (!weatherCondition) {
            reasons.add("天候が悪いです")
        }
        
        val reason = if (isRecommended) {
            "換気に適した天気です"
        } else {
            reasons.joinToString(", ")
        }
        
        return DailyAdvice(
            type = AdviceType.VENTILATION,
            isRecommended = isRecommended,
            reason = reason,
            icon = if (isRecommended) "💨" else "❌"
        )
    }
    
    private fun getCarFrostAdvice(
        minTemp: Double,
        weatherCode: Int
    ): DailyAdvice {
        // 凍結の可能性: 最低気温が3度以下、または雪の天気
        val tempCondition = minTemp <= 3
        val snowCondition = when (weatherCode) {
            71, 73, 75, 77, 85, 86 -> true
            else -> false
        }
        
        val isRecommended = tempCondition || snowCondition
        
        val reasons = mutableListOf<String>()
        if (tempCondition) {
            reasons.add("最低気温が低いです (${minTemp}°C)")
        }
        if (snowCondition) {
            reasons.add("雪の可能性があります")
        }
        
        val reason = if (isRecommended) {
            "フロントガラスの凍結に注意が必要です"
        } else {
            "凍結の心配はありません"
        }
        
        return DailyAdvice(
            type = AdviceType.CAR_FROST,
            isRecommended = isRecommended,
            reason = reason,
            icon = if (isRecommended) "❄️" else "✅"
        )
    }
    
    private fun getWindowOpeningAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        // 窓開けに適している条件: 気温10-30度、降水確率30%未満、風速8m/s未満
        val tempCondition = minTemp >= 10 && maxTemp <= 30
        val precipitationCondition = precipitationProb < 30
        val windCondition = windSpeed < 8
        val weatherCondition = when (weatherCode) {
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> true
            else -> false
        }
        
        val isRecommended = tempCondition && precipitationCondition && windCondition && weatherCondition
        
        val reasons = mutableListOf<String>()
        if (!tempCondition) {
            reasons.add("気温が適切ではありません (${minTemp}°C〜${maxTemp}°C)")
        }
        if (!precipitationCondition) {
            reasons.add("降水確率が高いです (${precipitationProb}%)")
        }
        if (!windCondition) {
            reasons.add("風が強すぎます (${windSpeed} m/s)")
        }
        if (!weatherCondition) {
            reasons.add("天候が悪いです")
        }
        
        val reason = if (isRecommended) {
            "窓を開けるのに適した天気です"
        } else {
            reasons.joinToString(", ")
        }
        
        return DailyAdvice(
            type = AdviceType.WINDOW_OPENING,
            isRecommended = isRecommended,
            reason = reason,
            icon = if (isRecommended) "🪟" else "❌"
        )
    }
    
    private fun getAirConditioningAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        // エアコンが必要な条件: 最高気温が25度以上、または最低気温が5度以下
        val hotCondition = maxTemp >= 25
        val coldCondition = minTemp <= 5
        val isRecommended = hotCondition || coldCondition
        
        val reasons = mutableListOf<String>()
        if (hotCondition) {
            reasons.add("最高気温が高いです (${maxTemp}°C)")
        }
        if (coldCondition) {
            reasons.add("最低気温が低いです (${minTemp}°C)")
        }
        
        val reason = if (isRecommended) {
            if (hotCondition && coldCondition) {
                "気温差が大きいため、エアコンの使用を推奨します"
            } else if (hotCondition) {
                "暑いため、冷房の使用を推奨します"
            } else {
                "寒いため、暖房の使用を推奨します"
            }
        } else {
            "エアコンの使用は不要です"
        }
        
        return DailyAdvice(
            type = AdviceType.AIR_CONDITIONING,
            isRecommended = isRecommended,
            reason = reason,
            icon = if (isRecommended) "❄️" else "✅"
        )
    }
    
    suspend fun getDailyWeatherAdvice(lat: Double, lon: Double): List<DailyWeatherAdvice> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("WeatherRepository", "日別天気アドバイスデータ取得開始: lat=$lat, lon=$lon")
                
                val response = weatherApiService.getWeatherForecast(
                    lat = lat,
                    lon = lon,
                    daily = "temperature_2m_max,temperature_2m_min,precipitation_probability_max,windspeed_10m_max,weathercode",
                    timezone = "auto"
                )
                
                val dailyAdviceList = mutableListOf<DailyWeatherAdvice>()
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                // 2日間のデータのみを処理
                for (i in 0 until minOf(2, response.daily.time.size)) {
                    val date = response.daily.time[i]
                    val maxTemp = response.daily.temperature_2m_max[i]
                    val minTemp = response.daily.temperature_2m_min[i]
                    val precipitationProb = response.daily.precipitation_probability_max[i]
                    val windSpeed = response.daily.windspeed_10m_max[i]
                    val weatherCode = response.daily.weathercode[i]
                    
                    val avgTemp = (maxTemp + minTemp) / 2.0
                    val weatherDescription = getWeatherDescription(weatherCode)
                    val isToday = date == today
                    
                    val advice = listOf(
                        getBicycleAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode),
                        getVentilationAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode),
                        getCarFrostAdvice(minTemp, weatherCode),
                        getWindowOpeningAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode),
                        getAirConditioningAdvice(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode)
                    )
                    
                    // 時間帯別の補足情報を生成
                    val timeBasedNotes = generateTimeBasedNotes(maxTemp, minTemp, precipitationProb, windSpeed, weatherCode)
                    
                    dailyAdviceList.add(
                        DailyWeatherAdvice(
                            date = date,
                            isToday = isToday,
                            temperature = avgTemp,
                            maxTemp = maxTemp,
                            minTemp = minTemp,
                            weatherDescription = weatherDescription,
                            humidity = 60,
                            windSpeed = windSpeed,
                            precipitationProbability = precipitationProb / 100.0,
                            advice = advice,
                            timeBasedNotes = timeBasedNotes
                        )
                    )
                }
                
                Log.d("WeatherRepository", "日別天気アドバイスデータ生成完了: ${dailyAdviceList.size}件")
                dailyAdviceList
            } catch (e: Exception) {
                Log.e("WeatherRepository", "日別天気アドバイスデータ取得エラー", e)
                throw e
            }
        }
    }
    
    private fun generateTimeBasedNotes(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): List<String> {
        val notes = mutableListOf<String>()
        
        // 気温差が大きい場合
        if (maxTemp - minTemp > 10) {
            notes.add("🌡️ 気温差が大きいため、時間帯によって服装の調整が必要です")
        }
        
        // 朝晩の気温が低い場合
        if (minTemp < 5) {
            notes.add("❄️ 朝晩は冷え込みが予想されます")
        }
        
        // 日中が暑い場合
        if (maxTemp > 30) {
            notes.add("☀️ 日中は暑さに注意が必要です")
        }
        
        // 降水確率が高い場合
        if (precipitationProb > 50) {
            notes.add("🌧️ 降水の可能性が高いため、外出時は傘をお持ちください")
        }
        
        // 風が強い場合
        if (windSpeed > 8) {
            notes.add("💨 風が強いため、洗濯物や傘にご注意ください")
        }
        
        // 天気が変わりやすい場合
        when (weatherCode) {
            80, 81, 82 -> notes.add("🌦️ にわか雨の可能性があります")
            95, 96, 99 -> notes.add("⚡ 雷雨の可能性があります")
        }
        
        return notes
    }
} 