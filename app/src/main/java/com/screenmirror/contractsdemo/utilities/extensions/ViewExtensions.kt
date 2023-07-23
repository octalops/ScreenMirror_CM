package com.screenmirror.contractsdemo.utilities.extensions

import android.content.Context
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.navigation.NavController
import com.screenmirror.contractsdemo.utilities.SafeClickListener

object ViewExtensions {

    fun Context.getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun View.hide() {
        this.visibility = View.GONE
    }

    fun View.visible() {
        this.visibility = View.VISIBLE
    }

    fun View.inVisible() {
        this.visibility = View.INVISIBLE
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    fun View.setSafeNavigationOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener(1500) {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    fun NavController.safeNavigate(
        action: Int,
        defaultInterval: Int = 5000
    ) {
        var lastTimeClicked: Long = 0
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            Log.e("save", "return")
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        Log.e("save", "perform")
        this.navigate(action)
    }
}