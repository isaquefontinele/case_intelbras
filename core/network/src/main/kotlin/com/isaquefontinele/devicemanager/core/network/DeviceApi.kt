package com.isaquefontinele.devicemanager.core.network

import com.isaquefontinele.devicemanager.core.network.dto.AlarmCentralDto
import com.isaquefontinele.devicemanager.core.network.dto.AlarmCentralRequestDto
import com.isaquefontinele.devicemanager.core.network.dto.ListResponseDto
import com.isaquefontinele.devicemanager.core.network.dto.VideoDeviceDto
import com.isaquefontinele.devicemanager.core.network.dto.VideoDeviceRequestDto
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceApi {

    @GET("alarm-centrals")
    suspend fun getAlarmCentrals(): ListResponseDto<AlarmCentralDto>

    @GET("alarm-centrals/{id}")
    suspend fun getAlarmCentral(@Path("id") id: String): AlarmCentralDto

    @POST("alarm-centrals")
    suspend fun createAlarmCentral(@Body body: AlarmCentralRequestDto): AlarmCentralDto

    @PATCH("alarm-centrals/{id}")
    suspend fun updateAlarmCentral(@Path("id") id: String, @Body body: AlarmCentralRequestDto): AlarmCentralDto

    // DELETE answers 200 without a documented body.
    @DELETE("alarm-centrals/{id}")
    suspend fun deleteAlarmCentral(@Path("id") id: String): Response<Unit>

    @GET("video-devices")
    suspend fun getVideoDevices(): ListResponseDto<VideoDeviceDto>

    @GET("video-devices/{id}")
    suspend fun getVideoDevice(@Path("id") id: String): VideoDeviceDto

    @POST("video-devices")
    suspend fun createVideoDevice(@Body body: VideoDeviceRequestDto): VideoDeviceDto

    @PATCH("video-devices/{id}")
    suspend fun updateVideoDevice(@Path("id") id: String, @Body body: VideoDeviceRequestDto): VideoDeviceDto

    @DELETE("video-devices/{id}")
    suspend fun deleteVideoDevice(@Path("id") id: String): Response<Unit>
}

internal fun createDeviceApi(baseUrl: String, okHttpClient: OkHttpClient, json: Json): DeviceApi = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(DeviceApi::class.java)
