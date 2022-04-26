package com.demo.view.ui.activities

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.demo.R
import com.demo.model.*
import com.demo.utilities.*
import com.demo.view.adapter.MultiDeviceListAdapter
import com.demo.view.interfaces.OnListClickListener
import com.demo.view.interfaces.WebAuthCallback
import com.demo.view.service.ApiClient
import com.demo.view.ui.base.BaseActivity
import com.demo.view.ui.fragments.ProfileFragment
import com.demo.view.ui.fragments.chats.ChatFragment
import com.demo.view.ui.fragments.chats.ChatListFragment
import com.demo.view.ui.fragments.notification.NotificationListFragment
import com.demo.view.ui.fragments.projects.ProjectsFragment
import kotlinx.android.synthetic.main.bottom_bar_layout.*
import kotlinx.android.synthetic.main.dialog_country_list.*
import kotlinx.android.synthetic.main.dialog_multi_device_login_list.*
import kotlinx.android.synthetic.main.dialog_web_auth.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.cardProfile
import kotlinx.android.synthetic.main.toolbar_layout.imgProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : BaseActivity(), WebAuthCallback {
    private val CHECK_OP_NO_THROW = "checkOpNoThrow"
    private val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"

    val TAG = this.javaClass.simpleName
    var extras: Bundle? = null
    var dataString: String? = null
    var notificationType: String? = null
    var jsonData: JSONObject? = null
    var showLogoutDialog = false
    var multiLoginDialog: Dialog? = null
    var webAuthDialog: Dialog? = null
    var myWebAuthCallback: WebAuthCallback? = null
    var verifyClicked = "1" // 1= verify 0=cancel
    var validUntill = 60L
    var handler = Handler()
    var runnable: Runnable? = null
    private val webAuthReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.v("====received", "-")
            if (intent!!.hasExtra(Constants.NOTIFICATION_DATA)) {

                var extras = intent.extras
                Log.v("====received", "-" + extras!![Constants.NOTIFICATION_DATA].toString())

                try {
                    jsonData = JSONObject(extras!![Constants.NOTIFICATION_DATA].toString())
                    notificationType = jsonData!!.getString("type")
                    if (notificationType == Constants.TYPE_TWO_FACTOR_AUTH_COMPLETE) {
                        webAuthDialog?.let {
                            if (webAuthDialog != null && webAuthDialog!!.isShowing) {
                                webAuthDialog!!.dismiss()
                                runnable?.let {
                                    Log.v("==removecallback=", "-")
                                    handler.removeCallbacks(it)
                                    handler.removeCallbacksAndMessages(null)

                                }
                            }

                        }

                    }
                    if (notificationType == Constants.TYPE_TWO_FACTOR_AUTH) {
                        validUntill = jsonData!!.getString("valid_until").toLong()
                        Log.v("==valid untill", "-" + validUntill.toString())
                        if (webAuthDialog != null && webAuthDialog?.isShowing == true) {
                            Log.v("==valid untill delay", "-" + validUntill.toString())
                            webAuthDialog?.dismiss()
                            runnable?.let {
                                Log.v("==removecallback=", "-")
                                handler.removeCallbacks(it)
                                handler.removeCallbacksAndMessages(null)
                            }
                        }
                        showWebAuthDialog()

                    }

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        var isClearAllNotification = false // not in use
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        init()
        setUp()
        addListeners()
        registerWebAuthReceiver()

    }

    private fun registerWebAuthReceiver() {
        val filterSend = IntentFilter()
        filterSend.addAction(Constants.WEB_AUTH_ACTION)
        registerReceiver(webAuthReceiver, filterSend)
    }

    private fun removerWebAuthReceiver() {
        if (webAuthReceiver != null) {
            this!!.unregisterReceiver(webAuthReceiver)
//            webAuthReceiver = null
        }
        Log.e(TAG, " Unregistering webAuthReceiver")
    }

    private fun init() {
        Log.e(TAG, " current user user_id : " + userModel?.userDetails?.id)
        Log.e(TAG, " current user isPremiumUser : " + userModel?.userDetails?.isPremiumUser)
        Log.e(TAG, " current user chatNotification : " + userModel?.userDetails?.chatNotification)
        extras = intent.extras
        if (extras != null && extras!!["data"] != null) {
            dataString = extras!!["data"].toString()
            Log.e(TAG, " dataString: $dataString")
            jsonData = JSONObject(dataString)
            notificationType = jsonData!!.getString("type")
            Log.e(TAG, " notificationType: $notificationType")

            isClearAllNotification = true
            if (notificationType == Constants.TYPE_TWO_FACTOR_AUTH) {
                Log.v("==valid untill init", "-" + validUntill.toString())
                validUntill = jsonData!!.getString("valid_until").toLong()
                showWebAuthDialog()
            }
        }

        try {
            showLogoutDialog = userModel?.userDetails?.showMultiLogout!!
//            showLogoutDialog = true
            Log.e(TAG, " User totalAppLogin : " + userModel?.userDetails?.totalAppLogin!!)
            Log.e(TAG, " User userDevices size : " + userModel?.userDetails?.userDevices?.size!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        getHashKey()
    }
    /*private fun getHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }*/

    private fun setUp() {
        if (!TextUtils.isEmpty(notificationType)) {

            when (notificationType) {
                Constants.TYPE_CHAT -> {
//                    var messageDetailStr = jsonData?.get("conversation")
                    var strConversationID = jsonData?.get("conversation_id").toString()
                    var messageDetailStr = jsonData?.get("message_detail")
                    var messageData = Gson().fromJson(
                        messageDetailStr.toString(),
                        ConversationData::class.java
                    )
                    // here we need msg_id secrete but its id secrete so we will replace secret with conversation_id
                    //conversation_id is the msg_id secret
                    messageData.secret = "" + strConversationID

                    var msgId = "" + messageData.secret
                    var type = "" + messageData.type
                    var orderID = "" + messageData.orderNo
                    var serviceId = "" + messageData.serviceSecret
                    var isAdmin = "" + messageData.isAdmin
                    var chatTypeText = ""

                    if (type == ChatListFragment.TYPE_ORDERS) {
                        chatTypeText = "" + messageData.orderNo
                    } else if (type == ChatListFragment.TYPE_SERVICES) {
                        chatTypeText = "" + messageData.serviceName
                    }


                    var userID = ""
                    var otherImage = ""
                    var otherName = ""

                    if (messageData.toUser?.id != userModel?.userDetails?.id) {
                        userID = "" + messageData.toUser?.secret
                        otherImage = "" + messageData.toUser?.profilePhoto
                        otherName = "" + messageData.toUser?.name

                    } else if (messageData.fromUser?.id != userModel?.userDetails?.id) {
                        userID = "" + messageData.fromUser?.secret
                        otherImage = "" + messageData.fromUser?.profilePhoto
                        otherName = "" + messageData.fromUser?.name
                    }

                    loadFragment(
                        ChatFragment.newInstance(
                            msgId,
                            userID,
                            type,
                            chatTypeText,
                            orderID,
                            serviceId,
                            otherImage,
                            otherName,
                            isAdmin
                        ),
                        "ChatFragment",
                        this.javaClass.simpleName
                    )
                }
                /*Constants.TYPE_NEW_ORDER -> {
                    loadProjectsFragment()
                }*/
                Constants.TYPE_TWO_FACTOR_AUTH -> {
                    loadChatListFragment()
                }
                else -> {
                    loadNotificationListFragment()
                }
            }

        } else {
            loadChatListFragment()
        }
        setUserProfileData()

        NotificationManagerCompat.from(this).cancelAll()

        if (showLogoutDialog) {
            Handler(Looper.getMainLooper()).postDelayed({
                callprofileApi()
            }, 400)
        }

    }

    override fun onResume() {
        super.onResume()
        setUserProfileData()
    }

    private fun setUserProfileData() {
        Log.e(TAG, "profilePhoto URL: " + userModel?.userDetails?.profilePhoto)
        userModel?.userDetails?.profilePhoto?.let {
            imgProfile.loadFromUrl(it)
        }
    }

    fun loadChatListFragment() {
        // loadFragmentWithClearedStack this only call when you need to clear back fragments,
        // generally use for 1st loading fragment
        ChatListFragment().let { it2 ->
            loadFragmentWithClearedStack(
                it2,
                it2.javaClass.simpleName,
                this@HomeActivity.javaClass.simpleName
            )
        }
    }

    fun loadNotificationListFragment() {
        NotificationListFragment().let { it2 ->
            loadFragment(
                it2,
                it2.javaClass.simpleName,
                this@HomeActivity.javaClass.simpleName
            )
        }
    }

    fun loadProjectsFragment() {
        ProjectsFragment().let {
            loadFragment(
                it,
                it.javaClass.simpleName,
                this@HomeActivity.javaClass.simpleName
            )
        }
    }

    fun loadProfileFragment() {
        ProfileFragment().let {
            loadFragment(
                it,
                it.javaClass.simpleName,
                this@HomeActivity.javaClass.simpleName
            )
        }
    }

    private fun addListeners() {

        imgProject.setOnClickListener {
            val selected: Int = lLayout1.indexOfChild(it)
            if (selected == 0) return@setOnClickListener
            TransitionManager.beginDelayedTransition(lLayout1, ChangeBounds())
            lLayout1.removeView(it)
            lLayout1.addView(it, 0)
            lLayout1.removeView(llEmpty)
            lLayout1.addView(llEmpty, 1)
            imgProject.setImageResource(R.mipmap.ic_project_selected)
            imgMessage.setImageResource(R.mipmap.ic_message_unselected)
            imgNotification.setImageResource(R.mipmap.ic_notification_unselected)

            if (getCurrentFragment() != ProjectsFragment()) {
                loadProjectsFragment()
            }
        }

        imgMessage.setOnClickListener {
            val selected: Int = lLayout1.indexOfChild(it)
            if (selected == 0) return@setOnClickListener
            TransitionManager.beginDelayedTransition(lLayout1, ChangeBounds())
            lLayout1.removeView(it)
            lLayout1.addView(it, 0)
            lLayout1.removeView(llEmpty)
            lLayout1.addView(llEmpty, 1)
            imgMessage.setImageResource(R.mipmap.ic_message_selected)
            imgProject.setImageResource(R.mipmap.ic_project_unselected)
            imgNotification.setImageResource(R.mipmap.ic_notification_unselected)

            if (getCurrentFragment() != ChatListFragment()) {
                loadChatListFragment()
            }
        }

        imgNotification.setOnClickListener {
            val selected: Int = lLayout1.indexOfChild(it)
            if (selected == 0) return@setOnClickListener
            TransitionManager.beginDelayedTransition(lLayout1, ChangeBounds())
            lLayout1.removeView(it)
            lLayout1.addView(it, 0)
            lLayout1.removeView(llEmpty)
            lLayout1.addView(llEmpty, 1)
            imgNotification.setImageResource(R.mipmap.ic_notification_selected)
            imgMessage.setImageResource(R.mipmap.ic_message_unselected)
            imgProject.setImageResource(R.mipmap.ic_project_unselected)

            if (getCurrentFragment() != NotificationListFragment()) {
                loadNotificationListFragment()
            }
        }

        cardProfile.setOnMyClickListener {
            val selected: Int = lLayout1.indexOfChild(llEmpty)
            TransitionManager.beginDelayedTransition(lLayout1, ChangeBounds())
            lLayout1.removeView(llEmpty)
            lLayout1.addView(llEmpty, 0)
            imgNotification.setImageResource(R.mipmap.ic_notification_unselected)
            imgMessage.setImageResource(R.mipmap.ic_message_unselected)
            imgProject.setImageResource(R.mipmap.ic_project_unselected)

            loadProfileFragment()
        }
    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount <= 1) {
            if (!TextUtils.isEmpty(notificationType)) {
                loadChatListFragment()
                notificationType = null
            } else {
                finishAffinity()
            }
        } else {
            super.onBackPressed()
        }

        /*supportFragmentManager.findFragmentById(R.id.fragment_container).let {
            when (it) {
                is ChatListFragment -> {
                    finishAffinity()
                }
                else -> {
                    if (supportFragmentManager.backStackEntryCount <= 1) {
                        supportFragmentManager.popBackStack()
                    } else {
                        super.onBackPressed()
                    }
                }

            }
        }*/


    }

    fun showMultiLoginDevicesDialog() {

        val key_device_token = "device_token"
        val key_device_type = "device_type"

        var deviceList = userModel?.userDetails?.userDevices!!

        multiLoginDialog = Dialog(this@HomeActivity, R.style.dialogTheme)
        multiLoginDialog?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        multiLoginDialog?.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(this@HomeActivity, R.drawable.dialog_bg_white)
        )
        multiLoginDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        multiLoginDialog?.setCancelable(false)
        multiLoginDialog?.setContentView(R.layout.dialog_multi_device_login_list)
        multiLoginDialog?.show()

        multiLoginDialog?.rvMultiDevices?.layoutManager = LinearLayoutManager(this@HomeActivity)
        multiLoginDialog?.rvMultiDevices?.isNestedScrollingEnabled = false

        multiLoginDialog?.rvMultiDevices?.adapter =
            MultiDeviceListAdapter(context = this@HomeActivity,
                arrayDeviceList = deviceList,
                onListClickListener = object : OnListClickListener {
                    override fun onListClick(position: Int, obj: Any?) {
                        var deviceData = obj as UserDevices
                        deviceData.isSelected = !deviceData.isSelected
                        deviceList[position] = deviceData
                        multiLoginDialog?.rvMultiDevices?.adapter?.notifyDataSetChanged()
                    }

                    override fun onListClickSimple(position: Int, string: String?) {
                    }

                    override fun onListShow(position: Int, obj: Any?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })

        multiLoginDialog?.btnLogout?.setOnClickListener {

            var isCurrentDeviceSelected = false
            var isAnyDeviceSelected = false
            var currentDeviceToken =
                Pref.getValue(this@HomeActivity, Constants.PREF_DEVICE_TOKEN, "")
            var deviceItem = JSONObject()
            var deviceJsonArray = JSONArray()
            var deviceJsonObject = JSONObject()

            for (data in deviceList) {
                if (data?.isSelected!!) {
                    isAnyDeviceSelected = true
                    deviceItem = JSONObject()
                    var strToken = data?.deviceToken
                    var strDeviceType = data?.deviceType
                    deviceItem.put(key_device_token, strToken)
                    deviceItem.put(key_device_type, strDeviceType)

                    deviceJsonArray.put(deviceItem)
                    if (!isCurrentDeviceSelected) {
                        isCurrentDeviceSelected = currentDeviceToken == strToken
                    }
                }
            }

            if (isAnyDeviceSelected) {
//                deviceJsonObject.put("devices", deviceJsonArray)
                var deviceLoggedIn = (deviceList.size - deviceJsonArray.length())
                if (deviceLoggedIn > Constants.DEVICE_COUNT_FOR_LOGIN) {
                    var deviceTologout = deviceList.size - Constants.DEVICE_COUNT_FOR_LOGIN
                    showToast("Please select atleast ${deviceTologout} device(s) to logout")
                } else {
                    multipleLogOutCall(deviceJsonArray, isCurrentDeviceSelected)
                }
                //                multipleLogOutCall(deviceJsonObject, isCurrentDeviceSelected)
//                multiLoginDialog?.dismiss()
            } else {
                showToast("Please select device(s) to logout")
            }

        }
    }

    private fun createJsonRequestBody(vararg params: Pair<String, JSONArray>) =
        RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            JSONObject(mapOf(*params)).toString()
        )

    fun multipleLogOutCall(deviceJsonObject: JSONArray, isCurrentDeviceSelected: Boolean) {
        Log.e(TAG, " deviceJsonString: ${deviceJsonObject.toString()}")
        Log.e(TAG, " isCurrentDeviceSelected: $isCurrentDeviceSelected")
        showProgressDialog()

//        var params: HashMap<String, JSONArray> = HashMap()
////        var params: HashMap<String, JSONObject> = HashMap()
//        params["devices"] = deviceJsonObject
//        Log.e(TAG, " params: $params")
        ApiClient.getClient(this@HomeActivity)
            .multipleLogout(
                Pref.getPrefAuthorizationToken(this@HomeActivity), createJsonRequestBody(
                    "devices" to deviceJsonObject
                )
            )
            .enqueue(object : Callback<MultipleLogoutResponse> {
                override fun onFailure(call: Call<MultipleLogoutResponse>, t: Throwable) {
                    dismissProgressDialog()
                    showToast("Something went wrong, Please try again")
                }

                override fun onResponse(
                    call: Call<MultipleLogoutResponse>,
                    response: Response<MultipleLogoutResponse>
                ) {
                    dismissProgressDialog()
                    showToast("" + response.body()?.message)
                    if (response.isSuccessful) {
                        var loginParams = Bundle()

                        loginParams.putString(
                            Constants.EVENT_PARAM_DEVICE_TYPE,
                            Constants.deviceType
                        );
                        loginParams.putString(
                            Constants.EVENT_PARAM_DEVICE_TOKEN, Pref.getValue(
                                this@HomeActivity,
                                Constants.PREF_DEVICE_TOKEN, ""
                            )
                        );
                        loginParams.putString(
                            Constants.EVENT_PARAM_DEVICES,
                            deviceJsonObject.toString()
                        );

                        UTILS.logFirebaseAnalyticsEvent(
                            this@HomeActivity,
                            Constants.EVENT_FIREBASE_MULTIPLE_LOGOUT,
                            loginParams
                        )

                        var multiDeviceResponse = response.body()
                        multiLoginDialog?.dismiss()
                        if (isCurrentDeviceSelected) {
                            NotificationManagerCompat.from(this@HomeActivity).cancelAll()
                            Pref.deleteAll(this@HomeActivity)
                            val logInScreen = Intent(this@HomeActivity, LoginActivity::class.java)
                            logInScreen.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(logInScreen)
                            finish()
                            this@HomeActivity.overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        } else {
                            userModel?.userDetails?.totalAppLogin =
                                multiDeviceResponse?.totalAppLogin
                            userModel?.userDetails?.showMultiLogout =
                                multiDeviceResponse?.showMultiLogout!!
                            userModel?.userDetails?.userDevices =
                                multiDeviceResponse?.loggedInDevices
                            Pref.setValue(
                                this@HomeActivity,
                                Constants.prefUserData,
                                Gson().toJson(userModel)
                            )
                        }
                    } else {
                        Log.e(TAG, "" + response.errorBody().toString())

                        response.errorBody()?.let { errorBody(responseErrorBody = it) };
                    }
                }
            })
    }

    /**
     * This api is called due to check user is looged out from another device or not
     */

    private fun callprofileApi() {
        ApiClient.getClient(this@HomeActivity)
            .getProfileDetails(Pref.getPrefAuthorizationToken(this@HomeActivity))
            .enqueue(object : Callback<UserModel> {
                override fun onFailure(call: Call<UserModel>, t: Throwable) {


                }

                override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                    if (response.isSuccessful) {
                        userModel?.userDetails = response.body()!!.userDetails
                        showLogoutDialog = userModel?.userDetails?.showMultiLogout!!
                        Pref.setValue(
                            this@HomeActivity,
                            Constants.prefUserData,
                            Gson().toJson(userModel)
                        )
                        if (showLogoutDialog) {
                            showMultiLoginDevicesDialog()
                        }
                    } else {
                        Log.e(TAG, "" + response.errorBody().toString())
                        response.errorBody()?.let { errorBody(responseErrorBody = it) };
                    }
                }
            })
    }

    override fun onDestroy() {
        ChatFragment.isChatScreenOpened = false
        removerWebAuthReceiver()

        super.onDestroy()
    }

    override fun onWebAuthGranted() {
        Log.e(TAG, "Home: onWebAuthGranted")
        showToast("onWebAuthGranted!!!")
    }

    override fun onWebAuthDismiss() {
        Log.e(TAG, "Home: onWebAuthDismiss")
        showToast("onWebAuthDismiss!")
    }

    private fun callTwoFactorAuthApi() {
        ApiClient.getClient(this@HomeActivity)
            .vrifyTwoFactorAuth(Pref.getPrefAuthorizationToken(this@HomeActivity), verifyClicked)
            .enqueue(object : Callback<BaseResponse> {
                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {


                }

                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@HomeActivity, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.e(TAG, "" + response.errorBody().toString())
                        response.errorBody()?.let { errorBody(responseErrorBody = it) };
                    }
                }
            })
    }

    //TODO: Show WebAuth Dialog
    fun showWebAuthDialog(
//        webAuthCallback: WebAuthCallback
    ) {
//        if (webAuthDialog != null && webAuthDialog?.isShowing == true) {
//            webAuthDialog?.dismiss()
//        }

        runnable = Runnable {
            //  handler.removeCallbacks(runnable!!);

            Log.v("==validuntillnotdelay", "-" + validUntill.toString())
            if (webAuthDialog != null && webAuthDialog?.isShowing == true) {
                Log.v("==valid untill delay", "-" + validUntill.toString())
                webAuthDialog?.dismiss()
            }
        }
        var status = handler.postDelayed(runnable!!, validUntill * 1000)
        Log.v("===status==", "-" + status)
        webAuthDialog = Dialog(this, R.style.dialogTheme)
        webAuthDialog?.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        webAuthDialog?.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.dialog_bg_white)
        )
        webAuthDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        webAuthDialog?.setCancelable(false)
        webAuthDialog?.setContentView(R.layout.dialog_web_auth)
        webAuthDialog?.show()

        webAuthDialog?.btnNo?.setOnClickListener {
            verifyClicked = "0";
            callTwoFactorAuthApi()
            webAuthDialog?.dismiss()
            myWebAuthCallback?.onWebAuthDismiss()
        }

        webAuthDialog?.btnYes?.setOnClickListener {
            verifyClicked = "1";
            callTwoFactorAuthApi()
            webAuthDialog?.dismiss()
            myWebAuthCallback?.onWebAuthGranted()
        }


    }

}