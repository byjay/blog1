package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.GeneratedPostSlot
import com.example.model.InfographicCardSpec
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.PublishLogEntry
import com.example.model.ServerAutomationConfig

object DefaultSpecDataFactory {

    fun buildInitialDaily5Slots(
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig = ProFunnelConfig()
    ): List<GeneratedPostSlot> {
        val cases = listOf(
            AuctionCaseIntel(
                caseId = "2025타경6297",
                dedupKey = "ca-20250118006297-1",
                courtName = "부산지방법원 동부지원",
                address = "부산광역시 기장군 정관읍 정관롯데캐슬2차 104동 1203호",
                propertyType = PropertyType.APT_OFFICETEL,
                appraisalPriceKrw = 420_000_000L,
                minBidPriceKrw = 268_800_000L,
                winningPriceKrw = null,
                bidCount = 2,
                discountRatePct = 64,
                auctionDate = "2026-12-03",
                baseRightDate = "2021-06-15",
                dividendDeadline = "2025-11-28",
                claimAmountKrw = 195_000_000L,
                depositRatePct = 10,
                possessionSummary = "채무자 겸 소유자 세대 점유(임차인 없음)",
                tenantSummary = "전입세대열람내역 소유자 세대 단독 등재, 배당요구 임차인 없음",
                assumedRightsText = "말소기준권리 2021-06-15 근저당 이후 전부 소멸, 공용관리비 체납 142만원 확인",
                specialCheckNotice = "대법원 2001다8677 판례상 체납 공용부분 관리비(3년 원금) 낙찰자 승계 여부 입찰 당일 관리사무소 재확인",
                recentRealTradeMedianKrw = 375_000_000L,
                stats3yRatePct = 78.4,
                clicksLast7d = 612,
                conservativeBidKrw = 289_500_000L,
                balancedBidKrw = 304_200_000L,
                aggressiveBidKrw = 318_000_000L
            ),
            AuctionCaseIntel(
                caseId = "2025타경11842",
                dedupKey = "ca-20250204011842-1",
                courtName = "서울남부지방법원",
                address = "서울특별시 강서구 화곡동 다세대주택 3층 302호",
                propertyType = PropertyType.VILLA_MULTIFAMILY,
                appraisalPriceKrw = 295_000_000L,
                minBidPriceKrw = 151_040_000L,
                winningPriceKrw = null,
                bidCount = 3,
                discountRatePct = 51,
                auctionDate = "2026-12-04",
                baseRightDate = "2022-03-11",
                dividendDeadline = "2025-10-17",
                claimAmountKrw = 210_000_000L,
                depositRatePct = 10,
                possessionSummary = "선순위 임차세대 점유 (말소기준일 이전 대항력 요건 구비·세부일자 비공개)",
                tenantSummary = "말소기준권리(2022-03-11)보다 선순위 임차세대 보증금 1억 8,000만원, 배당요구 미신청",
                assumedRightsText = "선순위 임차세대 대항력 유지 및 배당요구 없음 → 보증금 1억 8,000만원 전액 인수 위험",
                specialCheckNotice = "전세사기 예방 필수 대조 항목: 배당요구 미필 선순위 임차인 보증금 인수분 반영 시 실투자금 급증 주의",
                recentRealTradeMedianKrw = 265_000_000L,
                stats3yRatePct = 66.8,
                clicksLast7d = 488,
                conservativeBidKrw = 151_500_000L,
                balancedBidKrw = 158_000_000L,
                aggressiveBidKrw = 164_000_000L
            ),
            AuctionCaseIntel(
                caseId = "2025타경8831",
                dedupKey = "ca-20250129008831-1",
                courtName = "수원지방법원 성남지원",
                address = "경기도 성남시 분당구 정자동 근린상가 1층 108호",
                propertyType = PropertyType.COMMERCIAL_RETAIL,
                appraisalPriceKrw = 780_000_000L,
                minBidPriceKrw = 382_200_000L,
                winningPriceKrw = null,
                bidCount = 2,
                discountRatePct = 49,
                auctionDate = "2026-12-08",
                baseRightDate = "2020-09-22",
                dividendDeadline = "2025-11-12",
                claimAmountKrw = 430_000_000L,
                depositRatePct = 10,
                possessionSummary = "현황 공실 상태(최근까지 카페 운영 후 폐업)",
                tenantSummary = "사업자등록상 후순위 임차인 퇴거 확인, 상임법 환산보증금 인수분 없음",
                assumedRightsText = "장기공실에 따른 체납 공용관리비 680만원 확인, 권리 인수 사항 없음",
                specialCheckNotice = "반경 500m 오피스·주거 배후세대 대비 1층 공실률 및 예상 CapRate 6.1% 수준 검토 필요",
                recentRealTradeMedianKrw = 640_000_000L,
                stats3yRatePct = 62.5,
                clicksLast7d = 734,
                conservativeBidKrw = 398_000_000L,
                balancedBidKrw = 421_000_000L,
                aggressiveBidKrw = 445_000_000L
            ),
            AuctionCaseIntel(
                caseId = "2024타경4507",
                dedupKey = "ca-20240130004507-1",
                courtName = "대구지방법원 포항지원",
                address = "경상북도 포항시 북구 흥해읍 토지(전) 일괄매각",
                propertyType = PropertyType.LAND_FARMLAND,
                appraisalPriceKrw = 4_864_120_000L,
                minBidPriceKrw = 2_383_420_000L,
                winningPriceKrw = null,
                bidCount = 2,
                discountRatePct = 49,
                auctionDate = "2026-12-09",
                baseRightDate = "2019-12-04",
                dividendDeadline = "2025-09-30",
                claimAmountKrw = 1_850_000_000L,
                depositRatePct = 10,
                possessionSummary = "지목 전(농지), 현황 휴경지 및 일부 비닐하우스 소재",
                tenantSummary = "등기부상 임차권 및 지상권 등기 없음, 제시외 비닐하우스 매각 제외 여부 확인",
                assumedRightsText = "4m 이상 현황도로 접면(맹지 아님), 분묘기지권 기일 내 신고 없음",
                specialCheckNotice = "농지취득자격증명원 매각결정기일까지 미제출 시 입찰보증금 10% 전액 몰수 주의",
                recentRealTradeMedianKrw = 3_920_000_000L,
                stats3yRatePct = 59.3,
                clicksLast7d = 845,
                conservativeBidKrw = 2_410_000_000L,
                balancedBidKrw = 2_540_000_000L,
                aggressiveBidKrw = 2_690_000_000L
            ),
            AuctionCaseIntel(
                caseId = "2025타경9405",
                dedupKey = "ca-20250211009405-1",
                courtName = "인천지방법원",
                address = "인천광역시 서구 오류동 일반공업지역 공장 및 기계기구 일괄",
                propertyType = PropertyType.FACTORY_SPECIAL,
                appraisalPriceKrw = 1_620_000_000L,
                minBidPriceKrw = 1_134_000_000L,
                winningPriceKrw = null,
                bidCount = 1,
                discountRatePct = 70,
                auctionDate = "2026-12-10",
                baseRightDate = "2021-08-19",
                dividendDeadline = "2025-12-01",
                claimAmountKrw = 980_000_000L,
                depositRatePct = 10,
                possessionSummary = "채무자 법인 자가 가동 중단 상태, 호이스트 5톤 2기 설치",
                tenantSummary = "유치권 신고서(설비 공사업체 비식별, 1억 4,000만원) 제출 — 점유 개시 시점 말소기준권리 이후",
                assumedRightsText = "공장저당법 제6조 기계기구 7점 목록 감정평가 포함, 대항력 있는 유치권 성립 불투명",
                specialCheckNotice = "40ft 대형 화물차 진입 도로폭 확보(8m) 및 계약전력 250kW, 산업단지 입주업종 코드 적합 확인 필수",
                recentRealTradeMedianKrw = 1_410_000_000L,
                stats3yRatePct = 71.2,
                clicksLast7d = 529,
                conservativeBidKrw = 1_145_000_000L,
                balancedBidKrw = 1_195_000_000L,
                aggressiveBidKrw = 1_240_000_000L
            )
        )

        val jitterOffsets = listOf(
            Triple("08:17:23", "09:04:41", "10:29:15"),
            Triple("10:42:11", "11:31:08", "12:58:44"),
            Triple("13:14:37", "14:02:19", "15:26:52"),
            Triple("15:41:09", "16:28:33", "17:49:18"),
            Triple("16:12:46", "17:03:22", "18:21:05")
        )

        return cases.mapIndexed { idx, auction ->
            val variants = buildDistinct3ChannelVariants(auction, idx, config, proFunnel = proFunnel)
            val (tCf, tTs, tNv) = jitterOffsets[idx]
            val scheduled = mapOf(
                ChannelPlatform.CLOUDFLARE to "2026-11-22 $tCf",
                ChannelPlatform.TISTORY to "2026-11-22 $tTs",
                ChannelPlatform.NAVER to "2026-11-22 $tNv"
            )
            val qaReport = QaVerificationEngine.auditSlot(auction, variants, scheduled, config)
            val status = when {
                idx == 0 -> PostStatus.PUBLISHED
                idx == 1 -> PostStatus.QUEUED
                else -> PostStatus.DRAFT
            }
            val publishedUrls = if (idx == 0) {
                mapOf(
                    ChannelPlatform.CLOUDFLARE to "${config.ddakchalApiBase}/blog/${auction.dedupKey}",
                    ChannelPlatform.TISTORY to "https://ddakchal-report.tistory.com/entry/${auction.caseId}",
                    ChannelPlatform.NAVER to "https://blog.naver.com/ddakchal_official/223698104211"
                )
            } else {
                emptyMap()
            }
            GeneratedPostSlot(
                postId = "post-${auction.dedupKey}",
                queueId = "q-20261122-${idx + 1}",
                slotIndex = idx + 1,
                publishDate = "2026-11-22",
                auctionCase = auction,
                variants = variants,
                scheduledTimes = scheduled,
                status = status,
                qaReport = qaReport,
                publishedUrls = publishedUrls,
                sourceLabel = "SPEC v13.4 기본 5슬롯"
            )
        }
    }

    fun buildDistinct3ChannelVariants(
        auction: AuctionCaseIntel,
        seedIndex: Int,
        config: ServerAutomationConfig,
        rewriteSalt: String = "",
        proFunnel: ProFunnelConfig = ProFunnelConfig()
    ): Map<ChannelPlatform, ChannelVariantContent> = buildSanitized3ChannelVariants(
        auction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction),
        seedIndex = seedIndex,
        config = config,
        rewriteSalt = rewriteSalt,
        proFunnel = proFunnel
    )

    private fun buildSanitized3ChannelVariants(
        auction: AuctionCaseIntel,
        seedIndex: Int,
        config: ServerAutomationConfig,
        rewriteSalt: String = "",
        proFunnel: ProFunnelConfig = ProFunnelConfig()
    ): Map<ChannelPlatform, ChannelVariantContent> {
        val region = auction.address.split(" ").take(2).joinToString(" ")
        val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)
        val appraisalStr = QaVerificationEngine.formatKrwShort(auction.appraisalPriceKrw)
        val minBidStr = QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw)
        val claimStr = QaVerificationEngine.formatKrwShort(auction.claimAmountKrw)
        val realTradeStr = QaVerificationEngine.formatKrwShort(auction.recentRealTradeMedianKrw)
        val consStr = QaVerificationEngine.formatKrwShort(auction.conservativeBidKrw)
        val balStr = QaVerificationEngine.formatKrwShort(auction.balancedBidKrw)
        val aggStr = QaVerificationEngine.formatKrwShort(auction.aggressiveBidKrw)
        val depositKrwStr = QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw * auction.depositRatePct / 100L)
        val ltv70LoanStr = QaVerificationEngine.formatKrwShort(proReport.ltv70LoanKrw)
        val equityNeedStr = QaVerificationEngine.formatKrwShort(proReport.netEquityRequiredKrw)
        val marginVsTradeStr = QaVerificationEngine.formatKrwShort(proReport.safetyMarginKrw)
        val evictionReserveStr = QaVerificationEngine.formatKrwShort(proReport.estimatedEvictionReserveKrw)
        val extraCostStr = QaVerificationEngine.formatKrwShort(proReport.assumedExtraCostKrw)
        val bepStr = QaVerificationEngine.formatKrwShort(proReport.breakevenResalePriceKrw)
        val nextDropStr = QaVerificationEngine.formatKrwShort(proReport.nextDropMinBidKrw)
        val stressDeltaStr = QaVerificationEngine.formatKrwShort(proReport.rateStressMonthlyDeltaKrw)

        val intro = BoilerplateAndTemplateEngine.introHookPool[seedIndex % BoilerplateAndTemplateEngine.introHookPool.size]
        val transition = BoilerplateAndTemplateEngine.transitionPhrasePool[seedIndex % BoilerplateAndTemplateEngine.transitionPhrasePool.size]
        val cta = BoilerplateAndTemplateEngine.ctaClosingPool[(seedIndex * 3 + 2) % BoilerplateAndTemplateEngine.ctaClosingPool.size]
        val utmBase = "${config.ddakchalApiBase}/?utm_source=blog&utm_campaign=daily5&case=${auction.caseId}"

        val registryFacts = RegistryAndMotionEngine.buildRegistryFacts(auction)
        val gapGuSummary = registryFacts.filter { it.section == com.example.model.RegistrySection.GAP_GU }
            .joinToString(", ") { "${it.orderNo} ${it.rightTitle}(${it.outcomeLabel})" }
        val eulGuSummary = registryFacts.filter { it.section == com.example.model.RegistrySection.EUL_GU }
            .joinToString(", ") { "${it.orderNo} ${it.registeredDate} ${it.rightTitle} ${it.amountText}" }
        val tenantRegistryNote = registryFacts.firstOrNull { it.section == com.example.model.RegistrySection.TENANT_COURT }
            ?.let { "${it.rightTitle} → ${it.outcomeLabel}" }
            ?: auction.tenantSummary

        val naverShotsText = proReport.photoGuideShots.take(4).joinToString("\n") { shot ->
            "${shot.shotNumber}. ${shot.targetTitle}: ${shot.checkPoint}. 여기서 가장 주의할 신호는 ${shot.redFlagSignal}입니다."
        }
        val naverFunnelTail = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(auction, ChannelPlatform.NAVER, config, proFunnel)
        val naverPinnedComment = ProAnalysisAndPhotoGuideEngine.buildNaverPinnedComment(auction, proFunnel)
        val naverTitle = RegistryAndMotionEngine.buildHumanVeteranNaverTitle(auction)

        // 1. NAVER (Human 30-year veteran field essay & deed anatomy, zero emojis, zero ■/Q1~Q4 AI clichés, zero direct URL)
        val naverBody = buildString {
            appendLine("$intro $rewriteSalt")
            appendLine("현장에서 30년 넘게 권리관계와 등기부등본을 직접 대조해 오면서 가장 안타까웠던 순간은, 숫자 몇 줄만 미리 확인했어도 막을 수 있었던 손실을 입찰 뒤에 뒤늦게 발견하는 경우입니다.")
            appendLine("오늘은 ${auction.courtName} ${auction.caseId} 물건을 두고, 백 명 넘는 실무 전담팀이 실제 임장 나가기 전에 반드시 손으로 짚어보는 순서 그대로 정리해 드립니다.")
            appendLine()
            appendLine("[입지·시세]")
            appendLine("${region} 권역에 나온 이번 ${auction.propertyType.displayLabel} 물건은 처음 책정된 감정가 $appraisalStr 에서 몇 차례 주인을 찾지 못해 현재 최저가 $minBidStr 선(${auction.discountRatePct}% 구간)으로 새 기일을 앞두고 있습니다.")
            appendLine("입찰 당일 아침 법정에 지참해야 할 보증금은 $depositKrwStr (${auction.depositRatePct}%)이며, 주변 최근 거래 중심가격이 $realTradeStr 언저리라 부대비용 $extraCostStr 를 제하고도 약 $marginVsTradeStr 만큼의 가격 여력이 보입니다.")
            appendLine("최근 일주일 동안에만 ${auction.clicksLast7d}명이 넘게 이 사건을 찾아봤고, 같은 동네 최근 3년 낙찰 비율이 ${auction.stats3yRatePct}%를 오르내리는 만큼 눈치 싸움이 적지 않을 것으로 읽힙니다.")
            appendLine()
            appendLine("[등기부 갑구·을구]")
            appendLine("서류에서 제일 먼저 펼쳐야 할 대목은 등기부등본입니다. 갑구 흐름은 $gapGuSummary 로 이어지고, 을구에서는 $eulGuSummary 가 기준선 역할을 맡습니다.")
            appendLine("즉 ${auction.baseRightDate} 설정 등기가 모든 권리의 지워지는 출발점이며, 현장과 임차 현황을 맞대어 본 결과는 '$tenantRegistryNote'로 압축됩니다.")
            appendLine("인도 과정은 실무 경험상 평균 ${proReport.estimatedEvictionDays}일 안팎의 협의 기간과 $evictionReserveStr 정도의 명도 예비비를 원가에 녹여 두시는 편이 마음 편합니다.")
            appendLine()
            appendLine("[현장 4컷 실사]")
            appendLine("서류가 깨끗해 보여도 발품을 건너뛰면 엉뚱한 수리비나 체납금에 발목이 잡힙니다. 현장에 도착하시면 휴대폰 카메라로 아래 네 장면부터 순서대로 찍어 기록해 두십시오.")
            appendLine(naverShotsText)
            appendLine()
            appendLine("[입찰선·딱찰 가입 안내]")
            appendLine("은행 대출 70%를 활용할 때 손에 쥐어야 할 현금은 약 $equityNeedStr 선입니다. 취득세·명도비·1년 보유이자까지 모두 넣은 손익분기 매도선은 $bepStr 이며, 한 번 더 유찰된다면 다음 최저가는 $nextDropStr 구간까지 내려갑니다.")
            appendLine("보수적 안전선은 $consStr, 흐름에 맞춘 기준선은 $balStr, 입지 확보가 최우선이라면 $aggStr 안팎까지 개인·공동·법인 세금 유불리를 미리 따져보셔야 합니다.")
            appendLine("입찰 전날 밤 법원 공고 변경 여부를 한 번 더 살피시고, $transition $cta")
            append(naverFunnelTail)
        }.trim()

        val tistoryWaterfallLines = proReport.dividendWaterfall.joinToString(" -> ") { step ->
            "${step.priorityRank}순 ${step.creditorLabel}[${step.statusTag}]"
        }
        val tistoryTaxRows = proReport.taxScenarios.joinToString("") { t ->
            "<tr><td>${t.entityType}</td><td>${t.acquisitionTaxPct}</td><td>${t.capitalGainsTaxNote}</td></tr>"
        }
        val tistoryFunnelText = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(auction, ChannelPlatform.TISTORY, config, proFunnel)
        val tistoryFunnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(auction, ChannelPlatform.TISTORY, config, proFunnel)

        // 2. TISTORY (Analytical financial, Dividend Waterfall, CapRate/ROE, 3-Tax Report & CAPEX Checklist)
        val tistoryBodyText = buildString {
            appendLine("[$region ${auction.propertyType.displayLabel} 자산가치 · 배당 · 세무 리포트] ${auction.caseId}")
            appendLine("본 리서치 노트는 30년 업력의 경매중개법인 자문본부가 정리한 정량 밸류에이션 모형과 배당·세제 비교표를 무상 공개하는 실전 분석서이다.")
            appendLine()
            appendLine("1. 밸류에이션 지표 및 가격 할인 구조")
            appendLine("사건 ${auction.caseId}는 최초 감정평가금액 $appraisalStr 대비 ${auction.bidCount}차 유찰로 최저매각가격 $minBidStr(${auction.discountRatePct}%)가 책정되었으며 매각 기일은 ${auction.auctionDate}로 지정되었다.")
            appendLine("권리 소멸 기준 시점은 ${auction.baseRightDate}, 청구 채권액은 $claimStr, 배당요구 종기는 ${auction.dividendDeadline}으로 집계된다.")
            appendLine("국토부 거래 중앙값 $realTradeStr 과 비교한 명목 차익 폭은 $marginVsTradeStr(${proReport.safetyMarginPct}%)이며, 부대·승계 리스크 충당 추정치는 $extraCostStr 규모다.")
            appendLine()
            appendLine("2. 채권 배당 워터폴 모델 및 인도 비용 산정")
            appendLine("표준 응찰가 $balStr 가정 시 배당 전개는 { $tistoryWaterfallLines } 순으로 배분된다.")
            appendLine("점유·인도 등급은 '${proReport.evictionRiskLevel}' 수준이며 예상 완료 기간 ${proReport.estimatedEvictionDays}일, 협상 예비 충당액 $evictionReserveStr 을 총매입원가에 편입해야 한다.")
            appendLine("특히 배당표상 경매 집행비용과 당해세 공제 이후 선순위 채권자의 잉여금 또는 미배당 잔여분이 인수 요인으로 전환되지 않는지 잔금 납부 전 재검증해야 한다.")
            appendLine()
            appendLine("3. 레버리지 현금흐름 · CapRate · 12개월 ROE 추정")
            appendLine("해당 시군구 36개월 누적 매각가율은 ${auction.stats3yRatePct}%, 주간 열람 수요 ${auction.clicksLast7d}건을 기록하였다.")
            appendLine("LTV 70% 담보대출($ltv70LoanStr) 실행 시 소요 Equity는 약 $equityNeedStr 이며, 추정 임대 CapRate는 연 ${proReport.estimatedCapRatePct}%, 1년 단기 회전 모델 기대 ROE는 ${proReport.estimated12mRoiPct}%로 계산된다.")
            appendLine("예상 월 임대가치(${QaVerificationEngine.formatKrwShort(proReport.estimatedMonthlyRentKrw)}) 대비 금리 +1.0%p 스트레스(월 +$stressDeltaStr) 반영 손익분기 매도단가는 $bepStr 로 산출된다.")
            appendLine()
            appendLine("4. 가격 스펙트럼 · 세제 3안 · CAPEX 점검 항목")
            appendLine("투자 성향별 응찰 스펙트럼은 1차 방어선 $consStr · 표준선 $balStr · 적극 상단 $aggStr 로 제시된다.")
            appendLine("단독 취득(1주택·장기보유공제 유리), 부부 공동 지분 분산(과세표준 5:5 분산 및 기본공제 250만원 이중 적용), 매매사업자 법인 취득(이자·수선비 손금 인정 및 세율 9~19%) 간 세후 수익 손익분기점을 비교한 뒤 낙찰 명의를 확정해야 한다.")
            appendLine("임장 과정에서는 ${proReport.photoGuideShots.joinToString(", ") { it.targetTitle }} 대조를 통해 리모델링 및 수선 오차를 통제할 것을 권장한다.")
            append(tistoryFunnelText)
        }.trim()

        val tistoryBodyHtml = """
<article>
  <h1>[$region ${auction.propertyType.displayLabel} 자산가치 리포트] ${auction.caseId}</h1>
  <p>${tistoryBodyText.replace("\n", "<br/>")}</p>
  <h2>사건 제원 · 수익률 · 세무 3안 비교표</h2>
  <table>
    <tr><th>사건번호</th><td>${auction.caseId}</td><th>매각기일</th><td>${auction.auctionDate}</td></tr>
    <tr><th>감정가 / 최저가</th><td>$appraisalStr / $minBidStr</td><th>안전마진 / CapRate</th><td>$marginVsTradeStr / ${proReport.estimatedCapRatePct}%</td></tr>
    <tr><th>LTV70 자기자본</th><td>$equityNeedStr</td><th>12개월 기대 ROE</th><td>${proReport.estimated12mRoiPct}%</td></tr>
  </table>
  <table>
    <tr><th>명의 유형</th><th>취득세 구간</th><th>양도/법인세 핵심 포인트</th></tr>
    $tistoryTaxRows
  </table>
  $tistoryFunnelHtml
</article>
        """.trimIndent()

        val cfFunnelText = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(auction, ChannelPlatform.CLOUDFLARE, config, proFunnel)
        val cfFunnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(auction, ChannelPlatform.CLOUDFLARE, config, proFunnel)

        // 3. CLOUDFLARE (GEO/AIO Answer-first encyclopedic briefing + official 4-doc citations + caption blueprint + FAQ)
        val cloudflareBodyText = buildString {
            appendLine("[두괄식 요지 · ${auction.courtName}] 사건번호 ${auction.caseId} (${auction.address} / 분류: ${auction.propertyType.blogCategory})는 법원 감정평가 ${appraisalStr}에서 ${auction.bidCount}회 유찰을 지나 ${minBidStr}(${auction.discountRatePct}%) 조건으로 ${auction.auctionDate} 공고된 매각 건임.")
            appendLine("기준권리일자 ${auction.baseRightDate}, 신청채권 $claimStr, 배당마감일 ${auction.dividendDeadline} 확인 완료. 인근 거래가 대비 할인 폭 ${proReport.safetyMarginPct}% 및 연 임대가치 비율 ${proReport.estimatedCapRatePct}% 기록.")
            appendLine()
            appendLine("[대법원 4대 공시 문건 요약]")
            appendLine("* 매각물건명세서: ${auction.assumedRightsText}")
            appendLine("* 집행관 현황조사서: ${auction.possessionSummary}")
            appendLine("* 임대차 및 배당내역: ${auction.tenantSummary}")
            appendLine("* 물건유형 특이요목: ${auction.specialCheckNotice}")
            appendLine("* 점유 및 자금요건: ${proReport.evictionRiskLevel}, 담보대출 70% 적용 시 투입 원금 $equityNeedStr 수준. 매각결정 이후 잔금 납부 기한(통상 1개월 내외) 내 경락대출 실행 가능 여부 사전 타진 요망.")
            appendLine()
            appendLine("[현장 사진 5단계 검증 규격]")
            proReport.photoGuideShots.forEach { shot ->
                appendLine("* 포착 ${shot.shotNumber}단계: ${shot.blogCaptionTemplate} (적발 포인트: ${shot.redFlagSignal})")
            }
            appendLine()
            appendLine("[통계치 · 3구간 입찰가 · 스키마 FAQ]")
            appendLine("* 실거래 중위가 $realTradeStr / 시군구 36개월 매각비율 ${auction.stats3yRatePct}% / 매수신청보증금 $depositKrwStr")
            appendLine("* 제시 가격대: 하단선 $consStr / 중간선 $balStr / 상단선 $aggStr (총원가 손익분기 $bepStr / 차회 유찰예상 $nextDropStr)")
            appendLine("문답1. ${auction.caseId} 권리 인수 여부의 핵심 판단 근거는? 답1. 등기부 기준일자(${auction.baseRightDate}) 선후 대조 및 최우선변제·체납관리비·당해세 공제 이후 미배당 잔액 유무를 검증하는 데 있음.")
            appendLine("문답2. 응찰 당일 지참 서류 및 보증금 규정은? 답2. 신분증, 도장, 최저가 ${auction.depositRatePct}%에 해당하는 $depositKrwStr 자기앞수표 1매 및 대리 입찰 시 인감증명 첨부 위임장을 준비해야 함.")
            appendLine("문답3. 공시 데이터 출처와 면책 조항은? 답3. 대법원 경매정보 및 국토교통부 실거래 공개시스템 원문 집계이며 최종 입찰 결정 책임은 응찰자 본인에게 있음.")
            append(cfFunnelText)
        }.trim()

        val jsonLd = """
{
  "@context": "https://schema.org",
  "@graph": [
    {
      "@type": "Article",
      "headline": "${auction.caseId} $region ${auction.propertyType.displayLabel} 경매 권리·세무·사진가이드 풀분석",
      "datePublished": "2026-11-22T08:17:00+09:00",
      "author": { "@type": "Organization", "name": "딱찰(DDAKCHAL)" },
      "about": "${auction.address}"
    },
    {
      "@type": "FAQPage",
      "mainEntity": [
        {
          "@type": "Question",
          "name": "${auction.caseId} 말소기준권리와 인수 위험은 무엇인가요?",
          "acceptedAnswer": { "@type": "Answer", "text": "말소기준권리는 ${auction.baseRightDate}이며 ${auction.assumedRightsText}" }
        },
        {
          "@type": "Question",
          "name": "${auction.caseId} 3전략 권장 입찰 구간과 실투자금은 얼마인가요?",
          "acceptedAnswer": { "@type": "Answer", "text": "보수 $consStr, 적정 $balStr, 공격 $aggStr 이며 LTV 70% 기준 자기자본은 약 ${equityNeedStr}입니다." }
        }
      ]
    }
  ]
}
        """.trimIndent()

        val cloudflareBodyHtml = """
<section class="tldr-box"><strong>TL;DR</strong> ${auction.caseId} 최저가 $minBidStr(${auction.discountRatePct}%) · 적정 입찰가 $balStr · 안전마진 $marginVsTradeStr</section>
<article><p>${cloudflareBodyText.replace("\n", "<br/>")}</p></article>
$cfFunnelHtml
<script type="application/ld+json">$jsonLd</script>
        """.trimIndent()

        val cardsByChannel = ChannelPlatform.entries.associateWith { platform ->
            ProAnalysisAndPhotoGuideEngine.build6InfographicCards(
                auction = auction,
                platform = platform,
                minBidStr = minBidStr,
                report = proReport,
                funnel = proFunnel,
                salt = rewriteSalt
            )
        }

        val commonTags = listOf(
            "딱찰",
            "부동산경매",
            auction.propertyType.displayLabel.replace("·", ""),
            region.replace(" ", ""),
            auction.caseId,
            "무료권리분석",
            "임장사진가이드"
        )

        val naverFunnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(auction, ChannelPlatform.NAVER, config, proFunnel)

        return mapOf(
            ChannelPlatform.CLOUDFLARE to ChannelVariantContent(
                platform = ChannelPlatform.CLOUDFLARE,
                categoryCode = auction.propertyType.code,
                categoryLabel = auction.propertyType.blogCategory,
                title = "[GEO 풀분석] ${auction.caseId} $region ${auction.propertyType.displayLabel} 경매 · 배당·사진가이드·3전략",
                bodyText = cloudflareBodyText,
                bodyHtml = cloudflareBodyHtml,
                metaDescription = "${auction.caseId} ${auction.address} 최저가 $minBidStr(${auction.discountRatePct}%), CapRate ${proReport.estimatedCapRatePct}% 무료 정밀 분석",
                tags = commonTags,
                ctaUrl = "${proFunnel.proLandingUrl.trimEnd('/')}/${auction.dedupKey}",
                relAttribute = "internal",
                jsonLdSchema = jsonLd,
                infographicCards = cardsByChannel[ChannelPlatform.CLOUDFLARE].orEmpty(),
                proFunnelBadge = "자체웹 직접 딥링크 PRO 티저 탑재"
            ),
            ChannelPlatform.TISTORY to ChannelVariantContent(
                platform = ChannelPlatform.TISTORY,
                categoryCode = auction.propertyType.code,
                categoryLabel = auction.propertyType.blogCategory,
                title = "[$region ${auction.propertyType.displayLabel} 무료 풀리포트] ${auction.caseId} 배당워터폴·ROE·세무3안",
                bodyText = tistoryBodyText,
                bodyHtml = tistoryBodyHtml,
                metaDescription = "${auction.courtName} ${auction.caseId} 실거래 $realTradeStr 대비 안전마진 $marginVsTradeStr 및 세무 3안 비교",
                tags = commonTags + "수익률분석",
                ctaUrl = "${proFunnel.proLandingUrl.trimEnd('/')}/${auction.dedupKey}?utm_source=tistory",
                relAttribute = "nofollow sponsored",
                jsonLdSchema = "",
                infographicCards = cardsByChannel[ChannelPlatform.TISTORY].orEmpty(),
                proFunnelBadge = "비교표 + nofollow sponsored PRO 퍼널"
            ),
            ChannelPlatform.NAVER to ChannelVariantContent(
                platform = ChannelPlatform.NAVER,
                categoryCode = auction.propertyType.code,
                categoryLabel = auction.propertyType.blogCategory,
                title = naverTitle,
                bodyText = naverBody,
                bodyHtml = "<div class='se-viewer'>${naverBody.replace("\n", "<br/>")}</div>$naverFunnelHtml",
                metaDescription = "${auction.caseId} ${auction.propertyType.blogCategory} 무료 권리분석 · 현장 사진 5컷 가이드",
                tags = commonTags.take(6),
                ctaUrl = if (config.enableExternalLinkNaver) utmBase else "검색어 '${proFunnel.naverSearchKeyword} ${auction.caseId}' + 고정댓글",
                relAttribute = if (config.enableExternalLinkNaver) "bottom-only" else "no-direct-url",
                jsonLdSchema = "",
                infographicCards = cardsByChannel[ChannelPlatform.NAVER].orEmpty(),
                pinnedCommentSnippet = naverPinnedComment,
                proFunnelBadge = "무링크 검색유도 + 6번 배너 + 고정댓글"
            )
        )
    }

    fun buildInitialLogs(): List<PublishLogEntry> = listOf(
        PublishLogEntry(
            logId = "log-001",
            postId = "post-ca-20250118006297-1",
            caseId = "2025타경6297",
            platform = ChannelPlatform.CLOUDFLARE,
            status = PostStatus.PUBLISHED,
            publishedUrl = "https://ddakchal.pages.dev/blog/ca-20250118006297-1",
            errorMessage = null,
            handoffReason = null,
            retryCount = 0,
            publishedAt = "2026-11-22 08:17:23"
        ),
        PublishLogEntry(
            logId = "log-002",
            postId = "post-ca-20250118006297-1",
            caseId = "2025타경6297",
            platform = ChannelPlatform.TISTORY,
            status = PostStatus.PUBLISHED,
            publishedUrl = "https://ddakchal-report.tistory.com/entry/2025타경6297",
            errorMessage = null,
            handoffReason = null,
            retryCount = 0,
            publishedAt = "2026-11-22 09:04:41"
        ),
        PublishLogEntry(
            logId = "log-003",
            postId = "post-ca-20250118006297-1",
            caseId = "2025타경6297",
            platform = ChannelPlatform.NAVER,
            status = PostStatus.PUBLISHED,
            publishedUrl = "https://blog.naver.com/ddakchal_official/223698104211",
            errorMessage = null,
            handoffReason = null,
            retryCount = 0,
            publishedAt = "2026-11-22 10:29:15"
        )
    )
}
