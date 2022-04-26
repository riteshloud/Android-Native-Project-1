package com.demo.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.demo.R
import com.demo.utilities.Constants
import com.demo.utilities.Pref
import com.demo.utilities.start
import com.demo.view.ui.base.BaseActivity

class SplashActivity : BaseActivity() {
    var TAG = "SplashActivity"
    var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (Pref.getValue(this@SplashActivity, Constants.prefUserData, null).isNullOrEmpty()) {
                start<LoginActivity>()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
                // removed onboarding screen on first time app lunch
                /*if (Pref.getValue(this@SplashActivity, Constants.prefOnBoardingScreen, false)) {
                    start<LoginActivity>()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    start<OnBoardingIntroActivity>()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }*/
            } else {
                val homeIntent = Intent(this, HomeActivity::class.java)
//                start<HomeActivity>()
                startActivity(homeIntent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }, 1000)

    }
}