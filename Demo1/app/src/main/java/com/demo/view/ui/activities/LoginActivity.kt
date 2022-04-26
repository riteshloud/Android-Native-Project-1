package com.demo.view.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.*
import com.demo.utilities.Constants.Companion.FromLogin
import com.demo.utilities.Constants.Companion.PREF_DEVICE_TOKEN
import com.demo.utilities.Constants.Companion.UserToken
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseActivity
import com.demo.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    private var loginSignUpViewModel: LoginViewModel? = null
    var emailID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        init()

        setUp()

        setOnClickListener()

    }

    private fun init() {
        loginSignUpViewModel =
            ViewModelProvider(this, MyViewModelFactory(LoginViewModel(this@LoginActivity))).get(
                LoginViewModel::class.java
            )
    }

    private fun setUp() {
        getDeviceToken()
        addObserver()
    }

    private fun setOnClickListener() {



        btnLogin.setOnMyClickListener {
            emailID = ""
            checkLoginValidation()
        }
    }

    private fun checkLoginValidation() {
        when {
            edtEmail.text.isNullOrEmpty() -> {
                showToast(getString(R.string.validation_email))
            }
            !Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString().trim()).matches() -> {
                showToast(getString(R.string.validation_valid_email))
            }
            edtPassword.text.toString().trim().isEmpty() -> {
                showToast(getString(R.string.validation_password))
            }
            else -> {

//                val logIn = Intent(this, HomeActivity::class.java)
//                logIn.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                startActivity(logIn)
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                var deviceNameModel: String = Build.MANUFACTURER + "(" + Build.MODEL + ")"
                emailID = edtEmail.text.toString()
                loginSignUpViewModel?.callForLogIn(
                    edtEmail.text.toString(),
                    edtPassword.text.toString(),
                    Pref.getValue(this@LoginActivity, PREF_DEVICE_TOKEN, "")!!,
                    deviceNameModel
                )
            }
        }
    }

    private fun addObserver() {

        loginSignUpViewModel?.isLoading?.observe(this, Observer {
            it?.let {
                if (it) {
                    showProgressDialog()
                } else {
                    dismissProgressDialog()
                }
            }
        })

        loginSignUpViewModel?.responseError?.observe(this, Observer {
            it?.let {
                errorBody(it)
            }
        })

        loginSignUpViewModel?.loginResponse?.observe(this, Observer {
            it?.let {
                var loginParams = Bundle()
                loginParams.putString(Constants.EVENT_PARAM_EMAIL, emailID);
                loginParams.putString(Constants.EVENT_PARAM_CODE, "" + it.code);
                loginParams.putString(Constants.EVENT_PARAM_SUCCESS, "" + it.success);
                loginParams.putString(Constants.EVENT_PARAM_MESSAGE, "" + it.message);
                loginParams.putString(Constants.EVENT_PARAM_DEVICE_TYPE, Constants.deviceType);
                UTILS.logFacebookEvent(
                    this@LoginActivity,
                    Constants.EVENT_FACEBOOK_LOGIN,
                    loginParams
                ) // make login event
                UTILS.logFirebaseAnalyticsEvent(
                    this@LoginActivity,
                    Constants.EVENT_FIREBASE_LOGIN,
                    loginParams
                ) // make login event

                if (it.isVerifyTowfactorauth == "1") {
                    val logIn = Intent(this, VerifyOTPActivity::class.java)
                    logIn.putExtra(FromLogin, true)
                    logIn.putExtra(UserToken, it.user_token)
                    startActivity(logIn)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {

                    Pref.setValue(this@LoginActivity, Constants.prefUserData, Gson().toJson(it))
                    Pref.setValue(
                        this@LoginActivity,
                        Constants.prefAuthorizationToken,
                        "Bearer ${it.accessToken!!}"
                    )

//                    showToast(it.message!!)
                    val logIn = Intent(this, HomeActivity::class.java)
                    logIn.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(logIn)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        })
    }

    private fun getDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(object : OnCompleteListener<InstanceIdResult?> {
                override fun onComplete(@NonNull task: Task<InstanceIdResult?>) {
                    if (!task.isSuccessful) {
                        return
                    }
                    val token: String = task.result!!.token
                    Log.e("DeviceToken", "000 $token")
                    Pref.setValue(this@LoginActivity, PREF_DEVICE_TOKEN, token)
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}