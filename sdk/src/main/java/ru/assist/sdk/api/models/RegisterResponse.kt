package ru.assist.sdk.api.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Root(name = "SOAP-ENV:Envelope", strict = false)
@NamespaceList(
    Namespace(prefix = "SOAP-ENV", reference = "http://schemas.xmlsoap.org/soap/envelope/"),
    Namespace(prefix = "xsi", reference = "http://www.w3.org/2001/XMLSchema-instance"),
    Namespace(prefix = "xsd", reference = "http://www.w3.org/2001/XMLSchema")
)
open class RegisterResponse(
    @field:Element(name = "Body", required = false)
    open var body: Body? = null
) {

    @Root(name = "Body", strict = false)
    open class Body(
        @field:Element(name = "getRegistrationResponse", required = false)
        var getRegistration: GetRegistration? = null,
        @field:Element(name = "Fault", required = false)
        var fault: Fault? = null
    )

    @Root(name = "getRegistrationResponse")
    open class GetRegistration(
        @field:Element(name = "registration_id", required = false)
        var regId: String? = null
    )
}