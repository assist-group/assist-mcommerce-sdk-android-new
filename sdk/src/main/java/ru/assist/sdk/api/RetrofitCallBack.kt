package ru.assist.sdk.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.assist.sdk.api.models.Fault
import ru.assist.sdk.api.models.FramedFault
import ru.assist.sdk.util.NetworkChecker.checkForInternet
import java.io.IOException

internal class RetrofitCallBack<T> {
    companion object {
        private const val ERROR = "Unknown error"
        private const val NETWORK_ERROR = "No network connection"
    }

    private var successAction: ((T) -> Unit)? = null
    private var errorAction: ((String) -> Unit)? = null

    private val gson by lazy {
        GsonBuilder().create()
    }

    fun call(context: Context, call: Call<T>) {
        if (!checkForInternet(context)) {
            errorAction?.let { it(NETWORK_ERROR) }
            return
        }
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    successAction?.let {
                        it(response.body()!!)
                    }
                } else {
                    errorAction?.let {
                        try {
                            response.errorBody().use { response ->
                                val errorJson = response?.string()
                                try {
                                    // Looking for correct fault format
                                    val f1 = gson.fromJson(errorJson, Fault::class.java)
                                    val f2 = gson.fromJson(errorJson, FramedFault::class.java)
                                    if (f1.faultCode != null) {
                                        it("${f1.faultCode} ${f1.faultString}")
                                    } else if (f2.fault.faultCode != null) {
                                        it("${f2.fault.faultCode} ${f2.fault.faultString}")
                                    }
                                } catch (e: JsonSyntaxException) {
                                    it(errorJson ?: ERROR)
                                }
                            }
                        } catch (e: IOException) {
                            logError(call, e)
                            it(ERROR)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                logError(call, t)
                errorAction?.let { it(ERROR) }
            }

            private fun logError(call: Call<T>, t: Throwable) {
                Log.e("RetrofitCallBack", "Fail to call ${call.request()}", t)
            }
        })
    }

    fun onSuccess(action: (T) -> Unit): RetrofitCallBack<T> {
        successAction = action
        return this
    }

    fun onError(action: (String) -> Unit): RetrofitCallBack<T> {
        errorAction = action
        return this
    }
}