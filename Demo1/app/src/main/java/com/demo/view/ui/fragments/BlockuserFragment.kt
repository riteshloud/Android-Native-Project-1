package com.demo.view.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.UTILS
import com.demo.view.adapter.BlockuserListAdapter
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseFragment
import com.demo.view.ui.fragments.chats.ChatFragment
import com.demo.viewmodel.ChatViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_blockuser.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class BlockuserFragment : BaseFragment() {
    var rootView: View? = null
    var chatViewModel: ChatViewModel? = null
    var blockUserHandleAdapter: BlockuserListAdapter? = null
    var blockListModel: BlockUserListModel? = null
var blockuser : BlockUserListModel.BlockUser?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_blockuser, container, false)
            init()
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addObserver()
        callFetchBlockuserApi(0)
        homeController.imgBack.setOnClickListener {
            homeController.onBackPressed()
        }
        rootView!!.rv_block_user.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!rootView!!.rv_block_user.canScrollVertically(1) && blockListModel!!.isPaginationEnded!! &&
                    chatViewModel?.isLoading?.value == false
                ) {

                    callFetchBlockuserApi(blockListModel?.block_user_list?.size!!)
                }
            }
        })

    }

    private fun callFetchBlockuserApi(offset: Int) {
        Log.e("ChatFragment", " msg_id: ${ChatFragment.msg_id} offset: $offset")
        chatViewModel!!.fetchBlockuser(offset)
    }

    private fun init() {

        chatViewModel =
            ViewModelProvider(this, MyViewModelFactory(ChatViewModel(activity!!))).get(
                ChatViewModel::class.java
            )
    }

    private fun addObserver() {
        chatViewModel!!.isLoading!!.observe(activity!!, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })

        chatViewModel!!.responseError!!.observe(activity!!, Observer {
            homeController!!.errorBody(it)


        })

        chatViewModel!!.blockUserModel!!.observe(activity!!, Observer {

            if(it.code==200) {
                Toast.makeText(homeController, it.message, Toast.LENGTH_SHORT).show()
            }
            callFetchBlockuserApi(0)


        })
        chatViewModel!!.blockUserListModel!!.observe(activity!!, Observer {
            blockListModel = it

            blockUserHandleAdapter = BlockuserListAdapter(
                activity!!,
                it.block_user_list,
                this
            )
            rootView!!.rv_block_user.adapter = blockUserHandleAdapter
         /*   (rootView!!.rv_block_user.adapter as BlockuserListAdapter?)?.notifyDataSetChanged()
                ?: run {
                    try {
                        Log.v("----list", "-inside" +it.block_user_list.size)

                        blockUserHandleAdapter = BlockuserListAdapter(
                            activity!!,
                            it.block_user_list,
                            this
                        )
                        rootView!!.rv_block_user.adapter = blockUserHandleAdapter

                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Log.e("ChatListFragment", " Exception: ${e.message} ")
                    }
                }*/
            upDateUserListUi()


        })

    }

    override fun onResume() {
        super.onResume()
        homeController.txtTitle.text = getString(R.string.block_user)
        homeController.llToolbar.makeVisible()
        homeController.llBottomBar.makeGone()
        homeController.imgLogo.makeGone()
        homeController.imgBack.makeVisible()
        homeController.txtTitle.makeVisible()
        homeController.cardProfile.makeGone()
        homeController.cardSettings.makeGone()

    }

    override fun onListClick(position: Int, obj: Any?) {
        super.onListClick(position, obj)


        blockuser=(obj as BlockUserListModel.BlockUser)
        activity?.let { UTILS.commonDialog(it,this,it.getString(R.string.unblock_message)) }
    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)

        blockuser?.let { chatViewModel!!.blockUser(it.block_user_secret, 0) }
    }
    private fun upDateUserListUi() {
        Log.v("----list", "-" + blockListModel?.block_user_list?.size)
        if (blockListModel?.block_user_list?.size!! > 0) {
            rootView!!.rv_block_user.makeVisible()
            rootView!!.tvEmptyUserList.makeGone()
        } else {
            rootView!!.rv_block_user.makeGone()
            rootView!!.tvEmptyUserList.makeVisible()
        }

    }
}