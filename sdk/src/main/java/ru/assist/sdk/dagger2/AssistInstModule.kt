package ru.assist.sdk.dagger2

import android.app.Application
import dagger.Module
import dagger.Provides
import ru.assist.sdk.identification.InstallationInfo
import javax.inject.Singleton

@Module
internal class AssistInstModule {
    @Provides
    @Singleton
    fun provideInstInfo(application: Application) = InstallationInfo.init(application)
}