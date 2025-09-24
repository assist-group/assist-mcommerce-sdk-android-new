package ru.assist.demo.pays

import android.app.Activity
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Optional

class GooglePay(
    context: Activity,
    env: Int,
    googlePayMerchantId: String,
    isReady: (Boolean) -> Unit
) {
    companion object {
        private const val TAG = "GooglePay"

        private const val googlePayGateway = "assist"
        var env: Int? = null
        var googlePayMerchantId: String? = null

        /**
         * Create a Google Pay API base request object with properties used in all requests
         *
         * @return Google Pay API base request object
         * @throws JSONException
         */
        @Throws(JSONException::class)
        private fun getBaseRequest(): JSONObject =
            JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0)

        /**
         * Identify your gateway and your app's gateway merchant identifier
         *
         * The Google Pay API response will return an encrypted payment method capable of being charged
         * by a supported gateway after payer authorization
         *
         * @return payment data tokenization for the CARD payment method
         * @throws JSONException
         */
        @Throws(JSONException::class)
        private fun getTokenizationSpecification(gatewayMerchantId: String): JSONObject =
            JSONObject()
                .put("type", "PAYMENT_GATEWAY")
                .put(
                    "parameters",
                    JSONObject()
                        .put("gateway", googlePayGateway)
                        .put("gatewayMerchantId", gatewayMerchantId)
                )

        /**
         * Card networks supported by your app and your gateway
         *
         * @return allowed card networks
         */
        private fun getAllowedCardNetworks(): JSONArray =
            JSONArray(listOf(
                "MASTERCARD",
                "VISA",
                "MIR"
            ))

        /**
         * Card authentication methods supported by your app and your gateway
         *
         * @return allowed card authentication methods
         */
        private fun getAllowedCardAuthMethods(): JSONArray =
            JSONArray(listOf(
                "CRYPTOGRAM_3DS",
//                "PAN_ONLY"
            ))

        /**
         * Describe your app's support for the CARD payment method
         *
         * The provided properties are applicable to both an IsReadyToPayRequest and a
         * PaymentDataRequest
         *
         * @return a CARD PaymentMethod object describing accepted cards
         * @throws JSONException
         */
        @Throws(JSONException::class)
        private fun getBaseCardPaymentMethod(): JSONObject =
            JSONObject()
                .put("type", "CARD")
                .put(
                    "parameters",
                    JSONObject()
                        .put("allowedAuthMethods", getAllowedCardAuthMethods())
                        .put("allowedCardNetworks", getAllowedCardNetworks())
                )

        /**
         * Describe the expected returned payment data for the CARD payment method
         *
         * @return a CARD PaymentMethod describing accepted cards and optional fields
         * @throws JSONException
         */
        @Throws(JSONException::class)
        private fun getCardPaymentMethod(gatewayMerchantId: String): JSONObject =
            getBaseCardPaymentMethod()
                .put(
                    "tokenizationSpecification",
                    getTokenizationSpecification(gatewayMerchantId)
                )

        /**
         * Provide Google Pay API with a payment amount, currency, and amount status
         *
         * @return information about the requested payment
         * @throws JSONException
         */
        @Throws(JSONException::class)
        private fun getTransactionInfo(amount: String, currency: String): JSONObject =
            JSONObject()
                .put("totalPrice", amount)
                .put("totalPriceStatus", "FINAL")
                .put("currencyCode", currency)

        /**
         * Information about the merchant requesting payment information
         *
         * @return information about the merchant
         * @throws JSONException
         */
        @Throws(JSONException::class)
        private fun getMerchantInfo(merchantName: String): JSONObject =
            JSONObject().apply {
                if (env == WalletConstants.ENVIRONMENT_PRODUCTION && !googlePayMerchantId.isNullOrBlank()) {
                    put("merchantId", googlePayMerchantId)
                }
                put("merchantName", merchantName)
            }

        /**
         * An object describing accepted forms of payment by your app, used to determine a viewer's
         * readiness to pay
         *
         * @return API version and payment methods supported by the app
         */
        private fun getIsReadyToPayRequest(): JSONObject? =
            try {
                getBaseRequest()
                    .put(
                        "allowedPaymentMethods",
                        JSONArray().put(getBaseCardPaymentMethod())
                    )
            } catch (e: JSONException) {
                null
            }

        /**
         * An object describing information requested in a Google Pay payment sheet
         *
         * @return payment data expected by your app
         */
        private fun getPaymentDataRequest(
            merchantName: String,
            gatewayMerchantId: String,
            amount: String,
            currency: String
        ): Optional<JSONObject> =
            try {
                val paymentDataRequest = getBaseRequest()
                    .put(
                        "allowedPaymentMethods",
                        JSONArray().put(getCardPaymentMethod(gatewayMerchantId))
                    )
                    .put("transactionInfo", getTransactionInfo(amount, currency))
                    .put("merchantInfo", getMerchantInfo(merchantName))

                Optional.of(paymentDataRequest)
            } catch (e: JSONException) {
                Optional.empty()
            }
    }

    private val paymentsClient: PaymentsClient

    init {
        GooglePay.env = env
        GooglePay.googlePayMerchantId = googlePayMerchantId

        paymentsClient = Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(env)
                .build()
        )

        getIsReadyToPayRequest().let { isReadyToPayJson ->
            val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
            val task = paymentsClient.isReadyToPay(request)
            task.addOnCompleteListener {
                try {
                    isReady(it.getResult(ApiException::class.java))
                } catch (exception: ApiException) {
                    Log.e(TAG, "Checking GooglePay feature error", exception)
                }
            }
        }
    }

    fun pay(merchantId: String, amount: String, currency: String, callback: (Task<PaymentData>) -> Unit) {
        val paymentDataRequestJson = getPaymentDataRequest(
            "Example Merchant", merchantId, amount, currency
        )
        if (!paymentDataRequestJson.isPresent) {
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString())
        callback(paymentsClient.loadPaymentData(request))
    }
}