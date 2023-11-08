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
open class TokenPayResponse(
    @field:Element(name = "Body", required = false)
    open var body: Body? = null
) {
    @Root(name = "Body", strict = false)
    open class Body(
        @field:Element(name = "PaymentTokenResponseParams", required = false)
        var paymentTokenResponseParams: PaymentTokenResponseParams? = null,
        @field:Element(name = "Fault", required = false)
        var fault: Fault? = null
    )
    @Root(name = "PaymentTokenResponseParams", strict = false)
    open class PaymentTokenResponseParams(
        @field:Element(name = "order", required = false)
        var order: Order? = null,
        @field:Element(name = "packetdate", required = false)
        var packetDate: String? = null,
        @field:Element(name = "signature", required = false)
        var signature: String? = null
    )
    @Root(name = "order", strict = false)
    open class Order(
        @field:Element(name = "ordernumber", required = false)
        var orderNumber: String? = null,
        @field:Element(name = "billnumber", required = false)
        var billNumber: String? = null,
        @field:Element(name = "testmode", required = false)
        var testMode: String? = null,
        @field:Element(name = "ordercomment", required = false)
        var orderComment: String? = null,
        @field:Element(name = "orderamount", required = false)
        var orderAmount: String? = null,
        @field:Element(name = "ordercurrency", required = false)
        var orderCurrency: String? = null,
        @field:Element(name = "firstname", required = false)
        var firstName: String? = null,
        @field:Element(name = "lastname", required = false)
        var lastName: String? = null,
        @field:Element(name = "middlename", required = false)
        var middleName: String? = null,
        @field:Element(name = "email", required = false)
        var email: String? = null,
        @field:Element(name = "orderstate", required = false)
        var orderState: String? = null,
        @field:Element(name = "orderdate", required = false)
        var orderDate: String? = null,
        @field:Element(name = "fraud_state", required = false)
        var fraudState: String? = null,
        @field:Element(name = "fraud_reason", required = false)
        var fraudReason: String? = null,
        @field:Element(name = "checkvalue", required = false)
        var checkValue: String? = null,
        @field:Element(name = "order_id", required = false)
        var orderId: String? = null,
        @field:Element(name = "operation", required = false)
        var operation: Operation? = null
    )

    @Root(name = "operation", strict = false)
    open class Operation(
        @field:Element(name = "billnumber", required = false)
        var billNumber: String? = null,
        @field:Element(name = "operationtype", required = false)
        var operationType: String? = null,
        @field:Element(name = "operationstate", required = false)
        var operationState: String? = null,
        @field:Element(name = "amount", required = false)
        var amount: String? = null,
        @field:Element(name = "currency", required = false)
        var currency: String? = null,
        @field:Element(name = "clientip", required = false)
        var clientIP: String? = null,
        @field:Element(name = "ipaddress", required = false)
        var ipAddress: String? = null,
        @field:Element(name = "meantype_id", required = false)
        var meanTypeId: String? = null,
        @field:Element(name = "meantypename", required = false)
        var meanTypeName: String? = null,
        @field:Element(name = "meansubtype", required = false)
        var meanSubtype: String? = null,
        @field:Element(name = "meannumber", required = false)
        var meanNumber: String? = null,
        @field:Element(name = "cardholder", required = false)
        var cardholder: String? = null,
        @field:Element(name = "cardexpirationdate", required = false)
        var cardExpirationDate: String? = null,
        @field:Element(name = "issuebank", required = false)
        var issueBank: String? = null,
        @field:Element(name = "bankcountry", required = false)
        var bankCountry: String? = null,
        @field:Element(name = "responsecode", required = false)
        var responseCode: String? = null,
        @field:Element(name = "message", required = false)
        var message: String? = null,
        @field:Element(name = "customermessage", required = false)
        var customerMessage: String? = null,
        @field:Element(name = "recommendation", required = false)
        var recommendation: String? = null,
        @field:Element(name = "approvalcode", required = false)
        var approvalCode: String? = null,
        @field:Element(name = "protocoltypename", required = false)
        var protocolTypeName: String? = null,
        @field:Element(name = "processingname", required = false)
        var processingName: String? = null,
        @field:Element(name = "operationdate", required = false)
        var operationDate: String? = null,
        @field:Element(name = "authresult", required = false)
        var authResult: String? = null,
        @field:Element(name = "authrequired", required = false)
        var authRequired: String? = null,
        @field:Element(name = "slipno", required = false)
        var slipno: String? = null
    )
}