package com.demo.view.interfaces

interface OnListClickListener {
    fun onListClick(position: Int, obj: Any?)
    fun onListClickSimple(position: Int, string: String?)
    fun onListShow(position: Int, obj: Any?)
}