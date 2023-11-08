package ru.assist.sdk

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import ru.assist.sdk.api.models.AssistPaymentData
import ru.assist.sdk.dagger2.AssistComponent
import ru.assist.sdk.dagger2.DaggerAssistComponent
import ru.assist.sdk.engine.Engine
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.identification.InstallationInfo
import ru.assist.sdk.models.Configuration
import ru.assist.sdk.models.PaymentTokenType
import ru.assist.sdk.scanner.CardScanner
import ru.assist.sdk.ui.PayActivity
import javax.inject.Inject

class AssistSDK {
    companion object {
        private const val baseApiUrl = "https://payments.t.paysecure.ru/"

        private var instance: AssistSDK? = null

        fun getInstance(): AssistSDK =
            instance ?: synchronized(this) {
                instance ?: AssistSDK().also { instance = it }
            }

        val sdkVersion by lazy { BuildConfig.VERSION_NAME }
    }

    @Inject
    internal lateinit var engine: Engine
    internal lateinit var component: AssistComponent

    val sdkVersion by lazy { BuildConfig.VERSION_NAME }

    fun clearRegistration() = InstallationInfo.clearAppRegID()

    fun configure(context: Context, configuration: Configuration): AssistSDK {
        if (configuration.apiURL == null) configuration.apiURL = baseApiUrl

        component = DaggerAssistComponent.builder()
            .application(context.applicationContext as Application)
            .apiUrl(configuration.apiURL!!)
            .storageEnabled(configuration.storageEnabled)
            .build()
        component.inject(this)

        engine.configure(configuration)

        return this
    }

    fun payWeb(
        context: AppCompatActivity,
        data: AssistPaymentData,
        scanner: CardScanner?,
        result: (AssistResult) -> Unit
    ) = engine.payWeb(context, data, scanner, result)

    fun createPayWebIntent(
        context: AppCompatActivity,
        data: AssistPaymentData,
        scanner: CardScanner?
    ) =
        Intent(context, PayActivity::class.java)
            .putExtra(PayActivity.EXTRA_ACTION, PayActivity.EXTRA_ACTION_PAY_WEB)
            .putExtra(PayActivity.EXTRA_PAYMENT_DATA, data)
            .putExtra(PayActivity.EXTRA_SCANNER, scanner)

    fun payToken(
        context: AppCompatActivity,
        data: AssistPaymentData,
        token: String,
        type: PaymentTokenType,
        result: (AssistResult) -> Unit
    ) = engine.payToken(context, data, token, type, result)

    fun createPayTokenIntent(
        context: AppCompatActivity,
        data: AssistPaymentData,
        token: String,
        type: PaymentTokenType
    ) =
        Intent(context, PayActivity::class.java)
            .putExtra(PayActivity.EXTRA_ACTION, PayActivity.EXTRA_ACTION_PAY_TOKEN)
            .putExtra(PayActivity.EXTRA_PAYMENT_DATA, data)
            .putExtra(PayActivity.EXTRA_PAYMENT_TOKEN, token)
            .putExtra(PayActivity.EXTRA_TOKEN_TYPE, type)

    fun declineByNumber(
        context: AppCompatActivity,
        data: AssistPaymentData,
        result: (AssistResult) -> Unit
    ) = engine.declineByNumber(context, data, result)

    fun createDeclineByNumberIntent(context: AppCompatActivity, data: AssistPaymentData) =
        Intent(context, PayActivity::class.java)
            .putExtra(PayActivity.EXTRA_ACTION, PayActivity.EXTRA_ACTION_DECLINE)
            .putExtra(PayActivity.EXTRA_PAYMENT_DATA, data)

    fun getOrderDataByLink(link: String, result: (AssistResult) -> Unit) =
        engine.getOrderDataByLink(link, result)

    fun getOrderDataByNumber(
        context: AppCompatActivity,
        order: AssistResult,
        result: (AssistResult) -> Unit
    ) = engine.getOrderDataByNumber(context, order, result)

    suspend fun getOrdersFromStorage(): List<AssistResult>
        = engine.getOrdersFromStorage()

    suspend fun deleteOrderInStorage(order: AssistResult)
        = engine.deleteOrderInStorage(order)
}