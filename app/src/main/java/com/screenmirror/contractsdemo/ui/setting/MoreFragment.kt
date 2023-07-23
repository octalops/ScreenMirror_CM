package com.screenmirror.contractsdemo.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.screenmirror.contractsdemo.databinding.MoreFragmentBinding
import com.screenmirror.contractsdemo.utilities.Constants.WEB_VIEW_FRAGMENT
import com.screenmirror.contractsdemo.utilities.datastore.DataStore
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoreFragment : Fragment() {

    var binding: MoreFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MoreFragmentBinding.inflate(inflater, container, false)

        setClickListeners()
        setDarkState()

        binding?.toolbar?.txtHeading?.text = "More"

        return binding?.root
    }

    private fun setDarkState() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val darkStatus = activity?.let { DataStore.getDarkMode(it) }?.first()

            withContext(Dispatchers.Main) {
                binding?.switchDark?.isChecked = darkStatus == true
            }
        }
    }

    private fun setClickListeners() {
        binding?.toolbar?.btnBack?.setSafeOnClickListener { findNavController().navigateUp() }

        binding?.viewPrivacyPolicy?.setSafeOnClickListener {
            findNavController().navigate(WEB_VIEW_FRAGMENT.toUri())
        }
        binding?.switchDark?.setOnCheckedChangeListener { _, b ->

            if (b) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    activity?.let { DataStore.setDarkMode(it, b) }
                }
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            } else {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    activity?.let { DataStore.setDarkMode(it, b) }
                }
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

    }
}