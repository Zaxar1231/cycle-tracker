package com.example.cycletracker.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(
    checkPin: (String) -> Boolean,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (value.length == 4) {
            if (checkPin(value)) {
                onUnlock()
            } else {
                error = true
                value = ""
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Введите PIN", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val filled = index < value.length
                val dotSize by animateDpAsState(if (filled) 18.dp else 14.dp, label = "pinDot")
                Box(
                    Modifier
                        .size(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(
                                if (error) MaterialTheme.colorScheme.error
                                else if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (error) "Неверный PIN" else "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    PinKey(
                        key = key,
                        onClick = {
                            when (key) {
                                "" -> {}
                                "⌫" -> {
                                    value = value.dropLast(1)
                                    error = false
                                }
                                else -> {
                                    if (value.length < 4) value += key
                                    error = false
                                }
                            }
                        }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun PinKey(key: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    if (key.isEmpty()) {
        Spacer(Modifier.size(76.dp))
        return
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(76.dp),
        shape = CircleShape,
        color = if (key == "⌫") Color.Transparent else scheme.surfaceContainerHigh.copy(alpha = 0.9f),
        contentColor = scheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "⌫") {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Стереть", modifier = Modifier.size(28.dp))
            } else {
                Text(text = key, fontSize = 28.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
