package com.example.sayitagain

data class QrCodeData(
    val code: String,
    val group: String,
    val language: String,
    val title: String,
    val titlePL: String? = null,
    val comment: List<String>
)