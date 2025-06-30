package com.example.mybestzitendate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.mybestzitendate.ui.WeatherViewModel
import com.example.mybestzitendate.ui.screens.WeatherScreen
import com.example.mybestzitendate.ui.screens.ProfileScreen
import com.example.mybestzitendate.ui.theme.MyBestZitenDateTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBestZitenDateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: WeatherViewModel
) {
    var currentRoute by remember { mutableStateOf("home") }
    
    when (currentRoute) {
        "home" -> {
            WeatherScreen(
                viewModel = viewModel,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                }
            )
        }
        "profile" -> {
            ProfileScreen(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        else -> {
            WeatherScreen(
                viewModel = viewModel,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                }
            )
        }
    }
}