package com.demo.view.ui.fragments.chats

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.*
import com.demo.utilities.permissioncheck.PermissionCheck
import com.demo.view.adapter.ChatRoomAdapter
import com.demo.view.adapter.HighLightArrayAdapter
import com.demo.view.service.ApiClient
import com.demo.view.service.MyViewModelFactory
import com.demo.view.service.NetworkUtil
import com.demo.view.ui.activities.WebViewActivity
import com.demo.view.ui.base.BaseFragment
import com.demo.viewmodel.ChatViewModel
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannel
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionState
import com.pusher.client.util.HttpAuthorizer
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_upload_file.view.*
import kotlinx.android.synthetic.main.fragment_add_template.*
import kotlinx.android.synthetic.main.fragment_add_template.view.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.fragment_report_spam.view.*
import kotlinx.android.synthetic.main.fragment_view_service.view.*
import kotlinx.android.synthetic.main.fragment_view_template.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ChatFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {

    private var offSet: Int = 0
    val TAG = ChatFragment.javaClass.simpleName
    var rootView: View? = null
    var fromBlockuser = false

    //Development or LIVE change from Constant
    private val PUSHER_APP_ID = Constants.PUSHER_APP_ID
    private val PUSHER_APP_SECRET = Constants.PUSHER_APP_SECRET
    private val PUSHER_APP_KEY = Constants.PUSHER_APP_KEY
    private val PUSHER_APP_CLUSTER = Constants.PUSHER_APP_CLUSTER
    private val PUSHER_APP_TIMEOUT = Constants.PUSHER_ACTIVITY_TIMEOUT
    private val channel = Constants.PUSHER_APP_CHANNEL
    private val eventName = Constants.PUSHER_APP_EVENT_NAME
    var pusher: Pusher? = null
    var options: PusherOptions? = null
    var subscribedChannel: PrivateChannel? = null
//    var SOCKET_ID = ""

    var adapterTemplate: HighLightArrayAdapter? = null
    var adapterService: HighLightArrayAdapter? = null
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var mSelectionPath: String? = null
    private var selectedType: Int = 0
    var photoFile: File? = null

    val serviceIdPARAM = "service_id"
    val typePARAM = "type"
    val chatTypeTextPARAM = "chatTypeText"
    val orderIdPARAM = "order_id"
    val userIdPARAM = "user_id"
    val messageIdPARAM = "message_id"
    val otherImagePARAM = "otherImageUrl"
    val otherNamePARAM = "otherName"
    val isAdminPARAM = "isAdmin"

    var chatViewModel: ChatViewModel? = null
    var fetchMessageDataModel: FetchMessageDataModel? = null
    var type = ""
    var chatTypeText = ""
    var service_id = ""
    var order_id = ""
    var user_id = ""
    var otherImageUrl: String = ""
    var otherName: String = ""
    var isAdmin: String = "0"

    var selectedTemplateString: String = ""
    var selectedServiceString: String = ""
    var isSpam: Boolean = false
    var isBlocked: Boolean = false
    var isConversationBlocked: Boolean = false
    var isDeleted: Boolean = false

    var chatArraylist: ArrayList<ConversationData>? = ArrayList()
    var chatLinearLayoutManager: LinearLayoutManager? = null
    var chatRoomAdapter: ChatRoomAdapter? = null
    var oldChatTimestamp: Long = 0
    var templateList = ArrayList<String>()
    var templateModelList: ArrayList<TemplateResponse.Template?> = ArrayList()
    var serviceModelList: ArrayList<ServiceResponseModel.Service?> = ArrayList()
    var selectedService: ServiceResponseModel.Service? = null
    var serviceList = ArrayList<String>()

    var isServiceMessage = false
    var showBottomScrollMessage = false
    var isFABOpen = false
    var needToClearMsgCounter = false
    var popup: PopupMenu? = null

    companion object {

        var isChatScreenOpened = false
        var msg_id: String = ""

        fun newInstance(
            message_id: String,
            user_id: String,
            type: String,
            chatTypeText: String,
            order_id: String,
            service_id: String,
            otherImage: String,
            otherName: String,
            isAdmin: String
        ) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(messageIdPARAM, message_id)
                    putString(userIdPARAM, user_id)
                    putString(typePARAM, type)
                    putString(chatTypeTextPARAM, chatTypeText)
                    putString(orderIdPARAM, order_id)
                    putString(serviceIdPARAM, service_id)
                    putString(otherImagePARAM, otherImage)
                    putString(otherNamePARAM, otherName)
                    putString(isAdminPARAM, isAdmin)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            msg_id = it.getString(messageIdPARAM).toString()
            type = it.getString(typePARAM).toString()
            chatTypeText = it.getString(chatTypeTextPARAM).toString()
            service_id = it.getString(serviceIdPARAM).toString()
            user_id = it.getString(userIdPARAM).toString()
            order_id = it.getString(orderIdPARAM).toString()
            otherImageUrl = it.getString(otherImagePARAM).toString()
            otherName = it.getString(otherNamePARAM).toString()
            isAdmin = it.getString(isAdminPARAM).toString()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_chat, container, false)
            init()
            setup()
        }
        return rootView
    }

    private fun setup() {
        popup = PopupMenu(homeController, rootView!!.iv_overflow)
        popup!!.setOnMenuItemClickListener(this)
        popup!!.inflate(R.menu.popup_menu)
        rootView!!.chatSwipeToRefresh.isEnabled = false
        Log.e(TAG, "other user secrete user_id: $user_id")
        Log.e(TAG, "msg_id: $msg_id")
        Log.e(TAG, "type: $type")
        Log.e(TAG, "service_id: $service_id")
        Log.e(TAG, "user_id: $user_id")
        Log.e(TAG, "order_id: $order_id")
        Log.e(TAG, "isAdmin: $isAdmin")
        Log.e(TAG, "chatTypeText: $chatTypeText")
        var authorization = Pref.getPrefAuthorizationToken(context!!)
        Log.e(TAG, "authorization: $authorization")

        if (TextUtils.isEmpty(chatTypeText)) {
            rootView!!.llChatType.makeGone()
            rootView!!.tvChatTypeText.text = ""
        } else {
            rootView!!.llChatType.makeVisible()
            rootView!!.tvChatTypeText.text = chatTypeText
        }

        addObserver()
        setupPusher()

        chatLinearLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
        chatLinearLayoutManager?.stackFromEnd = true

        rootView!!.tvOtherName.text = "" + otherName
        var myImageUrl = userModel?.userDetails?.profilePhoto

        Glide.with(activity!!)
            .load(myImageUrl)
            .apply(
                RequestOptions().placeholder(R.mipmap.ic_user_profile)
                    .error(R.mipmap.ic_user_profile)
            )
            .into(rootView!!.ivMyProfile)


        if (isAdmin == "1") {
            //  rootView!!.tv_report.makeGone()
            rootView!!.iv_overflow.makeGone()
            Glide.with(activity!!)
                .load(R.mipmap.ic_logo_round)
                .into(rootView!!.ivOtherProfile)
        } else {
            Glide.with(activity!!)
                .load(otherImageUrl)
                .apply(
                    RequestOptions().placeholder(R.mipmap.ic_user_profile)
                        .error(R.mipmap.ic_user_profile)
                )
                .into(rootView!!.ivOtherProfile)
        }


        try {
            if (userModel?.userDetails?.isPremiumUser!!) {
                rootView!!.cv_add_template.makeVisible()
            } else {
                rootView!!.cv_add_template.makeGone()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        var chatEventParams = Bundle()
        chatEventParams.putString(Constants.EVENT_PARAM_USER_ID, "" + user_id);
        chatEventParams.putString(Constants.EVENT_PARAM_MSG_ID, "" + msg_id);
        chatEventParams.putString(Constants.EVENT_PARAM_TYPE, "" + type);
        chatEventParams.putString(Constants.EVENT_PARAM_SERVICE_ID, "" + service_id);
        chatEventParams.putString(Constants.EVENT_PARAM_ORDER_ID, "" + order_id);
        chatEventParams.putString(Constants.EVENT_PARAM_IS_ADMIN, "" + isAdmin);
        UTILS.logFirebaseAnalyticsEvent(
            requireContext(),
            Constants.EVENT_FIREBASE_CHAT,
            chatEventParams
        )
        callFetchMessageApi(0)

    }

    private fun callFetchMessageApi(offset: Int) {
        offSet = offset
        Log.e("ChatFragment", " msg_id: $msg_id offset: $offset")
        chatViewModel!!.fetchMessages(msg_id, offset)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addOnClickListener()

    }

    private fun rotateImageView(imageView: ImageView, degree: Float) {
        val rotateAnim = RotateAnimation(
            0.0f, degree,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnim.duration = 0
        rotateAnim.fillAfter = true
        imageView.startAnimation(rotateAnim)
    }

    private fun showFABMenu() {
        isFABOpen = true
        rootView!!.cv_add_template.animate()
            .translationY(resources.getDimension(R.dimen.standard_45))
        rootView!!.cv_view_template.animate()
            .translationY(resources.getDimension(R.dimen.standard_95))
        rootView!!.cv_view_service.animate()
            .translationY(resources.getDimension(R.dimen.standard_145))
        rotateImageView(rootView!!.ivMenu, 0F)
    }

    private fun closeFABMenu() {
        isFABOpen = false
        rootView!!.cv_add_template.animate().translationY(0F)
        rootView!!.cv_view_template.animate().translationY(0F)
        rootView!!.cv_view_service.animate().translationY(0F)
        rotateImageView(rootView!!.ivMenu, 45F)
    }

    private fun addOnClickListener() {

        rootView!!.iv_overflow.setOnClickListener {

            popup!!.show()

        }
        rootView!!.fabMenu.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }
        // to close menu on initially
        if (!isFABOpen) {
            rootView!!.fabMenu.callOnClick()
        }


        rootView!!.cv_view_template.setOnMyClickListener {
            if (templateModelList!!.size > 0) {
                showTemplateView()
            } else {
                chatViewModel!!.getTemplates()
            }
        }

        /*rootView!!.chatSwipeToRefresh.setOnRefreshListener {
            if (fetchMessageDataModel?.isPaginationEnded!!) {
                Log.e(TAG, " ===== fetchMessages pagination called ===== ")
                callFetchMessageApi(chatArraylist?.size!!)
            }else{
                showToast("No more chat available")
            }
            rootView!!.chatSwipeToRefresh.isRefreshing = false
        }*/

        rootView!!.imgBackBtn.setOnMyClickListener {
            homeController.onBackPressed()
        }

        rootView!!.ivOtherProfile.setOnClickListener {
            loadEndUserChatListFragment()
        }

        rootView!!.tvOtherName.setOnClickListener {
            loadEndUserChatListFragment()
        }

        rootView!!.imgCamera.setOnMyClickListener {
            PermissionCheck.with(activity!!).setPermissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).onAccepted {
//                dispatchTakePictureIntent()
                //   showSelectionDailog()
                sendTakePictureIntent()
            }.onDenied {
                showSettingsDialog()

            }.ask()
        }

        rootView!!.imgAttachDoc.setOnMyClickListener {
            PermissionCheck.with(activity!!).setPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).onAccepted {
                showSelectionDailog()
                //   pickPdfIntent()
            }.onDenied {
                showSettingsDialog()
            }.ask()
        }

        rootView!!.iv_send.setOnMyClickListener {
            callSendMessageApi()
        }

        rootView!!.tv_report_as_spam.setOnMyClickListener {
            var reason: String = rootView!!.etSpamReason.text.toString()
            if (!TextUtils.isEmpty(reason)) {
                chatViewModel?.reportAsSpam(msg_id, reason)
            } else {
                showToast("Please enter reason!")
            }
        }

        rootView!!.tv_cancel_report_spam.setOnMyClickListener {
            rootView!!.layout_report_spam.makeGone()
        }

        rootView!!.tv_done_add.setOnMyClickListener {
            var title = edt_text_title.text.toString().trim()
//            var message = etMessageTemplate.text.toString().trim()
            var message = rootView!!.edt_message.text.toString().trim()

            if (TextUtils.isEmpty(title)) {
                showToast("Please enter title")
            } else if (TextUtils.isEmpty(message)) {
                showToast("Please enter message")
            } else {
                chatViewModel?.saveTemplate(title, message)
                rootView!!.layout_add_temp.makeGone()
                rootView!!.fabMenu.callOnClick() // to close menus
            }
        }

        rootView!!.tv_cancel_add.setOnMyClickListener {
            rootView!!.layout_add_temp.makeGone()
        }
        rootView!!.tv_done_view_template.setOnMyClickListener {
            if (!TextUtils.isEmpty(selectedTemplateString)) {
                rootView!!.edt_message.setText(selectedTemplateString)
                rootView!!.layout_view_template.makeGone()
                rootView!!.fabMenu.callOnClick()  // to close fab menus
            } else {
                showToast("Please select template")
            }

        }

        rootView!!.tv_cancel_view_temp.setOnMyClickListener {
            rootView!!.layout_view_template.makeGone()
        }

        rootView!!.cv_view_service.setOnMyClickListener {
            if (serviceList!!.size > 0) {
                showServiceView()
            } else {
                chatViewModel!!.getServices()
            }
        }

        rootView!!.tv_done_view_service.setOnMyClickListener {
//            rootView!!.edt_message.setText(selectedServiceString)
            if (isServiceMessage) {
                rootView!!.layout_view_service.makeGone()
                callSendMessageApi()
                rootView!!.fabMenu.callOnClick()  // to close fab menus
            } else {
                showToast("Please select service")
            }

        }

        rootView!!.tv_cancel_view_service.setOnMyClickListener {
            isServiceMessage = false
            rootView!!.layout_view_service.makeGone()
        }

        /*rootView!!.ivCloseChatServiceView.setOnMyClickListener {
           closeSelectedServiceView()
        }*/

        rootView!!.cv_add_template.setOnMyClickListener {
            edt_text_title.setText("")
//            etMessageTemplate.setText("")
            rootView!!.edt_message.setText("")
            rootView!!.layout_add_temp.makeVisible()
        }

        rootView!!.ll_select_temp.setOnClickListener {
            rootView!!.sp_select_template.performClick()
        }

        rootView!!.ll_select_service.setOnClickListener {
            rootView!!.sp_select_service.performClick()
        }

        rootView!!.ll_select_problem.setOnClickListener {
//            rootView!!.sp_select_problem.performClick()
        }
        rootView!!.tv_report.setOnClickListener {
            if (isSpam) {
                showToast("Conversation is already reported as spam")
            } else {
                rootView!!.etSpamReason.setText("")
                rootView!!.layout_report_spam.makeVisible()
            }
        }
        rootView!!.sp_select_template.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position == 0) {
                        selectedTemplateString = ""
                        rootView!!.tvSpinnerText.text = "Select template"
                    } else {
//                        selectedTemplateString = rootView!!.sp_select_template.selectedItem.toString()
                        var selectedPos = rootView!!.sp_select_template.selectedItemPosition - 1
                        if (selectedPos < templateModelList!!.size) {
                            selectedTemplateString =
                                templateModelList[selectedPos]?.message.toString()
                            rootView!!.tvSpinnerText.text =
                                rootView!!.sp_select_template.selectedItem.toString()
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
        rootView!!.sp_select_service.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {
                    if (position == 0) {
                        selectedServiceString = ""
                        rootView!!.tvSpinnerServiceText.text = "Select Service"
                        rootView!!.cvChatService.makeGone()
                        isServiceMessage = false
                    } else {
                        var selectedPos = rootView!!.sp_select_service.selectedItemPosition - 1
//                         selectedServiceString = rootView!!.sp_select_service.selectedItem.toString()
//                         rootView!!.tvSpinnerServiceText.text = selectedServiceString
                        selectedService = serviceModelList[selectedPos]
                        isServiceMessage = true
                        showSelectedServiceView()
                        rootView!!.cvChatService.makeVisible()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        /*rootView!!.rvChatData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rvChatData.canScrollVertically(-1) && fetchMessageDataModel?.isPaginationEnded!!) {
//                    contractChatAndUserViewModel?.getTimeLineForChat(chatUser.id!!, mMessages.size)
                    Log.e(TAG, " ===== fetchMessages pagination called ===== ")
//                    chatViewModel!!.fetchMessages(msg_id, chatArraylist?.size!!)
                    callFetchMessageApi(chatArraylist?.size!!)
                }
            }
        })*/


        rootView!!.rvChatData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    //scrolled to BOTTOM
                    Log.e(TAG, " ===== scrolled to BOTTOM called ===== ")
                    if (showBottomScrollMessage) {
                        showToast("You're up to date")
//                        showSwipeToRefresh("No new messages",1000)
                    }
                    showBottomScrollMessage = true
                } else if (!recyclerView.canScrollVertically(-1) && dy < 0) {
                    //scrolled to TOP
                    Log.e(TAG, " ===== scrolled to TOP called  ===== ")
                    showBottomScrollMessage = true
                    if (fetchMessageDataModel?.isPaginationEnded!!) {
//                        showSwipeToRefresh("",600)
                        Log.e(TAG, " ===== fetchMessages pagination called ===== ")
                        callFetchMessageApi(chatArraylist?.size!!)
                    } else {
                        showSwipeToRefresh("No more chat available", 1000)
                    }

                }
            }
        })
    }

    private fun showSwipeToRefresh(mMessage: String, delayMillis: Long) {
        rootView!!.chatSwipeToRefresh.isRefreshing = true
        Handler().postDelayed(Runnable {
            rootView!!.chatSwipeToRefresh.isRefreshing = false
            if (!TextUtils.isEmpty(mMessage)) {
                showToast("" + mMessage)
            }
        }, delayMillis)

    }

    private fun loadEndUserChatListFragment() {
        homeController.onBackPressed()// will close chat screen to reduce pusher call
        homeController.loadFragment(
            ChatListEndUserFragment.newInstance(user_id),
            "ChatListEndUserFragment",
            this.javaClass.simpleName
        )
    }

    /*private fun closeSelectedServiceView() {
        isServiceMessage = false
//        rootView!!.layoutChatServiceView.makeGone()
        rootView!!.edt_message.setText("")
    }*/
    private fun showSelectedServiceView() {

        Glide.with(activity!!).load(selectedService?.image_url).error(R.mipmap.ic_splash_screen)
            .into(rootView!!.imgServicePhoto)
        rootView!!.tvChatServiceTitle.text = selectedService?.title
        rootView!!.rbChatRatting.rating = selectedService?.rating!!.toFloat()
        rootView!!.tvChatReviews.text = "( " + selectedService?.total_review + " Reviews )"
        rootView!!.tvChatPrice.text = "Starting at $" + selectedService?.price!!
        rootView!!.tvChatViewService.setOnClickListener {
            activity!!.startActivity(
                Intent(context, WebViewActivity::class.java).putExtra(
                    "url",
                    selectedService?.service_url
                ).putExtra("title", selectedService?.title)
            )
        }
//        rootView!!.layoutChatServiceView.makeVisible()
    }

    private fun showServiceView() {
        rootView!!.sp_select_service.adapter = adapterService
        rootView!!.sp_select_service.setSelection(0)
        rootView!!.layout_view_service.makeVisible()
    }

    private fun showTemplateView() {
        rootView!!.sp_select_template.adapter = adapterTemplate
        rootView!!.sp_select_template.setSelection(0)
        rootView!!.layout_view_template.makeVisible()
    }

    private fun showSelectionDailog() {
        //rootView!!.bottom_selection_sheet.visibility = View.VISIBLE
        mBottomSheetDialog = BottomSheetDialog(activity!!)
        val sheetView = activity!!.layoutInflater.inflate(R.layout.dialog_upload_file, null)
        mBottomSheetDialog!!.setContentView(sheetView)
        mBottomSheetDialog!!.show()
        sheetView.cancel.setOnClickListener { mBottomSheetDialog!!.dismiss() }
        /* sheetView.capturePicture.setOnClickListener {
             sendTakePictureIntent()
             mBottomSheetDialog!!.dismiss()
         }*/
        sheetView.choosePicture.setOnClickListener {
            pickImageIntent()
            mBottomSheetDialog!!.dismiss()
        }
        sheetView.choosePdf.setOnClickListener {
            pickPdfIntent()
            mBottomSheetDialog!!.dismiss()
        }
    }

    private fun sendTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(homeController.packageManager)?.also {
                // Create the File where the photo should go
                try {
                    photoFile = FileUtils.getNewImageFile(homeController)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    ex.printStackTrace()
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileUtils.getFileProviderUri(homeController, it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, Constants.codeCameraRequest)
                }
            }
        }
    }

    private fun pickImageIntent() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_picture_tag)),
            Constants.codePickImageRequest
        )
    }

    private fun pickPdfIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_pdf_tag)),
            Constants.codePickPdfRequest
        )
    }

    private fun callSendMessageApi() {
        var messageStr = rootView!!.edt_message.text.toString().trim()
        var attachment: MultipartBody.Part? = null
        if (mSelectionPath != null) {
            attachment =
                if (selectedType == Constants.codeCameraRequest || selectedType == Constants.codePickImageRequest) {
                    prepareImageFilePart("file", mSelectionPath!!)
                } else {
                    preparePdfFilePart("file", mSelectionPath!!)
                }
        }

        if (attachment == null && TextUtils.isEmpty(messageStr) && !isServiceMessage) {
            showToast("Can't send empty message")
            return
        }

        if (isServiceMessage) {
            messageStr = "[{@SERVICE_ID=" + selectedService?.secret!! + "@}]"
        }

        var deviceType = Constants.deviceType
        chatViewModel!!.sendMessages(
            message = messageStr.toRequestBody("text/plain".toMediaTypeOrNull()),
            user_id = user_id.toRequestBody("text/plain".toMediaTypeOrNull()),
            type = type.toRequestBody("text/plain".toMediaTypeOrNull()),
            order_id = order_id.toRequestBody("text/plain".toMediaTypeOrNull()),
            service_id = service_id.toRequestBody("text/plain".toMediaTypeOrNull()),
            message_id = msg_id.toRequestBody("text/plain".toMediaTypeOrNull()),
            deviceType = deviceType.toRequestBody("text/plain".toMediaTypeOrNull()),
            attachment = attachment
        )
    }


    override fun onResume() {
        super.onResume()
        ChatFragment.isChatScreenOpened = true
        homeController.llToolbar.makeGone()
        homeController.llBottomBar.makeGone()
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatFragment.isChatScreenOpened = false
        if (needToClearMsgCounter) {
            chatViewModel!!.makeChatSeen(msg_id)
        }
        try {
//            homeController.pusher?.unsubscribe(subscribedChannel?.name)
            Log.e(
                TAG,
                " pusher connection state onDestroy : " + pusher?.connection?.state.toString()
            )
            if (pusher?.connection?.state == ConnectionState.CONNECTED) {
//                var  subscribedChannel = pusher?.getPrivateChannel(channel)
                if (subscribedChannel != null) {
                    if (subscribedChannel?.isSubscribed!!) {
                        pusher?.unsubscribe(subscribedChannel?.name)
                    }
                }
                pusher?.disconnect()
//                pusher?.unsubscribe(channel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMapAuthorizationHeaders(): HashMap<String, String> {
        return try {
            var authHeader: HashMap<String, String> = HashMap<String, String>();
            authHeader["Authorization"] = Pref.getPrefAuthorizationToken(homeController);
            authHeader;
        } catch (e: Exception) {
            HashMap()
        }
    }

    private fun setupPusher() {
        ChatFragment.isChatScreenOpened = true
        activity!!.runOnUiThread(Runnable {

            val authorizer = HttpAuthorizer(ApiClient.PUSHER_CHAT_END_POINT)
            authorizer.setHeaders(getMapAuthorizationHeaders())

            options = PusherOptions()
            options?.setCluster(PUSHER_APP_CLUSTER)
            options?.activityTimeout = PUSHER_APP_TIMEOUT
            options?.authorizer = authorizer

            pusher = Pusher(PUSHER_APP_KEY, options)
            subscribedChannel = pusher!!.subscribePrivate(channel)

            subscribedChannel?.bind(eventName, object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
//                    Log.e("ChatFragment", " onEvent data:  " + event.toString()!!)
                    sendEventToUI(event)
                }

                override fun onSubscriptionSucceeded(channelName: String?) {
                    Log.e(TAG, "channel onSubscriptionSucceeded: $channelName")
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {
                    Log.e(TAG, "channel onAuthenticationFailure: $message")
                }

            })
            pusher?.connect();
            Log.e(
                TAG,
                " pusher connection state onSetupPusher : " + pusher?.connection?.state.toString()
            )
            /*Handler(Looper.getMainLooper()).postDelayed(Runnable {
                Log.e(TAG, " pusher connection state onSetupPusher : " + pusher?.connection?.state.toString())
            }, 1000)*/


            // this method get crash issue , FATAL EXCEPTION: pusher-java-client eventQueue, dont use this
            /*pusher?.connect(object : ConnectionEventListener {
                override fun onConnectionStateChange(connectionStateChange: ConnectionStateChange) {
                    Log.e("connectionStateChange", connectionStateChange.currentState.toString())
                    if (connectionStateChange.currentState == ConnectionState.CONNECTED) {

//                    SOCKET_ID = pusher!!.connection.socketId
                        *//*homeController.runOnUiThread {
                           chatViewModel!!.authenticatePusher(SOCKET_ID, channel)
                       }*//*
                        *//*try {
                            viewLifecycleOwner.lifecycleScope.launch {
                                setAuthorize()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }*//*
                    }
                }

                override fun onError(s: String, s1: String, e: Exception) {
//                e.printStackTrace()
//                Log.e(TAG, " pusher connect onError: " + e.message)
//                throw e
                }
            })*/

        })

    }

    private fun sendEventToUI(event: PusherEvent?) {
        Log.e("ChatFragment", " onEvent data:  " + event!!)
        var eventDataStr = event!!.data.toString()
        var eventDataJson = JSONObject(eventDataStr)
        Log.e("ChatFragment", " onEvent data json:  " + eventDataJson)

        var conversation_id = ""

        if (eventDataJson.has("conversation_id")) {
            conversation_id = eventDataJson.get("conversation_id").toString()
        }

        /* if (eventDataJson.has("is_deleted")) {
             activity!!.runOnUiThread {
                 isDeleted = eventDataJson.get("is_deleted") == 1
                 if (eventDataJson.get("is_deleted") == 1) {
                     rootView!!.ll_block_user.visibility = View.VISIBLE;
                     rootView!!.tv_message.text =
                         homeController.getString(R.string.deleted_user_detail);
                     rootView!!.ll_message_send.visibility = View.GONE
                     rootView!!.fabMenu.makeGone()

                 } else {
                     rootView!!.ll_block_user.visibility = View.GONE;
                     rootView!!.ll_message_send.visibility = View.VISIBLE
                     rootView!!.fabMenu.makeVisible()

                 }
             }
         }*/
        try {
            var messageDetailStr = eventDataJson.get("message_detail")
            var messageData = Gson().fromJson(
                messageDetailStr.toString(),
                ConversationData::class.java
            )


            if (conversation_id == msg_id) {
                // without runOnUiThread adapter not update list
                activity!!.runOnUiThread {

                    if (userModel?.userDetails?.id != messageData.fromUser?.id) {
                        chatRoomAdapter?.setMarkAsRead(true)
                        addNewMessage(messageData)
                        needToClearMsgCounter = true

                    } else {

                        // update current user messages from pusher
                        addNewMessage(messageData)
                        chatRoomAdapter?.setMarkAsRead(false)

                        //will sendBroadcast to update current user message in message listing(MessageList/EndUserList)
                        // make sure same keys passing as per notification data coz same Broadcast firing while receiving notification

                        // no need bellow code as we refreshing list on resume on message ChatListFragment/ChatListEndUserFragment page
                        /*activity!!.sendBroadcast(
                            Intent(Constants.NOTIFICATION_ACTION).putExtra(
                                Constants.NOTIFICATION_DATA,
                                eventDataStr
                            )
                        )*/

                        Log.e(TAG, " onEvent sendBroadcast :")
                    }
                }

                if (eventDataJson.has("is_blocked")) {
                    activity?.let {
                        it.runOnUiThread {
                            isBlocked = eventDataJson.get("is_blocked") == 1
                            Log.v(
                                "====Blocked",
                                "-" + isBlocked + "----" + eventDataJson.get("is_blocked")
                            )
                            if (isBlocked) {
                                popup!!.menu.getItem(1).setTitle(R.string.unblock)
                                rootView!!.ll_block_user.visibility = View.VISIBLE;
                                rootView!!.tv_message.text =
                                    homeController.getString(R.string.block_user_detail);
                                rootView!!.ll_message_send.visibility = View.GONE
                                rootView!!.fabMenu.makeGone()
                            } else {
                                popup!!.menu.getItem(1).setTitle(R.string.block)
                                rootView!!.ll_block_user.visibility = View.GONE;
                                rootView!!.ll_message_send.visibility = View.VISIBLE
                                rootView!!.fabMenu.makeVisible()
                            }
                        }
                    }
                }
                if (eventDataJson.has("is_conversation_block")) {

                    activity?.let {
                        it.runOnUiThread {
                            isConversationBlocked = eventDataJson.get("is_conversation_block") == 1
                            if (eventDataJson.get("is_conversation_block") == 1) {
                                rootView!!.ll_block_user.visibility = View.VISIBLE;
                                rootView!!.tv_message.text =
                                    homeController.resources.getString(R.string.block_user_detail);
                                rootView!!.ll_message_send.visibility = View.GONE
                                rootView!!.fabMenu.makeGone()

                            } else {
                                rootView!!.ll_block_user.visibility = View.GONE;
                                rootView!!.ll_message_send.visibility = View.VISIBLE
                                rootView!!.fabMenu.makeVisible()
                            }
                        }


                    }
                }

            } else {
                // current user msg , we have added on message sent response
                Log.e(
                    TAG,
                    " current user msg , we have added on message sent response "
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /*fun setAuthorize() {
//        var result = setChannel()
//        Log.v("==RESULT", "-" + result)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            authorizeChannel("")
        }, 1000)
    }*/

    /*fun authorizeChannel(result: String) {
        if (!ChatFragment.isChatScreenOpened){
            return
        }
        try {
            subscribedChannel= pusher?.subscribePrivate(
                channel, object : PrivateChannelEventListener {

                    override fun onSubscriptionSucceeded(channelName: String?) {
                        Log.e(TAG, "channel onSubscriptionSucceeded: " + channelName!!)
                    }

                    override fun onEvent(event: PusherEvent?) {
                        Log.e("ChatFragment", " onEvent data:  " + event!!)
                        var eventDataStr = event!!.data.toString()
                        var eventDataJson = JSONObject(eventDataStr)
                        var conversation_id = ""

                        if (eventDataJson.has("conversation_id")) {
                            conversation_id = eventDataJson.get("conversation_id").toString()
                        }

                        try {
                            var messageDetailStr = eventDataJson.get("message_detail")
                            var messageData = Gson().fromJson(
                                messageDetailStr.toString(),
                                ConversationData::class.java
                            )
                            if (conversation_id == msg_id) {
                                // without runOnUiThread adapter not update list
                                activity!!.runOnUiThread {

                                    if (userModel?.userDetails?.id != messageData.fromUser?.id) {
                                        chatRoomAdapter?.setMarkAsRead(true)
                                        addNewMessage(messageData)
                                        needToClearMsgCounter = true

                                    } else {
                                        //will sendBroadcast to update current user message in message listing(MessageList/EndUserList)
                                        // make sure same keys passing as per notification data
                                        activity!!.sendBroadcast(
                                            Intent(Constants.NOTIFICATION_ACTION).putExtra(
                                                Constants.NOTIFICATION_DATA,
                                                eventDataStr
                                            )
                                        )
                                        Log.e(TAG, " onEvent sendBroadcast :")
                                    }
                                }

                            } else {
                                // current user msg , we have added on message sent response
                                Log.e(
                                    TAG,
                                    " current user msg , we have added on message sent response "
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onAuthenticationFailure(
                        message: String?,
                        e: java.lang.Exception?
                    ) {
                        Log.e(TAG, "channel onAuthenticationFailure: " + message!!)
                    }
                },
                eventName
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }*/


    /*suspend fun setChannel(): String {
        var result = ""
        try {
            withContext(Dispatchers.IO) {
                val authorizer = HttpAuthorizer(ApiClient.PUSHER_CHAT_END_POINT)
                authorizer.setHeaders(getMapAuthorizationHeaders())
                result = authorizer.authorize(channel, SOCKET_ID)
                options!!.setAuthorizer(authorizer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }*/

    private fun init() {

        chatViewModel =
            ViewModelProvider(this, MyViewModelFactory(ChatViewModel(activity!!))).get(
                ChatViewModel::class.java
            )
    }

    private fun addObserver() {
        chatViewModel!!.isLoading!!.observe(activity!!, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })

        chatViewModel!!.responseError!!.observe(activity!!, Observer {

            val res = it.string()
            val jsonObject = JSONObject(res)
            if (jsonObject.getInt("code") == 400) {
                rootView!!.ll_block_user.visibility = View.VISIBLE;
                rootView!!.ll_message_send.visibility = View.GONE
                rootView!!.fabMenu.makeGone()
                rootView!!.edt_message.setText("")
                Toast.makeText(homeController, jsonObject.getString("message"), Toast.LENGTH_SHORT)
                    .show()

            } else {
                homeController!!.errorBody(it)
            }
        })
        chatViewModel!!.blockUserModel!!.observe(activity!!, Observer {
            /* isBlocked=!isBlocked
             if (!isBlocked) {
                 popup!!.menu.getItem(1).setTitle(R.string.block)
                 rootView!!.ll_block_user.visibility = View.VISIBLE;
                 rootView!!.ll_message_send.visibility = View.GONE
                 if (!isConversationBlocked) {
                     rootView!!.ll_block_user.visibility = View.GONE;
                     rootView!!.ll_message_send.visibility = View.VISIBLE
                 }
             } else {
                 popup!!.menu.getItem(1).setTitle(R.string.unblock)
                 rootView!!.ll_block_user.visibility = View.VISIBLE;
                 rootView!!.ll_message_send.visibility = View.GONE
             }*/
            callFetchMessageApi(0)

            Toast.makeText(homeController, it.message, Toast.LENGTH_SHORT).show()
        })
        chatViewModel!!.fetchMessages!!.observe(activity!!, Observer {
            ChatFragment.isChatScreenOpened = true // sometimes this remain false so doing here too
            fetchMessageDataModel = it
            chatArraylist = fetchMessageDataModel?.conversations!!
//            chatArraylist!!.reverse()

            /*chatRoomAdapter = ChatRoomAdapter(
                activity!!, this,
                chatArraylist!!, userModel?.userDetails?.id.toString(), it.time
            )
            rootView!!.rvChatData.adapter = chatRoomAdapter*/
// commented on 17-09-21

            if (fromBlockuser) {
                if (offSet == 0) {
                    chatRoomAdapter = ChatRoomAdapter(
                        activity!!,
                        this,
                        chatArraylist!!,
                        userModel?.userDetails?.id.toString()
                    )
                    chatRoomAdapter?.setTimeDiff(0)
                    oldChatTimestamp = System.currentTimeMillis()
                    rootView!!.rvChatData.layoutManager = chatLinearLayoutManager
                    rootView!!.rvChatData.adapter = chatRoomAdapter
                    showBottomScrollMessage = false
                    scrollToBottom()
                } else {
                    (rootView!!.rvChatData.adapter as ChatRoomAdapter?)?.let {
                        /**already adapter set just need to notify*/
                        Log.v("====", "=adapter")
                        chatRoomAdapter?.notifyDataSetChanged()

                    } ?: run {
                        /**need to set new adapter*/
                        chatRoomAdapter = ChatRoomAdapter(
                            activity!!,
                            this,
                            chatArraylist!!,
                            userModel?.userDetails?.id.toString()
                        )
                        chatRoomAdapter?.setTimeDiff(0)
                        oldChatTimestamp = System.currentTimeMillis()
                        rootView!!.rvChatData.layoutManager = chatLinearLayoutManager
                        rootView!!.rvChatData.adapter = chatRoomAdapter
                        showBottomScrollMessage = false
                        scrollToBottom()
                    }
                }
            } else {
                (rootView!!.rvChatData.adapter as ChatRoomAdapter?)?.let {
                    /**already adapter set just need to notify*/
                    Log.v("====", "=adapter")
                    chatRoomAdapter?.notifyDataSetChanged()

                } ?: run {
                    /**need to set new adapter*/
                    chatRoomAdapter = ChatRoomAdapter(
                        activity!!,
                        this,
                        chatArraylist!!,
                        userModel?.userDetails?.id.toString()
                    )
                    chatRoomAdapter?.setTimeDiff(0)
                    oldChatTimestamp = System.currentTimeMillis()
                    rootView!!.rvChatData.layoutManager = chatLinearLayoutManager
                    rootView!!.rvChatData.adapter = chatRoomAdapter
                    showBottomScrollMessage = false
                    scrollToBottom()
                }
            }
            rootView!!.tv_last_message.makeGone()
            if (chatArraylist!!.size > 0) {
                rootView!!.rvChatData.makeVisible()
                rootView!!.tvNoData.makeGone()

                if (!TextUtils.isEmpty(it.time)) {
                    rootView!!.tv_last_message.makeVisible()
                    rootView!!.tv_last_message.text = "Last response " + it.time
                }

            } else {
                rootView!!.rvChatData.makeGone()
                rootView!!.tvNoData.makeVisible()
            }

            isSpam = it.is_spam
            isBlocked = it.is_blocked == 1
            Log.v("====Blocked", "-" + isBlocked + "----" + it.is_blocked);
            if (isBlocked) {
                popup!!.menu.getItem(1).setTitle(R.string.unblock)
                rootView!!.ll_block_user.visibility = View.VISIBLE;
                rootView!!.tv_message.text = homeController.getString(R.string.block_user_detail);
                rootView!!.ll_message_send.visibility = View.GONE
                rootView!!.fabMenu.makeGone()
            } else {
                popup!!.menu.getItem(1).setTitle(R.string.block)
                rootView!!.ll_block_user.visibility = View.GONE;
                rootView!!.ll_message_send.visibility = View.VISIBLE
                rootView!!.fabMenu.makeVisible()

            }
            isConversationBlocked = it.is_conversation_block == 1
            if (it.is_conversation_block == 1) {
                rootView!!.ll_block_user.visibility = View.VISIBLE;
                rootView!!.ll_message_send.visibility = View.GONE
                rootView!!.tv_message.text = homeController.getString(R.string.block_user_detail);
                rootView!!.fabMenu.makeGone()
            } else {
                rootView!!.ll_block_user.visibility = View.GONE;
                rootView!!.ll_message_send.visibility = View.VISIBLE
                rootView!!.fabMenu.makeVisible()

            }
            // if is_user_available is 0 means User is deleted from backend
            isDeleted = it.is_user_available == 0
            if (it.is_user_available == 0) {
                rootView!!.ll_block_user.visibility = View.VISIBLE;
                rootView!!.ll_message_send.visibility = View.GONE
                rootView!!.tv_message.text = homeController.getString(R.string.deleted_user_detail);
                rootView!!.iv_overflow.makeGone()
                rootView!!.fabMenu.makeGone()

            }

            /*if (chatArraylist!!.size <= 20){
                chatViewModel!!.getTemplates()
                chatViewModel!!.getServices()
            }*/
        })

        /*chatViewModel!!.authenticationPusher?.observe(activity!!, Observer {
            auth = it.auth

        })*/

        chatViewModel!!.serviceResponse!!.observe(activity!!, Observer {
            if (it.success!! && it.services!!.isNotEmpty()) {
                serviceModelList = it.services!!
                serviceList = ArrayList<String>()
                serviceList.add("Select Service")
                for (data in it.services!!) {
                    var message = data?.title
                    serviceList.add("" + message)
                }
                if (activity != null && !activity!!.isFinishing) {
                    setServiceAdapter(serviceList)
                }
                showServiceView()
            } else {
                if (it.services!!.size == 0) {
                    showToast("No service found!")
                }
            }
        })

        chatViewModel!!.templateResponse!!.observe(activity!!, Observer {

            if (it.success!! && it.templates!!.size > 0) {
                templateModelList = it.templates!!
                templateList = ArrayList<String>()
                templateList.add("Select template")

                for (data in it.templates!!) {
                    var message = data?.title
                    templateList.add("" + message)
                }
                if (activity != null && !activity!!.isFinishing) {
                    setTemplateAdapter(templateList)
                }
                showTemplateView()
            } else {
                if (it.templates!!.size == 0) {
                    showToast("No templates found!")
                }
            }
        })

        chatViewModel!!.saveTemplateResponse!!.observe(activity!!, Observer {
            showToast("" + it.message)
            if (it.status!!) {
                if (templateModelList!!.size > 0) {
                    templateModelList.add(it.templates)
                    templateList.add(it.templates?.title!!)
                }
                rootView!!.edt_message.setText("")
            }
        })

        chatViewModel!!.reportSpamResponse!!.observe(activity!!, Observer {
            if (it.success!!) {
                rootView!!.layout_report_spam.makeGone()
                showToast("" + it.message)
                isSpam = true
            }
        })

        chatViewModel!!.sendMessage!!.observe(activity!!, Observer {
            if (it.success!!) {

                selectedType = 0
                if (!TextUtils.isEmpty(mSelectionPath)) {
                    // file message
//                    rootView!!.edt_message.setText("")
                } else if (isServiceMessage) {
                    // service message
//                    rootView!!.edt_message.setText("")
                } else {
                    // text message
                    rootView!!.edt_message.setText("")
                }
//                addNewMessage(it.messageDetail)
//                chatRoomAdapter?.setMarkAsRead(false)
            }
            mSelectionPath = null
            isServiceMessage = false
//            closeSelectedServiceView()
        })


    }

    private fun addNewMessage(messageData: ConversationData?) {
        showBottomScrollMessage = false

        var now = System.currentTimeMillis()
        Log.e(TAG, " Now : $now  , old $oldChatTimestamp")
        var lastMsgTimeDiff: Long = System.currentTimeMillis().minus(oldChatTimestamp)
        Log.e(TAG, "lastMsgTimeDiff: $lastMsgTimeDiff")
        chatRoomAdapter?.setTimeDiff(lastMsgTimeDiff!!)
        chatRoomAdapter?.notifyDataSetChanged()


        chatRoomAdapter?.setTimeDiff(0)
        messageData?.timestamp = "" + now
        if (chatArraylist!!.size > 0) {
            chatArraylist?.add(0, messageData!!) // to add at chat list bottom
        } else {
            chatArraylist?.add(messageData!!)
            rootView!!.rvChatData.makeVisible()
            rootView!!.tvNoData.makeGone()
        }
        chatRoomAdapter?.notifyDataSetChanged()

        oldChatTimestamp = now

        scrollToBottom()

        // to update in chat list
        /*var strMessage = ""
        strMessage = if (messageData?.is_service_preview!!) {
            "Shared a service"
        } else {
            if (messageData?.attachment == 1) {
                "Shared a file"
            } else {
                "" + messageData?.message
            }
        }
        ChatListEndUserFragment.lastChatMessage = strMessage
        ChatListEndUserFragment.lastChatTimeAgo = "" + now*/
//        showBottomScrollMessage = true

    }

    private fun scrollToBottom() {
        try {
            if (chatArraylist!!.size > 0) {
//                chatLinearLayoutManager?.scrollToPosition(0);
                rootView!!.rvChatData.smoothScrollToPosition(0);
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setTemplateAdapter(arrayList: ArrayList<String>) {
        adapterTemplate = HighLightArrayAdapter(
            activity!!,
            R.layout.row_spinner,
            arrayList,
            selectionColor = R.color.colorPrimary,
            selectedTextColor = R.color.black,
            unSelectedTextColor = R.color.black
        )
    }

    private fun setServiceAdapter(arrayList: ArrayList<String>) {
        adapterService = HighLightArrayAdapter(
            activity!!,
            R.layout.row_spinner,
            arrayList,
            selectionColor = R.color.colorPrimary,
            selectedTextColor = R.color.black,
            unSelectedTextColor = R.color.black
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {


            if (requestCode == Constants.codeCameraRequest) {
                try {

                    mSelectionPath = photoFile!!.absolutePath
                    selectedType = Constants.codeCameraRequest

                    Log.e(TAG, " real path - $mSelectionPath")

                    callSendMessageApi()

                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, " exception - $e")
                }

            } else if (requestCode == Constants.codePickImageRequest) {
                try {

                    Log.e(TAG, "data uri authority: " + data?.data!!.authority)
                    mSelectionPath = FileUtils.getFileCachePath(activity!!, data?.data!!)

                    Log.e(TAG, " real path - $mSelectionPath")
                    selectedType = Constants.codePickImageRequest

                    callSendMessageApi()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == Constants.codePickPdfRequest) {
                try {
                    Log.e(TAG, "data uri authority: " + data?.data!!.authority)
                    mSelectionPath = FileUtils.getFileCachePath(activity!!, data?.data!!)

                    Log.e(TAG, " real path - $mSelectionPath")

                    selectedType = Constants.codePickPdfRequest

                    callSendMessageApi()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun showSettingsDialog() {
        val builder =
            AlertDialog.Builder(homeController!!)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton(
            "GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which ->
            dialog.cancel()
        }
        builder.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", homeController!!.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    private fun prepareImageFilePart(partName: String, sfile: String): MultipartBody.Part {
        val file = File(sfile)
        try {
            var bitmap = BitmapFactory.decodeFile(file.path)
            bitmap = FileUtils.rotateImageIfRequired(bitmap, file.path)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, FileOutputStream(file))
        } catch (t: Throwable) {
            Log.e("ERROR", "Error compressing file.$t")
            t.printStackTrace()
        }
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, reqFile)
    }

    private fun preparePdfFilePart(partName: String, sfile: String): MultipartBody.Part {
        val file = File(sfile)
        val reqFile = file.asRequestBody("pdf/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, reqFile)
    }

    override fun onListClickSimple(position: Int, imageUrl: String?) {
        super.onListClickSimple(position, imageUrl)
        if (fromBlockuser) {
            if (isBlocked) {
                chatViewModel!!.blockUser(user_id, 0);
            } else {
                chatViewModel!!.blockUser(user_id, 1);
            }
        } else {
            if (!NetworkUtil.isInternetAvailable(activity!!)) {
                showToast(getString(R.string.no_internet_connection))
                return
            }
            showImagePdfDialog(imageUrl.toString())
        }
    }

    override fun onListClick(position: Int, obj: Any?) {
        super.onListClick(position, obj)

        showBottomSheetDialog(obj as ConversationData)
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun showBottomSheetDialog(model: ConversationData) {
        val bottomSheetDialog =
            BottomSheetDialog(activity!!, R.style.ThemeOverlay_Demo_BottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.dialog_chat_functionality)
        val bottomSheetBehavior = bottomSheetDialog.behavior
        bottomSheetBehavior.isDraggable = true
        var copyMessage = bottomSheetDialog.findViewById<LinearLayout>(R.id.ll_copy_message)
        var unreadMessage = bottomSheetDialog.findViewById<LinearLayout>(R.id.ll_copy_message)
        var copyMessageText =
            bottomSheetDialog.findViewById<AppCompatTextView>(R.id.tv_copy_message)
        var unreadMessageText =
            bottomSheetDialog.findViewById<AppCompatTextView>(R.id.tv_unread_message)
        var view_copy = bottomSheetDialog.findViewById<View>(R.id.view_copy)

        if (model.attachment == 0 && !model.is_service_preview) {
            copyMessage!!.visibility = View.VISIBLE
            view_copy!!.visibility = View.VISIBLE
        }

        copyMessageText!!.isClickable = true
        copyMessageText!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.v("==CLicked", "-")
                val clipboard =
                    activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("newtext", model.message)
                clipboard.setPrimaryClip(clip)
                showToast("Copied!")
                bottomSheetDialog.dismiss()
            }

        })

        unreadMessageText!!.setOnClickListener {
            Log.v("==CLicked", "-" + "unread" + chatArraylist!!.size)
            var index = getIndexOfUnreadMessage(model.id!!)
            for (i in index downTo 0) {
                chatArraylist!![i].is_read = 0
                Log.v("====msg", "-" + chatArraylist!![i].message)
            }

            chatRoomAdapter!!.notifyDataSetChanged()
            bottomSheetDialog.dismiss()

        }
        bottomSheetDialog.show()

    }

    fun getIndexOfUnreadMessage(Id: Int): Int {
        for (i in 0..chatArraylist!!.size - 1) {
            if (chatArraylist!![i].id == Id) {
                return i
            }


        }
        return -1
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.menu_report -> {
                if (isSpam) {
                    showToast("Conversation is already reported as spam")
                } else {
                    rootView!!.etSpamReason.setText("")
                    rootView!!.layout_report_spam.makeVisible()
                }

                return true
            }
            // do your code

            R.id.menu_block -> {
                fromBlockuser = true
                if (isBlocked) {
                    activity?.let {
                        UTILS.commonDialog(
                            it,
                            this,
                            it.getString(R.string.unblock_message)
                        )
                    }

                    // chatViewModel!!.blockUser(user_id, 0);
                } else {
                    activity?.let {
                        UTILS.commonDialog(
                            it,
                            this,
                            it.getString(R.string.block_message)
                        )
                    }

                    // chatViewModel!!.blockUser(user_id, 1);
                }
                return true
            }         // do your code
            else -> false
        }

    }


}