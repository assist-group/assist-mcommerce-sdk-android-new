package ru.assist.sdk.api.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Parcelize
@Root(name = "Fault", strict = false)
open class Fault(
    @SerializedName("faultcode")
    @field:Element(name = "faultcode", required = false)
    var faultCode: String? = null,
    @SerializedName("faultstring")
    @field:Element(name = "faultstring", required = false)
    var faultString: String? = null,
    @SerializedName("detail")
    @field:Element(name = "detail", required = false)
    var detail: Detail? = null
) : Parcelable {
    @Parcelize
    @Root(name = "detail", strict = false)
    open class Detail(
        @SerializedName("message")
        @field:Element(name = "message", required = false)
        var message: String? = null,
        @SerializedName("recomendation")
        @field:Element(name = "recomendation", required = false)
        var recomendation: String? = null
    ) : Parcelable
}