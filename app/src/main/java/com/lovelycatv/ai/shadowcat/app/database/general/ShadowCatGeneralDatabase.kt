package com.lovelycatv.ai.shadowcat.app.database.general

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lovelycatv.ai.shadowcat.app.database.general.entity.AccountEntity
import com.lovelycatv.ai.shadowcat.app.database.general.entity.ServerConnectionEntity
import com.lovelycatv.ai.shadowcat.app.database.general.entity.SettingEntity
import com.lovelycatv.ai.shadowcat.app.database.general.mapper.AccountDAO
import com.lovelycatv.ai.shadowcat.app.database.general.mapper.ServerConnectionDAO
import com.lovelycatv.ai.shadowcat.app.database.general.mapper.SettingDAO
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants
import com.lovelycatv.ai.shadowcat.app.util.common.runOnMain
import com.lovelycatv.ai.shadowcat.app.viewmodel.GlobalViewModel

@Database(
    entities = [AccountEntity::class, ServerConnectionEntity::class, SettingEntity::class],
    version = 1
)
abstract class ShadowCatGeneralDatabase : RoomDatabase() {
    companion object {
        @JvmStatic
        private var instance: ShadowCatGeneralDatabase? = null

        @JvmStatic
        fun getInstance(applicationContext: Context): ShadowCatGeneralDatabase {
            if (this.instance == null) {
                this.instance = Room.databaseBuilder(
                    applicationContext,
                    ShadowCatGeneralDatabase::class.java,
                    "shadow-cat-app"
                ).allowMainThreadQueries().fallbackToDestructiveMigration().build()
            }

            return this.instance!!
        }
    }

    abstract fun accountDAO(): AccountDAO
    abstract fun serverConnectionDAO(): ServerConnectionDAO
    abstract fun settingDAO(): SettingDAO

    fun changeAccount(account: AccountEntity) {
        updateSettingItem(GlobalConstants.SETTING_CONNECTED_SERVER_ID, account.serverId.toString())
        updateSettingItem(GlobalConstants.SETTING_CONNECTED_ACCOUNT_ID, account.id.toString())
        updateSettingItem(GlobalConstants.SETTING_CURRENT_USER_ID, account.userId.toString())
        updateSettingItem(GlobalConstants.SETTING_CURRENT_TOKEN, "")
    }

    fun updateAvatarOfCurrentUser(newAvatar: String, globalViewModel: GlobalViewModel? = null) {
        val account = accountDAO().getAccountById(getSettingItem(GlobalConstants.SETTING_CONNECTED_ACCOUNT_ID)?.toInt() ?: -1)
        if (account == null) {
            return
        }
        account.avatar = newAvatar
        accountDAO().update(account)

        runOnMain {
            globalViewModel?.setUserMeta(account)
        }
    }

    fun logout() {
        updateSettingItem(GlobalConstants.SETTING_CONNECTED_SERVER_ID, "")
        updateSettingItem(GlobalConstants.SETTING_CONNECTED_ACCOUNT_ID, "")
        updateSettingItem(GlobalConstants.SETTING_CURRENT_USER_ID, "")
        updateSettingItem(GlobalConstants.SETTING_CURRENT_TOKEN, "")
    }
}

fun ShadowCatGeneralDatabase.getSettingItem(key: String): SettingEntity? {
    return this.settingDAO().getSettingItem(key)
}

fun ShadowCatGeneralDatabase.updateSettingItem(key: String, value: String) {
    with(getSettingItem(key)) {
        if (this == null) {
            this@updateSettingItem.settingDAO().insert(SettingEntity(0, key, value))
        } else {
            this@updateSettingItem.settingDAO().update(this.apply {
                this.value = value
            })
        }
    }
}