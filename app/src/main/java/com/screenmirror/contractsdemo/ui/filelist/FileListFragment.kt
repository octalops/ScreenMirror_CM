package com.screenmirror.contractsdemo.ui.filelist

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.screenmirror.contractsdemo.databinding.ActivityFileListBinding
import com.screenmirror.contractsdemo.models.Media
import com.screenmirror.contractsdemo.ui.filelist.adapter.MediaAdapter
import com.screenmirror.contractsdemo.utilities.enums.Enums
import com.screenmirror.contractsdemo.utilities.extensions.ContextExtensions.mainHolderActivity
import com.screenmirror.contractsdemo.utilities.extensions.ViewExtensions.setSafeNavigationOnClickListener

class FileListFragment : Fragment() {
    var mediaListRecyclerView: RecyclerView? = null
    var binding: ActivityFileListBinding? = null
    private var mediaAdapter: MediaAdapter? = null
    var mediaType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreate(savedInstanceState)
        binding = ActivityFileListBinding.inflate(inflater, container, false)
        mediaType = arguments?.getString(Enums.BundleValues.MediaType.value)

        setClickListener()


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) { override fun handleOnBackPressed() {
                    mainHolderActivity?.showInterstitialAd {
                        backPressed()
                    }
                }
            })

        when (mediaType) {
            Enums.MediaType.Audio.value -> {
                binding?.toolbar?.txtHeading?.text = "Audios"
                mediaListRecyclerView = binding?.recycleViewMedia
                mediaListRecyclerView?.layoutManager = LinearLayoutManager(activity)
                mediaListRecyclerView?.setHasFixedSize(true)
                mediaType?.let {
                    mediaAdapter = MediaAdapter(audio, it, findNavController(), this)
                    mediaListRecyclerView?.adapter = mediaAdapter
                }
            }
            Enums.MediaType.Video.value -> {
                binding?.toolbar?.txtHeading?.text = "Videos"
                mediaListRecyclerView = binding?.recycleViewMedia
                mediaListRecyclerView?.layoutManager = GridLayoutManager(activity, 3)
                mediaListRecyclerView?.setHasFixedSize(true)
                mediaType?.let {
                    mediaAdapter = MediaAdapter(video, it, findNavController(), this)
                    mediaListRecyclerView?.adapter = mediaAdapter
                }
            }
            Enums.MediaType.Photo.value -> {
                binding?.toolbar?.txtHeading?.text = "Images"
                mediaListRecyclerView = binding?.recycleViewMedia
                mediaListRecyclerView?.layoutManager = GridLayoutManager(activity, 3)
                mediaListRecyclerView?.setHasFixedSize(true)
                mediaType?.let {
                    mediaAdapter = MediaAdapter(photos, it, findNavController(), this)
                    mediaListRecyclerView?.adapter = mediaAdapter
                }
            }
        }
        return binding?.root

    }// Get values of columns for a given video.

    private fun setClickListener() {
        binding?.toolbar?.btnBack?.setSafeNavigationOnClickListener {
            backPressed()
        }
    }

    private fun backPressed() {
        mainHolderActivity?.showInterstitialAd {
            findNavController().navigateUp()
        }
    }

    private val video: List<Media>
        get() {
            val videoList: MutableList<Media> = ArrayList()
            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME
            )
            val sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC"
            activity?.contentResolver?.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            ).use { cursor ->
                // Cache column indices.
                val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                while (cursor?.moveToNext() == true) {
                    // Get values of columns for a given video.
                    val id = idColumn?.let { cursor.getLong(it) }
                    val name = nameColumn?.let { cursor.getString(it) }
                    val contentUri = id?.let {
                        ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, it
                        )
                    }

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList.add(Media(contentUri, name))
                }
            }
            return videoList
        }// Get values of columns for a given video.

    // Stores column values and the contentUri in a local object
    // that represents the media file.
// Cache column indices.
    private val photos: List<Media>
        get() {
            val photoList: MutableList<Media> = ArrayList()
            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            val sortOrder = MediaStore.Images.Media.DISPLAY_NAME + " ASC"
            activity?.contentResolver?.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            ).use { cursor ->
                // Cache column indices.
                val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                while (cursor?.moveToNext() == true) {
                    // Get values of columns for a given video.
                    val id = idColumn?.let { cursor.getLong(it) }
                    val name = nameColumn?.let { cursor.getString(it) }
                    val contentUri = id?.let {
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it
                        )
                    }

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    photoList.add(Media(contentUri, name))
                }
            }
            return photoList
        }// Get values of columns for a given video.

    // Stores column values and the contentUri in a local object
    // that represents the media file.
// Cache column indices.
    private val audio: List<Media>
        get() {
            val audioList: MutableList<Media> = ArrayList()
            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME
            )
            val sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC"
            activity?.contentResolver?.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            ).use { cursor ->
                // Cache column indices.
                val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                while (cursor?.moveToNext() == true) {
                    // Get values of columns for a given video.
                    val id = idColumn?.let { cursor.getLong(it) }
                    val name = nameColumn?.let { cursor.getString(it) }
                    val contentUri = id?.let {
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it
                        )
                    }

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    audioList.add(Media(contentUri, name))
                }
            }
            return audioList
        }
}