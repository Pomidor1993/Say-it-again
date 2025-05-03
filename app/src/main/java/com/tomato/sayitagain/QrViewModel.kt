package com.tomato.sayitagain

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.media3.common.util.UnstableApi
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom


@UnstableApi
class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val _allQrCodes = mutableStateListOf<QrCodeData>()
    val isLoaded = mutableStateOf(false)
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
            val jsonString = withContext(Dispatchers.IO) {
                getApplication<Application>().resources
                    .openRawResource(R.raw.valid_qr_codesnew)
                    .bufferedReader()
                    .use { it.readText() }
            }
            Log.d("QrViewModel", "JSON starts with: ${jsonString.take(50)}")

            val parsed = mutableListOf<QrCodeData>()
            try {
                val rootElement = JsonParser.parseString(jsonString)
                val array: JsonArray = when {
                    rootElement.isJsonArray -> rootElement.asJsonArray
                    rootElement.isJsonObject -> {
                        rootElement.asJsonObject.entrySet()
                            .firstOrNull { it.value.isJsonArray }
                            ?.value
                            ?.asJsonArray
                            ?: throw IllegalStateException("Nie znalazłem tablicy w wrapperze JSON")
                    }
                    else -> throw IllegalStateException("JSON nie jest ani tablicą, ani obiektem")
                }

                // iteruj po każdym elemencie i konstruuj QrCodeData „ręcznie”
                array.forEachIndexed { idx, je ->
                    try {
                        val obj = je.asJsonObject
                        val code = obj.get("code").asString
                        val group = obj.get("group").asString
                        val language = obj.get("language").asString
                        val title = obj.get("title").asString
                        val titlePL = if (obj.has("titlePL") && !obj.get("titlePL").isJsonNull)
                            obj.get("titlePL").asString
                        else null

                        val commentJson = obj.getAsJsonArray("comment")
                        val commentList = commentJson.map { it.asString }

                        parsed += QrCodeData(code, group, language, title, titlePL, commentList)

                    } catch (e: Exception) {
                        Log.w("QrViewModel", "Błąd parsowania elementu #$idx", e)
                        // nie przerywamy pętli, po prostu pomijamy ten element
                    }
                }

                _allQrCodes.clear()
                _allQrCodes.addAll(parsed)
                _availableGroups.value = _allQrCodes.map { it.group }.toSet()

                Log.d("QrViewModel", "Załadowałem ${parsed.size} kodów QR z ${array.size()} elementów.")

                _availableGroups.value = _allQrCodes.map { it.group }.toSet()
                // AFTER loading data:
                isLoaded.value = true
            }
            catch (e: Exception) {
                // zdarzy się tylko, jeśli JSON jest kompletnie nie w formacie tablicy lub obiektu
                Log.e("QrViewModel", "Błąd parsowania JSON: ${e.javaClass.simpleName}")
                isLoaded.value = true
            }
        }
    }



    fun getQrCodeData(rawValue: String): QrCodeData? {
        // 1. Najpierw sprawdź protokół
        if (!rawValue.startsWith("sayitagain://")) {
            Log.w("QR_SECURITY", "Nieprawidłowy protokół: $rawValue")
            return null
        }

        // 2. Przetwórz ścieżkę
        val path = rawValue
            .removePrefix("sayitagain://")
            .replace("/", "%2F")

        // 3. Sprawdź iniekcję ścieżki
        if (path.contains("..")) {
            Log.e("QR_SECURITY", "Wykryto iniekcję ścieżki: $path")
            Firebase.crashlytics.recordException(SecurityException("Path traversal attempt"))
            return null
        }

        // 4. Sprawdź regex
        if (!path.matches(Regex("[a-zA-Z0-9%\\-_.~]+"))) {
            Log.e("QR_SECURITY", "Niedozwolone znaki w ścieżce: $path")
            return null
        }

        // 5. Znajdź QR w bazie
        return _allQrCodes.firstOrNull { qr ->
            qr.code.equals(path, ignoreCase = true)
        }
    }



    override fun onCleared() {
        super.onCleared()
        mediaPlayerHelper.release()
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return QrViewModel(application) as T
            }
        }
    }


    fun getRandomUnusedQrCode(): QrCodeData? {
        val eligibleQrCodes = _allQrCodes
            .filter {
                _selectedGroups.value.contains(it.group) &&
                        _selectedLanguages.value.contains(it.language)
            }
            .filterNot { _usedQrCodes.contains(it) }

        if (eligibleQrCodes.isEmpty()) return null

        return try {
            // Użyj najsilniejszego dostępnego algorytmu
            val secureRandom = SecureRandom.getInstanceStrong()
            // Dodaj losowe ziarno (np. z systemowego źródła entropii)
            secureRandom.nextBytes(ByteArray(16))

            val randomIndex = secureRandom.nextInt(eligibleQrCodes.size)
            val selectedQr = eligibleQrCodes[randomIndex]
            _usedQrCodes.add(selectedQr)
            selectedQr
        } catch (e: NoSuchAlgorithmException) {
            // Fallback na domyślny SecureRandom
            Log.e("QR_SECURITY", "Błąd SecureRandom: ${e.message}")
            null
        }
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