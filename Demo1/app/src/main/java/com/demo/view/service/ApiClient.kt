package com.demo.view.service

import ConnectivityInterceptor
import android.app.Activity
import com.demo.utilities.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    //TODO: change pusher IDs @Constant class if base url changed
    //
   var BASE_URL = "Localhost:\\"  //Dev URL
    val PUSHER_CHAT_END_POINT = BASE_URL + "chat/auth"

    private var retrofit: Retrofit? = null
    fun getClient(context: Activity): ApiInterface {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .connectTimeout(Constants.connectionTimeOut, TimeUnit.SECONDS)
            .addInterceptor(ConnectivityInterceptor(context))
            .addInterceptor(logging)
            .readTimeout(Constants.readTimeOut, TimeUnit.SECONDS).build()

        if (retrofit == null) {
            // <-- this is the important line!
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        return retrofit!!.create(ApiInterface::class.java)
    }

}
