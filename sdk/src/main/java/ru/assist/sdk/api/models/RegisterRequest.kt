package ru.assist.sdk.api.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Root(name = "soapenv:Envelope", strict = false)
@NamespaceList(
    Namespace(prefix = "soapenv", reference = "http://schemas.xmlsoap.org/soap/envelope/")
)
class RegisterRequest(appName: String?, appVersion: String?, deviceUniqueId: String?) {
    @field:Element(name = "soapenv:Body", required = false)
    var body: Body? = null

    init {
        body = Body().apply {
            getRegistration = GetRegistration().apply {
                this.appName = appName
                this.appVersion = appVersion
                this.deviceUniqueId = deviceUniqueId
            }
        }
    }

    @Root(name = "soapenv:Body", strict = false)
    class Body {
        @field:Element(name = "getRegistration", required = false)
        var getRegistration: GetRegistration? = null
    }

    @Root(name = "getRegistration")
    @Namespace(prefix = "ns1", reference = "http://www.paysecure.ru/ws/")
    class GetRegistration {
        @field:Element(name = "ApplicationName", required = false)
        var appName: String? = null
        @field:Element(name = "ApplicationVersion", required = false)
        var appVersion: String? = null
        @field:Element(name = "DeviceUniqueId", required = false)
        var deviceUniqueId: String? = null
    }
}