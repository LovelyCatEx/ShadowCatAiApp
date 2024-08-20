package com.lovelycatv.ai.shadowcat.app.activity.session.creator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lovelycatv.ai.shadowcat.app.database.func.entity.SessionEntity
import com.lovelycatv.ai.shadowcat.app.net.response.remote.ModelResult
import com.lovelycatv.ai.shadowcat.app.net.retrofit.getShadowCatServerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SessionCreatorViewModel : ViewModel() {
    private val _models = MutableLiveData<List<ModelResult>>()
    val models: LiveData<List<ModelResult>> get() = _models

    fun setModels(newData: List<ModelResult>) {
        this._models.postValue(newData)
    }

    private val _selectedModel = MutableLiveData<ModelResult>()
    val selectedModel: LiveData<ModelResult> get() = _selectedModel

    fun setSelectedModel(newData: ModelResult) {
        this._selectedModel.value = newData
    }

    private val _sessionName = MutableLiveData<String>()
    val sessionName: LiveData<String> get() = _sessionName

    fun setSessionName(newData: String) {
        this._sessionName.value = newData
    }

    private val _editingSessionEntity = MutableLiveData<SessionEntity>(null)
    val editingSessionEntity: LiveData<SessionEntity> get() = _editingSessionEntity

    fun setCurrentEditingEntity(entity: SessionEntity) {
        this._editingSessionEntity.postValue(entity)
    }

    suspend fun fetchModels(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            val pair = getShadowCatServerApi()
            val api = pair.second

            val response = api.getModels(token)
            if (response.isSuccessful) {
                val arrayData = response.body()!!.getExplicitArrayData(ModelResult::class.java)
                setModels(arrayData ?: emptyList())
                true
            } else {
                false
            }
        }
    }
}