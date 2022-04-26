package com.demo.view.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.inflate
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_block_user_list.view.*

class BlockuserListAdapter(
    context: Context,
    blockUserList: ArrayList<BlockUserListModel.BlockUser>,
    onListClickListener: OnListClickListener
) : RecyclerView.Adapter<BlockuserListAdapter.MyHolder>() {
    var context = context
    var onListClickListener = onListClickListener
    var blockUserList = blockUserList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent.inflate(R.layout.row_block_user_list, false))
    }


    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        var userData = blockUserList!![position]!!
        holder.itemView.tv_user_name.text = userData.Name
        holder.itemView.tv_user_email.text = userData.username
        holder.itemView.tv_date.text = userData.date

        holder.itemView.cv_unblock.setOnMyClickListener {
            onListClickListener.onListClick(position, userData);
        }
//
    }


    override fun getItemCount(): Int {
        return blockUserList!!.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}