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
        return if (state != null) {
            "$name, $state, $country"
        } else {
            "$name, $country"
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