package com.example.engine

import com.example.model.ChannelIndexHealth
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.KeywordBlogExposureReport
import com.example.model.LowQualityRiskLevel
import com.example.model.LowQualityRiskReport
import com.example.model.PostStatus
import com.example.model.PublishLogEntry
import com.example.model.QueueSyncState
import com.example.model.ServerAutomationConfig
import com.example.model.VisitorTrafficLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object ChannelIndexRiskEngine {

    private val urlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    fun evaluate(
        config: ServerAutomationConfig,
        slots: List<GeneratedPostSlot>,
        publishLogs: List<PublishLogEntry>,
        visitorLogs: List<VisitorTrafficLog>,
        queueSyncState: QueueSyncState = QueueSyncState.Idle,
        naverKeywordReport: KeywordBlogExposureReport? = null,
        alreadyPausedByRisk: Boolean = false,
        nowText: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
    ): LowQualityRiskReport {
        val queueItems = (queueSyncState as? QueueSyncState.Loaded)?.items.orEmpty()
        val latestVisitor = visitorLogs.getOrNull(0)
        val prevVisitor = visitorLogs.getOrNull(1)
        val latestDwell = latestVisitor?.avgDwellSec ?: 0
        val prevDwell = prevVisitor?.avgDwellSec ?: latestDwell

        val healthList = ChannelPlatform.entries.map { platform ->
            evaluateSingleChannel(
                platform = platform,
                config = config,
                slots = slots,
                publishLogs = publishLogs,
                queueItems = queueItems,
                latestVisitor = latestVisitor,
                prevVisitor = prevVisitor,
                latestDwell = latestDwell,
                prevDwell = prevDwell,
                naverKeywordReport = if (platform == ChannelPlatform.NAVER) naverKeywordReport else null,
                nowText = nowText
            )
        }

        val overallRisk = when {
            healthList.any { it.riskLevel == LowQualityRiskLevel.HIGH_RISK } -> LowQualityRiskLevel.HIGH_RISK
            healthList.any { it.riskLevel == LowQualityRiskLevel.CAUTION } -> LowQualityRiskLevel.CAUTION
            else -> LowQualityRiskLevel.SAFE
        }

        val lowestIndex = healthList.minOfOrNull { it.currentIndexScore } ?: 80
        val riskScore = (100 - lowestIndex).coerceIn(0, 100)

        val highRiskChannels = healthList.filter { it.riskLevel == LowQualityRiskLevel.HIGH_RISK }
        val cautionChannels = healthList.filter { it.riskLevel == LowQualityRiskLevel.CAUTION }

        val triggeredAlerts = mutableListOf<String>()
        highRiskChannels.forEach { ch ->
            triggeredAlerts += "[고위험] ${ch.platform.displayName} 지수 ${ch.currentIndexScore}점 (${formatDelta(ch.indexDelta)}) — ${ch.riskFactors.firstOrNull().orEmpty()}"
        }
        cautionChannels.forEach { ch ->
            triggeredAlerts += "[주의] ${ch.platform.displayName} 지수 ${ch.currentIndexScore}점 (${formatDelta(ch.indexDelta)}) — ${ch.riskFactors.firstOrNull().orEmpty()}"
        }

        val shouldPause = config.autoPauseOnHighRisk && (overallRisk == LowQualityRiskLevel.HIGH_RISK || alreadyPausedByRisk)
        val pauseReason = when {
            overallRisk == LowQualityRiskLevel.HIGH_RISK && config.autoPauseOnHighRisk ->
                "${highRiskChannels.joinToString(", ") { it.platform.displayName }} 저품질 고위험 감지(지수 하락 ${highRiskChannels.minOf { it.indexDelta }}p · 실패/세션/규정 이상)로 자동 발행을 일시 중지했습니다."
            alreadyPausedByRisk ->
                "이전 저품질 고위험 감지로 자동 발행이 일시 중지된 상태입니다. 원인 조치 후 '자동발행 재개'를 눌러 주세요."
            else -> null
        }

        val recoveryActions = buildList {
            if (healthList.any { it.externalLinkViolationCount > 0 || (it.platform == ChannelPlatform.NAVER && config.enableExternalLinkNaver) }) {
                add("네이버 본문 외부 URL을 즉시 제거하고 6번 배너·고정댓글 검색유도 형식으로 전환하세요.")
            }
            if (healthList.any { it.errorRatePct >= 30 || it.failedCount >= 2 }) {
                add("실패·캡차 채널의 워커 릴레이/세션 쿠키 상태를 복구하고 발행 간격(MIN_DELAY)을 60분 이상으로 늘리세요.")
            }
            if (healthList.any { it.qualityFlagCount > 0 }) {
                add("Quality Defense 정화·3채널 리라이팅으로 AI 상투어/유사도 15% 미만 교정을 실행하세요.")
            }
            if (config.dailyPostLimit > 5) {
                add("일일 발행 제한(DAILY_POST_LIMIT=${config.dailyPostLimit})을 3~5건 이하로 낮춰 검색엔진 스팸 감지를 예방하세요.")
            }
            if (isEmpty()) {
                add("전 채널 지수 정상 — 현재 시간차 분산(Cloudflare→티스토리→네이버) 규칙을 유지하세요.")
            }
        }

        return LowQualityRiskReport(
            overallRisk = overallRisk,
            overallRiskScore = riskScore,
            autoPublishPaused = shouldPause,
            pauseReason = pauseReason,
            channelHealthList = healthList,
            triggeredAlerts = triggeredAlerts,
            recoveryActions = recoveryActions,
            evaluatedAt = nowText
        )
    }

    private fun evaluateSingleChannel(
        platform: ChannelPlatform,
        config: ServerAutomationConfig,
        slots: List<GeneratedPostSlot>,
        publishLogs: List<PublishLogEntry>,
        queueItems: List<com.example.model.DdakchalQueueItemRecord>,
        latestVisitor: VisitorTrafficLog?,
        prevVisitor: VisitorTrafficLog?,
        latestDwell: Int,
        prevDwell: Int,
        naverKeywordReport: KeywordBlogExposureReport?,
        nowText: String
    ): ChannelIndexHealth {
        val baseline = when (platform) {
            ChannelPlatform.CLOUDFLARE -> 84
            ChannelPlatform.TISTORY -> 80
            ChannelPlatform.NAVER -> 82
        }

        val chLogs = publishLogs.filter { it.platform == platform }
        val chQueue = queueItems.filter { it.platform == platform }

        val publishedCount = chLogs.count { it.status == PostStatus.PUBLISHED } +
            chQueue.count { it.status == PostStatus.PUBLISHED }
        val failedCount = chLogs.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF } +
            chQueue.count { it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF }
        val handoffCount = chLogs.count { it.status == PostStatus.HANDOFF } +
            chQueue.count { it.status == PostStatus.HANDOFF }
        val totalDecided = publishedCount + failedCount
        val errorRatePct = if (totalDecided > 0) ((failedCount * 100.0) / totalDecided).roundToInt() else 0

        // Consecutive recent failures / retry penalties
        val recentConsecutiveFails = chLogs.takeWhile {
            it.status == PostStatus.FAILED || it.status == PostStatus.HANDOFF
        }.size

        // Quality & external link flags from published/queued slots (post-publish defense)
        var qualityFlags = 0
        var externalLinkViolations = 0
        val activeSlots = slots.filter { it.status == PostStatus.PUBLISHED || it.status == PostStatus.QUEUED }
        val auditedSlots = activeSlots.ifEmpty { slots.take(1) }
        auditedSlots.forEach { slot ->
            val v = slot.variants[platform]
            if (v != null) {
                val defense = QualityDefenseEngine.defendText(v.title, v.bodyText, platform, slot.auctionCase)
                qualityFlags += defense.totalHitsCount
                if (platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
                    if (urlRegex.containsMatchIn(v.bodyText) || urlRegex.containsMatchIn(v.bodyHtml)) {
                        externalLinkViolations++
                    }
                }
            }
            if (!slot.qaReport.similarityPassed) {
                qualityFlags++
            }
        }

        // Visitor traffic delta per channel
        val latestViews = when (platform) {
            ChannelPlatform.NAVER -> latestVisitor?.naverViews ?: 0
            ChannelPlatform.TISTORY -> latestVisitor?.tistoryViews ?: 0
            ChannelPlatform.CLOUDFLARE -> latestVisitor?.cloudflareViews ?: 0
        }
        val prevViews = when (platform) {
            ChannelPlatform.NAVER -> prevVisitor?.naverViews ?: latestViews
            ChannelPlatform.TISTORY -> prevVisitor?.tistoryViews ?: latestViews
            ChannelPlatform.CLOUDFLARE -> prevVisitor?.cloudflareViews ?: latestViews
        }
        val viewDropPct = if (prevViews > 0) {
            (((prevViews - latestViews) * 100.0) / prevViews).roundToInt()
        } else 0
        val dwellDropPct = if (prevDwell > 0) {
            (((prevDwell - latestDwell) * 100.0) / prevDwell).roundToInt()
        } else 0

        var delta = 0
        val factors = mutableListOf<String>()

        if (handoffCount > 0) {
            delta -= handoffCount * 15
            factors += "캡차/2차인증 중단(HANDOFF) ${handoffCount}건 감지"
        }
        if (recentConsecutiveFails >= 2) {
            delta -= 18
            factors += "최근 연속 발행 실패 ${recentConsecutiveFails}회 (릴레이·세션 장애)"
        } else if (failedCount > 0 && errorRatePct >= 40) {
            delta -= 14
            factors += "채널 실패율 ${errorRatePct}% (${failedCount}/${totalDecided}건)"
        } else if (failedCount > 0) {
            delta -= 6
            factors += "발행 실패 이력 ${failedCount}건"
        }

        if (externalLinkViolations > 0) {
            delta -= externalLinkViolations * 10
            factors += "네이버 무링크 규격 위반 ${externalLinkViolations}건"
        } else if (platform == ChannelPlatform.NAVER && config.enableExternalLinkNaver) {
            delta -= 8
            factors += "네이버 본문 외부링크 허용 상태 (C-Rank 감점 주의)"
        }

        if (qualityFlags > 0 && (activeSlots.isNotEmpty() || failedCount > 0)) {
            delta -= (qualityFlags * 3).coerceAtMost(10)
            factors += "탈AI/유사도/금칙어 품질 경고 ${qualityFlags}건"
        } else if (qualityFlags > 0) {
            delta -= 3
            factors += "초안 품질 검수 권장 (${qualityFlags}건 · 발행 전 자동정화 적용 대상)"
        }

        if (viewDropPct >= 35) {
            delta -= 16
            factors += "발행 후 방문 유입 ${viewDropPct}% 급감 (${prevViews}→${latestViews})"
        } else if (viewDropPct >= 15) {
            delta -= 7
            factors += "방문 유입 ${viewDropPct}% 감소"
        } else if (latestViews > prevViews && publishedCount > 0) {
            delta += 4
        }

        if (dwellDropPct >= 30 || (latestDwell in 1..75)) {
            delta -= 10
            factors += "평균 체류시간 저하 (${latestDwell}초)"
        }

        if (config.dailyPostLimit > 6) {
            delta -= 8
            factors += "과다 일일 발행 설정 (${config.dailyPostLimit}건/일)"
        }

        val exposureScore = naverKeywordReport?.exposureScore ?: when {
            publishedCount > 0 && failedCount == 0 && viewDropPct <= 0 -> 82
            viewDropPct >= 35 -> 42
            else -> (baseline + delta).coerceIn(10, 100)
        }
        if (naverKeywordReport != null && naverKeywordReport.exposureScore < 45) {
            delta -= 12
            factors += "네이버 검색 노출 점수 저하 (${naverKeywordReport.exposureScore}점)"
        } else if (naverKeywordReport != null && naverKeywordReport.exposureScore >= 75) {
            delta += 4
        }

        if (publishedCount > 0 && failedCount == 0 && qualityFlags == 0 && externalLinkViolations == 0 && viewDropPct <= 5) {
            delta += 3
        }

        val currentScore = (baseline + delta).coerceIn(5, 100)
        val riskLevel = when {
            currentScore <= 55 || delta <= -22 || handoffCount > 0 || recentConsecutiveFails >= 2 || viewDropPct >= 40 || (failedCount >= 2 && errorRatePct >= 50) ->
                LowQualityRiskLevel.HIGH_RISK
            currentScore <= 72 || delta <= -8 || failedCount > 0 || externalLinkViolations > 0 || viewDropPct >= 15 || (qualityFlags > 0 && activeSlots.isNotEmpty()) ->
                LowQualityRiskLevel.CAUTION
            else ->
                LowQualityRiskLevel.SAFE
        }

        if (factors.isEmpty()) {
            factors += if (publishedCount == 0) {
                "발행 대기 중 · 규격 위반 0건"
            } else {
                "발행 후 지수·유입 정상 (${publishedCount}건 완료)"
            }
        }

        return ChannelIndexHealth(
            platform = platform,
            publishedCount = publishedCount,
            failedCount = failedCount,
            errorRatePct = errorRatePct,
            baselineIndexScore = baseline,
            currentIndexScore = currentScore,
            indexDelta = delta,
            avgDwellSec = latestDwell,
            exposureScore = exposureScore,
            qualityFlagCount = qualityFlags,
            externalLinkViolationCount = externalLinkViolations,
            riskLevel = riskLevel,
            riskFactors = factors,
            lastEvaluatedAt = nowText
        )
    }

    private fun formatDelta(delta: Int): String = if (delta >= 0) "+$delta" else "$delta"
}
