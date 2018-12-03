package com.wavesplatform.wallet.v2.injection.module

import android.content.Context
import android.util.Log
import com.github.simonpercic.oklog3.OkLogInterceptor
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.factory.RxErrorHandlingCallAdapterFactory
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.ErrorManager
import com.wavesplatform.wallet.v2.data.remote.*
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import ren.yale.android.retrofitcachelibrx2.RetrofitCache
import ren.yale.android.retrofitcachelibrx2.intercept.CacheForceInterceptorNoNet
import ren.yale.android.retrofitcachelibrx2.intercept.CacheInterceptorOnNet
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    internal fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheSize = 200 * 1024 * 1024
        val cacheDirectory = File(context.getCacheDir(), "httpcache")

        return Cache(cacheDirectory, cacheSize.toLong())
    }

    @Provides
    @Singleton
    @Named("ReceivedCookiesInterceptor")
    internal fun receivedCookiesInterceptor(prefsUtil: PrefsUtil): Interceptor {
        return Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            if (originalResponse.request().url().url().toString().contains(BuildConfig.NODE_URL)
                    && originalResponse.headers("Set-Cookie").isNotEmpty()
                    && prefsUtil.getGlobalValue(PrefsUtil.KEY_GLOBAL_NODE_COOKIES).isEmpty()) {
                val cookies = originalResponse.headers("Set-Cookie").toHashSet()
                prefsUtil.setGlobalValue(PrefsUtil.KEY_GLOBAL_NODE_COOKIES, cookies)
            }
            originalResponse
        }
    }

    @Provides
    @Singleton
    @Named("AddCookiesInterceptor")
    internal fun addCookiesInterceptor(prefsUtil: PrefsUtil): Interceptor {
        return Interceptor { chain ->
            val cookies = prefsUtil.getGlobalValue(PrefsUtil.KEY_GLOBAL_NODE_COOKIES)
            if (cookies.isNotEmpty() && chain.request().url().url().toString().contains(BuildConfig.NODE_URL)) {
                val builder = chain.request().newBuilder()
                cookies.forEach {
                    builder.addHeader("Cookie", it)
                }
                chain.proceed(builder.build())
            } else {
                chain.proceed(chain.request())
            }
        }
    }


    @Singleton
    @Provides
    internal fun provideOkHttpClient(cache: Cache, @Named("timeout") timeout: Int,
                                     @Named("ReceivedCookiesInterceptor") receivedCookiesInterceptor: Interceptor,
                                     @Named("AddCookiesInterceptor") addCookiesInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(cache)
                .readTimeout(timeout.toLong(), TimeUnit.SECONDS)
                .writeTimeout(timeout.toLong(), TimeUnit.SECONDS)
                .addInterceptor(receivedCookiesInterceptor)
                .addInterceptor(addCookiesInterceptor)
                .addInterceptor(CacheForceInterceptorNoNet())
                .addNetworkInterceptor(CacheInterceptorOnNet())
                .addInterceptor(OkLogInterceptor.builder().withAllLogData().build())
                .addInterceptor(LoggingInterceptor.Builder()
                        .loggable(BuildConfig.DEBUG)
                        .setLevel(Level.BASIC)
                        .log(Log.INFO)
                        .request("Request")
                        .response("Response")
                        .build())
                .build()
    }

    @Provides
    @Singleton
    internal fun provideGson(): Gson {
        return GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) // if filed status_code need as statusCode
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create()
    }


    @Singleton
    @Named("NodeRetrofit")
    @Provides
    internal fun provideNodeRetrofit(gson: Gson, httpClient: OkHttpClient, errorManager: ErrorManager): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.NODE_URL)
                .client(httpClient)
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory(errorManager))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }

    @Singleton
    @Named("MatcherRetrofit")
    @Provides
    internal fun provideMatcherRetrofit(gson: Gson, httpClient: OkHttpClient, errorManager: ErrorManager): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.MATCHER_URL)
                .client(httpClient)
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory(errorManager))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }

    @Singleton
    @Named("DataFeedRetrofit")
    @Provides
    internal fun provideDataFeedRetrofit(gson: Gson, httpClient: OkHttpClient, errorManager: ErrorManager): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.DATA_FEED_URL)
                .client(httpClient)
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory(errorManager))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }


    @Singleton
    @Named("SpamRetrofit")
    @Provides
    internal fun provideSpamRetrofit(gson: Gson, httpClient: OkHttpClient, errorManager: ErrorManager): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.SPAM_URL)
                .client(httpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory(errorManager))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }

    @Singleton
    @Named("CoinomatRetrofit")
    @Provides
    internal fun provideCoinomatRetrofit(gson: Gson, httpClient: OkHttpClient, errorManager: ErrorManager): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.COINOMAT_URL)
                .client(httpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory(errorManager))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }


    @Singleton
    @Named("ApiRetrofit")
    @Provides
    internal fun provideApiRetrofit(gson: Gson, httpClient: OkHttpClient, errorManager: ErrorManager): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .client(httpClient)
                .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory(errorManager))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }

    @Singleton
    @Provides
    internal fun provideNodeService(@Named("NodeRetrofit") retrofit: Retrofit): NodeService {
        return retrofit.create(NodeService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideMatcherService(@Named("MatcherRetrofit") retrofit: Retrofit): MatcherService {
        return retrofit.create(MatcherService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideApiService(@Named("ApiRetrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideSpamService(@Named("SpamRetrofit") retrofit: Retrofit): SpamService {
        return retrofit.create(SpamService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideCoinomatService(@Named("CoinomatRetrofit") retrofit: Retrofit): CoinomatService {
        return retrofit.create(CoinomatService::class.java)
    }

    @Singleton
    @Provides
    internal fun provideDataFeedService(@Named("DataFeedRetrofit") retrofit: Retrofit): DataFeedService {
        return retrofit.create(DataFeedService::class.java)
    }

    @Named("timeout")
    @Provides
    internal fun provideTimeoutConstant(): Int {
        return 30
    }
}
