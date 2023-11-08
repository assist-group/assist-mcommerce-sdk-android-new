package ru.assist.sdk.models

data class Configuration(
    var apiURL: String? = null,
    var link: String? = null,
    var useCamera: Boolean = false,
    val storageEnabled: Boolean = true
)