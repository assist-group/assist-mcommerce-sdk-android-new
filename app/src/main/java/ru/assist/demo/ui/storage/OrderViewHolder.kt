package ru.assist.demo.ui.storage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.assist.demo.R
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.models.OrderState
import java.text.SimpleDateFormat
import java.util.Locale

class OrderViewHolder(
    private val onClick: (AssistResult) -> Unit,
    parent: ViewGroup
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.list_item_order, parent, false)
) {

    private val tvOrderTime: TextView = itemView.findViewById(R.id.tvOrderTime)
    private val tvTotal: TextView = itemView.findViewById(R.id.tvTotal)
    private val tvCurrency: TextView = itemView.findViewById(R.id.tvCurrency)
    private val tvOrderNumber: TextView = itemView.findViewById(R.id.tvOrderNumber)

    fun bind(model: AssistResult) {
        val millis = model.result?.dateMillis
        tvOrderTime.text = if (millis != null)
            SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US).format(millis)
        else "-"
        tvTotal.text = model.result?.amount
        tvCurrency.text = model.result?.currency
        tvOrderNumber.text = model.result?.orderNumber

        itemView.setBackgroundColor(when (model.result?.orderState) {
            OrderState.APPROVED -> Color.parseColor("#8AE18A")
            OrderState.IN_PROCESS -> Color.parseColor("#dce158")
            OrderState.UNKNOWN -> Color.parseColor("#E18A8A")
            OrderState.CANCEL_ERROR -> Color.parseColor("#FF0000")
            else -> Color.parseColor("#777777")
        })

        itemView.setOnClickListener { onClick(model) }
    }
}