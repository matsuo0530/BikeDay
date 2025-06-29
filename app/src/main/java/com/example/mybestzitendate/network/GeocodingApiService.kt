package com.example.mybestzitendate.network

import com.example.mybestzitendate.data.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocations(
        @Query("name") query: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "ja",
        @Query("format") format: String = "json"
    ): GeocodingResponse
} 