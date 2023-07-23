package com.screenmirror.contractsdemo.utilities.extensions

import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong


object IntExtensions {

    fun Int.formatTime(): String {
        var seconds = (this.toDouble() / 1000).roundToLong()
        val hours: Long = TimeUnit.SECONDS.toHours(seconds)
        if (hours > 0) seconds -= TimeUnit.HOURS.toSeconds(hours)
        val minutes: Long = if (seconds > 0) TimeUnit.SECONDS.toMinutes(seconds) else 0
        if (minutes > 0) seconds -= TimeUnit.MINUTES.toSeconds(minutes)
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds) else String.format("%02d:%02d", minutes, seconds)
    }

}