package com.demo.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import kotlin.collections.ArrayList
import android.widget.TextView
import com.demo.R
import kotlinx.android.synthetic.main.row_default.view.*


class HighLightArrayAdapter : ArrayAdapter<String> {

    private var mSelectedIndex = -1

    var mContext: Context
    lateinit var objects: ArrayList<String>
    var selectionColor: Int = R.color.white
    var selectedTextColor: Int? = null
    var unSelectedTextColor: Int? = null
    var resourceLayout: Int? = null
    fun setSelection(position: Int) {
        mSelectedIndex = position
        notifyDataSetChanged()
    }

    constructor(context: Context, resource: Int, id: Int, objects: ArrayList<String>) : super(
        context,
        resource,
        objects
    ) {
        mContext = context
        resourceLayout = resource

    }


    constructor(context: Context, resource: Int, objects: ArrayList<String>) : super(
        context,
        resource,
        objects
    ) {
        mContext = context
        this.objects = objects
        resourceLayout = resource

    }


    constructor(
        context: Context,
        resource: Int,
        objects: ArrayList<String>,
        selectionColor: Int
    ) : super(context, resource, objects) {
        mContext = context
        this.objects = objects
        this.selectionColor = selectionColor
        resourceLayout = resource

    }

    constructor(
        context: Context,
        resource: Int,
        objects: ArrayList<String>,
        selectionColor: Int,
        selectedTextColor: Int,
        unSelectedTextColor: Int
    ) : super(context, resource, objects) {
        mContext = context
        this.objects = objects
        this.selectionColor = selectionColor
        this.selectedTextColor = selectedTextColor
        this.unSelectedTextColor = unSelectedTextColor
        resourceLayout = resource

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = View.inflate(context, R.layout.row_default, null)
        val textView = view.findViewById(R.id.txt) as TextView
        textView.text = objects[position]
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = LayoutInflater.from(context).inflate(resourceLayout!!, null)
        if (position == mSelectedIndex) {
            itemView.setBackgroundColor(ContextCompat.getColor(mContext, selectionColor))
            //itemView.setBackgroundResource(R.drawable.two_color_bg)
            if (selectedTextColor != null) {
                itemView.txt.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        selectedTextColor!!
                    )
                )
            }
        } else {
            itemView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white))
            if (unSelectedTextColor != null) {
                itemView.txt.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        unSelectedTextColor!!
                    )
                )
            }
        }
        itemView.txt.text = objects[position]

        return itemView
    }

     fun updateList(empList: ArrayList<String>) {
        objects = empList
        notifyDataSetChanged()
    }
}