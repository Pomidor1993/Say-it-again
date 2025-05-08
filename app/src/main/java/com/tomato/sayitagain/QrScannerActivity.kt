package com.tomato.sayitagain

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.tomato.sayitagain.ui.theme.QrScannerBorder
import java.util.concurrent.Executors
import androidx.compose.ui.unit.Dp
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

@UnstableApi
class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QrScannerScreen(onBackClick = { finish() })
        }
    }
}

@OptIn(ExperimentalGetImage::class, UnstableApi::class)
@Composable
fun QrScannerScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val viewModel: QrViewModel = viewModel()
    val isLoaded by viewModel.isLoaded
    val mediaPlayerHelper = viewModel.mediaPlayerHelper
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var scanning by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var currentQrData by remember { mutableStateOf<QrCodeData?>(null) }
    var playbackEnded by remember { mutableStateOf(false) }

    val cameraPermissionGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> cameraPermissionGranted.value = isGranted }

    fun stopCamera() {
        cameraProviderFuture.get().unbindAll()
        scanning = false
    }

    fun resetScanner() {
        if (!isLoaded) return
        stopCamera()
        mediaPlayerHelper.stop()
        scanning = true
        scanError = null
        currentQrData = null
        playbackEnded = false
    }
    LaunchedEffect(cameraPermissionGranted.value, isLoaded) {
        if (cameraPermissionGranted.value && isLoaded) {
            resetScanner()
        }
    }
    LaunchedEffect(scanError) {
        scanError?.let { error ->
            Firebase.crashlytics.log("QR Scanner Error: $error")
            Firebase.crashlytics.recordException(Exception(error))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isLoaded) {
            LoadingOverlay()
            return@Box
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!cameraPermissionGranted.value) {
            PermissionRequestScreen {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            when {
                scanError != null -> ErrorOverlay(scanError!!) { resetScanner() }

                scanning -> {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { pv ->
                            cameraProviderFuture.addListener({
                                try {
                                    val provider = cameraProviderFuture.get()
                                    val preview = androidx.camera.core.Preview.Builder().build().also {
                                        it.surfaceProvider = pv.surfaceProvider
                                    }
                                    val analyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                    analyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                                        proxy.image?.let { image ->
                                            val input = InputImage.fromMediaImage(
                                                image,
                                                proxy.imageInfo.rotationDegrees
                                            )
                                            BarcodeScanning.getClient().process(input)
                                                .addOnSuccessListener { barcodes ->
                                                    barcodes.firstOrNull()?.rawValue
                                                        ?.takeIf { it != currentQrData?.code }
                                                        ?.also { raw ->

                                                            val qrData =
                                                                viewModel.getQrCodeData(raw)
                                                            if (qrData == null) {
                                                                scanError =
                                                                    "Nie rozpoznano kodu QR, spróbuj ponownie"
                                                                stopCamera()
                                                                return@addOnSuccessListener
                                                            }

                                                            currentQrData = qrData
                                                            playbackEnded = false
                                                            mediaPlayerHelper.playFromFirebaseFile(
                                                                qrData.code
                                                            )
                                                            stopCamera()
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    scanError = "Błąd skanowania"
                                                }
                                                .addOnCompleteListener { proxy.close() }
                                        }
                                    }
                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analyzer
                                    )
                                } catch (e: Exception) {
                                    scanError = e.toCameraErrorString()
                                    scanning = false
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }
                    )
                    QrScannerBorderOverlay()
                }

                currentQrData != null -> QrContentScreen(
                    mediaPlayerHelper = mediaPlayerHelper,
                    currentQrData = currentQrData!!,
                    playbackEnded = playbackEnded,
                    onPlaybackEndedChange = { playbackEnded = it },
                    onNext = { resetScanner() }
                )
            }
            BackButton(onBackClick)
        }
    }
}

@Composable
private fun ErrorOverlay(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = errorMessage,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Spróbuj ponownie")
            }
        }
    }
}

@Composable
private fun QrScannerBorderOverlay() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f)
        ) {
            QrScannerBorder()
        }
    }
}

@Composable
private fun BackButton(onBackClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.buttonback),
        contentDescription = "Przycisk powrotu",
        modifier = Modifier
            .size(80.dp)
            .padding(16.dp)
            .clip(CircleShape)
            .clickable(onClick = onBackClick)
    )
}

@Composable
private fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Wymagane uprawnienia do kamery",
            color = Color.White,
            fontSize = 24.sp
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Udziel uprawnień", fontSize = 18.sp)
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun QrContentScreen(
    mediaPlayerHelper: MediaPlayerHelper,
    currentQrData: QrCodeData,
    playbackEnded: Boolean,
    onPlaybackEndedChange: (Boolean) -> Unit,
    onNext: () -> Unit,
) {

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth: Dp = with(density) { windowInfo.containerSize.width.toDp() }
    val spacing = screenWidth * 0.1f
    var isPlaying by remember { mutableStateOf(false) }
    var isManuallyPaused by remember { mutableStateOf(false) }

    DisposableEffect(mediaPlayerHelper) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        onPlaybackEndedChange(true)
                        isPlaying = false
                        isManuallyPaused = false
                    }
                    Player.STATE_READY -> {
                        isPlaying = mediaPlayerHelper.exoPlayer.isPlaying
                        if (isPlaying) onPlaybackEndedChange(false)
                    }
                    Player.STATE_BUFFERING -> isPlaying = false
                    Player.STATE_IDLE -> {
                        isPlaying = false
                        isManuallyPaused = false
                    }
                }
            }

            override fun onIsPlayingChanged(isNowPlaying: Boolean) {
                isPlaying = isNowPlaying && !isManuallyPaused
            }
        }

        mediaPlayerHelper.exoPlayer.addListener(listener)
        onDispose {
            mediaPlayerHelper.exoPlayer.removeListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ControllableGif(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f),
            gifResId = R.drawable.soundwave,
            isPlaying = isPlaying
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (playbackEnded) {
                PlayerControlButton(
                    iconRes = R.drawable.replay_24px,
                    description = "Odtwórz ponownie",
                    onClick = {
                        currentQrData.let {
                            mediaPlayerHelper.playFromFirebaseFile(currentQrData.code)
                            onPlaybackEndedChange(false)
                        }
                    }
                )
            } else {
                PlayerControlButton(
                    iconRes = if (isPlaying) R.drawable.pause_circle_24px else R.drawable.play_circle_24px,
                    description = if (isPlaying) "Pauza" else "Wznów",
                    onClick = {
                        if (mediaPlayerHelper.exoPlayer.isPlaying) {
                            mediaPlayerHelper.pause()
                            isManuallyPaused = true
                        } else {
                            if (mediaPlayerHelper.exoPlayer.currentPosition >= mediaPlayerHelper.exoPlayer.duration) {
                                mediaPlayerHelper.seekTo(0)
                            }
                            mediaPlayerHelper.resume()
                            isManuallyPaused = false
                        }
                        isPlaying = mediaPlayerHelper.exoPlayer.isPlaying
                    }
                )
            }

            Spacer(Modifier.width(spacing))

            PlayerControlButton(
                iconRes = R.drawable.qr_code_scanner_24px,
                description = "Dalej",
                onClick = {
                    mediaPlayerHelper.stop()
                    onNext()
                },
            )
        }
    }
}

@Composable
private fun PlayerControlButton(
    iconRes: Int,
    description: String,
    onClick: () -> Unit,
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = description,
        modifier = Modifier
            .clickable(onClick = onClick)
            .size(80.dp)
    )
}

private fun Exception.toCameraErrorString(): String = when (this) {
    is SecurityException -> "Brak dostępu do kamery"
    else -> "Błąd kamery: ${localizedMessage ?: "Nieznany błąd"}"
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Ładowanie pytań…",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}