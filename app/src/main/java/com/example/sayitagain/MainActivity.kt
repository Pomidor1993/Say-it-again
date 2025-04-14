package com.example.sayitagain

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.example.sayitagain.ui.theme.SayItAgainTheme
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        setContent {
            SayItAgainTheme {
                var onlineModeStep by remember { mutableIntStateOf(0) }
                var playerNames by remember { mutableStateOf<List<String>?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (onlineModeStep) {
                        0 -> MainScreen(
                            onPhysicalModeClick = { startActivity(Intent(this@MainActivity, QrScannerActivity::class.java)) },
                            onOnlineModeClick = { onlineModeStep = 1 },
                            onHelpActivityClick = { startActivity(Intent(this@MainActivity, HelpActivity::class.java)) }
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
                                onNextClick = { rounds, groups, languages -> // Dodajemy trzeci parametr
                                    startActivity(
                                        Intent(this@MainActivity, GameActivity::class.java).apply {
                                            putStringArrayListExtra("playerNames", ArrayList(names))
                                            putExtra("rounds", rounds)
                                            putStringArrayListExtra("groups", ArrayList(groups))
                                            putStringArrayListExtra("languages", ArrayList(languages)) // Teraz mamy źródło dla 'languages'
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

@Composable
fun MainScreen(onPhysicalModeClick: () -> Unit, onOnlineModeClick: () -> Unit, onHelpActivityClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Tło ekranu
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.sayitagainphysical1),
                contentDescription = "Graj z planszówką!",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(top = 5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onPhysicalModeClick() }
            )

            Image(
                painter = painterResource(id = R.drawable.sayitagainonline1),
                contentDescription = "Graj Online!",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(top = 35.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onOnlineModeClick() }

            )
            Image(
                painter = painterResource(id = R.drawable.sayitagainrules),
                contentDescription = "Poznaj zasady!",
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onHelpActivityClick() }
            )
        }
    }
}