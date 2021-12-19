package com.ar7lab.cherieapp.di

import android.content.Context
import com.ar7lab.cherieapp.BuildConfig
import com.ar7lab.cherieapp.base.interceptors.ConnectivityInterceptor
import com.ar7lab.cherieapp.network.repositories.*
import com.ar7lab.cherieapp.network.service.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Logger.Companion.DEFAULT
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val KEY_AUTHORIZATION = "auth"

    /**
     * Building retrofit client for networking request
     *  @param httpClient
     *  @return retrofit
     */
    private fun retrofitClient(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(httpClient)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi()))
            .build()
    }

    /**
     * Building moshi for parsing json into Kotlin class
     */
    private fun moshi(): Moshi {
        val moshiBuilder = Moshi.Builder().add(KotlinJsonAdapterFactory())
        return moshiBuilder.build()
    }

    /**
     * Building http client to perform a network request
     * @param context, sharePreferencesManager
     * @return OkHttpClient
     */
    private fun httpClient(
        context: Context,
        sharePreferencesManager: SharePreferencesManager
    ): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor(DEFAULT)
        val clientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder.addInterceptor(httpLoggingInterceptor)
        }

        // headers
        clientBuilder.addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            val headers = HashMap<String, String>()
            headers[KEY_AUTHORIZATION] = "${sharePreferencesManager.token}"

            for ((key, value) in headers) {
                builder.addHeader(key, value)
            }
            chain.proceed(builder.build())
        }

        clientBuilder.addInterceptor(ConnectivityInterceptor(context))
        clientBuilder.connectTimeout(15, TimeUnit.SECONDS)
        clientBuilder.readTimeout(15, TimeUnit.SECONDS)
        clientBuilder.writeTimeout(15, TimeUnit.SECONDS)
        return clientBuilder.build()
    }

    private fun createNetworkClient(context: Context, sharePreferencesManager: SharePreferencesManager): Retrofit {
        return retrofitClient(httpClient(context, sharePreferencesManager))
    }

    @Provides
    fun provideRetrofit(@ApplicationContext context: Context, sharePreferencesManager: SharePreferencesManager): Retrofit {
        return createNetworkClient(context, sharePreferencesManager)
    }

    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Provides
    fun provideAuthRepository(authService: AuthService): AuthRepository {
        return AuthRepositoryImpl(authService)
    }

    @Provides
    fun provideArtService(retrofit: Retrofit): ArtService {
        return retrofit.create(ArtService::class.java)
    }

    @Provides
    fun provideArtRepository(artService: ArtService): ArtRepository {
        return ArtRepositoryImpl(artService)
    }

    @Provides
    fun provideArtistService(retrofit: Retrofit): ArtistService {
        return retrofit.create(ArtistService::class.java)
    }

    @Provides
    fun provideArtistRepository(artistService: ArtistService): ArtistRepository {
        return ArtistRepositoryImpl(artistService)
    }

    @Provides
    fun providePaymentRepository(paymentService: PaymentService): PaymentRepository {
        return PaymentRepositoryImpl(paymentService)
    }

    @Provides
    fun providePaymentService(retrofit: Retrofit): PaymentService {
        return retrofit.create(PaymentService::class.java)
    }

    @Provides
    fun provideWalletRepository(walletService: WalletService): WalletRepository {
        return WalletRepositoryImpl(walletService)
    }

    @Provides
    fun provideWalletService(retrofit: Retrofit): WalletService {
        return retrofit.create(WalletService::class.java)
    }

    @Provides
    fun provideNewsService(retrofit: Retrofit): NewsService =
        retrofit.create(NewsService::class.java)

    @Provides
    fun notificationService(retrofit: Retrofit): NotificationService =
        retrofit.create(NotificationService::class.java)

    @Provides
    fun provideNewsRepository(newsService: NewsService): NewsRepository =
        NewsRepositoryImpl(newsService)

    @Provides
    fun provideNotificationRepository(newsService: NotificationService): NotificationRepository =
        NotificationRepositoryImpl(newsService)

    @Provides
    fun provideCommonService(retrofit: Retrofit): CommonService =
        retrofit.create(CommonService::class.java)

    @Provides
    fun provideCommonRepository(commonService: CommonService): CommonRepository =
        CommonRepositoryImpl(commonService)

    @Provides
    fun provideUserService(retrofit: Retrofit): UserService =
        retrofit.create(UserService::class.java)

    @Provides
    fun provideUserRepository(userService: UserService): UserRepository =
        UserRepositoryImpl(userService)
}