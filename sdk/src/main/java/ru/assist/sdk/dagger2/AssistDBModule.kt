package ru.assist.sdk.dagger2

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.assist.sdk.storage.AssistDatabase
import javax.inject.Singleton

@Module
internal class AssistDBModule {
    @Provides
    @Singleton
    fun provideDB(application: Application)
        = Room.databaseBuilder(
            application,
            AssistDatabase::class.java,
            "assist-database"
        )
            .fallbackToDestructiveMigration()
            .build()
            .orderDao()
}