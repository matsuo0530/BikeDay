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

// 時間帯別の天気情報
data class HourlyWeatherInfo(
    val hour: Int,
    val temperature: Double,
    val weatherDescription: String,
    val windSpeed: Double,
    val precipitationProbability: Double
)

// 1日分の総合アドバイス情報
data class DailyWeatherAdvice(
    val date: String,
    val isToday: Boolean,
    val temperature: Double,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val precipitationProbability: Double,
    val advice: List<DailyAdvice>,
    val hourlyInfo: List<HourlyWeatherInfo>? = null,
    val timeBasedNotes: List<String> = emptyList()
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

// 多様なルーティーンアドバイス用のデータクラス
data class WeatherAdviceInfo(
    val date: String,
    val temperature: Double,
    val weatherDescription: String,
    val humidity: Int,
    val windSpeed: Double,
    val precipitationProbability: Double,
    val advice: List<DailyAdvice>
)

data class DailyAdvice(
    val type: AdviceType,
    val isRecommended: Boolean,
    val reason: String,
    val icon: String
)

enum class AdviceType {
    BICYCLE,           // 自転車に乗れるか
    VENTILATION,       // 窓を開けて換気できるか
    CAR_FROST,         // 車のフロントガラスが凍結している可能性
    WINDOW_OPENING,    // 窓を開けて換気に適しているか
    AIR_CONDITIONING   // エアコンをつけるべきか
}

// 2日間の天気アドバイス用のデータクラス
data class TwoDayWeatherAdvice(
    val today: DailyWeatherAdvice,
    val tomorrow: DailyWeatherAdvice
) 