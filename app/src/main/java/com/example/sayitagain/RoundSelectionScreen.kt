package com.example.sayitagain

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp

@Composable
fun RoundSelectionScreen(
    playerNames: List<String>,
    onBackClick: () -> Unit,
    onNextClick: (Int, Set<String>, Set<String>) -> Unit // Dodano języki
) {
    var selectedRounds by remember { mutableIntStateOf(5) }
    val categories = listOf(
        "Filmy" to "Movie",
        "Seriale" to "TV Series",
        "Gry" to "Game"
    )

    val selectedGroups = remember { mutableStateOf(setOf<String>()) }
    val languages = listOf("Polski" to "PL", "Angielski" to "ENG")
    val selectedLanguages = remember { mutableStateOf(setOf<String>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.sayitagainbackground),
            contentDescription = "Tło ekranu głównego",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        )
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
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gracze: ${playerNames.joinToString(", ")}",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Wybór liczby rund
            Text(
                text = "Wybierz liczbę rund:",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                listOf(5, 10, 15).forEach { rounds ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = selectedRounds == rounds,
                            onClick = { selectedRounds = rounds },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.Green,
                                unselectedColor = Color.White
                            )
                        )
                        Text(
                            text = "$rounds",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }


            // Wybór kategorii
            Text(
                text = "Wybierz kategorie:",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                categories.forEach { (displayName, groupName) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                selectedGroups.value =
                                    if (selectedGroups.value.contains(groupName)) {
                                        selectedGroups.value - groupName
                                    } else {
                                        selectedGroups.value + groupName
                                    }
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = selectedGroups.value.contains(groupName),
                            onCheckedChange = { isChecked ->
                                selectedGroups.value = if (isChecked) {
                                    selectedGroups.value + groupName
                                } else {
                                    selectedGroups.value - groupName
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Green,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Text(
                text = "Wybierz język cytatów:",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                languages.forEach { (displayName, langCode) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                selectedLanguages.value =
                                    if (selectedLanguages.value.contains(langCode)) {
                                        selectedLanguages.value - langCode
                                    } else {
                                        selectedLanguages.value + langCode
                                    }
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = selectedLanguages.value.contains(langCode),
                            onCheckedChange = { isChecked ->
                                selectedLanguages.value = if (isChecked) {
                                    selectedLanguages.value + langCode
                                } else {
                                    selectedLanguages.value - langCode
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Green,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Przycisk "Dalej"
            Image(
                painter = painterResource(id = R.drawable.approvebutton),
                contentDescription = "Dalej",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 24.dp)
                    .clickable(enabled = selectedGroups.value.isNotEmpty() && selectedLanguages.value.isNotEmpty()) {
                        onNextClick(selectedRounds, selectedGroups.value, selectedLanguages.value)
                    }
            )

            if (selectedGroups.value.isEmpty() || selectedLanguages.value.isEmpty()) {
                val errorMessage = buildString {
                    if (selectedGroups.value.isEmpty()) append("Musisz wybrać przynajmniej jedną kategorię!\n")
                    if (selectedLanguages.value.isEmpty()) append("Musisz wybrać przynajmniej jeden język!")
                }
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}