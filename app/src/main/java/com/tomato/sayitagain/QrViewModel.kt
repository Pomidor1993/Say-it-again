package com.tomato.sayitagain

import android.app.Application
import android.content.Context
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
import java.net.URLDecoder
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
                    }
                }

                _allQrCodes.clear()
                _allQrCodes.addAll(parsed)
                _availableGroups.value = _allQrCodes.map { it.group }.toSet()
                Log.d("QrViewModel", "Załadowałem ${parsed.size} kodów QR z ${array.size()} elementów.")
                isLoaded.value = true
            } catch (e: Exception) {
                Log.e("QrViewModel", "Błąd parsowania JSON: ${e.javaClass.simpleName}")
                isLoaded.value = true
            }
        }
    }

    /**
     * Zwraca QrCodeData dla zeskanowanego ciągu (sayitagain://...)
     * oraz dokonuje dekodowania pola code (%2F → /) w zwróconym obiekcie.
     */
    fun getQrCodeData(rawValue: String): QrCodeData? {
        // 1. Sprawdź prefiks
        if (!rawValue.startsWith("sayitagain://")) {
            Log.w("QR_SECURITY", "Nieprawidłowy protokół: $rawValue")
            return null
        }

        // 2. Wyodrębnij i dekoduj ścieżkę po schemacie
        val rawPath = rawValue.removePrefix("sayitagain://")

        if (rawPath.contains("..")) {
            Log.e("QR_SECURITY", "Wykryto iniekcję ścieżki: $rawPath")
            Firebase.crashlytics.recordException(SecurityException("Path traversal attempt"))
            return null
        }

        if (!rawPath.matches(Regex("[a-zA-Z0-9/\\-_.~]+"))) {
            Log.e("QR_SECURITY", "Niedozwolone znaki w ścieżce: $rawPath")
            return null
        }

        val match = _allQrCodes.firstOrNull { qr ->
            val decoded = URLDecoder.decode(qr.code, "UTF-8")
            decoded.equals(rawPath, ignoreCase = true)
        } ?: return null

        return match.copy(code = rawPath)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayerHelper.release()
    }

    fun getRandomUnusedQrCode(): QrCodeData? {
        val eligible = _allQrCodes
            .filter { _selectedGroups.value.contains(it.group) && _selectedLanguages.value.contains(it.language) }
            .filterNot { _usedQrCodes.contains(it) }

        if (eligible.isEmpty()) return null

        return try {
            val secureRandom = SecureRandom.getInstanceStrong()
            secureRandom.nextBytes(ByteArray(16))
            val index = secureRandom.nextInt(eligible.size)
            val selected = eligible[index]
            _usedQrCodes.add(selected)
            val decodedCode = URLDecoder.decode(selected.code, "UTF-8")
            selected.copy(code = decodedCode)
        } catch (e: NoSuchAlgorithmException) {
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

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return QrViewModel(application) as T
            }
        }
    }
}