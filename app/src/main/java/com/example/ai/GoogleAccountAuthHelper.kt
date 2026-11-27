package com.example.ai

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.model.GoogleGeminiAccountState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

object GoogleAccountAuthHelper {
    private const val TAG = "GoogleAccountAuthHelper"

    fun currentGoogleAccountState(
        primaryGeminiFirst: Boolean = true,
        grokFallbackEnabled: Boolean = true,
        grokModelName: String = "grok-3-mini",
        statusOverride: String? = null
    ): GoogleGeminiAccountState {
        val user: FirebaseUser? = runCatching { Firebase.auth.currentUser }.getOrNull()
        val signedIn = user != null
        val primaryLabel = when {
            !primaryGeminiFirst -> "1순위 규칙 엔진 우선 (Gemini 대기)"
            signedIn -> "1순위 Google Gemini (계정 ${user?.email ?: user?.displayName ?: "연결됨"} 할당·사용 중)"
            else -> "1순위 Google Gemini (Firebase AI · 계정 연동 대기)"
        }
        val backupLabel = if (grokFallbackEnabled) {
            "2순위 Grok API ($grokModelName · Gemini 장애 시 자동 보조)"
        } else {
            "2순위 Grok API 비활성"
        }
        return GoogleGeminiAccountState(
            isSignedIn = signedIn,
            email = user?.email.orEmpty(),
            displayName = user?.displayName.orEmpty(),
            uid = user?.uid.orEmpty(),
            activePrimaryEngineLabel = primaryLabel,
            activeBackupEngineLabel = backupLabel,
            lastSignInStatus = statusOverride ?: if (signedIn) {
                "Google 계정 연결됨 (${user?.email ?: user?.uid}) · 내 계정 기반 Gemini 검증 우선 동작"
            } else {
                "미로그인 (Google 계정 로그인 시 내 계정 기반 Gemini 할당·검증을 최우선 사용)"
            }
        )
    }

    suspend fun attemptSilentGoogleSignIn(context: Context): Result<FirebaseUser?> {
        return try {
            val clientId = resolveWebClientId(context) ?: return Result.success(
                runCatching { Firebase.auth.currentUser }.getOrNull()
            )
            val existing = runCatching { Firebase.auth.currentUser }.getOrNull()
            if (existing != null) return Result.success(existing)

            val cm = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = cm.getCredential(context, request)
            val signedUser = signInWithCredentialData(result.credential.data, result.credential as? CustomCredential)
            Result.success(signedUser)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Silent Google sign-in cancelled by user", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Silent Google sign-in unavailable: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.w(TAG, "Silent Google sign-in skipped: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleInteractive(context: Context): Result<FirebaseUser> {
        return try {
            val clientId = resolveWebClientId(context)
                ?: return Result.failure(IllegalStateException("default_web_client_id가 아직 구성되지 않았습니다."))
            val cm = CredentialManager.create(context)
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            val result = cm.getCredential(context, request)
            val cred = result.credential
            if (cred !is CustomCredential || cred.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return Result.failure(IllegalStateException("유효한 Google ID 토큰 자격 증명을 받지 못했습니다."))
            }
            val user = signInWithCredentialData(cred.data, cred)
                ?: return Result.failure(IllegalStateException("Firebase Google 계정 인증 결과를 확인할 수 없습니다."))
            Result.success(user)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Interactive Google sign-in cancelled by user", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Interactive Google sign-in failed", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google sign-in", e)
            Result.failure(e)
        }
    }

    suspend fun signOutGoogle(context: Context) {
        runCatching { Firebase.auth.signOut() }
        runCatching {
            val cm = CredentialManager.create(context)
            cm.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private suspend fun signInWithCredentialData(
        data: android.os.Bundle,
        customCred: CustomCredential?
    ): FirebaseUser? {
        if (customCred != null && customCred.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return null
        }
        val googleIdCred = GoogleIdTokenCredential.createFrom(data)
        val firebaseCred = GoogleAuthProvider.getCredential(googleIdCred.idToken, null)
        return Firebase.auth.signInWithCredential(firebaseCred).await().user
    }

    private fun resolveWebClientId(context: Context): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId == 0) return null
        return runCatching { context.getString(resId).trim() }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
