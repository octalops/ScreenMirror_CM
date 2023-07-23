package com.screenmirror.contractsdemo.utilities.manager

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.getScreenWidth

object WaitDialogManager {

    fun makeWaitDialog(context: Context): Dialog {

        val waitDialog = Dialog(context)
        waitDialog.setCancelable(false)
        waitDialog.setContentView(R.layout.waitcustomdiolog)
        context.let { waitDialog.window?.setLayout(it.getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT) }

        return waitDialog
    }
}