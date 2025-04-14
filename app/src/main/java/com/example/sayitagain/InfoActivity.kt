package com.example.sayitagain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sayitagain.ui.theme.SayItAgainTheme

class InfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SayItAgainTheme {
                InfoScreen(onBackClick = {
                    finish()
                })
            }
        }
    }
}

@Composable
fun InfoScreen(onBackClick: () -> Unit) {
    LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f))
    ) {
        // Tło ekranu
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Przycisk powrotu
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

        // Główna kolumna z tekstem
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sekcja "Ilu graczy"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.playericon),
                    contentDescription = "Ilu graczy",
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // 20% szerokości ekranu
                )
                Text(
                    text = "* Maksymalna liczba zespołów - od Was zależy, ilu graczy będzie w każdym zespole",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier
                        .padding(vertical = 26.dp)
                )
            }

            // Sekcja "Wiek graczy"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ageicon),
                    contentDescription = "Wiek graczy",
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // 20% szerokości ekranu
                )
                Text(
                    text = "* Ze względu na wulgarny język w oryginalnych źródłach",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(vertical = 26.dp)
                )
            }

            // Sekcja "Czas gry"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.timeicon),
                    contentDescription = "Czas gry",
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // 20% szerokości ekranu
                )
                Text(
                    text = "* Na każdy zespół biorący udział w grze, przykładowo 2vs2 = około 60 minut, 1vs1vs1 = około 90 minut itd.",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(vertical = 26.dp)
                )
            }
        }
    }
}