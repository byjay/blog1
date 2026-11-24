package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.BenchmarkTierRow
import com.example.model.ChannelPlatform
import com.example.model.DividendWaterfallStep
import com.example.model.FieldPhotoGuideShot
import com.example.model.InfographicCardSpec
import com.example.model.ProAuctionAnalysisReport
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.ServerAutomationConfig
import com.example.model.TaxScenarioBreakdown
import kotlin.math.roundToInt

object ProAnalysisAndPhotoGuideEngine {

    fun buildProReport(auction: AuctionCaseIntel): ProAuctionAnalysisReport {
        val safetyMarginKrw = (auction.recentRealTradeMedianKrw - auction.minBidPriceKrw).coerceAtLeast(0L)
        val safetyMarginPct = if (auction.recentRealTradeMedianKrw > 0L) {
            ((safetyMarginKrw.toDouble() / auction.recentRealTradeMedianKrw.toDouble()) * 1000.0).roundToInt() / 10.0
        } else 0.0

        val hasSeniorTenantRisk = auction.assumedRightsText.contains("선순위") && auction.assumedRightsText.contains("인수")
        val assumedExtraCostKrw = when {
            hasSeniorTenantRisk -> (auction.minBidPriceKrw * 0.45).toLong().coerceAtLeast(50_000_000L)
            auction.propertyType == PropertyType.COMMERCIAL_RETAIL -> 6_800_000L
            auction.propertyType == PropertyType.APT_OFFICETEL -> 2_200_000L
            auction.propertyType == PropertyType.FACTORY_SPECIAL -> 12_000_000L
            else -> 4_500_000L
        }

        val effectiveCostKrw = auction.balancedBidKrw + assumedExtraCostKrw + ((auction.balancedBidKrw * 0.035).toLong())
        val ltv70LoanKrw = (auction.balancedBidKrw * 70L) / 100L
        val netEquityRequiredKrw = (effectiveCostKrw - ltv70LoanKrw).coerceAtLeast(auction.minBidPriceKrw * 20L / 100L)

        val annualRentYieldBase = when (auction.propertyType) {
            PropertyType.COMMERCIAL_RETAIL -> 0.062
            PropertyType.FACTORY_SPECIAL -> 0.068
            PropertyType.APT_OFFICETEL -> 0.044
            PropertyType.VILLA_MULTIFAMILY -> 0.053
            PropertyType.LAND_FARMLAND -> 0.028
        }
        val estimatedMonthlyRentKrw = ((auction.recentRealTradeMedianKrw * annualRentYieldBase) / 12.0).toLong()
        val estimatedCapRatePct = if (effectiveCostKrw > 0L) {
            (((estimatedMonthlyRentKrw * 12.0) / effectiveCostKrw.toDouble()) * 1000.0).roundToInt() / 10.0
        } else 0.0

        val netResaleGain = auction.recentRealTradeMedianKrw * 0.96 - effectiveCostKrw
        val estimated12mRoiPct = if (netEquityRequiredKrw > 0L) {
            ((netResaleGain / netEquityRequiredKrw.toDouble()) * 1000.0).roundToInt() / 10.0
        } else 0.0

        val evictionRiskLevel = when {
            hasSeniorTenantRisk -> "주의 (선순위 보증금 인수 확인)"
            auction.assumedRightsText.contains("유치권") -> "주의 (유치권 성립 여부 검증)"
            auction.possessionSummary.contains("공실") -> "낮음 (공실 · 즉시 명도 가능)"
            else -> "보통 (인도명령 + 협의 명도)"
        }
        val estimatedEvictionDays = when {
            auction.possessionSummary.contains("공실") -> 14
            hasSeniorTenantRisk || auction.assumedRightsText.contains("유치권") -> 120
            else -> 65
        }
        val estimatedEvictionReserveKrw = when {
            auction.possessionSummary.contains("공실") -> 1_000_000L
            auction.propertyType == PropertyType.FACTORY_SPECIAL -> 8_000_000L
            else -> 3_500_000L
        }

        // 4-Tier Dividend Waterfall simulation based on balanced bid
        var remainingPool = auction.balancedBidKrw
        val step0Claim = 3_200_000L
        val step0Dist = minOf(remainingPool, step0Claim)
        remainingPool = (remainingPool - step0Dist).coerceAtLeast(0L)

        val step1Claim = if (hasSeniorTenantRisk) 180_000_000L else (auction.appraisalPriceKrw * 0.04).toLong().coerceAtMost(45_000_000L)
        val step1Absorbed = if (hasSeniorTenantRisk) step1Claim else 0L
        val step1Dist = if (hasSeniorTenantRisk) 0L else minOf(remainingPool, step1Claim)
        remainingPool = (remainingPool - step1Dist).coerceAtLeast(0L)

        val step2Claim = 4_800_000L
        val step2Dist = minOf(remainingPool, step2Claim)
        remainingPool = (remainingPool - step2Dist).coerceAtLeast(0L)

        val step3Claim = auction.claimAmountKrw
        val step3Dist = minOf(remainingPool, step3Claim)
        val step3Shortfall = (step3Claim - step3Dist).coerceAtLeast(0L)

        val waterfall = listOf(
            DividendWaterfallStep(
                priorityRank = 0,
                creditorLabel = "0순위 경매 집행비용 · 필요비",
                claimAmountKrw = step0Claim,
                distributedKrw = step0Dist,
                absorbedByBuyerKrw = 0L,
                statusTag = "전액배당"
            ),
            DividendWaterfallStep(
                priorityRank = 1,
                creditorLabel = if (hasSeniorTenantRisk) "1순위 선순위 임차인 (미배당 대항력)" else "1순위 소액임차 최우선변제 · 공용관리비",
                claimAmountKrw = step1Claim,
                distributedKrw = step1Dist,
                absorbedByBuyerKrw = step1Absorbed,
                statusTag = if (hasSeniorTenantRisk) "낙찰자인수" else "전액배당"
            ),
            DividendWaterfallStep(
                priorityRank = 2,
                creditorLabel = "2순위 당해세 (재산세 · 종부세)",
                claimAmountKrw = step2Claim,
                distributedKrw = step2Dist,
                absorbedByBuyerKrw = 0L,
                statusTag = "전액배당"
            ),
            DividendWaterfallStep(
                priorityRank = 3,
                creditorLabel = "3순위 말소기준 근저당 (${auction.baseRightDate})",
                claimAmountKrw = step3Claim,
                distributedKrw = step3Dist,
                absorbedByBuyerKrw = 0L,
                statusTag = if (step3Shortfall == 0L) "완전소멸" else "부분배당후소멸"
            )
        )

        val taxScenarios = buildTaxScenarios(auction.propertyType)
        val photoShots = buildFieldPhotoGuide(auction.propertyType, auction)
        val nextDropMinBidKrw = (auction.minBidPriceKrw * 80L) / 100L
        val breakevenResalePriceKrw = effectiveCostKrw + estimatedEvictionReserveKrw + ((ltv70LoanKrw * 45L) / 1000L)
        val rateStressMonthlyDeltaKrw = (ltv70LoanKrw / 100L) / 12L

        return ProAuctionAnalysisReport(
            caseId = auction.caseId,
            safetyMarginKrw = safetyMarginKrw,
            safetyMarginPct = safetyMarginPct,
            assumedExtraCostKrw = assumedExtraCostKrw,
            effectiveCostKrw = effectiveCostKrw,
            ltv70LoanKrw = ltv70LoanKrw,
            netEquityRequiredKrw = netEquityRequiredKrw,
            estimatedMonthlyRentKrw = estimatedMonthlyRentKrw,
            estimatedCapRatePct = estimatedCapRatePct,
            estimated12mRoiPct = estimated12mRoiPct,
            evictionRiskLevel = evictionRiskLevel,
            estimatedEvictionDays = estimatedEvictionDays,
            estimatedEvictionReserveKrw = estimatedEvictionReserveKrw,
            dividendWaterfall = waterfall,
            taxScenarios = taxScenarios,
            photoGuideShots = photoShots,
            nextDropMinBidKrw = nextDropMinBidKrw,
            breakevenResalePriceKrw = breakevenResalePriceKrw,
            rateStressMonthlyDeltaKrw = rateStressMonthlyDeltaKrw,
            privacyMaskedConfirmed = true
        )
    }

    fun buildPaidAppVsBlogBenchmark(): List<BenchmarkTierRow> = listOf(
        BenchmarkTierRow(
            dimension = "개인정보 비식별(채무자·임차인)",
            generalBlogLevel = "원문 복사로 실명·성씨 노출 위험",
            paidAppLevel = "유료 조회 시 마스킹 일부 제공",
            ddakchalFreeBlogLevel = "성명·성씨·전화·전입일 100% 자동 차단",
            ddakchalSignupProLevel = "공시 권리관계만 익명 무결성 아카이브"
        ),
        BenchmarkTierRow(
            dimension = "권리·배당 워터폴 & 인수금",
            generalBlogLevel = "소멸/인수 추측성 한줄 요약",
            paidAppLevel = "월 3~9만원 유료 결제 후 열람",
            ddakchalFreeBlogLevel = "0~3순위 배당표·인수보증금 무료 산출",
            ddakchalSignupProLevel = "입찰가 슬라이더 연동 실시간 배당 변동"
        ),
        BenchmarkTierRow(
            dimension = "총원가 손익분기(BEP)·유찰감가",
            generalBlogLevel = "감정가·최저가 단순 나열",
            paidAppLevel = "취득세/명도비 반영 없는 단순 할인율",
            ddakchalFreeBlogLevel = "취득세·인수금·명도비·이자 포함 BEP 무료",
            ddakchalSignupProLevel = "금리·보유기간별 순이익 몬테카를로"
        ),
        BenchmarkTierRow(
            dimension = "명의별 세무 3안(개인·공동·법인)",
            generalBlogLevel = "미제공",
            paidAppLevel = "별도 유료 세무 계산기/컨설팅",
            ddakchalFreeBlogLevel = "취득·보유·양도 손익 비교 3안 무료",
            ddakchalSignupProLevel = "보유주택수 맞춤형 세무·대출 한도 심사"
        ),
        BenchmarkTierRow(
            dimension = "현장 임장 사진 가이드",
            generalBlogLevel = "법원 제공 사진 1~2장 캡처",
            paidAppLevel = "법원 전경사진 단순 제공",
            ddakchalFreeBlogLevel = "유형별 임장 5컷 촬영법·적신호 판별 무료",
            ddakchalSignupProLevel = "전문가 현장 실사 리포트·체납 확정액"
        ),
        BenchmarkTierRow(
            dimension = "등기부 도식 & 65초 모션 영상",
            generalBlogLevel = "텍스트 줄글 위주(체류시간 저조)",
            paidAppLevel = "텍스트 표 형태(블로그 시각화 불가)",
            ddakchalFreeBlogLevel = "갑구·을구 4종 인포그래픽 + 5장면 콘티",
            ddakchalSignupProLevel = "실시간 등기부 초단위 변동 푸시 연동"
        ),
        BenchmarkTierRow(
            dimension = "입찰 직전 경쟁률·전용 고급정보",
            generalBlogLevel = "사후 결과만 확인 가능",
            paidAppLevel = "고액 유료 멤버십(월정액)",
            ddakchalFreeBlogLevel = "사전 공개 무료 분석으로 유료앱 초월",
            ddakchalSignupProLevel = "딱찰 무료 회원가입 시 실시간 경쟁률 히트맵 제공"
        )
    )

    private fun buildTaxScenarios(propertyType: PropertyType): List<TaxScenarioBreakdown> {
        val isHousing = propertyType == PropertyType.APT_OFFICETEL || propertyType == PropertyType.VILLA_MULTIFAMILY
        return listOf(
            TaxScenarioBreakdown(
                entityType = "개인 단독명의",
                acquisitionTaxPct = if (isHousing) "1.1~3.3% (1주택) / 다주택 8~12%" else "4.6% 단일세율",
                holdingTaxNote = "1세대 단독 공제 기준 · 장기보유특별공제 유리",
                capitalGainsTaxNote = "2년 보유 시 일반세율(6~45%), 단기매도(1년내) 70% 주의",
                recommendedWhen = "무주택·1주택 실거주 또는 2년 이상 장기 보유 전략"
            ),
            TaxScenarioBreakdown(
                entityType = "부부 공동명의 (5:5)",
                acquisitionTaxPct = if (isHousing) "1.1~3.3% (지분합산 주택수 기준)" else "4.6% (임대소득 분산)",
                holdingTaxNote = "인별 재산·종부세 분산 및 건강보험 피부양자 체크",
                capitalGainsTaxNote = "양도차익 50% 분산으로 누진세율 구간 하향 + 기본공제 250만×2",
                recommendedWhen = "예상 시세차익 1억원 이상 매각 예정 시 절세 극대화"
            ),
            TaxScenarioBreakdown(
                entityType = "매매·임대 법인명의",
                acquisitionTaxPct = if (isHousing) "12.4~13.4% 중과 유의" else "4.6% (비주거 중과 배제)",
                holdingTaxNote = "이자·수선비·인건비·차량유지비 전액 손금(필요경비) 인정",
                capitalGainsTaxNote = "법인세 9~19% 구간 적용 (주거용 추가과세 20% 별도 점검)",
                recommendedWhen = "상가·공장·토지 단기 매도(NPL/밸류애드) 및 다수 물건 회전"
            )
        )
    }

    fun buildFieldPhotoGuide(propertyType: PropertyType, auction: AuctionCaseIntel): List<FieldPhotoGuideShot> =
        when (propertyType) {
            PropertyType.APT_OFFICETEL -> listOf(
                FieldPhotoGuideShot(1, "1동 현관 우편함 & 반송 우편물", "채무자/임차인 실거주 여부 및 독촉장 수신 날짜 촬영", "우편물 2개월 이상 적체 + 전기요금 체납 스티커", "${auction.caseId} 현장 우편함·점유 실사 컷"),
                FieldPhotoGuideShot(2, "세대 계량기 (전기·수도·가스)", "현재 지침 회전 여부 및 최근 검침량 비교 촬영", "계량기 봉인·철거 또는 제로 정지 상태", "${auction.caseId} 공실·거주 판별 계량기 체크"),
                FieldPhotoGuideShot(3, "관리사무소 체납관리비 확인서", "대법원 2001다8677 기준 공용·전유부분 분리 내역 촬영", "공용관리비 3년 치 체납액 300만원 이상 누적", "공용관리비 승계 리스크 확인 포인트"),
                FieldPhotoGuideShot(4, "외벽 균열 · 최상층/발코니 누수 흔적", "외부 창호 실리콘 노후, 결로·곰팡이, 윗층 누수 확인", "샤시 틀 뒤틀림 및 천장 누수 얼룩 확장", "수선비·CAPEX 산정 외관 실사 사진"),
                FieldPhotoGuideShot(5, "단지 내 일조권 · 주차 동선 · 학군 정문", "오전/오후 채광, 지하주차장 연결, 급매물 호가 전단지", "지하주차장 미연결 구축 또는 혐오시설 근접", "인근 실거래·임대 수요 검증 임장 컷")
            )
            PropertyType.VILLA_MULTIFAMILY -> listOf(
                FieldPhotoGuideShot(1, "건물 전체 외벽 & 필로티 주차장 기둥", "구조 균열, 내진 보강, 주차 구획 실사용 가능 대수 촬영", "외벽 타일 탈락·필로티 균열 또는 불법 증축 구조", "${auction.caseId} 다세대 외관·주차 실사"),
                FieldPhotoGuideShot(2, "현관 호수 표기 vs 건축물대장 대조", "문패 호수(예: 302호)와 등기부·전입세대 호수 일치 여부", "실제 문패와 공부상 호수가 어긋난 다가구/전세사기 위험", "선순위 임차인 호수 불일치 검증 필수 컷"),
                FieldPhotoGuideShot(3, "옥상 방수층 · 우수관 · 보일러실", "우레탄 방수 들뜸 및 반지하/최상층 누수·결로 흔적", "옥상 불법 옥탑방(위반건축물 이행강제금 대상)", "위반건축물·방수 하자 체크 사진"),
                FieldPhotoGuideShot(4, "공동현관 초인종 · 계량기함 전체", "전 세대 계량기 가동 현황 및 세입자 전입 여부 파악", "현관에 '선순위 대항력 행사 중' 경고문 부착", "점유자·대항력 실태 현장 포착"),
                FieldPhotoGuideShot(5, "인근 재개발/모아타운 현수막 & 역세권 도보", "정비사업 추진 현수막, 신축 빌라 분양가, 전세가율 확인", "주변 공실 다수 및 깡통전세 밀집 블럭", "권역 호재·유동성 리스크 현장 컷")
            )
            PropertyType.COMMERCIAL_RETAIL -> listOf(
                FieldPhotoGuideShot(1, "주출입구 전면 간판 폭 & 가시성", "횡단보도·버스정류장 기준 보행자 시선 가시성 촬영", "가로수·전봇대·계단 단차로 매장 입구 시야 가림", "${auction.caseId} 상가 전면 가시성 테스트"),
                FieldPhotoGuideShot(2, "상가 내부 천장 층고 · 급배수 · 전력용량", "음식점/카페 입점 가능 배수관·덕트·승압 가능 여부", "덕트 설치 불가 민원 단지 또는 누수 흔적", "업종 제한·배수·전기 CAPEX 확인 컷"),
                FieldPhotoGuideShot(3, "반경 500m 공실 점포 비율 & 동종업종", "동일 층 및 인접 건물 '임대문의' 현수막 밀도 기록", "1층 3개 중 2개 이상 장기 공실 방치", "상권 공실률·CapRate 검증 사진"),
                FieldPhotoGuideShot(4, "상가번영회/관리실 체납 내역 & 업종제한 규약", "장기공실 관리비 체납액 및 지정업종(약국·편의점 독점) 확인", "관리단 규약상 유흥/음식점 금지 및 수백만원 체납", "체납관리비 및 독점업종 체크 컷"),
                FieldPhotoGuideShot(5, "피크타임(점심/퇴근) 유동인구 & 주차장", "방문자 주차 1시간 무료 지원 여부 및 피크타임 통행량", "주차 불가 및 배후 세대 단절 골목", "실임대료 산정용 배후수요 임장 컷")
            )
            PropertyType.LAND_FARMLAND -> listOf(
                FieldPhotoGuideShot(1, "접면 현황도로 폭(4m 이상) & 포장 상태", "차량 진입 가능 여부, 아스콘/콘크리트 현황도로 실측", "타인 사유지 통과 필수인 맹지 또는 폭 2m 미만 농로", "${auction.caseId} 맹지 여부·진입도로 컷"),
                FieldPhotoGuideShot(2, "지적 경계점 및 인접 필지 침범 여부", "현장 말뚝·담장·전신주 위치와 지적도상 경계 대조", "인접 건물 처마·축대가 매각 토지 경계 침범", "경계 침범·법정지상권 체크 사진"),
                FieldPhotoGuideShot(3, "지목 현황(전·답·임야) & 제시외 수목/비닐하우스", "현재 경작 작물 종류, 다년생 유실수·비닐하우스 매각제외 여부", "무단 경작자 존재 또는 분묘(봉분) 1기 이상 발견", "농취증·분묘기지권 현장 확인 컷"),
                FieldPhotoGuideShot(4, "우수 배수로 · 성토 높이 · 고압선 철탑", "도로면 대비 지대 높이(저지대 침수) 및 송전선로 통과 여부", "상습 침수 저지대 또는 부지 상공 고압선 횡단", "개발행위·토목공사비 변수 촬영"),
                FieldPhotoGuideShot(5, "주변 산업단지·IC·국도 연결 진입로", "계획도로 예정지 안내판 및 인근 실거래 매물 현수막", "접도구역·개발제한구역(그린벨트) 지정 안내", "토지 활용가치 및 HBU 입지 컷")
            )
            PropertyType.FACTORY_SPECIAL -> listOf(
                FieldPhotoGuideShot(1, "진입도로 폭(8m 이상) & 40ft 대형차량 회전반경", "대형 화물트럭(윙바디/추레라) 상하차 도크 진입 가능 여부", "진입로 사도 지분 누락(통행료 분쟁) 또는 좁은 코너", "${auction.caseId} 대형차량 진입동선 실사"),
                FieldPhotoGuideShot(2, "감정평가서 기계기구(제6조) 명판 & 시리얼 대조", "호이스트(톤수)·컴프레서·CNC 등 주요 설비 실물·명판 촬영", "감정서상 고가 기계기구 반출·멸실 또는 파손", "공장저당 기계기구 실물 대조 필수 컷"),
                FieldPhotoGuideShot(3, "수변전실 변압기 용량(kW) & 층고(m)", "계약전력 한전 계량기 용량표, 공장동 유효 층고·크레인 레일", "변압기 노후(교체비 수천만원) 또는 전력량 부족", "전력·층고·호이스트 스펙 확인 컷"),
                FieldPhotoGuideShot(4, "정문 '유치권 행사 중' 현수막 & 점유 흔적", "현수막 설치 일자, 자물쇠·경비용역 상주 및 실제 점유 여부", "공사대금 계약서·세금계산서 갖춘 진성 유치권 정황", "유치권 진위 판별 현장 증거 촬영"),
                FieldPhotoGuideShot(5, "산업단지 관리공단 안내판 & 폐수/환경 설비", "입주 업종코드(C코드) 제한 표지, 정화조·집진시설 상태", "특정업종 입주 불가 또는 폐기물 무단 방치(처리비 수천만)", "인허가·폐기물 처리 리스크 확인 컷")
            )
        }

    fun buildChannelProFunnelText(
        auction: AuctionCaseIntel,
        platform: ChannelPlatform,
        config: ServerAutomationConfig,
        funnel: ProFunnelConfig,
        seedIndex: Int = 0
    ): String {
        if (!funnel.funnelEnabled) return ""
        val proUrl = "${funnel.proLandingUrl.trimEnd('/')}/${auction.dedupKey}?case=${auction.caseId}"
        val signupUrl = "${funnel.proLandingUrl.trimEnd('/')}/signup?case=${auction.caseId}"
        val lockedItems = funnel.proLockedItems
        val variantIdx = ((seedIndex % 12) + 12) % 12
        return when (platform) {
            ChannelPlatform.CLOUDFLARE -> buildString {
                appendLine()
                val header = listOf(
                    "[딱찰 회원가입 전용 심화 모듈]",
                    "[실시간 PRO 리서치 데이터 연동]",
                    "[입찰 당일 경쟁률·등기알림 가입 안내]",
                    "[딱찰 회원 전용 정밀 보정 지표]",
                    "[추가 심화 배당·한도 시뮬레이션]",
                    "[딱찰 플랫폼 직결 데이터 브리핑]",
                    "[마감 전 변동추적 계정 연동 안내]",
                    "[딱찰 PRO 정밀 리포트 개방 경로]",
                    "[모니터링 알림 및 경쟁분포 조회]",
                    "[경매 마감 전 최종 변동 연동창구]",
                    "[회원 인증 기반 추가 통계 패널]",
                    "[사건 변동 푸시·심층 분석 연결]"
                )[variantIdx]
                appendLine(header)
                val body = when (variantIdx) {
                    0 -> "무료 브리핑 범위를 넘어 ${lockedItems.getOrElse(0) { "실시간 경쟁률 히트맵" }}과 ${lockedItems.getOrElse(1) { "등기 변동 알림" }} 조회는 딱찰 회원가입($signupUrl) 후 $proUrl 에서 지원함."
                    1 -> "유료 정보망 수준의 세무·배당 요약에 더해 딱찰 회원가입($signupUrl) 완료 계정은 응찰 집중도와 등기부 경보를 $proUrl 경로로 즉시 수신함."
                    2 -> "기일 당일 오전 응찰 변동 추이 및 경락잔금 한도 역산 도구는 딱찰 무료 회원가입($signupUrl) 이용자에 한해 $proUrl 주소에서 개방함."
                    3 -> "매각 전야 공고 변동 푸시와 호가 대비 경쟁 분포 열람이 필요하면 딱찰 회원가입($signupUrl) 뒤 사건 전용 URL($proUrl)을 통해 확인 가능함."
                    4 -> "비식별 공시 분석 외 추가 담보한도 시뮬레이터와 권리 경보는 딱찰 회원가입($signupUrl) 인증 페이지($proUrl)에서 실행함."
                    5 -> "제시 구간 외에도 실제 응찰자 밀집도 및 기일 변경 이력 추적을 원할 경우 딱찰 회원가입($signupUrl) 이후 $proUrl 화면을 활용함."
                    6 -> "채권 최고액 변동과 선순위 대위변제 발생 알림을 놓치지 않으려면 딱찰 무료 회원가입($signupUrl) 후 전용 대시보드($proUrl)를 구독해야 함."
                    7 -> "지역 평균 낙찰비율 대비 과열·저평가 시그널 대조 기능은 딱찰 회원가입($signupUrl) 절차 완료 후 $proUrl 페이지에서 제공함."
                    8 -> "법정 전날까지 접수되는 취하·연기 통지 및 권리 변동 스냅샷은 딱찰 계정 가입($signupUrl) 뒤 전용 리서치뷰($proUrl)에 자동 반영됨."
                    9 -> "자기자본 변동에 따른 레버리지 시뮬레이션과 호별 공시 대조표는 딱찰 무료 회원가입($signupUrl) 링크를 거쳐 $proUrl 에서 제공함."
                    10 -> "공고 이후 추가 설정된 가압류·관리비 정산 변경값은 딱찰 플랫폼 가입($signupUrl) 완료 시 $proUrl 리포트에 즉시 표기됨."
                    else -> "유관 권역 낙찰 분포와 기관별 담보비율 비교 모듈은 딱찰 무료 가입($signupUrl) 인증 뒤 $proUrl 경로로 개방함."
                }
                appendLine(body)
            }
            ChannelPlatform.TISTORY -> buildString {
                appendLine()
                val header = listOf(
                    "[무료 정량 분석 외 딱찰 가입 안내]",
                    "[PRO 멤버십 실시간 데이터 연동]",
                    "[딱찰 무료 회원가입 추가 리포트]",
                    "[경쟁률·경락대출 한도 정밀 조회]",
                    "[등기부 변동 알람 계정 연동 가이드]",
                    "[심층 배당 계산기 회원 전용 구간]",
                    "[입찰 당일 시황 보정치 열람 안내]",
                    "[딱찰 플랫폼 실시간 추적 안내]",
                    "[세무·대출 상세 모델링 연결 통로]",
                    "[기일 직전 법원 공시 알림 연동]",
                    "[매수 전략 검증 추가 데이터 안내]",
                    "[사후 변동 모니터링 무료 회원 혜택]"
                )[variantIdx]
                appendLine(header)
                val body = when (variantIdx) {
                    0 -> "앞서 산출한 계량 모형 외 ${lockedItems.getOrElse(2) { "대출 한도 정밀 심사" }}와 ${lockedItems.getOrElse(3) { "입찰 당일 경쟁률 분포" }}는 딱찰 무료 회원가입 계정에서 추가로 열람할 수 있다."
                    1 -> "유료 플랫폼보다 먼저 공개한 세제 수치 이외에 기일 직전 응찰자 유입 지수는 딱찰 회원가입 뒤 리서치 패널에서 확인 가능하다."
                    2 -> "등기부 실시간 경보 및 금융권 담보인정비율 역산 모델은 딱찰 무료 회원가입 사용자에게 별도 요금 없이 개방된다."
                    3 -> "본 보고서의 수익률 산식 이후 매각 오전까지 갱신되는 취하·변경 공고와 예상 입찰자 수는 딱찰 회원가입 완료 즉시 조회된다."
                    4 -> "제시 가격 스펙트럼 외 실제 보유 주택 수별 중과세율과 개인별 대출 가능액 비교는 딱찰 회원가입 전용 시뮬레이터에서 구동된다."
                    5 -> "총매입원가 기준 손익분기점을 통과해 실제 응찰을 결정했다면 딱찰 무료 회원가입으로 ${lockedItems.getOrElse(0) { "경쟁률 히트맵" }}과 변경 알람을 함께 점검하길 권한다."
                    6 -> "배당표상 미확정 이자 증가분이나 추가 압류 기재 여부는 입찰 전날 딱찰 회원가입 계정을 통해 최종 변동 내역을 확인해야 오차를 줄인다."
                    7 -> "권역별 낙찰분포와 차회 유찰 대비 기대수익 탄력성 그래프는 딱찰 무료 회원가입 이후 추적 화면에서 상시 업데이트된다."
                    8 -> "금융비용 변동과 세후 현금흐름 민감도를 개인 조건에 맞춰 다시 계산하려면 딱찰 무료 가입 후 계산기 패널을 이용할 수 있다."
                    9 -> "집행관 현황조사 이후 추가 제출된 이해관계인 의견서와 매각변경 공지는 딱찰 회원가입 계정에 실시간 푸시로 연결된다."
                    10 -> "단기 매각과 장기 임대 보유 전략 사이의 세목별 세액 차이는 딱찰 회원 가입 시 제공하는 다주택 시뮬레이터로 정밀 비교된다."
                    else -> "최종 입찰표 작성에 앞서 최근 경매장 응찰 인원 추이와 낙찰가 분포를 보려면 딱찰 회원가입 화면을 활용하면 편리하다."
                }
                appendLine(body)
            }
            ChannelPlatform.NAVER -> buildString {
                appendLine()
                val header = listOf(
                    "[더 깊은 실시간 자료는 딱찰 회원가입으로 확인하세요]",
                    "[입찰 직전 변동 소식은 딱찰 가입 안내를 참고해 주세요]",
                    "[실시간 응찰 추이와 등기 변동 알람 받는 법]",
                    "[마감 전날 한 번 더 짚어보는 딱찰 회원가입]",
                    "[추가 심화 지표와 기일 변경 푸시 안내]",
                    "[개인정보 비공개 원칙 및 딱찰 무료 가입 혜택]",
                    "[입찰 당일 경쟁률·대출한도 사전 점검 방법]",
                    "[딱찰 회원 전용 실시간 현장 보정 안내]",
                    "[변동 공고 즉시 확인하는 무료 가입 절차]",
                    "[실시간 권리 경보 및 대출 가심사 연동]",
                    "[법정 가기 전 마지막 변동 체크 방법]",
                    "[딱찰 무료 계정으로 이어보는 맞춤 수치]"
                )[variantIdx]
                appendLine(header)
                val body = when (variantIdx) {
                    0 -> "채무자와 임차인의 민감한 신상 정보는 모두 가렸고, 손익분기·현장 사진 순서는 본문에 전부 담았습니다. 기일 직전 움직이는 경쟁률 흐름과 등기 변경 알림은 딱찰 회원가입 뒤 전용 메뉴에서 받아보실 수 있습니다."
                    1 -> "당사자 실명이나 전입 날짜 같은 신상 내역은 철저히 가리고 핵심 수지 계산만 투명하게 적었습니다. 혹시 입찰 전날 밤 취하·연기 여부나 응찰 몰림 정도가 궁금하시다면 딱찰 회원가입으로 간편히 이어서 살펴보세요."
                    2 -> "공개된 등기부와 주변 시세만으로 1차 윤곽을 잡으셨다면 마지막으로 실제 법원 변동 접수와 금융권 대출 한도 확인이 남습니다. 이러한 실시간 보조 수치는 딱찰 회원가입을 마치신 분들께 무료 연동 화면으로 열어드리고 있습니다."
                    3 -> "독자 혼선을 줄이기 위해 원문 기준 핵심 숫자와 임장 체크 순서 위주로 알기 쉽게 풀어냈습니다. 회차별 낙찰비율 추이나 당일 예상 경쟁 강도처럼 계속 변하는 데이터는 딱찰 회원가입 후 바로 대조해 보시기 바랍니다."
                    4 -> "현장 확인 기준과 세무 유불리는 지금 읽으신 글만으로도 충분히 가늠되실 겁니다. 여기에 매각 당일 기일 변경 문자나 신규 가압류 알람까지 챙기고 싶으실 때 딱찰 회원가입 기능을 켜 두시면 한결 마음 편히 준비하실 수 있습니다."
                    5 -> "별도 결제를 하지 않으셔도 필수 권리분석과 원가 기준은 아낌없이 공유해 드렸습니다. 한 단계 더 들어가 금융기관별 잔금대출 한도와 시간대별 관심 집중도를 따져보시려면 딱찰 회원가입 안내를 활용하시면 좋습니다."
                    6 -> "종이 서류와 발품으로 확인한 기초 사실 위에 입찰 당일 오전까지 들어오는 채권 변동과 취소 공고까지 겹쳐 보아야 완벽합니다. 딱찰 회원가입을 해두시면 실시간 변동 내역을 깔끔하게 받아보실 수 있습니다."
                    7 -> "유료 정보를 찾지 않아도 판단이 서도록 주요 계산을 풀어 적었으니 먼저 본문 숫자를 꼼꼼히 대조해 보십시오. 실제 입찰표 작성 직전에 경쟁률 분포와 최종 변경 공고를 재확인하실 때는 딱찰 회원가입 계정이 요긴하게 쓰입니다."
                    8 -> "지금 정리한 내용으로 큰 틀의 안전 여력은 파악되셨을 텐데요, 입찰 당일 오전 금융권 금리 조건과 법정 공고 변경사항은 딱찰 무료 회원가입 메뉴를 통해 한눈에 확인해 두시기를 권해드립니다."
                    9 -> "본문에 담은 현장 점검표를 들고 발품을 파신 다음, 응찰 전 마지막으로 체납액 변동과 경락잔금 한도를 재점검하고 싶으실 때는 딱찰 회원가입 화면을 열어보시면 바로 대조가 가능합니다."
                    10 -> "숫자 하나 차이로 명도 비용과 수익률이 달라지는 만큼, 당일 새벽까지 올라오는 추가 문건 접수 소식은 딱찰 무료 가입 알림을 통해 챙기시는 편이 실무상 훨씬 수월합니다."
                    else -> "발품 임장으로 확인한 건물 상태에 더해 시시각각 바뀌는 권역 낙찰 열기와 대출 규제 조건까지 확인하시려면 딱찰 회원가입으로 이어가며 차분히 최종가를 정하시기 바랍니다."
                }
                appendLine(body)
                appendLine("검색창에 '${funnel.naverSearchKeyword} ${auction.caseId}' 를 입력하시거나 하단 고정 댓글 안내를 따라오시면 됩니다.")
                if (config.enableExternalLinkNaver) {
                    appendLine("딱찰 가입·상세 보기: $signupUrl")
                }
            }
        }
    }

    fun buildChannelProFunnelHtml(
        auction: AuctionCaseIntel,
        platform: ChannelPlatform,
        config: ServerAutomationConfig,
        funnel: ProFunnelConfig
    ): String {
        if (!funnel.funnelEnabled) return ""
        val baseUrl = funnel.proLandingUrl.trimEnd('/')
        val proUrl = "$baseUrl/${auction.dedupKey}?case=${auction.caseId}&utm_source=${platform.code.lowercase()}&utm_medium=pro_funnel"
        val signupUrl = "$baseUrl/signup?case=${auction.caseId}&utm_source=${platform.code.lowercase()}"
        val lockedListHtml = funnel.proLockedItems.joinToString("") { "<li>$it</li>" }
        return when (platform) {
            ChannelPlatform.CLOUDFLARE -> """
<aside class="ddakchal-pro-teaser" style="border:1px solid #10B981;padding:14px;border-radius:10px;margin-top:16px;">
  <h3>${funnel.teaserHeadline}</h3>
  <p>개인정보는 100% 비식별 처리했고, 유료앱급 분석은 무료 공개했습니다. 더 고급 실시간 인텔리전스는 딱찰 회원가입 후 이용하세요.</p>
  <ul>$lockedListHtml</ul>
  <p><a href="$signupUrl" rel="internal">딱찰 무료 회원가입하고 ${auction.caseId} 실시간 PRO 리포트 열기 →</a></p>
</aside>
            """.trimIndent()
            ChannelPlatform.TISTORY -> """
<section class="pro-funnel-box" style="border-left:4px solid #F59E0B;padding:12px 16px;background:#111827;color:#F8FAFC;margin-top:16px;">
  <h3>유료앱 능가 무료 공개 vs 딱찰 회원가입(PRO) 전용 데이터</h3>
  <p>본 글은 권리·배당·세무 3안·손익분기(BEP)·현장 사진 가이드를 100% 무료 공개했습니다. 실시간 경쟁률·등기 변동 푸시는 딱찰 가입 회원에게 제공됩니다.</p>
  <ul>$lockedListHtml</ul>
  <p><a href="$proUrl" rel="nofollow sponsored">딱찰 회원가입 · ${auction.caseId} 실시간 경쟁률 히트맵 확인하기</a></p>
</section>
            """.trimIndent()
            ChannelPlatform.NAVER -> {
                val naverActionHtml = if (config.enableExternalLinkNaver) {
                    "<p><a href=\"$signupUrl\">딱찰 무료 회원가입 후 고급정보 보기</a></p>"
                } else {
                    "<p><strong>딱찰 가입 안내:</strong> 네이버에서 '${funnel.naverSearchKeyword} ${auction.caseId}' 검색 후 딱찰 회원가입 또는 고정 댓글 확인</p>"
                }
                """
<div class="naver-pro-teaser">
  <p><strong>${funnel.teaserHeadline}</strong></p>
  <ul>$lockedListHtml</ul>
  $naverActionHtml
</div>
                """.trimIndent()
            }
        }
    }

    fun buildNaverPinnedComment(
        auction: AuctionCaseIntel,
        funnel: ProFunnelConfig
    ): String {
        val url = "${funnel.proLandingUrl.trimEnd('/')}/${auction.dedupKey}?case=${auction.caseId}"
        return funnel.naverPinnedCommentTemplate
            .replace("{사건번호}", auction.caseId)
            .replace("{검색어}", "${funnel.naverSearchKeyword} ${auction.caseId}")
            .replace("{링크}", url)
            .trim()
    }

    fun build6InfographicCards(
        auction: AuctionCaseIntel,
        platform: ChannelPlatform,
        minBidStr: String,
        report: ProAuctionAnalysisReport,
        funnel: ProFunnelConfig,
        salt: String
    ): List<InfographicCardSpec> {
        val regionSlug = auction.address.split(" ").take(2).joinToString("-")
        val marginStr = QaVerificationEngine.formatKrwShort(report.safetyMarginKrw)
        val equityStr = QaVerificationEngine.formatKrwShort(report.netEquityRequiredKrw)
        val cardDefs = listOf(
            "THUMBNAIL_SUMMARY" to "${auction.caseId} 최저가 $minBidStr (${auction.discountRatePct}% · 마진 $marginStr)",
            "COURT_DOCS_RIGHTS" to "${auction.caseId} 4대공부 배당워터폴 · ${report.evictionRiskLevel}",
            "STATS_3Y_AND_TRADES" to "$regionSlug 실거래 대비 CapRate ${report.estimatedCapRatePct}% · ROE ${report.estimated12mRoiPct}%",
            "BID_TAX_WATERFALL" to "3전략 입찰 · LTV70 자기자본 $equityStr · 세무 3안",
            "FIELD_PHOTO_GUIDE" to "${auction.propertyType.displayLabel} 현장 임장 사진 5컷 체크포인트",
            "PRO_FUNNEL_CTA_BANNER" to if (platform == ChannelPlatform.NAVER) {
                "네이버 '${funnel.naverSearchKeyword} ${auction.caseId}' · 딱찰 가입 후 PRO 열람"
            } else {
                "딱찰 무료 회원가입 · 실시간 AI 경쟁률 · 등기변동 알림 바로가기"
            }
        )

        return cardDefs.mapIndexed { i, (type, headline) ->
            val seed = "${auction.caseId}|${platform.code}|$type|${platform.imageResolutionSpec}|$salt|crop${i + 1}"
            InfographicCardSpec(
                cardIndex = i + 1,
                cardType = type,
                aspectRatio = platform.imageResolutionSpec,
                headline = headline,
                subline = "EXIF 삭제 · ${platform.displayName} 전용 해시 변형(${platform.imageResolutionSpec})",
                seoFilename = "${auction.caseId}-$regionSlug-${auction.propertyType.code.lowercase()}-${i + 1}-${platform.code.lowercase()}.webp",
                altText = "${auction.caseId} ${auction.address} ${auction.propertyType.displayLabel} $headline",
                md5Hash = QaVerificationEngine.md5Short(seed),
                sha256Hash = QaVerificationEngine.sha256Short(seed),
                exifStripped = true
            )
        }
    }
}
