package com.example.cycletracker.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.cycletracker.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val version: String,
    val title: String,
    val notes: String,
    val apkUrl: String,
    val apkSize: Long,
    val pageUrl: String
)

enum class ApkCheck {
    OK,
    NOT_NEWER,
    SIGNATURE_MISMATCH,
    INVALID
}

object Updater {

    private const val TIMEOUT_MS = 15_000
    private const val UPDATES_DIR = "updates"

    val currentVersion: String
        get() = BuildConfig.VERSION_NAME

    /** Последний опубликованный (не черновик и не pre-release) релиз с APK, или null, если APK нет. */
    suspend fun fetchLatest(): ReleaseInfo? = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/${BuildConfig.UPDATE_REPO}/releases/latest")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "CycleTracker/${BuildConfig.VERSION_NAME}")
        }
        try {
            when (conn.responseCode) {
                HttpURLConnection.HTTP_OK -> Unit
                HttpURLConnection.HTTP_NOT_FOUND -> return@withContext null
                403, 429 -> throw IOException("GitHub временно ограничил запросы, попробуйте позже")
                else -> throw IOException("Ошибка сервера: ${conn.responseCode}")
            }
            val root = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val assets = root.optJSONArray("assets") ?: return@withContext null
            val apk = (0 until assets.length())
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.optString("name").endsWith(".apk", ignoreCase = true) }
                ?: return@withContext null
            val tag = root.optString("tag_name")
            ReleaseInfo(
                version = tag.removePrefix("v").removePrefix("V"),
                title = root.optString("name").ifBlank { tag },
                notes = root.optString("body").trim(),
                apkUrl = apk.getString("browser_download_url"),
                apkSize = apk.optLong("size"),
                pageUrl = root.optString("html_url")
            )
        } finally {
            conn.disconnect()
        }
    }

    /** true, если [remote] новее [current]. Сравниваются числовые части: 1.10 > 1.9, 1.0.1 > 1.0. */
    fun isNewer(remote: String, current: String): Boolean {
        val r = versionParts(remote)
        val c = versionParts(current)
        for (i in 0 until maxOf(r.size, c.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = c.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    private fun versionParts(version: String): List<Int> =
        version.trim().removePrefix("v").removePrefix("V")
            .substringBefore('-')
            .split('.')
            .map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }

    suspend fun download(
        context: Context,
        release: ReleaseInfo,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, UPDATES_DIR)
        dir.deleteRecursively()
        dir.mkdirs()
        val target = File(dir, "cycle-tracker-${release.version}.apk")
        val partial = File(dir, "${target.name}.part")

        val conn = (URL(release.apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "CycleTracker/${BuildConfig.VERSION_NAME}")
        }
        try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Не удалось скачать файл: ${conn.responseCode}")
            }
            val total = conn.contentLengthLong.takeIf { it > 0 } ?: release.apkSize
            conn.inputStream.use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        done += read
                        if (total > 0) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            if (!partial.renameTo(target)) throw IOException("Не удалось сохранить файл")
            target
        } catch (e: Throwable) {
            partial.delete()
            throw e
        } finally {
            conn.disconnect()
        }
    }

    /** Проверяет, что скачанный APK — это наше приложение, новее установленного и подписан тем же ключом. */
    fun verify(context: Context, apk: File): ApkCheck {
        val pm = context.packageManager
        val archive = archiveInfo(pm, apk.absolutePath) ?: return ApkCheck.INVALID
        if (archive.packageName != context.packageName) return ApkCheck.INVALID
        val installed = installedInfo(pm, context.packageName) ?: return ApkCheck.INVALID
        if (versionCodeOf(archive) <= versionCodeOf(installed)) return ApkCheck.NOT_NEWER
        val archiveSigs = signaturesOf(archive)
        val installedSigs = signaturesOf(installed)
        if (archiveSigs.isEmpty() || archiveSigs != installedSigs) return ApkCheck.SIGNATURE_MISMATCH
        return ApkCheck.OK
    }

    fun canInstall(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun openReleasePage(context: Context, release: ReleaseInfo) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.pageUrl))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun archiveInfo(pm: PackageManager, path: String): PackageInfo? =
        if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageArchiveInfo(
                path,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
        } else if (Build.VERSION.SDK_INT >= 28) {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES)
        }

    private fun installedInfo(pm: PackageManager, packageName: String): PackageInfo? = runCatching {
        if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
            )
        } else if (Build.VERSION.SDK_INT >= 28) {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        }
    }.getOrNull()

    private fun versionCodeOf(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode
        else @Suppress("DEPRECATION") info.versionCode.toLong()

    private fun signaturesOf(info: PackageInfo): Set<String> {
        val sigs = if (Build.VERSION.SDK_INT >= 28) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION")
            info.signatures
        }
        return sigs.orEmpty().map { it.toCharsString() }.toSet()
    }
}
