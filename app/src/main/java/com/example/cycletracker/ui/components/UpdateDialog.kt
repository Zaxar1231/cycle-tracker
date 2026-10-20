package com.example.cycletracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cycletracker.UpdateStatus
import com.example.cycletracker.update.ReleaseInfo
import com.example.cycletracker.update.Updater
import java.util.Locale

@Composable
fun UpdateDialog(
    status: UpdateStatus,
    onDownload: (ReleaseInfo) -> Unit,
    onInstall: () -> Unit,
    onCancelDownload: () -> Unit,
    onSkip: (ReleaseInfo) -> Unit,
    onOpenPage: (ReleaseInfo) -> Unit,
    onDismiss: () -> Unit
) {
    when (status) {
        is UpdateStatus.Available -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Доступно обновление") },
            text = { ReleaseText(status.release) },
            confirmButton = {
                TextButton(onClick = { onDownload(status.release) }) { Text("Обновить") }
            },
            dismissButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onDismiss) { Text("Позже") }
                    TextButton(onClick = { onSkip(status.release) }) { Text("Пропустить версию") }
                }
            }
        )

        is UpdateStatus.Downloading -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Загрузка обновления") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(status.progress * 100).toInt()}%" +
                            (if (status.release.apkSize > 0) " из ${sizeText(status.release.apkSize)}" else ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Скрыть") }
            },
            dismissButton = {
                TextButton(onClick = onCancelDownload) { Text("Отмена") }
            }
        )

        is UpdateStatus.ReadyToInstall -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Обновление скачано") },
            text = {
                Text(
                    "Версия ${status.release.version} готова к установке. Если Android попросит разрешение " +
                        "«Установка неизвестных приложений» — разрешите его для «Мой цикл» и вернитесь сюда. " +
                        "Ваши данные сохранятся."
                )
            },
            confirmButton = {
                TextButton(onClick = onInstall) { Text("Установить") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Позже") }
            }
        )

        is UpdateStatus.Failed -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Не удалось обновить") },
            text = { Text(status.message) },
            confirmButton = {
                val release = status.release
                if (release != null) {
                    TextButton(onClick = { onOpenPage(release) }) { Text("Открыть релиз") }
                } else {
                    TextButton(onClick = onDismiss) { Text("OK") }
                }
            },
            dismissButton = {
                if (status.release != null) {
                    TextButton(onClick = onDismiss) { Text("Закрыть") }
                }
            }
        )

        else -> Unit
    }
}

@Composable
private fun ReleaseText(release: ReleaseInfo) {
    Column(
        modifier = Modifier
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Установлена версия ${Updater.currentVersion}, доступна ${release.version}.")
        if (release.apkSize > 0) {
            Text(
                text = "Размер: ${sizeText(release.apkSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (release.notes.isNotEmpty()) {
            Text("Что нового:", style = MaterialTheme.typography.titleSmall)
            Text(release.notes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun sizeText(bytes: Long): String =
    String.format(Locale("ru"), "%.1f МБ", bytes / 1024.0 / 1024.0)
