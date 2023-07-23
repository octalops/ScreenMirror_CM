package com.screenmirror.contractsdemo.utilities

import android.util.DisplayMetrics
import android.view.View
import com.screenmirror.contractsdemo.MainHolderActivity
import com.google.android.gms.ads.AdSize

object Common {

    fun getAdSize(activity: MainHolderActivity, view: View): AdSize? {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = view.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }


}