package ru.assist.demo.ui

import android.os.Bundle
import android.view.View
import ru.assist.demo.R
import ru.assist.demo.ui.base.BaseActivity
import ru.assist.demo.databinding.ActivityResultBinding
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.models.AssistResult
import ru.assist.sdk.models.Configuration

class ResultActivity : BaseActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var sdk: AssistSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdk = AssistSDK.getInstance().configure(this, Configuration())

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        progressView = binding.progress
        intent.extras?.getParcelable<AssistResult>("result")?.let { result ->
            parseAndFill(result)
            binding.btRefresh.visibility = View.VISIBLE
            binding.btRefresh.setOnClickListener {
                showProgress(true)
                sdk.getOrderDataByNumber(this, result, ::parseAndFill)
            }
        }
        binding.btBack.setOnClickListener {
            finish()
        }
    }

    private fun parseAndFill(result: AssistResult) {
        result.result?.let {
            binding.tvResultStatus.text = it.orderState.toString()
            binding.tvResultOrderNumber.text = it.orderNumber
            binding.tvResultOrderAmount.text = getString(R.string.withSpace, it.amount, it.currency)
            binding.tvResultOrderComment.text = it.comment
            binding.tvResultExtInfo.text = getString(
                R.string.withSpace,
                it.approvalCode ?: "",
                it.extraInfo ?: ""
            )
        }
        result.error?.let {
            showToast(it.msg)
        }
        showProgress(false)
    }
}