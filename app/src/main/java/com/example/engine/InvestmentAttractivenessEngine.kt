package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.CompetitorBlogBenchmarkRow
import com.example.model.InvestmentAttractivenessScore
import com.example.model.LegalDoctrineReport

/**
 * Computes content-selection & investment-review attractiveness score (0..100)
 * and benchmarks Ddakchal Studio against top competitor auction blogs.
 * Note: This score is explicitly an editorial selection & investment review aid,
 * NOT a profit guarantee indicator.
 */
object InvestmentAttractivenessEngine {

    fun score(
        auction: AuctionCaseIntel,
        legalReport: LegalDoctrineReport = LegalDoctrineVerifier.verify(auction)
    ): InvestmentAttractivenessScore {
        val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)

        // 1. 실거래가 대비 가격 경쟁력 (max 25)
        val priceMarginPct = proReport.safetyMarginPct
        val pricePts = when {
            priceMarginPct >= 30.0 -> 25
            priceMarginPct >= 20.0 -> 22
            priceMarginPct >= 12.0 -> 18
            priceMarginPct >= 5.0 -> 13
            else -> 7
        }

        // 2. 유찰 횟수 및 할인율 매력도 (max 20)
        val dropPts = when {
            auction.bidCount >= 3 || auction.discountRatePct <= 51 -> 20
            auction.bidCount == 2 || auction.discountRatePct <= 64 -> 17
            auction.bidCount == 1 || auction.discountRatePct <= 80 -> 13
            else -> 9
        }

        // 3. 권리분석 명확성과 법리 안전성 (max 20)
        val rightsPts = when {
            legalReport.criticalBlockCount > 0 -> 4
            legalReport.totalEstimatedLegalBurdenKrw == 0L -> 20
            legalReport.totalEstimatedLegalBurdenKrw <= 5_000_000L -> 17
            else -> 12
        }

        // 4. 주변 유사 물건 최근 3년 낙찰통계 (max 15)
        val statsPts = when {
            auction.stats3yRatePct >= 75.0 -> 15
            auction.stats3yRatePct >= 66.0 -> 13
            auction.stats3yRatePct >= 58.0 -> 11
            else -> 8
        }

        // 5. 예상 수익률 및 부대비용 비율 (max 10)
        val yieldPts = when {
            proReport.estimated12mRoiPct >= 14.0 && proReport.safetyMarginKrw > 0L -> 10
            proReport.estimated12mRoiPct >= 8.0 -> 8
            else -> 5
        }

        // 6. 주간 검색 수요 및 조회 관심도 (max 10)
        val demandPts = when {
            auction.clicksLast7d >= 700 -> 10
            auction.clicksLast7d >= 500 -> 8
            auction.clicksLast7d >= 300 -> 6
            else -> 4
        }

        val total = (pricePts + dropPts + rightsPts + statsPts + yieldPts + demandPts).coerceIn(0, 100)
        val grade = when {
            total >= 88 -> "S등급 · 최우선 발행 선정"
            total >= 78 -> "A등급 · 주간 주력 분석 물건"
            total >= 65 -> "B등급 · 리스크 해설 특화"
            else -> "C등급 · 주의 경보 참고용"
        }
        val verdict = buildString {
            append("주간 20~35건 목표 우선순위: $grade (${total}/100점) · ")
            append("할인 ${auction.discountRatePct}% / 안전마진 ${proReport.safetyMarginPct}% / ")
            append("법리부담 ${QaVerificationEngine.formatKrwShort(legalReport.totalEstimatedLegalBurdenKrw)}")
        }

        return InvestmentAttractivenessScore(
            caseId = auction.caseId,
            totalScore = total,
            gradeLabel = grade,
            priceCompetitivenessPts = pricePts,
            dropDiscountPts = dropPts,
            rightsClarityPts = rightsPts,
            stats3yPts = statsPts,
            yieldCostPts = yieldPts,
            demandSearchPts = demandPts,
            selectionVerdict = verdict
        )
    }

    fun buildCompetitorComparisonRows(): List<CompetitorBlogBenchmarkRow> = listOf(
        CompetitorBlogBenchmarkRow(
            blogName = "조은인연법률경매 이소장",
            weeklyPostsStat = "주 56건 (대량 게시형)",
            primaryFocus = "전국 사건번호 중심 물건 다량 소개",
            ddakchalSuperiority = "딱찰: 대량 발행 + 6대 법리판례 검증 + 3안 세무·BEP·Quality Defense 탈AI 동시 보장"
        ),
        CompetitorBlogBenchmarkRow(
            blogName = "태인경매 대표 블로그",
            weeklyPostsStat = "주 38.8건 (심층 분석형)",
            primaryFocus = "감정가·실거래가·권리관계 물건 분석",
            ddakchalSuperiority = "딱찰: 네이버 무료 API 실시간 급매 비교 재산정 + 등기 갑구/을구 인포그래픽 + 후속 낙찰·유찰 자동 추적"
        ),
        CompetitorBlogBenchmarkRow(
            blogName = "YG부동산경매",
            weeklyPostsStat = "주 25.2건 (산업용·수익형)",
            primaryFocus = "공장·창고·원룸·숙박시설 사례",
            ddakchalSuperiority = "딱찰: 공장저당법 6조·유치권 압류선후 검증 + 호이스트/전력 5컷 현장 가이드 + 비식별 안전"
        ),
        CompetitorBlogBenchmarkRow(
            blogName = "DSD 전국법원경매",
            weeklyPostsStat = "주 21건 (아파트 중심형)",
            primaryFocus = "아파트 단지 입지·주변 시세 비교",
            ddakchalSuperiority = "딱찰: 아파트+빌라+상가+토지+공장 5유형 분리 템플릿 및 3채널(네이버·티스토리·CF) 동시 최적화"
        ),
        CompetitorBlogBenchmarkRow(
            blogName = "FactoryPro 경매 콘텐츠",
            weeklyPostsStat = "주 20건+ (최근 사례형)",
            primaryFocus = "사건정보 및 최저매각가격 정리",
            ddakchalSuperiority = "딱찰: 30항목 95점 게이트 통과 원고만 등록 + 딱찰 회원가입 PRO 실시간 경쟁률 퍼널 연결"
        )
    )
}
