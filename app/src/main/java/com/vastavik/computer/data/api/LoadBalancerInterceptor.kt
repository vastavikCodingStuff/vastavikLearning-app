package com.vastavik.computer.data.api

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that rewrites every request's base URL
 * to whichever backend [BackendLoadBalancer] has picked as fastest.
 */
@Singleton
class LoadBalancerInterceptor @Inject constructor(
    private val loadBalancer: BackendLoadBalancer
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val bestUrl = loadBalancer.bestBaseUrl.trimEnd('/').toHttpUrl()

        val newUrl = originalRequest.url.newBuilder()
            .scheme(bestUrl.scheme)
            .host(bestUrl.host)
            .port(bestUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
