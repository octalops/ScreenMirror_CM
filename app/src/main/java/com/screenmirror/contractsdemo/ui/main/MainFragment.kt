package com.screenmirror.contractsdemo.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.ActivityMainBinding
import com.screenmirror.contractsdemo.utilities.datastore.DataStore
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.getWiFiInfoSSID
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.mainHolderActivity
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.getScreenWidth
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeNavigationOnClickListener
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainFragment : Fragment() {
    var binding: ActivityMainBinding? = null
    var retriever = MediaMetadataRetriever()
    var index = 0

    private val someActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result: Boolean ->
        if (result) {
            when (index) {
                0 -> {
                    navigateToMediaList(Enums.MediaType.Video.value)
                }
                1 -> {
                    navigateToMediaList(Enums.MediaType.Photo.value)
                }
                2 -> {
                    navigateToMediaList(Enums.MediaType.Audio.value)
                }
            }
        }
    }

    private val casting = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.e("mirror", it.data.toString())
        Log.e("mirror", it.resultCode.toString())
        Log.e("mirror", it.data?.action.toString())
        Log.e("mirror", it.toString())


    }


    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.e("location", "allowed fine")
                    binding?.txtWifiName?.text = activity?.getWiFiInfoSSID()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.e("location", "allowed coarse")
                    binding?.txtWifiName?.text = activity?.getWiFiInfoSSID()
                }
                else -> {
                    Toast.makeText(
                        activity,
                        "Please allow location permission, otherwise application may not work properly.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater, container, false)
        setClickListeners()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            if (activity?.let { DataStore.getIsFirstTime(it).first() } == true) {
                withContext(Dispatchers.Main) {
                    showDialog()
                }
            }
        }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        binding?.txtWifiName?.text = activity?.getWiFiInfoSSID()


        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    mainHolderActivity?.showInterstitialAd {
                        showExitDialog()
                    }
                }
            })

        return binding?.root
    }

    private fun setClickListeners() {
        binding?.imgVideo?.setSafeNavigationOnClickListener { v: View? ->
            index = 0
            navigateToMediaList(Enums.MediaType.Video.value)
        }
        binding?.txtVideo?.setSafeNavigationOnClickListener { v: View? ->
            index = 0
            navigateToMediaList(Enums.MediaType.Video.value)
        }

        binding?.txtPhoto?.setSafeNavigationOnClickListener { v: View? ->
            index = 1
            navigateToMediaList(Enums.MediaType.Photo.value)
        }
        binding?.imgPhoto?.setSafeNavigationOnClickListener { v: View? ->
            index = 1
            navigateToMediaList(Enums.MediaType.Photo.value)
        }

        binding?.txtAudio?.setSafeNavigationOnClickListener { v: View? ->
            index = 2
            navigateToMediaList(Enums.MediaType.Audio.value)
        }
        binding?.imgAudio?.setSafeNavigationOnClickListener { v: View? ->
            index = 2
            navigateToMediaList(Enums.MediaType.Audio.value)
        }

        binding?.btnMirror?.setSafeOnClickListener { v: View? ->

            try {
                casting.launch(Intent("android.settings.CAST_SETTINGS"))
                return@setSafeOnClickListener
            } catch (exception1: Exception) {
                exception1.printStackTrace()
                Toast.makeText(activity, "Device not supported", Toast.LENGTH_LONG).show()
            }
        }

        binding?.imgSetting?.setSafeOnClickListener {
            mainHolderActivity?.showInterstitialAd {
                findNavController().navigate(R.id.actionFragmentMainToMore)
            }
        }
    }

    private fun navigateToMediaList(mediaType: String) {

        when {
            activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                val bundle = bundleOf(Enums.BundleValues.MediaType.value to mediaType)

                mainHolderActivity?.showInterstitialAd {
                    findNavController().navigate(R.id.actionMainToFileList, bundle)
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {

//                val mySnackbar = binding?.snackLayout?.let {
//                    Snackbar.make(it, "Open Settings and allow storage permission to continue !", LENGTH_LONG)
//                        .setAction("Open Setting") {
//                            val intent =
//                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                            val uri: Uri =
//                                Uri.fromParts("package", activity?.packageName, null)
//                            intent.data = uri
//                            startActivity(intent)
//                        }
//                }
//                mySnackbar?.setActionTextColor(Color.BLUE)
//
//                val snackbarView = mySnackbar?.view
//                snackbarView?.setBackgroundColor(Color.WHITE)
//                val textView =
//                    snackbarView?.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
//                textView.setTextColor(Color.BLACK)
//                textView.textSize = 18f
//
//                mySnackbar.show()

                Toast.makeText(
                    activity,
                    "Open App Settings and allow storage permission to continue !",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                index = 0
                someActivityResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun extractVideoLocationInfo(videoUri: Uri) {
        try {
            retriever.setDataSource(activity, videoUri)
        } catch (e: RuntimeException) {
            Log.d("APP_TAG", "Cannot retrieve video file", e)
        }
        Log.d("link", videoUri.path!!)
    }

    private fun showDialog() {

        val dialog = activity?.let { Dialog(it) }
        dialog?.setContentView(R.layout.connection_dialog)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.transparentColor)))
        activity?.let {
            dialog?.window?.setLayout(
                it.getScreenWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialog?.show()
        val btnOk = dialog?.findViewById<Button>(R.id.btnOk)
        btnOk?.setSafeOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                activity?.let { DataStore.setIsFirstTime(it, false) }
                withContext(Dispatchers.Main) {
                    mainHolderActivity?.showInterstitialAd {
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    fun showExitDialog() {
        val dialog = activity?.let { Dialog(it) }
        dialog?.setContentView(R.layout.exit_dialog)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.transparentColor)))
        activity?.let {
            dialog?.window?.setLayout(
                it.getScreenWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialog?.show()
        val btnOk = dialog?.findViewById<Button>(R.id.btnOkExit)
        val btnCancel = dialog?.findViewById<Button>(R.id.btnCancelExit)

        btnCancel?.setSafeOnClickListener {
            dialog.dismiss()
        }
        btnOk?.setSafeOnClickListener {
            activity?.moveTaskToBack(true);
            activity?.finish()
            dialog.dismiss()
        }
    }

}