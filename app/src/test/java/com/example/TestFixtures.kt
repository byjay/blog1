package com.example

import com.example.engine.DefaultSpecDataFactory
import com.example.engine.QaVerificationEngine
import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.CourtDocKind
import com.example.model.CourtDocumentRecord
import com.example.model.FieldVisitRecord
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.RegistryNodeTone
import com.example.model.RegistrySection
import com.example.model.RegistryVisualNode
import com.example.model.ServerAutomationConfig
import com.example.model.VisitRecordSource

/**
 * 단위/Robolectric 테스트 전용 픽스처 모음.
 * 프로덕션(src/main)에는 가짜·샘플 사건을 두지 않고, 테스트 실행 시에만 엔진·UI 검증용으로 사용한다.
 */
object TestFixtures {

    fun sampleAuctions(): List<AuctionCaseIntel> = listOf(
        AuctionCaseIntel(
            dedupKey = "test-apt-001",
            caseId = "2025타경6297",
            courtName = "부산지방법원 동부지원",
            address = "부산광역시 기장군 정관읍 모전리 정관롯데캐슬2차 105동 1201호",
            propertyType = PropertyType.APT_OFFICETEL,
            appraisalPriceKrw = 420_000_000L,
            minBidPriceKrw = 268_800_000L,
            recentRealTradeMedianKrw = 395_000_000L,
            stats3yRatePct = 84.6,
            bidCount = 2,
            discountRatePct = 64,
            depositRatePct = 10,
            clicksLast7d = 128,
            auctionDate = "2026-12-18",
            baseRightDate = "2020-05-14",
            dividendDeadline = "2026-03-10",
            claimAmountKrw = 240_000_000L,
            possessionSummary = "채무자 겸 소유자 세대 점유(임차인 없음)",
            tenantSummary = "전입세대열람 결과 소유자 세대만 등재(보증금 인수 없음)",
            assumedRightsText = "말소기준권리 이후 후순위 근저당·압류 전부 소멸",
            conservativeBidKrw = 281_000_000L,
            balancedBidKrw = 304_000_000L,
            aggressiveBidKrw = 322_000_000L,
            specialCheckNotice = "공용관리비 장기체납 36개월 중 공용부분 승계 범위(대법원 2001다8677) 현장 확인"
        ),
        AuctionCaseIntel(
            dedupKey = "test-villa-002",
            caseId = "2025타경11842",
            courtName = "서울남부지방법원 경매3계",
            address = "서울특별시 강서구 화곡동 392-14 다세대주택 301호",
            propertyType = PropertyType.VILLA_MULTIFAMILY,
            appraisalPriceKrw = 310_000_000L,
            minBidPriceKrw = 198_400_000L,
            recentRealTradeMedianKrw = 292_000_000L,
            stats3yRatePct = 79.2,
            bidCount = 2,
            discountRatePct = 64,
            depositRatePct = 10,
            clicksLast7d = 94,
            auctionDate = "2026-12-22",
            baseRightDate = "2021-08-20",
            dividendDeadline = "2026-04-02",
            claimAmountKrw = 165_000_000L,
            possessionSummary = "선순위 임차인 점유 상태 (배당요구 미신청)",
            tenantSummary = "말소기준권리 이전 선순위 임차인 보증금 8,500만원 배당요구 미신청",
            assumedRightsText = "선순위 임차인 보증금 8,500만원 낙찰자 인수 필수 (전입일 말소기준일 선행)",
            conservativeBidKrw = 202_000_000L,
            balancedBidKrw = 214_000_000L,
            aggressiveBidKrw = 228_000_000L,
            specialCheckNotice = "선순위 임차보증금 8,500만원 인수 및 누수 여부 현장 점검"
        ),
        AuctionCaseIntel(
            dedupKey = "test-retail-003",
            caseId = "2025타경4410",
            courtName = "수원지방법원 성남지원",
            address = "경기도 성남시 분당구 정자동 178-4 근린상가 1층 108호",
            propertyType = PropertyType.COMMERCIAL_RETAIL,
            appraisalPriceKrw = 680_000_000L,
            minBidPriceKrw = 476_000_000L,
            recentRealTradeMedianKrw = 640_000_000L,
            stats3yRatePct = 81.5,
            bidCount = 1,
            discountRatePct = 70,
            depositRatePct = 10,
            clicksLast7d = 76,
            auctionDate = "2026-12-24",
            baseRightDate = "2020-11-03",
            dividendDeadline = "2026-04-15",
            claimAmountKrw = 390_000_000L,
            possessionSummary = "현 임차인 영업 중 (상가임대차 확인)",
            tenantSummary = "사업자등록 후순위 임차인 확인, 부가세 포괄양수도 검토",
            assumedRightsText = "말소기준권리 이후 후순위 근저당·압류 전부 소멸",
            conservativeBidKrw = 488_000_000L,
            balancedBidKrw = 516_000_000L,
            aggressiveBidKrw = 542_000_000L,
            specialCheckNotice = "상가 관리단 체납관리비 공용부분 및 부가세 별도 여부 확인"
        ),
        AuctionCaseIntel(
            dedupKey = "test-land-004",
            caseId = "2025타경7732",
            courtName = "춘천지방법원 원주지원",
            address = "강원특별자치도 원주시 지정면 가곡리 412-8 전(계획관리지역)",
            propertyType = PropertyType.LAND_FARMLAND,
            appraisalPriceKrw = 148_000_000L,
            minBidPriceKrw = 94_720_000L,
            recentRealTradeMedianKrw = 152_000_000L,
            stats3yRatePct = 76.8,
            bidCount = 2,
            discountRatePct = 64,
            depositRatePct = 10,
            clicksLast7d = 61,
            auctionDate = "2026-12-29",
            baseRightDate = "2022-02-11",
            dividendDeadline = "2026-04-28",
            claimAmountKrw = 85_000_000L,
            possessionSummary = "나대지 및 일부 경작 현황 (제시외 수목 확인)",
            tenantSummary = "임대차 신고 내역 없음 · 농지취득자격증명 발급 필요",
            assumedRightsText = "말소기준권리 이후 후순위 압류 전부 소멸, 법정지상권 미성립",
            conservativeBidKrw = 97_500_000L,
            balancedBidKrw = 106_000_000L,
            aggressiveBidKrw = 115_000_000L,
            specialCheckNotice = "4m 현황도로 접면 여부 및 농취증 발급 가능성 확인"
        ),
        AuctionCaseIntel(
            dedupKey = "test-factory-005",
            caseId = "2025타경9120",
            courtName = "창원지방법원 마산지원",
            address = "경상남도 창원시 마산회원구 봉암동 공단로 74 공장 일괄",
            propertyType = PropertyType.FACTORY_SPECIAL,
            appraisalPriceKrw = 1_450_000_000L,
            minBidPriceKrw = 928_000_000L,
            recentRealTradeMedianKrw = 1_380_000_000L,
            stats3yRatePct = 74.2,
            bidCount = 2,
            discountRatePct = 64,
            depositRatePct = 10,
            clicksLast7d = 53,
            auctionDate = "2026-12-30",
            baseRightDate = "2019-09-17",
            dividendDeadline = "2026-05-05",
            claimAmountKrw = 980_000_000L,
            possessionSummary = "채무자 법인 자가 가동 중단 상태",
            tenantSummary = "임차인 없음 · 경매개시결정 기입등기 이후 시공업체 유치권 신고 접수",
            assumedRightsText = "압류 이후(경매개시결정 등기 이후) 점유 취득한 유치권 신고로 매수인 대항 불가",
            conservativeBidKrw = 945_000_000L,
            balancedBidKrw = 995_000_000L,
            aggressiveBidKrw = 1_055_000_000L,
            specialCheckNotice = "공장저당법 제6조 기계기구 목록 일치 및 압류 후 유치권 배제 입증"
        )
    )

    fun sampleBusanLands(): List<AuctionCaseIntel> = listOf(
        AuctionCaseIntel(
            dedupKey = "busan-land-2025-101915",
            caseId = "2025타경101915",
            courtName = "부산지방법원 동부지원",
            address = "부산광역시 기장군 일광읍 화전리 214-7 전 (개발제한·도시자연 인접)",
            propertyType = PropertyType.LAND_FARMLAND,
            appraisalPriceKrw = 64_000_000L,
            minBidPriceKrw = 32_768_000L,
            recentRealTradeMedianKrw = 58_000_000L,
            stats3yRatePct = 78.4,
            bidCount = 3,
            discountRatePct = 51,
            depositRatePct = 10,
            clicksLast7d = 112,
            auctionDate = "2026-12-21",
            baseRightDate = "2021-06-15",
            dividendDeadline = "2026-04-10",
            claimAmountKrw = 42_000_000L,
            possessionSummary = "공실 나대지 상태 · 경계 펜스 일부 설치",
            tenantSummary = "전입·임차 내역 없음",
            assumedRightsText = "말소기준권리 이후 후순위 가압류 전부 소멸",
            conservativeBidKrw = 34_200_000L,
            balancedBidKrw = 37_500_000L,
            aggressiveBidKrw = 41_000_000L,
            specialCheckNotice = "부산 5천만원 미만 소액 토지 · 현황도로 폭원 및 농취증 발급 확인"
        ),
        AuctionCaseIntel(
            dedupKey = "busan-land-2025-20368",
            caseId = "2025타경20368",
            courtName = "부산지방법원 서부지원",
            address = "부산광역시 강서구 대저1동 1820-4 답 (농지)",
            propertyType = PropertyType.LAND_FARMLAND,
            appraisalPriceKrw = 72_000_000L,
            minBidPriceKrw = 46_080_000L,
            recentRealTradeMedianKrw = 69_000_000L,
            stats3yRatePct = 81.2,
            bidCount = 2,
            discountRatePct = 64,
            depositRatePct = 10,
            clicksLast7d = 88,
            auctionDate = "2026-12-23",
            baseRightDate = "2020-12-08",
            dividendDeadline = "2026-04-12",
            claimAmountKrw = 50_000_000L,
            possessionSummary = "인근 경작인 단순 경작 중 (농지법 점검 대상)",
            tenantSummary = "대항력 있는 임차인 없음",
            assumedRightsText = "말소기준권리 이후 압류 일괄 소멸",
            conservativeBidKrw = 47_500_000L,
            balancedBidKrw = 51_200_000L,
            aggressiveBidKrw = 54_800_000L,
            specialCheckNotice = "부산 강서구 소액 농지 · 농지취득자격증명 7일 내 발급 필요"
        )
    )

    fun sampleGapGu(caseId: String, courtName: String): List<RegistryVisualNode> = listOf(
        RegistryVisualNode(
            nodeId = "$caseId-gap-1",
            section = RegistrySection.GAP_GU,
            rankOrder = "갑구 1번",
            registeredDate = "2019-03-15",
            title = "소유권 이전등기",
            holderOrCreditor = "현 소유자",
            amountOrShare = "지분 1/1",
            statusBadge = "매각으로 소유권 이전",
            plainExplanation = "매각 대금 완납 시 낙찰자 명의로 소유권이 이전됩니다.",
            nodeTone = RegistryNodeTone.NEUTRAL_OWNERSHIP,
            isBaseRight = false,
            isAssumedRisk = false
        ),
        RegistryVisualNode(
            nodeId = "$caseId-gap-2",
            section = RegistrySection.GAP_GU,
            rankOrder = "갑구 2번",
            registeredDate = "2023-07-20",
            title = "지자체 압류",
            holderOrCreditor = "지방세 과세관청",
            amountOrShare = "당해세 우선 여부 대조",
            statusBadge = "배당 후 말소",
            plainExplanation = "기준권리 후순위 압류는 배당 후 말소됩니다.",
            nodeTone = RegistryNodeTone.EXTINGUISHED_SAFE,
            isBaseRight = false,
            isAssumedRisk = false
        ),
        RegistryVisualNode(
            nodeId = "$caseId-gap-3",
            section = RegistrySection.GAP_GU,
            rankOrder = "갑구 3번",
            registeredDate = "2025-02-10",
            title = "임의경매개시결정 등기",
            holderOrCreditor = "$courtName 경매계",
            amountOrShare = "청구채권 집행",
            statusBadge = "낙찰 시 소멸",
            plainExplanation = "매각 허가 및 대금 완납 시 법원이 직권 말소합니다.",
            nodeTone = RegistryNodeTone.EXTINGUISHED_SAFE,
            isBaseRight = false,
            isAssumedRisk = false
        )
    )

    fun sampleEulGu(caseId: String, baseRightDate: String): List<RegistryVisualNode> = listOf(
        RegistryVisualNode(
            nodeId = "$caseId-eul-1",
            section = RegistrySection.EUL_GU,
            rankOrder = "을구 1번",
            registeredDate = baseRightDate,
            title = "1순위 근저당권 설정",
            holderOrCreditor = "1금융권 시중은행",
            amountOrShare = "채권최고액 2억 4,000만원",
            statusBadge = "말소기준권리",
            plainExplanation = "말소기준권리로서 이후 설정된 담보권과 임차권은 전부 소멸합니다.",
            nodeTone = RegistryNodeTone.BASE_RIGHT_ANCHOR,
            isBaseRight = true,
            isAssumedRisk = false
        )
    )

    fun sampleDocuments(caseId: String, courtName: String): List<CourtDocumentRecord> = listOf(
        CourtDocumentRecord(
            docKind = CourtDocKind.SALE_SPECIFICATION,
            referenceNo = "$caseId-매각명세",
            issuedDate = "2026-11-18",
            issuer = courtName,
            summaryHighlight = "매각물건명세서상 인수 권리 없음 확인",
            fullRecordLines = listOf("최선순위 설정일 확인 완료", "매각조건 특이사항 비고 확인"),
            riskFlagNote = "현황조사서와 입찰 전 재대조 권장"
        ),
        CourtDocumentRecord(
            docKind = CourtDocKind.STATUS_SURVEY,
            referenceNo = "$caseId-현황조사",
            issuedDate = "2026-11-12",
            issuer = "$courtName 집행관실",
            summaryHighlight = "집행관 1차·2차 방문 및 점유조사 결과 수록",
            fullRecordLines = listOf("현장 방문 시 폐문 부재로 안내문 부착", "전입세대열람 확인"),
            riskFlagNote = "관리비 체납 내역 별도 확인 필요"
        ),
        CourtDocumentRecord(
            docKind = CourtDocKind.APPRAISAL_REPORT,
            referenceNo = "$caseId-감정평가",
            issuedDate = "2026-10-25",
            issuer = "법원 지정 감정평가법인",
            summaryHighlight = "감정평가 기준가 및 제시외 물건 유무 검토 완료",
            fullRecordLines = listOf("거래사례비교법 적용 산정", "현황도로 및 주변 시세 반영"),
            riskFlagNote = "최근 실거래가와 괴리율 확인"
        )
    )

    fun sampleVisits(caseId: String): List<FieldVisitRecord> = listOf(
        FieldVisitRecord(
            visitId = "$caseId-visit-1",
            visitRound = "집행관 1차 현황조사",
            visitedAt = "2026-11-08",
            source = VisitRecordSource.BAILIFF_SURVEY,
            visitorLabel = "법원 집행관",
            occupancyStatus = "소유자 점유 진술 확인",
            findingsNote = "현장 방문 결과 임대차 관계 없음, 우편함 정상 수령 확인"
        ),
        FieldVisitRecord(
            visitId = "$caseId-visit-2",
            visitRound = "관리실 탐문 확인",
            visitedAt = "2026-11-20",
            source = VisitRecordSource.MANAGEMENT_OFFICE,
            visitorLabel = "임장 점검",
            occupancyStatus = "공용관리비 납부 양호",
            findingsNote = "체납 관리비 및 누수 민원 이력 없음 확인"
        )
    )

    fun sampleSlots(
        config: ServerAutomationConfig = ServerAutomationConfig(),
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        withCourtEvidence: Boolean = true
    ): List<GeneratedPostSlot> {
        return sampleAuctions().mapIndexed { idx, auction ->
            val gap = if (withCourtEvidence) sampleGapGu(auction.caseId, auction.courtName) else emptyList()
            val eul = if (withCourtEvidence) sampleEulGu(auction.caseId, auction.baseRightDate) else emptyList()
            val docs = if (withCourtEvidence) sampleDocuments(auction.caseId, auction.courtName) else emptyList()
            val visits = if (withCourtEvidence) sampleVisits(auction.caseId) else emptyList()
            val photos = if (withCourtEvidence) listOf("https://r2.ddakchal.pages.dev/photos/${auction.dedupKey}-1.webp") else emptyList()
            val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
                auction = auction,
                seedIndex = idx,
                config = config,
                proFunnel = proFunnel,
                serverGapGu = gap,
                serverEulGu = eul,
                serverDocuments = docs,
                visitRecords = visits
            )
            val min = 11 + idx * 7
            val scheduled = mapOf(
                ChannelPlatform.CLOUDFLARE to "2026-11-23 09:${min}:13",
                ChannelPlatform.TISTORY to "2026-11-23 10:${min + 4}:27",
                ChannelPlatform.NAVER to "2026-11-23 11:${min + 9}:41"
            )
            val qa = QaVerificationEngine.auditSlot(auction, variants, scheduled, config)
            GeneratedPostSlot(
                postId = "post-test-${idx + 1}",
                queueId = "q-test-${idx + 1}",
                slotIndex = idx + 1,
                publishDate = "2026-11-23",
                auctionCase = auction,
                variants = variants,
                scheduledTimes = scheduled,
                status = PostStatus.QUEUED,
                qaReport = qa,
                sourceLabel = "테스트 픽스처",
                courtPhotoUrls = photos,
                visitRecords = visits,
                serverRegistryGapGu = gap,
                serverRegistryEulGu = eul,
                serverCourtDocuments = docs
            )
        }
    }
}
