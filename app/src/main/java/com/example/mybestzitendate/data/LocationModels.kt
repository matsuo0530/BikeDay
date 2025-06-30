package com.example.mybestzitendate.data

data class LocationSearchResult(
    val name: String,
    val country: String,
    val state: String?,
    val latitude: Double,
    val longitude: Double,
    val displayName: String
) {
    fun getFullDisplayName(): String {
        return when {
            // 日本の場合
            country == "日本" -> {
                when {
                    state != null && state.isNotEmpty() -> "$state$name"
                    else -> name
                }
            }
            // その他の国の場合
            state != null && state.isNotEmpty() -> "$name, $state, $country"
            else -> "$name, $country"
        }
    }
    
    fun getShortDisplayName(): String {
        return when {
            country == "日本" -> {
                when {
                    state != null && state.isNotEmpty() -> "$state$name"
                    else -> name
                }
            }
            else -> name
        }
    }
    
    fun getDetailedDisplayName(): String {
        return when {
            country == "日本" -> {
                when {
                    state != null && state.isNotEmpty() -> "$state$name, $country"
                    else -> "$name, $country"
                }
            }
            state != null && state.isNotEmpty() -> "$name, $state, $country"
            else -> "$name, $country"
        }
    }
}

data class GeocodingResponse(
    val results: List<GeocodingResult>
)

data class GeocodingResult(
    val name: String,
    val country: String,
    val state: String?,
    val latitude: Double,
    val longitude: Double
) 