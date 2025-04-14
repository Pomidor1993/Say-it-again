package com.example.sayitagain

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import androidx.media3.common.util.UnstableApi

@UnstableApi
class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val _allQrCodes = mutableStateListOf<QrCodeData>()
    private val _usedQrCodes = mutableStateListOf<QrCodeData>()
    private val _selectedGroups = mutableStateOf(setOf("Movie"))
    private val _availableGroups = mutableStateOf<Set<String>>(emptySet())
    private val _selectedLanguages = mutableStateOf(setOf("ENG", "PL"))

    val mediaPlayerHelper = MediaPlayerHelper(application)

    init {
        loadQrCodes()
    }

    private fun loadQrCodes() {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().resources.openRawResource(R.raw.valid_qr_codesnew)
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<QrCodeData>>() {}.type
                _allQrCodes.addAll(Gson().fromJson(jsonString, type))
                _availableGroups.value = _allQrCodes.map { it.group }.toSet()
            } catch (e: Exception) {
                Log.e("QrViewModel", "Błąd ładowania kodów", e)
            }
        }
    }

    fun getQrCodeData(code: String): QrCodeData? {
        return _allQrCodes.firstOrNull { it.code == code }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayerHelper.release()
    }

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                QrViewModel(application)
            }
        }
    }

    fun getRandomUnusedQrCode(): QrCodeData? {
        return _allQrCodes
            .filter {
                _selectedGroups.value.contains(it.group) &&
                        _selectedLanguages.value.contains(it.language)
            }
            .filterNot { _usedQrCodes.contains(it) }
            .randomOrNull()
            ?.also { _usedQrCodes.add(it) }
    }

    fun resetUsedQrCodes() {
        _usedQrCodes.clear()
    }

    fun setSelectedGroups(groups: Set<String>) {
        _selectedGroups.value = groups
    }

    fun setSelectedLanguages(languages: Set<String>) {
        _selectedLanguages.value = languages
    }
}