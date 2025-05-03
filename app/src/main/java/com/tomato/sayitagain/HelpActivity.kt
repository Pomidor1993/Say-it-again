package com.tomato.sayitagain

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import com.tomato.sayitagain.ui.theme.SayItAgainTheme

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SayItAgainTheme {
                HelpScreen(onBackClick = {
                    finish()
                })
            }
        }
    }
}

@Composable
fun HelpScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

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

        // Zaciemnienie tła
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Napis na górze: "INFORMACJE O GRZE"
            Text(
                text = "INFORMACJE O GRZE",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Opis tekstowy
            Text(
                text = "Say it Again! – Wejdź do świata kultowych cytatów i sprawdź, jak dobrze znasz filmy, seriale i gry!" +
                        " Graj solo lub z przyjaciółmi, wybierz liczbę rund i kategorię, a aplikacja wylosuje dla Ciebie cytaty" +
                        " do odgadnięcia. Zbieraj punkty, odkrywaj ciekawostki i udowodnij, że popkultura nie ma przed Tobą tajemnic!" +
                        " Wybierz tryb językowy i tematykę – interesują Cię filmy? Seriale? Gry? A może wszystko naraz?" +
                        " Czy rozpoznasz legendarny cytat po jednym zdaniu?",
                color = Color.White,
                fontSize = 19.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Przyciski na dole
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Informacje ogólne
                Text(
                    text = "INFORMACJE OGÓLNE ➔",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            context.startActivity(Intent(context, InfoActivity::class.java))
                        }
                )

                // Jak grać - instrukcja
                Text(
                    text = "GRA Z PLANSZÓWKĄ ➔",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            context.startActivity(Intent(context, RulesActivity::class.java))
                        }
                )

                Text(
                    text = "GRA W 100% W APLIKACJI ➔",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            context.startActivity(Intent(context, AppRulesActivity::class.java))
                        }
                )

                // Jak grać - wideo
                Text(
                    text = "JAK GRAĆ - WIDEO ➔",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                    // Dodaj kliknięcie, jeśli chcesz
                )
            }
        }
    }
}