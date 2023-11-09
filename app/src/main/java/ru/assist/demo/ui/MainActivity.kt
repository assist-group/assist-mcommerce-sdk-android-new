package ru.assist.demo.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.ekassir.mirpaysdk.client.MirApp
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import org.json.JSONObject
import ru.assist.demo.BuildConfig
import ru.assist.demo.R
import ru.assist.demo.ui.base.BaseActivity
import ru.assist.demo.databinding.ActivityMainBinding
import ru.assist.demo.pays.GooglePay
import ru.assist.demo.pays.MirPay
import ru.assist.demo.pays.SamPay
import ru.assist.demo.scanner.CardIOScanner
import ru.assist.demo.ui.storage.StorageActivity
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.api.models.AssistPaymentData
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.models.Configuration
import ru.assist.sdk.models.Language
import ru.assist.sdk.models.PaymentTokenType
import ru.assist.sdk.ui.PayActivity
import java.math.BigDecimal

class MainActivity : BaseActivity() {
    private val urls = arrayOf(
        "https://payments.t.paysecure.ru",
        "https://test.paysec.by",
        "https://payments.paysec.by",
        "https://test.paysecure.ru",
        "https://payments.paysecure.ru"
    )

    private val defaultMerchantID = "679471"
    private val defaultLogin = "admin679471"
    private val defaultPassword = "admin679471"
    private val defaultAmount = "0.01"
    private val defaultCurrency = "RUB"
    private val defaultLanguage = Language.RU
    private val defaultItems = """
        {"items":[
        {"id"=1, "name"="Копейка", "quantity"=1, "price"=0.01, "amount"=0.01}
        ]}
        """.trimIndent()
    private val defaultFirstName = "Ivanka"
    private val defaultLastName = "Ivanova"
    private val defaultEmail = "ivanka@ivcorp.zz"

    // GooglePay
    private val loadGooglePaymentDataRequestCode = 42
    private lateinit var gp: GooglePay

    // MirPay
    private val mirRequestCode = 100
    private lateinit var mp: MirPay

    // SamsungPay
    private lateinit var sp: SamPay

    // Код для работы через intent
    private val assistRequestCode = 101

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        initURLs()
        initLanguages()
        initMirPay()
        initGooglePay()
        initSamsungPay()
        setDefaults()
    }

    private fun initUI() {
        progressView = binding.progress
        binding.tvVersion.text =
            getString(R.string.versions, BuildConfig.VERSION_NAME, AssistSDK.sdkVersion)
        binding.nsvScroll.setOnScrollChangeListener { _, _, y, _, _ ->
            binding.tvVersion.visibility = if (y == 0) View.VISIBLE else View.GONE
        }
        binding.btWebPay.setOnClickListener { payWeb() }
        binding.btStorage.setOnClickListener { goToStorage() }
        binding.btDecline.setOnClickListener { declineByNumber() }
    }

    private fun initURLs() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            urls
        )
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spURL.adapter = adapter
        binding.spURL.setSelection(0,false) // To avoid selection on listener init
        binding.spURL.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                AssistSDK.getInstance().configure(this@MainActivity, getConfiguration()).clearRegistration()
            }
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun initLanguages() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Language.values()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.spLanguage.adapter = adapter
    }

    private fun fillFieldsByLink(link: String, doAfter: () -> Unit) {
        val sdk = AssistSDK.getInstance().configure(this, getConfiguration())
        sdk.getOrderDataByLink(link, result = {
            val result = it.result
            if (result != null) {
                binding.etMerchantID.setText(result.merchantId)
                binding.etOrderNumber.setText(result.orderNumber)
                binding.etOrderAmount.setText(result.amount)
                binding.etCurrency.setText(result.currency)
                doAfter()
            } else {
                showToast(it.error?.msg)
            }
        })
    }

    private fun initMirPay() {
        val btMirPay = binding.btMirPay
        btMirPay.setOnClickListener {
            val link = binding.etLink.text.toString()
            if (link.isNotBlank()) {
                // Для формирования мирпей-токена необходимы поля заказа,
                // amount, orderNumber - которые надо узнать по ссылке
                fillFieldsByLink(link, ::startMirPay)
            } else {
                startMirPay()
            }
        }
        if (MirApp.isMirPayInstalled(this)) {
            btMirPay.visibility = View.VISIBLE
        } else {
            showToast(R.string.no_mir_pay)
        }
    }

    private fun startMirPay() {
        val orderNumber = binding.etOrderNumber.text.toString()
        val amount = binding.etOrderAmount.text.toString()
        if (!checkValues(binding.etOrderNumber, binding.etOrderAmount)) return

        // TODO приватный ключ, использованный для генерации JWKS в ЛК Assist
        val privateKey = """
                MIIEpAIBAAKCAQEAgJdW2p25jSPwCHkmkP1nDGFBI/9jppkcTOM8WhVcyEUFMJEe
                ...
                h9Drn1hWyExoIZtH54k4FCjpuUFaJY//eDMyB7MRZc0IWrpKt4jrKg==
                """.trimIndent()

        // TODO JWKS необходимо сгенерировать и скачать в ЛК Assist
        val jwks = "{\"keys\":[...]}"
        // TODO merchantNspkId надо взять из наименования скачанного JWKS
        val merchantNspkId = "12345"

        mp = object : MirPay(jwks, privateKey, merchantNspkId) {
            override fun runMirPay(intent: Intent) {
                startActivityForResult(intent, mirRequestCode)
            }

            override fun doWithToken(result: String) {
                payToken(result, PaymentTokenType.MIR_PAY)
            }
        }
        val formattedAmount = BigDecimal(amount).multiply(BigDecimal(100)).toInt()
        mp.pay(this, orderNumber, formattedAmount)
    }

    private fun initGooglePay() {
        gp = GooglePay(this, isReady = {
            if (it) {
                binding.btGooglePay.setOnClickListener {
                    val link = binding.etLink.text.toString()
                    if (link.isNotBlank()) {
                        // Для формирования гуглпей-токена необходимы поля заказа:
                        // amount, merchant_id, currency - которые надо узнать по ссылке
                        fillFieldsByLink(link, ::startGooglePay)
                    } else {
                        startGooglePay()
                    }
                }
                binding.btGooglePay.visibility = View.VISIBLE
            } else {
                showToast(R.string.no_google_pay)
            }
        })
    }

    private fun startGooglePay() {
        if (!checkValues(
                binding.etMerchantID,
                binding.etOrderAmount,
                binding.etCurrency))
            return
        gp.pay(
            binding.etMerchantID.text.toString(),
            binding.etOrderAmount.text.toString(),
            binding.etCurrency.text.toString(),
            callback = {
                AutoResolveHelper.resolveTask(it, this, loadGooglePaymentDataRequestCode)
            }
        )
    }

    private fun initSamsungPay() {
        sp = object : SamPay(this@MainActivity) {
            override fun doWithToken(result: String) {
                payToken(result, PaymentTokenType.SAMSUNG_PAY)
            }
        }

        if (sp.isAvailable) {
            binding.btSamsungPay.setOnClickListener {
                val link = binding.etLink.text.toString()
                if (link.isNotBlank()) {
                    // Для формирования гуглпей-токена необходимы поля заказа:
                    // amount, orderNumber, merchant_id, currency - которые надо узнать по ссылке
                    fillFieldsByLink(link, ::startSamsungPay)
                } else {
                    startSamsungPay()
                }
            }
            binding.btSamsungPay.visibility = View.VISIBLE
        } else {
            showToast(R.string.no_samsung_pay)
        }
    }

    private fun startSamsungPay() {
        if (!checkValues(
                binding.etMerchantID,
                binding.etOrderNumber,
                binding.etOrderAmount,
                binding.etCurrency))
            return
        val data = AssistPaymentData(
            merchantID = binding.etMerchantID.text.toString(),
            orderNumber = binding.etOrderNumber.text.toString(),
            orderAmount = binding.etOrderAmount.text.toString(),
            orderCurrency = binding.etCurrency.text.toString(),
        )
        sp.startSamsungPay(data)
    }

    private fun setDefaults() {
        binding.etMerchantID.setText(defaultMerchantID)
        binding.etLogin.setText(defaultLogin)
        binding.etPassword.setText(defaultPassword)
        binding.etOrderAmount.setText(defaultAmount)
        binding.etCurrency.setText(defaultCurrency)
        binding.etOrderItems.setText(defaultItems)
        binding.spLanguage.setSelection(Language.values().indexOf(defaultLanguage))
        binding.etFirstname.setText(defaultFirstName)
        binding.etLastname.setText(defaultLastName)
        binding.etEmail.setText(defaultEmail)
    }

    private fun getConfiguration(): Configuration =
        Configuration(
            apiURL = urls[binding.spURL.selectedItemPosition],
            link = binding.etLink.text.toString(),
        )

    private fun goToStorage() {
        startActivity(Intent(this, StorageActivity::class.java))
    }

    private fun payWeb() {
        log("Pay by web")

        val yooMoney = binding.chYMPayment.isChecked
        val webMoney = binding.chWMPayment.isChecked
        val qiwi = binding.chQIWIPayment.isChecked
        val qiwiMTS = binding.chQIWIMtsPayment.isChecked
        val qiwiMegafon = binding.chQIWIMegafonPayment.isChecked
        val qiwiBeeline = binding.chQIWIBeelinePayment.isChecked
        val fastPay = binding.chFastPayPayment.isChecked

        val data = AssistPaymentData(
            merchantID = binding.etMerchantID.text.toString(),
            login = binding.etLogin.text.toString(),
            password = binding.etPassword.text.toString(),
            orderNumber = binding.etOrderNumber.text.toString(),
            orderAmount = binding.etOrderAmount.text.toString(),
            orderCurrency = binding.etCurrency.text.toString(),
            language = Language.values()[binding.spLanguage.selectedItemPosition].name,
            chequeItems = binding.etOrderItems.text.toString(),
            orderComment = binding.etOrderComment.text.toString(),
            customerNumber = binding.etCustomerNumber.text.toString(),
            signature = binding.etSignature.text.toString(),
            lastname = binding.etLastname.text.toString(),
            firstname = binding.etFirstname.text.toString(),
            middlename = binding.etMiddlename.text.toString(),
            email = binding.etEmail.text.toString(),
            address = binding.etAddress.text.toString(),
            homePhone = binding.etHomePhone.text.toString(),
            workPhone = binding.etWorkPhone.text.toString(),
            mobilePhone = binding.etMobilePhone.text.toString(),
            fax = binding.etFax.text.toString(),
            country = binding.etCountry.text.toString(),
            state = binding.etState.text.toString(),
            city = binding.etCity.text.toString(),
            zip = binding.etZip.text.toString(),
            taxpayerID = binding.etTaxpayerID.text.toString(),
            customerDocID = binding.etCustomerDocID.text.toString(),
            paymentAddress = binding.etPaymentAddress.text.toString(),
            paymentPlace = binding.etPaymentPlace.text.toString(),
            cashier = binding.etCashier.text.toString(),
            cashierINN = binding.etCashierINN.text.toString(),
            paymentTerminal = binding.etPaymentTerminal.text.toString(),
            transferOperatorPhone = binding.etTransferOperatorPhone.text.toString(),
            transferOperatorName = binding.etTransferOperatorName.text.toString(),
            transferOperatorAddress = binding.etTransferOperatorAddress.text.toString(),
            transferOperatorINN = binding.etTransferOperatorINN.text.toString(),
            paymentReceiverOperatorPhone = binding.etPaymentReceiverOperatorPhone.text.toString(),
            paymentAgentPhone = binding.etPaymentAgentPhone.text.toString(),
            paymentAgentOperation = binding.etPaymentAgentOperation.text.toString(),
            supplierPhone = binding.etSupplierPhone.text.toString(),
            paymentAgentMode = binding.etPaymentAgentMode.text.toString(),
            documentRequisite = binding.etDocumentRequisite.text.toString(),
            userRequisites = binding.etUserRequisites.text.toString(),
            ymPayment = if (yooMoney) "1" else "0",
            wmPayment = if (webMoney) "1" else "0",
            qiwiPayment = if (qiwi) "1" else "0",
            qiwiMtsPayment = if (qiwiMTS) "1" else "0",
            qiwiMegafonPayment = if (qiwiMegafon) "1" else "0",
            qiwiBeelinePayment = if (qiwiBeeline) "1" else "0",
            fastPayPayment = if (fastPay) "1" else "0",
            generateReceipt = binding.etGenerateReceipt.text.toString(),
            receiptLine = binding.etReceiptLine.text.toString(),
            tax = binding.etTax.text.toString(),
            fpmode = binding.etFPMode.text.toString(),
            taxationSystem = binding.etTaxationSystem.text.toString(),
            prepayment = binding.etPrepayment.text.toString()
        )

        val scanner = if (binding.cbUseCamera.isChecked) CardIOScanner() else null

        val sdk = AssistSDK.getInstance().configure(this, getConfiguration())
        // Прямой вызов через метод
        sdk.payWeb(this, data, scanner, ::processResult)
        // Вызов через intent
//        val intent = sdk.createPayWebIntent(this, data, scanner)
//        startActivityForResult(intent, assistRequestCode)
    }

    private fun payToken(token: String, type: PaymentTokenType) {
        log("Pay by token=$token, type=$type")

        showProgress(true)

        val data = AssistPaymentData(
            merchantID = binding.etMerchantID.text.toString(),
            login = binding.etLogin.text.toString(),
            password = binding.etPassword.text.toString(),
            orderNumber = binding.etOrderNumber.text.toString(),
            orderAmount = binding.etOrderAmount.text.toString(),
            orderCurrency = binding.etCurrency.text.toString(),
            orderComment = binding.etOrderComment.text.toString(),
            lastname = binding.etLastname.text.toString(),
            firstname = binding.etFirstname.text.toString(),
            email = binding.etEmail.text.toString()
        )

        val sdk = AssistSDK.getInstance().configure(this, getConfiguration())
        // Прямой вызов через метод
        sdk.payToken(this, data, token, type, result = {
            showProgress(false)
            processResult(it)
        })
        // Вызов через intent
//        val intent = sdk.createPayTokenIntent(this, data, token, type)
//        startActivityForResult(intent, assistRequestCode)
    }

    private fun processResult(result: AssistResult) {
        if (result.result != null) {
            goToResultActivity(result)
        } else {
            showToast(result.error?.msg ?: getString(R.string.unknown_error))
        }
    }

    private fun declineByNumber() {
        log("Decline orderNumber=${binding.etOrderNumber.text}")

        showProgress(true)

        val sdk = AssistSDK.getInstance().configure(this, getConfiguration())

        val data = AssistPaymentData(
            merchantID = binding.etMerchantID.text.toString(),
            login = binding.etLogin.text.toString(),
            password = binding.etPassword.text.toString(),
            orderNumber = binding.etOrderNumber.text.toString()
        )

        // Прямой вызов через метод
        sdk.declineByNumber(this, data, result = {
            showProgress(false)
            processResult(it)
        })
        // Вызов через intent
//        val intent = sdk.createDeclineByNumberIntent(this, data)
//        startActivityForResult(intent, assistRequestCode)
    }

    private fun goToResultActivity(result: AssistResult) =
        startActivity(
            Intent(this, ResultActivity::class.java).putExtra("result", result)
        )

    private fun checkValues(vararg fields: EditText): Boolean {
        for (field in fields) {
            if (field.text.toString().isBlank()) {
                val badField = resources.getResourceEntryName(field.id)
                showToast(R.string.empty_field, badField)
                return false
            }
        }
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        log("requestCode: $requestCode; resultCode: $resultCode")
        if (requestCode == loadGooglePaymentDataRequestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    log("RESULT_OK")
                    try {
                        val paymentData = PaymentData.getFromIntent(data!!)
                        val json = paymentData!!.toJson()

                        val paymentMethodData = JSONObject(json).getJSONObject("paymentMethodData")
                        val paymentToken = paymentMethodData
                            .getJSONObject("tokenizationData")
                            .getString("token")
                        payToken(paymentToken, PaymentTokenType.GOOGLE_PAY)
                    } catch (e: Exception) {
                        logE("Getting GooglePay payment data error", e)
                    }
                }
                RESULT_CANCELED -> log("RESULT_CANCELED")
                AutoResolveHelper.RESULT_ERROR -> {
                    log("RESULT_ERROR")
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    // Log the status for debugging.
                    // Generally, there is no need to show an error to the user.
                    // The Google Pay payment sheet will present any account errors.
                    logE("Getting Google Pay token error: $status")
                }
                else -> log("DEFAULT")
            }
        } else if (requestCode == mirRequestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    log("RESULT_OK")
                    data?.let { mp.setResult(it) }
                }
                else -> log("DEFAULT")
            }
        } else if (requestCode == assistRequestCode) { // Ответ SDK при работе через intent
            log("Assist result code=$resultCode")
            (data?.getParcelableExtra(PayActivity.EXTRA_ASSIST_RESULT) as? AssistResult)?.let {
                showProgress(false)
                // Если resultCode=RESULT_OK, то возвращается AssistResult.result с ответом сервера Assist
                // Если resultCode=RESULT_CANCELED, то возвращается AssistResult.msg с текстом ошибки
                processResult(it)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}