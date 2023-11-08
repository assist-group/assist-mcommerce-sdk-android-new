package ru.assist.sdk.models

enum class PaymentTokenType(
    private val value: String
) {
    GOOGLE_PAY("2"),
    SAMSUNG_PAY("3"),
    MIR_PAY("6");

    override fun toString(): String {
        return value
    }
}