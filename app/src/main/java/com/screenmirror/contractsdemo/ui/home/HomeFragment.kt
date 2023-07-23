package com.screenmirror.contractsdemo.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.FragmentHomeBinding
import com.screenmirror.contractsdemo.utilities.Constants
import com.screenmirror.contractsdemo.utilities.Constants.WEB_VIEW_FRAGMENT
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeNavigationOnClickListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setSafeOnClickListeners()


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setSafeOnClickListeners() {

        binding.btnLetsGo.setSafeNavigationOnClickListener {
            findNavController().navigate(
                Uri.parse(Constants.HOME_FRAGMENT_DEEP_LINK), NavOptions.Builder()
                    .setPopUpTo(
                        R.id.fragmentMain,
                        false
                    ).setLaunchSingleTop(true)
                    .build()
            )
        }

        binding.txtPrivacyPolicy.setSafeNavigationOnClickListener {
            findNavController().navigate(
                WEB_VIEW_FRAGMENT.toUri(), NavOptions.Builder()
                    .setPopUpTo(
                        R.id.fragmentWebView,
                        false
                    ).setLaunchSingleTop(true)
                    .build()
            )
        }

    }
}