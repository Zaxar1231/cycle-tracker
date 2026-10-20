package com.example.cycletracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cycletracker.data.SettingsState
import com.example.cycletracker.logic.ASSISTANT_QA
import com.example.cycletracker.logic.PETS
import com.example.cycletracker.logic.PET_EMOJI
import com.example.cycletracker.ui.components.AppCard

@Composable
fun AssistantScreen(
    settings: SettingsState,
    onSetPet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val petEmoji = PET_EMOJI[settings.pet] ?: "🐱"
    val filtered = remember(query) {
        if (query.isBlank()) ASSISTANT_QA
        else ASSISTANT_QA.filter { (q, a) ->
            q.contains(query, ignoreCase = true) || a.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppCard(modifier = Modifier.padding(16.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = petEmoji, fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Привет! Я ваш помощник. Задайте вопрос или найдите нужную функцию.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PETS.forEach { (key, label) ->
                        val emoji = PET_EMOJI[key] ?: ""
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onSetPet(key) }
                                .padding(6.dp)
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Text(label.substringAfter(" "), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Поиск по вопросам и функциям") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { (question, answer) ->
                AppCard {
                    Column(Modifier.padding(14.dp)) {
                        Text("$petEmoji $question", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "Ничего не найдено. Попробуйте другой запрос, например «овуляция» или «PIN».",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
