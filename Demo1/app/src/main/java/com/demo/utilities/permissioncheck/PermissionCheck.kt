package com.demo.utilities.permissioncheck

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

object PermissionCheck {

    internal lateinit var allPermission: List<String>
    internal var permissionNeed: ArrayList<String> = ArrayList()
    internal var isSkipDenyAndForceDeny: Boolean = false
    internal var acceptedCallback: WeakReference<PermissionStatus>? = null
    internal var deniedCallback: WeakReference<PermissionStatus>? = null
    internal var foreverDeniedCallback: WeakReference<PermissionStatus>? = null
    internal const val REQ_PERMISSION_CODE = 1

    @JvmStatic
    fun with(activity: Activity?): PermissionExecution {
        return PermissionExecution(activity)
    }

    class PermissionExecution(private val mContext: Activity?) {

        fun setPermissions(vararg _allPermission: String) = this@PermissionExecution.apply {
            allPermission = _allPermission.toList()
        }

        fun skipDenyAndForceDeny() = this@PermissionExecution.apply {
            isSkipDenyAndForceDeny = true
        }

        fun onAccepted(callback: (list: MutableList<String>?) -> Unit) =
            this@PermissionExecution.apply {
                acceptedCallback = WeakReference(object : PermissionStatus {
                    override fun onResult(list: MutableList<String>?) {
                        callback(list)
                    }
                })
            }

        fun onAccepted(callback: PermissionStatus) = this@PermissionExecution.apply {
            acceptedCallback = WeakReference(callback)
        }

        fun onDenied(callback: (list: MutableList<String>?) -> Unit) =
            this@PermissionExecution.apply {
                deniedCallback = WeakReference(object : PermissionStatus {
                    override fun onResult(list: MutableList<String>?) {
                        callback(list)
                    }
                })
            }

        fun onDenied(callback: PermissionStatus) = this@PermissionExecution.apply {
            deniedCallback = WeakReference(callback)
        }

        fun onForeverDenied(callback: (list: MutableList<String>?) -> Unit) =
            this@PermissionExecution.apply {
                foreverDeniedCallback = WeakReference(object : PermissionStatus {
                    override fun onResult(list: MutableList<String>?) {
                        callback(list)
                    }
                })
            }

        fun onForeverDenied(callback: PermissionStatus) =
            this@PermissionExecution.apply {
                foreverDeniedCallback = WeakReference(callback)
            }

        fun ask() {
            mContext?.apply {

                if (isFinishing) {
                    return
                }

                if (alreadyGranted(this as Context)) {
                    acceptedCallback?.get()?.onResult(allPermission as ArrayList<String>)
                    return
                }

                mContext.startActivity(Intent(this, PermissionCheckActivity::class.java))
                mContext.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        private fun alreadyGranted(context: Context): Boolean {
            permissionNeed.clear()
            allPermission.filterTo(permissionNeed) {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            return permissionNeed.isEmpty()
        }
    }
}