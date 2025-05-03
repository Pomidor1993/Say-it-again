package com.tomato.sayitagain

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.crashlytics.crashlytics

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Lepsza inicjalizacja Firebase z obsługą błędów
        try {
            val isDebug = isDebuggable(this)
            Log.d("AppCheck", "Initializing in ${if (isDebug) "DEBUG" else "RELEASE"} mode")

            // Inicjalizacja Firebase
            FirebaseApp.initializeApp(this)
            Firebase.crashlytics.isCrashlyticsCollectionEnabled = true // Włącz zbieranie błędów


            // Konfiguracja AppCheck z cache'owaniem
            setupAppCheck()

        } catch (e: Exception) {
            Log.e("MyApp", "Firebase initialization failed", e)
            // Tutaj możesz dodać crash reporting
        }
    }

    private fun setupAppCheck() {
        val factory = PlayIntegrityAppCheckProviderFactory.getInstance()

        FirebaseAppCheck.getInstance().apply {
            installAppCheckProviderFactory(factory)
            setTokenAutoRefreshEnabled(true)
        }
    }

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}