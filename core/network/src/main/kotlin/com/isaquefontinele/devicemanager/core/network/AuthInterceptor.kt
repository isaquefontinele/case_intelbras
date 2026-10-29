package com.isaquefontinele.devicemanager.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** Adds the API bearer token to every request. */
class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
