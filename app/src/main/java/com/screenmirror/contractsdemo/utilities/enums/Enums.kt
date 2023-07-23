package com.screenmirror.contractsdemo.utilities.enums

object Enums {

    enum class MediaType(val value: String,val number: Int) {
        Audio("audio", 2),
        Video("video", 1),
        Photo("photo", 0)
    }

    enum class BundleValues(val value: String) {
        MediaType("mediaType"),
        Link("link")
    }
}