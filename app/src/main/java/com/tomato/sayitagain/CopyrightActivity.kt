package com.tomato.sayitagain

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CopyrightActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        // Ustawienie Toolbar jako ActionBar i strzałki wstecz
        val toolbar: Toolbar = findViewById(R.id.toolbarPolicy)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener { finish() }

        // Ładowanie pliku HTML
        val webView: WebView = findViewById(R.id.webViewPolicy)
        webView.settings.javaScriptEnabled = false
        val html = resources.openRawResource(R.raw.copyright_notice)
            .bufferedReader().use { it.readText() }
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }
}
