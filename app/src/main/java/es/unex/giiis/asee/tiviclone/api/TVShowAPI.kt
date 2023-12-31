package es.unex.giiis.asee.tiviclone.api

import es.unex.giiis.asee.tiviclone.data.api.TvShow
import es.unex.giiis.asee.tiviclone.data.api.TvShowDetail
import es.unex.giiis.asee.tiviclone.data.api.TvShowPage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val service: TVShowAPI by lazy {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor())
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://www.episodate.com/api/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    retrofit.create(TVShowAPI::class.java)
}

fun getNetworkService() = service

interface TVShowAPI {

    @GET("most-popular")
    fun getShows(
        @Query("page") page: Int
    ): Call<TvShowPage>

    @GET("show-details")
    fun getShowDetail(
        @Query("q") id: Int
    ): Call<TvShowDetail>
}

class APIError(message: String, cause: Throwable?) : Throwable(message, cause)

interface APICallback {
    fun onCompleted(tvShows:List<TvShow?>)
    fun onError(cause: Throwable)
}