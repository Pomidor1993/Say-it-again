package com.tomato.sayitagain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.sayitagain.ui.theme.SayItAgainTheme

class AppRulesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SayItAgainTheme {
                AppRulesScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun AppRulesScreen(onBackClick: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = 0) { 3 } // Domyślnie zaczynamy od strony 0

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> AppRulePage(
                    title = "ZASADY GRY ONLINE",
                    content = "1. Zadeklarujcie ilość graczy - gra umożliwia udział maksymalnie sześciu zespołów.\n" +
                            "2. Zdecydujcie, jak długa rozgrywkę planujecie - do wyboru 5, 10 lub 15 rund.\n" +
                            "3. Wybierzcie interesujące was kategorie - Filmy, Seriale, Gry.\n" +
                            "4. Wybierzcie, w jakim języku chcecie odtwarzać cytaty. Ważne! W przypadku języka angielskiego odtwarzane będą jedynie cytaty z oryginalnego źródła," +
                            " w przypadku języka polskiego źródłem mogą być zarówno polskie tytuły, jak i dubbingowane produkcje zagraniczne.",
                    arrow = ">>"
                )

                1 -> AppRulePage(
                    title = "ZASADY GRY ONLINE",
                    content = "5. Po rozpoczęciu gry na ekranie pojawi się informacja o tym, która obecnie jest runda oraz czyja teraz kolej na zgadywanie.\n" +
                            "6. Po kliknięciu na przycisk odtwarzania, usłyszycie cytat - pamiętajcie, żeby nie przeszkadzać osobie zgadującej!\n" +
                            "7. Aplikacja  umożliwia pauzowanie oraz ponowne odtwarzanie cytatu, a używanie tych opcji nie jest w żaden sposób ograniczone.\n" +
                            "8. Kiedy już zdecydujecie się na odpowiedź, wybierzcie opcję odpowiedzi i wpiszcie w dostępnym polu tytuł źródła." +
                            "8. Tytuły możecie podawać w języku oryginalnym lub w języku polskim - obie wersje są tak samo zaliczane.\n",
                    arrow = "<< >>"
                )

                2 -> AppRulePage(
                    title = "ZASADY GRY ONLINE",
                    content = "9. Po zatwierdzeniu odpowiedzi, na ekranie wyświetli się informacja, czy była ona prawidłowa, czy też nie.\n" +
                            "10. W tym momencie na ekranie pojawi się również zbiór informacji i ciekawostek na temat danego źródła - to dobry moment, żeby wspólnie się nimi zapoznać! \n" +
                            "11. Kiedy zdecydujecie już, że chcecie grać dalej, wystarczy kliknąć w przycisk dostępny na dole ekranu, który rozpocznie turę następnego gracza\n" +
                            "12. Po zakończeniu deklarowanej liczby rund, na ekranie pojawi się krótkie podsumowanie z informacją o zdobytej ilości punktów.\n",
                    arrow = "<<"
                )
            }
        }

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
    }
}

@Composable
fun AppRulePage(title: String, content: String, arrow: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tytuł i treść wyświetlane w górnej części
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = title,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            androidx.compose.material3.Text(
                text = content,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Justify,
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Tekst "arrow" wyrównany na dole ekranu
        androidx.compose.material3.Text(
            text = arrow,
            style = TextStyle(
                color = Color.White,
                fontSize = 38.sp,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter) // Wyrównanie na dole ekranu
                .padding(bottom = 80.dp) // Odstęp od dolnej krawędzi
        )
    }
}