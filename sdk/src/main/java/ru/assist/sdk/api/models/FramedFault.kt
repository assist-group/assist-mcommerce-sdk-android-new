package ru.assist.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Parcelize
@Root(name = "Fault", strict = false)
open class FramedFault(
    @SerializedName("fault")
    @field:Element(name = "fault", required = false)
    val fault: Fault
) : Parcelable