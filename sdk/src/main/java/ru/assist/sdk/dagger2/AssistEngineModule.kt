package ru.assist.sdk.dagger2

import dagger.Module
import dagger.Provides
import ru.assist.sdk.api.AssistApi
import ru.assist.sdk.engine.Engine
import ru.assist.sdk.identification.InstallationInfo
import ru.assist.sdk.storage.OrderDao
import javax.inject.Singleton

@Module
internal class AssistEngineModule {
    @Provides
    @Singleton
    fun provideEngine(api: AssistApi, storage: OrderDao, instInfo: InstallationInfo)
        = Engine.init(api, storage, instInfo)

    @Provides
    @Singleton
    fun provideWebProcessor(engine: Engine) = engine.webProcessor!!
}