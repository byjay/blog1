package com.example.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.TransactionTooLargeException
import android.widget.Toast
import androidx.compose.ui.text.AnnotatedString
import androidx.core.content.FileProvider
import java.io.File

data class ClipboardCopyResult(
    val success: Boolean,
    val copiedChars: Int,
    val byteSize: Int,
    val message: String
)

object SafeClipboardHelper {

    // Android Binder transaction limit is ~1MB across the entire process; stay safely under 450KB per clip.
    private const val SAFE_CLIPBOARD_MAX_BYTES = 450_000

    fun copyPlainTextSafely(
        context: Context,
        label: String,
        text: String,
        composeClipboard: androidx.compose.ui.platform.ClipboardManager? = null,
        showToast: Boolean = true
    ): ClipboardCopyResult {
        if (text.isBlank()) {
            val msg = "복사할 내용이 비어 있습니다."
            if (showToast) showToastSafely(context, msg)
            return ClipboardCopyResult(false, 0, 0, msg)
        }

        val byteCount = text.toByteArray(Charsets.UTF_8).size
        if (byteCount > SAFE_CLIPBOARD_MAX_BYTES) {
            val kb = byteCount / 1024
            val msg = "HTML 용량(${kb}KB)이 Android 클립보드 한도를 초과해 복사가 차단되었습니다. '1건씩 복사' 또는 'HTML 파일 공유·저장'을 사용해 주세요."
            if (showToast) showToastSafely(context, msg)
            return ClipboardCopyResult(false, 0, byteCount, msg)
        }

        return try {
            val systemClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (systemClipboard != null) {
                systemClipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            } else {
                composeClipboard?.setText(AnnotatedString(text))
            }

            // Readback verification when system clipboard is accessible
            val verifiedLen = runCatching {
                systemClipboard?.primaryClip?.getItemAt(0)?.text?.length ?: text.length
            }.getOrDefault(text.length)

            if (verifiedLen <= 0) {
                val failMsg = "시스템 클립보드 검증에 실패했습니다. 텍스트 상자를 길게 눌러 선택 복사하거나 파일 저장을 이용해 주세요."
                if (showToast) showToastSafely(context, failMsg)
                ClipboardCopyResult(false, 0, byteCount, failMsg)
            } else {
                val okMsg = "$label 복사 완료 (${verifiedLen}자 · ${((byteCount + 512) / 1024).coerceAtLeast(1)}KB)"
                if (showToast) showToastSafely(context, okMsg)
                ClipboardCopyResult(true, verifiedLen, byteCount, okMsg)
            }
        } catch (e: TransactionTooLargeException) {
            val msg = "클립보드 전송 크기 초과 오류: '이 글 1건 복사' 또는 'HTML 파일 저장·공유'를 사용해 주세요."
            if (showToast) showToastSafely(context, msg)
            ClipboardCopyResult(false, 0, byteCount, msg)
        } catch (e: Exception) {
            // Secondary attempt via Compose clipboard wrapper if direct system service threw
            val fallbackOk = runCatching {
                composeClipboard?.setText(AnnotatedString(text))
                true
            }.getOrDefault(false)
            if (fallbackOk) {
                val okMsg = "$label 복사 완료 (${text.length}자)"
                if (showToast) showToastSafely(context, okMsg)
                ClipboardCopyResult(true, text.length, byteCount, okMsg)
            } else {
                val errMsg = "복사 실패: ${e.localizedMessage ?: "권한 또는 클립보드 제한"}"
                if (showToast) showToastSafely(context, errMsg)
                ClipboardCopyResult(false, 0, byteCount, errMsg)
            }
        }
    }

    fun shareHtmlAsFile(
        context: Context,
        fileName: String,
        htmlContent: String
    ): Boolean {
        return runCatching {
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._\\-가-힣]"), "_")
                .let { if (it.endsWith(".html", ignoreCase = true)) it else "$it.html" }
            val exportDir = File(context.cacheDir, "html_exports").apply { mkdirs() }
            val outFile = File(exportDir, safeName)
            outFile.writeText(htmlContent, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_SUBJECT, safeName)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "HTML 파일 내보내기·저장"))
            true
        }.getOrElse {
            runCatching {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, fileName)
                    putExtra(Intent.EXTRA_TEXT, htmlContent.take(120_000))
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "HTML 텍스트 공유"))
                true
            }.getOrDefault(false)
        }
    }

    private fun showToastSafely(context: Context, message: String) {
        runCatching {
            Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}
