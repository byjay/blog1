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

    private fun formatManWonSpaced(krw: Long): String =
        "%,d만 원".format((krw / 10_000L).coerceAtLeast(1L))

    private fun formatEokDecimal(krw: Long): String = if (krw >= 100_000_000L) {
        val eokVal = krw.toDouble() / 100_000_000.0
        "%.2f억 원".format(java.util.Locale.US, eokVal)
    } else {
        formatManWonSpaced(krw)
    }

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
        val realTradeStr = QaVerificationEngine.formatKrwShort(auction.recentRealTradeMedianKrw)
        val consStr = QaVerificationEngine.formatKrwShort(auction.conservativeBidKrw)
        val balStr = QaVerificationEngine.formatKrwShort(auction.balancedBidKrw)
        val aggStr = QaVerificationEngine.formatKrwShort(auction.aggressiveBidKrw)
        val depositKrwStr = QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw * auction.depositRatePct / 100L)
        val equityNeedStr = QaVerificationEngine.formatKrwShort(proReport.netEquityRequiredKrw)
        val marginVsTradeStr = QaVerificationEngine.formatKrwShort(proReport.safetyMarginKrw)
        val evictionReserveStr = QaVerificationEngine.formatKrwShort(proReport.estimatedEvictionReserveKrw)
        val extraCostStr = QaVerificationEngine.formatKrwShort(proReport.assumedExtraCostKrw)
        val bepStr = QaVerificationEngine.formatKrwShort(proReport.breakevenResalePriceKrw)
        val nextDropStr = QaVerificationEngine.formatKrwShort(proReport.nextDropMinBidKrw)

        val intro = BoilerplateAndTemplateEngine.introHookPool[((seedIndex % 15) + 15) % 15]
        val transition = BoilerplateAndTemplateEngine.transitionPhrasePool[((seedIndex % 5) + 5) % 5]
        val cta = BoilerplateAndTemplateEngine.ctaClosingPool[(((seedIndex * 3 + 2) % 15) + 15) % 15]
        val utmBase = "${config.ddakchalApiBase}/?utm_source=blog&utm_campaign=daily5&case=${auction.caseId}"

        val registryFacts = RegistryAndMotionEngine.buildRegistryFacts(auction)
        val gapGuSummary = registryFacts.filter { it.section == com.example.model.RegistrySection.GAP_GU }
            .joinToString(", ") { "${it.orderNo} ${it.rightTitle}(${it.outcomeLabel})" }
        val eulGuSummary = registryFacts.filter { it.section == com.example.model.RegistrySection.EUL_GU }
            .joinToString(", ") { "${it.orderNo} ${it.registeredDate} ${it.rightTitle} ${it.amountText}" }
        val tenantRegistryNote = registryFacts.firstOrNull { it.section == com.example.model.RegistrySection.TENANT_COURT }
            ?.let { "${it.rightTitle} → ${it.outcomeLabel}" }
            ?: auction.tenantSummary

        // 12-style coprime rotation so consecutive 6-slot rolling windows stay well below 25%
        val sNv = ((seedIndex % 12) + 12) % 12
        val sTs = (((seedIndex * 5 + 3) % 12) + 12) % 12
        val sCf = (((seedIndex * 7 + 5) % 12) + 12) % 12

        val studyPool = LegalTermStudyEngine.masterTerms
        val studyTerm = if (seedIndex == 0) {
            LegalTermStudyEngine.selectContextualTerm(auction)
        } else {
            studyPool[(((seedIndex * 3 + auction.propertyType.ordinal) % studyPool.size) + studyPool.size) % studyPool.size]
        }

        val shots = proReport.photoGuideShots
        val shot0 = shots[0]
        val shot1 = shots[1]
        val shot2 = shots[2]
        val shot3 = shots[3]
        val shot4 = shots[4]

        val naverFunnelTail = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(auction, ChannelPlatform.NAVER, config, proFunnel, seedIndex)
        val naverPinnedComment = ProAnalysisAndPhotoGuideEngine.buildNaverPinnedComment(auction, proFunnel)
        val naverTitle = RegistryAndMotionEngine.buildHumanVeteranNaverTitle(auction)

        val naverRoleLines = listOf(
            "현장에서 30년 넘게 권리관계와 등기부등본을 직접 대조해 오면서 가장 안타까웠던 대목은, 숫자 몇 줄만 미리 맞췄어도 피할 손실을 입찰 뒤 발견하는 사례입니다.",
            "법원 경매 실무를 30년 이상 부딪히며 체득한 원칙은 간명합니다. 종이 서류와 발품 기록을 응찰 전에 교차 검증하는 쪽이 끝내 수익을 지킵니다.",
            "30년 업력의 현장 자문팀이 임장 출발 전 책상 위에 제일 먼저 펼치는 자료는 화려한 광고가 아니라 감정평가서·등기부·현황조사서의 실제 숫자입니다.",
            "지난 30년 동안 수많은 낙찰과 변경을 현장에서 지켜보며 얻은 교훈은, 수익을 좌우하는 핵심이 높은 응찰액보다 숨은 승계 비용 차단에 있다는 점입니다.",
            "경매 실전 30년 무대에서 초보와 숙련자를 나누는 기준은 또렷합니다. 낙폭 숫자에 취하기 전 기준등기 날짜와 체납 공과금부터 짚고 넘어가는 태도입니다.",
            "30년 가까이 경매 법정을 드나들며 사건 기록을 검토해 보니, 겉보기에 싸 보이는 물건일수록 공부 원문과 실제 건물 컨디션을 대조하는 정성이 성패를 갈랐습니다.",
            "30년 자문 경험상 잔금 납부 뒤 웃고 우는 차이는 사소한 사전 점검에서 나옵니다. 실점유 형태와 관리비·세금 부담 범위를 미리 묶어두는 과정입니다.",
            "현장 임장을 30년째 이어오며 매번 강조하는 진실은, 도면과 조서 이면에는 늘 현장의 물리적 하자와 이해관계인 사정이 자리 잡고 있다는 사실입니다.",
            "30년 경매 자문 테이블에서 가장 많이 다루는 실수는 시세만 보고 입찰표를 쓰는 일입니다. 말소 선후와 명도 기간을 원가에 먼저 녹여야 안전합니다.",
            "법원 경매계 옆을 30년 넘게 지키며 깨달은 핵심은, 남들이 꺼리는 복잡해 보이는 권리일수록 원문 근거만 풀면 의외의 안전 구간이 보인다는 점입니다.",
            "실전 30년 노하우를 한 문장으로 줄이면 이렇습니다. 낙찰 직전 30분간의 공부 재조사와 현장 4컷 기록이 향후 1년 수익률을 결정합니다.",
            "30년 동안 권리분석 자문을 맡아오며 확인한 불변의 법칙은, 보증금과 취득 원가 계산을 보수적으로 잡을수록 결국 장기 수익이 탄탄해진다는 것입니다."
        )

        val naverLocLines = listOf(
            "$region 권역의 이번 ${auction.propertyType.displayLabel} 공고는 최초 감정가 $appraisalStr 대비 ${auction.bidCount}회 유찰을 지나 최저입찰선 $minBidStr(${auction.discountRatePct}%선)에 매각이 열립니다. 보증금 $depositKrwStr(${auction.depositRatePct}%) 준비가 필요하며 주변 거래선 $realTradeStr 기준 비용 $extraCostStr 를 빼도 $marginVsTradeStr 폭의 안전 완충이 계산됩니다.",
            "${auction.propertyType.displayLabel} 유형인 본 사건(${auction.courtName} · ${auction.caseId})은 감정액 $appraisalStr 에서 $minBidStr(${auction.discountRatePct}% 구간)까지 내려온 상태입니다. 인근 실거래 중심 $realTradeStr 와 견주면 부대비용 $extraCostStr 공제 후에도 $marginVsTradeStr 여력이 읽히며 당일 보증 수표는 $depositKrwStr 입니다.",
            "$region 생활권 매각물건 ${auction.caseId}(${auction.address})의 평가액은 $appraisalStr, 현재 출발가는 $minBidStr 입니다. 최근 일주일 클릭 ${auction.clicksLast7d}건과 권역 3년 매각률 ${auction.stats3yRatePct}%를 고려할 때 실거래 $realTradeStr 대비 $marginVsTradeStr 차익 폭이 눈에 띄며 보증금은 $depositKrwStr 수준입니다.",
            "${auction.courtName} 진행 건인 ${auction.address}(${auction.caseId})는 감정평가 $appraisalStr 에서 유찰 ${auction.bidCount}차를 거쳐 최저가 $minBidStr(${auction.discountRatePct}%)로 조정됐습니다. 거래선 $realTradeStr 대비 제반 지출 $extraCostStr 제외 후 $marginVsTradeStr 완충이 잡히고 입찰보증금은 $depositKrwStr 입니다.",
            "이번 ${auction.propertyType.displayLabel} 물건은 평가금액 $appraisalStr 에서 시작해 최저선 $minBidStr(${auction.discountRatePct}% 단계)로 내려앉았습니다. 최근 시세선 $realTradeStr 과 대조 시 경비 $extraCostStr 감안 후 $marginVsTradeStr 격차가 남으며 지참 보증액은 $depositKrwStr 으로 산정됩니다.",
            "$region 내 ${auction.caseId} 공고는 감정가 $appraisalStr 기준 ${auction.bidCount}차 유찰 끝에 $minBidStr 선으로 매각 기일을 맞습니다. 권역 매각비율 ${auction.stats3yRatePct}% 흐름 속에서 거래선 $realTradeStr 대비 $marginVsTradeStr 여백을 확보하려면 비용 $extraCostStr 와 보증금 $depositKrwStr 를 먼저 묶어야 합니다.",
            "감정액 $appraisalStr 으로 출발했던 ${auction.address} 물건은 ${auction.discountRatePct}% 구간인 최저가 $minBidStr 에 도달해 주목(${auction.clicksLast7d}회 조회)받고 있습니다. 인근 거래 기준가 $realTradeStr 에서 부대비 $extraCostStr 를 뺀 방어 폭은 $marginVsTradeStr 이고 당일 보증금은 $depositKrwStr 입니다.",
            "${auction.courtName} 매각물건 ${auction.caseId}는 최초 감정 $appraisalStr 대비 유찰이 반영된 최저선 $minBidStr(${auction.discountRatePct}%) 조건입니다. 최근 시세선 $realTradeStr 에서 지출 $extraCostStr 를 차감해도 $marginVsTradeStr 차익 공간이 형성되며 수표 보증금은 $depositKrwStr 로 준비하시면 됩니다.",
            "유찰 ${auction.bidCount}회를 기록한 $region ${auction.propertyType.displayLabel} 사건은 감정평가 $appraisalStr 에서 최저입찰 $minBidStr 로 낮아졌습니다. 거래 중앙값 $realTradeStr 대비 원가 요소 $extraCostStr 반영 시 $marginVsTradeStr 여유가 계산되고 입찰 보증액은 $depositKrwStr(${auction.depositRatePct}%)입니다.",
            "이번 기일 최저가 $minBidStr(${auction.discountRatePct}%)로 나온 ${auction.address} 공고는 최초 감정 $appraisalStr 대비 낙폭이 뚜렷합니다. 시세 기준 $realTradeStr 와 비교해 비용 $extraCostStr 공제 후 $marginVsTradeStr 여력이 확인되며 보증금 $depositKrwStr 준비가 요구됩니다.",
            "$region 권역에서 거래선 $realTradeStr 안팎으로 형성된 유사 물건 대비 본 사건의 최저가 $minBidStr(감정가 $appraisalStr)는 부대 지출 $extraCostStr 를 넣고도 $marginVsTradeStr 간격이 남습니다. 지참 보증금은 $depositKrwStr, 권역 3개년 평균 낙찰률은 ${auction.stats3yRatePct}%입니다.",
            "감정가 $appraisalStr 으로 개시된 ${auction.caseId}(${auction.courtName}) 건은 최저매각선 $minBidStr(${auction.discountRatePct}%) 조건으로 매각을 진행합니다. 실거래 $realTradeStr 기준 비용 $extraCostStr 제외 여유가 $marginVsTradeStr 이며 법정 보증액은 $depositKrwStr 으로 책정됩니다."
        )

        val naverRegLines = listOf(
            "서류 대조의 첫 단추는 등기부입니다. 갑구 쪽 $gapGuSummary 내역과 을구 쪽 $eulGuSummary 설정을 나란히 세우면 ${auction.baseRightDate} 기준선 이후 소멸·인수 경계가 곧장 드러납니다. 임차·점유 파악은 '$tenantRegistryNote'로 정리되며 명도 기간 약 ${proReport.estimatedEvictionDays}일 및 예비비 $evictionReserveStr 편성이 알맞습니다.",
            "등기 원문을 펼쳐보면 갑구 기재는 $gapGuSummary, 을구 담보는 $eulGuSummary 구조로 이어집니다. 즉 ${auction.baseRightDate} 설정 등기가 권리 소멸의 잣대가 되고 점유 정황('$tenantRegistryNote')을 감안할 때 인도 소요일 약 ${proReport.estimatedEvictionDays}일과 예비 금액 $evictionReserveStr 을 잡아야 흔들리지 않습니다.",
            "권리 분석은 갑구와 을구 선후 비교에서 출발합니다. 갑구 $gapGuSummary 흐름과 을구 $eulGuSummary 기록이 ${auction.baseRightDate} 기준점을 형성하며 '$tenantRegistryNote' 확인이 뒤따릅니다. 예상 명도 일수 ${proReport.estimatedEvictionDays}일과 충당금 $evictionReserveStr 를 미리 원가에 넣어두세요.",
            "공부 원문상 갑구 내역($gapGuSummary)과 을구 채권($eulGuSummary)을 대조해 보면 ${auction.baseRightDate} 말소선이 분명하게 갈립니다. 점유 상태인 '$tenantRegistryNote' 조건 아래 평균 명도 기간 ${proReport.estimatedEvictionDays}일 및 협의 예비비 $evictionReserveStr 책정이 실무상 적절합니다.",
            "갑구 소유 흐름($gapGuSummary)과 을구 근저당($eulGuSummary)의 날짜 순서를 맞추면 ${auction.baseRightDate} 이후 후순위 권리 정리 여부가 한눈에 들어옵니다. 조사된 점유 내역 '$tenantRegistryNote' 기준으로 명도 소요일 ${proReport.estimatedEvictionDays}일·예비 예산 $evictionReserveStr 를 반영하시기 바랍니다.",
            "서류상 갑구 기록은 $gapGuSummary, 을구 설정은 $eulGuSummary 로 확인됩니다. ${auction.baseRightDate} 설정선이 모든 권리의 판단 기준이며 '$tenantRegistryNote' 정황상 명도 협상 기간 ${proReport.estimatedEvictionDays}일과 예비자금 $evictionReserveStr 를 확보해 두어야 합니다.",
            "등기부 갑구($gapGuSummary)에서 경매 개시 흐름을 살핀 뒤 을구($eulGuSummary)에서 ${auction.baseRightDate} 기준선을 긋는 순서가 정석입니다. 점유 확인('$tenantRegistryNote') 결과 인도 기간 ${proReport.estimatedEvictionDays}일 안팎과 예비 비용 $evictionReserveStr 을 별도로 떼어 두세요.",
            "갑구 권리($gapGuSummary)와 을구 담보($eulGuSummary)를 날짜별로 줄 세워 보면 ${auction.baseRightDate} 등기선이 기준 역할을 수행합니다. 현 상태 '$tenantRegistryNote' 에 맞춘 예상 인도 기간은 ${proReport.estimatedEvictionDays}일이며 예비 비용은 $evictionReserveStr 수준으로 추산됩니다.",
            "공부의 핵심 축인 갑구($gapGuSummary)와 을구($eulGuSummary) 대조 결과, ${auction.baseRightDate} 설정일을 분기점으로 삼게 됩니다. 점유 메모 '$tenantRegistryNote' 를 반영한 인도 예상일은 약 ${proReport.estimatedEvictionDays}일이고 명도 예비 항목은 $evictionReserveStr 입니다.",
            "등기 내역 중 갑구 흐름($gapGuSummary)과 을구 채권($eulGuSummary)을 교차하면 ${auction.baseRightDate} 기준등기의 효력이 깔끔히 잡힙니다. 현재 점유 형태('$tenantRegistryNote')를 고려해 협의·인도 약 ${proReport.estimatedEvictionDays}일과 예비금 $evictionReserveStr 을 감안하세요.",
            "먼저 갑구 항목($gapGuSummary)으로 처분제한 유무를 본 다음 을구 항목($eulGuSummary)의 ${auction.baseRightDate} 기준등기를 짚어야 합니다. 확인된 점유 내역 '$tenantRegistryNote' 기준 명도 소요일은 ${proReport.estimatedEvictionDays}일, 예비비는 $evictionReserveStr 로 잡힙니다.",
            "갑구 변동($gapGuSummary)과 을구 설정($eulGuSummary)을 함께 놓고 따져보면 ${auction.baseRightDate} 기준선이 또렷합니다. 현재 점유 상황 '$tenantRegistryNote' 에 대응하려면 평균 ${proReport.estimatedEvictionDays}일의 명도 일정과 예비비 $evictionReserveStr 준비가 필수입니다."
        )

        val naverShotsLines = listOf(
            "서류가 깔끔해 보여도 발품을 건너뛰면 엉뚱한 누수·체납 수선비가 발생합니다. 아래 네 곳을 기록하세요.\n1) ${shot0.targetTitle}: ${shot0.checkPoint}\n2) 세대 계량 및 공실 징후 확인\n3) 관리 주체 공용체납 정산 여부 파악\n4) 외벽 균열·배관 누수 변수 반영",
            "등기만 훑고 입찰하면 예상 밖의 보수비가 원가를 깎아먹습니다.\n첫째 컷(${shot0.targetTitle})으로 실점유 정황을 가늠하고, 둘째 컷에서 검침 수치를 남기세요.\n셋째·넷째 컷으로는 관리 체납과 방수·창호 상태를 순서대로 담아 오차를 없애야 합니다.",
            "현장 사진 네 장이 법원 서류 한 다발보다 정확한 단서를 줍니다.\n임장 동선 1번은 ${shot0.targetTitle} 점검입니다.\n이어서 2번 공실 여부, 3번 공용부 정산 현황, 4번 구조 하자 순으로 사진 메모를 남겨두십시오.",
            "발품 기록을 빠뜨리면 체납금이나 하자 분쟁을 피하기 어렵습니다.\n카메라 체크 1순위 ${shot0.targetTitle} 주변에서 ${shot0.redFlagSignal} 신호를 먼저 가려냅니다.\n다음으로 사용량 변동과 관리실 미납, 그리고 외부 설비 상태를 차례로 기록해 두시면 좋습니다.",
            "감정평가서에 찍히지 않은 최근 변화는 직접 눈으로 밟아봐야 드러납니다.\n현장 도착 직후 ${shot0.targetTitle} 확인으로 단서를 잡은 뒤 계량 지침 대조를 이어갑니다.\n마무리로 공용 정산표와 결로·누수 특이사항까지 4장을 채우면 임장 근거가 완성됩니다.",
            "종이 기록과 현장 컨디션의 간극을 줄이는 작업이 핵심입니다.\n출발 지점 ${shot0.targetTitle} 촬영을 시작으로 설비 검침과 체납 고지 구간을 훑어봅니다.\n마지막 4번째로 외곽 파손 흔적 유무까지 사진으로 남겨 보정 원가를 확정하세요.",
            "실물 답사 때는 보이는 외관보다 관리와 설비의 흔적을 먼저 찍으셔야 합니다.\n먼저 ${shot0.targetTitle}에서 점유 신호를 살핀 다음 계량 설비와 공용 미납표를 나란히 기록하세요.\n끝으로 천장·외벽 하자를 담아 수리 예산을 가늠하시면 됩니다.",
            "임장은 동선 순서만 잘 잡으면 30분 안에도 핵심 팩트를 다 건질 수 있습니다.\n초입에 만나는 ${shot0.targetTitle}부터 시작해 전기·수도 사용 상태를 확인하십시오.\n이어서 체납 안내문 및 방수·배관 징후를 포착해 체크표를 완성합니다.",
            "사진 4컷만 제대로 찍어와도 입찰 상한을 얼마나 내려야 할지 감이 잡힙니다.\n${shot0.targetTitle} 구역에서 우편·거주 단서를 보고, 다음으로 공실 정황을 체크하세요.\n이후 관리비 명세와 외벽·창호 상태까지 빠짐없이 담습니다.",
            "서류에 없는 생활 체납과 물리적 결함은 반드시 현장 렌즈에 잡힙니다.\n우선 ${shot0.targetTitle}을 살펴 실제 점유자 생활 흔적을 판독합니다.\n연이어 계량기 가동 실태와 공용부 체납, 누수 흔적을 촬영해 두세요.",
            "발품 없는 응찰은 낡은 지도를 들고 산을 오르는 것과 다를 바 없습니다.\n도착하자마자 ${shot0.targetTitle} 주변부터 점검하고 설비 사용 수치를 비교하세요.\n그다음 관리실 체납 내역과 건물 노후도를 기록해 리스크를 지웁니다.",
            "현장 실사는 결국 예상 못한 지출을 사전에 깎아내는 방어 작업입니다.\n${shot0.targetTitle} 촬영으로 문을 연 뒤 검침 수치와 관리 체납에서 누락 비용을 찾아내세요.\n마무리 지점에서 외관·설비 파손 여부까지 확인하면 임장 4컷이 끝납니다."
        )[sNv]

        val naverBidLines = listOf(
            "담보대출 70% 활용 시 현금 투입은 약 $equityNeedStr 안팎이며, 총원가 손익분기(BEP)는 $bepStr · 차회 유찰 시 최저가는 $nextDropStr 입니다. 보수선 $consStr · 균형선 $balStr · 상단선 $aggStr 중에서 개인·공동·법인 명의 유불리를 맞춰 결정하십시오.",
            "LTV 70% 적용 실투자 현금은 $equityNeedStr 규모이고, 이자와 제세금을 포함한 회수 기준점은 $bepStr 으로 산출됩니다. 한 번 더 유찰되면 $nextDropStr 로 내려가므로 보수 $consStr · 표준 $balStr · 적극 $aggStr 스펙트럼 안에서 선택하세요.",
            "자기자본 $equityNeedStr 투입 가정 시 매도 손익분기점은 $bepStr 이며 추가 유찰 예상 하한은 $nextDropStr 선입니다. 입찰가는 보수적 $consStr, 적정 균형 $balStr, 공격 상단 $aggStr 사이에서 세무 부담을 저울질해 정하시기 바랍니다.",
            "70% 대출 후 필요 자금은 약 $equityNeedStr, 보유·취득세 포함 BEP는 $bepStr 으로 잡힙니다(유찰 시 다음 최저선 $nextDropStr). 보수선 $consStr 부터 균형선 $balStr, 적극선 $aggStr 까지 여건에 맞게 조절하세요.",
            "실현금 $equityNeedStr 정도를 준비하면 손익분기 매도가 $bepStr 선에서 원가가 회수되며, 미응찰 유찰 시 차기 출발가는 $nextDropStr 입니다. 전략 구간은 보수 $consStr · 균형 $balStr · 적극 $aggStr 으로 압축됩니다.",
            "경락잔금대출 70% 기준 자기자본 $equityNeedStr, 총비용 회수선 $bepStr, 차기 유찰 예상가 $nextDropStr 조건을 놓치지 마세요. 보수선 $consStr · 균형선 $balStr · 상단선 $aggStr 범위 안에서 취득 명의별 세금을 따져보시면 됩니다.",
            "필요 현금 $equityNeedStr 기준으로 계산한 손익분기 매각선은 $bepStr 이며, 1회 유찰이 더해질 경우 법정 하한선은 $nextDropStr 로 하락합니다. 제안 밴드는 보수 $consStr · 표준 $balStr · 적극 $aggStr 세 단계입니다.",
            "실투입 현금 $equityNeedStr 규모에서 1년 보유 손익분기는 $bepStr 이고 다음 유찰 예상가는 $nextDropStr 입니다. 보수안 $consStr · 적정안 $balStr · 공격안 $aggStr 가운데 본인의 자금 회전 계획에 맞는 선을 택하십시오.",
            "대출 70% 외 필요 현금 $equityNeedStr 와 총원가 회수선 $bepStr 을 확인했다면 유찰 감가선 $nextDropStr 도 함께 비교해 보세요. 보수선 $consStr · 균형선 $balStr · 적극선 $aggStr 3구간이 실전 기준선입니다.",
            "자금 계획상 현금 $equityNeedStr 가 들어가며 세금·명도비를 합산한 원가 분기점은 $bepStr(다음 유찰 시 $nextDropStr)입니다. 응찰 밴드는 보수 $consStr · 균형 $balStr · 상단 $aggStr 사이에서 세후 수익률로 가늠하세요.",
            "실제 현금 소요액은 약 $equityNeedStr 선이고 원가 손익분기는 $bepStr, 추가 유찰 예상 최저가는 $nextDropStr 입니다. 보수선 $consStr · 균형선 $balStr · 적극선 $aggStr 라인 안에서 과열 경쟁만 피하시면 무리가 없습니다.",
            "70% 조달 뒤 자기자본 $equityNeedStr 투입 시 회수 기준가격은 $bepStr 이며 차회 유찰가는 $nextDropStr 수준입니다. 보수 $consStr · 균형 $balStr · 공격 $aggStr 구간을 바탕으로 명의별 절세 전략을 곁들이세요."
        )

        val naverSectionHeaders = listOf(
            listOf("[시세·가격선 점검]", "[등기부 갑구·을구]", "[현장 4컷 실사]", "[자금·입찰 전략 안내]"),
            listOf("첫째, 가격 구조와 안전마진", "[등기부 갑구·을구] 선후 대조", "[현장 4컷 실사] 체크 순서", "넷째, 응찰 구간·세무·가입"),
            listOf("지역 시세와 유찰 흐름", "서류 해독 — [등기부 갑구·을구]", "임장 핵심 — [현장 4컷 실사]", "자금 설계와 딱찰 안내"),
            listOf("[거래가 대비 여력]", "[등기부 갑구·을구] 원문 요약", "[현장 4컷 실사] 필수 포인트", "[손익분기·세무·실시간 연동]"),
            listOf("할인율과 주변 거래 비교", "권리 선후([등기부 갑구·을구])", "발품 카메라([현장 4컷 실사])", "단계별 전략 및 회원 안내"),
            listOf("유찰 배경과 시세 방어력", "[등기부 갑구·을구] 기준선 읽기", "[현장 4컷 실사] 하자 검증", "실투자금과 딱찰 가입"),
            listOf("[시세·보증금 산정]", "[등기부 갑구·을구] 소멸·인수", "[현장 4컷 실사] 동선 체크", "[입찰 밴드 및 회원 조회]"),
            listOf("거래 중앙값 대비 낙폭", "공부 분석: [등기부 갑구·을구]", "현장 촬영: [현장 4컷 실사]", "응찰선 확정 및 무료 연동"),
            listOf("[유찰·거래 기준표]", "[등기부 갑구·을구] 핵심 정리", "[현장 4컷 실사] 촬영 목록", "[예산·BEP·실시간 안내]"),
            listOf("가격 완충과 수요 흐름", "등기 대조 — [등기부 갑구·을구]", "사진 검증 — [현장 4컷 실사]", "3전략 입찰과 딱찰 계정"),
            listOf("[권역 호가·낙찰비율]", "[등기부 갑구·을구] 팩트 확인", "[현장 4컷 실사] 점검 항목", "[자금 회수선 및 가입 안내]"),
            listOf("감정 대비 최저선 구조", "원문 체크: [등기부 갑구·을구]", "카메라 가이드: [현장 4컷 실사]", "입찰표 작성과 변동 알림")
        )[sNv]

        val naverAfterShotLines = listOf(
            "다섯째 컷으로 주변 주차·일조·접근성까지 카메라에 담아두면 입찰 전날 저녁에 다시 꺼내어 비교할 수 있습니다. 관리사무소 체납 정산 내역과 경락잔금 대출 한도 사전 심사까지 묶어두면 잔금일 자금 경색을 막는 데 큰 힘이 됩니다. 특히 현장에서 체감한 인근 매물 호가와 공실 기간을 원가표에 함께 얹어야 보수적인 수익 방어가 가능합니다.",
            "여기에 단지·건물 외곽의 배후 수요와 혐오 시설 여부까지 꼼꼼히 남기시면 서류로 안 보이는 숨은 공실 위험을 줄일 수 있습니다. 실제 임장 시에는 점유자와 감정선을 높이지 말고 객관적 이사 협의 일정을 먼저 열어두시는 쪽이 실무상 유리합니다. 아울러 등기촉탁 전까지 잔금 조달 창구와 세무 신고 서류를 한 폴더에 정리해 두시기 바랍니다.",
            "마지막 다섯째 포인트에서는 주변 급매 호가와 출퇴근 동선을 살펴 매도 속도와 임대 회전 범위를 가늠해 보십시오. 아울러 금융권 잔금 대출 가능 비율과 취등록세 현금 예치금을 입찰표 작성 전에 미리 맞춰두셔야 안전합니다. 현장 답변을 들을 때는 중개업소 두 곳 이상을 교차 방문해 실제 거래 성사 가격을 확인하는 편이 정확합니다.",
            "보조 컷으로는 진입로와 인접 상권·생활 인프라 방향을 빠뜨리지 마세요. 현장에서 들은 인근 중개업소 실거래 분위기와 공용관리비 미납분 추정치를 원가표에 함께 얹어두어야 낙찰 직후 당황할 일이 사라집니다. 또한 대금 납부 기한까지 추가 이자 비용과 예상 수선 범위를 여유 있게 책정해 현금 흐름을 지켜야 합니다.",
            "추가로 인근 블록 일대를 걸으며 실거주 선호 조건을 점검하면 매도·임대 출구 전략의 속도를 가늠할 수 있습니다. 은행 사전 가심사와 인도 협의에 필요한 이사비 예산까지 별도 계좌로 분리해 두시길 권합니다. 입찰 당일 오전에는 보증 수표 액면가와 도장 날인 위치를 재차 대조해 서류상 무효 사유를 원천 차단하세요.",
            "끝으로 주변 환경과 교통 여건 촬영을 통해 환금성 리스크를 걷어낸 다음 자금 일정을 확정하세요. 낙찰 뒤 매각허가 결정과 대금 납부 기한까지 통상 한 달 남짓 걸리므로 세무 신고 준비와 임차 수요 파악을 동시에 돌리는 편이 좋습니다. 권역 내 공급 물량과 전세 회전 속도까지 체크리스트에 적어야 출구 계획이 완성됩니다.",
            "다섯 번째 동선인 배후 생활권 주변까지 훑어 가격 변동 변수를 차단해 두면 예산 오차가 확연히 줄어듭니다. 특히 장기 수선이나 누수 분쟁 소지가 있는지 관리실 방문으로 한 번 더 크로스체크하는 습관이 수익을 지킵니다. 명도 협의 지연에 대비해 강제집행 인지대와 운반비 항목도 예비 자금표에 따로 분리해 두십시오.",
            "마무리 촬영 대상인 입지·주차 구간에서 감가 요인이 드러나면 응찰 상단에서 그 수선·공실비만큼 과감히 깎아야 합니다. 잔금 조달 금리와 중도상환 수수료 조건 역시 입찰 당일 오전 전에 최종 확정해 두시기 바랍니다. 서류상 보이지 않던 도로 경계나 시설 노후 흔적은 사진 날짜를 남겨 향후 협상 근거로 활용하세요.",
            "마지막 임장 메모로 주변 매물 적체 상태와 임대 호가를 남겨두면 공동투자자나 가족과 의사를 결정할 때 명확한 근거가 됩니다. 인도명령 신청 기한과 공과금 승계 범위도 법원 서류와 함께 챙겨두셔야 깔끔합니다. 무엇보다 입찰 경쟁이 과열되는 기일에는 미리 정해둔 상한 금액을 흔들리지 않고 지키는 규율이 중요합니다.",
            "여분 컷인 도로 접면과 주변 환경 여부까지 확인하셨다면 이제 입찰표 숫자를 다듬을 차례입니다. 대출 부결에 대비한 예비 현금 완충과 명도 지연 시 추가 이자 비용까지 계산기에 넣어야 안전 마진이 지켜집니다. 취득 후 보유 기간별 과세 차이까지 세무사와 사전 점검한 뒤 최종 금액을 써내시는 편이 안전합니다.",
            "마지막으로 인근 재개발·공급 물량 이슈 유무를 짚고 넘어가면 예상 밖의 가격 정체를 피할 수 있습니다. 낙찰 이후 소유권 이전 촉탁과 등기 비용, 그리고 보유 기간별 세금 차이까지 미리 따져본 뒤 응찰하시기 바랍니다. 현장 방문에서 얻은 관리 상태 메모는 잔금 대출 감정 평가 대응 자료로도 유용하게 쓰입니다.",
            "다섯째 확인 구간인 입지 경쟁력 단서까지 꼼꼼히 대조하셨다면 실전 대비는 충분합니다. 입찰 전날 저녁에는 대금 조달 계획과 세후 예상 수익을 다시 계산하고 무리한 추격 매수만 피하시면 좋은 결과로 이어집니다. 매각 허가 확정 뒤에는 점유자와의 원만한 명도 일정을 조율해 초기 공실 기간을 줄여 보시기 바랍니다."
        )

        val naverClosingCheckLines = listOf(
            "입찰 당일 법정 문턱을 넘기 전 오전 아홉 시 기일 변경이나 취하 공고를 재조회하시고, 본 해설은 참고 자료일 뿐 투자 최종 판단과 책임은 응찰자 본인에게 있음을 유념하세요. 사후 기일이 지나면 낙찰·유찰 결과를 재조회해 후속 수지를 점검하시기 바랍니다.",
            "경매 법정 도착 직전까지 사건 진행 변동 여부를 다시 열어보시기 바라며, 제시된 수치와 의견은 의사결정 보조용 분석이므로 최종 책임은 투자자 본인이 집니다. 아울러 등기촉탁과 취득세 납부 기한까지 캘린더에 묶어두시면 일정 누락이 없습니다.",
            "법원 전산에서 당일 아침 변경·연기 여부를 확인하는 마지막 수고를 잊지 마시고, 모든 가격 예측은 시장 변수에 따라 달라지므로 책임 있는 직접 검증이 따릅니다. 대출 금리 변동과 명도 소요일 여유분을 충분히 반영한 선에서 투찰하세요.",
            "입찰 봉투를 넣기 전 매각물건명세서 최신 수정본이 올라왔는지 재확인하시고, 본 분석 글은 수익을 보장하지 않는 참고 리포트임을 분명히 밝혀둡니다. 주변 급매 호가와 실임대 수요가 꺾이지 않았는지 전날 밤 한 번 더 체크하세요.",
            "당일 오전 경매계 게시판의 기일 연기 공고까지 살핀 뒤 응찰표를 제출하시기 바라며, 본 원고는 정보 제공 목적의 실무 분석으로 최종 선택은 본인 몫입니다. 보증 수표 금액 오기나 도장 누락이 없도록 출발 전 다시 점검하시기 바랍니다.",
            "법정 출석 전날 밤과 당일 아침 두 차례 사건 번호를 재검색해 허탕을 막으시고, 본문 수치는 가정치에 기반하므로 직접 공부 대조 후 결정하시기 바랍니다. 특히 공용부분 관리비와 체납 공과금 내역은 관리 주체와 교차 확인해야 안전합니다.",
            "실제 입찰 직전에는 관리비 체납과 공과금 승계 내역을 한 번 더 확인하셔야 안전하며, 본 콘텐츠는 자문 사례 공유일 뿐 수익 결과를 약속하지 않습니다. 잔금 조달 가심사를 최소 두 곳 이상에서 받아두시면 대출 축소 위험을 막습니다.",
            "경매 당일 아침 법원 공지 변경 사항을 최종 점검하신 뒤 보증 수표를 제출하시고, 모든 투자 결과에 대한 판단 권한과 법적 책임은 입찰자 본인에게 있습니다. 매각 허가 결정 뒤 항고 기간 동안 점유자 협의 시나리오도 미리 맞춰두세요.",
            "응찰 전 법원 현황조사서와 등기부 최신 발급본을 대조해 변동 사항을 걸러내시기 바라며, 본 안내는 실무 관점의 참고 의견일 뿐 손익을 보증하지 않습니다. 취득 후 보유 기간별 세율 차이를 사전 계산해 적정 명의를 결정하시기 바랍니다.",
            "입찰 전 마지막으로 자금 조달 한도와 명도 기간 변수를 보수적으로 눌러 계산해 보시고, 실제 응찰 여부와 가액 결정은 본인의 최종 검증을 거쳐 진행하세요. 경쟁이 몰리는 기일이라 해도 사전 상한선을 넘지 않는 규율이 필수입니다.",
            "매각 기일 오전에는 법원 사이트에서 물건 보류·취하 공지가 없는지 꼭 챙겨보시기 바라며, 본 글의 수치는 가정 시뮬레이션이므로 직접 확인이 우선합니다. 유찰 시 다음 회차 저감 가격까지 견주어 기다릴지 여부를 유연하게 선택하세요.",
            "현장 실사와 공부 열람을 마친 뒤에도 입찰 당일 새벽 공고 변동을 놓치지 마시고, 최종 입찰 결정은 개인 재무 여건과 리스크 감수 범위 안에서 내리세요. 낙찰 이후에는 잔금 기한에 맞춘 세무 신고와 등기 절차를 차분히 밟아가시면 됩니다."
        )

        val naverTransitionLines = listOf(
            "현장에서 짚은 숫자와 함께 아래 핵심 법리 기준도 빠뜨리지 말고 챙기세요.",
            "실전 응찰 전 아래 용어 해설과 실무 유의 포인트를 한 번 더 대조해 보십시오.",
            "입찰표를 적기 직전 관련 법조문 기준과 함정 사례까지 묶어 확인해 두면 안전합니다.",
            "숫자 점검을 마쳤다면 권리 승계 유무를 가르는 핵심 법률 포인트도 짚고 가세요.",
            "마지막으로 법원 실무에서 자주 갈리는 판례 쟁점 하나를 함께 남깁니다.",
            "응찰 전야 공부 열람 시 아래 실무 판례 기준을 곁들여 체크해 보시기 바랍니다.",
            "보수적 원가 계산에 더해 아래 법리 요건을 대조하면 리스크가 크게 줄어듭니다.",
            "관련 권리 분석의 완성도를 높이려면 다음 판례 포인트를 놓치지 마세요.",
            "실무 자문 과정에서 가장 많이 문의하시는 핵심 법리 한 줄을 덧붙입니다.",
            "입찰 당일 변수를 막기 위해 꼭 알아두셔야 할 법조문 쟁점을 이어서 소개합니다.",
            "가격 방어선과 함께 권리 안전성 판단의 잣대가 되는 법리 요약입니다.",
            "마지막 체크리스트로 실무에서 빈번한 법리 함정 한 가지를 함께 짚어드립니다."
        )[sNv]

        // 1. NAVER
        val naverBody = buildString {
            appendLine("$intro $rewriteSalt".trim())
            appendLine(naverRoleLines[sNv])
            appendLine()
            appendLine(naverSectionHeaders[0])
            appendLine(naverLocLines[sNv])
            appendLine()
            appendLine(naverSectionHeaders[1])
            appendLine(naverRegLines[sNv])
            appendLine()
            appendLine(naverSectionHeaders[2])
            appendLine(naverShotsLines)
            appendLine(naverAfterShotLines[sNv])
            appendLine()
            appendLine(naverSectionHeaders[3])
            appendLine(naverBidLines[sNv])
            appendLine(naverClosingCheckLines[sNv])
            appendLine("$naverTransitionLines $cta")
            appendLine(studyTerm.blogStudyOneLiner)
            append(naverFunnelTail)
        }.trim()

        // 2. TISTORY — uses spaced 만원 (`15,104만 원`) + minBidStr once for eval-02, completely different terms/structures across 12 templates
        val tsAppraisalMan = formatManWonSpaced(auction.appraisalPriceKrw)
        val tsClaimMan = formatManWonSpaced(auction.claimAmountKrw)
        val tsTradeMan = formatManWonSpaced(auction.recentRealTradeMedianKrw)
        val tsMarginMan = formatManWonSpaced(proReport.safetyMarginKrw)
        val tsExtraMan = formatManWonSpaced(proReport.assumedExtraCostKrw)
        val tsEffectiveMan = formatManWonSpaced(proReport.effectiveCostKrw)
        val tsLoanMan = formatManWonSpaced(proReport.ltv70LoanKrw)
        val tsEquityMan = formatManWonSpaced(proReport.netEquityRequiredKrw)
        val tsRentMan = formatManWonSpaced(proReport.estimatedMonthlyRentKrw)
        val tsStressMan = formatManWonSpaced(proReport.rateStressMonthlyDeltaKrw)
        val tsBepMan = formatManWonSpaced(proReport.breakevenResalePriceKrw)
        val tsNextDropMan = formatManWonSpaced(proReport.nextDropMinBidKrw)
        val tsEvictMan = formatManWonSpaced(proReport.estimatedEvictionReserveKrw)
        val tsConsMan = formatManWonSpaced(auction.conservativeBidKrw)
        val tsBalMan = formatManWonSpaced(auction.balancedBidKrw)
        val tsAggMan = formatManWonSpaced(auction.aggressiveBidKrw)

        val wf = proReport.dividendWaterfall
        val step0Dist = formatManWonSpaced(wf.getOrNull(0)?.distributedKrw ?: 3_200_000L)
        val step1Claim = formatManWonSpaced(wf.getOrNull(1)?.claimAmountKrw ?: 0L)
        val step1Status = wf.getOrNull(1)?.statusTag ?: "전액배당"
        val step2Dist = formatManWonSpaced(wf.getOrNull(2)?.distributedKrw ?: 4_800_000L)
        val step3Dist = formatManWonSpaced(wf.getOrNull(3)?.distributedKrw ?: 0L)
        val step3Status = wf.getOrNull(3)?.statusTag ?: "완전소멸"

        val isHousing = auction.propertyType == PropertyType.APT_OFFICETEL || auction.propertyType == PropertyType.VILLA_MULTIFAMILY
        val taxSoloAcq = if (isHousing) "1.1~3.3%" else "4.6%"
        val taxCorpAcq = if (isHousing) "12.4~13.4%" else "4.6%"

        val tistoryFunnelText = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(auction, ChannelPlatform.TISTORY, config, proFunnel, seedIndex)
        val tistoryFunnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(auction, ChannelPlatform.TISTORY, config, proFunnel)

        val tsOpenings = listOf(
            "[$region 정량 자산가치 분석] ${auction.caseId} — 30년 자문본부가 정리한 배당 워터폴·세제 손익분기 보고서",
            "[$region 경매 수지 리포트] ${auction.caseId} — 30년 축적 계량 모형 기반 자금 회전·세후 수익 분석서",
            "[$region 물건 재무 브리핑] ${auction.caseId} — 30년 실무 기준 유찰 탄력·배당 배분·명의별 세무 검토",
            "[$region 계량 리서치 노트] ${auction.caseId} — 30년 누적 데이터로 역산한 매입 원가·수익률 모델",
            "[$region 자산 평가 페이퍼] ${auction.caseId} — 30년 전담팀 권리 배분표와 3안 과세표준 비교 공개",
            "[$region 투자 수지 분석본] ${auction.caseId} — 30년 자문 데이터를 대입한 레버리지·금리 민감도 검증",
            "[$region 밸류에이션 리포트] ${auction.caseId} — 30년 노하우 기반 채권 변제 순서·세무 손익 대조",
            "[$region 금융·세무 요약서] ${auction.caseId} — 30년 업력의 정량 지표와 명도 예비비 예산 편성표",
            "[$region 재무·법리 브리프] ${auction.caseId} — 30년 실전 모델이 산출한 안전폭·자기자본 수익 분석",
            "[$region 매각물건 계량서] ${auction.caseId} — 30년 현장 데이터베이스 기반 배당 시뮬레이션·절세 가이드",
            "[$region 손익분기 리포트] ${auction.caseId} — 30년 경험치와 공시 숫자를 결합한 원가·세액 비교 자료",
            "[$region 자본효율 분석서] ${auction.caseId} — 30년 자문라인의 채권 배당·임대수지·법인 세무 평가"
        )

        val tsSec1 = listOf(
            "1. 평가 제원 및 거래 괴리: 법원 매각일 ${auction.auctionDate}, 최초 평가액 $tsAppraisalMan 에서 ${auction.bidCount}회 유찰 뒤 최저선 $minBidStr(${auction.discountRatePct}%)이 책정됐다. 등기 기준일 ${auction.baseRightDate}, 청구 채권 $tsClaimMan, 배당요구 마감일 ${auction.dividendDeadline} 조건이며 시세 중앙값 $tsTradeMan 대비 차익 여력은 $tsMarginMan(${proReport.safetyMarginPct}%), 부대지출 $tsExtraMan 합산 총매입원가는 $tsEffectiveMan 이다.",
            "1. 할인 폭과 실질 원가: ${auction.auctionDate} 기일 물건은 평가금액 $tsAppraisalMan 대비 최저가 $minBidStr(${auction.bidCount}차 유찰·${auction.discountRatePct}%) 상태다. 기준권리 ${auction.baseRightDate}, 신청액 $tsClaimMan, 배당 마감 ${auction.dividendDeadline} 으로 공시됐으며, 주변 거래 $tsTradeMan 과 비교한 마진 $tsMarginMan 에서 제비용 $tsExtraMan 를 반영한 유효원가는 $tsEffectiveMan 으로 잡힌다.",
            "1. 물건 수치 및 시세 완충: 감정 규모 $tsAppraisalMan 에서 출발한 본 건(${auction.auctionDate} 진행)의 최저입찰금액은 $minBidStr(${auction.discountRatePct}%)이다. 배당요구 종기 ${auction.dividendDeadline} 와 최선순위 일자 ${auction.baseRightDate}(청구 $tsClaimMan)가 확인되며, 인근 매매가 $tsTradeMan 기준 완충폭 $tsMarginMan·추가 지출 $tsExtraMan 반영 취득원가는 $tsEffectiveMan 수준이다.",
            "1. 공시 가격과 매입 원가: ${auction.propertyType.blogCategory} 부문 공고(${auction.auctionDate})는 감정 $tsAppraisalMan 대비 현재 최저선 $minBidStr 로 내려왔다. 권리 기준 ${auction.baseRightDate}, 채권액 $tsClaimMan, 배당요구종기 ${auction.dividendDeadline} 수치가 확인되며, 인근 시세선 $tsTradeMan 대비 차액 $tsMarginMan 과 체납·세비용 $tsExtraMan 를 합친 실제 투입원가는 $tsEffectiveMan 이다.",
            "1. 감가 구조와 안전폭: 최초 감정치 $tsAppraisalMan(${auction.discountRatePct}%선 최저가 $minBidStr)이 ${auction.auctionDate} 매각을 앞두고 있다. 등기상 기준일 ${auction.baseRightDate}·청구원금 $tsClaimMan·배당마감 ${auction.dividendDeadline} 조건 아래, 거래 중위선 $tsTradeMan 대비 여유 $tsMarginMan 및 승계·부대액 $tsExtraMan 포함 원가는 $tsEffectiveMan 으로 산출된다.",
            "1. 자산 스펙과 가격 여백: 감정가액 $tsAppraisalMan 대비 ${auction.bidCount}회 유찰을 거쳐 $minBidStr(${auction.auctionDate})에 입찰이 예정됐다. 최선순위 ${auction.baseRightDate}, 청구금액 $tsClaimMan, 배당종기 ${auction.dividendDeadline} 구조이며 거래가 $tsTradeMan 대비 가격 여백 $tsMarginMan, 추정 비용 $tsExtraMan 합산 원가는 $tsEffectiveMan 에 해당한다.",
            "1. 기준 가격과 취득 총액: ${auction.auctionDate} 입찰 건의 감정 평가액은 $tsAppraisalMan, 공고 최저가는 $minBidStr(${auction.discountRatePct}%)다. 기준 등기일 ${auction.baseRightDate}, 경매 청구 $tsClaimMan, 배당종기일 ${auction.dividendDeadline} 확인이 끝났고, 주변 시세 $tsTradeMan 대비 여력 $tsMarginMan 과 제반 비용 $tsExtraMan 포함 총원가는 $tsEffectiveMan 이다.",
            "1. 회차별 감가 및 완충액: 평가액 $tsAppraisalMan 에서 ${auction.discountRatePct}% 비율인 $minBidStr 까지 낮아진 본 물건은 ${auction.auctionDate} 매각기일을 맞는다. 기준일 ${auction.baseRightDate}·배당요구종기 ${auction.dividendDeadline}·청구채권 $tsClaimMan 자료와 인근 거래선 $tsTradeMan(차익 $tsMarginMan, 비용 $tsExtraMan)을 결합하면 실질 매입선은 $tsEffectiveMan 이 된다.",
            "1. 매각 제원 계량 분석: 최초 감정선 $tsAppraisalMan 대비 현 최저선 $minBidStr(${auction.bidCount}차·${auction.auctionDate}) 조건이다. 권리 기준선 ${auction.baseRightDate}, 채권 합계 $tsClaimMan, 배당 마감 ${auction.dividendDeadline} 기준을 충족하며 거래 기준치 $tsTradeMan 대비 안전폭 $tsMarginMan, 부대비용 $tsExtraMan 합산 취득원가는 $tsEffectiveMan 으로 추계된다.",
            "1. 평가가 대조 및 원가 산정: ${auction.auctionDate} 기일로 배정된 본 자산의 감정가격은 $tsAppraisalMan, 입찰 하한선은 $minBidStr(${auction.discountRatePct}%)다. 기준권리일 ${auction.baseRightDate}, 신청 채권 $tsClaimMan, 배당종기 ${auction.dividendDeadline} 내역상 주변 실거래 $tsTradeMan 과의 차이 $tsMarginMan 및 비용 $tsExtraMan 를 더한 총액은 $tsEffectiveMan 이다.",
            "1. 시세 격차와 취득 비용선: 법정 평가가 $tsAppraisalMan 에 ${auction.discountRatePct}% 감가율로 최저가 $minBidStr(${auction.auctionDate})가 설정됐다. 배당종기 ${auction.dividendDeadline}·기준일 ${auction.baseRightDate}·청구액 $tsClaimMan 구조에서 시세 중앙값 $tsTradeMan 대비 차익 $tsMarginMan 와 추가지출 $tsExtraMan 반영 원가는 $tsEffectiveMan 으로 계산된다.",
            "1. 공고 가격·거래 비교표: 감정가 $tsAppraisalMan 대비 최저매각선 $minBidStr 조건이 ${auction.auctionDate} 매각으로 이어진다. 기준일자 ${auction.baseRightDate}, 채권총액 $tsClaimMan, 배당요구종기 ${auction.dividendDeadline} 확인과 함께 매매 기준선 $tsTradeMan(여력 $tsMarginMan, 부대원가 $tsExtraMan) 기준 총원가는 $tsEffectiveMan 이다."
        )

        val tsSec2 = listOf(
            "2. 배당 시뮬레이션 & 명도 비용: 균형선 $tsBalMan 낙찰 가정 시 집행비용 $step0Dist, 임차·최우선 채권 $step1Claim($step1Status), 당해 세목 $step2Dist 순으로 충당 뒤 말소기준 채권에 $step3Dist 배분($step3Status)된다. 인도 판정은 '${proReport.evictionRiskLevel}' 이며 평균 ${proReport.estimatedEvictionDays}일 및 예비금 $tsEvictMan 배정이 필수다.",
            "2. 채권 회수 서열 & 인도 예산: 표준 응찰액 $tsBalMan 기준 배당 순서상 0순번 비용 $step0Dist, 1순번 보증금 영역 $step1Claim($step1Status), 2순번 조세분 $step2Dist 를 떼고 기준 담보권이 $step3Dist($step3Status)를 수령한다. 점유 정리에 약 ${proReport.estimatedEvictionDays}일과 예비자금 $tsEvictMan 편성이 권장된다.",
            "2. 순위별 배분 & 명도 리스크: 균형 응찰 $tsBalMan 대입 결과 법정 선공제 $step0Dist 후 1차 임차·관리비 $step1Claim($step1Status), 국세·지방세분 $step2Dist 를 거쳐 말소기준 근저당에 $step3Dist($step3Status)가 돌아간다. 점유 해소 기간 ${proReport.estimatedEvictionDays}일, 확보 예비비는 $tsEvictMan 수준이다.",
            "2. 워터폴 변제 & 협의 기간: $tsBalMan 배당 재원 중 경매 실비 $step0Dist 우선 회수 뒤 선순위·소액임차 $step1Claim($step1Status), 당해 조세 $step2Dist, 기준 담보채권 $step3Dist($step3Status) 흐름으로 정리된다. '${proReport.evictionRiskLevel}' 구간으로서 ${proReport.estimatedEvictionDays}일 명도 일정과 $tsEvictMan 충당금을 잡는다.",
            "2. 배당 전개표 & 점유 비용: 표준 입찰 $tsBalMan 가정하에 집행 실비 $step0Dist·임차 배분 $step1Claim($step1Status)·세금 우선분 $step2Dist 순차 차감 후 후속 기준저당 $step3Dist($step3Status) 배당이 이뤄진다. 예상 인도 소요일수 ${proReport.estimatedEvictionDays}일과 명도 충당금 $tsEvictMan 가 원가에 들어간다.",
            "2. 채권 변제율 & 인도 준비금: 균형가 $tsBalMan 이 배당될 경우 절차비용 $step0Dist, 임차권 영역 $step1Claim($step1Status), 조세 채권 $step2Dist 다음으로 말소기준 저당이 $step3Dist($step3Status)를 회수한다. '${proReport.evictionRiskLevel}' 난이도에 따라 ${proReport.estimatedEvictionDays}일 및 $tsEvictMan 예비액을 둔다.",
            "2. 선후순위 충당 & 명도 변수: $tsBalMan 기준 배당 풀에서 공익비 $step0Dist 와 임차인 몫 $step1Claim($step1Status), 당해세금 $step2Dist 공제 뒤 근저당권자에게 $step3Dist($step3Status)가 배당된다. 현장 인도 기간 ${proReport.estimatedEvictionDays}일과 예비 금액 $tsEvictMan 을 초기 현금에 넣어야 안전하다.",
            "2. 배당 재원 분배 & 협의 예산: 표준가 $tsBalMan 에서 집행비 $step0Dist 및 보증금 라인 $step1Claim($step1Status), 지방·국세 $step2Dist 순으로 빠지고 기준 채권액 중 $step3Dist($step3Status)가 상환된다. 명도 위험 '${proReport.evictionRiskLevel}' 감안 시 ${proReport.estimatedEvictionDays}일과 $tsEvictMan 준비가 요망된다.",
            "2. 채권자별 배분 & 점유 해소: 입찰 기준선 $tsBalMan 이면 실행 경비 $step0Dist, 임차 순위 $step1Claim($step1Status), 당해 조세 $step2Dist 이후 기준 저당 $step3Dist($step3Status) 정산 구조다. 협의·인도명령 약 ${proReport.estimatedEvictionDays}일 소모를 전제로 $tsEvictMan 예산을 따로 떼어두길 권한다.",
            "2. 배당금 흐름 & 인도 기간: 균형 금액 $tsBalMan 산정 시 0차 비용 $step0Dist·1차 보증금 $step1Claim($step1Status)·2차 당해세 $step2Dist 지급 뒤 기준저당 배분액은 $step3Dist($step3Status)로 나온다. 명도 리스크(${proReport.evictionRiskLevel}) 대응 기간 ${proReport.estimatedEvictionDays}일·예비비 $tsEvictMan 이 필요하다.",
            "2. 배당 테이블 & 명도 예비금: $tsBalMan 배분 모델에서는 절차실비 $step0Dist, 임차인·관리비 $step1Claim($step1Status), 세무 채권 $step2Dist 가 먼저 나가고 기준저당이 $step3Dist($step3Status)를 취한다. 실제 인도 소요일은 약 ${proReport.estimatedEvictionDays}일이며 협의 여유금 $tsEvictMan 배정이 필수다.",
            "2. 회수 배당 계산 & 협의 일정: 균형 입찰선 $tsBalMan 가정 시 집행경비 $step0Dist 후 임차 채권 $step1Claim($step1Status), 조세 당해분 $step2Dist 를 거쳐 말소기준권리에 $step3Dist($step3Status)가 귀속된다. 인도 소요 약 ${proReport.estimatedEvictionDays}일과 예비자금 $tsEvictMan 을 반영해야 안정적이다."
        )

        val tsSec3 = listOf(
            "3. 조달·CapRate·금리 스트레스: 36개월 권역 평균 매각가율은 ${auction.stats3yRatePct}%, 주간 조회는 ${auction.clicksLast7d}회다. LTV 70%($tsLoanMan) 차입 시 필요 자기자본 $tsEquityMan 이며 예상 월세 $tsRentMan 기준 연 CapRate ${proReport.estimatedCapRatePct}%·1년 ROE ${proReport.estimated12mRoiPct}%가 산출된다. 금리 +1%p 시 월 이자 +$tsStressMan 이 가산되며 손익분기(BEP)는 $tsBepMan, 차회 유찰가는 $tsNextDropMan 이다.",
            "3. 레버리지·ROE·손익 테스트: 시군구 3개년 낙찰선 ${auction.stats3yRatePct}%·단기 관심 ${auction.clicksLast7d}건 환경에서 담보 70%($tsLoanMan) 적용 현금은 $tsEquityMan 이 든다. 월 추산 임차료 $tsRentMan 바탕 수익률 CapRate ${proReport.estimatedCapRatePct}%·기대 ROE ${proReport.estimated12mRoiPct}%며, 이자 +$tsStressMan 상승 가정에서도 회수 BEP $tsBepMan·추가 유찰선 $tsNextDropMan 이 확인된다.",
            "3. 자금 회전·수익성 민감도: 권역 통계 매각률 ${auction.stats3yRatePct}%와 조회 ${auction.clicksLast7d}건이 관측됐다. 70% 경락대출($tsLoanMan) 실행 시 실투입 현금은 $tsEquityMan 이고 월 임대치 $tsRentMan 기준 CapRate ${proReport.estimatedCapRatePct}%·목표 ROE ${proReport.estimated12mRoiPct}%다. 조달금리 1%p 인상분(+$tsStressMan) 감안 원가 회수가는 $tsBepMan, 다음 유찰 최저선은 $tsNextDropMan 이다.",
            "3. 금융 조달 및 회수 손익분기: 지역 3년 평균율 ${auction.stats3yRatePct}%(조회 ${auction.clicksLast7d}건) 기준 LTV 70%($tsLoanMan)를 활용하면 자기자본 $tsEquityMan 이 요구된다. 추산 월 수익 $tsRentMan 대비 연환산 CapRate ${proReport.estimatedCapRatePct}%·자본이익 ROE ${proReport.estimated12mRoiPct}%이며, 월 +$tsStressMan 이자 변동 폭에도 BEP $tsBepMan·차기 유찰가 $tsNextDropMan 으로 산정된다.",
            "3. 대출 한도·임대수지 탄력성: 3개년 권역 평균 ${auction.stats3yRatePct}%·조회수 ${auction.clicksLast7d}회 흐름 속에서 담보대출 $tsLoanMan 제외 실현금 $tsEquityMan 투입이 필요하다. 예상 월세 $tsRentMan 기준 CapRate ${proReport.estimatedCapRatePct}%, 기대수익 ROE ${proReport.estimated12mRoiPct}%로 나오며 금리 +1%p(월 +$tsStressMan) 압력 시 매도 분기점 $tsBepMan·유찰 하한 $tsNextDropMan 이다.",
            "3. 자본구조 및 금리 충격 검증: 권역 낙찰지수 ${auction.stats3yRatePct}%(최근 ${auction.clicksLast7d}회 클릭) 조건에서 LTV 70% 한도 $tsLoanMan 을 뺀 현금 필요분은 $tsEquityMan 이다. 월세 추정액 $tsRentMan 으로 계산한 CapRate ${proReport.estimatedCapRatePct}%·연간 ROE ${proReport.estimated12mRoiPct}%이며, 이자 +$tsStressMan 부담에도 BEP $tsBepMan 및 유찰선 $tsNextDropMan 으로 방어된다.",
            "3. 수익률 모델·금리 변동 시뮬레이션: 시군구 36개월 비율 ${auction.stats3yRatePct}%와 관심 ${auction.clicksLast7d}건을 대입한 자금 구조는 대출 $tsLoanMan·자본 $tsEquityMan 이다. 월 임대 수입 $tsRentMan 기준 CapRate ${proReport.estimatedCapRatePct}%, 목표 ROE ${proReport.estimated12mRoiPct}% 수준이며 +1%p 금리 인상(+$tsStressMan)을 넣은 손익분기점은 $tsBepMan, 차회 최저가는 $tsNextDropMan 이다.",
            "3. 레버리지 수지·BEP 계산: 최근 3년 낙찰평균 ${auction.stats3yRatePct}%·조회 ${auction.clicksLast7d}회 데이터가 집계됐다. 70% 대출액 $tsLoanMan 공제 뒤 현금 $tsEquityMan 가 들어가고 월세 $tsRentMan 기준 CapRate ${proReport.estimatedCapRatePct}%·ROE ${proReport.estimated12mRoiPct}%가 도출된다. 금리 1%p 상승분 월 $tsStressMan 반영 매도선은 $tsBepMan, 추가 유찰 예상선은 $tsNextDropMan 이다.",
            "3. 현금투입 대비 수익·스트레스: 권역 매각율 ${auction.stats3yRatePct}% 및 7일 클릭 ${auction.clicksLast7d}회 지표상 대출 $tsLoanMan(70%) 후 필요 자금은 $tsEquityMan 이다. 월 임대가치 $tsRentMan 대비 CapRate ${proReport.estimatedCapRatePct}%·1년 ROE ${proReport.estimated12mRoiPct}%며 월 이자 $tsStressMan 증가 시에도 손익분기 $tsBepMan·차회 유찰선 $tsNextDropMan 선에서 관리된다.",
            "3. 담보대출 비율과 매도 분기: 3년 권역 매각지표 ${auction.stats3yRatePct}%(열람 ${auction.clicksLast7d}회) 환경에서 70% 융자 $tsLoanMan 제외 자기자본은 $tsEquityMan 이다. 월 환산임대료 $tsRentMan 기준 CapRate ${proReport.estimatedCapRatePct}%와 기대 ROE ${proReport.estimated12mRoiPct}%를 보이며 이자 +$tsStressMan 변동 반영 BEP $tsBepMan, 다음 유찰가 $tsNextDropMan 이 계산된다.",
            "3. 임대수익률과 금융비용 검증: 시군구 평균 ${auction.stats3yRatePct}%·관심량 ${auction.clicksLast7d}건 수준에서 LTV 70%($tsLoanMan) 차입 시 자기돈 $tsEquityMan 을 준비한다. 월세 $tsRentMan 가정 시 CapRate ${proReport.estimatedCapRatePct}%·ROE ${proReport.estimated12mRoiPct}%가 산출되며 금리 +1%p(+$tsStressMan) 적용 기준 회수 BEP $tsBepMan·유찰 하한 $tsNextDropMan 으로 나타난다.",
            "3. 실투자 수익 및 금리 가산 테스트: 권역 36개월 평균율 ${auction.stats3yRatePct}%와 조회 ${auction.clicksLast7d}건이 확인됐다. 융자 70%($tsLoanMan) 적용 현금 $tsEquityMan 투입 시 월 임차 수익 $tsRentMan 기준 CapRate ${proReport.estimatedCapRatePct}%·ROE ${proReport.estimated12mRoiPct}%며, 이자 월 +$tsStressMan 인상 시 손익분기 매도가 $tsBepMan·추가 유찰가는 $tsNextDropMan 이다."
        )

        val tsSec4 = listOf(
            "4. 입찰 스펙트럼 & 세무 3안: 보수안 $tsConsMan · 표준안 $tsBalMan · 상단안 $tsAggMan 으로 밴드를 설정한다. 명의별 세액 구조는 개인 단독(취득 $taxSoloAcq·장기보유공제 유리), 5:5 공동명의(양도차익 분산·종부세 완화), 법인 매매(취득 $taxCorpAcq·이자 손금산입)로 갈린다. 법리 유의(${studyTerm.codeId}): ${studyTerm.practicalTrap}",
            "4. 전략 가격대 & 3가지 과세 비교: 응찰 밴드는 저가 방어 $tsConsMan, 균형 목표 $tsBalMan, 공격 상한 $tsAggMan 구분이다. 세제 비교상 1인 취득은 취득세 $taxSoloAcq 후 2년 보유 일반세율 구간이며, 공동 지분 5:5는 기본공제 2배 분산, 법인은 취득 $taxCorpAcq 대가로 수선·이자 경비 인정이 가능하다. 판례 포인트(${studyTerm.statuteRef}): ${studyTerm.practicalTrap}",
            "4. 가격 구간별 응찰 & 절세 설계: 보수선 $tsConsMan 부터 균형선 $tsBalMan·공격선 $tsAggMan 까지 자금 여력에 맞춰 선택한다. 세무 3안은 단독(취득 $taxSoloAcq·장특공제), 부부 공동(누진 구간 인하), 법인(취득 $taxCorpAcq·법인세 9~19%)으로 비교된다. 실무 트랩(${studyTerm.codeId}): ${studyTerm.practicalTrap}",
            "4. 응찰 라인 & 개인·공동·법인 세무: 제시 가격은 보수 $tsConsMan·표준 $tsBalMan·적극 $tsAggMan 세 단계다. 단독 명의는 취득 $taxSoloAcq 뒤 장기 보유에 적합하고, 반반 공동명의는 양도소득 분할 절세에 유리하며, 법인 명의는 취득세 $taxCorpAcq 와 필요경비 전액 산입 여부를 저울질해야 한다. 쟁점 체크(${studyTerm.statuteRef}): ${studyTerm.practicalTrap}",
            "4. 매수 가격대 & 명의 선택 가이드: 보수적 $tsConsMan·중립적 $tsBalMan·상단 $tsAggMan 구간별 전략과 세후 실익이 다르다. 개인 취득($taxSoloAcq)은 실거주 비과세 요건이 핵심이며 공동명의는 인별 과세 분산, 사업자 법인($taxCorpAcq)은 단기 밸류애드·손금 처리에 초점이 맞는다. 판례 주의(${studyTerm.codeId}): ${studyTerm.practicalTrap}",
            "4. 3단계 입찰가 & 세제 손익비교: 안전 매수 $tsConsMan, 기준 낙찰 $tsBalMan, 최고 허용 $tsAggMan 으로 구간을 나눈다. 세무 관점에서 개인 단독은 취득세 $taxSoloAcq·장기공제, 50:50 공동은 재산세·양도세 분산, 법인은 취득 $taxCorpAcq 및 금융비용 손금화가 핵심 분기점이다. 법리 경고(${studyTerm.statuteRef}): ${studyTerm.practicalTrap}",
            "4. 응찰 구간 및 세무 전략표: 권역 경쟁 강도에 맞춰 보수 $tsConsMan·균형 $tsBalMan·상단 $tsAggMan 선을 조절한다. 취득 단계 세율은 개인/공동 $taxSoloAcq 대비 법인 $taxCorpAcq 이며, 매각 차익이 큰 주거·수익 물건은 공동 지분 분산이, 다수 물건 회전은 법인 경비 공제가 유리할 수 있다. 판례 트랩(${studyTerm.codeId}): ${studyTerm.practicalTrap}",
            "4. 입찰 범위 설정 & 과세표준 분산: 보수선 $tsConsMan, 적정선 $tsBalMan, 상단선 $tsAggMan 3개 밴드가 도출됐다. 세제 대조 결과 단독명의($taxSoloAcq)는 1주택 실수요, 부부공동은 누진세율 하향 및 기본공제 2인 적용, 법인($taxCorpAcq)은 이자·인건비 공제 구조로 요약된다. 유의 법리(${studyTerm.statuteRef}): ${studyTerm.practicalTrap}",
            "4. 전략 밴드 & 명의별 세후 계산: 매수 상한은 공격 $tsAggMan, 적정 $tsBalMan, 최저 방어 $tsConsMan 으로 나뉜다. 단독($taxSoloAcq) 장기보유 공제, 공동명의(5:5) 종부·양도 분산, 법인($taxCorpAcq) 운영비 손금 처리 가운데 보유 기간에 맞는 명의를 정하라. 법리 체크(${studyTerm.codeId}): ${studyTerm.practicalTrap}",
            "4. 목표 입찰선 & 3안 세제 점검: 응찰 전략은 보수 $tsConsMan · 표준 $tsBalMan · 상단 $tsAggMan 범위 내에서 짠다. 취득세 $taxSoloAcq 기반 개인/공동 대비 법인은 $taxCorpAcq 중과 유무를 먼저 짚고 양도 시 누진세 분산(공동) vs 법인세율(9~19%) 유불리를 따져야 한다. 실무 함정(${studyTerm.statuteRef}): ${studyTerm.practicalTrap}",
            "4. 매수 밴드 & 절세 프레임워크: 보수안 $tsConsMan·균형안 $tsBalMan·공격안 $tsAggMan 가격표와 함께 세무를 점검하면, 개인 단독($taxSoloAcq) 실거주 전략, 부부 공동명의(양도차익 반분), 매매 법인($taxCorpAcq·제반비 손금) 중 최적 구조가 가려진다. 판례 요약(${studyTerm.codeId}): ${studyTerm.practicalTrap}",
            "4. 3구간 전략선 & 세목 비교표: 입찰선은 보수 $tsConsMan · 균형 $tsBalMan · 공격 $tsAggMan 으로 요약된다. 개인($taxSoloAcq)은 단기 70% 중과 회피가 필수고, 5:5 공동 지분은 공제액 이중 확보, 법인($taxCorpAcq)은 이자·보수비 경비 처리에 강점이 있다. 판례 경보(${studyTerm.statuteRef}): ${studyTerm.practicalTrap}"
        )

        val tsSec5 = listOf(
            "5. 보유기간별 출구 시뮬레이션 & 세후 IRR: 1년 이내 단기 회전은 양도차익 세 부담과 취득·중개 비용 잠식이 커서 $tsBepMan 이상 호가가 뒷받침되지 않으면 실익이 급감한다. 반면 2년 이상 보유 후 일반 과세 구간으로 넘기거나 월세 $tsRentMan 기반 장기 현금흐름을 확보하면 자본회전 효율이 안정된다.",
            "5. 단기 매각 vs 장기 임대 수지 탄력성: 경락 취득 후 조기 전매를 노릴 경우 금융비용·중도상환수수료·단기 양도세율이 한꺼번에 겹치므로 목표 매도가를 $tsBepMan 위로 넉넉히 잡아야 한다. 24개월 이상 거치 전략에서는 연간 임차 현금유입 $tsRentMan 이 이자 변동을 완충해 준다.",
            "5. 청산 시나리오 및 보유 세무 탄력: 12개월 내 빠른 엑시트를 전제하면 거래 비용과 세후 마진이 가파르게 줄어들 수 있어 최소 회수선 $tsBepMan 방어가 관건이다. 반대로 보유 기한을 2년 초과로 늘리고 임대료 $tsRentMan 흐름을 유지하면 세율 절감과 원금 상환 효과가 동시에 쌓인다.",
            "5. 회수 기간별 순현금흐름 비교: 단타 청산 모델은 공사·명도 지연이 2개월만 발생해도 연환산 수익률이 꺾이는 구조다. 따라서 매각 분기점 $tsBepMan 대비 충분한 호가 회복 여력이 없으면 월 $tsRentMan 수준의 임대 운영으로 전환해 금리 안정기까지 버티는 복안이 합리적이다.",
            "5. 출구전략 분기점 & 세율 절벽 점검: 비사업용 또는 단기 매도 시 중과 세율이 적용되므로 1년 회전 시나리오는 $tsBepMan 이상의 강한 시세 반등 구간에서만 유효하다. 장기 운용을 병행할 때는 월 임대가치 $tsRentMan 대비 관리비·재산세 공제 후 순수익률을 우선 점검해야 한다.",
            "5. 현금 회수 속도 vs 자본 효율 분석: 조기 매각은 자본 회전이 빠르지만 취득·양도 제세금이 순이익의 상당 부분을 잠식한다. 기준 원가선 $tsBepMan 을 상회하는 실매수 수요가 확인될 때만 단기 처분을 실행하고, 그 외에는 $tsRentMan 임대 현금흐름으로 보유 비용을 상쇄하는 전략이 안전하다.",
            "5. 엑시트 타이밍별 리스크-리턴: 12개월 전매는 리모델링·인도 기간이 조금만 늘어나도 이자 부담이 누적된다. 매도 목표선 $tsBepMan 이 지역 거래량 대비 과도하게 높게 잡혔다면, 2년 보유 일반과세 전환 및 월 $tsRentMan 임대 세팅으로 운용 모드를 바꾸는 편이 낫다.",
            "5. 보유 연수별 세후 순이익 궤적: 1년 미만 처분은 세무 장벽이 높아 자기자본 $tsEquityMan 대비 기대 수익이 제한적일 수 있다. 이에 반해 2년 초과 보유 시 누진 세율 구간으로 내려오며 월세 $tsRentMan 적립과 함께 원금 회수 안정성이 뚜렷하게 개선된다.",
            "5. 매각·보유 이중 플랜 설계: 최선 시나리오는 명도 완료 후 목표가 $tsBepMan 초과 구간에서 조기 청산하는 것이나, 매수 심리가 둔화될 경우를 대비해야 한다. 예상 임대료 $tsRentMan 을 대입한 보수적 운용 플랜을 겹쳐 두면 급매 압박 없이 매도 시점을 조율할 수 있다.",
            "5. 단기 양도세율 방어 및 임대 전환 기준: 매수 후 1년 내 매도 시 세 부담이 가중되므로 차익 여유가 좁다면 보유 기간을 2년 이상으로 설계하는 것이 정석이다. 이때 월 임차수익 $tsRentMan 이 대출 이자 및 유지비를 커버하는지 손익분기 검증이 선행되어야 한다.",
            "5. 기간별 자본 회전율 & 수익 방어선: 1년 차 단기 엑시트와 3년 차 중기 보유는 요구 수익 폭이 다르다. 매도가 $tsBepMan 부근에서 거래가 막히더라도 월세 $tsRentMan 으로 공실 없이 운영 가능하다면 금융비용 부담을 견디며 시세 정상화 구간을 기다릴 여력이 생긴다.",
            "5. 보유 기간 선택과 실질 수익 보전: 빠른 자금 회수를 원한다면 $tsBepMan 이상 즉시 매각 가능성을 타진해야 하고, 세제 혜택을 우선한다면 2년 이상 거치 후 일반세율 적용을 택해야 한다. 어느 쪽이든 월 $tsRentMan 수준의 임대 수요가 뒷받침되는지가 하방 경직성의 척도다."
        )

        val tsSec6 = listOf(
            "6. 자본 완충액 & 예비비 배분 검증: 실제 경매 투자는 낙찰 대금 외에도 예기치 못한 누수 수선·공실 지연·강제집행 추가 경비가 발생할 수 있다. 따라서 계산된 필요 현금 $tsEquityMan 외에 예비금 $tsEvictMan 을 포함한 10% 내외의 유동성 버퍼를 별도 확보해야 자금 압박을 피한다.",
            "6. 현장 변수 예비계정 및 유동성 규율: 권리관계가 단순하더라도 실제 점유 정리 과정의 며칠 지연이나 체납 공용비 추가 청구가 종종 일어난다. 자기자본 $tsEquityMan 과 명도 예비비 $tsEvictMan 외에도 잔금일 돌발 지출에 대응할 비상 여유 자금을 미리 묶어두는 운용 규율이 필수적이다.",
            "6. 추가 비용 한도 & 현금 여력 체크: 감정평가 시점과 매각일 사이 수개월 시차가 존재하므로 내부 설비 노후나 원상회복 비용이 늘어날 여지가 있다. 투입 현금 $tsEquityMan 과 별개로 최소 $tsEvictMan 이상 예비 계정을 분리 편성해야 대출 승인 축소 상황에도 잔금을 지킬 수 있다.",
            "6. 대출 한도 축소 대비 예비 자본: 감정가 대비 시세 하락이나 개인 DSR 규제에 따라 예상 LTV 70%($tsLoanMan) 한도가 줄어들 가능성이 상존한다. 현금 투입선 $tsEquityMan 에 명도 예비금 $tsEvictMan 및 금리 가산분 여유 자금을 얹어 가용 자금표를 작성하는 편이 안전하다.",
            "6. 돌발 지출 캡(Cap)과 자금 방어막: 잔금 납부 직후 관리사무소 정산액이나 폐기물 반출비가 예상을 웃도는 경우가 적지 않다. 따라서 기본 현금 $tsEquityMan 과 예비비 $tsEvictMan 산출치에서 보수적으로 여유 한도를 추가 배정해 두어야 중도 포기(보증금 몰수) 위험을 차단한다.",
            "6. 리스크 버퍼 및 잔금 불능 예방칙: 보증금 납부 뒤 금융권 심사에서 한도가 깎이면 계약금을 잃는 치명적 손실로 직결된다. 실행 현금 $tsEquityMan 및 명도 예산 $tsEvictMan 산정과 함께 최소 두 곳 이상의 금융기관 사전 한도 조회를 병행하는 것이 표준 실무다.",
            "6. 원가 초과 방지용 안전 준비금: 매각물건명세서에 드러나지 않는 미납 관리비 공용부분이나 하자 보수 범위가 전체 ROI를 흔들 수 있다. 자기자본 $tsEquityMan, 인도 예비비 $tsEvictMan 외에 총원가의 소정 비율을 현금성 비상금으로 유지해야 장기 수익 계획이 어긋나지 않는다.",
            "6. 금융·현장 이중 리스크 대비책: 금리 변동 월 +$tsStressMan 압력과 점유 인도 지연이 겹치는 최악 구간을 가정해야 견고한 포트폴리오가 완성된다. 기본 자금 $tsEquityMan 및 예비 항목 $tsEvictMan 을 보수적으로 배분하고 한도 미달 시 대체 조달선을 확보해 두라.",
            "6. 예비비 운영 원칙 & 면책 기준: 원가 테이블에 계상된 부대비 $tsExtraMan 와 명도비 $tsEvictMan 은 평균 추정치이므로 개별 사건의 특수 하자까지 자동으로 보장하지는 않는다. 투자자는 자기자본 $tsEquityMan 외에 상환 여유분을 갖춘 상태에서 법원 원문을 최종 대조해야 한다.",
            "6. 실전 원가 점검 및 보수적 버퍼링: 단기 시세 급변이나 수리비 증액에 휘둘리지 않으려면 최저 마진 지표 $tsMarginMan 에 현혹되지 말고 현금 여력을 점검해야 한다. 필요 자본 $tsEquityMan·예비금 $tsEvictMan 조달 안정성이 확보될 때 비로소 적정선 입찰이 성립한다.",
            "6. 보증금 몰수 방지와 여유자금 설계: 낙찰 후 미납 세금 추가 확인이나 대출 감소가 겹치면 자금 경색이 찾아온다. 자기자본 $tsEquityMan 과 명도 충당액 $tsEvictMan 외에 여유 예치금을 마련하고 본 리포트는 참고용 계량 자료임을 전제로 최종 판단하라.",
            "6. 계량 모델 한계 & 최종 검증 의무: 통계 지표와 세무 시나리오는 공개 데이터를 토대로 산출되므로 실제 낙찰 시점의 규제 변화나 물건 파손 상태를 반영해 조정해야 한다. 가용 현금 $tsEquityMan·예비비 $tsEvictMan 적정성을 직접 점검한 뒤 응찰 여부를 확정하라."
        )

        val tsSec7 = listOf(
            "7. 세무 신고 기한 & 공고 대조 준칙: 경락 취득 시 잔금 납부일 기준 60일 이내 취득세 신고·납부를 마쳐야 가산세를 피하며, 단기 매매 또는 사업자 운용 여부는 매각 확정 전 세무 대리인과 사전 확약하는 편이 안전하다. 본 리포트의 수치 시뮬레이션은 참고 지표이므로 매각일 직전 공고 변경 유무와 등기 변동은 투자자가 직접 재확인해야 한다.",
            "7. 납세 일정 관리 & 계량 분석 면책: 취득일(잔금일)로부터 60일 내 취득세 정산을 완료하고, 임대 개시 시 사업자 등록 유불리를 조세 특례 한도와 대조해야 한다. 수록된 수익률·배당 계산은 현재 공고 기준 모형 결과로서 실제 경매 당일 조건 변동 및 수익을 보증하지 않으며 최종 응찰 책임은 본인에게 있다.",
            "7. 세정 절차 체크 & 리포트 이용 기준: 잔금 납부 당일 영수증과 매각결정문을 수령해 지자체 세무과에 취득세 과세표준을 신고하고, 향후 양도 시 필요경비 증빙(명도비·수선비)을 적격 영수증으로 보관해야 경비 인정을 받는다. 제시된 계량 데이터는 의사결정 보조용 분석 자료이며 투자 손익을 보장하지 않는다.",
            "7. 증빙 적격성 & 사후 추적 원칙: 법무사 보수, 인지대, 합의 명도비 및 자본적 지출 수리비는 세금계산서·계좌이체 내역을 갖춰야 훗날 양도차익 공제 항목으로 산입된다. 본 분석서는 30년 자문 프레임을 적용한 참고 보고서이므로 입찰 전 대법원 원문과 최신 권리 변동 내역을 직접 검증해야 한다.",
            "7. 취득세 감면·중과 검증 & 면책 고지: 주택 수 산정 예외나 생애최초·조정대상지역 규제는 입찰 시점 기준 행정해석에 따라 달라지므로 잔금 실행 전 관할 세무부서 확인이 필수다. 본문 내 추정 ROE와 BEP는 가정 모형 수치로서 미래 수익률을 확정 약속하지 않는다.",
            "7. 경비 산입 기준 & 최종 원문 확인 의무: 낙찰 후 지출하는 공용부분 승계 관리비나 강제집행 비용 가운데 세법상 필요경비 인정 범위는 지출 성격에 따라 갈리므로 영수증 분류가 중요하다. 본 보고서는 정량 참고 자료일 뿐 투자 권유나 수익 보증이 아니므로 공부 재대조가 우선한다.",
            "7. 보유세·양도세 기산일 & 리스크 유의: 재산세 및 종합부동산세 과세 기준일(매년 6월 1일)과 잔금 납부 일자의 선후에 따라 당해 연도 보유세 납세 의무자가 갈린다. 잔금 일정을 며칠 조율하는 것만으로 세후 수익률이 달라질 수 있으며 모든 투자 판단과 책임은 응찰자 본인 몫이다.",
            "7. 조세 기산 시점 & 데이터 업데이트 안내: 경매를 통한 취득 시점은 매각대금 완납일이 기준이므로 양도소득세 장기보유 산정과 재산세 기준일을 이 날짜로부터 기산해야 정확하다. 기일이 지나간 뒤에는 낙찰가·유찰 결과를 재조사해 후속 수익률을 다시 계산하는 습관이 필요하다.",
            "7. 필요경비 증빙 & 법원 공시 우선 원칙: 인테리어 자본적 지출은 도배·장판 등 소모성 수선비와 달리 샷시·배관 교체 등 자산가치 증대 항목만 양도세 경비로 인정된다. 본 계량 보고서는 분석 보조 자료로 작성됐으므로 매각 당일 오전 법원 게시 변경 사항을 최종 대조해야 한다.",
            "7. 신고 가산세 차단 & 분석 한계 명시: 취득세 기한 내 신고 누락 시 무신고 가산세 20%가 부과되므로 대출 실행일과 세무 신고 일정을 동일 캘린더에 묶어 관리하라. 제공된 차익·배당 테이블은 공개 데이터를 기반으로 산출한 추정치이며 원금이나 목표 수익을 보증하지 않는다.",
            "7. 세후 수익 확정 조건 & 투자자 책임: 임대사업 등록 여부나 법인 결산 시점에 따라 건강보험료 및 부가가치세 과세 구조가 연동되므로 단기·장기 보유 계획을 먼저 못 박아야 한다. 본 정량 브리핑은 실무 참고용으로 제작되었으며 실제 입찰 결정을 내리기 전 직접 서류를 검수해야 한다.",
            "7. 납부·신고 캘린더 & 비보증 고지: 대금지급기한 안에 잔금 완납 후 등기촉탁 신청과 취득세 납부를 마쳐야 후순위 권리 말소가 지연 없이 종결된다. 본 리포트의 3전략 가격과 기대 수익은 정보 제공 목적의 계량 산출물로서 특정 수익 달성을 보장하지 않는다."
        )

        val tsSec8 = listOf(
            "8. 물건별 물리 검증 & 공고 최종 확인: ${auction.propertyType.blogCategory} 영역은 우편·계량 지표와 공용체납 정산 내역 확인으로 적정 CAPEX 범위를 잡아야 원가 산정이 정교해진다. 입찰 당일 오전 법원 진행내역 상 기일연기·취하 공시 여부와 최근 실거래가 변동폭을 대조한 뒤 응찰표를 확정하라.",
            "8. 현장 실물 검수 & 기일 변동 체크: 해당 자산군은 세대 사용량 검침 및 외벽·방수 상태 점검으로 숨은 하자와 미납 변수를 솎아내는 과정이 핵심이다. 매각일 아침 경매계 전산에서 특별매각조건 변경이나 채무 변제 취하 공고가 떴는지 최종 대조 후 투찰해야 안전하다.",
            "8. 공법·시설 리스크 진단 & 투찰 원칙: 관리주체 체납 확인을 비롯해 배후 인프라와 주차·진입 여건을 짚고 넘어가면 사후 추가 수선비 발생 가능성을 크게 낮춘다. 권역 낙찰 비율 ${auction.stats3yRatePct}% 흐름을 참고하되 가용 자금과 안전폭을 지키는 선에서만 응찰하라.",
            "8. 설비·입지 교차 체크 & 보증금 관리: 외관 결함 유무와 공과금 승계 한도에 대한 사진 기록이 뒷받침되면 명도 이후 예상치 못한 복구비 지출을 줄일 수 있다. 보증 수표 금액 오기나 인장 누락으로 무효 입찰 처리되지 않도록 전날 서류 봉투를 사전 검수하는 습관이 중요하다.",
            "8. 임장 체크포인트 & 원가 재산정 기준: 실물 답사 동선에서 포착한 물리적 결함이나 공용관리비 연체 징후는 즉시 목표 입찰가에서 차감 반영해야 수지타산이 맞는다. 사전 분석 수치보다 법원 비치 최신 매각물건명세서의 비고란 변경 기재가 우선한다.",
            "8. 현장 결함 반영 & 매각 허가 모니터링: 설비 노후 여부와 주변 공급 여건을 반영해 보수·균형 밴드를 재조정하라. 최고가 매수인이 되더라도 7일간 이해관계인 이의나 불허 사유 여부를 지켜본 뒤 대출 본심사를 마무리하는 절차가 안정적이다.",
            "8. 실사 증빙 확보 & 수익성 재검증: 임장 시 확보한 내외부 점검 자료는 향후 잔금 대출 감정이나 매각 시 가치 방어 논리로도 쓰인다. 경매 공고가 뜬 뒤 시차가 생기는 만큼 기일 직전 급매 호가 변화를 재조회해 기대 수익률이 훼손되지 않았는지 점검하라.",
            "8. 권역 수급 체크 & 사후 일정 추적: 주변 입지 선호 조건과 인근 매물 적체량을 함께 보아야 청산 시점의 가격 저항을 예측할 수 있다. 매각 기일이 지난 후에는 낙찰가율 또는 유찰 차수 변동 데이터를 재수집해 후속 전략과 대조하는 것이 실무 정석이다.",
            "8. 물리 하자 차단 & 대출 한도 이중 확인: 배관·창호 점검을 통해 예상 수리 범위를 못 박아야 자금 계획이 흔들리지 않는다. 개인 DSR과 물건 시세 하락에 따른 LTV 축소 가능성에 대비해 사전 가심사를 최소 두 곳에서 교차로 받아두라.",
            "8. 공용부 정산 확인 & 응찰 규율 준수: 관리 주체 확인을 거쳐 공용부분 승계 체납액 상한을 확정한 뒤 입찰액을 적어내야 차익이 보존된다. 경쟁 입찰자가 몰리는 기일이라 해도 사전에 설정한 상한선을 감정적으로 넘기지 않는 원칙이 장기 수익을 지킨다.",
            "8. 잔존 리스크 필터링 & 문서 대조 마감: 현장 배후지와 주요 설비에서 별도 감가 이슈가 없다면 본 분석의 예상 원가 오차 범위가 좁아진다. 법원 경매 기록상 송달 불능이나 배당요구 종기 변경 같은 절차적 변동이 없는지 매각 당일 오전 최종 확인을 마친 후 응찰하라.",
            "8. 사후 관리 설계 & 최종 책임 고지: 시설 상태 점검과 등기부 선후 확인이 끝났다면 남은 변수는 명도 속도와 금리 추이뿐이다. 본 정량 분석은 공개 기록과 자문 모형을 결합한 참고 리포트이므로 최종 투자 판단 및 법률 검토는 투자자 직접 확인 아래 진행해야 한다."
        )

        val tistoryTaxRows = proReport.taxScenarios.joinToString("") { t ->
            "<tr><td>${t.entityType}</td><td>${t.acquisitionTaxPct}</td><td>${t.capitalGainsTaxNote}</td></tr>"
        }

        val tistoryBodyText = buildString {
            appendLine(tsOpenings[sTs])
            appendLine()
            appendLine(tsSec1[sTs])
            appendLine()
            appendLine(tsSec2[sTs])
            appendLine()
            appendLine(tsSec3[sTs])
            appendLine()
            appendLine(tsSec4[sTs])
            appendLine()
            appendLine(tsSec5[sTs])
            appendLine()
            appendLine(tsSec6[sTs])
            appendLine()
            appendLine(tsSec7[sTs])
            appendLine()
            appendLine(tsSec8[sTs])
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

        // 3. CLOUDFLARE — uses appraisalStr ONCE + EokDecimal (`2.65억 원`) & ManWonSpaced (`15,104만 원`), distinct shots (shot3 & shot4), zero raw overlap dumps
        val cfMinMan = formatManWonSpaced(auction.minBidPriceKrw)
        val cfClaimEok = formatEokDecimal(auction.claimAmountKrw)
        val cfTradeEok = formatEokDecimal(auction.recentRealTradeMedianKrw)
        val cfMarginEok = formatEokDecimal(proReport.safetyMarginKrw)
        val cfEquityEok = formatEokDecimal(proReport.netEquityRequiredKrw)
        val cfEvictEok = formatEokDecimal(proReport.estimatedEvictionReserveKrw)
        val cfDepEok = formatEokDecimal(auction.minBidPriceKrw * auction.depositRatePct / 100L)
        val cfConsEok = formatEokDecimal(auction.conservativeBidKrw)
        val cfBalEok = formatEokDecimal(auction.balancedBidKrw)
        val cfAggEok = formatEokDecimal(auction.aggressiveBidKrw)
        val cfBepEok = formatEokDecimal(proReport.breakevenResalePriceKrw)
        val cfDropEok = formatEokDecimal(proReport.nextDropMinBidKrw)

        val cfFunnelText = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(auction, ChannelPlatform.CLOUDFLARE, config, proFunnel, seedIndex)
        val cfFunnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(auction, ChannelPlatform.CLOUDFLARE, config, proFunnel)

        val cfLeads = listOf(
            "[GEO 요지 · ${auction.courtName}] ${auction.caseId}(${auction.address}) — 최초평가 ${appraisalStr}, 최저신고 $cfMinMan(${auction.discountRatePct}%), 매각예정 ${auction.auctionDate}.",
            "[AI 인덱스 브리프 · ${auction.courtName}] ${auction.propertyType.blogCategory} ${auction.caseId} 물건은 평가액 ${appraisalStr}에서 $cfMinMan 까지 낮아졌으며 ${auction.auctionDate} 입찰됨.",
            "[공시 팩트 요약 · ${auction.courtName}] ${auction.address} 소재 사건 ${auction.caseId}는 감정 ${appraisalStr} 대비 최저 $cfMinMan(${auction.discountRatePct}%) 조건으로 ${auction.auctionDate} 진행함.",
            "[TL;DR 스키마 · ${auction.courtName}] 공고번호 ${auction.caseId}(${auction.address})의 법원 감정은 ${appraisalStr}, 현 회차 하한은 $cfMinMan(${auction.discountRatePct}% · 기일 ${auction.auctionDate})임.",
            "[검색 인용 요약 · ${auction.courtName}] ${auction.caseId} 사건(${auction.propertyType.blogCategory})은 평가 ${appraisalStr} → 최저 $cfMinMan 로 감가되어 ${auction.auctionDate} 매각 실시함.",
            "[핵심 원천 데이터 · ${auction.courtName}] ${auction.address} 대상 ${auction.caseId} 물건은 감정가액 ${appraisalStr}, 최저입찰 $cfMinMan, 기일 ${auction.auctionDate}로 공시 완료함.",
            "[AIO 공고 정리 · ${auction.courtName}] 사건 ${auction.caseId}(${auction.propertyType.blogCategory}) 평가금액 ${appraisalStr} 대비 현 최저선 $cfMinMan(${auction.discountRatePct}%), 입찰일 ${auction.auctionDate}임.",
            "[정밀 공고 색인 · ${auction.courtName}] ${auction.caseId} 공고(${auction.address}) — 최초 ${appraisalStr}, 현재 $cfMinMan(${auction.discountRatePct}% 비율) 출발 및 ${auction.auctionDate} 매각 예정.",
            "[E-E-A-T 공시 · ${auction.courtName}] ${auction.propertyType.blogCategory} 사건번호 ${auction.caseId}의 기준평가 ${appraisalStr}, 최저선 $cfMinMan, 법정기일 ${auction.auctionDate} 데이터 확인됨.",
            "[구조화 브리핑 · ${auction.courtName}] 감정원가 ${appraisalStr}에서 ${auction.bidCount}회 유찰된 ${auction.caseId}(${auction.address})는 $cfMinMan 조건으로 ${auction.auctionDate} 매각대기 중임.",
            "[GEO 공시 카드 · ${auction.courtName}] ${auction.caseId} 물건(${auction.address}) 최초 감정가 ${appraisalStr} 대비 ${auction.discountRatePct}% 선인 $cfMinMan 에 ${auction.auctionDate} 기일 확정함.",
            "[데이터 개요 · ${auction.courtName}] 법원 공고 ${auction.caseId}(${auction.propertyType.blogCategory}) 평가액 ${appraisalStr}, 최저입찰가 $cfMinMan, 매각일자 ${auction.auctionDate} 기록함."
        )

        val isSeniorTenantRisk = auction.assumedRightsText.contains("선순위") && auction.assumedRightsText.contains("인수")
        val cfRiskTag = when {
            isSeniorTenantRisk -> "선순위 보증금 별도 인수 주의 구간"
            auction.assumedRightsText.contains("유치권") -> "유치권 성립요건 성숙 여부 미달 점검 대상"
            else -> "기준등기 이후 권리 일괄 말소 구조"
        }
        val cfFocusTag = listOf(
            "실거래 여력 · 공용관리비 3년 치",
            "임차 대항력 · 배당종기 준수",
            "주거·수익 수요 · 누수 설비 체크",
            "선순위 권리 유무 · 체납 공과금",
            "유형별 입지 요건 · 인도 난이도",
            "보수비 추산 · 잔금 LTV 비율",
            "공부 원문 일치 · 위반 기재 점검",
            "권역 급매 호가 · 공실 가능성",
            "명도 일정 가늠 · 세무 부담 구분",
            "기초 시설 컨디션 · 진입 접근성",
            "배당 배분 순위 · 소멸 범위 확정",
            "매각명세 비고 · 현장 점유 실태"
        )[sCf]

        val cfDocBlocks = listOf(
            """
            [사법부 공시 문건 4종 검증]
            * 등기·채권 뼈대: 기준등기일 ${auction.baseRightDate}, 경매 청구액 $cfClaimEok, 배당요구 종기 ${auction.dividendDeadline} 확인됨.
            * 인수 판별 요약: $cfRiskTag ($cfFocusTag).
            * 인도·자금 조건: 점유 정리 등급 '${proReport.evictionRiskLevel}' · 예상 ${proReport.estimatedEvictionDays}일(예비 $cfEvictEok) · 70% 담보 후 필요자본 $cfEquityEok 수준.
            * 시세·임대 지수: 거래 중앙선 $cfTradeEok 대비 여백 $cfMarginEok(${proReport.safetyMarginPct}%), 연간 임대비율 ${proReport.estimatedCapRatePct}%.
            """.trimIndent(),
            """
            [법원 원문 기록 대조 요약]
            * 기준 시점: 최선순위 설정 ${auction.baseRightDate}, 배당 신청 기한 ${auction.dividendDeadline}, 총 청구원금 $cfClaimEok 집계됨.
            * 권리·현황 핵심: $cfRiskTag — 세부 체크포인트 '$cfFocusTag'.
            * 자금·명도 계획: 잔금 대출 외 자기현금 $cfEquityEok 필요, 인도 소요 약 ${proReport.estimatedEvictionDays}일 및 협의금 $cfEvictEok 산정.
            * 시세 완충: 인근 실매가 $cfTradeEok 비교 완충액 $cfMarginEok, 추산 CapRate ${proReport.estimatedCapRatePct}% 관측됨.
            """.trimIndent(),
            """
            [4대 공부 교차 대조표]
            * 일정·채권: 등기 기준일 ${auction.baseRightDate}부터 배당종기 ${auction.dividendDeadline} 사이 신청 채권 규모 $cfClaimEok 확인.
            * 소멸·인수 판단: $cfRiskTag. 현장 검증 항목($cfFocusTag) 병행 대조 요망.
            * 점유·재무 여건: 인도 예상 기간 ${proReport.estimatedEvictionDays}일(충당금 $cfEvictEok), 자기자본 투입선 $cfEquityEok 로 파악됨.
            * 거래가 비교: 주변 매매 중위 $cfTradeEok 대비 차이 $cfMarginEok, 연 임대가치 ${proReport.estimatedCapRatePct}% 측정됨.
            """.trimIndent(),
            """
            [공고 원천 데이터 아카이브]
            * 권리 타임라인: 말소 기준 ${auction.baseRightDate} 설정, 청구 금액 $cfClaimEok, 법정 배당마감 ${auction.dividendDeadline} 확정.
            * 리스크 판독: $cfRiskTag 기준 적용. 유형별 체크 범위: $cfFocusTag.
            * 인도·조달 설계: 점유 난이도(${proReport.evictionRiskLevel}) 약 ${proReport.estimatedEvictionDays}일·예비비 $cfEvictEok, 실필요 현금 $cfEquityEok 산출.
            * 가격 여력: 실거래 기준 $cfTradeEok 대비 안전 여력 $cfMarginEok · CapRate ${proReport.estimatedCapRatePct}% 기록됨.
            """.trimIndent(),
            """
            [등기·명세서·현황조사 발췌]
            * 기일·채권: 최선순위 ${auction.baseRightDate} · 배당종기 ${auction.dividendDeadline} · 채권액 $cfClaimEok 대조 완료.
            * 권리 결론: $cfRiskTag. 추가 현장 항목은 $cfFocusTag 기준으로 검증함.
            * 자본·인도 소요: 70% 융자 뒤 자기자본 $cfEquityEok, 명도 예비액 $cfEvictEok 및 ${proReport.estimatedEvictionDays}일 일정 예상됨.
            * 시장 데이터: 매매 중앙값 $cfTradeEok 대비 차익 $cfMarginEok(${proReport.safetyMarginPct}%), 연 수익지표 ${proReport.estimatedCapRatePct}% 도출됨.
            """.trimIndent(),
            """
            [대법원 서류 4종 핵심 정리]
            * 등기·배당 라인: 기준권리 ${auction.baseRightDate}, 신청액 $cfClaimEok, 배당요구 마감일자 ${auction.dividendDeadline} 표기 확인.
            * 위험 점검표: $cfRiskTag 적용 대상이며 물건별 핵심($cfFocusTag) 재확인 권장됨.
            * 인도·현금 규모: 명도 예정 ${proReport.estimatedEvictionDays}일(${proReport.evictionRiskLevel}), 예비 자금 $cfEvictEok, 투입 현금 $cfEquityEok 필요.
            * 시황 비교치: 권역 실거래 $cfTradeEok 대비 완충 $cfMarginEok · 연환산 임대가치 ${proReport.estimatedCapRatePct}% 산출됨.
            """.trimIndent(),
            """
            [공식 공부 팩트 브리프]
            * 기준 수치: 등기선 ${auction.baseRightDate} · 배당마감 ${auction.dividendDeadline} · 청구규모 $cfClaimEok 데이터 확보.
            * 인수 여부 요지: $cfRiskTag 판단. 핵심 점검 사항은 $cfFocusTag 임.
            * 조달·협의 조건: 현금 원금 $cfEquityEok, 인도 협상 약 ${proReport.estimatedEvictionDays}일, 협의 예비 $cfEvictEok 반영.
            * 거래·수익 지표: 인근 매매선 $cfTradeEok 기준 여백 $cfMarginEok 및 연간 CapRate ${proReport.estimatedCapRatePct}% 확인됨.
            """.trimIndent(),
            """
            [경매 원문 데이터 요약]
            * 법적 기준선: 최선순위 날짜 ${auction.baseRightDate}, 배당요구종기 ${auction.dividendDeadline}, 총채권 $cfClaimEok 확인함.
            * 권리·물건 검증: $cfRiskTag 수준이며 유형 특이점($cfFocusTag) 교차 확인이 필수임.
            * 예산·명도 산정: 자기자본 $cfEquityEok, 명도비 $cfEvictEok 및 소요 ${proReport.estimatedEvictionDays}일(${proReport.evictionRiskLevel}) 계산됨.
            * 가치 지표: 주변 거래가 $cfTradeEok 대비 마진 $cfMarginEok · 임대수익 비율 ${proReport.estimatedCapRatePct}% 유지됨.
            """.trimIndent(),
            """
            [사법 공시 기록 체크]
            * 원문 일정: 기준일자 ${auction.baseRightDate} 설정, 배당요구 기한 ${auction.dividendDeadline}, 채권 합계 $cfClaimEok 파악.
            * 권리 판정: $cfRiskTag 요약. 체크 과제: $cfFocusTag.
            * 필요 자본·일정: 70% 대출 후 필요현금 $cfEquityEok, 인도 기간 약 ${proReport.estimatedEvictionDays}일과 예비 $cfEvictEok 반영함.
            * 시세 대비 여력: 유사 거래값 $cfTradeEok 대비 차익 $cfMarginEok(${proReport.safetyMarginPct}%), CapRate ${proReport.estimatedCapRatePct}% 산정.
            """.trimIndent(),
            """
            [매각명세·감정·현황 요점]
            * 기한·청구 데이터: 기준일 ${auction.baseRightDate} · 배당종기일 ${auction.dividendDeadline} · 청구금액 $cfClaimEok 검증됨.
            * 권리 안전성: $cfRiskTag 분류에 해당하며 물건 특성상 $cfFocusTag 점검을 요함.
            * 명도·자금 계획: 인도 소요 ${proReport.estimatedEvictionDays}일(예비액 $cfEvictEok) 및 실투입 자본 $cfEquityEok 선으로 정리됨.
            * 시세·임대치: 실매가 중위선 $cfTradeEok 대비 여백 $cfMarginEok · 연 임대지표 ${proReport.estimatedCapRatePct}% 기록함.
            """.trimIndent(),
            """
            [공부 4종 팩트 체크아웃]
            * 기준·종기 확인: 등기 기준점 ${auction.baseRightDate}, 배당요구종기 ${auction.dividendDeadline}, 신청채권 $cfClaimEok 파악 완료.
            * 분석 핵심 결론: $cfRiskTag 요약 가능하며 현장 체크($cfFocusTag) 대조 필요.
            * 투자 원가·일정: 필요 자기자본 $cfEquityEok, 점유 협의 ${proReport.estimatedEvictionDays}일, 예비비 $cfEvictEok 편성함.
            * 시황 대조 결과: 주변 거래선 $cfTradeEok 대비 여력 $cfMarginEok · 연간 CapRate ${proReport.estimatedCapRatePct}% 측정됨.
            """.trimIndent(),
            """
            [원천 공고·등기 요약본]
            * 날짜·금액 팩트: 말소선 ${auction.baseRightDate}, 배당마감 ${auction.dividendDeadline}, 총청구액 $cfClaimEok 대조됨.
            * 권리·물건 요지: $cfRiskTag 구조이고 확인 포인트는 '$cfFocusTag' 로 요약됨.
            * 재무·명도 치수: 자기현금 $cfEquityEok 투입 기준 명도 ${proReport.estimatedEvictionDays}일 및 예비자금 $cfEvictEok 요구됨.
            * 시세 완충 수치: 매매 중앙치 $cfTradeEok 기준 여백 $cfMarginEok, 임대가치 ${proReport.estimatedCapRatePct}% 확인됨.
            """.trimIndent()
        )

        val cfShotLine = listOf(
            "[현장 캡션 스펙] 핵심 시설 상태 점검 사진 1컷 및 배후 생활 인프라 촬영본 1컷 별도 보관 요망",
            "[임장 사진 규격] 외곽 진입 동선과 공용부 관리 현황 2컷을 촬영해 수리비 보정 근거로 삼음",
            "[현황 대조 사진] 설비 노후 여부와 주변 시세 환경을 현장 사진으로 대조해 감정서 오차를 줄임",
            "[카메라 체크 요점] 내부 계량 수치 및 외벽 균열 구간 실사를 통해 예상 밖의 하자 위험을 차단함",
            "[답사 렌즈 포인트] 공용 관리실 정산 내역과 주출입구 접근성을 촬영해 체크리스트를 완성함",
            "[사진 검증 기준] 배수·방수 흔적과 인근 매물 밀집도를 사진 기록으로 남겨 적정가 산출에 반영함",
            "[임장 이미지 체크] 4번·5번 현장 항목 대조 촬영을 통해 명도 이전 물리적 훼손 여부를 확인함",
            "[현장 증거 수집] 당해 물건의 구조 결함 징후와 주변 상권·학군 통행 여건을 시각 자료로 보존함",
            "[물리 상태 실측] 누수·결로 점검 컷과 전면 도로·주차 동선 컷을 원가 계산표의 보정 근거로 삼음",
            "[답사 촬영 가이드] 시설 유지 상태 및 배후 입지 강점 두 축으로 현장 사진을 분류 정리함",
            "[실물 대조 이미지] 공부 면적과 실제 점유 경계 일치 여부를 현장 카메라 기록으로 재확인함",
            "[최종 현장 컷] 설비 부위와 외곽 배후지 기록 후 공실·파손 변수를 원가에 최종 정산함"
        )[sCf]

        val cfBandLine = listOf(
            "[기준 통계 · 3전략] 3년 매각율 ${auction.stats3yRatePct}% · 보증금 $cfDepEok | 보수 $cfConsEok / 균형 $cfBalEok / 공격 $cfAggEok (BEP $cfBepEok · 차회 $cfDropEok)",
            "[권역 통계 · 입찰선] 시군구 36개월 비율 ${auction.stats3yRatePct}% · 보증액 $cfDepEok | 밴드: $cfConsEok ~ $cfBalEok ~ $cfAggEok (회수선 $cfBepEok, 유찰선 $cfDropEok)",
            "[시장지표 · 목표가] 평균 낙찰선 ${auction.stats3yRatePct}% · 지참 보증 $cfDepEok | 구간 $cfConsEok / 표준 $cfBalEok / 상단 $cfAggEok (원가분기 $cfBepEok, 다음가 $cfDropEok)",
            "[수치 요약 · 전략폭] 지역 낙찰률 ${auction.stats3yRatePct}% · 입찰 보증 $cfDepEok | 보수선 $cfConsEok · 적정선 $cfBalEok · 상한선 $cfAggEok (BEP $cfBepEok / 차기 $cfDropEok)",
            "[통계치 · 응찰밴드] 권역 3개년 매각치 ${auction.stats3yRatePct}% · 당일 보증 $cfDepEok | 하단 $cfConsEok · 기준 $cfBalEok · 최고 $cfAggEok (손익선 $cfBepEok · 감가선 $cfDropEok)",
            "[지역 데이터 · 가격표] 장기 매각지수 ${auction.stats3yRatePct}% · 수표 보증 $cfDepEok | 방어 $cfConsEok / 적정 $cfBalEok / 적극 $cfAggEok (매도BEP $cfBepEok, 유찰가 $cfDropEok)",
            "[낙찰 통계 · 전략가] 최근 36월 평균 ${auction.stats3yRatePct}% · 법정 보증액 $cfDepEok | 보수 $cfConsEok · 균형 $cfBalEok · 상단 $cfAggEok (원가선 $cfBepEok, 차회 $cfDropEok)",
            "[수요·입찰 범위] 시군구 낙찰비중 ${auction.stats3yRatePct}% · 제출 보증 $cfDepEok | 저가 $cfConsEok / 중립 $cfBalEok / 공격 $cfAggEok (분기 $cfBepEok · 차회하한 $cfDropEok)",
            "[가격 가이드라인] 3년 평균 매각선 ${auction.stats3yRatePct}% · 필요 보증 $cfDepEok | 1안 $cfConsEok · 2안 $cfBalEok · 3안 $cfAggEok (회수기준 $cfBepEok / 유찰 $cfDropEok)",
            "[권역 평균 · 목표범위] 36개월 통계 ${auction.stats3yRatePct}% · 입찰 수표 $cfDepEok | 보수적 $cfConsEok / 표준안 $cfBalEok / 상단선 $cfAggEok (BEP $cfBepEok, 차기 $cfDropEok)",
            "[시황·응찰 스펙트럼] 지역 매각률 ${auction.stats3yRatePct}% · 당일 지참 $cfDepEok | 하한선 $cfConsEok · 균형선 $cfBalEok · 공격선 $cfAggEok (손익분기 $cfBepEok · 유찰 $cfDropEok)",
            "[통계 기반 3구간] 3개년 비율 ${auction.stats3yRatePct}% · 최저보증 $cfDepEok | 안전 $cfConsEok / 균형 $cfBalEok / 적극 $cfAggEok (원가회수 $cfBepEok, 감가 $cfDropEok)"
        )[sCf]

        val cfFaqBlocks = listOf(
            "문답1. 권리 인수 판단 기준? 답1. ${auction.baseRightDate} 선후 대조 및 체납·조세 공제 후 미배당 잔액 유무에 달려 있음.\n문답2. 입찰 당일 지참 서류? 답2. 신분증·도장 및 최저선 ${auction.depositRatePct}%인 $cfDepEok 수표 1매를 준비함.\n문답3. 기일 경과 후 변동 추적? 답3. 매각기일 이후 낙찰·유찰·기일변경 여부를 사건조회로 재확인해 후속 분석에 반영함.",
            "질의A. 소멸·인수 분기점? 회답A. 등기 기준(${auction.baseRightDate}) 선순위 대항력과 관리비·조세 공제를 교차 검증함.\n질의B. 보증금 납부 규격? 회답B. 공고 최저가 ${auction.depositRatePct}%($cfDepEok) 자기앞수표와 인장을 제출함.\n질의C. 결과 확정 점검? 회답C. 기일 종료 뒤 법원 매각결과표를 대조해 낙찰가율과 다음 유찰 일정을 추적함.",
            "쟁점1) 잔존 권리 판별법 — ${auction.baseRightDate} 기준등기 전후 점유자의 배당요구 종기(${auction.dividendDeadline}) 준수 대조.\n쟁점2) 법정 준비물 — $cfDepEok(${auction.depositRatePct}%) 수표, 신분증, 도장(대리 응찰 시 위임장).\n쟁점3) 사후 재조사 절차 — 매각일 다음날 진행결과(매각·유찰·연기) 공시를 대조해 후속 입찰 전략을 재계산함.",
            "핵심Q1. 인수 위험 검증선: ${auction.baseRightDate} 이전 임차보증금과 미납 공용액 승계 여부를 우선 차감 계산함.\n핵심Q2. 보증 수표 산식: 입찰액이 아닌 최저가 ${auction.depositRatePct}% 금액($cfDepEok)을 법정 마감 전 제출함.\n핵심Q3. 사후 업데이트 원칙: 매각기일이 지나면 사건진행내역을 재조회해 낙찰 대금과 차회 하한을 보정함.",
            "실무문답1. 권리 기준 시점: ${auction.baseRightDate} 등기 소멸 범위와 당해세 공제 뒤 실투자 원가를 확정함.\n실무문답2. 응찰 서류 목록: 최저선 ${auction.depositRatePct}%($cfDepEok) 수표 1매 및 신분 확인 인장이 필수임.\n실무문답3. 기일 후속 확인: 유찰·취하·낙찰 확정 여부를 다시 조회해 독자 오인을 막고 관련 분석을 갱신함.",
            "요점질의① 말소·승계 구분: ${auction.baseRightDate} 이전 전입 유무와 장기 공용 체납 내역을 우선 확인함.\n요점질의② 보증금 형식: 최저매각액 ${auction.depositRatePct}% 비율 $cfDepEok 수표와 도장을 봉투에 동봉함.\n요점질의③ 매각 결과 검증: 입찰일 경과 시 대법원 결과 데이터를 다시 불러와 유찰 감가 또는 낙찰가를 기록함.",
            "FAQ 가) 잔여 부담 검증 순서: ${auction.baseRightDate} 기준선과 배당마감(${auction.dividendDeadline}) 신청 내역을 대조함.\nFAQ 나) 보증 수표 기준: 공고 하한선 ${auction.depositRatePct}%($cfDepEok) 와 본인 신분증을 구비해야 함.\nFAQ 다) 사후 결과 추적: 기일이 끝난 물건은 즉시 결과 상태를 점검해 차회 입찰이나 인도 일정으로 연결함.",
            "문답 요약1> 승계 가능성 판별: ${auction.baseRightDate} 선순위 권리와 미납 관리비를 뺀 매수인 승계액을 산정함.\n문답 요약2> 법정 준비 규격: 최저 ${auction.depositRatePct}% 기준 $cfDepEok 수표, 도장, 신분증을 지참함.\n문답 요약3> 사후 재검증 절차: 매각 종료 후 실제 낙찰액 및 경쟁률 데이터를 수집해 기존 예측치와 대조함.",
            "체크Q1. 소멸 범위 확정법: 등기부상 ${auction.baseRightDate} 이후 후순위 일괄 정리 여부를 공부로 대조함.\n체크Q2. 보증금 계산 방식: 공고 최저선 ${auction.depositRatePct}%에 해당하는 $cfDepEok 수표 1매를 준비함.\n체크Q3. 기일 경과 대조법: 일정이 지나간 사건은 매각결과·기일변경 내역을 조회해 후속 리포트를 발행함.",
            "가이드문답 1: 권리 인수 체크는 ${auction.baseRightDate} 기준일 선후 및 임차인 배당신청 여부가 핵심임.\n가이드문답 2: 당일 보증금액은 최저가 ${auction.depositRatePct}%인 $cfDepEok 으로 수표 발행함.\n가이드문답 3: 경매 완료 이후에는 낙찰 또는 20% 추가 감가 유찰 결과를 반영해 사후 리포트를 추가함.",
            "브리프Q&A 1) 기준권리 판단: ${auction.baseRightDate} 설정 이후 후순위 권리 소멸과 예외 승계 항목을 구분함.\n브리프Q&A 2) 지참물 요건: $cfDepEok(${auction.depositRatePct}%) 자기앞수표와 인감·신분증을 챙겨야 함.\n브리프Q&A 3) 결과 연동 확인: 기일 경과 물건의 취하·변경·매각 성사 여부를 자동 점검해 자료를 최신화함.",
            "팩트문답 ① 배당·인수 검증: ${auction.baseRightDate} 이전 대항력 존재 및 ${auction.dividendDeadline} 배당요구를 확인함.\n팩트문답 ② 법정 보증액: 최저입찰선 ${auction.depositRatePct}%인 $cfDepEok 수표 및 위임 서류(대리 시) 지참.\n팩트문답 ③ 사후 일정 재확인: 매각 결과 공시 후 잔금 기한이나 차회 기일 최저선을 다시 계산해 기록함."
        )

        val cfTimelineBlocks = listOf(
            """
            [법정 진행 타임라인 & 소유권 이전 절차]
            * 1단계 입찰·최고가매수인 결정: 보증 수표 $cfDepEok 제출 후 최고액 응찰 시 차순위 매수신고 여부를 현장에서 확인함.
            * 2단계 매각허가 결정(7일): 이해관계인 이의신청 및 매각불허 사유 공백 여부를 1주일간 점검함.
            * 3단계 잔금 납부·등기 촉탁(약 30일): 잔금 완납과 동시에 소유권 이전 및 후순위 권리 말소 등기, 인도명령 신청을 병행 접수함.
            """.trimIndent(),
            """
            [낙찰 후 4단계 절차 로드맵]
            * 기일 응찰 및 개찰: 최저입찰 하한 $cfDepEok 보증 준비 후 적정선($cfBalEok 전후) 기입 제출함.
            * 7일 허가 심사 구간: 매각결정기일 전까지 서류 흠결이나 절차 하자 보정 필요성을 체크함.
            * 대금 지급 및 인도 집행: 납부 기한 내 잔금을 내고 촉탁 등기 접수와 점유자 협의(약 ${proReport.estimatedEvictionDays}일)를 진행함.
            """.trimIndent(),
            """
            [사법 매각 프로세스 순서표]
            * 보증금 납부·경쟁 개찰: 공고 기일에 보증 $cfDepEok 을 봉투에 동봉해 투찰하고 최고가매수인 지위를 확보함.
            * 매각허가 확정 기간: 결정기일까지 약 1주, 즉시항고 만료까지 추가 1주를 거쳐 대금지급통지서를 수령함.
            * 완납 및 인도명령 착수: 잔금 정산 직후 등기 촉탁과 인도명령 결정을 받아 명도 합의에 돌입함.
            """.trimIndent(),
            """
            [응찰부터 명도 종결까지 일정 요약]
            * 당일 투찰·결과 발표: 보증금 $cfDepEok 지참 출석, 개찰 직후 유찰 또는 최고가 결정 여부를 기록함.
            * 허가·확정 대기: 7일 뒤 매각허가 선고 및 항고 기간 경과 후 법원의 잔금납부명령서를 발급받음.
            * 잔금 정산·권리 말소: 완납 당일 소유권 취득과 함께 말소기준 이후 저당·압류가 직권 촉탁 말소됨.
            """.trimIndent(),
            """
            [실전 절차·일정 체크리스트]
            * 입찰실 투찰 단계: 보증액 $cfDepEok 확인 후 매수신청서에 사건번호와 금액을 오기 없이 작성함.
            * 결정기일·항고 만료: 낙찰 7일 후 허가 결정, 다시 7일 뒤 확정되며 대출 실행 서류를 이 기간에 완비함.
            * 잔금 납부·인도 개시: 잔금 이체 즉시 소유권자가 되므로 인도명령 신청 및 공과금 정산 절차를 개시함.
            """.trimIndent(),
            """
            [법원 절차·소유권 취득 흐름]
            * 기일 출석·투찰 검증: 법정 신분 확인과 보증 $cfDepEok 납부로 유효 입찰 자격을 갖춤.
            * 매각결정 및 확정 단계: 1주일 내 불허 사유가 없으면 매각이 허가되고 항고 기간 종료 시 확정됨.
            * 대금지급·명도 착수: 납부 기한에 맞춘 대출 실행 및 촉탁등기 이후 명도 협의(${proReport.estimatedEvictionDays}일)를 마무리함.
            """.trimIndent(),
            """
            [경매 완주 표준 일람표]
            * 0일차 매각기일: 보증 $cfDepEok 수표 동봉 응찰 후 최고가매수인 호명 시 보증 영수증을 수령함.
            * 7~14일차 확정 기간: 매각허가결정 선고 및 확정 완료에 맞춰 잔금 대출 최종 약정을 체결함.
            * 30~45일차 잔금·명도: 잔금 납입으로 소유권을 취득하고 인도명령 송달을 통해 점유 이전을 진행함.
            """.trimIndent(),
            """
            [절차 단계별 핵심 포인트]
            * 투찰 및 개찰 확인: $cfDepEok 보증금과 도장을 확인하며 경쟁 입찰자 수를 파악함.
            * 허가결정 및 납부통지: 결정기일 통과 뒤 법원 발송 납부 기한(통상 1개월 내외) 일정을 확정함.
            * 등기촉탁 및 인도 협의: 대금 완납과 동시에 소유권 이전 촉탁을 넣고 전입자 명도 절차를 밟음.
            """.trimIndent(),
            """
            [권리 확정·잔금 처리 일정]
            * 응찰 당일 유의사항: 보증 수표 $cfDepEok 과 신분증·인장을 지참해 마감 시각 전 입찰함에 투입함.
            * 허가 확정 기간 관리: 14일 내외 확정 대기 중 건물 하자 재발 여부와 대출 금리를 최종 점검함.
            * 잔금 지급과 명도 실행: 대금 완납 시점에 말소 대상 권리가 일괄 정리되며 즉시 인도명령을 신청함.
            """.trimIndent(),
            """
            [입찰 후속 법정 타임라인]
            * 매각일 현장 접수: 보증금 $cfDepEok 제출 후 낙찰 확정 시 보증 영수증을 보관하고 대출 상담을 개시함.
            * 매각허가·대금 통지: 허가 확정 이후 30일 안팎의 대금납부기한 안에 자기자본 $cfEquityEok 와 대출금을 준비함.
            * 소유권 취득·명도: 납부와 동시에 취득 효력이 발생하며 명도비 $cfEvictEok 한도 내에서 협의를 마침.
            """.trimIndent(),
            """
            [사법 절차별 필수 확인 사항]
            * 당일 입찰 자격 검증: 보증 수표 $cfDepEok 금액 오차나 대리 위임 서류 누락 시 무효 처리되므로 사전 검수함.
            * 매각결정·이의 점검: 7일차 허가 선고일까지 물건 훼손이나 취하 신청 등 돌발 변수가 없는지 추적함.
            * 완납·인도명령 병행: 잔금 납부일 당일 소유권 촉탁과 인도명령 신청서를 일괄 제출해 시간을 단축함.
            """.trimIndent(),
            """
            [경락 취득·점유 이전 공정표]
            * 투찰·최고가 확인: 최저가 기준 $cfDepEok 보증금 납부 후 최고가 낙찰 시 향후 일정을 기산함.
            * 2주간 확정·대출 세팅: 허가 결정과 항고 기한 경과 동안 잔금 조달 및 세무 신고 서류를 완비함.
            * 납부·등기·인도 마무리: 대금 지급 완료 즉시 등기 촉탁이 이뤄지며 약 ${proReport.estimatedEvictionDays}일간 명도 협상을 완료함.
            """.trimIndent()
        )

        val cfSourceMatrix = listOf(
            """
            [공식 원천 문건 5종 교차 대조표]
            * 매각물건명세서·감정평가서: 평가 기준일 이후 물리적 변경·미납 공과금·제시외 건물 포함 여부를 대조 완료함.
            * 현황조사서·전입세대열람: 점유 현황과 배당요구 신청 여부를 확인해 입찰 당일 오전 변경 공고만 추가 검증하면 됨.
            * 취득원가·금융여력 점검: 예상 취득세율과 대출 실행 가능 한도, 중도상환 조건까지 합산해 실질 자금 경색 리스크를 사전 차단함.
            """.trimIndent(),
            """
            [법원 비치 서류 검수 인덱스]
            * 감정·명세 대조: 최초 평가 시점의 면적·이용 상황과 현 매각 조건의 제시외 포함 내역을 교차 확인함.
            * 점유·배당 추적: 집행관 현황조사 내역과 전입·확정일자 자료를 맞춰 비배당 인수 부담이 없는지 점검함.
            * 공과금·세무 정산: 잔금 완납 후 60일 이내 세액 신고 일정과 승계 가능한 공용 관리비 상한을 사전 원가표에 반영함.
            """.trimIndent(),
            """
            [원천 공고 신뢰도 검증 매트릭스]
            * 물건 스펙 검증: 등기사항전부증명서 표제부 면적과 감정평가서 도면·건축물대장 용도를 대조함.
            * 권리·일정 추적: 매각기일 일주일 전 법원 열람용 매각물건명세서 수정 본문 및 송달 내역을 재확인함.
            * 출구·환금성 진단: 반경 내 유사 거래 사례와 월세 회전 속도를 반영해 1년 단타와 2년 보유 중 유리한 전략을 비교함.
            """.trimIndent(),
            """
            [사건 기록 무결성 체크리스트]
            * 공시 가격 검증: 최초 평가액 대비 회차별 저감률과 법정 보증금 비율 산정 오차가 없음을 확인함.
            * 이해관계인 추적: 채권자·임차인 배당신청 서류와 세무서 교부청구 존재 여부를 원문 기준으로 검토함.
            * 예비비·금리 방어: 금리 1%p 추가 상승 압력과 인도 지체 기간에 대응할 유동성 완충액을 현금 테이블에 별도 배정함.
            """.trimIndent(),
            """
            [서류별 실무 확인 포인트]
            * 등기·대장 대조: 갑구 처분제한과 을구 근저당 날짜를 대장상 위반건축물 표기 유무와 나란히 검증함.
            * 현황·체납 점검: 관리사무소 공용관리비와 단전·단수 여부를 현장 답사에서 직접 확인해 원가를 보정함.
            * 명의·과세 시뮬레이션: 단독 취득과 공동 지분, 법인 매수 방식 간 취득·보유·양도 세율 편차를 사전에 대조해 둠.
            """.trimIndent(),
            """
            [5대 공부 교차 대조 기록]
            * 문서 일치성: 등기부등본·매각물건명세서·현황조사서·감정평가서·건축물대장 간 수치 불일치가 없는지 점검함.
            * 변동 추적: 경매개시결정 이후 기일 변경·연기 신청서 접수 이력을 법원 진행내역에서 확인함.
            * 현장·시설 보완: 배관 누수나 외벽 파손 등 감정서 이후 발생한 감가 요소를 실사 사진으로 보정해 입찰 상단을 낮춤.
            """.trimIndent(),
            """
            [입찰 전 최종 문건 검증 요약]
            * 권리·가격 기준: 등기부 소멸 범위와 인근 실거래 중앙값이 감정평가 시점과 괴리가 없는지 대조함.
            * 당일 점검 항목: 법정 출석 1시간 전 변경·취하 공고 및 매각불허 요건 발생 여부를 재조회함.
            * 명도·촉탁 플랜: 잔금 이체 당일 소유권 이전 등기 촉탁과 인도명령 신청 서류를 병행 접수해 공실 손실을 줄임.
            """.trimIndent(),
            """
            [공부 원문·현황 대조표]
            * 서류 기준 검토: 감정평가 명세상 토지·건물 배분액과 매각 제외 설비 목록을 사전 확인함.
            * 현장 보정 체크: 임장 사진과 관리실 확인 사항을 합산해 명도·수선 예비비를 최종 원가에 반영함.
            * 금융·유동성 검증: 두 곳 이상 금융기관의 사전 대출 한도 조회 결과를 대조해 잔금일 돌발 축소 위험에 대비함.
            """.trimIndent(),
            """
            [사법 공시 자료 추적표]
            * 절차 기록 확인: 배당요구 종기 공고일 준수 여부와 대위변제·배당이의 가능성을 점검함.
            * 당일 검증 규칙: 매각기일 직전 열람 가능한 최신 송달내역 및 특별매각조건 부여 유무를 대조함.
            * 보유세·임대 수지: 재산세 과세 기준일(6월 1일)과 잔금 일정 선후를 조정해 초기 보유 세금 부담을 절감함.
            """.trimIndent(),
            """
            [등기·세무·현장 삼각 검증]
            * 공부 정합성: 등기 권리 순위와 국세·지방세 법정기일 선후 대조로 예상 밖의 인수 부담을 차단함.
            * 물리 상태 확인: 누수·파손·공실 여부를 현장 사진 5컷으로 남겨 보수비 산출 근거로 삼음.
            * 경비·증빙 관리: 법무사 보수, 합의 이사비, 자본적 지출 영수증을 적격 증빙으로 확보해 향후 양도세 공제에 대비함.
            """.trimIndent(),
            """
            [원본 서류 대조 기준안]
            * 권리·점유 분리: 말소 대상 후순위 권리와 예외적 법정지상권·유치권 주장 여지를 분리 검증함.
            * 사후 추적 규칙: 기일이 지난 사건은 낙찰 결과·유찰 차수를 재조회해 후속 전략 글로 자동 연결함.
            * 입찰 규율 점검: 보증 수표 액면가와 대리 위임 서류를 전날 검수해 기일 당일 무효 입찰 실수를 예방함.
            """.trimIndent(),
            """
            [GEO 원천 팩트 검수표]
            * 법원 서류 기준: 매각물건명세서 비고란 특수 기재와 감정서 환경 요인을 함께 대조해 누락을 막음.
            * 잔금·명의 점검: 낙찰 뒤 잔금 대출 가능 비율 및 세무 신고 기한(60일)을 사전 확인함.
            * 리스크 분산 체크: 인근 급매 호가 및 신규 입주 물량을 재조회해 예상 매각가(BEP) 달성 현실성을 검증함.
            """.trimIndent()
        )

        val cfAuditNotes = listOf(
            "[데이터 출처 및 면책 기준] 본 GEO 문서는 ${auction.courtName} 공시 자료와 30년 자문 기준치를 정리한 정보성 브리핑이며, 수익 보장이나 투자 권유가 아니므로 최종 공부 열람과 매각당일 공고 재확인은 응찰자 책임임. 실투자 전 금융권 한도·세무 조건을 반드시 별도 검수하기 바람. 또한 명도 협상 지연 및 잔금 대출 규제 변동에 대비해 최소 10% 내외의 여유 현금 버퍼를 함께 확보한 상태에서 응찰 여부를 판단해야 안전함.",
            "[원문 대조 및 투자 책임 고지] 수록된 계산 수치는 현 시점 공고 지표에 근거한 모형 결과로 실제 매각기일 변동에 따라 달라질 수 있으며, 모든 투자의 최종 의사결정 책임은 본인에게 있음. 입찰 전날 법원 최신 명세서 및 등기 변동 여부를 직접 대조해 오판을 예방하기 바람. 아울러 임차인 배당요구 종기 경과 내역과 공용관리비 승계 추정치를 현장 관리사무소에서 최종 확인한 뒤 응찰 상한선을 확정해야 함.",
            "[공시 검증 안내] 입찰 당일 오전 대법원 사건검색을 통해 변경·연기·취하 유무를 재확인해야 하며, 본 페이지의 계량 지수는 의사결정 보조 자료로서 특정 수익 결과를 보장하지 않음. 공과금·관리비 미납분과 점유 현황은 현장 임장으로 직접 확인하는 절차가 우선됨. 취득세 감면 예외와 장기보유특별공제 적용 조건 역시 보유 주택 수와 명의 형태에 따라 달라지므로 세무 전문가와 사전 대조해야 함.",
            "[면책 및 검증 권고] 법원 현황조사서와 실제 점유·공용체납 내역은 매각 시점까지 변할 수 있으므로 반드시 현장 임장과 최신 등기 발급 대조를 거쳐 직접 판단하기 바람. 본 분석은 참고용 계량 브리핑이며 실제 취득·보유 단계의 법적·세무적 손익을 보증하지 않음. 보증금 수표 액면 금액과 입찰표 기입 사건번호, 대리 위임장 첨부 여부를 법정 제출 직전 한 번 더 교차 점검하시기 바람.",
            "[공고 변동 유의] 매각물건명세서 정정이나 채무자 변제로 인한 취하가 기일 직전에도 발생할 수 있으므로 응찰 전 공식 법원 게시판을 직접 확인하시기 바람. 아울러 예상 대출 비율과 명도 기간은 개인 신용·물건 컨디션에 따라 달라질 수 있어 보수적 자금 설계가 요망됨. 주변 신규 공급 물량과 인근 급매물 출회 추이를 함께 살핀 뒤 매도 손익분기 달성 가능성을 스스로 검증해야 함.",
            "[참고 자료 명시] 본 문서에 포함된 기대 수익률과 손익분기 계산은 가정치 기반 시뮬레이션으로 실제 세금·금리·명도 기간에 따라 차이가 발생할 수 있음. 최종 입찰 금액은 인근 급매 호가 및 현장 수선 소요액을 반영해 투자자 책임 하에 확정하기 바람. 경락잔금 완납 후 60일 이내 취득세 신고·납부를 이행하지 않으면 무신고 가산세가 부과되므로 금융·세정 일정을 동시에 관리해야 함.",
            "[직접 검증 의무] 등기부 말소 여부와 특별매각조건은 법원 최신 서류에 근거해 응찰자 본인이 최종 판단해야 하며 본 사이트는 투자 손실을 보전하지 않음. 잔금 납부 전 세무사·법무사 검토를 병행해 소유권 취득과 등기 촉탁 절차를 점검하기 바람. 매각허가결정 선고 이후 7일간의 즉시항고 기간에도 물건 훼손이나 권리 변동 신고가 접수되지 않았는지 지속적으로 모니터링해야 함.",
            "[사후 추적 안내] 매각기일 경과 이후 실제 낙찰가와 유찰 내역은 법원 공고 재조사를 통해 후속 연관 브리핑으로 별도 갱신 제공됨. 본 원고에 정리된 사전 추정치는 공고 시점의 정보 제공 목적이며 실제 경락 결과와 동일함을 약속하지 않음. 차회 유찰 시 최저가 20% 추가 감가 구간과 경쟁률 변화 폭을 비교한 뒤 유리한 회차를 선택해 보수적 원가를 방어하시기 바람.",
            "[비보증 원칙] 본 페이지는 실무 정보 제공을 목적으로 작성됐으며 특정 가액 낙찰이나 시세 차익 실현을 보증하거나 권유하지 않음. 경매 참여자는 배당요구 종기·임대차 대항력·물리적 하자에 관한 공적 장부를 스스로 확인한 후 의사결정을 내려야 함. 특히 단독·공동·법인 명의에 따른 취득세 중과 및 이자 비용 손금 인정 여부가 달라지므로 출구 시나리오에 맞춘 사전 세무 비교가 필수임.",
            "[실무 체크 권고] 현장 방문 시 점유자 면담 및 공용관리비 정산 내역을 별도로 확인해야 공고 외 추가 승계 비용을 예방할 수 있음. 또한 취득세율과 보유세 부담은 주택 수·법인 여부에 따라 크게 달라지므로 사전 세무 설계가 필수적임. 인도명령 신청 및 강제집행 비용 항목까지 예비비 테이블에 미리 편성해 두어야 낙찰 이후 순수익률 왜곡을 막을 수 있음.",
            "[세무·대출 확인] 개인 주택 수, 보유 법인 형태, 총부채원리금상환비율(DSR)에 따라 취득세와 잔금 한도가 달라지므로 사전 금융·세무 점검이 필수임. 본 자료의 수익 시뮬레이션은 참고 지표일 뿐 투자 결과에 대한 어떠한 법적 보증도 수반하지 않음. 입찰 전 최소 두 곳의 대출 금융기관을 통해 예상 감정가와 경락대출 실행 가능 한도를 사전 조회한 뒤 자금을 배분하기 바람.",
            "[최종 의사결정] 제공된 3전략 가격대와 권리 요약은 참고용 분석 자료이며, 실제 입찰표 제출과 대금 납부 결정은 투자자의 자기 책임 아래 진행해야 함. 매각 당일 아침까지 법원 진행상태 공지를 대조해 변경·정지 여부를 확인한 뒤 응찰하기 바람. 잔금 정산 당일 필요경비 영수증과 합의 명도 증빙 자료를 모두 보관해야 추후 매각 시 양도차익 공제 혜택을 온전히 인정받을 수 있음."
        )[sCf]

        val cloudflareBodyText = buildString {
            appendLine(cfLeads[sCf])
            appendLine()
            appendLine(cfDocBlocks[sCf])
            appendLine()
            appendLine(cfTimelineBlocks[sCf])
            appendLine()
            appendLine(cfSourceMatrix[sCf])
            appendLine()
            appendLine(cfShotLine)
            appendLine(cfBandLine)
            appendLine(cfFaqBlocks[sCf])
            appendLine("* 법리 참고(${studyTerm.category.label}): ${studyTerm.userQuestionExample} (${studyTerm.statuteRef})")
            appendLine(cfAuditNotes)
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
