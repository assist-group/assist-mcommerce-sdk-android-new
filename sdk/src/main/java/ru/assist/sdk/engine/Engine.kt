package ru.assist.sdk.engine

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.api.AssistApi
import ru.assist.sdk.api.RetrofitCallBack
import ru.assist.sdk.api.models.AssistPaymentData
import ru.assist.sdk.api.models.DeclineRequest
import ru.assist.sdk.api.models.DeclineResponse
import ru.assist.sdk.api.models.RegisterRequest
import ru.assist.sdk.api.models.RegisterResponse
import ru.assist.sdk.api.models.ResultRequest
import ru.assist.sdk.api.models.ResultResponse
import ru.assist.sdk.api.models.TokenPayResponse
import ru.assist.sdk.exception.AssistSdkException
import ru.assist.sdk.identification.InstallationInfo
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.models.Configuration
import ru.assist.sdk.models.OrderState
import ru.assist.sdk.models.PaymentTokenType
import ru.assist.sdk.scanner.CardScanner
import ru.assist.sdk.storage.OrderDao
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.IllegalArgumentException
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

internal object Engine {
    private const val TAG = "AssistEngine"

    private lateinit var api: AssistApi
    private lateinit var storage: OrderDao
    private lateinit var instInfo: InstallationInfo

    private lateinit var configuration: Configuration

    var webProcessor: WebProcessor? = null

    fun init(api: AssistApi, storage: OrderDao, instInfo: InstallationInfo): Engine {
        this.api = api
        this.storage = storage
        this.instInfo = instInfo
        return this
    }

    fun configure(configuration: Configuration) {
        this.configuration = configuration
    }

    fun payWeb(
        context: AppCompatActivity,
        data: AssistPaymentData,
        scanner: CardScanner?,
        result: (AssistResult) -> Unit,
        allowRedirect: Boolean
    ) =
        checkRegistration(
            context,
            result,
            doAfter = {
                val link = configuration.link

                val request = ResultRequest(
                    deviceId = instInfo.getDeviceUniqueId(),
                    regId = instInfo.getAppRegId(),
                    merchantId = data.merchantID
                )

                if (link.isNullOrBlank()) {
                    data.mobileDevice = "5"
                    data.device = "AssistCommerceSDK"
                    data.deviceUniqueID = instInfo.getDeviceUniqueId()
                    data.applicationName = instInfo.appName()
                    data.applicationVersion = instInfo.versionName()
                    data.assistSDKVersion = AssistSDK.sdkVersion

                    val url = "${configuration.apiURL}/pay/order.cfm"
                    val content = mapToWebPay(data, instInfo.getAppRegId())
                    webProcessor =
                        WebProcessor(context, url, content, scanner, request, ::getOrderResult, result, allowRedirect)
                } else {
                    webProcessor =
                        WebProcessor(context, checkLink(link), "", scanner, request, ::getOrderResult, result, allowRedirect)
                }
            }
        )

    fun payToken(
        context: AppCompatActivity,
        data: AssistPaymentData,
        token: String,
        type: PaymentTokenType,
        result: (AssistResult) -> Unit
    ) =
        checkRegistration(
            context,
            result,
            doAfter = {
                val link = configuration.link
                val call = if (link.isNullOrBlank()) {
                    val tokenPayMap = mapToTokenPay(data, token, type)
                    api.payToken(tokenPayMap)
                } else {
                    checkLink(link)
                    val tokenPayMap = mapToTokenLinkPay(data, getCFSIDFromLink(link), token, type)
                    api.payTokenLink(tokenPayMap)
                }
                RetrofitCallBack<TokenPayResponse>()
                    .onSuccess { response ->
                        Log.d("AssistSDK", "response=$response")
                        val request = ResultRequest(
                            deviceId = instInfo.getDeviceUniqueId(),
                            regId = instInfo.getAppRegId(),
                            merchantId = data.merchantID,
                            rOrderNumber = response.body?.paymentTokenResponseParams?.order?.orderNumber,
                            rDate = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(System.currentTimeMillis())
                        )
                        getOrderResult(context, request, result)
                    }
                    .onError { error ->
                        Log.d(TAG, "error=$error")
                        result(AssistResult("Token pay error: $error"))
                    }
                    .call(context, call)
            }
        )

    @Throws(AssistSdkException::class)
    private fun getCFSIDFromLink(link: String): String {
        val cfsid =
            URLDecoder.decode(link.split("CFSID=".toRegex())[1].split("&".toRegex())[0], "UTF-8")
        if (cfsid.matches("[\\w+/=]+".toRegex())) {
            return cfsid
        }
        Log.e(TAG, "Link parsing error. Check your CFSID")
        throw AssistSdkException("Link parsing error. Check your CFSID")
    }

    fun declineByNumber(
        context: AppCompatActivity,
        data: AssistPaymentData,
        result: (AssistResult) -> Unit
    ) =
        checkRegistration(
            context,
            result,
            doAfter = {
                val decRequest = DeclineRequest(
                    login = data.login,
                    password = data.password,
                    orderNumber = data.orderNumber,
                    merchantId = data.merchantID
                )
                api.declineByNumber(context, decRequest, response = {
                    RetrofitCallBack<DeclineResponse>()
                        .onSuccess { response ->
                            Log.d("AssistSDK", "response=$response")
                            val resRequest = ResultRequest(
                                deviceId = instInfo.getDeviceUniqueId(),
                                regId = instInfo.getAppRegId(),
                                merchantId = data.merchantID,
                                rOrderNumber = data.orderNumber,
                                rDate = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(System.currentTimeMillis())
                            )
                            getOrderResult(context, resRequest, result)
                        }
                        .onError { error ->
                            Log.d(TAG, "error=$error")
                            result(AssistResult("Decline by number error: $error"))
                        }
                        .call(context, it)
                }, result)
            }
        )

    fun getOrderDataByLink(link: String, result: (AssistResult) -> Unit) =
        api.getAddress("$link&type=json").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.raw().request.url.toString().contains("pay.cfm")) {
                    val bytes = response.body()?.bytes()
                    if (bytes == null) {
                        result(AssistResult("Empty response"))
                    } else {
                        val html = bytes.inputStream().bufferedReader().use { it.readText() }
                        result(getOrderFromHtml(html))
                    }
                } else {
                    result(AssistResult("Error getting order data"))
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                result(AssistResult(t.message))
            }
        })

    fun getOrderDataByNumber(context: AppCompatActivity, order: AssistResult, result: (AssistResult) -> Unit) =
        checkRegistration(
            context,
            result,
            doAfter = {
                val millis = order.result?.dateMillis ?: System.currentTimeMillis()
                getOrderResult(
                    context,
                    ResultRequest(
                        deviceId = instInfo.getDeviceUniqueId(),
                        regId = instInfo.getAppRegId(),
                        merchantId = order.result?.merchantId,
                        rOrderNumber = order.result?.orderNumber,
                        rDate = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(millis)
                    ),
                    result
                )
            }
        )

    suspend fun getOrdersFromStorage() =
        if (configuration.storageEnabled)
            storage.getOrders()
        else
            throw AssistSdkException("Storage is disabled")

    suspend fun deleteOrderInStorage(order: AssistResult) =
        if (configuration.storageEnabled)
            storage.deleteOrder(order)
        else
            throw AssistSdkException("Storage is disabled")

    private fun getOrderFromHtml(json: String): AssistResult {
        val obj = JSONObject(json)
        val order = obj.getJSONObject("order")
        val operation = obj.getJSONObject("operation")
        return AssistResult(
            merchantId = obj.getJSONObject("merchant").getString("id"),
            orderState = OrderState.fromString(order.getString("statename")),
            approvalCode = "",
            billNumber = order.getString("billnumber"),
            extraInfo = "",
            orderNumber = order.getString("ordernumber"),
            amount = operation.getString("amount"),
            currency = operation.getString("currencycode"),
            comment = order.getString("comment"),
            email = "",
            firstName = "",
            lastName = "",
            middleName = "",
            signature = "",
            checkValue = "",
            meanTypeName = operation.getString("meantype"),
            meanNumber = operation.getString("meannumber"),
            cardholder = "",
            cardExpirationDate = "",
            chequeItems = ""
        )
    }

    private fun getOrderResult(context: AppCompatActivity, request: ResultRequest, result: (AssistResult) -> Unit) {
        RetrofitCallBack<ResultResponse>()
            .onSuccess { response ->
                val body = response.body
                if (body?.orderResultResponse != null) {
                    body.orderResultResponse?.orderResult?.orders?.get(0)?.let {
                        val operation = it.operations?.last()
                        val order = AssistResult(
                            merchantId = request.body?.orderresult?.merchantId,
                            orderState = OrderState.fromString(it.orderState),
                            approvalCode = operation?.approvalCode,
                            billNumber = it.billNumber,
                            extraInfo = operation?.customerMessage,
                            orderNumber = it.orderNumber,
                            amount = it.orderAmount,
                            currency = it.orderCurrency,
                            comment = it.orderComment,
                            email = it.email,
                            firstName = it.firstName,
                            lastName = it.lastName,
                            middleName = it.middleName,
                            signature = it.signature,
                            checkValue = it.checkValue,
                            meanTypeName = operation?.meanTypeName,
                            meanNumber = operation?.meanNumber,
                            cardholder = operation?.cardholder,
                            cardExpirationDate = operation?.cardExpirationDate,
                            chequeItems = operation?.chequeItems
                        )
                        if (configuration.storageEnabled) {
                            context.lifecycleScope.launch {
                                storage.addOrUpdateOrder(order)
                            }
                        }
                        result(order)
                    }
                } else {
                    body?.fault?.let {
                        result(AssistResult("${it.faultCode} ${it.faultString}"))
                    }
                }
            }
            .onError {
                result(AssistResult(it))
            }
            .call(context, api.getOrderResult(request))
    }

    private fun checkRegistration(
        context: Context,
        result: (AssistResult) -> Unit,
        doAfter: () -> Unit
    ) =
        if (instInfo.getAppRegId() == null) {
            val deviceId = InstallationInfo.generateDeviceId()

            val regRequest = RegisterRequest(
                appName = instInfo.appName(),
                appVersion = instInfo.versionName(),
                deviceUniqueId = deviceId
            )

            RetrofitCallBack<RegisterResponse>()
                .onSuccess { response ->
                    response.body?.getRegistration?.regId?.let {
                        instInfo.setAppRegID(deviceId, it)
                        doAfter()
                    }
                    response.body?.fault?.let {
                        result(AssistResult("${it.faultCode} ${it.faultString}"))
                    }
                }
                .onError {
                    Log.d(TAG, "error=$it")
                    result(AssistResult("Registration error: $it"))
                }
                .call(context, api.register(regRequest))
        } else {
            doAfter()
        }

    @Throws(IllegalArgumentException::class)
    private fun checkLink(link: String): String {
        val apiURL = configuration.apiURL
        if (apiURL == null || !Patterns.WEB_URL.matcher(apiURL).matches()) {
            throw IllegalArgumentException("API URL is not valid")
        }
        if (!Patterns.WEB_URL.matcher(link).matches()
            || !link.startsWith(apiURL)
            || !link.contains("CFSID=", true)) {
            throw IllegalArgumentException("Link is not valid")
        }
        return link
    }

    private fun mapToWebPay(data: AssistPaymentData, regId: String?): String {
        return """
            merchant_id=${urlEncode(data.merchantID)}&
            login=${urlEncode(data.login)}&
            password=${urlEncode(data.password)}&
            OrderNumber=${urlEncode(data.orderNumber)}&
            OrderAmount=${urlEncode(data.orderAmount)}&
            OrderCurrency=${urlEncode(data.orderCurrency)}&
            OrderComment=${urlEncode(data.orderComment)}&
            language=${urlEncode(data.language)}&
            chequeItems=${urlEncode(data.chequeItems)}&
            customerNumber=${urlEncode(data.customerNumber)}&
            signature=${urlEncode(data.signature)}&
            lastname=${urlEncode(data.lastname)}&
            firstname=${urlEncode(data.firstname)}&
            middlename=${urlEncode(data.middlename)}&
            email=${urlEncode(data.email)}&
            address=${urlEncode(data.address)}&
            homePhone=${urlEncode(data.homePhone)}&
            workPhone=${urlEncode(data.workPhone)}&
            mobilePhone=${urlEncode(data.mobilePhone)}&
            fax=${urlEncode(data.fax)}&
            country=${urlEncode(data.country)}&
            state=${urlEncode(data.state)}&
            city=${urlEncode(data.city)}&
            zip=${urlEncode(data.zip)}&
            taxpayerID=${urlEncode(data.taxpayerID)}&
            customerDocID=${urlEncode(data.customerDocID)}&
            paymentAddress=${urlEncode(data.paymentAddress)}&
            paymentPlace=${urlEncode(data.paymentPlace)}&
            cashier=${urlEncode(data.cashier)}&
            cashierINN=${urlEncode(data.cashierINN)}&
            paymentTerminal=${urlEncode(data.paymentTerminal)}&
            transferOperatorPhone=${urlEncode(data.transferOperatorPhone)}&
            transferOperatorName=${urlEncode(data.transferOperatorName)}&
            transferOperatorAddress=${urlEncode(data.transferOperatorAddress)}&
            transferOperatorINN=${urlEncode(data.transferOperatorINN)}&
            paymentReceiverOperatorPhone=${urlEncode(data.paymentReceiverOperatorPhone)}&
            paymentAgentPhone=${urlEncode(data.paymentAgentPhone)}&
            paymentAgentOperation=${urlEncode(data.paymentAgentOperation)}&
            supplierPhone=${urlEncode(data.supplierPhone)}&
            paymentAgentMode=${urlEncode(data.paymentAgentMode)}&
            documentRequisite=${urlEncode(data.documentRequisite)}&
            userRequisites=${urlEncode(data.userRequisites)}&
            ymPayment=${urlEncode(data.ymPayment)}&
            wmPayment=${urlEncode(data.wmPayment)}&
            qiwiPayment=${urlEncode(data.qiwiPayment)}&
            qiwiMtsPayment=${urlEncode(data.qiwiMtsPayment)}&
            qiwiMegafonPayment=${urlEncode(data.qiwiMegafonPayment)}&
            qiwiBeelinePayment=${urlEncode(data.qiwiBeelinePayment)}&
            fastPayPayment=${urlEncode(data.fastPayPayment)}&
            generateReceipt=${urlEncode(data.generateReceipt)}&
            receiptLine=${urlEncode(data.receiptLine)}&
            tax=${urlEncode(data.tax)}&
            fpmode=${urlEncode(data.fpmode)}&
            taxationSystem=${urlEncode(data.taxationSystem)}&
            prepayment=${urlEncode(data.prepayment)}&
            registration_id=${urlEncode(regId)}&
            device=${urlEncode(data.device)}&
            deviceUniqueID=${urlEncode(data.deviceUniqueID)}&
            mobileDevice=${urlEncode(data.mobileDevice)}&
            applicationName=${urlEncode(data.applicationName)}&
            applicationVersion=${urlEncode(data.applicationVersion)}&
            assistSDKVersion=${urlEncode(data.assistSDKVersion)}&
            urlReturnOk=${urlEncode(data.urlReturnOk)}&
            urlReturnNo=${urlEncode(data.urlReturnNo)}
        """.replace("[\\s\\n\\t]".toRegex(), "").replace("[^=&]+=&".toRegex(), "")
    }

    private fun urlEncode(param: String?): String =
        param?.let { URLEncoder.encode(it, "UTF-8") } ?: ""

    private fun mapToTokenPay(
        data: AssistPaymentData,
        token: String,
        type: PaymentTokenType
    ): HashMap<String, String?> =
        hashMapOf(
            "merchant_id" to data.merchantID,
            "Login" to data.login,
            "Password" to data.password,
            "OrderNumber" to data.orderNumber,
            "OrderComment" to data.orderComment,
            "OrderAmount" to data.orderAmount,
            "OrderCurrency" to data.orderCurrency,
            "Lastname" to data.lastname,
            "Firstname" to data.firstname,
            "Email" to data.email,
            "PaymentToken" to token,
            "TokenType" to type.toString(),
            "Format" to "4"
        )

    private fun mapToTokenLinkPay(
        data: AssistPaymentData,
        cfsid: String,
        token: String,
        type: PaymentTokenType
    ): HashMap<String, String?> =
        hashMapOf(
            "merchant_id" to data.merchantID,
            "Login" to data.login,
            "Password" to data.password,
            "outcfsid" to cfsid,
            "PaymentToken" to token,
            "TokenType" to type.toString(),
            "Format" to "4"
        )
}