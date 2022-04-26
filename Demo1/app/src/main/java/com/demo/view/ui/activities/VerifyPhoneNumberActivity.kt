package com.demo.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.Constants
import com.demo.view.interfaces.OnListClickListener
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseActivity
import com.demo.viewmodel.VerifyPhoneNumberViewModel
import kotlinx.android.synthetic.main.activity_verify_phone_number.*

class VerifyPhoneNumberActivity : BaseActivity() {

    private var verifyPhoneNumberViewModel: VerifyPhoneNumberViewModel? = null
    var registerDetail: RegisterDataModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone_number)

        init()

        setUp()

        setOnClickListener()

    }


    private fun init() {

        verifyPhoneNumberViewModel =
            ViewModelProvider(
                this,
                MyViewModelFactory(VerifyPhoneNumberViewModel(this@VerifyPhoneNumberActivity))
            ).get(
                VerifyPhoneNumberViewModel::class.java
            )

    }


    private fun setUp() {

        registerDetail =
            Gson().fromJson(intent.getStringExtra("registerData"), RegisterDataModel::class.java)

        addObserver()

    }


    private fun addObserver() {

        verifyPhoneNumberViewModel?.isLoading?.observe(this, Observer {
            it?.let {
                if (it) {
                    showProgressDialog()
                } else {
                    dismissProgressDialog()
                }
            }
        })

        verifyPhoneNumberViewModel?.responseError?.observe(this, Observer {
            it?.let {
                errorBody(it)
            }
        })

        verifyPhoneNumberViewModel?.verifyPhoneNumberResponse?.observe(this, Observer {
            showToast(it.message!!)

            val intent = Intent(this, VerifyOTPActivity::class.java)
            intent.putExtra(Constants.CountryCode, edtPhoneCode.text.toString())
            intent.putExtra(Constants.PhoneNumber, edtNumber.text.toString())
            intent.putExtra(Constants.UserToken, it.userToken)
            intent.putExtra(Constants.MobileNoToken, it.mobileNoToken)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        })

    }


    private fun setOnClickListener() {

        btnVerifyPhoneNumber.setOnMyClickListener {
            checkValidationForNumber()
        }

        txtSkipNow.setOnMyClickListener {
            onBackPressed()
        }

        edtPhoneCode.setOnMyClickListener {
            for (i in 0..registerDetail!!.countryCode!!.size - 1) {
                if (registerDetail!!.countryCode!!.get(i)!!.isSelected) {
                    Log.v("===Selected==Verify", "-" + registerDetail!!.countryCode!!.get(i)!!.name)
                }

            }
            setupCountryCode(edtPhoneCode, registerDetail!!.countryCode,
                onListClickListener = object : OnListClickListener {
                    override fun onListClick(position: Int, obj: Any?) {

                    }

                    override fun onListClickSimple(position: Int, string: String?) {

                        for (i in 0..registerDetail!!.countryCode!!.size - 1) {
                            if (position == i) {
                                registerDetail!!.countryCode!!.get(i)!!.isSelected = true

                            } else {
                                registerDetail!!.countryCode!!.get(i)!!.isSelected = false

                            }

                        }
                    }

                    override fun onListShow(position: Int, obj: Any?) {
                    }

                })
        }
    }


    private fun checkValidationForNumber() {
        when {
            edtPhoneCode.text.isNullOrEmpty() -> {
                showToast(getString(R.string.validation_for_country_code))
            }
            edtNumber.text.isNullOrEmpty() -> {
                showToast(getString(R.string.validation_for_phone_number))
            }
            edtNumber.text.toString().trim().length < 10 -> {
                showToast(getString(R.string.validation_for_valid_phone_number))
            }
            else -> {

                verifyPhoneNumberViewModel?.callSendOtp(
                    edtPhoneCode.text.toString(),
                    edtNumber.text.toString(),
                    registerDetail?.userToken!!
                )

//                val intent = Intent(this, VerifyOTPActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                intent.putExtra(Constants.CountryCode, edtPhoneCode.text.toString())
//                intent.putExtra(Constants.PhoneNumber, edtNumber.text.toString())
//                startActivity(intent)
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                finish()
            }
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}