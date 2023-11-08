package ru.assist.demo.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
	private val tag = "TAG_${javaClass.simpleName}"

	protected lateinit var progressView: View

	fun showToast(@StringRes resId: Int, vararg fields: String) {
		showToast(getString(resId, *fields))
	}

	fun showToast(message: String?) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
	}

	fun log(message: String?) {
		Log.d(tag, message.orEmpty())
	}

	fun logE(message: String?) {
		Log.e(tag, message.orEmpty())
	}

	fun logE(message: String?, t: Throwable) {
		Log.e(tag, message.orEmpty(), t)
	}

	fun showProgress(show: Boolean) {
		val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
		progressView.visibility = if (show) View.VISIBLE else View.GONE
		progressView.animate()
			.setDuration(shortAnimTime.toLong())
			.alpha(if (show) 1f else 0f)
			.setListener(object : AnimatorListenerAdapter() {
				override fun onAnimationEnd(animation: Animator) {
					progressView.visibility = if (show) View.VISIBLE else View.GONE
				}
			})
	}
}