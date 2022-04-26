package com.demo.utilities

open class Constants {

    companion object {

        //TODO: Pusher detail change here according BASE URL
         //  Development
        const val PUSHER_APP_ID = "365436"
        const val PUSHER_APP_KEY = "654653"
        const val PUSHER_APP_SECRET = "65465465"

        //common
        const val PUSHER_APP_CLUSTER = "444"
        const val PUSHER_APP_CHANNEL = "555555555"
        const val PUSHER_APP_EVENT_NAME = "messaging"
        const val PUSHER_ACTIVITY_TIMEOUT = 20 * 60 * 1000L; // 120,000 (2 minutes) default

        //TODO: Network callingide
        const val paginationLimit: Int = 20 //1
        const val connectionTimeOut: Long = 30000
        const val readTimeOut: Long = 30000

        //TODO: Login Details
        const val Localization: String = "localization"
        const val prefAuthorizationToken: String = "authorizationtoken"
        const val PREF_DEVICE_TOKEN: String = "PREF_DEVICE_TOKEN"
        const val pref: String = "mypreference"
        const val prefLoginPassword: String = "password"
        const val prefLoginUsername: String = "email"

        //only for fingerprint enable to save to next time login with this user
        const val prefIsRemember: String = "isRememeberME"
        const val prefCommonData: String = "commondata"
        const val prefUserData: String = "userdata"
        const val prefOnBoardingScreen: String = "prefOnBoardingScreen"
        const val deviceType: String = "android"
        const val APP_SUPPORT_EMAIL: String = "help@demo.com"
        const val PHONE_STATE_PERMISSION = 111
        const val GPSPremission = 401
        const val codeSettings = 101
        const val codeCameraRequest = 201
        const val codePickImageRequest = 301
        const val codePickPdfRequest = 401
        const val codePermissions = 1111
        const val CHANNEL_ID = "Notification_02"
        const val CHANNEL_ID_GENERAL = "Notification_03"

        const val CountryCode = "CountryCode"
        const val PhoneNumber = "PhoneNumber"
        const val FromLogin = "FromLogin"
        const val UserToken = "UserToken"
        const val MobileNoToken = "MobileNoToken"
        const val PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"


        const val DEVICE_COUNT_FOR_LOGIN_WITHOUT_OWN_USER: Int = 2

    }
}