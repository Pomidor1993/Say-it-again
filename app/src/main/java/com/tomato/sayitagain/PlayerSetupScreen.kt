package com.tomato.sayitagain

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.sayitagain.ui.theme.Adamu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSetupScreen(onBackClick: () -> Unit, onNextClick: (List<String>) -> Unit) {
    var numberOfPlayers by remember { mutableIntStateOf(0) }
    var playerNames by remember { mutableStateOf(List(numberOfPlayers) { "" }) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val playersOptions = (0..6).toList()

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sekcja wyboru ilości graczy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Ilość graczy/zespołów:",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .weight(0.9f)
                        .padding(start = 5.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .weight(0.15f)
                ) {
                    OutlinedTextField(
                        value = numberOfPlayers.toString(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.9f))
                    ) {
                        playersOptions.forEach { option ->
                            if (option > 0) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "$option",
                                            color = Color.White,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    onClick = {
                                        numberOfPlayers = option
                                        playerNames = List(option) { "" }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Dynamiczny układ pól tekstowych
            if (numberOfPlayers > 0) {
                val columns = if (numberOfPlayers > 3) 2 else 1
                val itemsPerColumn = if (columns == 2) (numberOfPlayers + 1) / 2 else numberOfPlayers

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(columns) { columnIndex ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val start = columnIndex * itemsPerColumn
                            val end = if (columnIndex == columns - 1) {
                                numberOfPlayers
                            } else {
                                (columnIndex + 1) * itemsPerColumn
                            }

                            for (i in start until end) {
                                PlayerNameField(
                                    index = i,
                                    playerNames = playerNames,
                                    onValueChange = { newValue ->
                                        playerNames = playerNames.toMutableList().apply {
                                            set(i, newValue)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                } else if (numberOfPlayers == 0) {
                    Text(
                        text = "Wybierz ilość zespołów",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                }


                Button(
                    onClick = {
                        when {
                            numberOfPlayers == 0 -> {
                                errorMessage = "Musisz wybrać ilość zespołów!"
                            }
                            playerNames.any { it.length < 3 } -> {
                                errorMessage = "Każda nazwa musi mieć minimum 3 znaki!"
                            }
                            playerNames.distinct().size != numberOfPlayers -> {
                                errorMessage = "Nazwy muszą być unikalne!"
                            }
                            else -> {
                                errorMessage = null
                                onNextClick(playerNames)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(top = 16.dp),
                    contentPadding = PaddingValues(
                        start = 2.dp,
                        top = 8.dp,
                        end = 2.dp,
                        bottom = 4.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color(0xFFFFFFFF)),
                    shape = RoundedCornerShape(8.dp)
                    ) {
                    Text(
                        "ZATWIERDŹ",
                        fontSize = 22.sp,
                        fontFamily = Adamu,
                        letterSpacing = 3.sp,
                        )

                }

            }
        }
    }
}

@Composable
private fun PlayerNameField(
    index: Int,
    playerNames: List<String>,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = playerNames[index],
        onValueChange = { newValue ->
            val filteredValue = newValue
                .uppercase()
                .replace(" ", "")
                .take(10)
            onValueChange(filteredValue)
        },
        label = { Text("Nazwa ${index + 1} zespołu", color = Color.White) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        singleLine = true,
        isError = playerNames[index].isNotBlank() && playerNames[index].length < 3,
        supportingText = {
            if (playerNames[index].isNotBlank() && playerNames[index].length < 3) {
                Text(
                    "Minimum 3 znaki!",
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}