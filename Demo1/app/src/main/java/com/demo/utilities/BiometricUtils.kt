package com.demo.utilities

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager

class BiometricUtils {
    companion object {
        fun isHardwareAvailable(context: Context): Boolean {
            val bm = BiometricManager.from(context)
            val canAuthenticate = bm.canAuthenticate()
            return !(canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE || canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
        }

        fun hasBiometricEnrolled(context: Context): Boolean {
            val bm = BiometricManager.from(context)
            val canAuthenticate = bm.canAuthenticate()
            return (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
        }

        fun checkBiometricPossible(context: Context): Boolean {
            return (isHardwareAvailable(context) && hasBiometricEnrolled(context))
        }
    }
}