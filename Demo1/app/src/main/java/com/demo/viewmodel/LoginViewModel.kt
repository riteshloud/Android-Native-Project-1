package com.demo.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.utilities.Constants
import com.demo.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(activity: Activity) : ViewModel() {

    var loginResponse: MutableLiveData<UserModel>? = MutableLiveData()
    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity

    fun callForLogIn(
        email: String,
        password: String,
        device_token: String,
        deviceName: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).login(email, password,Constants.deviceType, device_token,deviceName)
            .enqueue(object : Callback<UserModel> {
                override fun onFailure(call: Call<UserModel>, t: Throwable) {
                    isLoading?.value = false
                }

                override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        loginResponse?.value = response.body()
                    } else {
                        responseError?.value = response.errorBody()
                    }
                }
            })
    }
}