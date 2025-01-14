package potatocake.katecam.everymoment.data.model.network.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "http://13.125.156.74:8080/"

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
//            .client(SendFilesUtil.client)
            .addConverterFactory(GsonConverterFactory.create()) // Gson 변환기
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): PotatoCakeApiService {
        return retrofit.create(PotatoCakeApiService::class.java)
    }
}