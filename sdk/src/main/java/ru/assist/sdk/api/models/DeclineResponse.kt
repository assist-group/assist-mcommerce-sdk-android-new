package ru.assist.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element

@Parcelize
data class DeclineResponse(
    @SerializedName("cancel") var cancel: Cancel? = null,
    @SerializedName("fault") var fault: Fault? = null
) : Parcelable {
    @Parcelize
    data class Cancel(
        @SerializedName("orderstate") var orderstate: String? = null
    ) : Parcelable
}