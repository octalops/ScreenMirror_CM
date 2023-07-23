package com.screenmirror.contractsdemo.ui.castingview

import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.screenmirror.contractsdemo.MainHolderActivity
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.MediaCastFragmentBinding
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.hide
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import java.io.File

class MediaCastFragment : Fragment() {

    private var binding: MediaCastFragmentBinding? = null
    private var isPlaying = true
    private var mediaType = 0
    private var mediaLink: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MediaCastFragmentBinding.inflate(inflater, container, false)

        mediaType = arguments?.getInt("mediaType") ?: 0
        mediaLink = arguments?.getString(Enums.BundleValues.Link.value)

        val file = File(mediaLink)
        binding?.toolbar?.txtHeading?.text = file.name
        setClickListeners()

        if (mediaType == Enums.MediaType.Photo.number) {
            binding?.btnPlusVolume?.hide()
            binding?.btnMinusVolume?.hide()
        }

        return binding?.root
    }

    private fun setClickListeners() {
        val wm = activity?.applicationContext?.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        val mainUrl = "http://$ip:4000"

        binding?.imgPlayMedia?.setSafeOnClickListener {
            if (isPlaying) {
                MainHolderActivity.stopCasting {

                }
                binding?.imgPlayMedia?.setImageResource(R.drawable.btn_play)
                isPlaying = false
            } else {
                MainHolderActivity.play(mainUrl, itemType = mediaType) {
                    if (it) {
                        binding?.imgPlayMedia?.setImageResource(R.drawable.icon_stop_cast)
                        isPlaying = true
                    } else {
                        Toast.makeText(activity, "Some Error Occured !", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }

                }

            }


        }

        binding?.toolbar?.btnBack?.setSafeOnClickListener {
            when (mediaType) {
                0 -> {
                    val bundle = bundleOf(Enums.BundleValues.MediaType.value to Enums.MediaType.Photo.value)
                    findNavController().navigate(R.id.actionCastingToMediaList, bundle)
                }
                1 -> {
                    val bundle = bundleOf(Enums.BundleValues.MediaType.value to Enums.MediaType.Video.value)
                    findNavController().navigate(R.id.actionCastingToMediaList, bundle)
                }
                2 -> {
                    val bundle = bundleOf(Enums.BundleValues.MediaType.value to Enums.MediaType.Audio.value)
                    findNavController().navigate(R.id.actionCastingToMediaList, bundle)
                }
            }
            MainHolderActivity.stopCasting {
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (mediaType) {
                    0 -> {
                        val bundle = bundleOf(Enums.BundleValues.MediaType.value to Enums.MediaType.Photo.value)
                        findNavController().navigate(R.id.actionCastingToMediaList, bundle)
                    }
                    1 -> {
                        val bundle = bundleOf(Enums.BundleValues.MediaType.value to Enums.MediaType.Video.value)
                        findNavController().navigate(R.id.actionCastingToMediaList, bundle)
                    }
                    2 -> {
                        val bundle = bundleOf(Enums.BundleValues.MediaType.value to Enums.MediaType.Audio.value)
                        findNavController().navigate(R.id.actionCastingToMediaList, bundle)
                    }
                }
                MainHolderActivity.stopCasting {
                }
            }
        })

        binding?.btnMinusVolume?.setSafeOnClickListener {
            MainHolderActivity.decreaseVolume()
        }

        binding?.btnPlusVolume?.setSafeOnClickListener {
            MainHolderActivity.increaseVolume()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MainHolderActivity.stopCasting {
        }
    }

}