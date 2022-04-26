package com.demo.view.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.Constants
import com.demo.utilities.Constants.Companion.FromLogin
import com.demo.utilities.Pref
import com.demo.utilities.setOnMyClickListener
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseActivity
import com.demo.viewmodel.VerifyOtpViewModel
import kotlinx.android.synthetic.main.activity_verify_o_t_p.*
import org.json.JSONObject

class VerifyOTPActivity : BaseActivity() {

    lateinit var countryCode: String
    lateinit var phoneNumber: String
    lateinit var userToken: String
    lateinit var mobileNoToken: String
    private var verifyOtpViewModel: VerifyOtpViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_o_t_p)

        init()
        setUp()
        setOnClickListener()
    }

    private fun init() {

        verifyOtpViewModel =
            ViewModelProvider(
                this,
                MyViewModelFactory(VerifyOtpViewModel(this@VerifyOTPActivity))
            ).get(VerifyOtpViewModel::class.java)

    }

    private fun setUp() {

        addObserver()

        if (intent.getBooleanExtra(FromLogin, false)) {
            userToken = intent.getStringExtra(Constants.UserToken)!!
            verifyOtpViewModel?.sendOtp(userToken)
        } else {
            countryCode = intent.getStringExtra(Constants.CountryCode)!!
            phoneNumber = intent.getStringExtra(Constants.PhoneNumber)!!
            userToken = intent.getStringExtra(Constants.UserToken)!!
            mobileNoToken = intent.getStringExtra(Constants.MobileNoToken)!!
            txtPhoneNumber.text = "$countryCode $phoneNumber"
        }

    }

    private fun addObserver() {

        verifyOtpViewModel?.isLoading?.observe(this, Observer {
            it?.let {
                if (it) {
                    showProgressDialog()
                } else {
                    dismissProgressDialog()
                }
            }
        })

        verifyOtpViewModel?.responseError?.observe(this, Observer {
            it?.let {
                errorBody(it)
            }
        })

        verifyOtpViewModel?.verifyOtpResponse?.observe(this, Observer {
            val res = it.string()

            val jsonObject = JSONObject(res)

            showToast(jsonObject.optString("message"))

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        })

        verifyOtpViewModel?.verifyLoginOtpResponse?.observe(this, Observer {
            showToast(it.message!!)
            Pref.setValue(this@VerifyOTPActivity, Constants.prefUserData, Gson().toJson(it))
            Pref.setValue(
                this@VerifyOTPActivity,
                Constants.prefAuthorizationToken,
                "Bearer ${it.accessToken!!}"
            )
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        })

        verifyOtpViewModel?.sendOtpResponse?.observe(this, Observer {
            showToast(it.message!!)
            userToken = it.userToken!!
            mobileNoToken = it.mobileNoToken!!
            txtPhoneNumber.text = it.mobileNo
        })

        verifyOtpViewModel?.ResendOtpResponse?.observe(this, Observer {
            showToast(it.message!!)
            userToken = it.userToken!!
            mobileNoToken = it.mobileNoToken!!
        })

    }

    private fun callVerifyOtpForRegistration() {
        verifyOtpViewModel?.verifySendOtp(
            countryCode,
            phoneNumber,
            userToken,
            mobileNoToken,
            edtOTP.text.toString()
        )
    }

    private fun callVerifyOtpForLogin() {
        var deviceNameModel:String = Build.MANUFACTURER + "(" + Build.MODEL +")"
        verifyOtpViewModel?.verifySendOtp(
            userToken,
            mobileNoToken,
            edtOTP.text.toString(),
            deviceNameModel
        )
    }

    private fun setOnClickListener() {

        btnVerifyOtp.setOnMyClickListener {
            when {
                edtOTP.text.isNullOrEmpty() -> {
                    showToast(getString(R.string.validation_for_otp))
                }
                edtOTP.text.toString().length < 4 -> {
                    showToast(getString(R.string.validation_for_otp_valid))
                }
                else -> {

                    if (intent.getBooleanExtra(FromLogin, false)) {
                        callVerifyOtpForLogin()
                    } else {
                        callVerifyOtpForRegistration()
                    }

//                    val intent = Intent(this, LoginActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    startActivity(intent)
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                    finish()
                }
            }
        }

        txtCancel.setOnMyClickListener {
            onBackPressed()
        }

        tvResendOtp.setOnMyClickListener {
            if (intent.getBooleanExtra(FromLogin, false)) {
                verifyOtpViewModel?.sendOtp(userToken)
            } else {
                verifyOtpViewModel?.callReSendOtp(
                    countryCode,
                    phoneNumber,
                    userToken!!
                )
            }
        }
    }
}