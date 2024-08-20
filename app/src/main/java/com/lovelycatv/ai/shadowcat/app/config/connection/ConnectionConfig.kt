package com.lovelycatv.ai.shadowcat.app.config.connection

import android.app.Activity
import com.lovelycatv.ai.shadowcat.app.config.base.AbstractInDatabaseConfig
import com.lovelycatv.ai.shadowcat.app.exception.SettingsItemNotExistException
import com.lovelycatv.ai.shadowcat.app.util.GlobalConstants


class ConnectionConfig(activity: Activity) : AbstractInDatabaseConfig<ConnectionSettings>(activity) {

    /**
     * This function may produce exception when there are no connection settings
     *
     * @return ConnectionSettings
     */
    override fun getSettings(): ConnectionSettings {
        val settingDAO = getDatabase().settingDAO()
        val serverConnectionDAO = getDatabase().serverConnectionDAO()
        val accountDAO = getDatabase().accountDAO()

        val serverId = with(GlobalConstants.SETTING_CONNECTED_SERVER_ID) {
            settingDAO.getSettingItem(this)?.toInt() ?: throw SettingsItemNotExistException(this)
        }

        val accountId = with(GlobalConstants.SETTING_CONNECTED_ACCOUNT_ID) {
            settingDAO.getSettingItem(this)?.toInt()  ?: throw SettingsItemNotExistException(this)

        }

        val settings = ConnectionSettings(
            serverConnectionDAO.getServerById(serverId)!!,
            accountDAO.getAccountById(accountId)!!
        )


        return settings
    }

    /**
     * Actually, any configuration object has getDatabase() could do this thing.
     *
     * @return Saved user token
     */
    fun getToken(): String {
        return getDatabase().settingDAO().getSettingItem(GlobalConstants.SETTING_CURRENT_TOKEN)?.stringValue() ?: ""
    }

    fun getUserId(): Long {
        return getDatabase().settingDAO().getSettingItem(GlobalConstants.SETTING_CURRENT_USER_ID)?.toLong() ?: 0
    }
}