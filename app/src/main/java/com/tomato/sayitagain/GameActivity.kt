package com.tomato.sayitagain

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.tomato.sayitagain.ui.theme.Adamu
import com.tomato.sayitagain.ui.theme.SayItAgainTheme
import com.tomato.sayitagain.ui.theme.calculateSimilarity
import com.tomato.sayitagain.ui.theme.normalizeForComparison
import pl.droidsonroids.gif.GifDrawable


@UnstableApi
class GameActivity : ComponentActivity() {
    private lateinit var viewModel: QrViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SayItAgainTheme {
                // Inicjalizacja ViewModel w kontekście Composable
                val vm: QrViewModel = viewModel(factory = QrViewModel.factory)
                viewModel = vm
                GameContent(vm)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // Bezpośredni dostęp do już zainicjalizowanego ViewModel
            viewModel.mediaPlayerHelper.stop()
        }
    }
}

@UnstableApi
@Composable
private fun GameContent(viewModel: QrViewModel) {
    val context = LocalContext.current
    val playerNames = remember {
        (context as ComponentActivity).intent.getStringArrayListExtra("playerNames") ?: arrayListOf()
    }
    val rounds = remember {
        (context as ComponentActivity).intent.getIntExtra("rounds", 5)
    }
    val selectedGroups = remember {
        (context as ComponentActivity).intent.getStringArrayListExtra("groups")?.toSet() ?: setOf("Movie")
    }
    val selectedLanguages = remember {
        (context as ComponentActivity).intent.getStringArrayListExtra("languages")?.toSet() ?: setOf("ENG", "PL")
    }

    LaunchedEffect(Unit) {
        viewModel.resetUsedQrCodes()
        viewModel.setSelectedGroups(selectedGroups)
        viewModel.setSelectedLanguages(selectedLanguages)
    }

    GameScreen(
        viewModel = viewModel,
        playerNames = playerNames,
        rounds = rounds,
        activity = context as ComponentActivity
    )
}

@UnstableApi
@Composable
fun GameScreen(
    viewModel: QrViewModel,
    playerNames: List<String>,
    rounds: Int,
    activity: ComponentActivity
) {
    val context = LocalContext.current
    var currentRound by remember { mutableIntStateOf(1) }
    var currentPlayerIndex by remember { mutableIntStateOf(0) }
    var currentQuote by remember { mutableStateOf<QrCodeData?>(null) }
    var showAnswer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackEnded by remember { mutableStateOf(false) }
    var showAnswerResult by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var currentComment by remember { mutableStateOf(emptyList<String>()) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isQuoteLoaded by remember { mutableStateOf(false) }
    var isManuallyPaused by remember { mutableStateOf(false) }
    val mediaPlayerHelper = viewModel.mediaPlayerHelper


    var playerScores by remember {
        mutableStateOf(playerNames.associateWith { 0 })
    }

    DisposableEffect(mediaPlayerHelper) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        isPlaying = mediaPlayerHelper.exoPlayer.isPlaying
                    }
                    Player.STATE_ENDED -> {
                        playbackEnded = true
                        isPlaying = false
                    }
                    Player.STATE_BUFFERING -> {
                        // Możesz dodać obsługę buforowania jeśli potrzebne
                    }
                    Player.STATE_IDLE -> {
                        // Możesz dodać obsługę stanu bezczynnego
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

    LaunchedEffect(currentQuote) {
        currentQuote?.let { quote ->
            try {
                isQuoteLoaded = false
                mediaPlayerHelper.playFromFirebaseFile(quote.code)
                isQuoteLoaded = true
            } catch (e: Exception) {
                // Dodajemy obsługę błędów
                Log.e("GameScreen", "Błąd ładowania cytatu", e)
                Toast.makeText(context, "Błąd ładowania nagrania", Toast.LENGTH_SHORT).show()
                currentQuote = null
            }
        }
    }

    fun stopPlayback() {
        if (mediaPlayerHelper.exoPlayer.isPlaying) {
            mediaPlayerHelper.pause()
        }
        playbackEnded = true
        isManuallyPaused = false
    }
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    "Uwaga!",
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Czy na pewno chcesz opuścić rozgrywkę?\nWszystkie postępy zostaną utracone!",
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { activity.finish() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Wyjdź")
                    }

                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Kontynuuj")
                    }
                }
            },
            containerColor = Color.LightGray
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Runda $currentRound",
                fontSize = 24.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = buildAnnotatedString {
                    append("Teraz kolej: ")
                    withStyle(style = SpanStyle(color = Color.Green)) {
                        append(playerNames[currentPlayerIndex])
                    }
                },
                fontSize = 18.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            when {
                showAnswerResult -> AnswerResultScreen(
                    isCorrect = isAnswerCorrect,
                    correctTitle = currentQuote?.title ?: "",
                    correctTitlePL = currentQuote?.titlePL,
                    comment = currentComment,
                    onNext = {
                        stopPlayback()
                        showAnswerResult = false
                        currentQuote = null
                        playbackEnded = false

                        val nextPlayerIndex = (currentPlayerIndex + 1) % playerNames.size

                        if (nextPlayerIndex == 0) {
                            if (currentRound >= rounds) {
                                val sortedPlayers = playerNames.zip(playerScores.values.toList())
                                    .sortedByDescending { it.second }

                                context.startActivity(
                                    Intent(context, SummaryActivity::class.java).apply {
                                        putStringArrayListExtra("playerNames", ArrayList(sortedPlayers.map { it.first }))
                                        putIntegerArrayListExtra("scores", ArrayList(sortedPlayers.map { it.second }))
                                    }
                                )
                                activity.finish()
                                return@AnswerResultScreen
                            }
                            currentRound++
                        }

                        currentPlayerIndex = nextPlayerIndex
                    }
                )

                showAnswer -> AnswerForm(
                    currentQuote = currentQuote,
                    onBack = { showAnswer = false
                        isManuallyPaused = false
                        if (!playbackEnded && !mediaPlayerHelper.exoPlayer.isPlaying) {
                            mediaPlayerHelper.resume()}},
                    onSubmit = { isCorrect ->
                        isAnswerCorrect = isCorrect
                        currentComment = currentQuote?.comment ?: emptyList()

                        if (isCorrect) {
                            val currentPlayer = playerNames[currentPlayerIndex]
                            playerScores = playerScores.toMutableMap().apply {
                                put(currentPlayer, getValue(currentPlayer) + 1)
                            }
                        }

                        showAnswer = false
                        showAnswerResult = true
                    }
                )

                playbackEnded -> Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp)
                ) {
                    ReplayButton(
                        onReplay = {currentQuote?.let { quote ->
                            mediaPlayerHelper.seekTo(0)
                            mediaPlayerHelper.playFromFirebaseFile(quote.code)
                            playbackEnded = false
                            isPlaying = true }},
                        modifier = Modifier
                            .scale(0.75f)
                            .padding(horizontal = 8.dp)
                            .aspectRatio(0.75f)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.responsebutton),
                        contentDescription = "Response",
                        modifier = Modifier
                            .weight(0.1f)
                            .padding(horizontal = 8.dp)
                            .aspectRatio(0.1f)
                            .clickable { showAnswer = true }
                    )
                }

                currentQuote != null -> {
                    if (isQuoteLoaded) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ControllableGif(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .aspectRatio(1f),
                                gifResId = R.drawable.soundwave,
                                isPlaying = isPlaying
                            )

                            PlayerControls(
                                mediaPlayerHelper = mediaPlayerHelper,
                                isPlaying = isPlaying,
                                playbackEnded = playbackEnded,
                                onAnswer = { showAnswer = true }
                            )
                        }
                    } else {
                        Text("Ładowanie cytatu...", color = Color.LightGray)
                    }
                }

                else -> PlayButton {
                    currentQuote = viewModel.getRandomUnusedQrCode() ?: run {
                        Toast.makeText(context, "Wyczerpały się dostępne cytaty!", Toast.LENGTH_SHORT).show()
                        null
                    }
                }
            }
        }
    }
}

@Composable
fun ControllableGif(
    modifier: Modifier = Modifier,
    gifResId: Int,
    isPlaying: Boolean
) {
    val context = LocalContext.current
    val gifDrawable = remember {
        GifDrawable(context.resources, gifResId).apply {
            stop()
        }
    }

    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                setImageDrawable(gifDrawable)
            }
        },
        modifier = modifier,
        update = {
            if (isPlaying) {
                gifDrawable.start()
            } else {
                gifDrawable.stop()
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            gifDrawable.recycle()
        }
    }
}

@Composable
private fun AnswerForm(
    currentQuote: QrCodeData?,
    onBack: () -> Unit,
    onSubmit: (Boolean) -> Unit
) {
    var answer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            label = { Text("Podaj tytuł źródła", color = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedLabelColor = Color.LightGray,
                unfocusedLabelColor = Color.LightGray,
                cursorColor = Color.LightGray
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(Color.Black)
                .padding(bottom = 16.dp)
        )
        Button(
            onClick = { currentQuote?.let { quote ->
                val similarity = calculateSimilarity(
                    answer.normalizeForComparison(),
                    quote.title.normalizeForComparison(),
                    quote.titlePL?.normalizeForComparison()
                )
                onSubmit(similarity >= 0.80)
            } ?: onSubmit(false) },
                        modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(
                start  = 2.dp,
                top    = 8.dp,
                end    = 2.dp,
                bottom = 4.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(2.dp, Color(0xFFFFFFFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("ZATWIERDŹ", fontSize = 22.sp,
                fontFamily = Adamu,
                letterSpacing = 3.sp
            )
        }
        Button(
            onClick = { onBack() },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 80.dp),
            contentPadding = PaddingValues(
                start  = 2.dp,
                top    = 8.dp,
                end    = 2.dp,
                bottom = 4.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(2.dp, Color(0xFFFFFFFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("WRÓĆ DO NAGRANIA", fontSize = 22.sp,
                fontFamily = Adamu,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun AnswerResultScreen(
    isCorrect: Boolean,
    correctTitle: String,
    correctTitlePL: String?,
    comment: List<String>,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val titleText = buildAnnotatedString {
            if (isCorrect) {
                append("Bardzo dobrze! Cytat pochodził z filmu ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(correctTitle)
                }
            } else {
                append("Niestety! Poprawny tytuł to: ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(correctTitle)
                }
            }

            correctTitlePL?.let { polishTitle ->
                append("\n(Tytuł PL: ")
                withStyle(
                    style = SpanStyle(
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp
                    )
                ) {
                    append(polishTitle)
                }
                append(")")
            }
        }

        Text(
            text = titleText,
            color = if (isCorrect) Color.Green else Color.Red,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Informacje dodatkowe:",
            color = Color.LightGray,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            comment.forEach { item ->
                Text(
                    text = "• $item",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            onClick = { onNext() },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(vertical = 25.dp),
            contentPadding = PaddingValues(
                start  = 2.dp,
                top    = 8.dp,
                end    = 2.dp,
                bottom = 4.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            border = BorderStroke(2.dp, Color(0xFFFFFFFF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("GRAJMY DALEJ", fontSize = 22.sp,
                fontFamily = Adamu,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun PlayButton(onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() } ,
        colors = IconButtonDefaults . iconButtonColors (contentColor = Color.White),
        modifier = Modifier
            .size(80.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.play_circle_24px),
            contentDescription = "Graj",
            modifier = Modifier
                .fillMaxSize()
                .scale(1f)
        )
    }
}




@OptIn(UnstableApi::class)
@Composable
fun PlayerControls(
    mediaPlayerHelper: MediaPlayerHelper,
    isPlaying: Boolean,
    playbackEnded: Boolean,
    onAnswer: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        // ▶︎ / ❚❚
        IconButton(
            onClick = {
                when {
                    playbackEnded -> {
                        mediaPlayerHelper.seekTo(0)
                        mediaPlayerHelper.resume()
                    }
                    isPlaying -> mediaPlayerHelper.pause()
                    else -> mediaPlayerHelper.resume()
                }
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White
            ),
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .scale(1f)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.pause_circle_24px else R.drawable.play_circle_24px
                ),
                contentDescription = if (isPlaying) "Pauza" else "Wznów",
                modifier = Modifier.fillMaxSize()
            )
        }

        // ✔︎
        IconButton(
            onClick = onAnswer,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
            modifier = Modifier
                .size(80.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.responsebutton),
                contentDescription = "Odpowiedz",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1f)
            )
        }
    }
}



@Composable
private fun ReplayButton(
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { onReplay() },
        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.replay_24px),
            contentDescription = "Odtwórz ponownie",
            modifier = modifier
                .fillMaxSize()
                .scale(1f)
        )
    }
}
