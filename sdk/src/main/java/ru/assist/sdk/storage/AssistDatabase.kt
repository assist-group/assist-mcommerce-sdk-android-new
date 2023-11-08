package ru.assist.sdk.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.assist.sdk.storage.models.AssistOrder

@Database(
    entities = [
        AssistOrder::class
    ],
    version = 1,
    exportSchema = false
)
internal abstract class AssistDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}