package com.demo.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.inflate
import com.demo.utilities.isVisible
import com.demo.utilities.makeGone
import com.demo.utilities.makeVisible
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_project_deliver_item.view.*

class ProjectDeliverListAdapter(var context: Context, var onListClickListner: OnListClickListener) : RecyclerView.Adapter<ProjectDeliverListAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent.inflate(R.layout.row_project_deliver_item,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.itemView.llDeliveryItem.setOnClickListener {
            if (holder.itemView.llExpandedDetail.isVisible()) {
                holder.itemView.llDeliveryItem.background = ContextCompat.getDrawable(context,R.drawable.bg_edittext_auth)
                holder.itemView.tvDeliver.setTextColor(ContextCompat.getColor(context,R.color.colorDarkGreen))
                holder.itemView.llExpandedDetail.makeGone()
            }else{
                holder.itemView.llDeliveryItem.background = ContextCompat.getDrawable(context,R.drawable.bg_send_delivery_project)
                holder.itemView.tvDeliver.setTextColor(ContextCompat.getColor(context,R.color.colorAccent))
                holder.itemView.llExpandedDetail.makeVisible()
            }
        }

        holder.itemView.llSendDelivery.setOnClickListener {
            onListClickListner.onListClickSimple(position,"s")
        }

    }

    override fun getItemCount(): Int {
        return 10
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}