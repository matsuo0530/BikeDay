package com.example.mybestzitendate.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybestzitendate.data.BicycleWeatherInfo
import com.example.mybestzitendate.data.LocationSearchResult
import com.example.mybestzitendate.data.WeatherAdviceInfo
import com.example.mybestzitendate.data.DailyWeatherAdvice
import com.example.mybestzitendate.data.AdviceType
import com.example.mybestzitendate.location.LocationManager
import com.example.mybestzitendate.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val weatherRepository = WeatherRepository()
    private val locationManager = LocationManager(application)
    
    // UI状態
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    // 現在の地域
    private val _currentLocation = MutableStateFlow("横浜市, 日本")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()
    
    // 現在の位置情報（デフォルトは横浜）
    private var currentLatitude = 35.4437
    private var currentLongitude = 139.6380
    
    // 天気データの状態
    private val _weatherData = MutableStateFlow<List<BicycleWeatherInfo>>(emptyList())
    val weatherData: StateFlow<List<BicycleWeatherInfo>> = _weatherData.asStateFlow()
    
    // 新しいアドバイスデータの状態
    private val _weatherAdviceData = MutableStateFlow<List<WeatherAdviceInfo>>(emptyList())
    val weatherAdviceData: StateFlow<List<WeatherAdviceInfo>> = _weatherAdviceData.asStateFlow()
    
    // 日別天気アドバイスデータの状態
    private val _dailyWeatherAdviceData = MutableStateFlow<List<DailyWeatherAdvice>>(emptyList())
    val dailyWeatherAdviceData: StateFlow<List<DailyWeatherAdvice>> = _dailyWeatherAdviceData.asStateFlow()
    
    // 明日の詳細表示状態
    private val _isTomorrowExpanded = MutableStateFlow(false)
    val isTomorrowExpanded: StateFlow<Boolean> = _isTomorrowExpanded.asStateFlow()
    
    // ローディング状態
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // エラー状態
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 地域検索の状態
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<LocationSearchResult>>(emptyList())
    val searchResults: StateFlow<List<LocationSearchResult>> = _searchResults.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _showSearchResults = MutableStateFlow(false)
    val showSearchResults: StateFlow<Boolean> = _showSearchResults.asStateFlow()
    
    init {
        loadWeatherData()
    }
    
    fun loadWeatherData() {
        viewModelScope.launch {
            Log.d("WeatherViewModel", "天気データ読み込み開始")
            _uiState.value = WeatherUiState.Loading
            _error.value = null
            
            try {
                Log.d("WeatherViewModel", "位置情報取得: lat=$currentLatitude, lon=$currentLongitude")
                
                // 2日間の日別天気アドバイスデータのみを取得
                val dailyAdviceData = weatherRepository.getDailyWeatherAdvice(currentLatitude, currentLongitude)
                Log.d("WeatherViewModel", "2日間のアドバイスデータ取得成功: ${dailyAdviceData.size}件")
                
                val locationName = locationManager.getAddressFromLocation(currentLatitude, currentLongitude)
                Log.d("WeatherViewModel", "地域名: $locationName")
                
                _currentLocation.value = locationName
                _dailyWeatherAdviceData.value = dailyAdviceData
                
                // 2日間のデータを自転車天気情報に変換
                val bicycleWeatherData = dailyAdviceData.map { dailyAdvice ->
                    val bicycleAdvice = dailyAdvice.advice.find { it.type == AdviceType.BICYCLE }
                    BicycleWeatherInfo(
                        date = dailyAdvice.date,
                        temperature = dailyAdvice.temperature,
                        weatherDescription = dailyAdvice.weatherDescription,
                        humidity = dailyAdvice.humidity,
                        windSpeed = dailyAdvice.windSpeed,
                        precipitationProbability = dailyAdvice.precipitationProbability,
                        isGoodForBicycle = bicycleAdvice?.isRecommended ?: false,
                        reason = bicycleAdvice?.reason ?: "判定できません"
                    )
                }
                
                _weatherData.value = bicycleWeatherData
                _uiState.value = WeatherUiState.Success(bicycleWeatherData)
                
                Log.d("WeatherViewModel", "UI状態更新完了: Success")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "天気データ読み込みエラー", e)
                _error.value = "天気情報の取得に失敗しました: ${e.message}"
                _uiState.value = WeatherUiState.Error("天気情報の取得に失敗しました: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleTomorrowExpanded() {
        _isTomorrowExpanded.value = !_isTomorrowExpanded.value
    }
    
    fun updateSearchQuery(query: String) {
        val trimmed = query.trim()
        _searchQuery.value = trimmed
        if (trimmed.isNotEmpty()) {
            performLocationSearch(trimmed)
        } else {
            _searchResults.value = emptyList()
            _showSearchResults.value = false
        }
    }
    
    private fun performLocationSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            
            try {
                // 検索の遅延を追加してAPIコールを最適化
                delay(300)
                
                // 検索クエリが変更された場合は検索をキャンセル
                if (_searchQuery.value != query) {
                    return@launch
                }
                
                val result = weatherRepository.searchLocations(query)
                result.fold(
                    onSuccess = { locations ->
                        _searchResults.value = locations
                        _showSearchResults.value = locations.isNotEmpty()
                        Log.d("WeatherViewModel", "Location search results: ${locations.size} locations")
                    },
                    onFailure = { exception ->
                        _error.value = "地域検索に失敗しました: ${exception.message}"
                        Log.e("WeatherViewModel", "Error searching locations", exception)
                        _searchResults.value = emptyList()
                        _showSearchResults.value = false
                    }
                )
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    fun selectLocation(location: LocationSearchResult) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        
        _searchQuery.value = location.getShortDisplayName()
        _searchResults.value = emptyList()
        _showSearchResults.value = false
        
        Log.d("WeatherViewModel", "Location selected: ${location.getDetailedDisplayName()}")
        
        // 新しい位置で天気データを再読み込み
        loadWeatherData()
    }
    
    fun hideSearchResults() {
        _showSearchResults.value = false
    }
    
    fun clearError() {
        _error.value = null
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: List<BicycleWeatherInfo>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
} 