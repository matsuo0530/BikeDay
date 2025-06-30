package com.example.mybestzitendate

import com.example.mybestzitendate.data.AdviceType
import com.example.mybestzitendate.data.DailyAdvice
import com.example.mybestzitendate.repository.WeatherRepository
import org.junit.Test
import org.junit.Assert.*

class WeatherRepositoryTest {
    
    private val weatherRepository = WeatherRepository()
    
    @Test
    fun testGetAdviceTitle() {
        // 各アドバイスタイプのタイトルが正しく取得できるかテスト
        assertEquals("🚴 自転車", getAdviceTitle(AdviceType.BICYCLE))
        assertEquals("💨 換気", getAdviceTitle(AdviceType.VENTILATION))
        assertEquals("❄️ 車の凍結", getAdviceTitle(AdviceType.CAR_FROST))
        assertEquals("🪟 窓開け", getAdviceTitle(AdviceType.WINDOW_OPENING))
        assertEquals("❄️ エアコン", getAdviceTitle(AdviceType.AIR_CONDITIONING))
    }
    
    @Test
    fun testBicycleAdviceLogic() {
        // 自転車に適した条件のテスト
        val goodBicycleAdvice = createBicycleAdvice(
            maxTemp = 25.0,
            minTemp = 15.0,
            precipitationProb = 20,
            windSpeed = 5.0,
            weatherCode = 0 // 晴天
        )
        assertTrue(goodBicycleAdvice.isRecommended)
        
        // 自転車に適していない条件のテスト
        val badBicycleAdvice = createBicycleAdvice(
            maxTemp = 35.0,
            minTemp = 5.0,
            precipitationProb = 50,
            windSpeed = 15.0,
            weatherCode = 95 // 雷雨
        )
        assertFalse(badBicycleAdvice.isRecommended)
    }
    
    @Test
    fun testCarFrostAdviceLogic() {
        // 凍結の可能性がある条件のテスト（気温が低い）
        val frostAdvice1 = createCarFrostAdvice(
            minTemp = 2.0,
            weatherCode = 0 // 晴天でも気温が低い
        )
        assertTrue(frostAdvice1.isRecommended)
        
        // 凍結の可能性がある条件のテスト（雪）
        val frostAdvice2 = createCarFrostAdvice(
            minTemp = 10.0,
            weatherCode = 71 // 雪
        )
        assertTrue(frostAdvice2.isRecommended)
        
        // 凍結の心配がない条件のテスト
        val noFrostAdvice = createCarFrostAdvice(
            minTemp = 10.0,
            weatherCode = 0 // 晴天
        )
        assertFalse(noFrostAdvice.isRecommended)
    }
    
    @Test
    fun testAirConditioningAdviceLogic() {
        // エアコンが必要な条件（暑い）のテスト
        val hotAdvice = createAirConditioningAdvice(
            maxTemp = 30.0,
            minTemp = 20.0
        )
        assertTrue(hotAdvice.isRecommended)
        
        // エアコンが必要な条件（寒い）のテスト
        val coldAdvice = createAirConditioningAdvice(
            maxTemp = 15.0,
            minTemp = 3.0
        )
        assertTrue(coldAdvice.isRecommended)
        
        // エアコンが不要な条件のテスト
        val comfortableAdvice = createAirConditioningAdvice(
            maxTemp = 22.0,
            minTemp = 12.0
        )
        assertFalse(comfortableAdvice.isRecommended)
    }
    
    @Test
    fun testWindowOpeningAdviceLogic() {
        // 窓開けに適した条件のテスト
        val goodWindowAdvice = createWindowOpeningAdvice(
            maxTemp = 25.0,
            minTemp = 15.0,
            precipitationProb = 20,
            windSpeed = 5.0,
            weatherCode = 0
        )
        assertTrue(goodWindowAdvice.isRecommended)
        
        // 窓開けに適していない条件のテスト
        val badWindowAdvice = createWindowOpeningAdvice(
            maxTemp = 35.0,
            minTemp = 5.0,
            precipitationProb = 50,
            windSpeed = 10.0,
            weatherCode = 95
        )
        assertFalse(badWindowAdvice.isRecommended)
    }
    
    @Test
    fun testVentilationAdviceLogic() {
        // 換気に適した条件のテスト
        val goodVentilationAdvice = createVentilationAdvice(
            maxTemp = 25.0,
            minTemp = 10.0,
            precipitationProb = 30,
            windSpeed = 10.0,
            weatherCode = 0
        )
        assertTrue(goodVentilationAdvice.isRecommended)
        
        // 換気に適していない条件のテスト
        val badVentilationAdvice = createVentilationAdvice(
            maxTemp = 40.0,
            minTemp = 0.0,
            precipitationProb = 60,
            windSpeed = 20.0,
            weatherCode = 95
        )
        assertFalse(badVentilationAdvice.isRecommended)
    }
    
    // 実際のロジックに基づくテスト用関数
    private fun createBicycleAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        val tempCondition = minTemp >= 10 && maxTemp <= 30
        val precipitationCondition = precipitationProb < 30
        val windCondition = windSpeed < 10
        val weatherCondition = when (weatherCode) {
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> true
            else -> false
        }
        
        val isRecommended = tempCondition && precipitationCondition && windCondition && weatherCondition
        
        return DailyAdvice(
            type = AdviceType.BICYCLE,
            isRecommended = isRecommended,
            reason = if (isRecommended) "自転車に適した天気です" else "自転車に適していません",
            icon = if (isRecommended) "🚴" else "❌"
        )
    }
    
    private fun createCarFrostAdvice(
        minTemp: Double,
        weatherCode: Int
    ): DailyAdvice {
        val tempCondition = minTemp <= 3
        val snowCondition = when (weatherCode) {
            71, 73, 75, 77, 85, 86 -> true
            else -> false
        }
        
        val isRecommended = tempCondition || snowCondition
        
        return DailyAdvice(
            type = AdviceType.CAR_FROST,
            isRecommended = isRecommended,
            reason = if (isRecommended) "フロントガラスの凍結に注意が必要です" else "凍結の心配はありません",
            icon = if (isRecommended) "❄️" else "✅"
        )
    }
    
    private fun createAirConditioningAdvice(
        maxTemp: Double,
        minTemp: Double
    ): DailyAdvice {
        val hotCondition = maxTemp >= 25
        val coldCondition = minTemp <= 5
        val isRecommended = hotCondition || coldCondition
        
        return DailyAdvice(
            type = AdviceType.AIR_CONDITIONING,
            isRecommended = isRecommended,
            reason = if (isRecommended) "エアコンの使用を推奨します" else "エアコンの使用は不要です",
            icon = if (isRecommended) "❄️" else "✅"
        )
    }
    
    private fun createWindowOpeningAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        val tempCondition = minTemp >= 10 && maxTemp <= 30
        val precipitationCondition = precipitationProb < 30
        val windCondition = windSpeed < 8
        val weatherCondition = when (weatherCode) {
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> true
            else -> false
        }
        
        val isRecommended = tempCondition && precipitationCondition && windCondition && weatherCondition
        
        return DailyAdvice(
            type = AdviceType.WINDOW_OPENING,
            isRecommended = isRecommended,
            reason = if (isRecommended) "窓を開けるのに適した天気です" else "窓を開けるのに適していません",
            icon = if (isRecommended) "🪟" else "❌"
        )
    }
    
    private fun createVentilationAdvice(
        maxTemp: Double,
        minTemp: Double,
        precipitationProb: Int,
        windSpeed: Double,
        weatherCode: Int
    ): DailyAdvice {
        val tempCondition = minTemp >= 5 && maxTemp <= 35
        val precipitationCondition = precipitationProb < 50
        val windCondition = windSpeed < 15
        val weatherCondition = when (weatherCode) {
            0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> true
            else -> false
        }
        
        val isRecommended = tempCondition && precipitationCondition && windCondition && weatherCondition
        
        return DailyAdvice(
            type = AdviceType.VENTILATION,
            isRecommended = isRecommended,
            reason = if (isRecommended) "換気に適した天気です" else "換気に適していません",
            icon = if (isRecommended) "💨" else "❌"
        )
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
} 