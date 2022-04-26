package com.demo.view.ui.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.demo.R
import com.demo.utilities.BiometricUtils
import com.demo.utilities.Pref
import com.demo.view.interfaces.BiometricCallback
import com.demo.view.interfaces.OnListClickListener
import com.demo.view.ui.activities.HomeActivity
import kotlinx.android.synthetic.main.dialog_image_view.*

abstract class BaseFragment : OnListClickListener, Fragment() {

    private var TAG = this.javaClass.simpleName
    private var lifecycleCheck = "zxczxc"
    lateinit var homeController: HomeActivity
    var userModel: UserModel? = null

    var myBiometricCallback: BiometricCallback? = null
    var myBioMetric: BiometricPrompt? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.e(lifecycleCheck, "lifecycle check - $TAG onAttach")
        homeController = context as HomeActivity
        userModel = Pref.getUserModel(context)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e(lifecycleCheck, "lifecycle check - $TAG onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e(lifecycleCheck, "lifecycle check - $TAG onActivityCreated")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(lifecycleCheck, "lifecycle check - $TAG onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onStart")
    }

    override fun onResume() {
        super.onResume()

        // setFullAppBackGround() //for comman background after dashboard in fragment
        Log.e(lifecycleCheck, "lifecycle check - $TAG onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onStop")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onDestroyView")
        try {
            //  homeController!!.lastDestroyedFragment = this.javaClass.simpleName
            hideSoftKeyboard()
        } catch (e: Exception) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onDestroy")

    }

    fun ScrollView.myRequestFocus(view: View) {
        view.requestFocus()
        //  this.smoothScrollTo(0, view.bottom)
    }

    private fun hideSoftKeyboard() {
        if (activity!!.window.decorView.rootView != null) {
            val inputMethodManager =
                activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager!!.hideSoftInputFromWindow(
                activity!!.window.decorView.rootView.windowToken,
                0
            )
        } else {

        }
    }

    fun showSoftKeyboard() {
        val inputMethodManager = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager != null) {
            val currentFocusView = activity!!.window.decorView.rootView
            if (currentFocusView != null) {
                val iBinderToken = currentFocusView.windowToken
                if (iBinderToken != null) {
                    inputMethodManager.toggleSoftInputFromWindow(
                        iBinderToken,
                        InputMethodManager.SHOW_FORCED, 0
                    )
                }
            }
        }
    }

    fun String.removeComma(): String {
        return this.replace(",", "")
    }

    fun showBiometric(
        title: String = getString(R.string.biometric_authentication_msg)
        , description: String = getString(R.string.biometric_authentication_desc_msg)
        , biometricCallback: BiometricCallback
    ) {
        if (!BiometricUtils.checkBiometricPossible(activity!!)) {
            Toast.makeText(activity!!, getString(R.string.biometric_not_supported_msg), Toast.LENGTH_SHORT).show()
            return
        }

        if (myBioMetric == null) {
            generateBasicBioMetric()
        }
        myBiometricCallback = biometricCallback
        var promptInfo: BiometricPrompt.PromptInfo
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.unlock_with_fingerprint_tag))
            .setSubtitle(getString(R.string.unlock_fingerprint_text))
            .setNegativeButtonText(getString(R.string.cancel_tag))
            .build()
        myBioMetric!!.authenticate(promptInfo)
    }

    private fun generateBasicBioMetric() {
        myBioMetric = BiometricPrompt(this@BaseFragment, ContextCompat.getMainExecutor(activity)!!,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    myBiometricCallback?.onError(errorCode, errString)
                }

                //
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    myBiometricCallback?.onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    myBiometricCallback?.onFail()
                }
            })
    }

    /**override methods from implemented interfaces are implemented below*/
    override fun onListClick(position: Int, obj: Any?) {}

    override fun onListClickSimple(position: Int, string: String?) {}

    override fun onListShow(position: Int, obj: Any?) {}

    fun showToast(mMessage: String) {
//        Toast.makeText(activity!!, "" + mMessage, Toast.LENGTH_SHORT).show()
        homeController.showToast(mMessage)
    }

    fun showImagePdfDialog(path: String) {
        if (path.endsWith("pdf")) {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(path)
                )
            activity!!.startActivity(browserIntent)
            return
        }

        var imgPreviewDialog = Dialog(activity!!)
        imgPreviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        imgPreviewDialog!!.setContentView(R.layout.dialog_image_view)
        imgPreviewDialog!!.getWindow()!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imgPreviewDialog!!.progressBar.visibility = View.VISIBLE

        imgPreviewDialog!!.img.setOnClickListener {
            imgPreviewDialog!!.dismiss()
        }
        imgPreviewDialog!!.img_close.setOnClickListener {
            imgPreviewDialog!!.dismiss()
        }

        Glide.with(activity!!)
            .load(path)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?, model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    imgPreviewDialog.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    imgPreviewDialog.progressBar.visibility = View.GONE
                    return false
                }
            }).into(imgPreviewDialog.img)

        //imgPreviewDialog.img.setOnClickListener { imgPreviewDialog.dismiss() }
        imgPreviewDialog.show()
    }
}
