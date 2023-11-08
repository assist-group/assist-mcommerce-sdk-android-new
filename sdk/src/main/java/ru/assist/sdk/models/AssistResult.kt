package ru.assist.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class AssistResult(
    val result: AssistOrderResult?,
    val error: AssistError?
) : Parcelable {

    constructor(
        merchantId: String?,
        orderState: OrderState?,
        approvalCode: String?,
        billNumber: String?,
        extraInfo: String?,
        orderNumber: String?,
        amount: String?,
        currency: String?,
        comment: String?,
        email: String?,
        firstName: String?,
        lastName: String?,
        middleName: String?,
        signature: String?,
        checkValue: String?,
        meanTypeName: String?,
        meanNumber: String?,
        cardholder: String?,
        cardExpirationDate: String?,
        chequeItems: String?
        ) : this(
        AssistOrderResult(
            merchantId = merchantId,
            orderState = orderState,
            approvalCode = approvalCode,
            billNumber = billNumber,
            extraInfo = extraInfo,
            orderNumber = orderNumber,
            amount = amount,
            currency = currency,
            comment = comment,
            email = email,
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            signature = signature,
            checkValue = checkValue,
            meanTypeName = meanTypeName,
            meanNumber = meanNumber,
            cardholder = cardholder,
            cardExpirationDate = cardExpirationDate,
            chequeItems = chequeItems
        ),
        null
    )

    constructor(msg: String?) : this(null, AssistError(msg))

    @Parcelize
    data class AssistError(
        val msg: String?
    ) : Parcelable

    @Parcelize
    data class AssistOrderResult(
        val merchantId: String?,
        val orderState: OrderState?,
        val approvalCode: String?,
        val billNumber: String?,
        val extraInfo: String?,
        val orderNumber: String?,
        val amount: String?,
        val currency: String?,
        val comment: String?,
        val email: String?,
        val firstName: String?,
        val lastName: String?,
        val middleName: String?,
        val signature: String?,
        val checkValue: String?,
        val meanTypeName: String?,
        val meanNumber: String?,
        val cardholder: String?,
        val cardExpirationDate: String?,
        val chequeItems: String?,
        var dateMillis: Long? = null
    ) : Parcelable
}