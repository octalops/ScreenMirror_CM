package com.screenmirror.contractsdemo.ui.privacypolicy

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.WebViewFragmentBinding
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import com.screenmirror.contractsdemo.utilities.manager.WaitDialogManager


class WebViewFragment : Fragment() {

    private var binding: WebViewFragmentBinding? = null
    private var dialog: Dialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WebViewFragmentBinding.inflate(inflater, container, false)
        binding?.webView?.webViewClient = WebViewClient()
        dialog = activity?.let { WaitDialogManager.makeWaitDialog(it) }
        dialog?.show()

        binding?.webView?.loadUrl(getString(R.string.privacy_policy_link))
        binding?.toolbar?.txtHeading?.text = "Privacy Policy"


        binding?.webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                dialog?.dismiss()
            }
        }

        binding?.toolbar?.btnBack?.setSafeOnClickListener {
            findNavController().navigateUp()
        }

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (binding?.webView != null) {
            //binding?.webView?.setWebViewClient(null)
            binding?.webView?.webChromeClient = null
            binding?.webView?.loadUrl("about:blank")
        }
    }
}