package ru.assist.sdk.api.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Root

@Root(name = "soapenv:Envelope", strict = false)
@NamespaceList(
    Namespace(prefix = "soapenv", reference = "http://schemas.xmlsoap.org/soap/envelope/"),
    Namespace(prefix = "ws", reference = "http://www.paysecure.ru/ws/")
)
open class ResultResponse(
    @field:Element(name = "Body", required = false)
    open var body: Body? = null
) {
    @Root(name = "Body", strict = false)
    open class Body(
        @field:Element(name = "orderresultResponse", required = false)
        var orderResultResponse: OrderResultResponse? = null,
        @field:Element(name = "Fault", required = false)
        var fault: Fault? = null
    )
    @Root(name = "orderresultResponse", strict = false)
    open class OrderResultResponse(
        @field:Element(name = "orderresult", required = false)
        var orderResult: OrderResult? = null
    )
    @Root(name = "orderresult", strict = false)
    open class OrderResult(
        @field:ElementList(entry = "order", inline = true)
        @param:ElementList(entry = "order", inline = true)
        val orders: List<Order>
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
        @field:Element(name = "merchantId", required = false)
        var merchantId: String? = null,
        @field:Element(name = "email", required = false)
        var email: String? = null,
        @field:Element(name = "orderstate", required = false)
        var orderState: String? = null,
        @field:Element(name = "orderdate", required = false)
        var orderDate: String? = null,
        @field:Element(name = "packetdate", required = false)
        var packetDate: String? = null,
        @field:Element(name = "signature", required = false)
        var signature: String? = null,
        @field:Element(name = "checkvalue", required = false)
        var checkValue: String? = null,
        @field:ElementList(entry = "operation", type = Operation::class, inline = true)
        var operations: List<Operation>? = null
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
        @field:Element(name = "emv_data", required = false)
        var emvData: String? = null,
        @field:Element(name = "transactionid", required = false)
        var transactionId: String? = null,
        @field:Element(name = "rrn", required = false)
        var rrn: String? = null,
        @field:Element(name = "merchantname", required = false)
        var merchantName: String? = null,
        @field:Element(name = "tid", required = false)
        var tid: String? = null,
        @field:Element(name = "emvaid", required = false)
        var emvaid: String? = null,
        @field:Element(name = "emvapp", required = false)
        var emvapp: String? = null,
        @field:Element(name = "emvtsi", required = false)
        var emvtsi: String? = null,
        @field:Element(name = "emvtvr", required = false)
        var emvtvr: String? = null,
        @field:Element(name = "emvac", required = false)
        var emvac: String? = null,
        @field:Element(name = "emvactype", required = false)
        var emvactype: String? = null,
        @field:Element(name = "paymentmodename", required = false)
        var paymentModeName: String? = null,
        @field:Element(name = "link_billnumber", required = false)
        var linkBillNumber: String? = null,
        @field:Element(name = "chequeitems", required = false)
        var chequeItems: String? = null
    )
}