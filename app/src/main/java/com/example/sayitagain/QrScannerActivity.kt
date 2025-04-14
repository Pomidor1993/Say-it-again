package com.example.sayitagain

import QrScannerBorder
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.sayitagain.ui.theme.SayItAgainTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

@UnstableApi
class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SayItAgainTheme {
                QrScannerScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class, UnstableApi::class)
@Composable
fun QrScannerScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val viewModel: QrViewModel = viewModel(factory = QrViewModel.factory)
    val mediaPlayerHelper = viewModel.mediaPlayerHelper
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    var cameraError by remember { mutableStateOf<String?>(null) }
    val hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isQuoteLoaded by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var scanning by remember { mutableStateOf(true) }
    var currentQrData by remember { mutableStateOf<QrCodeData?>(null) }
    var playbackEnded by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val requestPermission = remember {
        { activity: ComponentActivity ->
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                QrViewModel.CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            requestPermission(context as ComponentActivity)
        }
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        scanning = false
    }

    fun resetScanner() {
        stopCamera()
        mediaPlayerHelper.stop()
        scannedCode = null
        currentQrData = null
        playbackEnded = false
        scanning = true
    }

    DisposableEffect(Unit) {
        onDispose {
            stopCamera()
            mediaPlayerHelper.stop()
        }
    }

    fun playSound(url: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    currentQrData = viewModel.getQrCodeData(url)
                    currentQrData?.let {
                        mediaPlayerHelper.stop()
                        playbackEnded = false
                        mediaPlayerHelper.play(it.code)
                        isQuoteLoaded = true
                        stopCamera()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Błąd odtwarzania: ${e.message}", Toast.LENGTH_LONG).show()
                    resetScanner()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Wymagane uprawnienia do kamery", color = Color.White)
                Button(
                    onClick = { requestPermission(context as ComponentActivity) },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Udziel uprawnień")
                }
            }
            return@Box
        }

        if (scanning) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { pv ->
                    try {
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            cameraProvider = provider

                            val preview = Preview.Builder()
                                .build()
                                .also { it.surfaceProvider = pv.surfaceProvider }

                            val resolutionSelector = ResolutionSelector.Builder()
                                .setResolutionStrategy(
                                    ResolutionStrategy(
                                        Size(1280, 720),
                                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER
                                    )
                                )
                                .build()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setResolutionSelector(resolutionSelector)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                                proxy.image?.let { image ->
                                    val inputImage = InputImage.fromMediaImage(
                                        image,
                                        proxy.imageInfo.rotationDegrees
                                    )

                                    barcodeScanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            barcodes.firstOrNull()?.rawValue?.let { value ->
                                                if (value != scannedCode) {
                                                    scannedCode = value
                                                    playSound(value)
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("QR_SCANNER", "Błąd skanowania", e)
                                        }
                                        .addOnCompleteListener {
                                            proxy.close()
                                        }
                                }
                            }

                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        }, ContextCompat.getMainExecutor(context))
                    } catch (e: Exception) {
                        cameraError = when (e) {
                            is SecurityException -> "Brak dostępu do kamery"
                            else -> "Błąd kamery: ${e.localizedMessage}"
                        }
                        scanning = false
                        Log.e("QR_SCANNER", "Błąd inicjalizacji kamery", e)
                    }
                }
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                ) {
                    QrScannerBorder()
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.buttonback),
            contentDescription = "Przycisk powrotu",
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .clickable { onBackClick() }
        )

        currentQrData?.let {
            QrContentScreen(
                mediaPlayerHelper = mediaPlayerHelper,
                currentQrData = it,
                playbackEnded = playbackEnded,
                onPlaybackEndedChange = { ended -> playbackEnded = ended },
                onNext = { resetScanner() }
            )
        }

        cameraError?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error, color = Color.Red)
                    Button(
                        onClick = {
                            cameraError = null
                            scanning = true
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Spróbuj ponownie")
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun QrContentScreen(
    mediaPlayerHelper: MediaPlayerHelper,
    currentQrData: QrCodeData?,
    playbackEnded: Boolean,
    onPlaybackEndedChange: (Boolean) -> Unit,
    onNext: () -> Unit,
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val buttonSize = screenWidth * 0.3f
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
                    iconRes = R.drawable.playagainbutton,
                    description = "Odtwórz ponownie",
                    onClick = {
                        currentQrData?.let {
                            mediaPlayerHelper.stop()
                            mediaPlayerHelper.playFromFirebase(it.code)
                            onPlaybackEndedChange(false)
                            isManuallyPaused = false
                        }
                    },
                    buttonSize = buttonSize
                )
            } else {
                PlayerControlButton(
                    iconRes = if (isPlaying) R.drawable.pausebutton else R.drawable.onlineplaybutton,
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
                    },
                    buttonSize = buttonSize
                )
            }

            Spacer(Modifier.width(spacing))

            PlayerControlButton(
                iconRes = R.drawable.skanujnextbutton,
                description = "Dalej",
                onClick = {
                    mediaPlayerHelper.stop()
                    onNext()
                },
                buttonSize = buttonSize
            )
        }
    }
}

@Composable
private fun PlayerControlButton(
    iconRes: Int,
    description: String,
    onClick: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = description,
        modifier = Modifier
            .size(buttonSize)
            .clickable(onClick = onClick)
    )
}