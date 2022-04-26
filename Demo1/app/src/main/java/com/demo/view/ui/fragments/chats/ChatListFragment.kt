package com.demo.view.ui.fragments.chats

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.Constants
import com.demo.utilities.Constants.Companion.NOTIFICATION_ACTION
import com.demo.view.adapter.ChatUserListAdapter
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseFragment
import com.demo.viewmodel.ChatUserListDataViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_bar_layout.*
import kotlinx.android.synthetic.main.fragment_chat_list.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.json.JSONObject


class ChatListFragment : BaseFragment() {
    var TAG = ChatListFragment.javaClass.simpleName
    var receiver: BroadcastReceiver? = null

    companion object {
        //dont change value as this will use in API
        const val TYPE_USERS = "users"
        const val TYPE_SERVICES = "services"
        const val TYPE_ORDERS = "orders"
        var lastChatConversationID: String =
            "" // not in use anymore as we refreshing list on resume so
    }

    var rootView: View? = null
    var userType = TYPE_USERS
    var chatUserListDataViewModel: ChatUserListDataViewModel? = null
    var chatUserListModel: ChatUserListModel? = null
    var conversationModel: ConversationData? = null
    var oldChatTimestamp: Long = 0
    private var chatUserListAdapter: ChatUserListAdapter? = null
    var order_id = ""
    var service_id = ""
    var user_id = ""
    var msgId = ""
    var otherImage = ""
    var otherName = ""
    var isAdmin = "0"
    var myUserId: String = ""
    var chatTypeText: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerNotificationReceiver();
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_chat_list, container, false)
            init()
            setUp()
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addOnClickListener()

    }

    private fun init() {

        chatUserListDataViewModel = ViewModelProvider(
            activity!!,
            MyViewModelFactory(ChatUserListDataViewModel(activity!!))
        ).get(ChatUserListDataViewModel::class.java)

    }

    private fun setUp() {

        myUserId = userModel?.userDetails?.id!!.toString()

        addObserver()
//        chatUserListModel?.conversations?.clear()
//        callAllUserForChatApi(0)
    }

    private fun callAllUserForChatApi(offset: Int) {
        if (offset == 0) {
            // to load fresh list else will added pagination list
            rootView!!.rvChatList.adapter = null
        }
        chatUserListDataViewModel?.getAllUserForChat(
            "" + userModel?.userDetails?.id!!,
            "false",
            userType,
            rootView!!.edt_search.text.toString(),
            offset
        )
    }

    private fun addOnClickListener() {
        rootView!!.llSeller.setOnMyClickListener {
            if (userType == TYPE_USERS) return@setOnMyClickListener
            userType = TYPE_USERS
            setAllUnselected(0)
//            chatUserListDataViewModel?.chatUserListResponse?.value?.conversations?.clear()
//            chatUserListDataViewModel?.chatUserListResponse?.value = null
//            chatUserListModel = null
            /*chatUserListDataViewModel?.getAllUserForChat(
                "" + userModel?.userDetails?.id!!,
                "false",
                userType,
                rootView!!.edt_search.text.toString(),
                0
            )*/
            rootView!!.edt_search.text.clear()
            callAllUserForChatApi(0)
        }

        rootView!!.llServices.setOnMyClickListener {
            if (userType == TYPE_SERVICES) return@setOnMyClickListener
            userType = TYPE_SERVICES
            setAllUnselected(1)
            rootView!!.edt_search.text.clear()
//            rootView!!.rvChatList.adapter = null
//            chatUserListDataViewModel?.chatUserListResponse?.value?.conversations?.clear()
//            chatUserListDataViewModel?.chatUserListResponse?.value = null
//            chatUserListModel = null
            /*chatUserListDataViewModel?.getAllUserForChat(
                "" + userModel?.userDetails?.id!!,
                "false",
                userType,
                rootView!!.edt_search.text.toString(),
                0
            )*/
            callAllUserForChatApi(0)
        }

        rootView!!.llOrders.setOnMyClickListener {
            if (userType == TYPE_ORDERS) return@setOnMyClickListener
            userType = TYPE_ORDERS
            setAllUnselected(2)
            rootView!!.edt_search.text.clear()
//            rootView!!.rvChatList.adapter = null
//            chatUserListDataViewModel?.chatUserListResponse?.value?.conversations?.clear()
//            chatUserListDataViewModel?.chatUserListResponse?.value = null
//            chatUserListModel = null
            /*chatUserListDataViewModel?.getAllUserForChat(
                "" + userModel?.userDetails?.id!!,
                "false",
                userType,
                rootView!!.edt_search.text.toString(),
                0
            )*/
            callAllUserForChatApi(0)
        }

        rootView!!.rvChatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rvChatList.canScrollVertically(1) && chatUserListModel!!.paginationEnded &&
                    chatUserListDataViewModel?.isLoading?.value == false
                ) {
                    /*chatUserListDataViewModel?.getAllUserForChat(
                        "" + userModel?.userDetails?.id!!,
                        "false",
                        userType,
                        rootView!!.edt_search.text.toString(),
                        chatUserListModel?.conversations?.size!!
                    )*/
                    callAllUserForChatApi(chatUserListModel?.conversations?.size!!)
                }
            }
        })

        rootView?.edt_search?.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    rootView!!.rvChatList.adapter = null
//                    chatUserListDataViewModel?.chatUserListResponse?.value?.conversations?.clear()
//                    chatUserListDataViewModel?.chatUserListResponse?.value = null
//                    chatUserListModel = null
                    /*chatUserListDataViewModel?.getAllUserForChat(
                        "" + userModel?.userDetails?.id!!,
                        "false",
                        userType,
                        rootView!!.edt_search.text.toString(),
                        0
                    )*/
                    callAllUserForChatApi(0)
                    return true
                }
                return false
            }
        })

        rootView!!.pullToRefresh.setOnRefreshListener(OnRefreshListener {
            callAllUserForChatApi(0)
            rootView!!.pullToRefresh.isRefreshing = false
        })

    }

    private fun addObserver() {

        chatUserListDataViewModel!!.isLoading!!.observe(activity!!, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })

        chatUserListDataViewModel!!.responseError!!.observe(activity!!, Observer {
            Log.e("ChatListFragment", " chatUserListDataViewModel responseError called: ")
            if (it == null) {
                return@Observer
            }
            homeController.errorBody(it)
            chatUserListDataViewModel?.responseError?.value = null
        })

        chatUserListDataViewModel!!.chatUserListResponse!!.observe(activity!!, Observer {

            it?.let {
                chatUserListModel = it

                (rootView!!.rvChatList.adapter as ChatUserListAdapter?)?.notifyDataSetChanged()
                    ?: run {
                        try {
                            chatUserListAdapter = ChatUserListAdapter(
                                activity!!, chatUserListModel?.conversations, this
                            )
                            rootView!!.rvChatList.adapter = chatUserListAdapter
                            chatUserListAdapter?.setTimeDiff(0)
                            oldChatTimestamp = System.currentTimeMillis()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            Log.e("ChatListFragment", " Exception: ${e.message} ")
                        }
                    }
                upDateUserListUi()
            }

        })

    }

    private fun registerNotificationReceiver() {
        //this event will come from notification and ChatScreen if opened,
        // so make sure same keys passing from both place
        Log.e(TAG, " Registering custom notification")
        var intentFilter = IntentFilter()
        intentFilter.addAction(NOTIFICATION_ACTION)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(mContext: Context?, intent: Intent?) {
                try {
                    var extras = intent!!.extras
                    var dataString = extras!![Constants.NOTIFICATION_DATA].toString()
//                    Log.e(TAG," onReceive dataString : "+dataString)
                    var jsonData: JSONObject = JSONObject(dataString)
//                    var notificationType = "" + jsonData!!.getString("type")
//                    var messageDetailStr = jsonData?.get("conversation")
                    var messageDetailStr = jsonData?.get("message_detail")
                    var strConversationID = "" + jsonData.get("conversation_id").toString()
                    var messageData =
                        Gson().fromJson(messageDetailStr.toString(), ConversationData::class.java)
                    var tabType = "" + messageData?.type

                    Log.e(TAG, " strConversationID:- $strConversationID tabType:- $tabType")

                    if (TextUtils.isEmpty(strConversationID) || TextUtils.isEmpty(tabType) || userType != tabType) {
                        return
                    }

                    // without runOnUiThread adapter not update list
                    if (activity != null && !activity!!.isFinishing) {
                        activity!!.runOnUiThread {
                            if (chatUserListModel != null) {
                                updateUserListItem(messageData, strConversationID)
                            }
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, " onReceive Exception : " + e.message)
                }

            }
        }
        activity!!.registerReceiver(receiver!!, intentFilter)
    }

    private fun unRegisterNotificationReceiver() {
        if (receiver != null) {
            activity!!.unregisterReceiver(receiver)
            receiver = null
        }
        Log.e(TAG, " Unregistering custom notification")
    }

    @Synchronized
    fun updateUserListItem(messageData: ConversationData?, conversation_id: String) {
        var conversationModel: ConversationData? = null
        var isNewMessage = true
        var foundedPosition = -1

        for ((positionData, data: ConversationData?) in chatUserListModel?.conversations!!.withIndex()) {
            if (data?.secret == conversation_id) {
                conversationModel = data
                foundedPosition = positionData
                isNewMessage = false
                break
            }
        }

        if (isNewMessage) {
            conversationModel = messageData
        }

        if (messageData?.fromUser?.id.toString() == myUserId) {
            conversationModel?.messageDetailCount = "0"
        } else {
            conversationModel?.messageDetailCount = messageData?.messageDetailCount
        }
        var strMessage = ""
        strMessage = if (messageData?.is_service_preview!!) {
            "Shared a service"
        } else {
            if (messageData?.attachment == 1) {
                "Shared a file"
            } else {
//                messageData?.message = "" + messageData?.lastMessage
                "" + messageData?.message
            }
        }
        conversationModel?.lastMessage = strMessage
        conversationModel?.timestamp = "" + messageData.timestamp

        updateTime() // must call before update this object to manage time properly
        conversationModel?.timestamp = "" + oldChatTimestamp

        if (isNewMessage) {
            // here we need to replace secret with conversation_id backend is giving different value
            conversationModel?.secret = "" + conversation_id
            chatUserListModel?.conversations!!.add(0, conversationModel)
        } else {
            chatUserListModel?.conversations!!.removeAt(foundedPosition)
            chatUserListModel?.conversations!!.add(0, conversationModel)
        }
        chatUserListAdapter?.notifyDataSetChanged()

    }

    private fun updateTime() {
        var now = System.currentTimeMillis()
        Log.e(TAG, " Now : $now  , old $oldChatTimestamp")
        var lastMsgTimeDiff: Long = System.currentTimeMillis().minus(oldChatTimestamp)
        Log.e(TAG, "lastMsgTimeDiff: $lastMsgTimeDiff")
        chatUserListAdapter?.setTimeDiff(lastMsgTimeDiff!!)
        chatUserListAdapter?.notifyDataSetChanged()
        chatUserListAdapter?.setTimeDiff(0)
        oldChatTimestamp = now
    }

    private fun updateLastChatItem(conversation_id: String) {
        var conversationModel: ConversationData? = null
        if (chatUserListModel == null) {
            return
        }
        for ((positionData, data: ConversationData?) in chatUserListModel?.conversations!!.withIndex()) {
            if (data?.secret == conversation_id) {
                conversationModel = data
                conversationModel.messageDetailCount = "0"
                chatUserListModel?.conversations!![positionData] = conversationModel
                chatUserListAdapter?.notifyDataSetChanged()
                break
            }
        }
    }

    private fun loadChatFragment() {
        homeController.loadFragment(
            ChatFragment.newInstance(
                msgId,
                user_id,
                userType,
                chatTypeText,
                order_id,
                service_id,
                otherImage,
                otherName,
                isAdmin
            ),
            "ChatFragment",
            this.javaClass.simpleName
        )
    }

    private fun upDateUserListUi() {

        if (chatUserListModel?.conversations?.size!! > 0) {
            rootView!!.rvChatList.makeVisible()
            rootView!!.tvEmptyUserList.makeGone()
        } else {
            rootView!!.rvChatList.makeGone()
            rootView!!.tvEmptyUserList.makeVisible()
        }

    }

    private fun setAllUnselected(stage: Int) {

        rootView!!.imgSeller.setImageResource(R.mipmap.ic_seller_unselected)
        rootView!!.tvSeller.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))

        rootView!!.imgServices.setImageResource(R.mipmap.ic_services_unselected)
        rootView!!.tvServices.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))

        rootView!!.imgOrders.setImageResource(R.mipmap.ic_orders_unselected)
        rootView!!.tvOrders.setTextColor(ContextCompat.getColor(activity!!, R.color.light_gray))

        if (stage == 0) {
            rootView!!.imgSeller.setImageResource(R.mipmap.ic_seller_selected)
            rootView!!.tvSeller.setTextColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.colorAccent
                )
            )
        } else if (stage == 1) {
            rootView!!.imgServices.setImageResource(R.mipmap.ic_services_selected)
            rootView!!.tvServices.setTextColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.colorAccent
                )
            )
        } else if (stage == 2) {
            rootView!!.imgOrders.setImageResource(R.mipmap.ic_orders_selected)
            rootView!!.tvOrders.setTextColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.colorAccent
                )
            )
        }

    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)
        user_id = ""
        otherImage = ""
        otherName = ""
        order_id = ""
        msgId = ""
        isAdmin = "0"
        service_id = ""
        chatTypeText = ""

        conversationModel = chatUserListModel?.conversations!![position]
        lastChatConversationID = "" + conversationModel?.secret!!
        val collapseKey = "" + conversationModel?.id!! // id is collapseKey for notification

        conversationModel!!.secret!!.let {
            msgId = it
        }

        conversationModel!!.isAdmin!!.let {
            isAdmin = it
        }

        if (conversationModel?.isAdmin == "1") {
            if (conversationModel!!.fromAdmin != null) {
                otherName = conversationModel!!.fromAdmin!!.name.toString()
                user_id = conversationModel!!.fromAdmin!!.secret.toString()
                otherImage = conversationModel!!.fromAdmin!!.profilePhoto.toString()
            }

        } else {

            if (conversationModel!!.toUser != null && conversationModel!!.toUser!!.id.toString() != myUserId) {

                user_id = conversationModel!!.toUser!!.secret.toString()

                conversationModel!!.toUser!!.name!!.let {
                    otherName = it
                }

                conversationModel!!.toUser!!.profilePhoto.let {
                    if (it != null) {
                        otherImage = it
                    }
                }

            } else if (conversationModel!!.fromUser!!.id.toString() != myUserId) {

                user_id = conversationModel!!.fromUser!!.secret.toString()

                conversationModel!!.fromUser!!.name!!.let {
                    otherName = it
                }

                conversationModel!!.fromUser!!.profilePhoto.let {
                    if (it != null) {
                        otherImage = it
                    }
                }
            }
        }

        if (userType == TYPE_ORDERS) {
            conversationModel!!.serviceSecret?.let {
                service_id = it
            }
            conversationModel!!.orderNo?.let {
                order_id = it
                chatTypeText = "" + it
            }

        } else if (userType == TYPE_SERVICES) {
            conversationModel!!.serviceSecret?.let {
                service_id = it
            }
            chatTypeText = "" + conversationModel!!.serviceName!!
            order_id = "0"
        } else {
            service_id = "0"
            order_id = "0"
        }
        if (!TextUtils.isEmpty(user_id)) {
            loadChatFragment()
        }

        //clear notification from tray if available for this conversation
        if (!TextUtils.isEmpty(collapseKey)) {
            NotificationManagerCompat.from(activity!!).cancel(collapseKey.toInt())
        }

    }

    override fun onResume() {
        super.onResume()
        homeController.llToolbar.makeVisible()
        homeController.llBottomBar.makeVisible()
        homeController.imgLogo.makeVisible()
        homeController.imgBack.makeGone()
        homeController.txtTitle.makeVisible()
        homeController.cardProfile.makeVisible()

        homeController.txtTitle.text = getString(R.string.txt_messages)
        homeController.imgMessage?.let {
            val selected: Int = homeController.lLayout1.indexOfChild(it)
            if (selected != 0) {
                TransitionManager.beginDelayedTransition(homeController.lLayout1, ChangeBounds())
                homeController.lLayout1.removeView(it)
                homeController.lLayout1.addView(it, 0)
                homeController.llEmpty?.let {
                    homeController.lLayout1.removeView(it)
                    homeController.lLayout1.addView(it, 1)
                }
                homeController.imgMessage.setImageResource(R.mipmap.ic_message_selected)
                homeController.imgProject.setImageResource(R.mipmap.ic_project_unselected)
                homeController.imgNotification.setImageResource(R.mipmap.ic_notification_unselected)
            }
        }

        callAllUserForChatApi(0)
        /*if (!TextUtils.isEmpty(lastChatConversationID)) {
            updateLastChatItem(lastChatConversationID)
        }*/
        lastChatConversationID = ""

        // need to do this when chat open from notification that time flag remains true so
        ChatFragment.isChatScreenOpened = false
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return AnimationUtils.loadAnimation(
            activity,
            if (enter) android.R.anim.fade_in else android.R.anim.fade_out
        )
    }

    override fun onDetach() {
        super.onDetach()
        lastChatConversationID = ""
        unRegisterNotificationReceiver()
    }

}