package ru.assist.sdk.engine

import android.content.Intent
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.api.AssistApi
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.api.models.ResultRequest
import ru.assist.sdk.scanner.CardData
import ru.assist.sdk.scanner.CardScanner
import ru.assist.sdk.ui.WebViewActivity
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

internal class WebProcessor(
    private val context: AppCompatActivity,
    postUrl: String,
    content: String,
    private val scanner: CardScanner?,
    private val request: ResultRequest,
    private val rr: (AppCompatActivity, ResultRequest, (AssistResult) -> Unit) -> Unit,
    private val result: (AssistResult) -> Unit
) {
    @Inject
    lateinit var api: AssistApi

    init {
        AssistSDK.getInstance().component.inject(this)
        context.startActivity(
            Intent(context, WebViewActivity::class.java)
                .putExtra("url", postUrl)
                .putExtra("content", content)
        )
    }

    fun getDataFromResult(url: String) {
        val call = AddressCall(
            action = { json ->
                rr(context, fillRequestDataByJson(json), result)
            }
        )
        api.getAddress(url).enqueue(call)
    }

    fun getDataFromError(url: String) {
        val call = AddressCall(
            action = { html ->
                result(AssistResult(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()))
            }
        )
        api.getAddress(url).enqueue(call)
    }

    fun checkScanner(url: String, doWithScanner: (CardScanner) -> Unit) {
        if (scanner != null) {
            val call = AddressCall(
                action = { html ->
                    if (isCardPage(html)) doWithScanner(scanner)
                }
            )
            api.getAddress(url).enqueue(call)
        }
    }

    fun getCardData(data: Intent): CardData? {
        return scanner?.getCardDataFromIntent(data)
    }

    fun stop() {
        result(AssistResult("Payment stopped"))
    }

    private fun fillRequestDataByJson(json: String): ResultRequest {
        val orderNumber = JSONObject(json)
            .getJSONObject("params")
            .getJSONObject("aordernumber")
            .getString("value")

        return request.apply {
            setOrderNumber(orderNumber)
            setDate(
                SimpleDateFormat("dd.MM.yyyy", Locale.US).format(System.currentTimeMillis())
            )
        }
    }

    private fun isCardPage(html: String) = html.contains("CardNumber")

    inner class AddressCall(private val action: (String) -> Unit) : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            val stream = ByteArrayInputStream(response.body()?.bytes())
            val html = BufferedReader(InputStreamReader(stream)).readText()
            action(html)
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            result(AssistResult(t.message))
        }
    }
}