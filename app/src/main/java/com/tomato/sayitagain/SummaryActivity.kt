package com.tomato.sayitagain

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.sayitagain.ui.theme.Adamu
import com.tomato.sayitagain.ui.theme.SayItAgainTheme

class SummaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerNames = intent.getStringArrayListExtra("playerNames") ?: arrayListOf()
        val scores = intent.getIntegerArrayListExtra("scores") ?: arrayListOf()

        setContent {
            SayItAgainTheme {
                SummaryScreen(
                    playerNames = playerNames,
                    scores = scores.map { it.toInt() }
                )
            }
        }
    }
}

@Composable
fun SummaryScreen(playerNames: List<String>, scores: List<Int>) {
    val context = LocalContext.current
    val sortedPlayers = playerNames
        .zip(scores) { name, score -> Pair(name, score) }
        .sortedByDescending { it.second }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)))
        Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.2f)) // Nowy spacer dla pozycjonowania

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Podsumowanie gry",
                    color = Color.LightGray,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 40.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(0.8f))
                {
                    sortedPlayers.forEachIndexed { index, (name, score) ->
                        PlayerScoreItem(
                            playerName = name,
                            score = score,
                            position = index + 1 // Opcjonalnie: wyświetl pozycję w rankingu
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as ComponentActivity).finish()
                    },
                    modifier = Modifier
                        .width(240.dp) // Stała szerokość dla lepszej kontroli
                        .height(100.dp) // Zwiększona wysokość
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = "WRÓĆ DO",
                            fontSize = 22.sp, // Zmniejszona wielkość czcionki
                            fontFamily = Adamu,
                            letterSpacing = 2.sp,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = "MENU GŁÓWNEGO",
                            fontSize = 22.sp,
                            fontFamily = Adamu,
                            letterSpacing = 2.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
                    }
                }
            }
        }



@Composable
fun PlayerScoreItem(playerName: String, score: Int, position: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$position.",
            color = Color.LightGray,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = playerName,
            color = Color.LightGray,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )

        Text(
            text = "$score pkt",
            color = Color.LightGray,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp))
    }
}