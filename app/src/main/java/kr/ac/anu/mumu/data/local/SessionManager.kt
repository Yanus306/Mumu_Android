package kr.ac.anu.mumu.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val accessToken: String?
        get() = preferences.getString(KEY_ACCESS_TOKEN, null)

    val refreshToken: String?
        get() = preferences.getString(KEY_REFRESH_TOKEN, null)

    val selectedPetId: Long?
        get() = preferences.getLong(KEY_SELECTED_PET_ID, -1L).takeIf { it > 0L }

    fun selectPet(petId: Long?) {
        preferences.edit().apply {
            if (petId == null) remove(KEY_SELECTED_PET_ID) else putLong(KEY_SELECTED_PET_ID, petId)
        }.apply()
    }

    fun saveTokens(accessToken: String, refreshToken: String, tokenType: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_TOKEN_TYPE, tokenType)
            .apply()
    }

    fun clearTokens() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).remove(KEY_TOKEN_TYPE)
            .remove(KEY_SELECTED_PET_ID)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "mumu_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_TYPE = "token_type"
        const val KEY_SELECTED_PET_ID = "selected_pet_id"
    }
}
