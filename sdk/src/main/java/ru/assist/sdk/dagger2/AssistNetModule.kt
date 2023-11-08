package ru.assist.sdk.dagger2

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import ru.assist.sdk.api.AssistApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
internal class AssistNetModule {

    companion object {
        private const val CONNECT_TIMEOUT = 20L
        private const val READ_TIMEOUT = 20L
    }
    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor()
        = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Provides
    @Singleton
    fun provideOkHttpClientBuilder(loggingInterceptor: HttpLoggingInterceptor)
        = OkHttpClient.Builder().addInterceptor(loggingInterceptor)

    @Provides
    @Singleton
    fun provideApiService(
        apiUrl: String,
        okHttpClientBuilder: OkHttpClient.Builder
    ): AssistApiService {
        val client = okHttpClientBuilder
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(XmlOrJsonConverterFactory())
            .client(client)
            .build()
            .create(AssistApiService::class.java)
    }
}