package com.app.daily.data.repository

import android.content.SharedPreferences
import com.app.daily.domain.repositories.SharedPreferencesRepository
import com.app.daily.utils.Constants.FIRST_LAUNCH
import com.app.daily.utils.Constants.USER_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SharedPreferencesRepository {

    override fun setFirstLaunch() {
        sharedPreferences.edit().apply {
            putBoolean(FIRST_LAUNCH, false)
            apply()
        }
    }

    override fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(FIRST_LAUNCH, true)
    }

    override fun setUserId(userId:String) {
        sharedPreferences.edit().apply {
            putString(USER_ID, userId)
            apply()
        }
    }

    override fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID, "")
    }

}