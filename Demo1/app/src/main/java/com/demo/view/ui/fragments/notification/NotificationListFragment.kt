package com.demo.view.ui.fragments.notification

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.demo.R
import com.demo.utilities.*
import com.demo.view.adapter.NotificationListAdapter
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseFragment
import com.demo.view.ui.fragments.chats.ChatFragment
import com.demo.view.ui.fragments.chats.ChatListFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_bar_layout.*
import kotlinx.android.synthetic.main.dialog_confirm_for_complete_order.*
import kotlinx.android.synthetic.main.dialog_confirm_for_complete_order_with_review.*
import kotlinx.android.synthetic.main.dialog_show_note_for_seller.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.fragment_chat_list.view.*
import kotlinx.android.synthetic.main.fragment_notification_list.view.*
import kotlinx.android.synthetic.main.fragment_notification_list.view.pullToRefresh
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.json.JSONObject


class NotificationListFragment : BaseFragment() {

    var rootView: View? = null
    var notificationDataViewModel: NotificationDataViewModel? = null
    var notificationListModel: NotificationListModel? = null
    var orderModel: ProjectsOrdersDataModel.Order? = null
    var isBuyingOrders = true
    var notificationAdapter: NotificationListAdapter? = null
    var selectedDeletePosition = -1
    var lastPositionSelected = -1
    var userID = ""
    var chatTypeText = ""
    var serviceID = ""
    var orderID = ""
    var otherName = ""
    var otherImage = ""
    var isAdmin = "0"
    var type = ChatListFragment.TYPE_ORDERS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_notification_list, container, false)
            init()
            setUp()
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addOnClickListener()
        rootView!!.pullToRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            notificationDataViewModel?.getAllNotification(0)
            rootView!!.pullToRefresh.isRefreshing = false
        })

    }

    private fun init() {

        notificationDataViewModel = ViewModelProvider(
            activity!!,
            MyViewModelFactory(NotificationDataViewModel(activity!!))
        ).get(NotificationDataViewModel(activity!!)::class.java)

    }

    private fun setUp() {

        addObserver()

    }

    private fun addOnClickListener() {

        rootView!!.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rvNotification.canScrollVertically(1) && notificationListModel!!.paginationEnded && notificationDataViewModel?.isLoading?.value == false) {
                    notificationDataViewModel?.getAllNotification(notificationListModel?.notifications!!.size)
                }
            }
        })
    }

    private fun addObserver() {

        notificationDataViewModel!!.isLoading!!.observe(activity!!, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })

        notificationDataViewModel!!.responseError!!.observe(activity!!, Observer {
            val res = it.string()
            try {
                val jsonObject = JSONObject(res)
                if (jsonObject.getInt("code") == 400) {

                    Toast.makeText(
                        homeController,
                        jsonObject.getString("message"),
                        Toast.LENGTH_SHORT
                    )
                        .show()

                } else {
                    homeController!!.errorBody(it)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        })

        notificationDataViewModel!!.notificationsResponse!!.observe(activity!!, Observer {
            it?.let {

                notificationListModel = it

                (rootView!!.rvNotification.adapter as NotificationListAdapter?)?.notifyDataSetChanged()
                    ?: run {
                        try {
                            notificationAdapter = NotificationListAdapter(
                                activity!!,
                                notificationListModel!!.notifications,
                                this
                            )
                            rootView!!.rvNotification.adapter = notificationAdapter
                            val swipeHandler = object : SwipeToDeleteCallback(activity!!) {
                                override fun onSwiped(
                                    viewHolder: RecyclerView.ViewHolder,
                                    direction: Int
                                ) {
                                    selectedDeletePosition = viewHolder.adapterPosition
                                    if (selectedDeletePosition >= notificationListModel!!.notifications!!.size) {
                                        return
                                    }
                                    var notificationID: Int =
                                        notificationListModel!!.notifications?.get(
                                            selectedDeletePosition
                                        )?.id!!
                                    notificationDataViewModel?.deleteNotification(notificationID)
                                }
                            }
                            val itemTouchHelper = ItemTouchHelper(swipeHandler)
                            itemTouchHelper.attachToRecyclerView(rootView!!.rvNotification)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                upDateUi()
            }
        })


        notificationDataViewModel!!.createConversation!!.observe(activity!!, Observer {
//            var msgId = it.message_id
            if (it == null) {
                return@Observer
            }
            if (it.success!!) {
                var msgId = it.message_secret
                Log.e("NotificationList", " =====ChatFragment ==== ")

                homeController.loadFragment(
                    ChatFragment.newInstance(
                        msgId,
                        userID,
                        type,
                        chatTypeText,
                        orderID,
                        serviceID,
                        otherImage,
                        otherName,
                        isAdmin
                    ),
                    "ChatFragment",
                    this.javaClass.simpleName
                )
            }
            notificationDataViewModel?.createConversation!!.value = null
        })

        notificationDataViewModel!!.deleteNotificationResponse!!.observe(activity!!, Observer {
            if (it == null) {
                return@Observer
            }
            showToast("" + it.message)
            if (it.success!!) {
                if (selectedDeletePosition >= 0) {
                    notificationListModel!!.notifications?.removeAt(selectedDeletePosition)
                    notificationAdapter?.notifyItemRemoved(selectedDeletePosition)
                }
            }
            selectedDeletePosition = -1
            notificationDataViewModel?.deleteNotificationResponse!!.value = null
        })

        notificationDataViewModel!!.completeOrderDataResponse!!.observe(activity!!, Observer {
            if (it == null) {
                return@Observer
            }
            homeController.showToast(it!!.message!!)
            if (it.success!!) {
                if (lastPositionSelected >= 0) {
                    notificationListModel!!.notifications!![lastPositionSelected]?.order =
                        it.response
                    notificationAdapter?.notifyDataSetChanged()
//                Log.v("===DATA==", "-" + it.response!!)
                }
            }
            notificationDataViewModel?.completeOrderDataResponse!!.value = null
        })

    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)
        lastPositionSelected = position
        userID = ""
        orderModel = notificationListModel!!.notifications!![position]?.order
        when (string) {
            "Complete" -> {
                showCompleteDialog()
            }
            "SubmitReview" -> {
                showSubmitReviewDialog(0)
            }
            "ChatMessage" -> {
                isBuyingOrders = false
                if (orderModel?.seller?.id?.toInt() != Pref.getUserModel(activity!!)?.userDetails?.id!!) {
                    isBuyingOrders = true
                }

                serviceID = "" + orderModel?.service?.secret!!
                orderID = "" + orderModel?.orderNo!!

                chatTypeText = "" + orderModel?.orderNo!!

                if (isBuyingOrders) {
                    userID = orderModel?.seller?.secret!!
                    otherName = orderModel?.seller?.name!!
                } else {
                    userID = orderModel?.user?.secret!!
                    otherName = orderModel?.user?.name!!
                }
                if (!TextUtils.isEmpty(userID)) {
                    notificationDataViewModel!!.createConversation(userID, serviceID, orderID)
                }
            }
            "OrderNote" -> {
                showNoteForOrderDialog()
            }
        }
    }

    private fun upDateUi() {
        if (!notificationListModel?.notifications.isNullOrEmpty()) {
            rootView!!.rvNotification.makeVisible()
            rootView!!.tvEmptyNotification.makeGone()
        } else {
            rootView!!.rvNotification.makeGone()
            rootView!!.tvEmptyNotification.makeVisible()
        }
    }

    //TODO: Show Complete Order Dialog
    private fun showCompleteDialog() {
        val completeOrderDialog = Dialog(activity!!, R.style.dialogTheme)
        completeOrderDialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        completeOrderDialog.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                activity!!,
                R.drawable.dialog_bg
            )
        )
        completeOrderDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        completeOrderDialog.setCancelable(false)
        completeOrderDialog.setContentView(R.layout.dialog_confirm_for_complete_order)
        completeOrderDialog.show()
        var name = orderModel!!.seller!!.name
        completeOrderDialog.tvSellerTitle.text = name + " Send Your Delivery"
        completeOrderDialog.btnCompleteAndReview.setOnClickListener {
            showSubmitReviewDialog(1)
            completeOrderDialog.dismiss()
        }
        completeOrderDialog.btnCompleteOrder.setOnClickListener {
            completeOrderDialog.dismiss()
            notificationDataViewModel?.callCompleteOrderFromId(orderModel!!.orderNo!!)
        }
        completeOrderDialog.imgCloseCompleteOrderDialog.setOnClickListener {
            completeOrderDialog.dismiss()
        }
    }

    //TODO: Show Rating and Review Dialog
    private fun showSubmitReviewDialog(review: Int) {
        val reviewRatingDialog = Dialog(activity!!, R.style.dialogTheme)
        reviewRatingDialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        reviewRatingDialog.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                activity!!,
                R.drawable.dialog_bg
            )
        )
        reviewRatingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        reviewRatingDialog.setCancelable(false)
        reviewRatingDialog.setContentView(R.layout.dialog_confirm_for_complete_order_with_review)
        reviewRatingDialog.show()
        reviewRatingDialog.tvOrderName.text = orderModel!!.service!!.title
        reviewRatingDialog.tvSellerName.text = orderModel!!.seller!!.name
        reviewRatingDialog.btnSubmitReview.setOnClickListener {
            if (reviewRatingDialog.edtReview.text.toString().isBlank()) {
                homeController.showToast("Please write a review!")
            } else {
                reviewRatingDialog.cancel()
                if (review == 0) {
                    notificationDataViewModel?.callAddOnlyRatingForOrder(
                        orderModel!!.orderNo!!,
                        reviewRatingDialog.edtReview.text.toString(),
                        "" + reviewRatingDialog.ratingBarReview.rating
                    )
                } else {
                    notificationDataViewModel?.callAddRatingForOrder(
                        orderModel!!.orderNo!!,
                        reviewRatingDialog.edtReview.text.toString(),
                        "" + reviewRatingDialog.ratingBarReview.rating
                    )
                }
            }
        }
        reviewRatingDialog.imgCloseCompleteOrderWithReviewDialog.setOnClickListener {
            reviewRatingDialog.dismiss()
        }
    }


    //TODO: Show Not For Order Dialog
    private fun showNoteForOrderDialog() {
        val noteDialog = Dialog(activity!!, R.style.dialogTheme)
        noteDialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        noteDialog.window!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                activity!!,
                R.drawable.dialog_bg
            )
        )
        noteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        noteDialog.setCancelable(false)
        noteDialog.setContentView(R.layout.dialog_show_note_for_seller)
        noteDialog.show()

        var strNote = "" + orderModel!!.order_note
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            noteDialog.tvSellerNote.text =
                Html.fromHtml(strNote, 0)
        } else {
            noteDialog.tvSellerNote.text =
                Html.fromHtml(strNote)
        }
        noteDialog.imgCloseDialogForNote.setOnMyClickListener {
            noteDialog.cancel()
        }

        noteDialog.tvDone.setOnMyClickListener {
            noteDialog.cancel()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!rootView!!.pullToRefresh.isRefreshing) {
            notificationListModel?.notifications?.clear()
            notificationDataViewModel?.getAllNotification(0)
        }
        homeController.llToolbar.makeVisible()
        homeController.llBottomBar.makeVisible()
        homeController.imgLogo.makeVisible()
        homeController.imgBack.makeGone()
        homeController.txtTitle.makeVisible()
        homeController.cardProfile.makeVisible()

        homeController.txtTitle.text = getString(R.string.txt_notification)
        homeController.imgNotification?.let {
            val selected: Int = homeController.lLayout1.indexOfChild(it)
            if (selected != 0) {
                TransitionManager.beginDelayedTransition(
                    homeController.lLayout1,
                    ChangeBounds()
                )
                homeController.lLayout1.removeView(it)
                homeController.lLayout1.addView(it, 0)
                homeController.llEmpty?.let {
                    homeController.lLayout1.removeView(it)
                    homeController.lLayout1.addView(it, 1)
                }
                homeController.imgNotification.setImageResource(R.mipmap.ic_notification_selected)
                homeController.imgMessage.setImageResource(R.mipmap.ic_message_unselected)
                homeController.imgProject.setImageResource(R.mipmap.ic_project_unselected)
            }
        }
    }

    override fun onCreateAnimation(
        transit: Int,
        enter: Boolean,
        nextAnim: Int
    ): Animation? {
        return AnimationUtils.loadAnimation(
            activity,
            if (enter) android.R.anim.fade_in else android.R.anim.fade_out
        )
    }

}