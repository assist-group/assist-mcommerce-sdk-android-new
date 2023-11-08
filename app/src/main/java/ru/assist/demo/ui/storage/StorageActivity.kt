package ru.assist.demo.ui.storage

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.assist.demo.R
import ru.assist.demo.databinding.ActivityStorageBinding
import ru.assist.demo.ui.ResultActivity
import ru.assist.demo.ui.base.BaseActivity
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.exception.AssistSdkException
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.models.Configuration

class StorageActivity : BaseActivity() {
    private var displayDensity = 0f

    private lateinit var binding: ActivityStorageBinding
    private lateinit var sdk: AssistSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayDensity = resources.displayMetrics.density
        sdk = AssistSDK.getInstance().configure(this, Configuration())

        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        val orderAdapter = OrderAdapter { onOrderClicked(it) }
        binding.listOfInputOrders.apply {
            adapter = orderAdapter
        }
        lifecycleScope.launch {
            try {
                orderAdapter.setItems(sdk.getOrdersFromStorage())
            } catch (e: AssistSdkException) {
                showToast(e.message)
            }
        }
        setSwipeToDelete()
    }

    private fun setSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.listOfInputOrders.adapter as OrderAdapter
                val position = viewHolder.adapterPosition
                val selected = adapter.getItem(position)

                lifecycleScope.launch {
                    sdk.deleteOrderInStorage(selected)
                }
                adapter.removeItem(position)
                showToast(getString(R.string.order_is_deleted, selected.result?.orderNumber))
            }
        }).attachToRecyclerView(binding.listOfInputOrders)
    }

    private fun onOrderClicked(order: AssistResult) =
        startActivity(
            Intent(this, ResultActivity::class.java).putExtra("result", order)
        )
}