package com.demo.view.ui.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.demo.R
import com.demo.utilities.*
import com.demo.view.adapter.CountryListAdapterNew
import com.demo.view.interfaces.BiometricCallback
import com.demo.view.interfaces.OnListClickListener
import com.demo.view.interfaces.WebAuthCallback
import com.demo.view.ui.activities.LoginActivity
import kotlinx.android.synthetic.main.dialog_country_list.*
import kotlinx.android.synthetic.main.dialog_progress.*
import kotlinx.android.synthetic.main.dialog_web_auth.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


open class BaseActivity : AppCompatActivity(), WebAuthCallback {

    var mProgressDialog: Dialog? = null
    var userModel: UserModel? = null
    var myBiometricCallback: BiometricCallback? = null
    var myBioMetric: BiometricPrompt? = null
    private val TAG = BaseActivity::class.java.simpleName

    private val webAuthReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
        //   showWebAuthDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        registerWebAuthReceiver()
        init()
    }

    override fun onDestroy() {
       // removerWebAuthReceiver()
        super.onDestroy()
    }

    private fun registerWebAuthReceiver() {
        val filterSend = IntentFilter()
        filterSend.addAction(Constants.WEB_AUTH_ACTION)
        registerReceiver(webAuthReceiver, filterSend)
    }

    private fun removerWebAuthReceiver() {
        if (webAuthReceiver != null) {
            this@BaseActivity!!.unregisterReceiver(webAuthReceiver)
//            webAuthReceiver = null
        }
        Log.e(TAG, " Unregistering webAuthReceiver")
    }

    private fun init() {
        if (userModel == null) {
            userModel = Pref.getUserModel(this@BaseActivity)
        }
//        myWebAuthCallback = this
    }

    //TODO: Hide keyboard
    private fun hideSoftKeyboard() {
        if (window.decorView.rootView != null) {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                window.decorView.rootView.windowToken,
                0
            )
        }
    }

    //TODO: Show keyboard
    fun showSoftKeyboard() {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = window.decorView.rootView
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

    //TODO: Show Dialog with custom title and message
    open fun openDialog(
        title: String?,
        msg: String?,
        isVisiblePositive: Boolean,
        isVisibleNagative: Boolean
    ) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setMessage(msg)
            .setCancelable(false)
        if (isVisiblePositive) {
            builder.setPositiveButton(
                R.string.ok
            ) { dialog, id -> dialog.cancel() }
        }
        if (isVisibleNagative) {
            builder.setNegativeButton(
                R.string.no
            ) { dialog, id ->
                dialog.cancel()
            }
        }
        val alert = builder.create()
        alert.setTitle(title)
        alert.show()
    }

    //TODO: Show Biometric popup
    fun showBiometric(
        title: String = getString(R.string.biometric_authentication_msg),
        description: String = getString(R.string.biometric_authentication_desc_msg),
        biometricCallback: BiometricCallback
    ) {
        if (!BiometricUtils.checkBiometricPossible(this)) {
            Toast.makeText(
                this,
                getString(R.string.biometric_not_supported_msg),
                Toast.LENGTH_SHORT
            ).show()
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
        myBioMetric = BiometricPrompt(this, ContextCompat.getMainExecutor(this)!!,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Authentication error: $errString")

                    myBiometricCallback?.onError(errorCode, errString)
                }

                //
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    /**success*/
                    /**success*/
                    /**success*/
                    /**success*/
                    myBiometricCallback?.onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
/* Toast.makeText(
activity!!, getString(R.string.auth_failed),
Toast.LENGTH_SHORT
)
.show()*/
                    myBiometricCallback?.onFail()

                }
            })
    }

    //TODO: Show Setting Dialog
    fun showSettingsDialog(msg: String) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this).create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.setTitle("Need Permissions")
        alertDialog.setButton(
            androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE,
            "GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettingsOfApp()
        }
        alertDialog.setButton(
            androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE,
            "Cancel"
        ) { dialog, which ->
            dialog.cancel()
            this@BaseActivity.finishAffinity()
        }
        alertDialog.show()
    }

    fun openSettingsOfApp() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this@BaseActivity.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    fun loadFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    fun loadFragmentWithClearedStack(fragment: Fragment, tag: String, fragName: String?) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    fun clearFragment(fragName: String) {
        supportFragmentManager.popBackStack(fragName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun loadFragment(fragment: Fragment, tag: String, backstack: String) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(backstack).commit()
    }

    fun addLoadFragment(fragment: Fragment, tag: String, backstack: String) {
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment, tag)
            .addToBackStack(backstack).commit()
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }

    fun NestedScrollView.myRequestFocus(view: View) {
        Log.e(
            "zxczxc",
            "NestedScrollView.myRequestFocus to ${resources.getResourceEntryName(view.id)}"
        )
        view.requestFocus()
        // this.smoothScrollTo(0, view.bottom)
    }

    fun ScrollView.myRequestFocus(view: View) {
        Log.e(
            "zxczxc",
            "ScrollView.myRequestFocus to ${resources.getResourceEntryName(view.id)}"
        )

        view.requestFocus()
        //     this.smoothScrollTo(0, view.bottom)
    }

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = Dialog(this@BaseActivity)
        }
        try {
            mProgressDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mProgressDialog!!.show()
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.setContentView(R.layout.dialog_progress)
            mProgressDialog!!.progressbar.makeVisible()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            try {
                mProgressDialog!!.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun errorBody(responseErrorBody: ResponseBody) {
        try {
            val res = responseErrorBody.string()
            val jsonObject = JSONObject(res)
            if (jsonObject.optInt("code") == 302 || jsonObject.optInt("code") == 301 || jsonObject.optInt(
                    "code"
                ) == 300 || jsonObject.optInt("code") == 401
            ) {
                val builder = AlertDialog.Builder(this@BaseActivity, R.style.MyDialogTheme)
                builder.setMessage(jsonObject.getString("message"))
                builder.setCancelable(false)
                builder.setPositiveButton(getString(R.string.ok),
                    DialogInterface.OnClickListener { dialog, which ->
                        val gson = Gson()
                        var is_remember =
                            Pref.getValue(this@BaseActivity, Constants.prefIsRemember, false)
                        var email =
                            Pref.getValue(this@BaseActivity, Constants.prefLoginUsername, "")
                        var password =
                            Pref.getValue(this@BaseActivity, Constants.prefLoginPassword, "")

                        Pref.deleteAll(this@BaseActivity)
                        Pref.setLocale(this@BaseActivity, "en")
                        if (is_remember) {
                            Pref.setValue(this@BaseActivity, Constants.prefIsRemember, is_remember)
                            Pref.setValue(this@BaseActivity, Constants.prefLoginUsername, email!!)
                            Pref.setValue(
                                this@BaseActivity,
                                Constants.prefLoginPassword,
                                password!!
                            )
                        }

                        startActivity(
                            Intent(
                                this@BaseActivity,
                                LoginActivity::class.java
                            )
                        ) //LoginActivity
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            finishAffinity()
                        } else {
                            finish()
                        }
                    })
                builder.create().apply {
                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                ContextCompat.getColor(
                                    this@BaseActivity,
                                    R.color.black
                                )
                            )
                    }
                }.show()
            } else if (jsonObject.optInt("code") == 419) {
                Toast.makeText(
                    this@BaseActivity,
                    jsonObject.getString("message"),
                    Toast.LENGTH_SHORT
                ).show()
                onBackPressed()
            } else {
                Toast.makeText(
                    this@BaseActivity,
                    jsonObject.getString("message"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IOException) {
            Toast.makeText(
                this@BaseActivity,
                getString(R.string.something_wrong_message),
                Toast.LENGTH_SHORT
            ).show()
            Log.e("zxczxc", "exception errorBody $e")
            e.printStackTrace()
        } catch (e: JSONException) {
            Toast.makeText(
                this@BaseActivity,
                getString(R.string.something_wrong_message),
                Toast.LENGTH_SHORT
            ).show()
            Log.e("zxczxc", "exception errorBody $e")
            e.printStackTrace()
        }

    }

    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(
                clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndexOfLink,
                startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    /** Set Up CountryCode For Profile */
    fun setupCountryCode(
        placeHolderOnSelection: TextView,
        countryList: ArrayList<RegisterDataModel.CountryCode?>?,
        onListClickListener: OnListClickListener
    ) {
        var arrayCountryList = countryList

        var dialogCountryList = Dialog(this@BaseActivity, R.style.DialogStyle)
        dialogCountryList.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCountryList.setContentView(R.layout.dialog_country_list)
        val lp = WindowManager.LayoutParams()
        val window: Window = dialogCountryList.window!!
        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        lp.copyFrom(window.attributes)
        dialogCountryList.window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        dialogCountryList.window!!.setGravity(Gravity.CENTER)
        dialogCountryList.window!!.setBackgroundDrawable(ColorDrawable(0))
        dialogCountryList.btn_cancel_country_code.setOnMyClickListener {
            dialogCountryList.dismiss()
        }

        dialogCountryList.edt_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isEmpty()) {
                    dialogCountryList.iv_close.visibility = View.GONE
                } else {
                    dialogCountryList.iv_close.visibility = View.VISIBLE
                }
                (dialogCountryList.rv_countries.adapter as CountryListAdapterNew).let {
                    it.filter(arrayCountryList!!.filter { model ->
                        model!!.name!!.trim().toLowerCase(Locale.getDefault())
                            .contains(s!!.toString().trim().toLowerCase(Locale.getDefault()))
                    } as ArrayList<RegisterDataModel.CountryCode?>)
                    if (it.arrayCountryList!!.isEmpty()) {
                        dialogCountryList.rv_countries.visibility = View.GONE
                    } else {
                        dialogCountryList.rv_countries.visibility = View.VISIBLE
                    }
                }
            }
        })

        dialogCountryList.iv_close.setOnMyClickListener { dialogCountryList.edt_search.text.clear() }
        dialogCountryList.rv_countries.layoutManager =
            LinearLayoutManager(this@BaseActivity)

        dialogCountryList.rv_countries.adapter =
            CountryListAdapterNew(context = this@BaseActivity,
                arrayCountryList = arrayCountryList,
                onListClickListener = object : OnListClickListener {
                    override fun onListClick(position: Int, obj: Any?) {
                        placeHolderOnSelection.text =
                            "+" + (obj as RegisterDataModel.CountryCode).phonecode
                        placeHolderOnSelection.tag = (obj).id

                        onListClickListener.onListClickSimple(position, "")
                        dialogCountryList.dismiss()
                    }

                    override fun onListClickSimple(position: Int, string: String?) {
                    }

                    override fun onListShow(position: Int, obj: Any?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
        hideSoftKeyboard()

        dialogCountryList.show()
    }

    fun showToast(string: String) {
        Toast.makeText(this, "" + string, Toast.LENGTH_SHORT).show()
    }



    override fun onWebAuthGranted() {
//        showToast("onWebAuthGranted!!!")
    }

    override fun onWebAuthDismiss() {
        showToast("onWebAuthDismiss!")
    }


}