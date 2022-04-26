package com.demo.view.adapter

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.demo.R
import com.demo.view.interfaces.OnListClickListener


/**
 */
class SlidingImageAdapter(private val context: Context, private val IMAGES: List<OnBoarding>,
onListClickListener: OnListClickListener) :
    PagerAdapter() {
    var onListClickListener = onListClickListener

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return IMAGES.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.fragment_on_boarding_one, view, false)!!
        val imageView = imageLayout.findViewById<View>(R.id.iv_imageview) as ImageView
        val title = imageLayout.findViewById<View>(R.id.tv_title) as TextView
        val subtitle = imageLayout.findViewById<View>(R.id.tv_description) as TextView
        val btnText = imageLayout.findViewById<View>(R.id.bt_board) as MaterialButton

        title.text = IMAGES[position].title
        subtitle.text = IMAGES[position].description
        btnText.text = IMAGES[position].button_text


        btnText.setOnClickListener {
            onListClickListener.onListClick(position,IMAGES[position])

        }
        Glide.with(context)
            .load(IMAGES[position].image)
            .centerCrop()
            .placeholder(viewImageProgress(context))
            .into(imageView)

        view.addView(imageLayout, 0)
        return imageLayout
    }

    fun viewImageProgress(context: Context): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
    override fun saveState(): Parcelable? {
        return null
    }

}