package com.demo.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide


inline fun <reified T : Activity> Context.start(vararg params: Pair<String, Any>) {
    val intent = Intent(this, T::class.java).apply {
        params.forEach {
            when (val value = it.second) {
                is Int -> putExtra(it.first, value)
                is String -> putExtra(it.first, value)
                is Double -> putExtra(it.first, value)
                is Float -> putExtra(it.first, value)
                is Boolean -> putExtra(it.first, value)
                is Bundle -> putExtra(it.first, value)
                else -> throw IllegalArgumentException("Wrong param type!")
            }
            return@forEach
        }
    }
    startActivity(intent)
}

class OnDebouncedClickListener(private val delayInMilliSeconds: Long, val action: () -> Unit) :
    View.OnClickListener {
    var enable = true
    override fun onClick(view: View?) {
        if (enable) {
            enable = false
            view?.postDelayed({ enable = true }, delayInMilliSeconds)
            action()
        }
    }
}

fun View.setOnMyClickListener(delayInMilliSeconds: Long = 500, action: () -> Unit) {
    val onDebouncedClickListener = OnDebouncedClickListener(delayInMilliSeconds, action)
    setOnClickListener(onDebouncedClickListener)
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

//Todo For Any View Visible and Gone
fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.isInvisible(): Boolean = visibility == View.INVISIBLE

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

//Todo For Load Any Image
fun ImageView.loadFromUrl(url: String) {
    Glide.with(context).load(url).into(this)
}

//Todo For UpperCase First Latter
fun String.upperCaseFirstLetter(): String {
    return this.substring(0, 1).toUpperCase().plus(this.substring(1))
}

fun isPasswordValid(text: Editable?): Boolean {
    return text != null && text.trim().length >= 6
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, observer: Observer<T>) {
    removeObserver(observer)
    observe(owner, observer)
}

fun <T, U> combine(first: Array<T>, second: Array<U>): Array<Any> {
    val list: MutableList<Any> = first.map { i -> i as Any }.toMutableList()
    list.addAll(second.map { i -> i as Any })
    return list.toTypedArray()
}

