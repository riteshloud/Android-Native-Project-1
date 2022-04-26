package com.demo.view.adapter

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.R
import com.demo.utilities.*
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_user_chat_item.view.*

class ChatUserListAdapter(
    context: Context,
    conversation: ArrayList<ConversationData?>?,
    onListClickListener: OnListClickListener
) : RecyclerView.Adapter<ChatUserListAdapter.MyHolder>() {
    var context = context
    var onListClickListener = onListClickListener
    var conversation = conversation
    var myUserId = Pref.getUserModel(context)?.userDetails?.id.toString()
    var emojiString = "&#"
    var addTimeDifference: Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent.inflate(R.layout.row_user_chat_item, false))
    }

    fun setTimeDiff(timeDiff: Long) {
        addTimeDifference = timeDiff
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        var userData = conversation!![position]!!

//        holder.itemView.tvTimeAgo.text = userData.time
        var timeAgo = UTILS.getTimeAgo(context, userData!!.timestamp!!.toLong() + addTimeDifference)
        holder.itemView.tvTimeAgo.text = timeAgo
        var msgCount = userData.messageDetailCount!!.toInt()
        if (msgCount > 0) {
            holder.itemView.tvMessageCount.makeVisible()
            holder.itemView.tvMessageCount.text = "" + msgCount
        } else {
            holder.itemView.tvMessageCount.makeGone()
        }

        var messageStr = userData.lastMessage
        if (messageStr!!.contains(emojiString)) {
            setHtmlText(holder.itemView.tvChatUserLastMessage, messageStr)
        } else {
            holder.itemView.tvChatUserLastMessage.text = messageStr
        }

        if (userData.isAdmin == "1") {
            holder.itemView.tvChatUserName.text = "demo Support Team"
            holder.itemView.imgProfile.setImageResource(R.mipmap.ic_logo)
        } else {

            if (userData.toUser != null && userData.toUser!!.id.toString() != myUserId) {
                var imageUrl = userData.toUser!!.profilePhoto
                if (!TextUtils.isEmpty(imageUrl)) {
                    holder.itemView.imgProfile.loadFromUrl(imageUrl!!)
                } else {
                    Glide.with(context).load(R.mipmap.ic_user_profile)
                        .into(holder.itemView.imgProfile)
                }
                holder.itemView.tvChatUserName.text = userData.toUser?.name
            } else if (userData.fromUser != null &&  userData.fromUser!!.id.toString() != myUserId) {
                var imageUrl = userData.fromUser?.profilePhoto
                if (!TextUtils.isEmpty(imageUrl)) {
                    holder.itemView.imgProfile.loadFromUrl(imageUrl!!)
                } else {
                    Glide.with(context).load(R.mipmap.ic_user_profile)
                        .into(holder.itemView.imgProfile)
                }
                holder.itemView.tvChatUserName.text = userData.fromUser?.name
            }
        }

        holder.itemView.llUserLabel.setOnClickListener {
            onListClickListener.onListClickSimple(position, "s")
        }

        var userType = ""+userData.type
        when (userType) {

            "users" -> {
                holder.itemView.tvServiceAndOrder.makeGone()
            }
            "services" -> {
                holder.itemView.tvServiceAndOrder.makeVisible()
                holder.itemView.tvServiceAndOrder.text = "Service: " + userData.serviceName
            }
            "orders" -> {
                holder.itemView.tvServiceAndOrder.makeVisible()
                holder.itemView.tvServiceAndOrder.text = "Order no.: " + userData.orderNo
            }
        }

        if (position == conversation!!.size - 1) {
            holder.itemView.emptyViewForLastSpace.makeVisible()
            holder.itemView.lineViewForLast.makeGone()
        } else {
            holder.itemView.emptyViewForLastSpace.makeGone()
            holder.itemView.lineViewForLast.makeVisible()
        }

    }

    private fun setHtmlText(mTextView: TextView?, message: String) {
        mTextView?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(message)
        }
    }

    override fun getItemCount(): Int {
        return conversation!!.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}