package com.tomato.sayitagain

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.firebase.FirebaseApp
import com.tomato.sayitagain.ui.theme.SayItAgainTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        setContent {
            SayItAgainTheme {
                var showContent by remember { mutableStateOf(false) }

                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.sayitagainsplash)
                )

                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    isPlaying = !showContent,
                    iterations = 1,
                    speed = 1.0f,
                    cancellationBehavior = LottieCancellationBehavior.OnIterationFinish
                )

                LaunchedEffect(progress) {
                    if (progress >= 0.99f) {
                        delay(300)
                        showContent = true
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.sayitagainbackground),
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (!showContent) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    if (showContent) {
                        var onlineModeStep by remember { mutableIntStateOf(0) }
                        var playerNames by remember { mutableStateOf<List<String>?>(null) }

                        when (onlineModeStep) {
                            0 -> BackHandler { moveTaskToBack(true) } // Minimalizacja na głównym ekranie
                            1 -> BackHandler { onlineModeStep = 0 }   // Powrót do MainScreen
                            2 -> BackHandler { onlineModeStep = 1 }    // Powrót do PlayerSetupScreen
                        }

                        when (onlineModeStep) {
                            0 -> MainScreen(
                                onPhysicalModeClick = {
                                    startActivity(Intent(this@MainActivity, QrScannerActivity::class.java))
                                },
                                onOnlineModeClick = { onlineModeStep = 1 },
                                onHelpActivityClick = {
                                    startActivity(Intent(this@MainActivity, HelpActivity::class.java))
                                },
                                onPrivacyPolicyClick = {
                                    startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                                },
                                onCopyrightClick = {
                                    startActivity(Intent(this@MainActivity, CopyrightActivity::class.java))
                                }
                            )

                                1 -> PlayerSetupScreen(
                                onBackClick = { onlineModeStep = 0 },
                                onNextClick = { names ->
                                    playerNames = names
                                    onlineModeStep = 2
                                }
                            )
                                    2 -> playerNames?.let { names ->
                                RoundSelectionScreen(
                                    playerNames = names,
                                    onBackClick = { onlineModeStep = 1 },
                                    onNextClick = { rounds, groups, languages ->
                                        startActivity(
                                            Intent(this@MainActivity, GameActivity::class.java).apply {
                                                putStringArrayListExtra("playerNames", ArrayList(names))
                                                putExtra("rounds", rounds)
                                                putStringArrayListExtra("groups", ArrayList(groups))
                                                putStringArrayListExtra("languages", ArrayList(languages))
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onPhysicalModeClick: () -> Unit,
    onOnlineModeClick: () -> Unit,
    onHelpActivityClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onCopyrightClick: () -> Unit

) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.sayitagainlogo),
                contentDescription = "Logo aplikacji",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .height(100.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.sayitagainphysical1),
                contentDescription = "Graj z planszówką!",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onPhysicalModeClick() }
            )

            Spacer(modifier = Modifier.height(35.dp))

            Image(
                painter = painterResource(id = R.drawable.sayitagainonline1),
                contentDescription = "Graj Online!",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onOnlineModeClick() }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = onCopyrightClick,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.copyright_24px),
                    contentDescription = "Prawa autorskie",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White
                )
            }
            IconButton(
                onClick = onHelpActivityClick,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.help_24px),
                    contentDescription = "Poznaj zasady",
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = onPrivacyPolicyClick,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.privacy_tip_24px),
                    contentDescription = "Polityka prywatności",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}