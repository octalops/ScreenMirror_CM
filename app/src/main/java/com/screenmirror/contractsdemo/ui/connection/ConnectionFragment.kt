package com.screenmirror.contractsdemo.ui.connection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.screenmirror.contractsdemo.MainHolderActivity
import com.screenmirror.contractsdemo.databinding.ConnectionFragmentBinding
import com.screenmirror.contractsdemo.ui.connection.adapter.MediaDeviceAdapter
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.getWiFiInfoSSID
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.mainHolderActivity
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.hide
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.visible

class ConnectionFragment : Fragment() {

    var binding: ConnectionFragmentBinding? = null
    var mediaLink: String? = null
    var mediaType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ConnectionFragmentBinding.inflate(layoutInflater, container, false)
        mediaLink = arguments?.getString(Enums.BundleValues.Link.value)
        mediaType = arguments?.getString(Enums.BundleValues.MediaType.value)

        Log.e("argumentlink", "link$mediaLink")

        Log.e("argumentType", "type$mediaType")

        setClickListeners()
        if (MainHolderActivity.devices?.isEmpty() == false) {
            binding?.txtNoDevice?.hide()
            binding?.rvDevices?.visible()
            binding?.rvDevices?.adapter = mediaLink?.let {
                mediaType?.let { it1 ->
                    MediaDeviceAdapter(
                        it,
                        it1,
                        findNavController()
                    )
                }
            }
        }
        binding?.txtWifiName?.text = activity?.getWiFiInfoSSID()


        return binding?.root
    }

    private fun setClickListeners() {
        binding?.btnCancel?.setSafeOnClickListener {
            mainHolderActivity?.showInterstitialAd {
                findNavController().navigateUp()
            }
        }

        binding?.imgRefresh?.setSafeOnClickListener {
            mainHolderActivity?.showInterstitialAd {
                if (MainHolderActivity.devices?.isEmpty() == false) {
                    binding?.txtNoDevice?.hide()
                    binding?.rvDevices?.visible()
                    binding?.rvDevices?.adapter = mediaLink?.let {
                        mediaType?.let { it1 ->
                            MediaDeviceAdapter(
                                it,
                                it1,
                                findNavController()
                            )
                        }
                    }
                } else {
                    binding?.txtNoDevice?.visible()
                }
            }
        }
    }
}