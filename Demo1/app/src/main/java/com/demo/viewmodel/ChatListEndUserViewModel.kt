package com.demo.viewmodel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.utilities.Constants
import com.demo.utilities.Pref
import com.demo.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatListEndUserViewModel(activity: Activity) : ViewModel() {

    var chatUserListResponse: MutableLiveData<ChatUserListModel>? = MutableLiveData()

    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity

    fun getEndUserChatList(
        user_id: String,
        search_text: String,
        offset: Int
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).getEndUserChatList(
            Pref.getPrefAuthorizationToken(context),
            user_id = user_id,
            search_text = search_text,
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<ChatUserListModel> {
            override fun onFailure(call: Call<ChatUserListModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<ChatUserListModel>,
                response: Response<ChatUserListModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (offset == 0) {
                        chatUserListResponse?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()?.conversations!!.size >= Constants.paginationLimit
                        }
                    }else{
                        chatUserListResponse?.value = chatUserListResponse?.value.apply {
                            this!!.conversations!!.addAll(response.body()?.conversations!!)
                            this.paginationEnded =
                                response.body()?.conversations!!.size >= Constants.paginationLimit
                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

}