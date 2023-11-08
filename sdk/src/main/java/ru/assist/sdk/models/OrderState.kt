package ru.assist.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class OrderState : Parcelable {
    UNKNOWN,
    IN_PROCESS,
    DELAYED,
    APPROVED,
    PARTIAL_APPROVED,
    PARTIAL_DELAYED,
    CANCELED,
    PARTIAL_CANCELED,
    DECLINED,
    TIMEOUT,
    CANCEL_ERROR,
    NON_EXISTENT;

    companion object {
        fun fromString(value: String?): OrderState =
            when (value?.uppercase()) {
                "UNKNOWN", "НЕИЗВЕСТНО" -> UNKNOWN
                "IN PROCESS", "В ПРОЦЕССЕ", "PROCESS" -> IN_PROCESS
                "DELAYED", "ОЖИДАЕТ ПОДТВЕРЖДЕНИЯ ОПЛАТЫ" -> DELAYED
                "APPROVED", "ОПЛАЧЕН" -> APPROVED
                "PARTIALAPPROVED", "ОПЛАЧЕН ЧАСТИЧНО" -> PARTIAL_APPROVED
                "PARTIALDELAYED", "ПОДТВЕРЖДЕН ЧАСТИЧНО", "ПОДТВЕРЖДЁН ЧАСТИЧНО" -> PARTIAL_DELAYED
                "CANCELED", "ОТМЕНЕН", "ОТМЕНЁН" -> CANCELED
                "PARTIALCANCELED", "ОТМЕНЕН ЧАСТИЧНО", "ОТМЕНЁН ЧАСТИЧНО" -> PARTIAL_CANCELED
                "DECLINED", "ОТКЛОНЕН", "ОТКЛОНЁН" -> DECLINED
                "TIMEOUT", "ЗАКРЫТ ПО ИСТЕЧЕНИИ ВРЕМЕНИ" -> TIMEOUT
                "CANCEL ERROR", "ОШИБКА ОТМЕНЫ" -> CANCEL_ERROR
                "NON-EXISTENT", "НЕ СУЩЕСТВУЕТ" -> NON_EXISTENT
                else -> UNKNOWN
            }
    }

    override fun toString(): String =
         when (this) {
            UNKNOWN -> "Неизвестно"
            IN_PROCESS -> "В процессе"
            DELAYED -> "Ожидает подтверждения оплаты"
            APPROVED -> "Оплачен"
            PARTIAL_APPROVED -> "Оплачен частично"
            PARTIAL_DELAYED -> "Подтверждён частично"
            CANCELED -> "Отменён"
            PARTIAL_CANCELED -> "Отменён частично"
            DECLINED -> "Отклонён"
            TIMEOUT -> "Закрыт по истечении времени"
            CANCEL_ERROR -> "Ошибка отмены"
            NON_EXISTENT -> "Не существует"
        }
}