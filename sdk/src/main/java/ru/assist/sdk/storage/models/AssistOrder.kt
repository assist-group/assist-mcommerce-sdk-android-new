package ru.assist.sdk.storage.models

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.assist.sdk.models.OrderState
import ru.assist.sdk.storage.OrderDao

@Entity(tableName = OrderDao.DB_TABLE_ORDERS)
data class AssistOrder(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = BaseColumns._ID)
    var merchantIdOrderNumber: String = "",

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_MID)
    var merchantId: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_STATE)
    var orderState: OrderState? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_APPROVAL_CODE)
    var approvalCode: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_BILL_NUMBER)
    var billNumber: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_EXTRA_INFO)
    var extraInfo: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_NUMBER)
    var orderNumber: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_AMOUNT)
    var amount: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_CURRENCY)
    var currency: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_COMMENT)
    var comment: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_EMAIL)
    var email: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_FIRST_NAME)
    var firstName: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_LAST_NAME)
    var lastName: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_MIDDLE_NAME)
    var middleName: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_USER_SIGNATURE)
    var signature: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_CHECK_VALUE)
    var checkValue: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_MEANTYPENAME)
    var meanTypeName: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_MEANNUMBER)
    var meanNumber: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_CARDHOLDER)
    var cardholder: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_CARDEXPIRATIONDATE)
    var cardExpirationDate: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_ITEMS_JSON)
    var chequeItems: String? = null,

    @ColumnInfo(name = OrderDao.COLUMN_ORDER_DATE_DEVICE_MILLIS)
    var dateMillis: Long? = null
) {
    companion object {
        internal fun createMerchantIdOrderNumber(merchantId: String, orderNumber: String)
            = "${merchantId}_$orderNumber"
    }
}