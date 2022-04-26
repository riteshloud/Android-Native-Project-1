package com.demo.view.service

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.demo.R
import com.demo.utilities.Constants
import com.demo.utilities.Pref
import com.demo.view.ui.activities.HomeActivity
import com.demo.view.ui.fragments.chats.ChatFragment
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Method


open class MyFirebaseMessagingService : FirebaseMessagingService() {
    var TAG = "MyFirebaseMessagingService"
    private val CHECK_OP_NO_THROW = "checkOpNoThrow"
    private val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from);

        if (remoteMessage.data.isNotEmpty()) {

            Log.e(TAG, " onMessageReceived remoteMessage data:  " + remoteMessage.data!!)
            val params = remoteMessage.data
            val jsonObject = JSONObject(params as Map<*, *>)
            val type = jsonObject.getString("type")
            val title = jsonObject.getString("title")
            val body = jsonObject.getString("body")
            var collapseKey = ""
            if (!type.equals(Constants.TYPE_GENERAL)) {
                collapseKey = jsonObject.getString("collapseKey")
            }
            Log.e(TAG, " Type=== : $type")
            Log.e(TAG, " collapse_key === : $collapseKey")
            var isChatWithCurrentUser = false

            if (type == Constants.TYPE_GENERAL) {
                if (jsonObject.getString("link").isNotEmpty()) {
                    sendNotificationURL(title, body, jsonObject)
                }
            } else if (type == Constants.TYPE_CHAT) {
                if (ChatFragment.isChatScreenOpened) {
                    try {
                        if (jsonObject.has("conversation_id")) {
                            var strConversationID = jsonObject.get("conversation_id").toString()
                            isChatWithCurrentUser = strConversationID == ChatFragment.msg_id
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                sendBroadcast(
                    Intent(Constants.NOTIFICATION_ACTION).putExtra(
                        Constants.NOTIFICATION_DATA,
                        jsonObject.toString()
                    )
                )


                //we handle chat notification on/off from app side to update message list real time
                var userModel = Pref.getUserModel(this)
                var strChatNotification = "" + userModel?.userDetails?.chatNotification
                Log.e(TAG, " strChatNotification=== : $strChatNotification")
                if (strChatNotification == "0") {
                    isChatWithCurrentUser = true
                }
            } else if (type == Constants.TYPE_TWO_FACTOR_AUTH) {
                //web auth dialog open

                if (isAppIsInBackground(this)) {
                    sendNotification(title, body, jsonObject)
                } else {
                    if (isNotificationEnabled(this)) {
                        sendBroadcast(
                            Intent(Constants.WEB_AUTH_ACTION).putExtra(
                                Constants.NOTIFICATION_DATA,
                                jsonObject.toString()
                            )
                        )
                    }
                }
            } else if (type == Constants.TYPE_TWO_FACTOR_AUTH_COMPLETE) {
                //web auth dialog open
                if (isNotificationEnabled(this)) {
                    sendBroadcast(
                        Intent(Constants.WEB_AUTH_ACTION).putExtra(
                            Constants.NOTIFICATION_DATA,
                            jsonObject.toString()
                        )
                    )
                }
            }
            if(type==Constants.TYPE_CHAT){
                 if (!isChatWithCurrentUser) {
//                sendNotification(title, body, jsonObject)
                    showNotificationGroup(title, body, type, collapseKey, jsonObject)
                }
            }
        }
    }

    private fun showNotificationGroup(
        title: String,
        msg: String,
        type: String,
        collapseKey: String,
        jsonObject: JSONObject
    ) {
//        value ++;
        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_logo)
        val pendingIntentID = System.currentTimeMillis().toInt()

        var intent: Intent? = null
        intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("data", jsonObject.toString())
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendingIntent: PendingIntent? = null
        pendingIntent = PendingIntent.getActivity(
            this,
            pendingIntentID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        var Notification_ID = collapseKey.toInt()
        var channelID = "channelId1"
        var channelName = "demo"

        val channelID1 = "channelId1"
        val channelID2 = "channelId2"
        val channelID3 = "channelId3"
        val channelID4 = "channelId4"

        val channelName1 = "demo1"
        val channelName2 = "demo2"
        val channelName3 = "demo3"
        val channelName4 = "demo4"

        var defaultDeviceUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification6Uri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.notification_6)
        val cashRegister01Uri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.cash_register_01)
        val notification5Uri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.notification_5)

        var defaultSoundUri: Uri? = null
        if (type == Constants.TYPE_CHAT) {
            channelID = channelID1
            channelName = channelName1
            defaultSoundUri = notification6Uri
        } else if (type == Constants.TYPE_NEW_ORDER) {
            channelID = channelID2
            channelName = channelName2
            defaultSoundUri = cashRegister01Uri
        } else if (type == Constants.TYPE_CANCEL_ORDER ||
            type == Constants.TYPE_COMPLETE_ORDER ||
            type == Constants.TYPE_DELIVERED_ORDER ||
            type == Constants.TYPE_EXTEND_ORDER_DATE ||
            type == Constants.TYPE_REVISION_REQUEST_ORDER ||
            type == Constants.TYPE_DISPUTE_ORDER
        ) {
            channelID = channelID3
            channelName = channelName3
            defaultSoundUri = notification5Uri
        } else {
            channelID = channelID4
            channelName = channelName4
            defaultSoundUri = defaultDeviceUri
        }

        val mBuilder =
            NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(title)
                .setLargeIcon(bitmap)
//                .setStyle(inboxStyle2)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
//                .setGroup(""+Notification_ID)
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
//                .setSound(defaultSoundUri)
//                .setDeleteIntent(getDeleteIntent()) // swipe listener

        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            notificationChannel.setSound(defaultSoundUri, attributes)
            mNotificationManager.createNotificationChannel(notificationChannel)
        } else {
            mBuilder.setSound(defaultSoundUri)
        }
        mNotificationManager.notify(Notification_ID, mBuilder.build())
    }

    //TODO: Show notification
    fun sendNotification(title: String, msg: String, jsonObject: JSONObject) {

        var intent: Intent? = null
        intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("data", jsonObject.toString())
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendingIntent: PendingIntent? = null
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.v("===sendnotification", "-")
        val channelId = Constants.CHANNEL_ID
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_logo)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.cancelAll()
        notificationManager.notify(0, notificationBuilder.build())
    }

    // TODO:show notification for url opening i browser
    fun sendNotificationURL(title: String, msg: String, jsonObject: JSONObject) {

        val notificationIntent = Intent(Intent.ACTION_VIEW)
        notificationIntent.data = Uri.parse(jsonObject.getString("link"))
        notificationIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        var pendingIntent: PendingIntent? = null
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.v("===sendnotification", "-")
        val channelId = Constants.CHANNEL_ID_GENERAL
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_logo)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.cancelAll()
        notificationManager.notify(0, notificationBuilder.build())
    }

    //TODO: Check App in background or not
    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            Log.e("context", "context : : " + context.packageName)
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                Log.e("context", "context else : : " + context.packageName)
                isInBackground = false
            }
        }
        return isInBackground
    }

    private fun getDeleteIntent(): PendingIntent? {
        val intent = Intent(this, OnCancelBroadcastReceiver::class.java)
        intent.action = "notification_cancelled"
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Log.e(TAG, " onNewToken newToken:  " + newToken)
    }

    private fun isNotificationEnabled(mContext: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mAppOps = mContext.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val appInfo = mContext.applicationInfo
            val pkg = mContext.applicationContext.packageName
            val uid = appInfo.uid
            val appOpsClass: Class<*>
            try {
                appOpsClass = Class.forName(AppOpsManager::class.java.name)
                val checkOpNoThrowMethod: Method = appOpsClass.getMethod(
                    CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                    String::class.java
                )
                val opPostNotificationValue: Field =
                    appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
                val value = opPostNotificationValue.get(Int::class.java) as Int
                return checkOpNoThrowMethod.invoke(
                    mAppOps, value, uid,
                    pkg
                ) as Int == AppOpsManager.MODE_ALLOWED
            } catch (ex: ClassNotFoundException) {
                ex.printStackTrace()
            }
            false
        } else {
            false
        }
    }

}