package com.example.mybestzitendate.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybestzitendate.ui.WeatherViewModel
import com.example.mybestzitendate.ui.WeatherUiState
import com.example.mybestzitendate.ui.components.LocationSearchBar
import com.example.mybestzitendate.ui.components.WeatherCard
import com.example.mybestzitendate.ui.components.TwoDayWeatherAdviceCard

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showSearchResults by viewModel.showSearchResults.collectAsState()
    val error by viewModel.error.collectAsState()
    val dailyWeatherAdviceData by viewModel.dailyWeatherAdviceData.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadWeatherData()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // タイトル
        Text(
            text = "2日間の天気アドバイス",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 地域検索バー
        LocationSearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            searchResults = searchResults,
            isSearching = isSearching,
            showSearchResults = showSearchResults,
            onLocationSelected = viewModel::selectLocation,
            onHideSearchResults = viewModel::hideSearchResults,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 現在の地域表示
        Text(
            text = "現在の地域: $currentLocation",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // エラーメッセージ
        error?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    TextButton(
                        onClick = viewModel::clearError
                    ) {
                        Text("閉じる")
                    }
                }
            }
        }
        
        // 天気データの表示
        when (uiState) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is WeatherUiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 2日間の天気アドバイスを表示
                    if (dailyWeatherAdviceData.isNotEmpty()) {
                        item {
                            TwoDayWeatherAdviceCard(
                                dailyAdviceList = dailyWeatherAdviceData,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    
                    // 従来の自転車天気カードも表示（参考用）
                    val weatherData = (uiState as WeatherUiState.Success).data
                    items(weatherData) { weatherInfo ->
                        WeatherCard(weatherInfo = weatherInfo)
                    }
                }
            }
            
            is WeatherUiState.Error -> {
                val errorMessage = (uiState as WeatherUiState.Error).message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "エラーが発生しました",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = viewModel::loadWeatherData
                        ) {
                            Text("再試行")
                        }
                    }
                }
            }
        }
    }
} 