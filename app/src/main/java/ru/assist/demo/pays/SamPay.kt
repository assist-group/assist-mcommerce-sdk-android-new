package ru.assist.demo.pays

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo
import com.samsung.android.sdk.samsungpay.v2.payment.CustomSheetPaymentInfo
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager.CustomSheetTransactionInfoListener
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountBoxControl
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.AmountConstants
import com.samsung.android.sdk.samsungpay.v2.payment.sheet.CustomSheet
import ru.assist.sdk.api.models.AssistPaymentData

abstract class SamPay(private val context: Context) {

    companion object {
        private const val TAG = "SamsungPay"
        private const val serviceId = "c84b694b18674b8f92e598"
        private const val AMOUNT_CONTROL_ID = "amountControlId"
        private const val PRODUCT_ITEM_ID = "productItemId"
        private const val PRODUCT_TAX_ID = "productTaxId"
        private const val PRODUCT_SHIPPING_ID = "productShippingId"
        private const val PRODUCT_FUEL_ID = "productFuelId"
    }

    private val samsungPay: SamsungPay

    var isAvailable = false

    init {
        val bundle = Bundle().apply {
            putString(SpaySdk.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.INAPP_PAYMENT.toString())
        }

        samsungPay = SamsungPay(context, PartnerInfo(serviceId, bundle))
        samsungPay.getSamsungPayStatus(object : StatusListener {
            override fun onSuccess(i: Int, bundle: Bundle) {
                processSamsungPayStatus(i, bundle)
            }

            override fun onFail(i: Int, bundle: Bundle) {
                Log.d(TAG,"checkSamsungPayStatus onFail(): $i")
            }
        })
    }

    private fun processSamsungPayStatus(status: Int, bundle: Bundle) {
        when (status) {
            SpaySdk.SPAY_NOT_SUPPORTED -> Log.w(TAG,"Samsung PAY is not supported")
            SpaySdk.SPAY_NOT_READY -> {
                when (val extraReason = bundle.getInt(SpaySdk.EXTRA_ERROR_REASON)) {
                    SpaySdk.ERROR_SPAY_APP_NEED_TO_UPDATE -> samsungPay.goToUpdatePage()
                    SpaySdk.ERROR_SPAY_SETUP_NOT_COMPLETED -> samsungPay.activateSamsungPay()
                    else -> {
                        Log.e(TAG,"Samsung PAY is not ready, extra reason: $extraReason")
                    }
                }
            }
            SpaySdk.SPAY_READY -> {
                Log.d(TAG,"Samsung PAY is ready")
                isAvailable = true
            }
            else -> Log.w(TAG,"Samsung PAY not expected status")
        }
    }

    fun startSamsungPay(data: AssistPaymentData) {
        if (!isAvailable) return

        try {
            val bundle = Bundle().apply {
                putString(SpaySdk.PARTNER_SERVICE_TYPE, SpaySdk.ServiceType.INAPP_PAYMENT.toString())
            }
            val paymentManager = PaymentManager(context, PartnerInfo(serviceId, bundle))
            val listener = transactionListener
            listener.amount = data.orderAmount!!.toDouble()
            listener.paymentManager = paymentManager
            paymentManager.startInAppPayWithCustomSheet(makeCustomSheetPaymentInfo(data), listener)
        } catch (e: NullPointerException) {
            Toast.makeText(context, "All mandatory fields cannot be null.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            Toast.makeText(context, "IllegalStateException", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Amount values is not valid", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(
                context, "PaymentInfo values not valid or all mandatory fields not set.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    private fun makeCustomSheetPaymentInfo(data: AssistPaymentData): CustomSheetPaymentInfo {
        // If the supported brand is not specified, all card brands in Samsung Pay are
        // listed in the Payment Sheet.
        val brandList = listOf(
            SpaySdk.Brand.VISA,
            SpaySdk.Brand.MASTERCARD
        )

        val amount = data.orderAmount!!.toDouble()
        val merchantId = data.merchantID

        val amountBoxControl = AmountBoxControl(
            AMOUNT_CONTROL_ID,
            data.orderCurrency
        ).apply {
            addItem(PRODUCT_ITEM_ID, "Item", amount, "") //item price
            addItem(PRODUCT_TAX_ID, "Tax", 0.0, "") // sales tax
            addItem(PRODUCT_SHIPPING_ID, "Shipping", 0.0, "") // Shipping fee
            addItem(PRODUCT_FUEL_ID, "Fuel", 0.0, "") // additional item status
            setAmountTotal(amount, AmountConstants.FORMAT_TOTAL_PRICE_ONLY) // grand total
        }

        val customSheet = CustomSheet()
        customSheet.addControl(amountBoxControl)

        return CustomSheetPaymentInfo.Builder()
            .setMerchantId(merchantId)
            .setMerchantName("Sample Merchant")
            .setOrderNumber(data.orderNumber)
            .setPaymentProtocol(CustomSheetPaymentInfo.PaymentProtocol.PROTOCOL_3DS)
            .setAddressInPaymentSheet(CustomSheetPaymentInfo.AddressInPaymentSheet.DO_NOT_SHOW)
            .setAllowedCardBrands(brandList)
            .setCardHolderNameEnabled(true)
            .setRecurringEnabled(false)
            .setCustomSheet(customSheet)
            .build()
    }

    abstract fun doWithToken(result: String)

    private val transactionListener = object : CustomSheetTransactionInfoListener {
        var amount: Double? = null
        var paymentManager: PaymentManager? = null
        // This callback is received when the user changes card on the custom payment sheet in Samsung Pay
        override fun onCardInfoUpdated(selectedCardInfo: CardInfo, customSheet: CustomSheet) {
            /*
             * Called when the user changes card in Samsung Pay.
             * Newly selected cardInfo is passed so merchant app can update transaction amount
             * based on different card (if needed),
             */
            try {
                val amountBoxControl =
                    (customSheet.getSheetControl(AMOUNT_CONTROL_ID) as AmountBoxControl).apply {
                        updateValue(PRODUCT_ITEM_ID, amount!!) //item price
                        updateValue(PRODUCT_TAX_ID, 0.0) // sales tax
                        updateValue(PRODUCT_SHIPPING_ID, 0.0) // Shipping fee
                        updateValue(PRODUCT_FUEL_ID, 0.0, "Pending") // additional item status
                        setAmountTotal(amount!!, AmountConstants.FORMAT_TOTAL_PRICE_ONLY) // grand total
                    }

                customSheet.updateControl(amountBoxControl)
                // Call updateAmount() method. This is mandatory.
                paymentManager?.updateSheet(customSheet)
            } catch (e: java.lang.IllegalStateException) {
                e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
        }

        override fun onSuccess(response: CustomSheetPaymentInfo,
                               paymentCredential: String, extraPaymentData: Bundle) {
            doWithToken(paymentCredential)
        }

        override fun onFailure(errorCode: Int, errorData: Bundle) {
            Toast.makeText(context, "Transaction : onFailure : $errorCode", Toast.LENGTH_LONG).show()
        }
    }
}