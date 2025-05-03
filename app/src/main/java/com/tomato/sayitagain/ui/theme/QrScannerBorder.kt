package com.tomato.sayitagain.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun QrScannerBorder() {
    val cornerLength = 40.dp
    val strokeWidth = 4.dp
    val color = Color.LightGray

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Lewy górny róg
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(cornerLength.toPx(), 0f),
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(0f, cornerLength.toPx()),
            strokeWidth = strokeWidth.toPx()
        )

        // Prawy górny róg
        drawLine(
            color = color,
            start = Offset(canvasWidth, 0f),
            end = Offset(canvasWidth - cornerLength.toPx(), 0f),
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            color = color,
            start = Offset(canvasWidth, 0f),
            end = Offset(canvasWidth, cornerLength.toPx()),
            strokeWidth = strokeWidth.toPx()
        )

        // Lewy dolny róg
        drawLine(
            color = color,
            start = Offset(0f, canvasHeight),
            end = Offset(cornerLength.toPx(), canvasHeight),
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            color = color,
            start = Offset(0f, canvasHeight),
            end = Offset(0f, canvasHeight - cornerLength.toPx()),
            strokeWidth = strokeWidth.toPx()
        )

        // Prawy dolny róg
        drawLine(
            color = color,
            start = Offset(canvasWidth, canvasHeight),
            end = Offset(canvasWidth - cornerLength.toPx(), canvasHeight),
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            color = color,
            start = Offset(canvasWidth, canvasHeight),
            end = Offset(canvasWidth, canvasHeight - cornerLength.toPx()),
            strokeWidth = strokeWidth.toPx()
        )
    }
}