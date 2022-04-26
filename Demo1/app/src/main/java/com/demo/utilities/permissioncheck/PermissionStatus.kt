package com.demo.utilities.permissioncheck

interface PermissionStatus {
    fun onResult(list: MutableList<String>? = null)
}