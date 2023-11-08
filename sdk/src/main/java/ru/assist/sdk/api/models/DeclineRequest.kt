package ru.assist.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeclineRequest(
    val login: String?,
    val password: String?,
    @SerializedName("ordernumber") val orderNumber: String?,
    @SerializedName("merchant_id") val merchantId: String?
) : Parcelable