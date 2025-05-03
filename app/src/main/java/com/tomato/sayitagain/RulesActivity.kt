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

class RulesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SayItAgainTheme {
                RulesScreen(onBackClick = { finish() })
            }
        }
    }
}

@Composable
fun RulesScreen(onBackClick: () -> Unit) {
    // Tworzymy stan pagera
    val pagerState = rememberPagerState(initialPage = 0) { 4 } // Domyślnie zaczynamy od strony 0

    Box(modifier = Modifier.fillMaxSize()) {
        // Tło ekranu
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Pager z zasadami
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> RulePage(
                    title = "CZĘŚĆ GŁÓWNA",
                    content = "1. Zeskanuj kod QR z użyciem aplikacji SayItAgain! Usłyszysz krótkie nagranie będące cytatem lub dźwiękiem z filmu/serialu/gry/odmętów internetu. \n" +
                            "2. W trakcie odsłuchu, na telefonie wyświetlą się krótkie pytania, przykładowo “Jaki jest tytuł filmu i imię postaci, która wypowiada dane słowa?” lub “Z jakiej gry pochodzi dany dźwięk i kiedy można go usłyszeć?” \n" +
                            "3. Możesz udzielić odpowiedzi na pytanie już w trakcie odsłuchiwania lub po zakończeniu nagrania.\n" +
                            "4. Jeśli z jakiegoś powodu nie usłyszałeś wszystkiego dokładnie, możesz po prostu zeskanować ponownie kod QR i raz jeszcze odsłuchać nagranie.\n",
                    arrow = ">>"
                )

                1 -> RulePage(
                    title = "CZĘŚĆ GŁÓWNA",
                    content = "5. Po udzieleniu odpowiedzi, możesz odwrócić kafelek aby zobaczyć, czy odgadłeś ją poprawnie. Nie pokazuj jeszcze odpowiedzi innym graczom!\n" +
                            "6. Jeśli udzielisz pełnej poprawnej odpowiedzi = gratulacje! Możesz zachować zeskanowany kafelek oraz pobrać z puli jeden fragment gwiazdy i umieścić go w swojej prywatnej gwieździe. Potrzebujesz 20 fragmentów, żeby uzupełnić ramiona swojej gwiazdy. \n" +
                            "7. Jeżeli nie uda Ci się odpowiedzieć lub odpowiesz tylko na część pytania - niestety! Twoja szansa przepada, odsłuchany cytat odkładasz na bok, a swoją turę zaczyna kolejny gracz.\n",
                    arrow = "<< >>"
                )

                2 -> RulePage(
                    title = "JA WIEM! JA WIEM!",
                    content = "To nie Twoja kolej, żeby zgadywać, ale uważasz, że znasz odpowiedź i widzisz, że obecnie zgadujący ma trudności w podaniu odpowiedzi? Próbuj śmiało! Pamiętaj jednak o paru zasadach:\n" +
                            "1. Żeby spróbować, musisz posiadać w danym momencie co najmniej 1 fragment gwiazdy. \n" +
                            "2. Odpowiadać możesz pod warunkiem, że gracz, którego runda obecnie trwa, podda się lub nie poda pełnej poprawnej odpowiedzi.\n" +
                            "3. Jeśli podasz poprawną odpowiedź = otrzymujesz fragment gwiazdy. Jeśli jednak się pomylisz, tracisz jeden z wcześniej zdobytych fragmentów.\n" +
                            "4. W przypadku, kiedy więcej osób deklaruje chęć do odpowiedzi, w pierwszej kolejności próbować swoich sił może ta osoba, która obecnie posiada najmniej fragmentów. Jeśli kilka osób posiada ich tyle samo, to o tym, kto pierwszy może podjąć się zgadywania, decyduje gracz, którego obecnie był ruch.",
                    arrow = "<< >>"
                )

                3 -> RulePage(
                    title = "RUNDA FINAŁOWA",
                    content = "1. Gdy już zbierzesz wystarczającą ilość odłamków (20), żeby w pełni uzupełnić ramiona gwiazdy, przechodzisz do finałowego etapu, w którym musisz zdobyć jeszcze jeden, ostatni punkt.\n" +
                            "2. Od tej pory w swojej turze nie losujesz już cytatów ze stosu kafelków, a zamiast niego używasz stosu gwiazd. \n" +
                            "3. Kiedy w swojej turze odpowiesz prawidłowo na wszystkie pytania cytatu ze stosu gwiazd - wygrywasz!\n" +
                            "4. Oczywiście może zdarzyć się tak, że będzie kilka rund finałowych - gra zawsze toczy się do momentu, aż pierwsza osoba odpowie na wszystkie pytania dotyczące cytatu ze stosu gwiazd.",
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
fun RulePage(title: String, content: String, arrow: String) {
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