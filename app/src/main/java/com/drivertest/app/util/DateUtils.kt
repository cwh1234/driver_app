package com.drivertest.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault())

    fun todayDateString(): String = dateFormat.format(Date())

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun daysAgoDateString(daysAgo: Int): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(cal.time)
    }

    fun yesterdayDateString(): String = daysAgoDateString(1)

    fun isConsecutiveDay(date1: String, date2: String): Boolean {
        try {
            val d1 = dateFormat.parse(date1)!!
            val d2 = dateFormat.parse(date2)!!
            val diff = (d1.time - d2.time) / (1000 * 60 * 60 * 24)
            return diff == 1L
        } catch (e: Exception) {
            return false
        }
    }
}
