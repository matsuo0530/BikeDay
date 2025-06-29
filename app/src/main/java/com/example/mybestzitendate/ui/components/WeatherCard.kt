package com.example.mybestzitendate.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybestzitendate.data.BicycleWeatherInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeatherCard(
    weatherInfo: BicycleWeatherInfo,
    modifier: Modifier = Modifier
) {
    // デバッグログ
    LaunchedEffect(weatherInfo) {
        Log.d("WeatherCard", "WeatherCard描画: date=${weatherInfo.date}, temp=${weatherInfo.temperature}, isGood=${weatherInfo.isGoodForBicycle}")
    }
    
    val backgroundColor = if (weatherInfo.isGoodForBicycle) {
        Color(0xFFE8F5E8) // 薄い緑色（自転車に適している）
    } else {
        Color(0xFFFFF3E0) // 薄いオレンジ色（自転車に適していない）
    }
    
    val borderColor = if (weatherInfo.isGoodForBicycle) {
        Color(0xFF4CAF50) // 緑色
    } else {
        Color(0xFFFF9800) // オレンジ色
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // 日付表示（より詳細に）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatDate(weatherInfo.date),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = formatFullDate(weatherInfo.date),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
                
                // 自転車適性の大きな表示
                val statusText = if (weatherInfo.isGoodForBicycle) "🚴 自転車OK" else "❌ 自転車NG"
                val statusColor = if (weatherInfo.isGoodForBicycle) Color(0xFF4CAF50) else Color(0xFFFF9800)
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (weatherInfo.isGoodForBicycle) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 天気情報（より見やすく整理）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    WeatherInfoItem("🌡️ 気温", "${weatherInfo.temperature}°C")
                    WeatherInfoItem("☁️ 天気", weatherInfo.weatherDescription)
                    WeatherInfoItem("💧 湿度", "${weatherInfo.humidity}%")
                }
                Column(modifier = Modifier.weight(1f)) {
                    WeatherInfoItem("💨 風速", "${weatherInfo.windSpeed} m/s")
                    WeatherInfoItem("🌧️ 降水確率", "${(weatherInfo.precipitationProbability * 100).toInt()}%")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 理由（より目立つように）
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (weatherInfo.isGoodForBicycle) Color(0xFFC8E6C9) else Color(0xFFFFCC80)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = weatherInfo.reason,
                    fontSize = 14.sp,
                    color = if (weatherInfo.isGoodForBicycle) Color(0xFF2E7D32) else Color(0xFFD84315),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun WeatherInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("M/d (E)")
        formatter.format(date)
    } catch (e: Exception) {
        Log.e("WeatherCard", "日付フォーマットエラー: $dateString", e)
        dateString
    }
}

private fun formatFullDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
        formatter.format(date)
    } catch (e: Exception) {
        Log.e("WeatherCard", "完全日付フォーマットエラー: $dateString", e)
        dateString
    }
} 