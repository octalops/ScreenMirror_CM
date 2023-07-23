package com.screenmirror.contractsdemo.ui.filelist.adapter

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.screenmirror.contractsdemo.R
import com.screenmirror.contractsdemo.databinding.MediaItemCardBinding
import com.screenmirror.contractsdemo.models.Media
import com.screenmirror.contractsdemo.utilities.FileUtil
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.mainHolderActivity
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.getScreenWidth
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeNavigationOnClickListener
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeOnClickListener

class MediaAdapter(
    videoList: List<Media>,
    mediaType: String,
    navController: NavController,
    fragment: Fragment
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    val fragment2 = fragment

    var videoList: List<Media> = ArrayList()
    var mediaType: String
    var navController: NavController
    override fun getItemViewType(position: Int): Int {
        return R.layout.media_item_card
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding =
            MediaItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(videoList, mediaType, position, navController)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MediaViewHolder(val itemBinding: MediaItemCardBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(
            videoList: List<Media>,
            mediaType: String,
            position: Int,
            navController: NavController
        ) {
            if (videoList.size > 0) {
                val media = videoList[position]
                //       getVideoThumbnail(media.getUri(),holder.itemView.getContext(), holder.getImgMedia());
                if (mediaType == Enums.MediaType.Audio.value) {

                    //getSongArt(FileUtil.getPath(itemBinding.root.context, media.uri), itemBinding.root.context, itemBinding.imgMedia)
                    itemBinding.cardMedia.visibility = View.GONE
                    itemBinding.musicItem.visibility = View.VISIBLE
                    itemBinding.txtAudioName.text = media.name
                } else {
                    Glide.with(itemBinding.root.context)
                        .load(media.uri)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(itemBinding.imgMedia)
                }
                itemBinding.musicItem.setSafeOnClickListener { v: View ->
                    fragment2.mainHolderActivity?.showInterstitialAd {
                        dialogClicked(v, mediaType, media, navController)
                        mediaName = media.name
                    }
                }

                itemBinding.cardMedia.setSafeOnClickListener { v: View ->
                    fragment2.mainHolderActivity?.showInterstitialAd {
                        dialogClicked(v, mediaType, media, navController)
                        mediaName = media.name
                    }
                }
            }
        }

        private fun dialogClicked(
            v: View,
            mediaType: String,
            media: Media,
            navController: NavController
        ) {
            val dialog = Dialog(v.context)
            dialog.setContentView(R.layout.media_action_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(v.context.resources.getColor(R.color.transparentColor)))
            v.context?.let {
                dialog.window?.setLayout(
                    it.getScreenWidth(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val radioBtnGroup: RadioGroup = dialog.findViewById(R.id.radioGroup)
            val btnDone = dialog.findViewById<Button>(R.id.btnDialog)
            var radioBtnStatus: String? = null

            radioBtnGroup.setOnCheckedChangeListener { radioGroup, i ->
                when (i) {
                    R.id.rdBtnHere -> {
                        Log.e("radio", "here")
                        radioBtnStatus = "here"
                    }
                    R.id.rdBtnTv -> {
                        Log.e("radio", "tv")
                        radioBtnStatus = "tv"
                    }
                }
            }

            btnDone.setSafeNavigationOnClickListener {
                fragment2.mainHolderActivity?.showInterstitialAd {
                    val link = if (mediaType == Enums.MediaType.Audio.value) {
                        FileUtil.getPath(itemBinding.root.context, media.uri)
                    } else {
                        FileUtil.getPath(itemBinding.root.context, media.uri)
                    }
                    val bundle = bundleOf(Enums.BundleValues.MediaType.value to mediaType)
                    bundle.putString(Enums.BundleValues.Link.value, link)
                    when (radioBtnStatus) {
                        "here" -> {
                            navController.navigate(R.id.actionMediaListToMediaPreview, bundle)
                            dialog.dismiss()
                        }
                        "tv" -> {
                            navController.navigate(R.id.actionMediaListToConnectionFragment, bundle)
                            dialog.dismiss()
                        }
                        else -> {
                            Toast.makeText(
                                it.context,
                                "Please Select an option to continue !",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            dialog.show()
        }

    }

    companion object {
        @JvmField
        var mediaName: String? = ""
    }

    init {
        this.videoList = videoList
        this.mediaType = mediaType
        this.navController = navController
    }
}