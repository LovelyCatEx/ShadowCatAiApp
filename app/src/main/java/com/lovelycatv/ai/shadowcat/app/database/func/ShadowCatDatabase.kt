package com.lovelycatv.ai.shadowcat.app.database.func

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lovelycatv.ai.shadowcat.app.database.func.entity.MessageEntity
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.database.func.mapper.MessageDAO
import com.lovelycatv.ai.shadowcat.app.database.func.mapper.SessionDAO
import com.lovelycatv.ai.shadowcat.app.util.common.runAsync
import com.lovelycatv.ai.shadowcat.app.util.android.runInTransactionAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

@Database(
    entities = [SessionEntity::class, MessageEntity::class],
    version = 1
)
abstract class ShadowCatDatabase : RoomDatabase() {
    companion object {
        private var instance: ShadowCatDatabase? = null

        @JvmStatic
        fun getInstance(applicationContext: Context, userId: Long): ShadowCatDatabase {
            if (this.instance == null) {
                this.instance = Room.databaseBuilder(
                    applicationContext,
                    ShadowCatDatabase::class.java,
                    "data-$userId"
                ).build()
            }

            return this.instance!!
        }
    }

    abstract fun sessionDAO(): SessionDAO
    abstract fun messageDAO(): MessageDAO

    /**
     * This function will delete the local session and all messages of it by sessionId
     * Also this function will run in global async,
     * If you want to specify the scope, set the param coroutineScope to you want
     *
     * @param sessionId sessionId
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun deleteLocalSession(sessionId: String, coroutineScope: CoroutineScope = GlobalScope) {
        runInTransactionAsync(coroutineScope) {
            messageDAO().deleteBySessionId(sessionId)
            sessionDAO().delete(SessionEntity.primaryKeyOnly(sessionId))
        }
    }

    /**
     * This function will clear all local history messages of the session.
     * Also this function will run in global async,
     * if you want to specify the scope, set the param coroutineScope to you want
     *
     * @param sessionId sessionId
     * @param coroutineScope coroutineScope
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun clearLocalHistoryMessages(sessionId: String, coroutineScope: CoroutineScope = GlobalScope) {
        runAsync(coroutineScope) {
            messageDAO().deleteBySessionId(sessionId)
        }
    }
}