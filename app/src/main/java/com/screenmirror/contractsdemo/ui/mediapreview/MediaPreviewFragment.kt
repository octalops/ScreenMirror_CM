package com.screenmirror.contractsdemo.ui.mediapreview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.VideoView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.MediaPreviewFragmentBinding
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.mainHolderActivity
import com.screenmirror.contractsdemo.utilities.extensions.IntExtensions.formatTime
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.visible
import kotlinx.coroutines.*
import java.io.File


class MediaPreviewFragment : Fragment() {

    private var binding: MediaPreviewFragmentBinding? = null
    private var mediaType: String? = null
    private var link: String? = null
    private var videoView: VideoView? = null
    private var repeatFun: Job? = null
    private var value = 1000
    private var totalDuration = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MediaPreviewFragmentBinding.inflate(layoutInflater, container, false)

        mediaType = arguments?.getString(Enums.BundleValues.MediaType.value)
        link = arguments?.getString(Enums.BundleValues.Link.value)
        if (link != null) {
            val file = File(link)
            binding?.toolbar?.txtHeading?.text = file.name
        }
        if (mediaType != null) {
            Log.e("mediaType", mediaType!!)
        }

        setLayout()
        setClickListener()
        return binding?.root
    }

    private fun setLayout() {
        binding?.toolbar?.imgCast?.visible()
        when (mediaType) {
//            Enums.MediaType.Audio.value -> {
//                // binding?.audioViewMain?.visible()
//
//            }
            Enums.MediaType.Video.value, Enums.MediaType.Audio.value -> {
                binding?.videoViewMain?.visible()
                videoView = binding?.videoView as? VideoView
                videoView?.setVideoPath(link)
//                val mediaPlayer = MediaPlayer()
//                mediaPlayer.setDataSource(link)
//                totalDuration = mediaPlayer.duration

                videoView?.setOnPreparedListener {
                    Log.e("prepared", "called")
                    totalDuration = videoView?.duration ?: 0
                    binding?.txtVideoTotalTime?.text = totalDuration.formatTime()
                    startRepeatingJob(totalDuration)
                    binding?.videoSeekbar?.max = totalDuration
                    videoView?.start()
                }

                videoView?.setOnCompletionListener {
                    binding?.imgPlayVideo?.setImageResource(R.drawable.btn_play)
                    binding?.videoSeekbar?.progress = 0
                }

                binding?.imgPlayVideo?.setSafeOnClickListener {
                    if (videoView?.isPlaying == true) {
                        videoView?.pause()
                        binding?.imgPlayVideo?.setImageResource(R.drawable.btn_play)
                        Log.e("m", "pause")
                    } else {

                        videoView?.start()
                        binding?.imgPlayVideo?.setImageResource(R.drawable.btn_pause)
                        Log.e("m", "resume")

                    }
                }

            }
            Enums.MediaType.Photo.value -> {
                binding?.photoViewMain?.visible()
                activity?.let {
                    binding?.imgView?.let { it1 ->
                        Glide.with(it)
                            .load(link)
                            .placeholder(R.drawable.image_button)
                            .error(R.drawable.image_button)
                            .into(it1)
                    }
                }
            }

        }

        setSeekbar()

    }

    private fun setSeekbar() {
        binding?.videoSeekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    //       videoView.re
                    p0?.progress?.let { videoView?.seekTo(it) }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }

    private fun startRepeatingJob(duration: Int): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                // add your task here
                updateUI(duration)
                delay(500)
            }
        }
    }

    private suspend fun updateUI(duration: Int) {
        if (binding?.videoSeekbar?.progress!! >= duration) {
            //Cancel the loop
            repeatFun?.cancel()
        }
        //  val current: Int? = videoView?.currentPosition
//        val progress = (current?.times(100) ?: 0) / duration
        withContext(Dispatchers.Main) {
            binding?.videoSeekbar?.progress = videoView?.currentPosition ?: 0
            binding?.txtVideoPlayTime?.text = videoView?.currentPosition?.formatTime()
        }
    }

    private fun setClickListener() {
        binding?.toolbar?.btnBack?.setSafeOnClickListener { findNavController().navigateUp() }
        binding?.toolbar?.imgCast?.setSafeOnClickListener {

            val bundle = bundleOf(Enums.BundleValues.Link.value to link)
            bundle.putString(Enums.BundleValues.MediaType.value, mediaType)
            mainHolderActivity?.showInterstitialAd {
                findNavController().navigate(R.id.actionFragmentPreviewToConnection, bundle)
            }
        }
    }

}