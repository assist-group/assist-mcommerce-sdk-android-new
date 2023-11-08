package ru.assist.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthRequest(
    @SerializedName("login") val login: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("audience") val audience: String = "POS",
    @SerializedName("components") val components: String = "ws/cancelbynumber"
) : Parcelable