package com.demo.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo.R
import com.demo.utilities.setOnMyClickListener
import com.demo.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_country_list.view.*

class CountryListAdapterNew(
    var context: Context,
    var arrayCountryList: ArrayList<RegisterDataModel.CountryCode?>?,
    var onListClickListener: OnListClickListener
) :
    RecyclerView.Adapter<CountryListAdapterNew.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(R.layout.row_country_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrayCountryList!!.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.itemView.tv_country_code.text = "+" + arrayCountryList!![position]!!.phonecode
        holder.itemView.tv_country_name.text = arrayCountryList!![position]!!.name
        if (arrayCountryList!![position]!!.isSelected) {
            holder.itemView.tv_country_code.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                )
            )
            holder.itemView.tv_country_name.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                )
            )

        }else{
            holder.itemView.tv_country_code.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimary
                )
            )
            holder.itemView.tv_country_name.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimary
                )
            )

        }
        holder.itemView.setOnMyClickListener {
            onListClickListener.onListClick(position, arrayCountryList!![position])
        }
    }

    fun filter(arrayFilteredCountryList: ArrayList<RegisterDataModel.CountryCode?>?) {
        arrayCountryList = arrayFilteredCountryList
        notifyDataSetChanged()
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

}