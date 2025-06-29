package com.example.mybestzitendate.data

import com.google.gson.annotations.SerializedName

// Open-Meteo API レスポンス用のデータクラス
data class WeatherResponse(
    val daily: DailyData,
    val daily_units: DailyUnits,
    val timezone: String
)

data class DailyData(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val precipitation_probability_max: List<Int>,
    val windspeed_10m_max: List<Double>,
    val weathercode: List<Int>
)

data class DailyUnits(
    val temperature_2m_max: String,
    val temperature_2m_min: String,
    val precipitation_probability_max: String,
    val windspeed_10m_max: String,
    val weathercode: String
)

// 自転車に適した日かどうかを判定するための拡張データクラス
data class BicycleWeatherInfo(
    val date: String,
    val temperature: Double,
    val weatherDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val precipitationProbability: Double,
    val isGoodForBicycle: Boolean,
    val reason: String
) 