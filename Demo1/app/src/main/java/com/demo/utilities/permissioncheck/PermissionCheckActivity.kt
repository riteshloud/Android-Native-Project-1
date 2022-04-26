package com.demo.utilities.permissioncheck

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.demo.R
import com.demo.utilities.permissioncheck.PermissionCheck.REQ_PERMISSION_CODE
import com.demo.utilities.permissioncheck.PermissionCheck.acceptedCallback
import com.demo.utilities.permissioncheck.PermissionCheck.deniedCallback
import com.demo.utilities.permissioncheck.PermissionCheck.foreverDeniedCallback
import com.demo.utilities.permissioncheck.PermissionCheck.isSkipDenyAndForceDeny
import com.demo.utilities.permissioncheck.PermissionCheck.permissionNeed

class PermissionCheckActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.check_premission)
        ActivityCompat.requestPermissions(
            this@PermissionCheckActivity,
            permissionNeed.toTypedArray(),
            REQ_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION_CODE -> {

                /**
                 * First list = is a list of all permission which is granted by user
                 * Second list = is a list of all permission which is denied by user
                 * Third list = is a list of all permission which is force denied (Don't ask again) by uesr
                 * */
                val acceptedPermissions = mutableListOf<String>()
                val askAgainPermissions = mutableListOf<String>()
                val refusedPermissions = mutableListOf<String>()

                for (i in permissions.indices) {
                    val permissionName = permissions[i]
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        acceptedPermissions.add(permissionName)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            shouldShowRequestPermissionRationale(permissionName)
                        ) {
                            askAgainPermissions.add(permissionName)
                        } else {
                            refusedPermissions.add(permissionName)
                        }
                    }
                }
                when {
                    refusedPermissions.isNotEmpty() && !isSkipDenyAndForceDeny -> foreverDeniedCallback?.get()
                        ?.onResult(refusedPermissions)
                    askAgainPermissions.isNotEmpty() && !isSkipDenyAndForceDeny -> deniedCallback?.get()
                        ?.onResult(askAgainPermissions)
                    acceptedPermissions.isNotEmpty() || isSkipDenyAndForceDeny -> acceptedCallback?.get()
                        ?.onResult(acceptedPermissions)
                }
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    override fun onDestroy() {
        isSkipDenyAndForceDeny = false
        acceptedCallback?.clear()
        deniedCallback?.clear()
        acceptedCallback?.clear()
        super.onDestroy()
    }

}