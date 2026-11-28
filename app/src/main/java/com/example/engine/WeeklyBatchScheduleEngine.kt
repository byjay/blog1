package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.ServerAutomationConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class AutoFixTo95Result(
    val slot: GeneratedPostSlot,
    val finalScore: Int,
    val passedGate95: Boolean,
    val iterationsRun: Int
)

/**
 * Generates deduplicated 7-day weekly batches (3..5 posts/day = 21..35 posts/week)
 * prioritizing live Ddakchal syndication items and automatically refining drafts to >= 95/100.
 */
object WeeklyBatchScheduleEngine {

    fun ensureScoreAtLeast95(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        recent7DayCorpus: List<String> = emptyList(),
        maxIterations: Int = 3
    ): AutoFixTo95Result {
        var current = slot
        var reg = RegistryInfographicEngine.buildTemplate(current.auctionCase)
        var eval = Article30PointEvaluator.evaluate(current, config, proFunnel, reg, recent7DayCorpus)
        var runs = 0

        val effectiveMax = maxIterations.coerceAtLeast(5)
        while ((!eval.eligibleForRegistration || !current.qaReport.overallPassed) && runs < effectiveMax) {
            runs++
            val structuralFail = !current.qaReport.jsonLdValid ||
                current.qaReport.charCountOkByChannel.values.any { !it } ||
                current.variants.values.any { it.tags.size !in 5..8 }
            val needsSeedRotation = runs > 1 || !current.qaReport.similarityPassed || structuralFail
            val rebuiltVariants = if (!needsSeedRotation) {
                current.variants
            } else {
                DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                    auction = current.auctionCase,
                    seedIndex = current.slotIndex + runs * 5,
                    config = config,
                    rewriteSalt = "",
                    proFunnel = proFunnel,
                    serverGapGu = current.serverRegistryGapGu,
                    serverEulGu = current.serverRegistryEulGu,
                    serverDocuments = current.serverCourtDocuments,
                    visitRecords = current.visitRecords
                )
            }
            val (defended, _) = QualityDefenseEngine.defendSlot(
                slot = current.copy(variants = rebuiltVariants),
                config = config,
                proFunnel = proFunnel,
                recent7DayCorpus = recent7DayCorpus
            )
            current = defended.copy(
                status = if (defended.qaReport.overallPassed) PostStatus.QUEUED else PostStatus.DRAFT
            )
            reg = RegistryInfographicEngine.buildTemplate(current.auctionCase)
            eval = Article30PointEvaluator.evaluate(current, config, proFunnel, reg, recent7DayCorpus)
        }

        val finalStatus = if (eval.eligibleForRegistration && current.qaReport.overallPassed) {
            if (current.status == PostStatus.PUBLISHED) PostStatus.PUBLISHED else PostStatus.QUEUED
        } else {
            PostStatus.DRAFT
        }
        return AutoFixTo95Result(
            slot = current.copy(status = finalStatus),
            finalScore = eval.totalScore,
            passedGate95 = eval.eligibleForRegistration && current.qaReport.overallPassed,
            iterationsRun = runs
        )
    }

    fun buildWeekly7DayPlan(
        dailyCount: Int,
        days: Int = 7,
        serverSyncedSlots: List<GeneratedPostSlot> = emptyList(),
        existingCaseIds: Set<String> = emptySet(),
        config: ServerAutomationConfig = ServerAutomationConfig(),
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        recent7DayCorpus: List<String> = emptyList(),
        startDateIso: String = PostAuctionFollowUpEngine.currentIsoDate()
    ): Pair<List<GeneratedPostSlot>, Int> {
        val safePerDay = dailyCount.coerceIn(3, 5)
        val totalDays = days.coerceIn(1, 7)
        val totalNeeded = safePerDay * totalDays
        val usedCaseIds = existingCaseIds.toMutableSet()
        val rollingCorpus = recent7DayCorpus.toMutableList()
        val result = mutableListOf<GeneratedPostSlot>()
        var autoFixedCount = 0

        val startCal = parseCal(startDateIso)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        val uniqueServerSlots = serverSyncedSlots.filter { slot ->
            usedCaseIds.add(slot.auctionCase.caseId)
        }
        if (uniqueServerSlots.isEmpty()) {
            return emptyList<GeneratedPostSlot>() to 0
        }

        var serverIdx = 0
        for (dayOffset in 0 until totalDays) {
            val dayCal = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, dayOffset) }
            val dateStr = fmt.format(dayCal.time)

            for (slotOfDay in 0 until safePerDay) {
                if (serverIdx >= uniqueServerSlots.size || result.size >= totalNeeded) break
                val globalIndex = dayOffset * safePerDay + slotOfDay + 1
                val scheduled = buildStaggeredTimesForDay(dateStr, slotOfDay, config)
                val srv = uniqueServerSlots[serverIdx++]
                val candidateSlot = srv.copy(
                    slotIndex = globalIndex,
                    publishDate = dateStr,
                    scheduledTimes = scheduled,
                    sourceLabel = "실서버 주간예약 (${dayOffset + 1}일차)"
                )

                val fixed = ensureScoreAtLeast95(
                    slot = candidateSlot,
                    config = config,
                    proFunnel = proFunnel,
                    recent7DayCorpus = rollingCorpus,
                    maxIterations = 3
                )
                if (fixed.iterationsRun > 0) autoFixedCount++
                result += fixed.slot
                rollingCorpus.addAll(fixed.slot.variants.values.map { it.bodyText.take(2200) })
                if (rollingCorpus.size > 18) {
                    while (rollingCorpus.size > 18) rollingCorpus.removeAt(0)
                }
            }
        }

        return result to autoFixedCount
    }

    private fun buildStaggeredTimesForDay(
        dateStr: String,
        slotOfDay: Int,
        config: ServerAutomationConfig
    ): Map<ChannelPlatform, String> {
        val baseHour = (8 + slotOfDay * 2).coerceAtMost(18)
        val minVal = ((slotOfDay * 13 + 17) % 47) + 7 // strictly never 00
        val secVal = ((slotOfDay * 19 + 23) % 43) + 11 // strictly never 00
        val cal = parseCal(dateStr).apply {
            set(Calendar.HOUR_OF_DAY, baseHour)
            set(Calendar.MINUTE, minVal)
            set(Calendar.SECOND, secVal)
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val tCf = fmt.format(cal.time)
        cal.add(Calendar.MINUTE, config.minDelayMinutes.coerceIn(35, 65))
        if (cal.get(Calendar.MINUTE) == 0) cal.add(Calendar.MINUTE, 3)
        val tTs = fmt.format(cal.time)
        cal.add(Calendar.MINUTE, config.minDelayMinutes.coerceIn(45, 85))
        if (cal.get(Calendar.MINUTE) == 0) cal.add(Calendar.MINUTE, 7)
        val tNv = fmt.format(cal.time)
        return mapOf(
            ChannelPlatform.CLOUDFLARE to tCf,
            ChannelPlatform.TISTORY to tTs,
            ChannelPlatform.NAVER to tNv
        )
    }

    private fun parseCal(iso: String): Calendar {
        val cal = Calendar.getInstance()
        val parts = iso.trim().take(10).split("-")
        if (parts.size == 3) {
            cal.set(
                parts[0].toIntOrNull() ?: 2026,
                ((parts[1].toIntOrNull() ?: 11) - 1).coerceIn(0, 11),
                (parts[2].toIntOrNull() ?: 23).coerceIn(1, 28)
            )
        }
        return cal
    }
}
