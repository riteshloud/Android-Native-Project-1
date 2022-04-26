package com.demo.view.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.demo.R
import com.demo.utilities.Constants
import com.demo.utilities.Pref
import com.demo.utilities.start
import com.demo.view.adapter.SlidingImageAdapter
import com.demo.view.interfaces.OnListClickListener
import com.demo.view.service.MyViewModelFactory
import com.demo.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_onboarding_intro.*


class OnBoardingIntroActivity : BaseActivity() {

    private var onBoardingViewModel: OnBoardingViewModel? = null
    var adapter: SlidingImageAdapter? = null
    var onBoardingArray: ArrayList<OnBoarding>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_intro)
        init()
        addObserver()
        /*   val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
           viewPagerAdapter.addFragment(OnBoardingOneFragment())
           viewPagerAdapter.addFragment(OnBoardingTwoFragment())
           viewPagerAdapter.addFragment(OnBoardingThreeFragment())
           viewPagerAdapter.addFragment(OnBoardingFourFragment())
           viewPagerOnBoarding.adapter = viewPagerAdapter
           viewPagerOnBoarding.setPageTransformer(true, ZoomOutPageTransformer())*/
        onBoardingViewModel!!.callForOnBoarding()
        val tabLayout: TabLayout = findViewById(R.id.tabLayoutIndicator)
        tabLayout.setupWithViewPager(viewPagerOnBoarding)

        tv_skip.setOnMyClickListener {
            Pref.setValue(this, Constants.prefOnBoardingScreen, true)

            start<LoginActivity>()
            finish()
        }


    }

    private fun init() {
        onBoardingViewModel =
            ViewModelProvider(
                this,
                MyViewModelFactory(OnBoardingViewModel(this@OnBoardingIntroActivity))
            ).get(OnBoardingViewModel::class.java)
    }

    private fun addObserver() {

        onBoardingViewModel?.isLoading?.observe(this, Observer {
            it?.let {
                if (it) {
                    showProgressDialog()
                } else {
                    dismissProgressDialog()
                }
            }
        })

        onBoardingViewModel?.responseError?.observe(this, Observer {
            it?.let {
                errorBody(it)
            }
        })

        onBoardingViewModel?.onBoardingResponse?.observe(this, Observer {
            onBoardingArray = it.onBoarding

            for (i in 0 until onBoardingArray!!.size) {
                when (onBoardingArray!![i].slug) {

                    "fund_deposit" -> onBoardingArray!![i].image = R.mipmap.ic_onboarding1

                    "join_our_coummunity" -> onBoardingArray!![i].image = R.mipmap.ic_onboarding2

                    "explore_webiste_services" -> onBoardingArray!![i].image = R.mipmap.ic_onboarding3

                    "learn_on_blog" -> onBoardingArray!![i].image = R.mipmap.ic_onboarding4

                }
            }

            setAdapter()
        })


    }


    private fun setAdapter() {
        adapter = SlidingImageAdapter(
            this,
            onBoardingArray!!,
            onListClickListener = object : OnListClickListener {
                override fun onListClick(position: Int, obj: Any?) {

                    try {
                        val myIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(onBoardingArray!!.get(position).button_url)
                        )
                        startActivity(myIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            this@OnBoardingIntroActivity, "No application can handle this request."
                                    + " Please install a webbrowser", Toast.LENGTH_LONG
                        ).show()
                        e.printStackTrace()
                    }
                }

                override fun onListClickSimple(position: Int, string: String?) {
                }

                override fun onListShow(position: Int, obj: Any?) {
                }

            })
        viewPagerOnBoarding.adapter = adapter
        viewPagerOnBoarding.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                // Check if this is the page you want.

            }
        })

    }

    internal class ViewPagerAdapter(supportFragmentManager: FragmentManager?) :
        FragmentPagerAdapter(supportFragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mList: MutableList<Fragment> = ArrayList()
        override fun getItem(i: Int): Fragment {
            return mList[i]
        }

        override fun getCount(): Int {
            return mList.size
        }

        fun addFragment(fragment: Fragment) {
            mList.add(fragment)
        }
    }

}