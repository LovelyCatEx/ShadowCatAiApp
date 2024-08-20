package com.lovelycatv.ai.shadowcat.app.util.android

import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RoomExtensions {
}

@OptIn(DelicateCoroutinesApi::class)
fun <T : RoomDatabase> T.runInTransactionAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatchers: CoroutineDispatcher = Dispatchers.IO,
    fx: suspend (T) -> Unit
) {
    this.runInTransaction {
        coroutineScope.launch(dispatchers) {
            fx(this@runInTransactionAsync)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun <T : RoomDatabase> T.runAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatchers: CoroutineDispatcher = Dispatchers.IO,
    fx: suspend (T) -> Unit
) {
    coroutineScope.launch(dispatchers) {
        fx(this@runAsync)
    }
}