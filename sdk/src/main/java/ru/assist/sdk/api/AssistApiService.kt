package ru.assist.sdk.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import ru.assist.sdk.api.models.AuthRequest
import ru.assist.sdk.api.models.AuthResponse
import ru.assist.sdk.api.models.DeclineRequest
import ru.assist.sdk.api.models.DeclineResponse
import ru.assist.sdk.api.models.RegisterRequest
import ru.assist.sdk.api.models.RegisterResponse
import ru.assist.sdk.api.models.ResultRequest
import ru.assist.sdk.api.models.ResultResponse
import ru.assist.sdk.api.models.TokenPayResponse
import ru.assist.sdk.dagger2.Json
import ru.assist.sdk.dagger2.UrlXml
import ru.assist.sdk.dagger2.Xml

internal interface AssistApiService {
	@Xml
	@Headers("Content-Type: text/xml", "Accept-Charset: utf-8")
	@POST("/registration/mobileregistration.cfm")
	fun register(@Body body: RegisterRequest): Call<RegisterResponse>

	@UrlXml
	@FormUrlEncoded
	@POST("/pay/tokenpay.cfm")
	fun payToken(@FieldMap params: HashMap<String, String?>): Call<TokenPayResponse>

	@UrlXml
	@FormUrlEncoded
	@POST("/pay/tokenpay_order.cfm")
	fun payTokenLink(@FieldMap params: HashMap<String, String?>): Call<TokenPayResponse>

	@GET
	fun getAddress(@Url url: String) : Call<ResponseBody>

	@Xml
	@POST("/orderresult/mobileorderresult.cfm")
	fun getOrderResult(@Body body: ResultRequest): Call<ResultResponse>

	@Json
	@POST("/api/v1/auth/login.cfm")
	fun auth(@Body body: AuthRequest): Call<AuthResponse>

	@Json
	@POST("/api/v1/order/cancel.cfm")
	fun declineByNumber(@Body body: DeclineRequest, @Header("Authorization") token: String): Call<DeclineResponse>
}