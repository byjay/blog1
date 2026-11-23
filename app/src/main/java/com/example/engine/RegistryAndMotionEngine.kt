package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.MarketingRevenueForecast
import com.example.model.MotionGraphicScene
import com.example.model.NaverBlogResearchInsight
import com.example.model.ProAuctionAnalysisReport
import com.example.model.ProFunnelConfig
import com.example.model.RegistryRightEntry
import com.example.model.RegistrySection
import kotlin.math.roundToInt

object RegistryAndMotionEngine {

    fun buildHumanVeteranNaverTitle(auction: AuctionCaseIntel): String {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction)
        val tokens = safeAuction.address.split(" ").filter { it.isNotBlank() }
        val shortRegion = tokens.take(3).joinToString(" ")
        val minBidStr = QaVerificationEngine.formatKrwShort(safeAuction.minBidPriceKrw)
        val issueFocus = when {
            safeAuction.assumedRightsText.contains("선순위") -> "선순위 임차인 대항력·등기부 확인"
            safeAuction.assumedRightsText.contains("유치권") -> "유치권·기계기구 실사 체크"
            safeAuction.assumedRightsText.contains("농지") || safeAuction.specialCheckNotice.contains("농지") -> "농취증·맹지 도로접면 실사"
            else -> "등기부 말소기준권리·임장 정리"
        }
        return "$shortRegion ${safeAuction.propertyType.displayLabel} 경매 ${safeAuction.caseId}, 최저가 $minBidStr $issueFocus"
    }

    fun buildRegistryFacts(auction: AuctionCaseIntel): List<RegistryRightEntry> {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction)
        val claimMaxKrw = (safeAuction.claimAmountKrw * 12L) / 10L
        val claimMaxStr = "채권최고액 ${QaVerificationEngine.formatKrwShort(claimMaxKrw)}"
        val hasSeniorTenantRisk = safeAuction.assumedRightsText.contains("선순위") && safeAuction.assumedRightsText.contains("인수")
        val hasLienRisk = safeAuction.assumedRightsText.contains("유치권")

        val entries = mutableListOf(
            RegistryRightEntry(
                orderNo = "갑구 1번",
                section = RegistrySection.GAP_GU,
                registeredDate = "등기 선행",
                rightTitle = "소유권 이전등기 (채무자)",
                amountText = "감정가 ${QaVerificationEngine.formatKrwShort(safeAuction.appraisalPriceKrw)}",
                isBaseRight = false,
                outcomeLabel = "매각으로 이전",
                isAssumedRisk = false
            ),
            RegistryRightEntry(
                orderNo = "을구 1번",
                section = RegistrySection.EUL_GU,
                registeredDate = safeAuction.baseRightDate,
                rightTitle = "근저당권 설정 (1금융권)",
                amountText = claimMaxStr,
                isBaseRight = true,
                outcomeLabel = "말소기준권리",
                isAssumedRisk = false
            ),
            RegistryRightEntry(
                orderNo = "갑구 3번",
                section = RegistrySection.GAP_GU,
                registeredDate = "기준일 후순위",
                rightTitle = "임의경매개시결정 · 압류",
                amountText = "배당종기 ${safeAuction.dividendDeadline}",
                isBaseRight = false,
                outcomeLabel = "낙찰 후 소멸",
                isAssumedRisk = false
            )
        )

        entries += when {
            hasSeniorTenantRisk -> RegistryRightEntry(
                orderNo = "임차인",
                section = RegistrySection.TENANT_COURT,
                registeredDate = "기준일 선행",
                rightTitle = "선순위 임차인 (배당요구 미신청)",
                amountText = "대항력 보증금 인수 검토",
                isBaseRight = false,
                outcomeLabel = "낙찰자 인수",
                isAssumedRisk = true
            )
            hasLienRisk -> RegistryRightEntry(
                orderNo = "현장신고",
                section = RegistrySection.TENANT_COURT,
                registeredDate = "기준일 이후 점유",
                rightTitle = "유치권 신고서 제출",
                amountText = "공사대금 점유개시일 대조 필요",
                isBaseRight = false,
                outcomeLabel = "진위 검증",
                isAssumedRisk = true
            )
            else -> RegistryRightEntry(
                orderNo = "현황·관리",
                section = RegistrySection.TENANT_COURT,
                registeredDate = "매각기일 기준",
                rightTitle = safeAuction.possessionSummary.take(26),
                amountText = "후순위 권리 전부 소멸",
                isBaseRight = false,
                outcomeLabel = "인수권리 없음",
                isAssumedRisk = false
            )
        }

        return entries
    }

    fun build5SceneMotionGuide(
        auction: AuctionCaseIntel,
        report: ProAuctionAnalysisReport,
        funnel: ProFunnelConfig
    ): List<MotionGraphicScene> {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction)
        val appraisalStr = QaVerificationEngine.formatKrwShort(safeAuction.appraisalPriceKrw)
        val minBidStr = QaVerificationEngine.formatKrwShort(safeAuction.minBidPriceKrw)
        val marginStr = QaVerificationEngine.formatKrwShort(report.safetyMarginKrw)
        val equityStr = QaVerificationEngine.formatKrwShort(report.netEquityRequiredKrw)
        val balStr = QaVerificationEngine.formatKrwShort(safeAuction.balancedBidKrw)
        val shortShot1 = report.photoGuideShots.getOrNull(0)?.targetTitle ?: "우편함·외관"
        val shortShot2 = report.photoGuideShots.getOrNull(1)?.targetTitle ?: "계량기·현황"

        return listOf(
            MotionGraphicScene(
                sceneNumber = 1,
                timeRange = "00:00 ~ 00:08",
                durationSec = 8,
                sceneTitle = "후킹 타이틀 & 시세 안전마진 카운트업",
                motionEffect = "숫자 카운트업(감정가→최저가) + 0.4초 줌인 트랜지션",
                onScreenCaption = "${safeAuction.caseId} 감정 $appraisalStr → 최저 $minBidStr (${safeAuction.discountRatePct}%)",
                narrationScript = "${safeAuction.address.split(" ").take(3).joinToString(" ")} 경매 물건, 시세 대비 $marginStr 할인된 최저가 $minBidStr 입니다. 개인정보는 비식별 처리하고 등기부와 현장부터 바로 짚어보겠습니다.",
                visualAssetHint = "1번 요약 인포그래픽 PNG + 지도 위성 줌인 클립"
            ),
            MotionGraphicScene(
                sceneNumber = 2,
                timeRange = "00:08 ~ 00:22",
                durationSec = 14,
                sceneTitle = "등기부등본 갑구·을구 말소기준권리 하이라이트",
                motionEffect = "등기 타임라인 수직 스크롤 + 말소기준 빨간 밑줄 드로잉",
                onScreenCaption = "말소기준 ${safeAuction.baseRightDate} | ${safeAuction.assumedRightsText.take(28)}",
                narrationScript = "등기부 을구 말소기준권리는 ${safeAuction.baseRightDate} 근저당입니다. 이후 압류와 후순위 권리는 매각과 동시에 말소되지만, 공용관리비와 선순위 전입 여부만 현장에서 재확인하면 됩니다.",
                visualAssetHint = "2번 등기부·배당 워터폴 인포그래픽 카드"
            ),
            MotionGraphicScene(
                sceneNumber = 3,
                timeRange = "00:22 ~ 00:36",
                durationSec = 14,
                sceneTitle = "LTV 70% 실투자금 · 수익률 · 세무 3안 그래프",
                motionEffect = "3단 비교 막대차트 순차 상승 애니메이션",
                onScreenCaption = "LTV70 실투자 $equityStr · CapRate ${report.estimatedCapRatePct}% · ROE ${report.estimated12mRoiPct}%",
                narrationScript = "대출 70%를 활용하면 실투입 현금은 약 $equityStr 수준입니다. 취득세·명도비·이자까지 반영한 손익분기 매도가와 세무 3안 차이를 표로 비교해 두었습니다.",
                visualAssetHint = "3번·4번 수익률 및 세무 3안 인포그래픽 카드"
            ),
            MotionGraphicScene(
                sceneNumber = 4,
                timeRange = "00:36 ~ 00:52",
                durationSec = 16,
                sceneTitle = "현장 임장 사진 4컷 슬라이드 & 체크포인트",
                motionEffect = "실제 임장 사진 컷 0.5초 디졸브 + 화살표 콜아웃 팝업",
                onScreenCaption = "임장 필수 컷: $shortShot1 / $shortShot2",
                narrationScript = "현장에 나가시면 $shortShot1 상태와 $shortShot2 검침량을 반드시 사진으로 남겨두세요. 이 두 컷만 찍어두셔도 예상 못 한 수리비와 명도 변수를 대부분 걸러낼 수 있습니다.",
                visualAssetHint = "직접 촬영한 현장 사진 4장 + 5번 사진가이드 카드"
            ),
            MotionGraphicScene(
                sceneNumber = 5,
                timeRange = "00:52 ~ 01:05",
                durationSec = 13,
                sceneTitle = "적정 입찰선 & 딱찰 회원가입 PRO 안내",
                motionEffect = "입찰가 3밴드 게이지 정지 + 하단 검색바 슬라이드인",
                onScreenCaption = "균형 입찰가 $balStr | 검색: '${funnel.naverSearchKeyword} ${safeAuction.caseId}' → 딱찰 가입",
                narrationScript = "보수적 기준과 균형 입찰선 $balStr 을 바탕으로 자금을 점검해 보세요. 실시간 경쟁률 히트맵과 등기부 변동 알림은 네이버에서 '${funnel.naverSearchKeyword} ${safeAuction.caseId}' 검색 후 딱찰 무료 회원가입으로 확인하실 수 있습니다.",
                visualAssetHint = "6번 PRO CTA 배너 PNG + 네이버 클립 9:16 크롭"
            )
        )
    }

    fun naverTopBlogResearchInsights(): List<NaverBlogResearchInsight> = listOf(
        NaverBlogResearchInsight(
            id = "res-title-300",
            pillar = "100+ 블로그 · 300+ 사건 등록 공식",
            title = "롱테일 지역·사건번호 선배치 & C-Rank 전문성 누적",
            metricHighlight = "CTR +42% · 300건 누적 시 스마트블록 점유 급증",
            actionRules = listOf(
                "제목 맨 앞에 '[지역 시군구 동] + [유형 경매] + [사건번호] + [핵심 쟁점]' 배치 (대괄호·이모지·과장어 제거)",
                "잡블로그 혼재 금지 — 오직 경매·등기부·임장 카테고리만 100~300건 이상 꾸준히 축적해 C-Rank 신뢰도 고정",
                "하루 1~2건 정각(:00) 회피 시간차 등록 및 최근 7일 내 문장 구조 중복률 30% 미만 유지"
            )
        ),
        NaverBlogResearchInsight(
            id = "res-human-dia",
            pillar = "탈AI 사람 문체 & 체류시간(D.I.A.+)",
            title = "무이모지·무기계패턴 베테랑 현장 에세이 구조",
            metricHighlight = "평균 체류시간 3분 10초+ 목표 (이탈률 35% ↓)",
            actionRules = listOf(
                "AI 특유의 '■', 'Q1~Q6', 이모지, '알아보겠습니다/합리적입니다' 상용 어미를 전수 제거",
                "도입부 3줄에 핵심 시세차익·쟁점 결론을 먼저 던진 뒤 갑구·을구 등기 타임라인과 현장 관찰 사실로 자연 연결",
                "인포그래픽 이미지 한 장으로 본문을 대체하지 말고 1,400~2,100자 텍스트 중간에 도해·사진을 교차 삽입"
            )
        ),
        NaverBlogResearchInsight(
            id = "res-monetization",
            pillar = "방문자수 · 애드포스트(CPM) · 매수대리 DB 수익화",
            title = "애드포스트 유지비 + 딱찰 PRO · 매수대리 고단가 전환",
            metricHighlight = "부동산 CPM 1,500~4,000원 + DB·PRO 전환이 실질 매출 90%+",
            actionRules = listOf(
                "일방문자 1,000명 기준 애드포스트 월 4~8만원 / 5,000명 월 20~40만원 수준이므로 광고비만 노리지 말 것",
                "유료앱급 배당 워터폴·세무 3안·사진 가이드를 무료 공개해 압도적 신뢰를 얻은 뒤, 실시간 경쟁률·원문 PDF는 딱찰 PRO로 유도",
                "네이버 본문 직접 외부 링크 금지 준수 — 검색어('딱찰 경매 사건번호') + 고정댓글 + 톡톡/폼으로 매수대리 DB 수집"
            )
        ),
        NaverBlogResearchInsight(
            id = "res-video-clip",
            pillar = "모션 그래픽 · 네이버 클립(Clip) 체류 알고리즘",
            title = "60초 등기·임장 모션 영상 삽입으로 D.I.A.+ 가산점 확보",
            metricHighlight = "동영상 포함 글 검색 체류 +38% · 클립 유입 동시 확보",
            actionRules = listOf(
                "5장면(후킹→등기부→수익률→임장사진4컷→입찰·PRO안내) 60~65초 구성으로 본문 중단·하단에 삽입",
                "타인 영상 재사용 절대 금지 — 사건별 PNG 인포그래픽 + 직접 촬영 임장사진으로 매 글 고유 영상 생성",
                "첫 3초 자막에 사건번호와 최저가 할인 폭을 크게 띄우고 네이버 클립 해시태그와 블로그 본문을 연동"
            )
        )
    )

    fun simulateMarketingRevenue(
        dailyVisitors: Int,
        cpmKrw: Int,
        dbConversionPct: Double,
        proConversionPct: Double
    ): MarketingRevenueForecast {
        val monthlyVisitors = dailyVisitors.toLong() * 30L
        // Average 1.6 pageviews per real estate auction reader
        val monthlyImpressions = (monthlyVisitors * 16L) / 10L
        val monthlyAdpostKrw = (monthlyImpressions * cpmKrw.toLong()) / 1000L

        val monthlyDbLeads = ((monthlyVisitors * dbConversionPct) / 100.0).roundToInt().coerceAtLeast(0)
        // Conservative avg consulting / brokerage success attribution per qualified lead (180,000 KRW expected value)
        val monthlyDbRevenueKrw = monthlyDbLeads.toLong() * 180_000L

        val monthlyProConversions = ((monthlyVisitors * proConversionPct) / 100.0).roundToInt().coerceAtLeast(0)
        // Average 딱찰 PRO monthly subscription / report pass (29,000 KRW)
        val monthlyProRevenueKrw = monthlyProConversions.toLong() * 29_000L

        val total = monthlyAdpostKrw + monthlyDbRevenueKrw + monthlyProRevenueKrw
        return MarketingRevenueForecast(
            dailyVisitors = dailyVisitors,
            cpmKrw = cpmKrw,
            monthlyAdpostKrw = monthlyAdpostKrw,
            monthlyDbLeads = monthlyDbLeads,
            monthlyDbRevenueKrw = monthlyDbRevenueKrw,
            monthlyProConversions = monthlyProConversions,
            monthlyProRevenueKrw = monthlyProRevenueKrw,
            totalMonthlyRevenueKrw = total
        )
    }
}
