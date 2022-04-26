package com.demo.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.TypedValue
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.demo.R
import com.demo.view.interfaces.OnListClickListener
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLConnection
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class UTILS {
    companion object {

        private val SECOND_MILLIS = 1000
        private val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private val DAY_MILLIS = 24 * HOUR_MILLIS
        private val WEEK_MILLIS = 7 * DAY_MILLIS
        public val Date_Pattern_yyyyMMddTHHmmssSSSSSSZ = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
        public val Time_Pattern_MMMddhhmma = "MMM dd hh:mm a"


        fun isAccessFineLocationGranted(context: Context): Boolean {
            return ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        }

        fun isLocationEnabled(context: Context): Boolean {
            val locationManager: LocationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        fun dpToPixel(context: Context, dp: Int): Int {
            val r = context.resources
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            ).toInt()
        }

        fun parseDouble(str: String): String {
            val nf = NumberFormat.getNumberInstance(Locale.US)
            val df = nf as DecimalFormat
            //df.applyPattern("##,##,##,##,##,##,##0.00")
            df.applyPattern("#0.00")
            val wallet = java.lang.Double.parseDouble(str)
            return df.format(wallet)

        }

        fun String.removeComma(): String {
            return this.replace(",", "")
        }

        fun convertUtcToLocal(dateStr: String): String {
            val df = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val date = df.parse(dateStr)
            df.timeZone = TimeZone.getDefault()
            return df.format(date)
        }

        fun convertDate(fromPattern: String, toPattern: String, dateString: String): String {
            return try {
                val date = SimpleDateFormat(fromPattern).parse(dateString)
                SimpleDateFormat(toPattern).format(date)
            } catch (e: Exception) {
                ""
            }

        }

        fun generateRequestBody(data: String): RequestBody {
            return data.toRequestBody()
            //   return data.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        fun getUserFormattedDate(time: Long, pattern: String): String {
            try {
                val sdf = SimpleDateFormat(pattern)
                val netDate = Date(time * 1000)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }


        fun getChatFormattedDate(smsTimeInMilis: Long): String? {
            return if (smsTimeInMilis == 0L) {
                "-"
            } else {
                val smsTime = Calendar.getInstance()
                val now = Calendar.getInstance()
                smsTime.timeInMillis = smsTimeInMilis
                if (now[Calendar.MONTH] == smsTime[Calendar.MONTH]
                    && now[Calendar.DATE] == smsTime[Calendar.DATE]
                ) {
                    "Today, " + DateFormat.format("hh:mm aa", smsTime)
                        .toString()
                } else {
                    DateFormat.format("dd MMM yyyy", smsTime).toString()
                }
            }
        }

        fun convertUTCToIST(utcDate: String): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            return formatter.format(utcDate)
        }

        fun getTimeAgo(context: Context, time: Long): String? {
            var time = time
            if (time < 1000000000000L) {
                time *= 1000
            }

            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return null
            }


            val diff = now - time
            return if (diff < MINUTE_MILLIS) {
                var seconds = (diff / SECOND_MILLIS)
                if (seconds <= 1) {
                    "1 second ago"
//                    context.getString(R.string.just_now)
                } else {
                    "$seconds seconds ago"
                }
//                context.getString(R.string.just_now)
//                (diff / SECOND_MILLIS).toString() + " seconds ago"
            } else if (diff < 2 * MINUTE_MILLIS) {
                "1 minute ago"
            } else if (diff < 50 * MINUTE_MILLIS) {
                (diff / MINUTE_MILLIS).toString() + " minutes ago"
            } else if (diff < 90 * MINUTE_MILLIS) {
                context.getString(R.string.one_hour_ago)
            } else if (diff < 24 * HOUR_MILLIS) {
                (diff / HOUR_MILLIS).toString() + " ${context.getString(R.string.hours_ago)}"
            } else if (diff < 48 * HOUR_MILLIS) {
                context.getString(R.string.yesterday)
//                "1 day ago"
            } else if ((diff / DAY_MILLIS).toInt() <= 1) {
                context.getString(R.string.day_ago)
            } else if ((diff / DAY_MILLIS).toInt() < 7) {
                (diff / DAY_MILLIS).toString() + " ${context.getString(R.string.days_ago)}"
            } else if ((diff / WEEK_MILLIS).toInt() < 2) {
                context.getString(R.string.one_week_ago)
            } else if ((diff / WEEK_MILLIS).toInt() < 5) {
                "" + (diff / WEEK_MILLIS).toInt() + " ${context.getString(R.string.weeks_ago)}"
            } else if ((diff / DAY_MILLIS).toInt() <= 59) {
                context.getString(R.string.one_month_ago)
            } else if ((diff / DAY_MILLIS).toInt() <= 364) {
                "" + ((diff / DAY_MILLIS).toInt()) / 30 + " ${context.getString(R.string.months_ago)}"
            } else if ((((diff / DAY_MILLIS).toInt()) / 365) < 2) {
                context.getString(R.string.year_ago)

            } else if ((((diff / DAY_MILLIS).toInt()) / 365) >= 2) {
                "${(((diff / DAY_MILLIS).toInt()) / 365).toInt()} ${context.getString(R.string.years_ago)}"
            } else {
                ""
            }
        }

        fun getTimeAgoDate(timestamp: Long): String {
            var time = timestamp
            if (time < 1000000000000L) {
                time *= 1000
            }

            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return ""
            }

            val diff = now - time
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = time

//            var timeString = "dd.MM.yy HH:mm"
            var timeString = "dd.MM.yy"
            if (DateUtils.isToday(time)) {
//                timeString = "HH:mm"
                timeString = "hh:mm a"
            }

            if (diff < 24 * HOUR_MILLIS) {
//                timeString = "HH:mm" //24 hours
                timeString = "h:mm a" //12 hours
                return DateFormat.format(timeString, calendar).toString()
            } else if (diff < 48 * HOUR_MILLIS) {
                return "Yesterday"
            } else {
                return DateFormat.format(timeString, calendar).toString()
            }
        }

        fun convertDuration(duration: Long): String? {
            var out: String? = null
            var hours: Long = 0
            hours = try {
                duration / 3600000
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return out
            }
            val remaining_minutes = (duration - hours * 3600000) / 60000
            var minutes = remaining_minutes.toString()
            if (minutes == "0") {
                minutes = "00"
            }
            val remaining_seconds =
                duration - hours * 3600000 - remaining_minutes * 60000
            var seconds = remaining_seconds.toString()
            seconds = if (seconds.length < 2) {
                "00"
            } else {
                if (seconds.length < 5) {
                    "0" + seconds.substring(0, 1)
                } else {
                    seconds.substring(0, 2)
                }
                //seconds.substring(0, 2)
            }
            out = if (hours > 0) {
                "$hours:$minutes:$seconds"
            } else {
                "$minutes:$seconds"
            }
            return out
        }

        fun isImageFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("image")
        }

        fun isPdfFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("pdf")
        }

        fun checkValidDateOrNot(checkin: String, checkOut: String): Boolean {
            val sdf = SimpleDateFormat("dd MMM, yyyy")
            val dateFrom: Date = sdf.parse(checkin)
            val dateTo: Date = sdf.parse(checkOut)
            var differenceInTime = dateTo.time - dateFrom.time
            var differenceInDate = differenceInTime / (1000 * 3600 * 24)

            if (differenceInDate < 1) {
                return false
            }
            return true
        }

        fun setUserImage(context: Context, url: String?, imgProfile: ImageView) {
            Glide.with(context)
                .load(url)
                .centerCrop()
                .placeholder(viewImageProgress(context!!))
                .error(R.mipmap.ic_user_profile)
                .into(imgProfile)
        }

        fun viewImageProgress(context: Context): CircularProgressDrawable {
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()
            return circularProgressDrawable
        }

        fun logFacebookEvent(context: Context, eventName: String, params: Bundle) {
            val logger = AppEventsLogger.newLogger(context)
            logger.logEvent(eventName, params);
//            logger.logEvent(eventName, 54.23, params);
        }

        fun logFirebaseAnalyticsEvent(context: Context, eventName: String, params: Bundle) {
            var firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
            firebaseAnalytics.logEvent(eventName, params);
        }

        fun checkForUrl(message: String?): Boolean {
            val URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$"
            var matched = false;
            val p: Pattern = Pattern.compile(URL_REGEX)
            val m: Matcher = p.matcher(message) //replace with string to compare

            if (m.find()) {
                matched = true;
            }
            return matched;
        }

         fun commonDialog(activity:Activity,onlistviewclicklistener:OnListClickListener, msg:String) {
            AlertDialog.Builder(activity!!, R.style.MyDialogTheme).apply {
                this.setMessage(msg)
                this.setPositiveButton(
                    activity.getString(R.string.yes)

                ) { dialog, _ ->
                    dialog.dismiss()
onlistviewclicklistener.onListClickSimple(0,"");
                }
                this.setNegativeButton(activity.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }

            }.create().apply {
                this.setOnShowListener {
                    this.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                    this.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                }
            }.show()

        }
    }


}