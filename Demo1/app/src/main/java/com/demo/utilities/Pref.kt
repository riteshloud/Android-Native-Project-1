package com.demo.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.util.Log
import com.google.gson.Gson
import java.util.*

open class Pref {

    companion object {

        private val TAG: String? = "Pref"
        private var sharedPreferences: SharedPreferences? = null

        fun openPref(context: Context) {
            sharedPreferences = context.getSharedPreferences(Constants.pref, Context.MODE_PRIVATE)
        }

        fun getValue(context: Context, key: String, defaultValue: String?): String? {
            try {
                openPref(context)
                val result = sharedPreferences!!.getString(key, defaultValue)
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return defaultValue
        }

        fun setValue(context: Context, key: String, value: Int) {
            openPref(context)
            var prefsPrivateEditor: Editor? = sharedPreferences!!.edit()
            prefsPrivateEditor!!.putInt(key, value)
            prefsPrivateEditor.commit()
        }

        fun setValue(context: Context, key: String, value: Long) {
            openPref(context)
            var prefsPrivateEditor: Editor? = sharedPreferences!!.edit()
            prefsPrivateEditor!!.putLong(key, value)
            prefsPrivateEditor.commit()
            sharedPreferences = null
        }

        fun getValue(context: Context, key: String, defaultValue: Int): Int {
            openPref(context)
            val result = sharedPreferences!!.getInt(key, defaultValue)
            sharedPreferences = null
            return result
        }

        fun getValue(context: Context, key: String, defaultValue: Long): Long {
            openPref(context)
            val result = sharedPreferences!!.getLong(key, defaultValue)
            sharedPreferences = null
            return result
        }

        fun setValue(context: Context, key: String, value: String) {
            openPref(context)
            var prefsPrivateEditor: Editor? = sharedPreferences!!.edit()
            prefsPrivateEditor!!.putString(key, value)
            prefsPrivateEditor.commit()
            sharedPreferences = null
        }

        fun getValue(context: Context, key: String, defaultValue: Boolean): Boolean {
            openPref(context)
            val result = sharedPreferences!!.getBoolean(key, defaultValue)
            sharedPreferences = null
            return result
        }

        fun setValue(context: Context, key: String, value: Boolean) {
            openPref(context)
            val prefsPrivateEditor = sharedPreferences!!.edit()
            prefsPrivateEditor.putBoolean(key, value)
            prefsPrivateEditor.commit()
            sharedPreferences = null
        }

        //TODO: Delete All Preference Data
        fun deleteAll(context: Context) {
            openPref(context)
            val gson = Gson()

            var isRemember = getValue(context, Constants.prefIsRemember, false)
            var email = getValue(context, Constants.prefLoginUsername, "")
            var password = getValue(context, Constants.prefLoginPassword, "")

//            var isFingerPrintSetInThisDevice = getUserModel(context)!!.payload!!.user!!.fingerPrintSetInThisDevice
//            var emailFinger = getValue(context, Constants.prefFingerUsername, "")
//            var passwordFinger =
//                getValue(context, Constants.prefFingerPassword, "")
//            var uuidFinger = if(getUserModel(context)!!.payload!!.user!!.fingerPrintSetInThisDevice) getUserModel(context)!!.payload!!.user!!.fingerUUID.toString().trim() else ""
//            Log.e("TestClerea","***   " + uuidFinger)
//            sharedPreferences!!.edit().clear().commit()
            if (isRemember) {
                setValue(context, Constants.prefIsRemember, isRemember)
                setValue(context, Constants.prefLoginUsername, email!!)
                setValue(context, Constants.prefLoginPassword, password!!)
            }
//            setValue(context, Constants.prefFingerUsername, emailFinger!!)
//            setValue(context, Constants.prefFingerPassword, passwordFinger!!)
//            setValue(context, Constants.prefFingerUUID, uuidFinger!!)
//            setValue(
//                context,
//                Constants.prefFingerPrintSetInThisDevice,
//                isFingerPrintSetInThisDevice
//            )

            setValue(context, Constants.prefUserData, "")
            setValue(context, Constants.prefOnBoardingScreen, true)
        }

        //TODO: Get All Common Data
//        fun getCommonDataModel(context: Context): CommonDataModel? {
//            val gson = Gson()
//            getValue(context, Constants.prefCommonData, null).let {
//                return gson.fromJson(it, CommonDataModel::class.java)
//            }
//            return null
//        }

        //TODO: Get User Data
        fun getUserModel(context: Context): UserModel? {
            val gson = Gson()
            getValue(context, Constants.prefUserData, null).let {
                return gson.fromJson(it, UserModel::class.java)
            }
            return null
        }

        //TODO: Get Authorization Token
        fun getPrefAuthorizationToken(context: Context): String {
            return getValue(context, Constants.prefAuthorizationToken, "").toString().apply {
                Log.d(TAG, "token - $this")
            }
        }

        //TODO: Get Localization
        fun getLocalization(context: Context): String {
            return getValue(context, Constants.Localization, "en").toString()
        }

        //TODO: Set Localization
        fun setLocale(mContext: Context, lang: String) {
            var lang = lang
            Log.e("zxczxc", " setting locale $lang")
            //header : X-localization:en // for english language, chi // for chinese language, my// for malay language if not pass set the default to english
            val config = Configuration()
            setValue(mContext, Constants.Localization, lang)

            if (lang.equals("my", ignoreCase = true)) {
                lang = "ms"
            }
            if (lang.equals("cn", ignoreCase = true)) {
                lang = "zh"
            }
            if (lang.equals("ko", ignoreCase = true)) {
                lang = "ko"
            }
            if (lang.equals("vi", ignoreCase = true)) {
                lang = "vi"
            }
            if (lang.equals("th", ignoreCase = true)) {
                lang = "th"
            }
            val locale = Locale(lang)
            Locale.setDefault(locale)
            config.setLocale(locale)

            mContext.resources.updateConfiguration(
                config,
                mContext.resources.displayMetrics
            )
        }

    }
}
