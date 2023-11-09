package ru.assist.sdk.models

data class Configuration(
    var apiURL: String? = null,
    var link: String? = null,
    val storageEnabled: Boolean = true
)