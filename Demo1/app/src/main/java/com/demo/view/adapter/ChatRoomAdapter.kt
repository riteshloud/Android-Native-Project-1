package com.demo.view.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.demo.R
import com.demo.utilities.UTILS
import com.demo.utilities.inflate
import com.demo.view.interfaces.OnListClickListener
import com.demo.view.ui.activities.WebViewActivity
import kotlinx.android.synthetic.main.row_chat_detail_item.view.*


class ChatRoomAdapter(
    context: Context,
    onListClickListener: OnListClickListener,
    chatArrayList: ArrayList<ConversationData>,
    user_id: String
) : RecyclerView.Adapter<ChatRoomAdapter.MyHolder>() {
    var context = context
    var onListClickListener = onListClickListener
    var chatArrayList = chatArrayList
    var user_id = user_id
    var imageExtArray = arrayOf(".png", ".jpg", ".jpeg", ".gif");
    var emojiString = "&#"
    var addTimeDifference: Long = 0
    var markMsgAsRead: Boolean = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent.inflate(R.layout.row_chat_detail_item, false))
    }

    fun setTimeDiff(timeDiff: Long) {
        addTimeDifference = timeDiff
    }

    fun setMarkAsRead(isRead: Boolean) {
        markMsgAsRead = isRead
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        var messageModel = chatArrayList[position]
        if (messageModel.isAdmin == "0" && messageModel.fromUser?.id.toString() == user_id) {

//            Log.e("ChatRoomAdapter"," from_user id: "+messageModel.from_user.id)
//            Log.e("ChatRoomAdapter"," user_id id: "+user_id)

            holder.itemView.llUserSide.makeVisible()
            holder.itemView.llSenderSide.makeGone()
            holder.itemView.llUserMessage.makeGone()
            holder.itemView.llUserOtherDocument.makeGone()
            holder.itemView.llUserImageDocument.makeGone()
            holder.itemView.llUserService.makeGone()

//            holder.itemView.tvUserTimeAgo.text = messageModel.time
            var timeAgo =
                UTILS.getTimeAgo(context, messageModel!!.timestamp!!.toLong() + addTimeDifference)
            holder.itemView.tvUserTimeAgo.text = timeAgo
            if (messageModel.is_read == 1 || markMsgAsRead) {
                messageModel.is_read = 1
                holder.itemView.ivUserMsgRead.setImageResource(R.mipmap.ic_double_blue_tick)
            } else {
                holder.itemView.ivUserMsgRead.setImageResource(R.mipmap.ic_double_gray_tick)
            }

            var attachmentType = messageModel.attachment
            var messageStr = messageModel.message

            if (attachmentType == 0) {
                if (messageModel.is_service_preview) {
                    //service
                    holder.itemView.llUserService.makeVisible()
                    Glide.with(context).load(messageModel.service_details?.image_url)
                        .error(R.mipmap.ic_splash_screen)
                        .into(holder.itemView.imgPhoto)
                    holder.itemView.tvSrviceTitle.text = messageModel.service_details?.title
                    holder.itemView.ratingBar.rating =
                        messageModel.service_details?.rating?.toFloat()!!
                    holder.itemView.tvReviews.text =
                        "( " + messageModel.service_details?.total_review + " Reviews )"
                    holder.itemView.tvPrice.text =
                        "Starting at $" + messageModel.service_details?.price
                    holder.itemView.tvViewService.setOnClickListener {
                        context.startActivity(
                            Intent(context, WebViewActivity::class.java).putExtra(
                                "url",
                                messageModel.service_details?.service_url
                            ).putExtra("title", messageModel.service_details?.title)
                        )
                    }
                } else {
                    //text
                    holder.itemView.llUserMessage.makeVisible()
//                    messageStr = "The Weeknd &#x1f604 King Of The Fall &#x1f604 fgfg &#x1f60a fdfdf &#x1f61b dffg &#x1f62d"
//                    messageStr = "hey \uD83E\uDDD1\u200D\uD83D\uDCBC hi"
                    if (messageStr.contains(emojiString)) {
                        setHtmlText(holder.itemView.txtMessageUser, messageStr)
                    } else {
                        holder.itemView.txtMessageUser.text = messageStr
                    }
//                    messageStr = "hey \uD83E\uDDD1\u200D\uD83D\uDCBC hi"
//                    holder.itemView.txtMessageUser.text = messageStr
                }

            } else if (attachmentType == 1) {

                if (messageStr.endsWith(imageExtArray[0]) || messageStr.endsWith(imageExtArray[1]) ||
                    messageStr.endsWith(imageExtArray[2]) || messageStr.endsWith(imageExtArray[3])
                ) {
                    // image
                    holder.itemView.llUserImageDocument.makeVisible()

                    Glide.with(context).load(messageStr)
                        .into(holder.itemView.imgUserPhoto)
                    holder.itemView.imgUserPhoto.setOnClickListener {
                        onListClickListener.onListClickSimple(position, messageStr)
                    }
                } else {
                    // doc
                    holder.itemView.llUserOtherDocument.makeVisible()
                    holder.itemView.tvDocFileName.text = messageModel.file_name
                    holder.itemView.llUserOtherDocument.setOnClickListener {
                        onListClickListener.onListClickSimple(position, messageStr)
                    }
                }
            }
            Glide.with(context).load(messageModel.fromUser?.profilePhoto)
                .apply(RequestOptions().placeholder(R.mipmap.ic_user_profile))
                .into(holder.itemView.imgUser)

        } else {

//            Log.e("ChatRoomAdapter"," from_user id: "+messageModel.to_user.id)
//            Log.e("ChatRoomAdapter"," user_id id: "+user_id)

            holder.itemView.llSenderSide.makeVisible()
            holder.itemView.llUserSide.makeGone()

            holder.itemView.llSenderMessage.makeGone()
            holder.itemView.llSenderImageDocument.makeGone()
            holder.itemView.llSenderOtherDocument.makeGone()
            holder.itemView.llSenderService.makeGone()
//                holder.itemView.tvSenderTimeAgo.text = messageModel.time
            var timeAgo =
                UTILS.getTimeAgo(context, messageModel!!.timestamp!!.toLong() + addTimeDifference)
            holder.itemView.tvSenderTimeAgo.text = timeAgo
            var attachmentType = messageModel.attachment
            var messageStr = messageModel.message
            if (attachmentType == 0) {

                if (messageModel.is_service_preview) {
                    //service
                    holder.itemView.llSenderService.makeVisible()
                    Glide.with(context).load(messageModel.service_details?.image_url)
                        .error(R.mipmap.ic_splash_screen)
                        .into(holder.itemView.imgSenderPhoto)
                    holder.itemView.tvSenderServiceTitle.text = messageModel.service_details?.title
                    holder.itemView.SenderRatingBar.rating =
                        messageModel.service_details?.rating?.toFloat()!!
                    holder.itemView.tvServiceReviews.text =
                        "( " + messageModel.service_details?.total_review + " Reviews )"
                    holder.itemView.tvServicePrice.text =
                        "Starting at $" + messageModel.service_details?.price
                    holder.itemView.tvSenderViewService.setOnClickListener {
                        context.startActivity(
                            Intent(context, WebViewActivity::class.java).putExtra(
                                "url",
                                messageModel.service_details?.service_url
                            ).putExtra("title", messageModel.service_details?.title)
                        )
                    }

                } else {
                    // text
                    holder.itemView.llSenderMessage.makeVisible()
//                    messageStr = "The Weeknd &#x1f604; King Of The Fall &#x1f604; fgfg &#x1f60a fdfdf &#x1f61b; dffg &#x1f62d;"
//                    messageStr = "The Weeknd &#x1f614\nKing Of The Fall&#x1f604"
                    if (messageStr.contains(emojiString)) {
                        setHtmlText(holder.itemView.txtMessageSender, messageStr)
                    } else {
                        holder.itemView.txtMessageSender.text = messageStr
                    }
//                    holder.itemView.txtMessageSender.text = messageStr
                }
            } else if (attachmentType == 1) {

                if (messageStr.endsWith(imageExtArray[0]) || messageStr.endsWith(imageExtArray[1]) ||
                    messageStr.endsWith(imageExtArray[2]) || messageStr.endsWith(imageExtArray[3])
                ) {
                    // image
                    holder.itemView.llSenderImageDocument.makeVisible()
                    Glide.with(context).load(messageStr)
                        .into(holder.itemView.imgSenderUserPhoto)
                    holder.itemView.imgSenderUserPhoto.setOnClickListener {
                        onListClickListener.onListClickSimple(position, messageStr)
                    }

                } else {
                    // doc
                    holder.itemView.llSenderOtherDocument.makeVisible()
                    holder.itemView.llSenderOtherDocument.setOnClickListener {
                        onListClickListener.onListClickSimple(position, messageStr)
                    }
                    holder.itemView.tvSenderDocFileName.text = messageModel.file_name
                }
            }
        }

        if (messageModel.isAdmin == "1") {
            Glide.with(context).load(R.mipmap.ic_logo)
                .into(holder.itemView.imgSender)
        } else {
            Glide.with(context).load(messageModel.fromUser?.profilePhoto)
                .apply(RequestOptions().placeholder(R.mipmap.ic_user_profile))
                .into(holder.itemView.imgSender)
        }

        holder.itemView.llSenderSide.setOnLongClickListener {
          //  onListClickListener.onListClick(position, messageModel)
            true // <- set to true
        }

        holder.itemView.llUserSide.setOnLongClickListener {

          //  onListClickListener.onListClick(position, messageModel)
            true // <- set to true
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
    return chatArrayList.size
}

class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}