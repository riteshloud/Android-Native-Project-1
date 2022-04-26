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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.Constants
import com.demo.view.adapter.ChatUserListAdapter
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseFragment
import com.demo.viewmodel.ChatListEndUserViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_bar_layout.*
import kotlinx.android.synthetic.main.fragment_chat_list_end_user.view.*
import kotlinx.android.synthetic.main.fragment_chat_list_end_user.view.edt_search
import kotlinx.android.synthetic.main.fragment_chat_list_end_user.view.pullToRefresh
import kotlinx.android.synthetic.main.fragment_chat_list_end_user.view.rvChatList
import kotlinx.android.synthetic.main.fragment_chat_list_end_user.view.tvEmptyUserList
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.json.JSONObject

class ChatListEndUserFragment : BaseFragment() {
    val TAG = ChatListEndUserFragment.javaClass.simpleName

    companion object {

        fun newInstance(
            enUserSecrete: String
        ) =
            ChatListEndUserFragment().apply {
                arguments = Bundle().apply {
                    putString(endUserSecretePARAM, enUserSecrete)
                }
            }

        //dont change value as this will use in API
        const val TYPE_USERS = "users"
        const val TYPE_SERVICES = "services"
        const val TYPE_ORDERS = "orders"
        var lastChatConversationID: String = "" // not in use anymore as we refreshing list on resume so
    }

    var rootView: View? = null
    var userType = ""
    var chatTypeText = ""
    var chatUserListDataViewModel: ChatListEndUserViewModel? = null
    var chatUserListModel: ChatUserListModel? = null
    var conversationModel: ConversationData? = null
    private var chatUserListAdapter: ChatUserListAdapter? = null
    var oldChatTimestamp: Long = 0

    //    private var chatUserListAdapter: ChatUserListAdapter? = null
//    var listConversation = ArrayList<ChatUserListModel.Conversation?>()
    var order_id = ""
    var service_id = ""
    var user_id = ""
    var msgId = ""
    var otherImage = ""
    var otherName = ""
    var isAdmin = "0"
    var myUserId: String = ""
    var endUserSecrete: String = ""

    val endUserSecretePARAM = "endUserSecrete"
    var receiver: BroadcastReceiver? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerNotificationReceiver();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            endUserSecrete = it.getString(endUserSecretePARAM).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_chat_list_end_user, container, false)
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
            MyViewModelFactory(ChatListEndUserViewModel(activity!!))
        ).get(ChatListEndUserViewModel::class.java)

    }

    private fun setUp() {
        Log.e(TAG, " endUserSecrete: $endUserSecrete")

        myUserId = userModel?.userDetails?.id!!.toString()

        addObserver()

//        chatUserListModel?.conversations?.clear()
        /*chatUserListDataViewModel?.getEndUserChatList(
            endUserSecrete,
            rootView!!.edt_search.text.toString(),
            0
        )*/
//        callEndUserChatListApi(0)
    }

    private fun callEndUserChatListApi(offset: Int) {
        if (offset == 0){
            rootView!!.rvChatList.adapter = null
        }
        chatUserListDataViewModel?.getEndUserChatList(
            endUserSecrete,
            rootView!!.edt_search.text.toString(),
            offset
        )
    }

    private fun addOnClickListener() {

        homeController.imgBack.setOnClickListener {
            homeController.onBackPressed()
        }

        rootView!!.rvChatList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rvChatList.canScrollVertically(1) && chatUserListModel!!.paginationEnded &&
                    chatUserListDataViewModel?.isLoading?.value == false
                ) {
                    /*chatUserListDataViewModel?.getEndUserChatList(
                        endUserSecrete,
                        rootView!!.edt_search.text.toString(),
                        chatUserListModel?.conversations?.size!!
                    )*/
                    callEndUserChatListApi(chatUserListModel?.conversations?.size!!)
                }
            }
        })
        /*rootView!!.edt_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (rootView!!.edt_search.text.trim().toString().equals("")) {
                    rootView!!.rvChatList.adapter = null
                    chatUserListDataViewModel?.chatUserListResponse?.value?.conversations?.clear()
                    chatUserListDataViewModel?.chatUserListResponse?.value = null
                    chatUserListModel = null
                    chatUserListDataViewModel?.getEndUserChatList(
                        "" + userModel?.userDetails?.id!!,
                        "false",
                        userType,
                        rootView!!.edt_search.text.toString(),
                        0
                    )
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }


        })*/

        rootView?.edt_search?.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    rootView!!.rvChatList.adapter = null
//                    chatUserListDataViewModel?.chatUserListResponse?.value?.conversations?.clear()
//                    chatUserListDataViewModel?.chatUserListResponse?.value = null
//                    chatUserListModel = null
                    /*chatUserListDataViewModel?.getEndUserChatList(
                        endUserSecrete,
                        rootView!!.edt_search.text.toString(),
                        0
                    )*/
                    callEndUserChatListApi(0)
                    return true
                }
                return false
            }
        })

        rootView!!.pullToRefresh.setOnRefreshListener {
            callEndUserChatListApi(0)
            rootView!!.pullToRefresh.isRefreshing = false
        }

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
            homeController.errorBody(it)
        })

        chatUserListDataViewModel!!.chatUserListResponse!!.observe(activity!!, Observer {

            it?.let {
                chatUserListModel = it

                /*(rootView!!.rvChatList.adapter as ChatUserListAdapter?)?.notifyDataSetChanged()
                    ?: run {
                        rootView!!.rvChatList.adapter = ChatUserListAdapter(
                            activity!!, userType, chatUserListModel?.conversations, this
                        )
                    }*/

                (rootView!!.rvChatList.adapter as ChatUserListAdapter?)?.notifyDataSetChanged()
                    ?:run {
                        try {
                            chatUserListAdapter = ChatUserListAdapter(
                                activity!!, chatUserListModel?.conversations, this
                            )
                            rootView!!.rvChatList.adapter = chatUserListAdapter
                            chatUserListAdapter?.setTimeDiff(0)
                            oldChatTimestamp = System.currentTimeMillis()
                        }catch (e:java.lang.Exception){
                            e.printStackTrace()
                            Log.e("ChatListEndUserFragment"," Exception: ${e.message} ")
                        }
                }

                upDateUserListUi()
            }

        })

    }

    private fun loadChatFragment() {
        homeController.onBackPressed() // will close chat screen to reduce pusher call
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
        userType = "" + conversationModel?.type

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
                chatTypeText = ""+it
            }

        } else if (userType == TYPE_SERVICES) {
            conversationModel!!.serviceSecret?.let {
                service_id = it
            }
            chatTypeText = ""+conversationModel!!.serviceName!!
            order_id = "0"
        } else {
            service_id = "0"
            order_id = "0"
        }
        if (!TextUtils.isEmpty(user_id)) {
            loadChatFragment()
        }

    }

    override fun onResume() {
        super.onResume()
        homeController.llToolbar.makeVisible()
        homeController.llBottomBar.makeGone()
        homeController.imgLogo.makeGone()
        homeController.imgBack.makeVisible()
        homeController.txtTitle.makeVisible()
        homeController.cardProfile.makeGone()

        homeController.txtTitle.text = getString(R.string.txt_end_user_chat)
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

        /*if (lastChatPosition >= 0 && chatUserListModel != null) {
            if (lastChatPosition >= chatUserListModel?.conversations!!.size) {
                return
            }

            var updatedItem = chatUserListModel?.conversations?.get(lastChatPosition)
            updatedItem?.messageDetailCount = "0"

            if (!TextUtils.isEmpty(lastChatMessage)) {
                updatedItem?.lastMessage = "" + lastChatMessage
            }

            if (!TextUtils.isEmpty(lastChatTimeAgo)) {
//                updatedItem?.time = lastChatTimeAgo
                updatedItem?.timestamp = lastChatTimeAgo

                // we only move to top if time have changed else just update at current position
                chatUserListModel?.conversations!!.removeAt(lastChatPosition)
                chatUserListModel?.conversations!!.add(0, updatedItem)
            } else {
                chatUserListModel?.conversations!![lastChatPosition] = updatedItem
            }
            (rootView!!.rvChatList.adapter as ChatUserListAdapter?)?.notifyDataSetChanged()
            Log.e("ChatListEndUserFragment", "Last chat updated at position : $lastChatPosition")
            lastChatMessage = ""
            lastChatTimeAgo = ""
            lastChatPosition = -1
        }*/

        callEndUserChatListApi(0)
        /*if (!TextUtils.isEmpty(lastChatConversationID)) {
            updateLastChatItem(lastChatConversationID)
        }*/
        lastChatConversationID = ""

    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return AnimationUtils.loadAnimation(
            activity,
            if (enter) android.R.anim.fade_in else android.R.anim.fade_out
        )
    }

    private fun registerNotificationReceiver() {
        Log.e(TAG, " Registering custom notification")
        var intentFilter = IntentFilter()
        intentFilter.addAction(Constants.NOTIFICATION_ACTION)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(mContext: Context?, intent: Intent?) {
                try {
                    var extras = intent!!.extras
                    var dataString = extras!![Constants.NOTIFICATION_DATA].toString()
                    var jsonData: JSONObject = JSONObject(dataString)
//                    var notificationType = "" + jsonData!!.getString("type")
//                    var messageDetailStr = jsonData?.get("conversation")
                    var messageDetailStr = jsonData?.get("message_detail")
                    var strConversationID = "" + jsonData.get("conversation_id").toString()
                    var messageData =
                        Gson().fromJson(messageDetailStr.toString(), ConversationData::class.java)
                    var tabType = "" + messageData?.type

                    Log.e(TAG, " strConversationID:- $strConversationID tabType:- $tabType")

                    if (TextUtils.isEmpty(strConversationID)) {
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
        if (chatUserListModel == null){
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

    override fun onDetach() {
        super.onDetach()
        lastChatConversationID = ""
        unRegisterNotificationReceiver()
    }

}