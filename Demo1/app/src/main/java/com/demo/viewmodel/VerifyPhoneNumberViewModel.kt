package com.demo.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyPhoneNumberViewModel(context: Activity) : ViewModel() {

    var verifyPhoneNumberResponse: MutableLiveData<SendOtpModel>? = MutableLiveData()
    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = context

    fun callSendOtp(country_code: String, mobile_no: String, user_token: String) {

        isLoading?.value = true
        ApiClient.getClient(context).sendOtp(
            country_code = country_code, mobile_no = mobile_no, user_token = user_token
        ).enqueue(object : Callback<SendOtpModel> {
            override fun onFailure(call: Call<SendOtpModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(call: Call<SendOtpModel>, response: Response<SendOtpModel>) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    verifyPhoneNumberResponse?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }
}