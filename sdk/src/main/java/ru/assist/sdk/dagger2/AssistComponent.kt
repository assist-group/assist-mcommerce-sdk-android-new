package ru.assist.sdk.dagger2

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import ru.assist.sdk.AssistSDK
import ru.assist.sdk.engine.WebProcessor
import ru.assist.sdk.ui.PayActivity
import ru.assist.sdk.ui.WebViewActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [
	AssistNetModule::class,
	AssistDBModule::class,
	AssistInstModule::class,
	AssistEngineModule::class
])
internal interface AssistComponent {
	fun inject(webProcessor: WebProcessor)
	fun inject(sdk: AssistSDK)
	fun inject(webViewActivity: WebViewActivity)
	fun inject(payActivity: PayActivity)

	@Component.Builder
	interface Builder {
		@BindsInstance
		fun application(application: Application): Builder
		@BindsInstance
		fun apiUrl(apiUrl: String): Builder
		@BindsInstance
		fun storageEnabled(storageEnabled: Boolean): Builder
		fun build(): AssistComponent
	}
}