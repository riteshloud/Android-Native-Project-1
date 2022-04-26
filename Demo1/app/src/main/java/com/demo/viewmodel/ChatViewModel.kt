package com.demo.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.model.*
import com.demo.utilities.Constants
import com.demo.utilities.Pref
import com.demo.view.service.ApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatViewModel(activity: Activity) : ViewModel() {

    //    var chatList: MutableLiveData<ChatUserListModel>? = MutableLiveData()
//    var createConversation: MutableLiveData<CreateConversationModel>? = MutableLiveData()
//    var authenticationPusher: MutableLiveData<PusherAuthDataModel>? = MutableLiveData()
    var blockUserListModel: MutableLiveData<BlockUserListModel>? = MutableLiveData()

    var fetchMessages: MutableLiveData<FetchMessageDataModel>? = MutableLiveData()
    var sendMessage: MutableLiveData<SendMessageModel>? = MutableLiveData()
    var blockUserModel: MutableLiveData<BlockUserModel>? = MutableLiveData()

    var templateResponse: MutableLiveData<TemplateResponse>? = MutableLiveData()
    var saveTemplateResponse: MutableLiveData<SaveTemplateResponse>? = MutableLiveData()
    var serviceResponse: MutableLiveData<ServiceResponseModel>? = MutableLiveData()
    var reportSpamResponse: MutableLiveData<ReportSpamResponse>? = MutableLiveData()
//    var makeChatSeenResponse: MutableLiveData<BaseResponse>? = MutableLiveData()

    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity
    fun fetchBlockuser(
        offset: Int
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).fetchBlockUserList(
            Pref.getPrefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<BlockUserListModel> {
            override fun onFailure(call: Call<BlockUserListModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<BlockUserListModel>,
                response: Response<BlockUserListModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
//                    fetchMessages!!.value=response.body()

                    if (offset == 0) {
                        blockUserListModel?.value = response.body().apply {
                            this?.isPaginationEnded =
                                response.body()?.block_user_list?.size!! >= Constants.paginationLimit
                        }
                    } else {
                        blockUserListModel?.value =
                            blockUserListModel?.value.apply {
                                this?.block_user_list?.addAll(response.body()?.block_user_list!!)
                                this?.isPaginationEnded =
                                    response.body()?.block_user_list?.size!! >= Constants.paginationLimit
                            }
                    }

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }
    /*fun authenticatePusher(
        socket_id: String,
        channel_name: String

    ) {
        isLoading?.value = true
        ApiClient.getClient(context).authenticatePusher(
            Pref.getPrefAuthorizationToken(context),
            socket_id = socket_id,
            channel_name = channel_name
        ).enqueue(object : Callback<PusherAuthDataModel> {
            override fun onFailure(call: Call<PusherAuthDataModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<PusherAuthDataModel>,
                response: Response<PusherAuthDataModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    authenticationPusher!!.value=response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }*/

    fun fetchMessages(
        message_id: String,
        offset: Int
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).fetchMessages(
            Pref.getPrefAuthorizationToken(context),
            message_id = message_id,
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<FetchMessageDataModel> {
            override fun onFailure(call: Call<FetchMessageDataModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<FetchMessageDataModel>,
                response: Response<FetchMessageDataModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
//                    fetchMessages!!.value=response.body()

                    if (fetchMessages?.value == null) {
                        fetchMessages?.value = response.body().apply {
                            this?.isPaginationEnded =
                                response.body()?.conversations?.size!! >= Constants.paginationLimit
                        }
                    } else {
                        if (offset == 0) {

                            Log.v("=======", "-")
                            fetchMessages?.value = response.body().apply {
                                this?.isPaginationEnded =
                                    response.body()?.conversations?.size!! >= Constants.paginationLimit
                            }
                        } else {
                            fetchMessages?.value =
                                fetchMessages?.value.apply {
                                    this?.conversations?.addAll(response.body()?.conversations!!)
                                    this?.isPaginationEnded =
                                        response.body()?.conversations?.size!! >= Constants.paginationLimit
                                }
                        }
                    }

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun sendMessages(
        message: RequestBody,
        user_id: RequestBody,
        type: RequestBody,
        order_id: RequestBody,
        service_id: RequestBody,
        message_id: RequestBody,
        deviceType: RequestBody,
        attachment: MultipartBody.Part?
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).sendMessage(
            authorization = Pref.getPrefAuthorizationToken(context),
            message = message,
            user_id = user_id,
            type = type,
            order_id = order_id,
            service_id = service_id,
            message_id = message_id,
            deviceType = deviceType,
            attachment = attachment
        ).enqueue(object : Callback<SendMessageModel> {
            override fun onFailure(call: Call<SendMessageModel>, t: Throwable) {
                Log.e("sendMessage", " onFailure ", t)
                Log.e("sendMessage", " onFailure " + t.message)
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<SendMessageModel>,
                response: Response<SendMessageModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    sendMessage!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun blockUser(
        secret: String,
        is_block: Int
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).blockUser(
            authorization = Pref.getPrefAuthorizationToken(context),
            secret = secret,
            is_block = is_block
        ).enqueue(object : Callback<BlockUserModel> {
            override fun onFailure(call: Call<BlockUserModel>, t: Throwable) {
                Log.e("sendMessage", " onFailure ", t)
                Log.e("sendMessage", " onFailure " + t.message)
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<BlockUserModel>,
                response: Response<BlockUserModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    blockUserModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun saveTemplate(
        title: String,
        message: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).storeTemplate(
            Pref.getPrefAuthorizationToken(context),
            title,
            2, message
        ).enqueue(object : Callback<SaveTemplateResponse> {
            override fun onFailure(call: Call<SaveTemplateResponse>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<SaveTemplateResponse>,
                response: Response<SaveTemplateResponse>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    saveTemplateResponse!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun getTemplates() {
        isLoading?.value = true
        ApiClient.getClient(context).getTemplates(
            Pref.getPrefAuthorizationToken(context)
        ).enqueue(object : Callback<TemplateResponse> {
            override fun onFailure(call: Call<TemplateResponse>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<TemplateResponse>,
                response: Response<TemplateResponse>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    templateResponse!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun getServices() {
        isLoading?.value = true
        ApiClient.getClient(context).getService(
            Pref.getPrefAuthorizationToken(context)
        ).enqueue(object : Callback<ServiceResponseModel> {
            override fun onFailure(call: Call<ServiceResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<ServiceResponseModel>,
                response: Response<ServiceResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    serviceResponse!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun reportAsSpam(
        message_id: String,
        reason: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).reportAsSpam(
            Pref.getPrefAuthorizationToken(context),
            message_id = message_id,
            reason = reason
        ).enqueue(object : Callback<ReportSpamResponse> {
            override fun onFailure(call: Call<ReportSpamResponse>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<ReportSpamResponse>,
                response: Response<ReportSpamResponse>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    reportSpamResponse!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun makeChatSeen(message_id: String) {
//        isLoading?.value = true
        ApiClient.getClient(context).makeChatSeen(
            Pref.getPrefAuthorizationToken(context),
            message_id = message_id
        ).enqueue(object : Callback<BaseResponse> {
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
//                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
//                isLoading?.value = false
                if (response.isSuccessful) {
//                    makeChatSeenResponse!!.value = response.body()

                } else {
//                    responseError?.value = response.errorBody()
                }
            }
        })
    }
}