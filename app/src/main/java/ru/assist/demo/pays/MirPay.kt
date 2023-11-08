package ru.assist.demo.pays

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import kotlinx.coroutines.launch
import org.jose4j.jwk.JsonWebKeySet
import org.jose4j.jwk.PublicJsonWebKey
import ru.nspk.mirpay.sdk.api.payment.MirPayPaymentClientFactory
import ru.nspk.mirpay.sdk.api.payment.MirPayPaymentResultExtractor
import ru.nspk.mirpay.sdk.data.model.payment.MerchantToken
import ru.nspk.mirpay.sdk.data.model.payment.PaymentToken
import java.net.URI
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date

abstract class MirPay(
    jwksString: String,
    privateKey: String,
    private val merchantId: String
) {

    companion object {
        private const val RUB = 643
    }

    private val signer: JWSSigner
    private val certs: List<Base64>
    private val kid: String // Только для DeepLink

    init {
        val jwks = JsonWebKeySet(jwksString)
        val jwk = jwks.jsonWebKeys[0] as PublicJsonWebKey
        certs = jwk.certificateChain.map {
            Base64.encode(it.encoded)
        }
        val pk = jwk.privateKey ?: getPrivateKeyFromString(privateKey)
        signer = RSASSASigner(pk)
        kid = jwk.keyId // Только для DeepLink
    }

    abstract fun doWithToken(result: String)

    abstract fun runMirPay(intent: Intent)

    private fun getPaymentToken(orderNumber: String, amount: Int): PaymentToken {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(JOSEObjectType.JWT)
                .x509CertChain(certs)
                .build(),
            JWTClaimsSet.Builder()
                .claim("iat", System.currentTimeMillis()) // Для MirPay нужны именно миллисекунды
                .issuer(getIssFromMerchantId(merchantId))
                .claim("orderId", orderNumber)
                .claim("sum", amount)
                .claim("cur", RUB)
                .claim("media", "ISDK")
                .build()
        )
        signedJWT.sign(signer)
        return PaymentToken(signedJWT.serialize())
    }

    // Для сценария работы через DeepLink
    private fun getDeepLink(orderNumber: String, amount: Int): String {
        val iss = getIssFromMerchantId(merchantId)
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(JOSEObjectType.JWT)
                .keyID(kid)
                .jwkURL(URI.create("https://payments.t.paysecure.ru/api/v1/rest/mirpay/jwks/$iss-jwks.json")) // Необходимо подставить URL своего JWKS
                .build(),
            JWTClaimsSet.Builder()
                .issueTime(Date())
                .issuer(iss)
                .claim("orderId", orderNumber)
                .claim("sum", amount)
                .claim("cur", RUB)
                .claim("media", "DL")
                .claim("rurl", "https://your_endpoint.url") // Необходимо подставить URL своего сервиса обработки ответа MirPay
                .build()
        )
        signedJWT.sign(signer)
        return "mirpay://pay.mironline.ru/inapp/${signedJWT.serialize()}"
    }

    private fun getMerchantToken(): MerchantToken {
        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.PS256)
                .type(JOSEObjectType.JWT)
                .x509CertChain(certs)
                .build(),
            JWTClaimsSet.Builder()
                .claim("iat", System.currentTimeMillis()) // Для MirPay нужны именно миллисекунды
                .issuer(getIssFromMerchantId(merchantId))
                .build()
        )
        signedJWT.sign(signer)
        return MerchantToken(signedJWT.serialize())
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun getPrivateKeyFromString(pkcs8Pem: String): PrivateKey {
        val onlyBase64 = pkcs8Pem.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                            .replace("-----END RSA PRIVATE KEY-----", "")
                            .replace("\\s+".toRegex(), "")
        val pkcs8EncodedBytes: ByteArray = org.bouncycastle.util.encoders.Base64.decode(onlyBase64)
        val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    private fun getIssFromMerchantId(merchantId: String): String {
        var result = merchantId
        do {
            result = "0$result"
        } while (result.length != 15)
        return result
    }

    fun pay(context: AppCompatActivity, orderNumber: String, amount: Int) {
        // С использованием mirPaySdk
        val merchantToken = getMerchantToken()
        val paymentToken = getPaymentToken(orderNumber, amount)
        context.lifecycleScope.launch {
            val client = MirPayPaymentClientFactory.create(context, merchantToken)
            val intent = client.createPaymentIntent(paymentToken)
            client.disconnect()
            runMirPay(intent)
        }
        // Без mirPaySdk. Через DeepLink
//        val url = getDeepLink(orderNumber, amount)
//        context.startActivity(
//            Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        )
    }

    fun setResult(intent: Intent) {
        doWithToken(MirPayPaymentResultExtractor.extract(intent).value)
    }
}