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

    private data class CandidateBlueprint(
        val caseId: String,
        val courtName: String,
        val address: String,
        val propertyType: PropertyType,
        val appraisalMan: Long,
        val minBidMan: Long,
        val bidCount: Int,
        val baseRightDate: String,
        val tenantNote: String,
        val assumedRights: String
    )

    private val diversePool = listOf(
        CandidateBlueprint("2026타경4102", "수원지방법원", "경기도 용인시 수지구 풍덕천동 래미안수지 105동 1102호", PropertyType.APT_OFFICETEL, 74000, 51800, 1, "2021-04-12", "소유자 세대 단독 점유(대항력 임차인 없음)", "말소기준권리 2021-04-12 근저당 이후 소멸, 공용관리비 95만원 확인"),
        CandidateBlueprint("2026타경4169", "서울서부지방법원", "서울특별시 은평구 응암동 신축 다세대주택 2층 201호", PropertyType.VILLA_MULTIFAMILY, 31000, 19840, 2, "2022-01-14", "후순위 임차인 보증금 소액배당 신청 완료(인수분 없음)", "말소기준권리 2022-01-14 이후 소멸, 위반건축물 미등재 확인"),
        CandidateBlueprint("2026타경4183", "서울동부지방법원", "서울특별시 광진구 자양동 근린상가 1층 105호", PropertyType.COMMERCIAL_RETAIL, 65000, 31850, 2, "2020-08-11", "현황 공실 상태(인테리어 원상복구 완료)", "장기공실 공용관리비 240만원 확인, 상임법 인수 보증금 없음"),
        CandidateBlueprint("2026타경4276", "청주지방법원", "충청북도 청주시 흥덕구 오송읍 계획관리 대지 412㎡", PropertyType.LAND_FARMLAND, 28500, 18240, 2, "2020-12-10", "나대지 현황(도로 6m 접면, 맹지 아님)", "말소기준권리 이후 소멸, 지상 제시외 건물 없음"),
        CandidateBlueprint("2026타경4258", "울산지방법원", "울산광역시 울주군 온산읍 국가산단 제조공장 및 호이스트 일괄", PropertyType.FACTORY_SPECIAL, 148000, 94720, 2, "2021-03-17", "자가 가동 중단 상태, 유치권 신고 없음", "공장저당법 제6조 기계기구 목록 일치, 40ft 대형차량 진입 가능"),
        CandidateBlueprint("2026타경4118", "서울북부지방법원", "서울특별시 노원구 상계동 주공6단지 608동 403호", PropertyType.APT_OFFICETEL, 59000, 37760, 2, "2020-11-19", "전입세대열람 소유자 세대 등재, 배당요구 임차인 없음", "말소기준권리 이후 권리 일괄 소멸, 공용관리비 118만원 확인"),
        CandidateBlueprint("2026타경4290", "광주지방법원", "광주광역시 서구 치평동 상무지구 근린상가 2층 203호", PropertyType.COMMERCIAL_RETAIL, 54000, 34560, 2, "2021-05-27", "후순위 임차인 영업 중(인도 협상 가능)", "상임법 환산보증금 후순위 소멸, 체납관리비 160만원 확인"),
        CandidateBlueprint("2026타경4339", "부산지방법원 서부지원", "부산광역시 사하구 하단동 역세권 다세대 4층 402호", PropertyType.VILLA_MULTIFAMILY, 22000, 14080, 2, "2021-08-04", "배당요구 후순위 임차인 거주(보증금 전액 소멸)", "말소기준권리 이후 소멸, 공용관리비 65만원 확인"),
        CandidateBlueprint("2026타경4489", "제주지방법원", "제주특별자치도 제주시 노형동 계획관리지역 대지 380㎡", PropertyType.LAND_FARMLAND, 49000, 31360, 2, "2021-02-11", "현황 나대지, 6m 포장도로 접면 확인", "말소기준권리 이후 소멸, 분묘·지상권 신고 없음"),
        CandidateBlueprint("2026타경4388", "천안지원", "충청남도 천안시 서북구 불당동 일반공업 지식산업센터 508호", PropertyType.FACTORY_SPECIAL, 36000, 23040, 2, "2021-06-18", "자가 사무실 공실 상태, 기계설비 제외", "말소기준권리 이후 소멸, 산업단지 입주업종 코드 적합"),
        CandidateBlueprint("2026타경4135", "인천지방법원", "인천광역시 연수구 송도동 더샵퍼스트월드 3동 1804호", PropertyType.APT_OFFICETEL, 86000, 60200, 1, "2021-09-03", "후순위 임차세대 점유(배당요구 종기 내 신청 완료)", "말소기준권리 2021-09-03 근저당 이후 후순위 소멸"),
        CandidateBlueprint("2026타경4372", "춘천지방법원", "강원특별자치도 원주시 반곡동 혁신도시 근린상가 1층 109호", PropertyType.COMMERCIAL_RETAIL, 46000, 29440, 2, "2020-10-20", "공실 상태(즉시 입점·임대 가능)", "말소기준권리 이후 소멸, 장기공실 관리비 190만원 반영"),
        CandidateBlueprint("2026타경4435", "서울중앙지방법원", "서울특별시 동작구 사당동 이수역 다세대주택 3층 301호", PropertyType.VILLA_MULTIFAMILY, 38000, 24320, 2, "2021-05-12", "후순위 임차인 배당신청 완료(인수금 없음)", "말소기준권리 이후 후순위 소멸, 전입세대 호수 일치"),
        CandidateBlueprint("2026타경4452", "수원지방법원 안산지원", "경기도 안산시 단원구 원시동 반월공단 창고형 공장 1동", PropertyType.FACTORY_SPECIAL, 182000, 116480, 2, "2020-07-16", "임차 법인 퇴거 완료, 전력 300kW 확보", "공장저당법 제6조 목록 대조 완료, 40ft 차량 회차 가능"),
        CandidateBlueprint("2026타경4676", "광주지방법원 순천지원", "전라남도 여수시 웅천동 계획관리 대지 450㎡", PropertyType.LAND_FARMLAND, 37000, 23680, 2, "2021-05-03", "현황 나대지, 6m 현황도로 접면(맹지 아님)", "말소기준권리 이후 소멸, 농취증 불필요 지목 대지"),
        CandidateBlueprint("2026타경4151", "부산지방법원", "부산광역시 해운대구 좌동 벽산1차 103동 902호", PropertyType.APT_OFFICETEL, 48000, 30720, 2, "2021-02-25", "채무자 겸 소유자 거주, 체납 관리비 확인 완료", "말소기준권리 이후 권리 소멸, 대법원 2001다8677 공용관리비 110만원"),
        CandidateBlueprint("2026타경4523", "수원지방법원 평택지원", "경기도 평택시 고덕면 고덕국제신도시 근린상가 1층 114호", PropertyType.COMMERCIAL_RETAIL, 72000, 35280, 2, "2020-11-05", "현황 공실 상태, 배후 아파트 4천세대 인접", "말소기준권리 이후 소멸, 체납관리비 215만원 산정"),
        CandidateBlueprint("2026타경4591", "청주지방법원 충주지원", "충청북도 음성군 대소면 산업단지 인접 창고용지 및 창고 1동", PropertyType.FACTORY_SPECIAL, 92000, 58880, 2, "2021-01-19", "자가 물류창고 공실, 층고 9m 확보", "말소기준권리 이후 소멸, 진입도로 10m 화물차 통행 양호"),
        CandidateBlueprint("2026타경4207", "대전지방법원", "대전광역시 유성구 봉명동 도안리버시티 오피스텔 912호", PropertyType.APT_OFFICETEL, 24000, 15360, 2, "2021-07-22", "공실 확인 및 공용관리비 정산내역 확보", "말소기준권리 이후 후순위 일괄 소멸, 전세수요 양호"),
        CandidateBlueprint("2026타경4659", "대구지방법원", "대구광역시 동구 신천동 동대구역 근린상가 1층 103호", PropertyType.COMMERCIAL_RETAIL, 61000, 39040, 2, "2020-09-14", "현황 공실 상태, 유동인구 밀집 역세권", "말소기준권리 이후 소멸, 상임법 인수분 없음"),
        CandidateBlueprint("2026타경4224", "대구지방법원", "대구광역시 수성구 범어동 수성위브더제니스 102동 2101호", PropertyType.APT_OFFICETEL, 112000, 54880, 2, "2020-06-30", "소유자 점유 확인, 대항력 임차인 없음", "말소기준권리 이후 소멸, 공용관리비 체납 185만원 확인"),
        CandidateBlueprint("2026타경4241", "창원지방법원", "경상남도 김해시 장유동 율하푸르지오 204동 701호", PropertyType.APT_OFFICETEL, 39000, 24960, 2, "2021-10-08", "후순위 임차인 명도협의 가능 상태", "말소기준권리 이후 일괄 소멸, 매각물건명세서 특이사항 없음"),
        CandidateBlueprint("2026타경4305", "수원지방법원 성남지원", "경기도 성남시 분당구 서현동 시범한양 312동 504호", PropertyType.APT_OFFICETEL, 98000, 68600, 1, "2021-01-28", "소유자 세대 점유(임차인 없음)", "말소기준권리 2021-01-28 근저당 이후 전부 소멸"),
        CandidateBlueprint("2026타경4321", "의정부지방법원", "경기도 남양주시 다산동 다산자이 109동 1401호", PropertyType.APT_OFFICETEL, 71000, 49700, 1, "2022-02-09", "전입세대열람 소유자 세대 단독 등재", "말소기준권리 이후 후순위 권리 일괄 소멸, 학군 입지 우수"),
        CandidateBlueprint("2026타경4354", "전주지방법원", "전북특별자치도 전주시 완산구 효자동 코오롱하늘채 107동 603호", PropertyType.APT_OFFICETEL, 41000, 26240, 2, "2021-11-15", "소유자 점유 상태, 별도 유치권·가처분 없음", "말소기준권리 이후 권리 소멸, 체납관리비 82만원 확인"),
        CandidateBlueprint("2026타경4404", "대구지방법원 서부지원", "대구광역시 달성군 다사읍 대실역 아파트 101동 1502호", PropertyType.APT_OFFICETEL, 44000, 28160, 2, "2021-03-03", "소유자 점유, 선순위 임차권 없음", "말소기준권리 이후 전부 소멸, 공용관리비 74만원 확인"),
        CandidateBlueprint("2026타경4419", "부산지방법원 동부지원", "부산광역시 기장군 일광읍 일광자이푸르지오 206동 1004호", PropertyType.APT_OFFICETEL, 52000, 36400, 1, "2022-04-06", "전입세대열람 확인 완료(대항력 임차인 없음)", "말소기준권리 이후 권리 소멸, 실거래 수요 탄탄"),
        CandidateBlueprint("2026타경4470", "인천지방법원 부천지원", "경기도 부천시 원미구 중동 위브더스테이트 6동 1903호", PropertyType.APT_OFFICETEL, 64000, 44800, 1, "2021-12-01", "소유자 점유 상태, 대항력 있는 임차인 없음", "말소기준권리 2021-12-01 근저당 이후 전부 소멸"),
        CandidateBlueprint("2026타경4506", "서울남부지방법원", "서울특별시 영등포구 당산동 삼성래미안 104동 805호", PropertyType.APT_OFFICETEL, 125000, 87500, 1, "2021-09-29", "소유자 세대 단독 거주, 배당요구 임차인 없음", "말소기준권리 이후 후순위 소멸, 공용관리비 130만원 확인"),
        CandidateBlueprint("2026타경4540", "대전지방법원 천안지원", "충청남도 아산시 배방읍 탕정역 아파트 108동 1201호", PropertyType.APT_OFFICETEL, 47000, 32900, 1, "2022-03-18", "소유자 점유, 선순위 권리 없음", "말소기준권리 이후 일괄 소멸, 공용관리비 68만원 확인"),
        CandidateBlueprint("2026타경4557", "서울북부지방법원", "서울특별시 성북구 길음동 길음뉴타운 402동 704호", PropertyType.APT_OFFICETEL, 84000, 58800, 1, "2021-07-07", "전입세대열람 소유자 세대 단독 등재", "말소기준권리 이후 권리 소멸, 학군 및 역세권 수요"),
        CandidateBlueprint("2026타경4574", "부산지방법원 동부지원", "부산광역시 수영구 광안동 역세권 오피스텔 1106호", PropertyType.APT_OFFICETEL, 27000, 17280, 2, "2021-04-28", "후순위 임차인 1세대(배당신청 완료)", "말소기준권리 이후 후순위 소멸, 체납관리비 52만원 확인"),
        CandidateBlueprint("2026타경4608", "서울서부지방법원", "서울특별시 마포구 공덕동 공덕자이 102동 604호", PropertyType.APT_OFFICETEL, 138000, 96600, 1, "2021-10-14", "소유자 세대 점유 확인 완료", "말소기준권리 이후 소멸, 공용관리비 145만원 반영"),
        CandidateBlueprint("2026타경4625", "수원지방법원 안양지원", "경기도 안양시 동안구 평촌동 초원대림 203동 1301호", PropertyType.APT_OFFICETEL, 79000, 55300, 1, "2022-01-25", "소유자 거주(임차인 없음)", "말소기준권리 2022-01-25 근저당 이후 일괄 소멸"),
        CandidateBlueprint("2026타경4642", "창원지방법원 마산지원", "경상남도 창원시 마산합포구 월영동 마린애시앙 111동 903호", PropertyType.APT_OFFICETEL, 43000, 27520, 2, "2021-08-30", "공실 상태 및 열쇠 인계 가능 확인", "말소기준권리 이후 권리 소멸, 관리비 정산 확인")
    )

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
            val needsSeedRotation = runs > 1 || !current.qaReport.similarityPassed
            val rebuiltVariants = if (!needsSeedRotation) {
                current.variants
            } else {
                DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                    auction = current.auctionCase,
                    seedIndex = current.slotIndex + runs * 5,
                    config = config,
                    rewriteSalt = "",
                    proFunnel = proFunnel
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
        val totalNeeded = safePerDay * days.coerceIn(1, 7)
        val usedCaseIds = existingCaseIds.toMutableSet()
        val rollingCorpus = recent7DayCorpus.toMutableList()
        val result = mutableListOf<GeneratedPostSlot>()
        var autoFixedCount = 0

        val startCal = parseCal(startDateIso)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

        // 1. Prioritize non-duplicate live Ddakchal server slots first
        val uniqueServerSlots = serverSyncedSlots.filter { slot ->
            usedCaseIds.add(slot.auctionCase.caseId)
        }

        var serverIdx = 0
        var poolIdx = 0

        for (dayOffset in 0 until days.coerceIn(1, 7)) {
            val dayCal = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, dayOffset) }
            val dateStr = fmt.format(dayCal.time)

            for (slotOfDay in 0 until safePerDay) {
                val globalIndex = dayOffset * safePerDay + slotOfDay + 1
                val scheduled = buildStaggeredTimesForDay(dateStr, slotOfDay, config)

                val candidateSlot = if (serverIdx < uniqueServerSlots.size) {
                    val srv = uniqueServerSlots[serverIdx++]
                    srv.copy(
                        slotIndex = globalIndex,
                        publishDate = dateStr,
                        scheduledTimes = scheduled,
                        sourceLabel = "딱찰 실서버 주간예약 (${dayOffset + 1}일차)"
                    )
                } else {
                    var bp: CandidateBlueprint? = null
                    while (poolIdx < diversePool.size) {
                        val next = diversePool[poolIdx++]
                        if (usedCaseIds.add(next.caseId)) {
                            bp = next
                            break
                        }
                    }
                    val chosen = bp ?: run {
                        val fallbackIdx = diversePool.size + globalIndex
                        val template = diversePool[globalIndex % diversePool.size]
                        val genCase = "2026타경${5000 + fallbackIdx * 13}"
                        usedCaseIds.add(genCase)
                        template.copy(caseId = genCase)
                    }
                    val auction = buildAuctionFromBlueprint(chosen, dateStr, globalIndex)
                    val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                        auction = auction,
                        seedIndex = globalIndex,
                        config = config,
                        proFunnel = proFunnel
                    )
                    val qa = QaVerificationEngine.auditSlot(auction, variants, scheduled, config, rollingCorpus)
                    GeneratedPostSlot(
                        postId = "post-${auction.dedupKey}",
                        queueId = "q-wk-${dateStr.replace("-", "")}-${slotOfDay + 1}",
                        slotIndex = globalIndex,
                        publishDate = dateStr,
                        auctionCase = auction,
                        variants = variants,
                        scheduledTimes = scheduled,
                        status = if (qa.overallPassed) PostStatus.QUEUED else PostStatus.DRAFT,
                        qaReport = qa,
                        sourceLabel = "주간 7일 자동예약 (${dayOffset + 1}일차 · 일${safePerDay}건)"
                    )
                }

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
                if (result.size >= totalNeeded) break
            }
        }

        return result to autoFixedCount
    }

    private fun buildAuctionFromBlueprint(
        bp: CandidateBlueprint,
        publishDate: String,
        seed: Int
    ): AuctionCaseIntel {
        val appraisalKrw = bp.appraisalMan * 10_000L
        val minBidKrw = bp.minBidMan * 10_000L
        val discountPct = ((minBidKrw.toDouble() / appraisalKrw.toDouble()) * 100.0).toInt().coerceIn(20, 95)
        val auctionDateCal = parseCal(publishDate).apply { add(Calendar.DAY_OF_MONTH, 12 + (seed % 9)) }
        val auctionDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(auctionDateCal.time)

        return AuctionCaseIntel(
            caseId = bp.caseId,
            dedupKey = "ca-wk-${bp.caseId}-${seed}",
            courtName = bp.courtName,
            address = bp.address,
            propertyType = bp.propertyType,
            appraisalPriceKrw = appraisalKrw,
            minBidPriceKrw = minBidKrw,
            winningPriceKrw = null,
            bidCount = bp.bidCount,
            discountRatePct = discountPct,
            auctionDate = auctionDateStr,
            baseRightDate = bp.baseRightDate,
            dividendDeadline = "2026-11-30",
            claimAmountKrw = (appraisalKrw * 0.54).toLong(),
            depositRatePct = 10,
            possessionSummary = bp.tenantNote,
            tenantSummary = bp.tenantNote,
            assumedRightsText = bp.assumedRights,
            specialCheckNotice = bp.propertyType.focusSummary,
            recentRealTradeMedianKrw = (appraisalKrw * 0.91).toLong(),
            stats3yRatePct = 68.5 + (seed % 11),
            clicksLast7d = 460 + seed * 17,
            conservativeBidKrw = (minBidKrw * 1.04).toLong(),
            balancedBidKrw = (minBidKrw * 1.11).toLong(),
            aggressiveBidKrw = (minBidKrw * 1.18).toLong()
        )
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
