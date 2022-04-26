package com.demo.view.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.*
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_notification_item.view.*
import kotlinx.android.synthetic.main.row_project_work_progress_item.view.*

class NotificationListAdapter(
    var context: Context,
    notificationList: ArrayList<NotificationListModel.Notification?>?,
    var onListClickListener: OnListClickListener
) :
    RecyclerView.Adapter<NotificationListAdapter.MyHolder>() {

    var notificationList = notificationList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent.inflate(R.layout.row_notification_item, false))
    }

    fun updateAllData( list: ArrayList<NotificationListModel.Notification?>?){
//        this.notificationList?.clear()
        this.notificationList = list
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val notification = notificationList!![position]!!

//        holder.itemView.tvNotificationDate.text = UTILS.convertDate(UTILS.Date_Pattern_yyyyMMddTHHmmssSSSSSSZ,UTILS.Time_Pattern_MMMddhhmma,notification.time!!)
        holder.itemView.tvNotificationDate.text = notification.time!!
        holder.itemView.tvNotificationTitle.text = notification.message
        holder.itemView.tvNotificationTitle.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.dark_black
            )
        )

        var notificationType = notification.type
        var isNotificationRead = notification.isRead

        if (notificationType == "complete_order"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_clock_gray)
            }else {
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.lightGreen
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_update_date)
            }
        }else if (notificationType == "new_order"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_started)
            }else {
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.dark_black
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_started_gray)
            }
        }else if (notificationType == "job_proposal_send"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_order_gray)
            }else if (isNotificationRead == "0"){
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_order)
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.dark_black
                    )
                )
            }
        } else if (notificationType == "delivered_order"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_clock_gray)
            }else if (isNotificationRead == "0"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.lightGreen
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_update_date)
            }
        } else if (notificationType == "cancel_order"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_bell_gray)
            }else if (isNotificationRead == "0"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_bell)
            }
        }else if (notificationType == "custom_order"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_order_gray)
            }else if (isNotificationRead == "0"){
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_order)
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.dark_black
                    )
                )
            }
        } else if (notificationType == "extend_order_date"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_bell_gray)
            }else if (isNotificationRead == "0"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.red
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_bell)
            }
        } else if (notificationType == "dispute_order"){
            if (isNotificationRead == "1"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_gray
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_clock_gray)
            }else if (isNotificationRead == "0"){
                holder.itemView.tvNotificationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.lightGreen
                    )
                )
                holder.itemView.imgNotificationImage.setImageResource(R.mipmap.ic_notification_update_date)
            }
        }

        holder.itemView.llProjectDetail.tag = 0
        holder.itemView.llProjectDetail.makeGone()

        if (position==notificationList!!.size-1){
            holder.itemView.emptyViewForLastSpace.makeVisible()
            holder.itemView.viewBottom.makeGone()
        }else{
            holder.itemView.emptyViewForLastSpace.makeGone()
            holder.itemView.viewBottom.makeVisible()
        }

        val projectData = notificationList!![position]!!.order

        holder.itemView.setOnMyClickListener {

            try {
                val collapseKey = ""+projectData?.id!! // id is collapseKey for notification
                //clear notification from tray if available for this conversation
                if (!TextUtils.isEmpty(collapseKey)){
                    NotificationManagerCompat.from(context!!).cancel(collapseKey.toInt())
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

            if (projectData == null){
                return@setOnMyClickListener
            }

            var llDetailTag = holder.itemView.llProjectDetail.tag
            if (llDetailTag == 0){
                holder.itemView.llProjectDetail.makeVisible()
                holder.itemView.llProjectDetail.tag = 1
            }else{
                holder.itemView.llProjectDetail.makeGone()
                holder.itemView.llProjectDetail.tag = 0
            }
//            holder.itemView.llProjectDetail.tag = 0
//            onListClickListener.onListClickSimple(position, "read")
        }



        if ( projectData != null){
//            var whichAdapter = "BuyingOrder"
            var whichAdapter = "SellerOrder"

        if (projectData?.seller?.id?.toInt() != Pref.getUserModel(context)?.userDetails?.id!!){
            whichAdapter = "BuyingOrder"
        }
        holder.itemView.tvProjectTitle.text = projectData?.service!!.title
//        holder.itemView.tvProjectTitle.text = projectData?.service!!.ServiceTitle
        holder.itemView.tvProjectPrice.text = "$" + projectData.price
            var orderType = projectData.orderType
            if (!TextUtils.isEmpty(orderType)){
                holder.itemView.tvOrderType.text = orderType
            }
        holder.itemView.tvOrderNumber.text = projectData.orderNo
        holder.itemView.tvOrderStatus.text = projectData.orderStatus
        holder.itemView.tvOrderStartDate.text =
            UTILS.convertDate("yyyy-MM-dd hh:mm:ss", "MMM dd yyyy", projectData.startDate!!)
        holder.itemView.tvOrderEndDate.text =
            UTILS.convertDate("yyyy-MM-dd hh:mm:ss", "MMM dd yyyy", projectData.endDate!!) + " " +
                    UTILS.convertDate("yyyy-MM-dd hh:mm:ss", "hh:mm a", projectData.endDate!!)
                        .toUpperCase()

        if (whichAdapter == "BuyingOrder") {
            holder.itemView.llNotes.makeGone()
            holder.itemView.btnMessageSeller.makeGone()
            if (projectData.status.equals("new", ignoreCase = true)) {
                holder.itemView.btnMessage.makeGone()
            }else{
                holder.itemView.btnMessage.makeVisible()
            }

            holder.itemView.tvSellerBuyer.text = context.getString(R.string.seller_tag)
            holder.itemView.tvSellerName.text = " " + projectData.seller!!.name

            if (projectData.status == "delivered") {
                holder.itemView.btnCompleteOrder.makeVisible()
                holder.itemView.llReview.makeGone()
            } else if (projectData.status == "completed" && projectData.isReview == "0") {
                holder.itemView.llReview.makeVisible()
                holder.itemView.btnCompleteOrder.makeGone()
            } else {
                holder.itemView.llReview.makeGone()
                holder.itemView.btnCompleteOrder.makeGone()
            }

        } else if (whichAdapter == "SellerOrder") {
            holder.itemView.llNotes.makeVisible()
            holder.itemView.llReview.makeGone()
            holder.itemView.btnCompleteOrder.makeGone()
            holder.itemView.btnMessage.makeGone()
            if (projectData.status.equals("new", ignoreCase = true)) {
                holder.itemView.btnMessageSeller.makeGone()
            }else{
                holder.itemView.btnMessageSeller.makeVisible()
            }

            holder.itemView.tvSellerBuyer.text = context.getString(R.string.buyer_tag)
            holder.itemView.tvSellerName.text = " " + projectData.user!!.name

        }

        if (projectData.order_note.isNullOrEmpty()) {
            holder.itemView.btnNotes.visibility = View.GONE
        } else {
            holder.itemView.btnNotes.visibility = View.VISIBLE
        }

            holder.itemView.btnCompleteOrder.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, "Complete")
            }

            holder.itemView.btnReview.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, "SubmitReview")
            }

            holder.itemView.btnMessage.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, "ChatMessage")
            }
            holder.itemView.btnMessageSeller.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, "ChatMessage")
            }
            holder.itemView.btnNotes.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, "OrderNote")
            }

            /*holder.itemView.btnMessage.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, whichAdapter)
//                onListClickListener.onListClick(position, whichAdapter)
//                onSendMessageClicked(position, whichAdapter)
            }

            holder.itemView.btnMessageSeller.setOnMyClickListener {
                onListClickListener.onListClickSimple(position, whichAdapter)
//                onListClickListener.onListClick(position, whichAdapter)
//                onSendMessageClicked(position, whichAdapter)
            }*/

        }
    }

    override fun getItemCount(): Int {
        return notificationList!!.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
