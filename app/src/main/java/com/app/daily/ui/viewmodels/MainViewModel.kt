package com.app.daily.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.daily.data.repository.SharedPreferencesRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val sharedPreferencesRepositoryImpl: SharedPreferencesRepositoryImpl
) : ViewModel() {
    private val _themeMode = MutableLiveData<Int>()
    val themeMode: LiveData<Int> = _themeMode

    var voiceLocale:String = Locale.getDefault().toString()

    var selectedThemeMode = 0

    fun setTheme(mode: Int) {
        try {
            sharedPreferencesRepositoryImpl.setThemeMode(mode)
            _themeMode.value = mode
            selectedThemeMode = mode
        } catch (_: Exception) {

        }
    }

    fun setLocale(newLocale:String){
        sharedPreferencesRepositoryImpl.setVoiceLocale(newLocale)
        voiceLocale=newLocale
    }

    init{
        setTheme(sharedPreferencesRepositoryImpl.getThemeMode())

        val locale = sharedPreferencesRepositoryImpl.getVoiceLocale()
        if(locale.isNotBlank() && locale != "null"){
            setLocale(sharedPreferencesRepositoryImpl.getVoiceLocale())
        }
    }
}