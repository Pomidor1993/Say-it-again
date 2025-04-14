package com.example.sayitagain.ui.theme

fun calculateSimilarity(answer: String, title: String, titlePL: String?): Double {
    val normalizedAnswer = answer.normalizeForComparison()

    val similarityTitle = calculateLevenshteinSimilarity(normalizedAnswer, title.normalizeForComparison())
    val similarityTitlePL = titlePL?.let {
        calculateLevenshteinSimilarity(normalizedAnswer, it.normalizeForComparison())
    } ?: 0.0

    return maxOf(similarityTitle, similarityTitlePL)
}

private fun calculateLevenshteinSimilarity(s1: String, s2: String): Double {
    val distance = levenshteinDistance(s1, s2)
    val maxLength = maxOf(s1.length, s2.length)
    return 1.0 - (distance.toDouble() / maxLength.toDouble())
}

fun String.normalizeForComparison(): String {
    return this
        .lowercase()
        .replace("[^a-z0-9]".toRegex(), "")
}

private fun levenshteinDistance(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

    for (i in 0..s1.length) {
        for (j in 0..s2.length) {
            when {
                i == 0 -> dp[i][j] = j
                j == 0 -> dp[i][j] = i
                else -> dp[i][j] = minOf(
                    dp[i - 1][j - 1] + if (s1[i - 1] == s2[j - 1]) 0 else 1,
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1
                )
            }
        }
    }
    return dp[s1.length][s2.length]
}