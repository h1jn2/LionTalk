package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import com.example.liontalk.data.local.datasource.PreferenceDataStore
import com.example.liontalk.model.ChatUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

class UserPreferenceRepository private constructor(private val context: Context){
    private val _user = MutableStateFlow<ChatUser?>(null)
    val user: StateFlow<ChatUser?> = _user
    val meOrNull: ChatUser? get() = _user.value

    fun requireMe(): ChatUser = requireNotNull(_user.value)

    val isInitialized: Boolean get() = _user.value != null

    suspend fun loadUserFromStorage() {
        val name = PreferenceDataStore.getString(context, "USER_NAME").first()
        val avatarUrl = PreferenceDataStore.getString(context, "AVATAR_URL").first()

        if (!name.isNullOrBlank()) {
            _user.value = ChatUser(name, avatarUrl)
        }
    }

    suspend fun setUser(user: ChatUser) {
        PreferenceDataStore.setString(context, "USER_NAME", user.name)
        user.avatarUrl?.let {
            PreferenceDataStore.setString(context, "AVATAR_URL", it)
        }
    }

    companion object {
        @Volatile
        private var _instance: UserPreferenceRepository? = null

        fun init(context: Context) {
            if (_instance == null) {
                synchronized(this) {
                    if (_instance == null) {
                        _instance = UserPreferenceRepository(context.applicationContext)
                        Log.d("UserPrefRepo", "UserPreferenceRepository initialized with ${context.applicationContext}")
                    }
                }
            }
        }

        fun getInstance(): UserPreferenceRepository {
            return _instance ?: throw IllegalStateException("UserPreferenceRepository not initialized")
        }
    }
}