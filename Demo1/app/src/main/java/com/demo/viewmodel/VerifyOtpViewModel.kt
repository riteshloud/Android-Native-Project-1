package com.demo.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyOtpViewModel(activity: Activity) : ViewModel() {

    //TODO: Verify OTP
    var verifyOtpResponse: MutableLiveData<ResponseBody>? = MutableLiveData()
    var verifyLoginOtpResponse: MutableLiveData<UserModel>? = MutableLiveData()

    //TODO: Send OTP
    var sendOtpResponse: MutableLiveData<SendOtpModel>? = MutableLiveData()
    var ResendOtpResponse: MutableLiveData<SendOtpModel>? = MutableLiveData()

    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity

    fun verifySendOtp(
        country_code: String,
        mobile_no: String,
        user_token: String,
        mobile_no_token: String,
        otp: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).verifySendOtp(
            country_code = country_code,
            mobile_no = mobile_no,
            user_token = user_token,
            mobile_no_token = mobile_no_token,
            otp = otp
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    verifyOtpResponse?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun verifySendOtp(
        user_token: String,
        mobile_no_token: String,
        otp: String,
        device_name:String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).verifySendOtp(
            user_token = user_token,
            mobile_no_token = mobile_no_token,
            otp = otp,
            device_name=device_name
        ).enqueue(object : Callback<UserModel> {
            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    verifyLoginOtpResponse?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun sendOtp(
        user_token: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).sendOtp(
            user_token = user_token
        ).enqueue(object : Callback<SendOtpModel> {
            override fun onFailure(call: Call<SendOtpModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(call: Call<SendOtpModel>, response: Response<SendOtpModel>) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    sendOtpResponse?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun callReSendOtp(country_code: String, mobile_no: String, user_token: String) {

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
                    ResendOtpResponse?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }


}