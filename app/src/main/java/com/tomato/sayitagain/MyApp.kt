package com.tomato.sayitagain

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1) Init Firebase
        FirebaseApp.initializeApp(this)
        Log.d("MyApp", "Firebase initialized")

        // 2) Wybierz factory w zależności od builda
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val factory = if (isDebug)
            DebugAppCheckProviderFactory.getInstance()
        else
            PlayIntegrityAppCheckProviderFactory.getInstance()

        // 3) Pobierz instancję i zainstaluj provider
        val appCheck = FirebaseAppCheck.getInstance()
        appCheck.installAppCheckProviderFactory(factory)
        // 4) I TU w Java API ustaw auto‑refresh
        appCheck.setTokenAutoRefreshEnabled(true)

        Log.d(
            "MyApp",
            "App Check zainstalowany: ${if (isDebug) "DEBUG provider" else "PLAY_INTEGRITY"}, auto-refresh ON"
        )
    }
}
