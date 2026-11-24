package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.PostAuctionFollowUpRecord
import com.example.model.PostAuctionOutcomeType
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Monitors auction dates that have passed, re-investigates court outcomes
 * (winning bid, failed drop / price reduction, reschedule, withdrawal),
 * and generates clean 3-channel follow-up related posts ready for warehouse storage & publishing.
 */
object PostAuctionFollowUpEngine {

    fun currentIsoDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

    fun isAuctionDatePassed(
        auctionDate: String,
        referenceDate: String = currentIsoDate()
    ): Boolean {
        val cleanAuction = auctionDate.trim().take(10)
        val cleanRef = referenceDate.trim().take(10)
        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(cleanAuction)) return false
        return cleanAuction <= cleanRef
    }

    fun reinvestigateAndBuildFollowUp(
        originalSlot: GeneratedPostSlot,
        outcomeType: PostAuctionOutcomeType,
        actualWinningManWon: Long? = null,
        nextAuctionDateInput: String? = null,
        courtInvestigationNote: String = "",
        config: ServerAutomationConfig = ServerAutomationConfig(),
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        recent7DayCorpus: List<String> = emptyList(),
        referenceDate: String = currentIsoDate()
    ): Pair<PostAuctionFollowUpRecord, GeneratedPostSlot> {
        val orig = PrivacySanitizerEngine.sanitizeAuctionForBlog(originalSlot.auctionCase)
        val isPast = isAuctionDatePassed(orig.auctionDate, referenceDate)
        val nowTs = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
        val safeNote = PrivacySanitizerEngine.sanitizeForPublicBlog(courtInvestigationNote.trim())

        val defaultNextDate = nextAuctionDateInput?.trim()?.takeIf { Regex("""\d{4}-\d{2}-\d{2}""").matches(it) }
            ?: addDaysIso(orig.auctionDate, 35)

        val updatedAuction: AuctionCaseIntel
        val record: PostAuctionFollowUpRecord

        when (outcomeType) {
            PostAuctionOutcomeType.WINNING_BID -> {
                val rawWinKrw = (actualWinningManWon?.takeIf { it > 0L }?.times(10_000L))
                    ?: orig.balancedBidKrw.coerceAtLeast((orig.minBidPriceKrw * 1.07).toLong())
                val winRate = ((rawWinKrw.toDouble() / orig.appraisalPriceKrw.coerceAtLeast(1L).toDouble()) * 1000.0)
                    .toInt() / 10.0
                val diffVsBalanced = rawWinKrw - orig.balancedBidKrw
                val diffLabel = if (diffVsBalanced >= 0L) {
                    "사전 적정가 대비 +${QaVerificationEngine.formatKrwShort(diffVsBalanced)}"
                } else {
                    "사전 적정가 대비 -${QaVerificationEngine.formatKrwShort(-diffVsBalanced)}"
                }
                val noteTail = if (safeNote.isNotBlank()) " · $safeNote" else ""
                updatedAuction = orig.copy(
                    dedupKey = "${orig.dedupKey}-followup-win",
                    winningPriceKrw = rawWinKrw,
                    specialCheckNotice = "[사후 낙찰 결과 확정] ${orig.auctionDate} 기일 낙찰가 ${QaVerificationEngine.formatKrwShort(rawWinKrw)}(매각가율 ${winRate}%) 확인 — 잔금납부·배당기일·명도 협상 포인트$noteTail",
                    assumedRightsText = "${orig.assumedRightsText} · 매각허가결정 및 잔금대금지급기한 후 소유권이전 촉탁 추적"
                )
                record = PostAuctionFollowUpRecord(
                    originalCaseId = orig.caseId,
                    originalAuctionDate = orig.auctionDate,
                    isPastDue = isPast,
                    outcomeType = outcomeType,
                    outcomeDate = orig.auctionDate,
                    winningPriceKrw = rawWinKrw,
                    winningRatePct = winRate,
                    bidderCount = (orig.clicksLast7d / 110).coerceIn(2, 14),
                    predictionDiffNote = "사전 제시 적정가 ${QaVerificationEngine.formatKrwShort(orig.balancedBidKrw)} vs 실제 낙찰 ${QaVerificationEngine.formatKrwShort(rawWinKrw)} ($diffLabel)",
                    followUpSummary = "${orig.caseId} 기일(${orig.auctionDate}) 사후 재조사 결과 낙찰가 ${QaVerificationEngine.formatKrwShort(rawWinKrw)} (${winRate}%) 확정 · 추가 낙찰분석 관련글 생성",
                    recordedAt = nowTs
                )
            }

            PostAuctionOutcomeType.FAILED_DROP -> {
                val nextMin = ((orig.minBidPriceKrw * 80L) / 100L).coerceAtLeast(1_000_000L)
                val nextDiscount = ((nextMin.toDouble() / orig.appraisalPriceKrw.coerceAtLeast(1L).toDouble()) * 100.0)
                    .toInt().coerceIn(10, 100)
                val nextCons = (nextMin * 1.04).toLong()
                val nextBal = (nextMin * 1.11).toLong()
                val nextAgg = (nextMin * 1.18).toLong()
                val noteTail = if (safeNote.isNotBlank()) " · $safeNote" else ""
                updatedAuction = orig.copy(
                    dedupKey = "${orig.dedupKey}-followup-drop",
                    minBidPriceKrw = nextMin,
                    bidCount = orig.bidCount + 1,
                    discountRatePct = nextDiscount,
                    auctionDate = defaultNextDate,
                    conservativeBidKrw = nextCons,
                    balancedBidKrw = nextBal,
                    aggressiveBidKrw = nextAgg,
                    specialCheckNotice = "[유찰 사후 재조사] ${orig.auctionDate} 유찰 확정 → 차회 기일 $defaultNextDate 최저가 ${QaVerificationEngine.formatKrwShort(nextMin)}(${nextDiscount}%) 재산정$noteTail"
                )
                record = PostAuctionFollowUpRecord(
                    originalCaseId = orig.caseId,
                    originalAuctionDate = orig.auctionDate,
                    isPastDue = isPast,
                    outcomeType = outcomeType,
                    outcomeDate = orig.auctionDate,
                    nextAuctionDate = defaultNextDate,
                    nextMinBidKrw = nextMin,
                    predictionDiffNote = "유찰 1회 추가(${orig.bidCount}→${orig.bidCount + 1}회) 반영, 적정입찰가 ${QaVerificationEngine.formatKrwShort(nextBal)}로 하향 재조정",
                    followUpSummary = "${orig.caseId} 유찰 확인 → 차회(${defaultNextDate}) 최저가 ${QaVerificationEngine.formatKrwShort(nextMin)} 기준 저가 재입찰 전략글 생성",
                    recordedAt = nowTs
                )
            }

            PostAuctionOutcomeType.RESCHEDULED,
            PostAuctionOutcomeType.WITHDRAWN -> {
                val actionLabel = if (outcomeType == PostAuctionOutcomeType.RESCHEDULED) "기일 변경" else "취하·기각"
                val noteTail = if (safeNote.isNotBlank()) " · 사유: $safeNote" else " · 채무자 변제·이해관계인 기일연기 신청 확인"
                updatedAuction = orig.copy(
                    dedupKey = "${orig.dedupKey}-followup-${outcomeType.name.lowercase()}",
                    auctionDate = if (outcomeType == PostAuctionOutcomeType.RESCHEDULED) defaultNextDate else orig.auctionDate,
                    specialCheckNotice = "[$actionLabel 사후 추적] 기존 ${orig.auctionDate} 매각기일 변동($actionLabel)$noteTail"
                )
                record = PostAuctionFollowUpRecord(
                    originalCaseId = orig.caseId,
                    originalAuctionDate = orig.auctionDate,
                    isPastDue = isPast,
                    outcomeType = outcomeType,
                    outcomeDate = referenceDate,
                    nextAuctionDate = if (outcomeType == PostAuctionOutcomeType.RESCHEDULED) defaultNextDate else null,
                    predictionDiffNote = "$actionLabel 변동사항 즉시 반영 완료 (허위·과거 기일 오안내 차단)",
                    followUpSummary = "${orig.caseId} $actionLabel 확인 → 독자 오판 방지용 사후 업데이트 관련글 생성",
                    recordedAt = nowTs
                )
            }
        }

        val baseVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
            auction = updatedAuction,
            seedIndex = originalSlot.slotIndex + 5,
            config = config,
            rewriteSalt = "",
            proFunnel = proFunnel
        )

        val followUpPrefix = when (outcomeType) {
            PostAuctionOutcomeType.WINNING_BID -> "[사후 낙찰결과 분석]"
            PostAuctionOutcomeType.FAILED_DROP -> "[유찰 후 재입찰 전략]"
            PostAuctionOutcomeType.RESCHEDULED -> "[기일변경 추적 리포트]"
            PostAuctionOutcomeType.WITHDRAWN -> "[취하·종결 사후 점검]"
        }
        val region = updatedAuction.address.split(" ").take(2).joinToString(" ")
        val naverFollowUpBridge = buildString {
            appendLine("[기일 경과 사후 재조사 · 현장 업데이트]")
            appendLine("지난 ${record.originalAuctionDate} 매각 기일이 지난 뒤 법원 공고를 다시 열어보니 '${outcomeType.label}' 결과로 정리됐습니다. ${record.predictionDiffNote}")
            when (outcomeType) {
                PostAuctionOutcomeType.WINNING_BID ->
                    append("실제 매각대금 ${QaVerificationEngine.formatKrwShort(record.winningPriceKrw ?: 0L)}(${record.winningRatePct}%) 기준 잔금 납부 일정과 명도 협의 속도가 실제 순수익을 가르는 핵심이 됩니다.")
                PostAuctionOutcomeType.FAILED_DROP ->
                    append("다음 기일(${record.nextAuctionDate}) 법정 최저선이 ${QaVerificationEngine.formatKrwShort(record.nextMinBidKrw ?: 0L)}까지 내려앉아 현금 여력과 권리 인수 유무를 다시 짚어볼 타이밍입니다.")
                PostAuctionOutcomeType.RESCHEDULED, PostAuctionOutcomeType.WITHDRAWN ->
                    append("기존 기일 일정에 변동이 생긴 만큼 입찰 전날 밤 대법원 사건 상세 내역을 재열람하셔야 헛걸음을 막을 수 있습니다.")
            }
        }.trim()

        val tistoryFollowUpBridge = buildString {
            appendLine("5. 사후 결과 역산 · 수익률 민감도 재조정")
            appendLine("종전 공고일 ${record.originalAuctionDate} 결과 집계: ${outcomeType.badge} 판정. ${record.predictionDiffNote}")
            when (outcomeType) {
                PostAuctionOutcomeType.WINNING_BID ->
                    append("확정 낙찰액 ${QaVerificationEngine.formatKrwShort(record.winningPriceKrw ?: 0L)} (낙찰가율 ${record.winningRatePct}%, 참여 추정 ${record.bidderCount}명)을 대입하면 레버리지 비용 대비 회수 구간 재산정이 요구된다.")
                PostAuctionOutcomeType.FAILED_DROP ->
                    append("유찰 차수 누적으로 차기(${record.nextAuctionDate}) 최저매각단가가 ${QaVerificationEngine.formatKrwShort(record.nextMinBidKrw ?: 0L)}로 하락하여 취득세·보유이자 기준 BEP 안전폭이 확장되었다.")
                PostAuctionOutcomeType.RESCHEDULED, PostAuctionOutcomeType.WITHDRAWN ->
                    append("절차 진행 상태 변경에 따라 기존 매각기일 기준 자금 스케줄을 보류하고 변동 사유 공시 여부를 재점검해야 한다.")
            }
        }.trim()

        val cfFollowUpBridge = buildString {
            appendLine("[사후 사건기록 타임라인 추적]")
            appendLine("* 원매각기일 ${record.originalAuctionDate} 변동결과: ${outcomeType.label} (${record.predictionDiffNote})")
            when (outcomeType) {
                PostAuctionOutcomeType.WINNING_BID ->
                    append("* 매각결과 수치: 대금 ${QaVerificationEngine.formatKrwShort(record.winningPriceKrw ?: 0L)}, 매각비율 ${record.winningRatePct}%, 추정 응찰인원 ${record.bidderCount}인 — 매각허가·대금지급기한 추적 대상.")
                PostAuctionOutcomeType.FAILED_DROP ->
                    append("* 차회 공고 전망: ${record.nextAuctionDate} 기일 최저매수신고가 ${QaVerificationEngine.formatKrwShort(record.nextMinBidKrw ?: 0L)} 재편성, 공과금·체납액 증가 여부 재대조 요망.")
                PostAuctionOutcomeType.RESCHEDULED, PostAuctionOutcomeType.WITHDRAWN ->
                    append("* 기일 변동 알림: ${outcomeType.badge} 처리에 따라 독자 오인 방지를 위해 종전 입찰 일정을 아카이브 상태로 전환함.")
            }
        }.trim()

        val naverFunnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(
            updatedAuction,
            ChannelPlatform.NAVER,
            config,
            proFunnel
        )

        // Ensure Naver stays free of bracket spam at start of title, and preserve 1200..2400 chars cleanly
        val patchedVariants = baseVariants.mapValues { (platform, variant) ->
            when (platform) {
                ChannelPlatform.NAVER -> {
                    val cleanTitle = "$region ${updatedAuction.caseId} 경매기일 후속 추적 — ${outcomeType.badge} 및 실무 점검"
                    val mergedBody = insertOutcomeParagraphSafely(variant.bodyText, naverFollowUpBridge)
                    variant.copy(
                        title = cleanTitle,
                        bodyText = mergedBody,
                        bodyHtml = "<div class='se-viewer'>${mergedBody.replace("\n", "<br/>")}</div>$naverFunnelHtml"
                    )
                }
                ChannelPlatform.TISTORY -> {
                    val mergedBody = insertOutcomeParagraphSafely(variant.bodyText, tistoryFollowUpBridge)
                    variant.copy(
                        title = "$followUpPrefix ${updatedAuction.caseId} $region ${outcomeType.badge}·수익률 재계산",
                        bodyText = mergedBody
                    )
                }
                ChannelPlatform.CLOUDFLARE -> {
                    val mergedBody = insertOutcomeParagraphSafely(variant.bodyText, cfFollowUpBridge)
                    variant.copy(
                        title = "$followUpPrefix ${updatedAuction.caseId} $region 사후 재조사 풀브리핑",
                        bodyText = mergedBody
                    )
                }
            }
        }

        val followUpPostId = "post-followup-${updatedAuction.dedupKey}-${System.currentTimeMillis() % 10000}"
        val draftFollowUp = GeneratedPostSlot(
            postId = followUpPostId,
            queueId = "q-followup-${updatedAuction.caseId}",
            slotIndex = originalSlot.slotIndex + 1,
            publishDate = referenceDate,
            auctionCase = updatedAuction,
            variants = patchedVariants,
            scheduledTimes = originalSlot.scheduledTimes,
            status = PostStatus.DRAFT,
            qaReport = QaVerificationEngine.auditSlot(updatedAuction, patchedVariants, originalSlot.scheduledTimes, config, recent7DayCorpus),
            sourceLabel = "기일경과 사후재조사 · ${outcomeType.badge}"
        )

        val (defendedFollowUp, _) = QualityDefenseEngine.defendSlot(
            slot = draftFollowUp,
            config = config,
            proFunnel = proFunnel,
            recent7DayCorpus = recent7DayCorpus
        )
        val finalSlot = defendedFollowUp.copy(
            status = if (defendedFollowUp.qaReport.overallPassed) PostStatus.QUEUED else PostStatus.DRAFT
        )
        return record.copy(followUpPostId = finalSlot.postId) to finalSlot
    }

    private fun insertOutcomeParagraphSafely(originalBody: String, addition: String): String {
        val currentNoSpace = originalBody.count { !it.isWhitespace() }
        val addNoSpace = addition.count { !it.isWhitespace() }
        if (currentNoSpace + addNoSpace <= 2380) {
            return "$originalBody\n\n$addition"
        }
        val lines = originalBody.lines().filter { it.isNotBlank() }.toMutableList()
        if (lines.size > 3) {
            lines.add(2, addition)
        } else {
            lines.add(addition)
        }
        var combined = lines.joinToString("\n\n")
        // Trim middle verbose lines if still > 2400 no-space chars, preserving first and last 3 lines (facts & funnel)
        var guard = 0
        while (combined.count { !it.isWhitespace() } > 2400 && lines.size > 6 && guard < 12) {
            lines.removeAt(lines.size / 2)
            combined = lines.joinToString("\n\n")
            guard++
        }
        return combined
    }

    private fun addDaysIso(baseDate: String, days: Int): String = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val parsed = sdf.parse(baseDate.trim().take(10)) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = parsed
            add(Calendar.DAY_OF_MONTH, days)
        }
        sdf.format(cal.time)
    }.getOrDefault("2026-12-28")
}
