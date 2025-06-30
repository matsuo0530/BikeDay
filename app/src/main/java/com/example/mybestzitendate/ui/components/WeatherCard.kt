package com.example.mybestzitendate.ui.components

import android.util.Log
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
import com.example.mybestzitendate.data.DailyAdvice
import com.example.mybestzitendate.data.AdviceType
import com.example.mybestzitendate.data.DailyWeatherAdvice
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherCard(
    weatherInfo: BicycleWeatherInfo,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(weatherInfo) {
        Log.d("WeatherCard", "WeatherCard描画: date=${weatherInfo.date}, temp=${weatherInfo.temperature}, isGood=${weatherInfo.isGoodForBicycle}")
    }
    
    val backgroundColor = if (weatherInfo.isGoodForBicycle) {
        Color(0xFFE8F5E8)
    } else {
        Color(0xFFFFF3E0)
    }
    
    val borderColor = if (weatherInfo.isGoodForBicycle) {
        Color(0xFF4CAF50)
    } else {
        Color(0xFFFF9800)
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
                
                val statusText = if (weatherInfo.isGoodForBicycle) "🚴 自転車OK" else "❌ 自転車NG"
                
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

@Composable
fun TwoDayWeatherAdviceCard(
    dailyAdviceList: List<DailyWeatherAdvice>,
    modifier: Modifier = Modifier
) {
    if (dailyAdviceList.size < 2) {
        return
    }
    
    val today = dailyAdviceList[0]
    val tomorrow = dailyAdviceList[1]
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        DailyAdviceCard(
            dailyAdvice = today,
            title = "今日 (${formatDate(today.date)})",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DailyAdviceCard(
            dailyAdvice = tomorrow,
            title = "明日 (${formatDate(tomorrow.date)})",
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun DailyAdviceCard(
    dailyAdvice: DailyWeatherAdvice,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dailyAdvice.isToday) Color(0xFFE3F2FD) else Color(0xFFF3E5F5)
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (dailyAdvice.isToday) Color(0xFF2196F3) else Color(0xFF9C27B0)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    WeatherInfoItem("🌡️ 気温", "${dailyAdvice.minTemp}°C〜${dailyAdvice.maxTemp}°C")
                    WeatherInfoItem("☁️ 天気", dailyAdvice.weatherDescription)
                    WeatherInfoItem("💧 湿度", "${dailyAdvice.humidity}%")
                }
                Column(modifier = Modifier.weight(1f)) {
                    WeatherInfoItem("💨 風速", "${dailyAdvice.windSpeed} m/s")
                    WeatherInfoItem("🌧️ 降水確率", "${(dailyAdvice.precipitationProbability * 100).toInt()}%")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "今日のアドバイス",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            dailyAdvice.advice.forEach { advice ->
                AdviceItem(advice = advice)
            }
            
            if (dailyAdvice.timeBasedNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "注意事項",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                dailyAdvice.timeBasedNotes.forEach { note ->
                    Text(
                        text = "• $note",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdviceItem(
    advice: DailyAdvice,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (advice.isRecommended) {
        Color(0xFFE8F5E8)
    } else {
        Color(0xFFFFF3E0)
    }
    
    val borderColor = if (advice.isRecommended) {
        Color(0xFF4CAF50)
    } else {
        Color(0xFFFF9800)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = advice.icon,
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getAdviceTitle(advice.type),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = advice.reason,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = if (advice.isRecommended) "推奨" else "注意",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (advice.isRecommended) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
        }
    }
}

private fun getAdviceTitle(type: AdviceType): String {
    return when (type) {
        AdviceType.BICYCLE -> "🚴 自転車"
        AdviceType.VENTILATION -> "💨 換気"
        AdviceType.CAR_FROST -> "❄️ 車の凍結"
        AdviceType.WINDOW_OPENING -> "🪟 窓開け"
        AdviceType.AIR_CONDITIONING -> "❄️ エアコン"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("M/d (E)", Locale.JAPANESE)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        Log.e("WeatherCard", "日付フォーマットエラー: $dateString", e)
        dateString
    }
}

private fun formatFullDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("yyyy年M月d日", Locale.JAPANESE)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        Log.e("WeatherCard", "完全日付フォーマットエラー: $dateString", e)
        dateString
    }
} 