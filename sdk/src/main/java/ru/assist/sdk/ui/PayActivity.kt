package ru.assist.sdk.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.engine.Engine
import ru.assist.sdk.api.models.AssistPaymentData
import ru.assist.sdk.databinding.PayActivityBinding
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.models.PaymentTokenType
import ru.assist.sdk.scanner.CardScanner
import javax.inject.Inject

class PayActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_ACTION = "ru.assist.sdk.action"
        const val EXTRA_ACTION_PAY_TOKEN = "ru.assist.sdk.paytoken"
        const val EXTRA_ACTION_PAY_WEB = "ru.assist.sdk.payweb"
        const val EXTRA_ACTION_DECLINE = "ru.assist.sdk.decline"
        const val EXTRA_PAYMENT_DATA = "ru.assist.sdk.paymentdata"
        const val EXTRA_SCANNER = "ru.assist.sdk.scanner"
        const val EXTRA_PAYMENT_TOKEN = "ru.assist.sdk.paymenttoken"
        const val EXTRA_TOKEN_TYPE = "ru.assist.sdk.tokentype"
        const val EXTRA_ASSIST_RESULT = "ru.assist.sdk.result"
    }

    @Inject
    internal lateinit var engine: Engine

    private lateinit var binding: PayActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AssistSDK.getInstance().component.inject(this)

        binding = PayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras == null) {
            errorResult("Extras is null")
        } else {
            when (extras.getString(EXTRA_ACTION)) {
                EXTRA_ACTION_PAY_WEB -> payWeb(extras)
                EXTRA_ACTION_PAY_TOKEN -> payToken(extras)
                EXTRA_ACTION_DECLINE -> declineByNumber(extras)
                else -> errorResult("Unknown action")
            }
        }
    }

    private fun payWeb(bundle: Bundle) {
        val data = bundle.getParcelable<AssistPaymentData>(EXTRA_PAYMENT_DATA)
        val scanner = bundle.getParcelable<CardScanner>(EXTRA_SCANNER)
        if (data != null) {
            engine.payWeb(this, data, scanner, ::processResult)
        } else {
            errorResult("PayWeb empty data")
        }
    }

    private fun payToken(bundle: Bundle) {
        val data = bundle.getParcelable<AssistPaymentData>(EXTRA_PAYMENT_DATA)
        val token = bundle.getString(EXTRA_PAYMENT_TOKEN)
        val type = bundle.getSerializable(EXTRA_TOKEN_TYPE) as? PaymentTokenType
        if (data != null && token != null && type != null) {
            engine.payToken(this, data, token, type, ::processResult)
        } else {
            errorResult("PayToken empty data")
        }
    }

    private fun declineByNumber(bundle: Bundle) {
        val data = bundle.getParcelable<AssistPaymentData>(EXTRA_PAYMENT_DATA)
        if (data != null) {
            engine.declineByNumber(this, data, ::processResult)
        } else {
            errorResult("DeclineByNumber empty data")
        }
    }

    private fun errorResult(msg: String) {
        setResult(
            Activity.RESULT_CANCELED,
            Intent().putExtra(EXTRA_ASSIST_RESULT, AssistResult(msg))
        )
        finish()
    }

    private fun processResult(result: AssistResult) {
        setResult(
            Activity.RESULT_OK,
            Intent().putExtra(EXTRA_ASSIST_RESULT, result)
        )
        finish()
    }
}