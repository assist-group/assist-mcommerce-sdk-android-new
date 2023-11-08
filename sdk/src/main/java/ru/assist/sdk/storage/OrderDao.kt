package ru.assist.sdk.storage

import android.provider.BaseColumns
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.assist.sdk.exception.AssistSdkException
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.storage.models.AssistOrder
import java.util.Calendar

@Dao
internal abstract class OrderDao {
    companion object {
        const val DB_TABLE_ORDERS = "orders"

        const val COLUMN_ORDER_MID = "omid"
        const val COLUMN_ORDER_DATE_DEVICE_MILLIS = "otime_millis"
        const val COLUMN_ORDER_NUMBER = "onum"
        const val COLUMN_ORDER_COMMENT = "ocom"
        const val COLUMN_ORDER_AMOUNT = "oamt"
        const val COLUMN_ORDER_CURRENCY = "ocur"
        const val COLUMN_ORDER_STATE = "ostat"
        const val COLUMN_ORDER_APPROVAL_CODE = "oacode"
        const val COLUMN_ORDER_EXTRA_INFO = "oextra"
        const val COLUMN_BILL_NUMBER = "bnum"
        const val COLUMN_USER_SIGNATURE = "usign"
        const val COLUMN_ORDER_ITEMS_JSON = "oitems"
        const val COLUMN_MEANNUMBER = "meann"
        const val COLUMN_MEANTYPENAME = "meant"
        const val COLUMN_CARDEXPIRATIONDATE = "cexp"
        const val COLUMN_CARDHOLDER = "chold"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_CHECK_VALUE = "chval"
        const val COLUMN_FIRST_NAME = "fname"
        const val COLUMN_MIDDLE_NAME = "mname"
        const val COLUMN_LAST_NAME = "lname"

        private fun mapToAssistResult(order: AssistOrder)
            = AssistResult(
                merchantId = order.merchantId,
                orderState = order.orderState,
                approvalCode = order.approvalCode,
                billNumber = order.billNumber,
                extraInfo = order.extraInfo,
                orderNumber = order.orderNumber,
                amount = order.amount,
                currency = order.currency,
                comment = order.comment,
                email = order.email,
                firstName = order.firstName,
                lastName = order.lastName,
                middleName = order.middleName,
                signature = order.signature,
                checkValue = order.checkValue,
                meanTypeName = order.meanTypeName,
                meanNumber = order.meanNumber,
                cardholder = order.cardholder,
                cardExpirationDate = order.cardExpirationDate,
                chequeItems = order.chequeItems
            ).apply {
                result?.dateMillis = order.dateMillis
            }

        private fun mapToDBOrder(order: AssistResult): AssistOrder {
            val merchantId = order.result?.merchantId
            val orderNumber = order.result?.orderNumber
            if (merchantId != null && orderNumber != null) {
                return AssistOrder(
                    merchantIdOrderNumber = AssistOrder.createMerchantIdOrderNumber(
                        merchantId,
                        orderNumber
                    ),
                    merchantId = merchantId,
                    orderState = order.result.orderState,
                    approvalCode = order.result.approvalCode,
                    billNumber = order.result.billNumber,
                    extraInfo = order.result.extraInfo,
                    orderNumber = orderNumber,
                    amount = order.result.amount,
                    currency = order.result.currency,
                    comment = order.result.comment,
                    email = order.result.email,
                    firstName = order.result.firstName,
                    lastName = order.result.lastName,
                    middleName = order.result.middleName,
                    signature = order.result.signature,
                    checkValue = order.result.checkValue,
                    meanTypeName = order.result.meanTypeName,
                    meanNumber = order.result.meanNumber,
                    cardholder = order.result.cardholder,
                    cardExpirationDate = order.result.cardExpirationDate,
                    chequeItems = order.result.chequeItems,
                    dateMillis = order.result.dateMillis
                )
            } else {
                throw AssistSdkException("Can't map order with empty id")
            }
        }
    }

    @Transaction
    open suspend fun getOrders(): List<AssistResult> {
        val query = "SELECT * FROM $DB_TABLE_ORDERS" +
            " ORDER BY $COLUMN_ORDER_DATE_DEVICE_MILLIS DESC"

        val result = getOrders(SimpleSQLiteQuery(query))

        return result.map { mapToAssistResult(it) }
    }

    @Transaction
    open fun getOrderAsDB(orderNumber: String, merchantId: String): AssistOrder? {
        val query = "SELECT * FROM $DB_TABLE_ORDERS WHERE " +
                "${BaseColumns._ID} = '${merchantId}_$orderNumber'" +
                " ORDER BY $COLUMN_ORDER_DATE_DEVICE_MILLIS DESC"

        val result = getOrders(SimpleSQLiteQuery(query))

        return if (result.isEmpty())
            null
        else
            result[0]
    }
    @Transaction
    open fun getOrder(orderNumber: String, merchantId: String): AssistResult? {
        val result = getOrderAsDB(orderNumber, merchantId)

        return if (result == null)
            null
        else
            mapToAssistResult(result)
    }

    @Transaction
    open suspend fun addOrUpdateOrder(order: AssistResult) {
        var millisInDB: Long? = null

        val merchantId = order.result?.merchantId
        val orderNumber = order.result?.orderNumber
        if (merchantId != null && orderNumber != null) {
            getOrderAsDB(orderNumber, merchantId)?.let {
                millisInDB = it.dateMillis
            }
        }
        insertOrder(
            mapToDBOrder(order).apply {
                if (dateMillis == null)
                    dateMillis = millisInDB ?: Calendar.getInstance().timeInMillis
            }
        )
    }

    @Transaction
    open suspend fun deleteOrder(order: AssistResult) {
        val merchantId = order.result?.merchantId
        val orderNumber = order.result?.orderNumber

        if (merchantId != null && orderNumber != null) {
            deleteOrder("${merchantId}_$orderNumber")
        }
    }

    @RawQuery
    protected abstract fun getOrders(query: SimpleSQLiteQuery): List<AssistOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertOrder(order: AssistOrder)

    @Query("DELETE FROM $DB_TABLE_ORDERS WHERE ${BaseColumns._ID} = :id")
    protected abstract fun deleteOrder(id: String)
}