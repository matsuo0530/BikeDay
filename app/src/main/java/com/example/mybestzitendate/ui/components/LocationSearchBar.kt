package com.example.mybestzitendate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybestzitendate.data.LocationSearchResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<LocationSearchResult>,
    isSearching: Boolean,
    showSearchResults: Boolean,
    onLocationSelected: (LocationSearchResult) -> Unit,
    onHideSearchResults: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("地域を検索") },
            placeholder = { Text("例: 東京、大阪、札幌...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "検索"
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        // 検索結果のドロップダウン
        if (showSearchResults && searchResults.isNotEmpty()) {
            SearchResultsDropdown(
                searchResults = searchResults,
                onLocationSelected = onLocationSelected,
                onDismiss = onHideSearchResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 60.dp)
            )
        }
    }
}

@Composable
fun SearchResultsDropdown(
    searchResults: List<LocationSearchResult>,
    onLocationSelected: (LocationSearchResult) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(searchResults) { location ->
                LocationSearchResultItem(
                    location = location,
                    onClick = {
                        onLocationSelected(location)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun LocationSearchResultItem(
    location: LocationSearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (location.state != null) {
                    "${location.state}, ${location.country}"
                } else {
                    location.country
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 