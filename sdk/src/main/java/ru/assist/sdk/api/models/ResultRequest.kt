package ru.assist.sdk.api.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Root(name = "soapenv:Envelope", strict = false)
@NamespaceList(
    Namespace(prefix = "soapenv", reference = "http://schemas.xmlsoap.org/soap/envelope/"),
    Namespace(prefix = "ws", reference = "http://www.paysecure.ru/ws/")
)
class ResultRequest(
    deviceId: String?,
    regId: String?,
    merchantId: String?,
    private var rDate: String? = null,
    private var rOrderNumber: String? = null
) {
    @field:Element(name = "soapenv:Body", required = false)
    var body: Body? = null

    init {
        body = Body().apply {
            orderresult = OrderResult().apply {
                this.deviceId = deviceId
                this.regId = regId
                this.merchantId = merchantId
                this.date = rDate
                this.orderNumber = rOrderNumber
            }
        }
    }

    fun setOrderNumber(orderNumber: String?) {
        body?.orderresult?.orderNumber = orderNumber
    }

    fun setDate(date: String?) {
        body?.orderresult?.date = date
    }

    @Root(name = "soapenv:Body", strict = false)
    class Body {
        @field:Element(name = "ws:orderresult", required = false)
        var orderresult: OrderResult? = null
    }

    @Root(name = "ws:orderresult")
    class OrderResult {
        @field:Element(name = "device_id", required = false)
        var deviceId: String? = null
        @field:Element(name = "registration_id", required = false)
        var regId: String? = null
        @field:Element(name = "ordernumber", required = false)
        var orderNumber: String? = null
        @field:Element(name = "merchant_id", required = false)
        var merchantId: String? = null
        @field:Element(name = "date", required = false)
        var date: String? = null
    }
}