package com.screenmirror.contractsdemo.ui.connection.adapter

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.screenmirror.contractsdemo.MainHolderActivity
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.ItemMediaDeviceBinding
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.getScreenWidth
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fourthline.cling.model.meta.Device

class MediaDeviceAdapter(private val mediaLink: String, private val mediaType: String, private val navController: NavController) : RecyclerView
.Adapter<MediaDeviceAdapter.MediaDeviceViewHolder>() {

    private val deviceList = MainHolderActivity.devices

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaDeviceViewHolder {
        val binding = ItemMediaDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaDeviceViewHolder, position: Int) {

        deviceList?.get(position)?.let { holder.bind(it, mediaLink, mediaType, navController) }
    }

    override fun getItemCount(): Int {
        return deviceList?.size ?: 0
    }


    class MediaDeviceViewHolder(private val itemBinding: ItemMediaDeviceBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(device: Device<*, *, *>, mediaLink: String, mediaType: String, navController: NavController) {
            itemBinding.txtDevicName.text = device.details.friendlyName

            Log.e("device", device.details.friendlyName + device.details.modelDetails.modelDescription)

            itemBinding.root.setSafeOnClickListener { v ->

                val dialog = Dialog(v.context)
                dialog.setContentView(R.layout.connecting_dialog)
                dialog.window?.setBackgroundDrawable(ColorDrawable(v.context.resources.getColor(R.color.transparentColor)))
                v.context?.let { dialog.window?.setLayout(it.getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT) }

                dialog.show()

                val wm = v.context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
                val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
                val mainUrl = "http://$ip:4000"

                if (mediaLink == "empty") {

                } else {
                    MainHolderActivity.setSelectionDevice(device)

                    itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
                        MainHolderActivity.startServer(mediaLink)
                        withContext(Dispatchers.Main) {
                            when (mediaType) {
                                Enums.MediaType.Audio.value -> {

                                    MainHolderActivity.play(mainUrl, 2) {
                                        if (it) {
                                            Log.e("play", it.toString())
                                            val bundle = bundleOf("mediaType" to 2)
                                            bundle.putString(Enums.BundleValues.Link.value, mediaLink)
                                            dialog.dismiss()

                                            navController.navigate(R.id.actionFragmentConnectionToMediaCasting, bundle)

                                        } else {
                                            Toast.makeText(itemView.context, "Some Error Occurred !", Toast.LENGTH_SHORT).show()

                                            dialog.dismiss()
                                        }
                                    }


                                }
                                Enums.MediaType.Video.value -> {

                                    MainHolderActivity.play(mainUrl, 1) {
                                        if (it) {
                                            Log.e("play", it.toString())
                                            val bundle = bundleOf("mediaType" to 1)
                                            bundle.putString(Enums.BundleValues.Link.value, mediaLink)

                                            dialog.dismiss()

                                            navController.navigate(R.id.actionFragmentConnectionToMediaCasting, bundle)
                                        } else {
                                            Toast.makeText(itemView.context, "Some Error Occurred !", Toast.LENGTH_SHORT).show()

                                            dialog.dismiss()
                                        }
                                    }

                                }
                                Enums.MediaType.Photo.value -> {

                                    MainHolderActivity.play(mainUrl, 0) {
                                        if (it) {
                                            Log.e("play", it.toString())
                                            val bundle = bundleOf("mediaType" to 0)
                                            bundle.putString(Enums.BundleValues.Link.value, mediaLink)

                                            dialog.dismiss()

                                            navController.navigate(R.id.actionFragmentConnectionToMediaCasting, bundle)
                                        } else {
                                            Toast.makeText(itemView.context, "Some Error Occurred !", Toast.LENGTH_SHORT).show()

                                            dialog.dismiss()
                                        }
                                    }
                                }
                            }

                        }
                    }

                }
            }
        }
    }
}