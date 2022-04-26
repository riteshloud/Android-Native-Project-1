package com.demo.view.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.UTILS
import com.demo.utilities.setOnMyClickListener
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_multi_device_login.view.*

class MultiDeviceListAdapter(
    var context: Context,
    var arrayDeviceList: ArrayList<UserDevices?>?,
    var onListClickListener: OnListClickListener
) : RecyclerView.Adapter<MultiDeviceListAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(R.layout.row_multi_device_login, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayDeviceList!!.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.itemView.tvDeviceName.text = "" + arrayDeviceList!![position]!!.deviceName
        var agoTime = ""+UTILS.getTimeAgo(context, arrayDeviceList!![position]!!.timestamp!!)
        if (TextUtils.isEmpty(agoTime)){
            agoTime = "1 second ago"
        }
        holder.itemView.tvDeviceTime.text = "Last use $agoTime"
        holder.itemView.cbDevice.isChecked = arrayDeviceList!![position]!!.isSelected

        holder.itemView.setOnMyClickListener {
            onListClickListener.onListClick(position, arrayDeviceList!![position])
        }
    }

    /*fun filter(arrayFilteredCountryList: ArrayList<RegisterDataModel.CountryCode?>?) {
        arrayCountryList = arrayFilteredCountryList
        notifyDataSetChanged()
    }*/

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

}