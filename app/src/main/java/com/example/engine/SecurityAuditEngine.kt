package com.example.engine

import com.example.BuildConfig
import com.example.data.ServerConfigStore
import com.example.model.AccountSecurityGrade
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.PublishLogEntry
import com.example.model.SecurityDashboardSummary
import com.example.model.SecurityGrade
import com.example.model.ServerAutomationConfig
import com.example.model.TokenSecurityItem

object SecurityAuditEngine {

    private val rawTokenLeakRegex = Regex(
        """(Bearer\s+[A-Za-z0-9._\-]{10,}|sk-[A-Za-z0-9]{16,}|cf_[A-Za-z0-9_\-]{12,}|x-crawler-sync-token)""",
        RegexOption.IGNORE_CASE
    )
    private val naverExternalUrlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    fun auditSecurity(
        config: ServerAutomationConfig,
        slots: List<GeneratedPostSlot>,
        publishLogs: List<PublishLogEntry>,
        naverReachable: Boolean? = null,
        tistoryReachable: Boolean? = null,
        nowText: String = "방금 전"
    ): SecurityDashboardSummary {
        val scanTargets = buildList {
            slots.forEach { slot ->
                slot.variants.values.forEach { variant ->
                    add("${slot.auctionCase.caseId}/${variant.platform.code}:title" to variant.title)
                    add("${slot.auctionCase.caseId}/${variant.platform.code}:body" to variant.bodyText)
                    add("${slot.auctionCase.caseId}/${variant.platform.code}:html" to variant.bodyHtml)
                }
            }
            publishLogs.forEach { log ->
                if (!log.errorMessage.isNullOrBlank()) {
                    add("log:${log.caseId}/${log.platform.code}" to log.errorMessage)
                }
            }
        }

        val leakDetails = mutableListOf<String>()
        val grokKey = BuildConfig.GROK_API_KEY.trim()
        val grokConfigured = grokKey.isNotBlank() && !grokKey.contains("DEFAULT_VALUE", ignoreCase = true)
        val activeSecrets = listOfNotNull(
            config.crawlerSyncToken.trim().takeIf { it.length >= 6 }?.let { "CRAWLER_SYNC_TOKEN" to it },
            config.cfApiToken.trim().takeIf { it.length >= 6 }?.let { "CF_API_TOKEN" to it },
            config.llmApiKey.trim().takeIf { it.length >= 6 }?.let { "LLM_API_KEY" to it },
            grokKey.takeIf { grokConfigured && it.length >= 6 }?.let { "GROK_API_KEY" to it }
        )

        var syncLeakCount = 0
        var cfLeakCount = 0
        var llmLeakCount = 0
        var grokLeakCount = 0

        scanTargets.forEach { (source, text) ->
            if (text.isBlank()) return@forEach
            activeSecrets.forEach { (name, secretVal) ->
                if (text.contains(secretVal)) {
                    leakDetails += "$source 에서 $name 원문 문자열 감지"
                    when (name) {
                        "CRAWLER_SYNC_TOKEN" -> syncLeakCount++
                        "CF_API_TOKEN" -> cfLeakCount++
                        "LLM_API_KEY" -> llmLeakCount++
                        "GROK_API_KEY" -> grokLeakCount++
                    }
                }
            }
            if (rawTokenLeakRegex.containsMatchIn(text)) {
                leakDetails += "$source 에서 인증 토큰 헤더 패턴 감지"
            }
        }

        val tokenItems = listOf(
            TokenSecurityItem(
                keyName = "CRAWLER_SYNC_TOKEN",
                label = "딱찰 크롤러·릴레이 동기화 토큰",
                isConfigured = config.crawlerSyncToken.isNotBlank(),
                maskedPreview = ServerConfigStore.maskSecret(config.crawlerSyncToken),
                leakCount = syncLeakCount,
                statusNote = when {
                    syncLeakCount > 0 -> "원고/로그 내 노출 ${syncLeakCount}건 탐지 — 즉시 폐기 필요"
                    config.crawlerSyncToken.isBlank() -> "미입력 (읽기 전용 / 운영 토큰 입력 시 이중헤더 인증)"
                    else -> "DataStore 로컬 격리 · Authorization/X-Crawler-Sync-Token 안전 전송"
                }
            ),
            TokenSecurityItem(
                keyName = "CF_API_TOKEN",
                label = "Cloudflare D1 · Worker 관리 토큰",
                isConfigured = config.cfApiToken.isNotBlank(),
                maskedPreview = ServerConfigStore.maskSecret(config.cfApiToken),
                leakCount = cfLeakCount,
                statusNote = when {
                    cfLeakCount > 0 -> "원고/로그 내 CF 토큰 유출 ${cfLeakCount}건 탐지"
                    config.cfApiToken.isBlank() -> "선택 항목 (D1 원격 자동배포 시 입력)"
                    else -> "Cloudflare API HTTPS Bearer 전용 · 본문 노출 0건"
                }
            ),
            TokenSecurityItem(
                keyName = "GROK_API_KEY",
                label = "AI 엔진 (#1 Google Gemini 우선 · #2 Grok 보조)",
                isConfigured = grokConfigured,
                maskedPreview = if (grokConfigured) ServerConfigStore.maskSecret(grokKey) else "Secrets 패널 대기",
                leakCount = grokLeakCount,
                statusNote = when {
                    grokLeakCount > 0 -> "Grok API 키 노출 ${grokLeakCount}건 탐지 — 즉시 재발급 필요"
                    grokConfigured -> "1순위 Google Gemini 우선 + 2순위 Grok 보조 키 BuildConfig 안전 격리"
                    else -> "1순위 Google Gemini 동작 중 · 2순위 Grok 보조는 AI Studio Secrets 패널(.env) 설정"
                }
            ),
            TokenSecurityItem(
                keyName = "LLM_API_KEY",
                label = "외부 커스텀 LLM 키 (선택)",
                isConfigured = config.llmApiKey.isNotBlank(),
                maskedPreview = ServerConfigStore.maskSecret(config.llmApiKey),
                leakCount = llmLeakCount,
                statusNote = when {
                    llmLeakCount > 0 -> "LLM 키 노출 ${llmLeakCount}건 탐지"
                    config.llmApiKey.isBlank() -> "Google Gemini 계정 할당 우선 사용 중"
                    else -> "로컬 마스킹 저장 · 본문/깃허브 미노출"
                }
            )
        )

        val naverZeroPw = config.naverPw.isBlank()
        val tistoryZeroPw = config.tistoryPw.isBlank()
        val legacyPwDetected = !naverZeroPw || !tistoryZeroPw

        // 1. Naver Account Security Grade
        val naverNaverUrlLeaks = slots.count { slot ->
            val nv = slot.variants[ChannelPlatform.NAVER]
            nv != null && (naverExternalUrlRegex.containsMatchIn(nv.bodyText) || naverExternalUrlRegex.containsMatchIn(nv.bodyHtml))
        }
        val naverRelayOrSessionReady = config.naverKeepLoggedIn ||
            config.naverWorkerRelayEndpoint.isNotBlank() ||
            config.crawlerSyncToken.isNotBlank() ||
            config.naverSessionStatus.contains("유효") ||
            config.naverSessionStatus.contains("유지") ||
            config.naverSessionStatus.contains("릴레이")
        var naverScore = 0
        val naverFindings = mutableListOf<String>()
        if (naverZeroPw) {
            naverScore += 30
        } else {
            naverFindings += "레거시 평문 NAVER_PW가 기기에 남아 있음 (소거 권장)"
        }
        if (!config.enableExternalLinkNaver && naverNaverUrlLeaks == 0) {
            naverScore += 25
        } else {
            naverFindings += "네이버 본문 외부 URL 차단 검증 확인 필요 (${naverNaverUrlLeaks}건)"
        }
        if (naverRelayOrSessionReady) {
            naverScore += 25
        } else {
            naverFindings += "워커 릴레이 또는 storage_state 세션 연동 대기"
        }
        if (config.naverBlogId.isNotBlank()) {
            naverScore += 20
        } else {
            naverFindings += "NAVER_BLOG_ID 미등록"
        }
        if (naverReachable == true) naverScore = (naverScore + 5).coerceAtMost(100)
        if (naverFindings.isEmpty()) naverFindings += "비번 미저장 · 항상 로그인 유지 · 무링크 규격 통과"

        val naverAccount = AccountSecurityGrade(
            channelName = "네이버 블로그",
            platform = ChannelPlatform.NAVER,
            targetAddress = if (config.naverBlogId.isNotBlank()) "https://blog.naver.com/${config.naverBlogId}" else "블로그 ID 미설정",
            score = naverScore.coerceIn(0, 100),
            grade = SecurityGrade.fromScore(naverScore),
            zeroPasswordCompliant = naverZeroPw,
            sessionStatus = if (naverZeroPw) config.naverSessionStatus else "평문 비밀번호 잔존 · 릴레이 전환 필요",
            endpointReachable = naverReachable,
            findings = naverFindings
        )

        // 2. Tistory Account Security Grade
        val cleanTistoryUrl = config.tistoryBlogUrl.trim()
        val tistoryHttpsOk = cleanTistoryUrl.startsWith("https://") ||
            (!cleanTistoryUrl.startsWith("http://") && cleanTistoryUrl.isNotBlank())
        var tistoryScore = 0
        val tistoryFindings = mutableListOf<String>()
        if (tistoryZeroPw) {
            tistoryScore += 30
        } else {
            tistoryFindings += "레거시 평문 TISTORY_PW가 기기에 남아 있음 (소거 권장)"
        }
        if (cleanTistoryUrl.isNotBlank() && tistoryHttpsOk) {
            tistoryScore += 30
        } else if (cleanTistoryUrl.isBlank()) {
            tistoryFindings += "TISTORY_BLOG_URL 미입력"
        } else {
            tistoryFindings += "HTTPS 보안 프로토콜(https://) 적용 필요"
        }
        if (tistoryReachable == true) {
            tistoryScore += 25
        } else if (tistoryReachable == false) {
            tistoryFindings += "티스토리 URL HTTP 연결 점검 필요"
        } else {
            tistoryScore += 15
            tistoryFindings += "실주소 접속 테스트 권장"
        }
        if (config.tistoryId.isNotBlank() || config.crawlerSyncToken.isNotBlank() || config.tistoryKeepLoggedIn) {
            tistoryScore += 15
        } else {
            tistoryFindings += "카카오 계정 식별 또는 릴레이 토큰 설정 권장"
        }
        if (tistoryFindings.isEmpty()) tistoryFindings += "비번 미저장 · HTTPS 주소 · 항상 로그인/바로 등록 안전"

        val tistoryAccount = AccountSecurityGrade(
            channelName = "티스토리 (카카오)",
            platform = ChannelPlatform.TISTORY,
            targetAddress = cleanTistoryUrl.ifBlank { "티스토리 주소 미설정" },
            score = tistoryScore.coerceIn(0, 100),
            grade = SecurityGrade.fromScore(tistoryScore),
            zeroPasswordCompliant = tistoryZeroPw,
            sessionStatus = if (tistoryZeroPw) config.tistorySessionStatus else "평문 비밀번호 잔존 · 세션 복구 필요",
            endpointReachable = tistoryReachable,
            findings = tistoryFindings
        )

        // 3. Cloudflare Pages / D1 Grade
        val cfSiteBase = config.cloudflareSiteBase.trim().removeSuffix("/").ifBlank { "https://aution.codeb.app" }
        var cfScore = 30
        val cfFindings = mutableListOf<String>()
        if (cfSiteBase.startsWith("https://")) {
            cfScore += 25
        } else {
            cfFindings += "HTTPS 도메인 사용 필수"
        }
        if (config.cfApiToken.isNotBlank() && config.cfAccountId.isNotBlank()) {
            cfScore += 25
        } else {
            cfFindings += "CF_ACCOUNT_ID / CF_API_TOKEN 미입력 (로컬 발행 모드)"
        }
        if (!config.cronExpression.startsWith("0 ")) {
            cfScore += 20
        } else {
            cfFindings += "정각(:00) 크론 금지 규칙 위반"
        }
        if (cfLeakCount > 0) cfScore = (cfScore - 40).coerceAtLeast(10)
        if (cfFindings.isEmpty()) cfFindings += "$cfSiteBase · D1·Cron 비정각 스케줄 안전"

        val cfAccount = AccountSecurityGrade(
            channelName = "Cloudflare 자체웹 ($cfSiteBase)",
            platform = ChannelPlatform.CLOUDFLARE,
            targetAddress = "$cfSiteBase${config.cloudflareBlogPath}",
            score = cfScore.coerceIn(0, 100),
            grade = SecurityGrade.fromScore(cfScore),
            zeroPasswordCompliant = true,
            sessionStatus = "Bearer API Token + App Check 격리",
            endpointReachable = null,
            findings = cfFindings
        )

        // 4. DDAKCHAL Sync Endpoint Grade
        var ddakScore = 35
        val ddakFindings = mutableListOf<String>()
        if (config.ddakchalApiBase.startsWith("https://")) ddakScore += 25 else ddakFindings += "HTTPS 엔드포인트 필요"
        if (config.crawlerSyncToken.isNotBlank()) ddakScore += 25 else ddakFindings += "CRAWLER_SYNC_TOKEN 미설정"
        if (syncLeakCount == 0 && leakDetails.isEmpty()) ddakScore += 15 else ddakFindings += "유출 패턴 ${leakDetails.size}건 즉시 소거"
        if (ddakFindings.isEmpty()) ddakFindings += "이중 인증 헤더 + 외부 유출 0건"

        val ddakAccount = AccountSecurityGrade(
            channelName = "딱찰 API 공급 서버",
            platform = null,
            targetAddress = config.ddakchalApiBase,
            score = ddakScore.coerceIn(0, 100),
            grade = SecurityGrade.fromScore(ddakScore),
            zeroPasswordCompliant = true,
            sessionStatus = if (config.crawlerSyncToken.isNotBlank()) "Bearer + X-Crawler-Sync-Token 이중 인증" else "공개 피드/토큰 대기",
            endpointReachable = null,
            findings = ddakFindings
        )

        val accountGrades = listOf(naverAccount, tistoryAccount, cfAccount, ddakAccount)
        val leakPenalty = (leakDetails.size * 15).coerceAtMost(40)
        val rawAvg = accountGrades.map { it.score }.average().toInt()
        val overallScore = (rawAvg - leakPenalty).coerceIn(0, 100)
        val overallGrade = SecurityGrade.fromScore(overallScore)

        val recommendation = when {
            leakDetails.isNotEmpty() -> "원고/로그에서 토큰 패턴이 탐지되었습니다. 해당 원고를 재생성하고 토큰을 재발급하세요."
            legacyPwDetected -> "기기에 남아있는 평문 비밀번호를 즉시 소거하고 워커 릴레이·세션 핸드오프 방식으로 전환하세요."
            overallScore >= 90 -> "외부 토큰 노출 0건 및 비밀번호 미저장(Zero-Password) 보안 기준을 모두 충족했습니다."
            else -> "네이버 블로그 ID, 티스토리 HTTPS 주소, 동기화 토큰을 등록·검증하면 S등급 보안 상태가 됩니다."
        }

        return SecurityDashboardSummary(
            overallScore = overallScore,
            overallGrade = overallGrade,
            legacyPlaintextPwDetected = legacyPwDetected,
            externalLeakCount = leakDetails.size,
            leakedTokensDetail = leakDetails.distinct(),
            tokenItems = tokenItems,
            accountGrades = accountGrades,
            lastAuditedAt = nowText,
            recommendationText = recommendation
        )
    }
}
