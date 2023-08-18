package com.app.daily.domain.repositories

interface SharedPreferencesRepository {
    fun setFirstLaunch()

    fun isFirstLaunch():Boolean

    fun setUserId(userId:String)

    fun getUserId():String?

    fun setThemeMode(themeMode:Int)

    fun getThemeMode():Int

    fun setVoiceLocale(locale:String)
    fun getVoiceLocale():String
}