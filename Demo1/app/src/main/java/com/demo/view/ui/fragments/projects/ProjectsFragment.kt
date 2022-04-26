package com.demo.view.ui.fragments.projects

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.Constants
import com.demo.view.adapter.MyProjectsAdapter
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.activities.WebViewActivity
import com.demo.view.ui.base.BaseFragment
import com.demo.view.ui.fragments.chats.ChatFragment
import com.demo.view.ui.fragments.chats.ChatListFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.bottom_bar_layout.*
import kotlinx.android.synthetic.main.dialog_confirm_for_complete_order.*
import kotlinx.android.synthetic.main.dialog_confirm_for_complete_order_with_review.*
import kotlinx.android.synthetic.main.dialog_show_note_for_seller.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.fragment_projects.*
import kotlinx.android.synthetic.main.fragment_projects.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.json.JSONObject
import java.lang.Exception

class ProjectsFragment : BaseFragment() {

    var rootView: View? = null
    var isBuyingOrders = true
    private var lastPositionSelected: Int = 0

    var ordersDataViewModel: ProjectsDataViewModel? = null

    var buyerOrdersModel: ProjectsOrdersDataModel? = null
    var sellerOrdersModel: ProjectsOrdersDataModel? = null
    var orderModel: ProjectsOrdersDataModel.Order? = null
    var isInitial = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_projects, container, false)
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

        ordersDataViewModel =
            ViewModelProvider(this, MyViewModelFactory(ProjectsDataViewModel(activity!!))).get(
                ProjectsDataViewModel::class.java
            )
    }

    private fun setUp() {

        addObserver()

        ordersDataViewModel?.getBuyerOrders(0)

    }

    private fun addObserver() {

        ordersDataViewModel!!.isLoading!!.observe(activity!!, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })

        ordersDataViewModel!!.responseError!!.observe(activity!!, Observer {
            val res = it.string()
            val jsonObject = JSONObject(res)
            if (jsonObject.getInt("code") == 400) {

                Toast.makeText(homeController, jsonObject.getString("message"), Toast.LENGTH_SHORT)
                    .show()

            } else {
                homeController!!.errorBody(it)
            }

        })

        ordersDataViewModel!!.buyerOrdersDataResponse!!.observe(activity!!, Observer {
            it?.let {
                buyerOrdersModel = it
                (rootView!!.rvBuyingOrders.adapter as MyProjectsAdapter?)?.notifyDataSetChanged()
                    ?: run {
                        try {
                            rootView!!.rvBuyingOrders.adapter =
                                MyProjectsAdapter(
                                    activity!!,
                                    "BuyingOrder",
                                    buyerOrdersModel!!.orders!!,
                                    this
                                )
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                upDateUi()
            }
        })

        ordersDataViewModel!!.sellerOrdersDataResponse!!.observe(activity!!, Observer {
            it?.let {
                sellerOrdersModel = it
                (rootView!!.rvSellerOrders.adapter as MyProjectsAdapter?)?.notifyDataSetChanged()
                    ?: run {
                        try {
                            rootView!!.rvSellerOrders.adapter =
                                MyProjectsAdapter(
                                    activity!!,
                                    "SellerOrder",
                                    sellerOrdersModel!!.orders!!,
                                    this
                                )
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                upDateUi()
            }
        })

        ordersDataViewModel!!.completeOrderDataResponse!!.observe(activity!!, Observer {
            homeController.showToast(it!!.message!!)
            buyerOrdersModel?.orders!![lastPositionSelected] = it.response
            //   rootView!!.rvBuyingOrders.adapter!!.notifyItemChanged(lastPositionSelected)
            rootView!!.rvBuyingOrders.adapter!!.notifyDataSetChanged()
//            Log.v("===DATA==", "-" + buyerOrdersModel?.orders!!)
        })

        ordersDataViewModel!!.createConversation!!.observe(activity!!, Observer {
//            var msgId = it.message_id
            var msgId = it.message_secret

            /* ChatFragment().apply {
             }.let {

             }*/

            var userID = ""
            var type = ChatListFragment.TYPE_ORDERS
            var orderID = orderModel!!.orderNo!!
            var serviceId = orderModel!!.service!!.secret!!
            var otherImage = ""
            var otherName = ""
            var chatTypeText = ""+orderModel!!.orderNo!!
            var isAdmin = "0"

            if (isBuyingOrders) {
                userID = orderModel!!.seller!!.secret!!
                otherName = orderModel!!.seller!!.name!!
            } else {
                userID = orderModel!!.user!!.secret!!
                otherName = orderModel!!.user!!.name!!
            }

            homeController.loadFragment(
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


        })

    }

    private fun upDateUi() {

        if (isBuyingOrders) {
            if (buyerOrdersModel != null && buyerOrdersModel?.orders?.size!! > 0) {
                rootView!!.llBuyingOrders.makeVisible()
                rootView!!.llSellerOrders.makeGone()
                rootView!!.rvBuyingOrders.makeVisible()
                rootView!!.tvEmptyBuyingOrder.makeGone()
            } else {
                rootView!!.rvBuyingOrders.makeGone()
                rootView!!.llSellerOrders.makeGone()
                rootView!!.tvEmptyBuyingOrder.makeVisible()
            }

        } else {
            if (sellerOrdersModel != null && sellerOrdersModel?.orders?.size!! > 0) {
                rootView!!.llBuyingOrders.makeGone()
                rootView!!.llSellerOrders.makeVisible()
                rootView!!.rvSellerOrders.makeVisible()
                rootView!!.tvEmptySellerOrders.makeGone()
            } else {
                rootView!!.rvSellerOrders.makeGone()
                rootView!!.tvEmptySellerOrders.makeVisible()
            }
        }


    }

    private fun addOnClickListener() {

        txtBuyingOrders.setOnMyClickListener {
            if (isBuyingOrders) return@setOnMyClickListener
            txtBuyingOrders.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
            txtSellerOrders.setTextColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.colorDarkGreen
                )
            )
            rootView!!.llBuyingOrders.makeVisible()
            rootView!!.llSellerOrders.makeGone()
            isBuyingOrders = true
            rootView!!.rvBuyingOrders.adapter = null
            ordersDataViewModel?.buyerOrdersDataResponse?.value?.orders?.clear()
            ordersDataViewModel?.buyerOrdersDataResponse?.value = null
            buyerOrdersModel = null
            ordersDataViewModel?.getBuyerOrders(0)

        }

        txtSellerOrders.setOnMyClickListener {
            if (!isBuyingOrders) return@setOnMyClickListener
            txtSellerOrders.setTextColor(ContextCompat.getColor(activity!!, R.color.colorAccent))
            txtBuyingOrders.setTextColor(ContextCompat.getColor(activity!!, R.color.colorDarkGreen))
            rootView!!.llBuyingOrders.makeGone()
            rootView!!.llSellerOrders.makeVisible()
            isBuyingOrders = false
            rootView!!.rvSellerOrders.adapter = null
            ordersDataViewModel?.sellerOrdersDataResponse?.value?.orders?.clear()
            ordersDataViewModel?.sellerOrdersDataResponse?.value = null
            sellerOrdersModel = null
            ordersDataViewModel?.getSellerOrders(0)

        }

        rootView!!.rvBuyingOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rvBuyingOrders.canScrollVertically(1) && buyerOrdersModel!!.paginationEnded && ordersDataViewModel?.isLoading?.value == false) {
                    ordersDataViewModel?.getBuyerOrders(buyerOrdersModel?.orders!!.size)
                }
            }
        })

        rootView!!.rvSellerOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rvSellerOrders.canScrollVertically(1) && sellerOrdersModel!!.paginationEnded && ordersDataViewModel?.isLoading?.value == false) {
                    ordersDataViewModel?.getSellerOrders(sellerOrdersModel?.orders!!.size)
                }
            }
        })

        btnSearchJob.setOnClickListener {
            startActivity(
                Intent(activity, WebViewActivity::class.java).putExtra(
                    "url",
                    Constants.SEARCH_JOB
                ).putExtra("isPdf", false).putExtra("title", "Search Job")
            )
            activity!!.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)
        lastPositionSelected = position
        when (string) {
            "Complete" -> {
                showCompleteDialog()
            }
            "SubmitReview" -> {
                showSubmitReviewDialog(0)
            }
            "ChatMessage" -> {
                if (isBuyingOrders) {
                    orderModel = buyerOrdersModel!!.orders!![position]!!
                    var userID = orderModel!!.seller!!.secret!!
                    var serviceID = orderModel!!.service!!.secret!!
                    var orderID = orderModel!!.orderNo!!
                    ordersDataViewModel!!.createConversation(userID, serviceID, orderID)

                } else {
                    orderModel = sellerOrdersModel!!.orders!![position]!!
                    var userID = orderModel!!.user!!.secret!!
                    var serviceID = orderModel!!.service!!.secret!!
                    var orderID = orderModel!!.orderNo!!

                    ordersDataViewModel!!.createConversation(userID, serviceID, orderID)

                }


            }
            "OrderNote" -> {
                showNoteForOrderDialog()
            }
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
        var name = buyerOrdersModel?.orders!![lastPositionSelected]!!.seller!!.name
        completeOrderDialog.tvSellerTitle.text = name + " Send Your Delivery"
        completeOrderDialog.btnCompleteAndReview.setOnClickListener {
            showSubmitReviewDialog(1)
            completeOrderDialog.dismiss()
        }
        completeOrderDialog.btnCompleteOrder.setOnClickListener {
            completeOrderDialog.dismiss()
            ordersDataViewModel?.callCompleteOrderFromId(buyerOrdersModel?.orders!![lastPositionSelected]!!.orderNo!!)
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
        reviewRatingDialog.tvOrderName.text =
            buyerOrdersModel?.orders!![lastPositionSelected]!!.service!!.title
        reviewRatingDialog.tvSellerName.text =
            buyerOrdersModel?.orders!![lastPositionSelected]!!.seller!!.name
        reviewRatingDialog.btnSubmitReview.setOnClickListener {
            if (reviewRatingDialog.edtReview.text.toString().isBlank()) {
                homeController.showToast("Please write a review!")
            } else {
                reviewRatingDialog.cancel()
                if (review == 0) {
                    ordersDataViewModel?.callAddOnlyRatingForOrder(
                        buyerOrdersModel?.orders!![lastPositionSelected]!!.orderNo!!,
                        reviewRatingDialog.edtReview.text.toString(),
                        "" + reviewRatingDialog.ratingBarReview.rating
                    )
                } else {
                    ordersDataViewModel?.callAddRatingForOrder(
                        buyerOrdersModel?.orders!![lastPositionSelected]!!.orderNo!!,
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

        var strNote = "" + sellerOrdersModel?.orders!![lastPositionSelected]!!.order_note
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
        homeController.llToolbar.makeVisible()
        homeController.llBottomBar.makeVisible()
        homeController.imgLogo.makeVisible()
        homeController.imgBack.makeGone()
        homeController.txtTitle.makeVisible()
        homeController.cardProfile.makeVisible()

        homeController.txtTitle.text = getString(R.string.txt_project)

        homeController.imgProject?.let {
            val selected: Int = homeController.lLayout1.indexOfChild(it)
            if (selected != 0) {
                TransitionManager.beginDelayedTransition(homeController.lLayout1, ChangeBounds())
                homeController.lLayout1.removeView(it)
                homeController.lLayout1.addView(it, 0)
                homeController.llEmpty?.let {
                    homeController.lLayout1.removeView(it)
                    homeController.lLayout1.addView(it, 1)
                }
                homeController.imgProject.setImageResource(R.mipmap.ic_project_selected)
                homeController.imgMessage.setImageResource(R.mipmap.ic_message_unselected)
                homeController.imgNotification.setImageResource(R.mipmap.ic_notification_unselected)
            }
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return AnimationUtils.loadAnimation(
            activity,
            if (enter) android.R.anim.fade_in else android.R.anim.fade_out
        )
    }
}