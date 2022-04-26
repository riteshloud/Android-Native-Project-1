package com.demo.view.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.*
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_project_work_progress_item.view.*

class MyProjectsAdapter(
    context: Context,
    whichAdapter: String,
    projectsOrders: ArrayList<ProjectsOrdersDataModel.Order?>?,
    onListClickListener: OnListClickListener
) : RecyclerView.Adapter<MyProjectsAdapter.MyHolder>() {
    var context = context
    var whichAdapter = whichAdapter
    var onListClickListener = onListClickListener
    var projectsOrders = projectsOrders

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent.inflate(R.layout.row_project_work_progress_item, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val projectData = projectsOrders!![position]!!
        holder.itemView.tvProjectTitle.text = projectData.service!!.title
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

        if (position == projectsOrders!!.size - 1) {
            holder.itemView.viewEmptyForLastOrder.makeVisible()
        } else {
            holder.itemView.viewEmptyForLastOrder.makeGone()
        }

    }

    override fun getItemCount(): Int {
        return projectsOrders!!.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}