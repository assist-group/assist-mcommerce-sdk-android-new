package ru.assist.sdk.api

import android.content.Context
import okhttp3.ResponseBody
import retrofit2.Call
import ru.assist.sdk.api.models.AuthRequest
import ru.assist.sdk.api.models.AuthResponse
import ru.assist.sdk.api.models.DeclineRequest
import ru.assist.sdk.api.models.DeclineResponse
import ru.assist.sdk.api.models.RegisterRequest
import ru.assist.sdk.api.models.RegisterResponse
import ru.assist.sdk.api.models.ResultRequest
import ru.assist.sdk.api.models.ResultResponse
import ru.assist.sdk.api.models.TokenPayResponse
import ru.assist.sdk.models.AssistResult
import javax.inject.Inject

internal class AssistApi @Inject constructor(private val apiService: AssistApiService) {
	fun register(requestBody: RegisterRequest): Call<RegisterResponse> =
		apiService.register(requestBody)

	fun payToken(params: HashMap<String, String?>): Call<TokenPayResponse> =
		apiService.payToken(params)

	fun payTokenLink(params: HashMap<String, String?>): Call<TokenPayResponse> =
		apiService.payTokenLink(params)

	fun getAddress(url: String): Call<ResponseBody> =
		apiService.getAddress(url)

	fun getOrderResult(requestBody: ResultRequest): Call<ResultResponse> =
		apiService.getOrderResult(requestBody)

	fun declineByNumber(
		context: Context,
		requestBody: DeclineRequest,
		response: (Call<DeclineResponse>) -> Unit,
		result: (AssistResult) -> Unit
	) {
		val auth = AuthRequest(requestBody.login, requestBody.password)
		RetrofitCallBack<AuthResponse>()
			.onSuccess {
				response(apiService.declineByNumber(requestBody, "Bearer ${it.accessToken}"))
			}
			.onError {
				result(AssistResult(it))
			}
			.call(context, apiService.auth(auth))
	}
}