package com.demo.view.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.demo.view.ui.activities.HomeActivity

class OnCancelBroadcastReceiver : BroadcastReceiver() {

    var TAG = "OnCancelBroadcastReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent!!.action
        Log.e(TAG, "onReceive :  $action")
        if (action == "notification_cancelled") {
            HomeActivity.isClearAllNotification = true
        }
    }
}