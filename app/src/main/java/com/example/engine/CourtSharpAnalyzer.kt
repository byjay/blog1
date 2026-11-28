package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.CourtDocKind
import com.example.model.CourtDocumentRecord
import com.example.model.CourtPhotoEvidence
import com.example.model.CourtSharpAnalysisReport
import com.example.model.CourtSharpDiscrepancyCheck
import com.example.model.FieldVisitRecord
import com.example.model.LegalDoctrineReport
import com.example.model.ProAuctionAnalysisReport
import com.example.model.PropertyType
import com.example.model.RegistryVisualNode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Razor-sharp 4-document court cross-verifier ("법원 공부 칼분석 엔진").
 * Cross-audits 매각물건명세서 × 현황조사서 × 감정평가서 × 등기부(갑·을구) × 집행관·현장 방문기록
 * and outputs both surgical audit checks and channel-distinct authority-promoting blog snippets
 * that stay 100% clean under QualityDefenseEngine and Article30PointEvaluator.
 */
object CourtSharpAnalyzer {

    fun analyze(
        auction: AuctionCaseIntel,
        photos: List<CourtPhotoEvidence>,
        documents: List<CourtDocumentRecord>,
        visitRecords: List<FieldVisitRecord>,
        registryGapGu: List<RegistryVisualNode>,
        registryEulGu: List<RegistryVisualNode>,
        legalReport: LegalDoctrineReport = LegalDoctrineVerifier.verify(auction),
        proReport: ProAuctionAnalysisReport = ProAnalysisAndPhotoGuideEngine.buildProReport(auction)
    ): CourtSharpAnalysisReport {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction)
        val hasAssumedNode = (registryGapGu + registryEulGu).any { it.isAssumedRisk }
        val hasSeniorTenantRisk = hasAssumedNode ||
            (safeAuction.assumedRightsText.contains("선순위") && safeAuction.assumedRightsText.contains("인수")) ||
            (safeAuction.tenantSummary.contains("선순위") && safeAuction.tenantSummary.contains("미신청"))

        val absorbedKrw = proReport.dividendWaterfall.sumOf { it.absorbedByBuyerKrw }
        val totalAcquisitionKrw = safeAuction.balancedBidKrw + absorbedKrw + proReport.assumedExtraCostKrw
        val absorbedText = if (absorbedKrw > 0L) QaVerificationEngine.formatKrwShort(absorbedKrw) else "0원(전액 소멸)"
        val totalCostText = QaVerificationEngine.formatKrwShort(totalAcquisitionKrw)
        val bepText = QaVerificationEngine.formatKrwShort(proReport.breakevenResalePriceKrw)
        val livePhotoCount = photos.count { it.imageUrl.isNotBlank() }

        val saleSpec = documents.firstOrNull { it.docKind == CourtDocKind.SALE_SPECIFICATION }
        val statusDoc = documents.firstOrNull { it.docKind == CourtDocKind.STATUS_SURVEY }
        val appraisalDoc = documents.firstOrNull { it.docKind == CourtDocKind.APPRAISAL_REPORT }

        val hasRegistry = registryGapGu.isNotEmpty() || registryEulGu.isNotEmpty()
        val hasVisits = visitRecords.isNotEmpty()

        // 1. 등기부(갑·을구) × 매각물건명세서
        val checkRegistryVsSpec = CourtSharpDiscrepancyCheck(
            id = "sharp-reg-spec",
            axisTitle = "1축. 등기부(갑·을구) × 매각물건명세서 교차 대조",
            documentsCompared = "갑구 ${registryGapGu.size}건·을구 ${registryEulGu.size}건 × ${saleSpec?.referenceNo ?: "명세서(${documents.size}종)"}",
            verdictBadge = when {
                !hasRegistry -> "등기 미수신"
                hasSeniorTenantRisk -> "선순위 인수 경계"
                else -> "말소 기준 확정"
            },
            isAlert = !hasRegistry || hasSeniorTenantRisk,
            findingDetail = if (hasRegistry) {
                "말소기준일 ${safeAuction.baseRightDate.ifBlank { "미기재" }} · 배당종기 ${safeAuction.dividendDeadline.ifBlank { "미기재" }} 대조 결과 매수인 별도 인수 추산액 $absorbedText. ${safeAuction.assumedRightsText}"
            } else {
                "서버 등기부 갑·을구 데이터가 아직 수신되지 않았습니다. 말소기준일(${safeAuction.baseRightDate.ifBlank { "미기재" }}) 기준 대조가 필요합니다."
            },
            actionDirective = when {
                !hasRegistry -> "딱찰 서버 등기 연동 또는 대법원 등기부등본 열람 후 갑·을구 순위를 확인하십시오."
                hasSeniorTenantRisk -> "미배당 선순위 보증금을 입찰가에서 100% 공제한 상한선 이내로만 응찰하십시오."
                else -> "후순위 등기 권리는 잔금 완납 촉탁으로 전액 말소되므로 대금 납부일 관리만 집중하십시오."
            }
        )

        // 2. 현황조사서 × 집행관·관리실·임장 방문기록
        val latestVisit = visitRecords.firstOrNull()
        val hasMultiVisits = visitRecords.size >= 2
        val checkSurveyVsVisits = CourtSharpDiscrepancyCheck(
            id = "sharp-survey-visit",
            axisTitle = "2축. 현황조사서 × 집행관·관리실 방문기록 대조",
            documentsCompared = "${statusDoc?.referenceNo ?: "현황조사서"} × 방문기록 ${visitRecords.size}건",
            verdictBadge = when {
                !hasVisits -> "방문기록 미수신"
                hasMultiVisits -> "다중 방문 검증필"
                else -> "단건 방문 확인"
            },
            isAlert = !hasVisits || safeAuction.possessionSummary.contains("불명") || safeAuction.possessionSummary.contains("유치권"),
            findingDetail = if (hasVisits) {
                "방문기록 ${visitRecords.size}건(${latestVisit?.visitRound ?: "현황조사"}: ${latestVisit?.occupancyStatus ?: safeAuction.possessionSummary}). ${safeAuction.tenantSummary}"
            } else {
                "서버에서 수신된 집행관 현황조사서 방문·임장 기록이 아직 없습니다. 현황 요약: ${safeAuction.possessionSummary}"
            },
            actionDirective = "공부상 점유와 실제 거주·체납 관리비(2001다8677) 차이를 입찰 전날 관리사무소·우편함으로 재대조하십시오."
        )

        // 3. 감정평가서(사진·면적) × 시세·특수리스크
        val propertyTrapCheck = when (safeAuction.propertyType) {
            PropertyType.APT_OFFICETEL, PropertyType.VILLA_MULTIFAMILY ->
                "대법원 2001다8677 공용관리비 승계(3년 원금) 및 전입세대 호수 일치 여부"
            PropertyType.COMMERCIAL_RETAIL ->
                "상임법 환산보증금 대항력·장기공실 관리비·반경 500m 공실률"
            PropertyType.LAND_FARMLAND ->
                "농지취득자격증명(미제출 시 보증금 10% 몰수)·지적도 4m 접면·분묘기지권"
            PropertyType.FACTORY_SPECIAL ->
                "공장저당법 제6조 기계기구 실물 대조·압류 후 유치권 파훼·40ft 진입로"
        }
        val checkAppraisalVsMarket = CourtSharpDiscrepancyCheck(
            id = "sharp-appraisal-market",
            axisTitle = "3축. 감정평가서(사진 ${livePhotoCount}컷) × 실거래·특수리스크",
            documentsCompared = "${appraisalDoc?.referenceNo ?: "감정평가서"} × 법원실사 ${livePhotoCount}건",
            verdictBadge = when {
                livePhotoCount == 0 -> "실사 미수신"
                proReport.safetyMarginPct >= 15.0 -> "안전마진 양호"
                else -> "마진 보수 검증"
            },
            isAlert = livePhotoCount == 0 || proReport.safetyMarginPct < 10.0,
            findingDetail = "감정가 ${QaVerificationEngine.formatKrwShort(safeAuction.appraisalPriceKrw)} → 최저가 ${QaVerificationEngine.formatKrwShort(safeAuction.minBidPriceKrw)}(${safeAuction.discountRatePct}%), 실거래 대비 안전마진 ${proReport.safetyMarginPct}%. 중점 확인: $propertyTrapCheck",
            actionDirective = if (livePhotoCount == 0) {
                "딱찰 서버 경매 사진 연동 또는 현장 촬영으로 외벽·도로·점유 상태를 직접 확인하십시오."
            } else {
                "감정평가 기준일 이후 누수·훼손·제시외 물건 변경분을 실사 사진 기준으로 원가에서 차감 반영하십시오."
            }
        )

        // 4. 6대 법리·판례 × 자금·BEP 스트레스 검산
        val checkDoctrineVsBep = CourtSharpDiscrepancyCheck(
            id = "sharp-doctrine-bep",
            axisTitle = "4축. 6대 법리·판례 × 총원가·손익분기(BEP) 칼검산",
            documentsCompared = "${legalReport.auditSummary} × BEP·금리 스트레스",
            verdictBadge = if (legalReport.overallPassed) "법리 모순 0건" else "법리 보정 필요",
            isAlert = !legalReport.overallPassed,
            findingDetail = "균형가(${QaVerificationEngine.formatKrwShort(safeAuction.balancedBidKrw)}) + 부대·인수비 합산 총취득원가 $totalCostText, 손익분기 매도가 $bepText, 금리 +1%p 시 월 +${QaVerificationEngine.formatKrwShort(proReport.rateStressMonthlyDeltaKrw)}.",
            actionDirective = "보수(${QaVerificationEngine.formatKrwShort(safeAuction.conservativeBidKrw)})~균형선 범위에서 세무 3안(단독·공동·법인)별 세후 수익률을 확정한 뒤 투찰하십시오."
        )

        val checks = listOf(
            checkRegistryVsSpec,
            checkSurveyVsVisits,
            checkAppraisalVsMarket,
            checkDoctrineVsBep
        )

        val killerTrapPoints = listOf(
            "함정 1 [낙찰가 착시 차단]: 표면 최저가만 보고 들어가면 인수액($absorbedText)·명도예비비·취득세로 실질 원가가 $totalCostText 까지 상승하므로 반드시 총원가 기준으로 상한을 잘라야 합니다.",
            "함정 2 [공부 간 불일치 대조]: 등기부(갑·을구 ${registryGapGu.size + registryEulGu.size}건)와 집행관 방문기록(${visitRecords.size}건) 시차 사이에 발생한 체납 공용관리비·미신고 점유를 당일 재확인해 손실을 막습니다.",
            "함정 3 [물건군 특화 급소 검증]: $propertyTrapCheck 항목과 매도 손익분기($bepText)를 동시에 통과하지 못하면 입찰하지 않는 원칙으로 원금 손실을 방어합니다."
        )

        val sharpGradeBadge = when {
            !legalReport.overallPassed || hasSeniorTenantRisk -> "정밀 경계 칼분석 · 인수위험 사전 포착"
            livePhotoCount > 0 && hasRegistry && hasVisits -> "S급 실서버 풀칼분석 · 사진 ${livePhotoCount} · 등기 ${registryGapGu.size + registryEulGu.size} · 방문 ${visitRecords.size}"
            livePhotoCount > 0 || hasRegistry || hasVisits -> "실서버 부분연동 칼분석 · 사진 ${livePhotoCount} · 등기 ${registryGapGu.size + registryEulGu.size} · 방문 ${visitRecords.size}"
            else -> "기본 수치 분석 (사진·등기·방문 서버 미수신)"
        }

        val executiveVerdict = "${safeAuction.caseId}은 등기부 갑·을구(${registryGapGu.size + registryEulGu.size}건)·명세서·현황서·감정서 및 방문기록 ${visitRecords.size}건을 4축 교차 검증한 결과, 매수인 추가 인수액은 $absorbedText 이고 균형 입찰 시 실질 총취득원가는 $totalCostText(손익분기 $bepText)로 산출됩니다."

        val authorityPromoHeadline = "표면 할인율만 보는 일반 분석과 달리, 30년 전담팀이 법원 4대 공부·등기 갑을구·방문기록 전건을 칼처럼 해부해 숨은 원가까지 0원 단위로 추출했습니다."

        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())
        return CourtSharpAnalysisReport(
            caseId = safeAuction.caseId,
            sharpGradeBadge = sharpGradeBadge,
            executiveVerdict = executiveVerdict,
            absorbedCostKrw = absorbedKrw,
            totalAcquisitionCostKrw = totalAcquisitionKrw,
            checks = checks,
            killerTrapPoints = killerTrapPoints,
            authorityPromoHeadline = authorityPromoHeadline,
            analyzedAt = now
        )
    }

    /**
     * Generates a concise, channel-distinct (~165–205 non-whitespace chars) razor-sharp
     * court cross-analysis & analytical authority showcase paragraph for Naver / Tistory / Cloudflare.
     * Strictly free of AI clichés, emojis, spam words, and raw Naver URLs.
     */
    fun buildBlogShowcaseSnippet(
        auction: AuctionCaseIntel,
        report: CourtSharpAnalysisReport,
        platform: ChannelPlatform,
        styleIndex: Int,
        gapGuCount: Int,
        eulGuCount: Int,
        docCount: Int,
        visitCount: Int
    ): String {
        val idx = ((styleIndex % 12) + 12) % 12
        val absorbedStr = if (report.absorbedCostKrw > 0L) {
            QaVerificationEngine.formatKrwShort(report.absorbedCostKrw)
        } else {
            "0원"
        }
        val totalCostStr = QaVerificationEngine.formatKrwShort(report.totalAcquisitionCostKrw)
        val regTotal = gapGuCount + eulGuCount

        return when (platform) {
            ChannelPlatform.NAVER -> listOf(
                "[법원 공부 4축 칼분석 · 전담 해부력 안내]\n단순 할인율만 읽는 보통 분석과 달리, 당사는 ${auction.caseId} 등기부 ${regTotal}건·법원 공부 ${docCount}종·현장 방문기록 ${visitCount}건을 날카롭게 교차 대조했습니다. 숨은 인수액 $absorbedStr 반영 실질 원가는 $totalCostStr 이며, 보통 응찰자가 놓치는 체납·점유 급소를 입찰 전 완봉합니다.",
                "[공부 교차 정밀 해부 · 30년 분석 차별점]\n표면 시세만 훑는 피상적 글과 달리 이번 ${auction.caseId} 리포트는 갑구·을구 ${regTotal}개 등기와 명세서·현황서·감정서 ${docCount}종, 방문로그 ${visitCount}건을 한 줄씩 맞췄습니다. 별도 승계 부담 $absorbedStr 합산 총원가 $totalCostStr 까지 뽑아내는 예리한 검증이 자산을 지킵니다.",
                "[법원 서류 완파 검증 · 전문가 안목]\n시중 요약본이 건너뛰는 현황조사 방문 ${visitCount}건과 등기부 ${regTotal}건을 ${auction.caseId} 법원 공부 ${docCount}종과 대조한 결과 추가 인수액은 $absorbedStr 로 잡힙니다. 실제 취득 총원가 $totalCostStr 까지 계산해 내는 당사만의 날 선 권리 해독력이 입찰 실수를 줄입니다.",
                "[4대 공부 칼검증 · 남다른 분석 깊이]\n유찰 숫자만 나열하는 분석으로는 숨은 명도·관리비 함정을 피할 수 없습니다. 당사는 ${auction.caseId} 등기 기록 ${regTotal}건·공식 서류 ${docCount}종·탐문 방문 ${visitCount}회를 교차해 인수금 $absorbedStr·총원가 $totalCostStr 를 도출하며 독자적인 정밀 판독 기준을 제시합니다.",
                "[서류·방문 입체 대조 · 실전 검증력]\n감정표 면적과 현황 방문기록 ${visitCount}건 사이의 미세한 틈을 잡아야 수익이 남습니다. ${auction.caseId} 공부 ${docCount}종과 갑을구 ${regTotal}건을 칼같이 쪼갠 결과 매수인 승계분 $absorbedStr 포함 원가는 $totalCostStr 이며, 이런 세밀한 교차 분석이 당사 30년 노하우의 핵심입니다.",
                "[공부 이면 해독 · 보수적 원가 칼계산]\n최저가 비율만 보고 진입했다가 체납·점유 변수에 발목 잡히는 사례가 잦습니다. ${auction.caseId} 법원 서류 ${docCount}종·등기 ${regTotal}건·현장 방문 ${visitCount}건을 빈틈없이 대조해 인수 예상액 $absorbedStr 과 총원가 $totalCostStr 를 확정하는 예리함이 차별화된 경쟁력입니다.",
                "[법원 원문 정밀 대조 · 함정 사전 차단]\n기계적 수치 나열이 아니라 등기부 ${regTotal}건과 방문기록 ${visitCount}건을 공부 ${docCount}종 원문에 맞물려 검증했습니다. ${auction.caseId} 추가 승계 $absorbedStr, 실질 원가 $totalCostStr 산출처럼 보이지 않는 리스크까지 짚어내는 치밀한 분석 역량이 안전한 낙찰의 기반입니다.",
                "[전문가 교차 해부 · 숨은 비용 0원 단위 추적]\n초보가 놓치는 배당종기·전입·공용체납 공백을 ${auction.caseId} 등기 ${regTotal}건·서류 ${docCount}종·방문 ${visitCount}건 교차표로 발라냈습니다. 예상 인수액 $absorbedStr 반영 총투입원가 $totalCostStr 계산까지 한 번에 끝내는 정밀한 분석 수준을 직접 비교해 보시기 바랍니다.",
                "[사법 공부 전수 해독 · 날카로운 리스크 선별]\n겉보기 낙폭 뒤의 법리 함정을 걸러내기 위해 ${auction.caseId} 갑을구 등기 ${regTotal}건, 법원 공부 ${docCount}종, 현장 방문 ${visitCount}건을 모두 맞대었습니다. 인수 추산 $absorbedStr 및 실질 원가 $totalCostStr 를 집어내는 깊이 있는 해부력이 당사 자문의 자부심입니다.",
                "[등기·현황·방문 삼각 칼분석 리포트]\n표면적인 낙찰가율 예측을 넘어 ${auction.caseId} 등기부 ${regTotal}건과 공식 문건 ${docCount}종, 방문 이력 ${visitCount}건을 깐깐하게 대조했습니다. 매수인 인수액 $absorbedStr 을 포함한 총원가 $totalCostStr 산출처럼 작은 오차도 허용하지 않는 정밀 분석을 원칙으로 삼습니다.",
                "[권리·현장 빈틈 Zero · 30년 해부 노하우]\n일반 자료에서 빠뜨리기 쉬운 점유·세무·체납 경계를 ${auction.caseId} 법원 문건 ${docCount}종과 등기 ${regTotal}건, 방문기록 ${visitCount}회로 재검증했습니다. 승계 부담 $absorbedStr·총취득원가 $totalCostStr 까지 계산해 내는 송곳 같은 분석 역량이 곧 수익 방어선입니다.",
                "[법원 데이터 정밀 해부 · 실무 전문가 검증]\n수박 겉핥기식 요약이 아니라 ${auction.caseId} 공부 ${docCount}종·등기 ${regTotal}건·방문기록 ${visitCount}건의 상호 모순 유무를 끝까지 파고들었습니다. 추가 인수 $absorbedStr, 총매입선 $totalCostStr 처럼 숫자의 근거를 밝히는 정직하고 예리한 분석으로 응찰자를 보호합니다."
            )[idx]

            ChannelPlatform.TISTORY -> listOf(
                "[4대 법원 공부 교차 칼분석 및 전문 검증 역량] 본 분석서는 ${auction.caseId}의 등기부 갑·을구 ${regTotal}건, 명세·현황·감정서 ${docCount}종, 집행관·임장 방문 ${visitCount}건을 교차 대조해 인수액 $absorbedStr 및 총취득원가 $totalCostStr 를 도출했다. 시중 피상적 요약과 구분되는 정량 판독력으로 보이지 않는 승계 위험을 차단한다.",
                "[공부 무결성 해부 및 실전 원가 검산] ${auction.caseId} 사건에서 단순 저감률 대신 등기 ${regTotal}건·법원 문건 ${docCount}종·방문로그 ${visitCount}건을 대조한 결과, 매수인 부담 $absorbedStr 반영 실질 원가는 $totalCostStr 로 계산됐다. 세목·점유 변수를 끝까지 추적하는 전문가 그룹의 날 선 분석 기법이 오차를 없앤다.",
                "[사법 자료 삼각 대조 · 고밀도 판독 리포트] 갑구·을구 ${regTotal}건과 법원 서류 ${docCount}종, 현장 방문 ${visitCount}회를 1:1 교차 검증한 끝에 ${auction.caseId}의 예상 승계액 $absorbedStr·총원가 $totalCostStr 를 확정했다. 겉값만 읊는 일반 정보와 달리 공부 이면의 급소까지 집어내는 정밀 역량이 돋보인다.",
                "[법리·현장 통합 해부 및 자문 차별화] 보통 입찰자가 간과하는 현황 시차와 체납 항목을 ${auction.caseId} 등기 ${regTotal}건·공부 ${docCount}종·방문 ${visitCount}건 교차검증으로 걸러내어 인수액 $absorbedStr, 총원가 $totalCostStr 를 산정했다. 데이터 간 충돌을 놓치지 않는 칼같은 분석 기준이 안정적 수익률을 뒷받침한다.",
                "[전담팀 4축 칼검증 · 숨은 원가 전수 포착] ${auction.caseId} 공고 원문을 구성하는 등기 기록 ${regTotal}건, 법원 3대 공부 ${docCount}종, 방문 이력 ${visitCount}건을 병렬 감사하여 추가 인수 $absorbedStr 및 총취득액 $totalCostStr 를 산출했다. 입찰 표면을 뚫고 실체 원가를 파헤치는 고도화된 실무 해독력이 본 리포트의 핵심 자산이다.",
                "[공부 교차 오차 0% 검증 프레임워크] 시세 차익 기대감에 가려진 권리 맹점을 찾기 위해 ${auction.caseId} 등기부 ${regTotal}건과 공부 ${docCount}종, 현장 방문 ${visitCount}건을 정밀 스캔해 인수분 $absorbedStr·총원가 $totalCostStr 로 압축했다. 30년 실전 케이스로 다듬어진 날카로운 검증 체계가 고위험 응찰을 사전에 막는다.",
                "[등기·명세·현황·감정 교차 판독 증명] ${auction.caseId} 관련 등기 ${regTotal}건, 공식 문서 ${docCount}종, 방문기록 ${visitCount}회를 빈틈없이 맞물려 검증함으로써 승계 원가 $absorbedStr 와 최종 취득선 $totalCostStr 를 도출했다. 숫자 하나도 교차 근거 없이 쓰지 않는 집요한 분석 태도가 일반 경매 글과의 결정적 차이다.",
                "[입찰 함정 선제 차단 · 고해상도 권리 해부] 본 건(${auction.caseId})의 등기 내역 ${regTotal}건, 법원 공부 ${docCount}종, 탐문 방문 ${visitCount}건을 해부해 숨은 인수 $absorbedStr 및 실질 매입가 $totalCostStr 를 계량화했다. 얕은 홍보성 글이 다루지 못하는 법원 자료 간 불일치를 포착하는 예리함이 자산을 지킨다.",
                "[원천 서류 대조 기반 정량 해부학] ${auction.caseId}의 등기 갑·을구 ${regTotal}건, 법원 서류 ${docCount}종, 방문로그 ${visitCount}건을 전수 대조해 매수자 인수 $absorbedStr, 총취득원가 $totalCostStr 로 정리했다. 법리·세무·현장 데이터를 하나의 계산표로 꿰어내는 30년 자문팀의 독보적 분석 품질을 확인할 수 있다.",
                "[공부 틈새 제거 · 정밀 리스크 필터링] 표면 수치에 현혹되지 않도록 ${auction.caseId} 등기 ${regTotal}건과 법원 공부 ${docCount}종, 방문기록 ${visitCount}건을 대조 검증해 인수 부담 $absorbedStr·총원가 $totalCostStr 를 정확히 분리했다. 미세한 권리 리스크까지 놓치지 않는 송곳 분석력이 현명한 응찰 기준을 세운다.",
                "[실전 4대 문서 완파 · 전문가 판독 가치] 경매 결과의 승패는 ${auction.caseId} 등기 ${regTotal}건·공부 ${docCount}종·방문 ${visitCount}건을 얼마나 촘촘히 맞추느냐에 달렸다. 예상 인수액 $absorbedStr 반영 실질 원가 $totalCostStr 를 산출하는 본 교차 엔진의 정교함은 단순 정보 나열형 블로그를 압도한다.",
                "[법원 데이터 교차 검증 · 실수 없는 의사결정] ${auction.caseId} 등기부 ${regTotal}건, 법원 문서 ${docCount}종, 현장 방문 ${visitCount}건을 4축으로 해부한 결과 추가 인수액은 $absorbedStr, 총취득원가는 $totalCostStr 로 판별됐다. 모든 쟁점을 수치와 판례로 직결시키는 예리한 실무 분석력이 곧 투자 안전망이다."
            )[idx]

            ChannelPlatform.CLOUDFLARE -> listOf(
                "[4대 공부 칼분석 · 전문가 검증 아카이브] ${auction.caseId} 등기부 ${regTotal}건·공부 ${docCount}종·방문기록 ${visitCount}건을 교차 감사한 결과, 추가 인수금 $absorbedStr 반영 총취득원가는 $totalCostStr 로 산출됨. 단순 호가 비교를 넘어 공부 모순과 숨은 체납을 짚어내는 당사의 고밀도 분석 역량이 원금 손실 가능성을 차단함.",
                "[정밀 권리 해부 · E-E-A-T 분석 권위] 표면 할인율에 기댄 얕은 브리핑과 달리 ${auction.caseId} 갑을구 ${regTotal}건·명세·현황·감정서 ${docCount}종·방문 ${visitCount}회를 전수 교차해 인수 부담 $absorbedStr, 총원가 $totalCostStr 를 확정함. 입찰 전 맹점을 칼처럼 도려내는 30년 실전 분석 체계가 안전한 기준을 제시함.",
                "[사법 서류 불일치 감사 · 실전 판독 스코프] ${auction.caseId}의 등기 ${regTotal}건, 법원 문서 ${docCount}종, 현장 방문 ${visitCount}건 데이터를 병렬 검증해 승계액 $absorbedStr 포함 총매입선 $totalCostStr 를 추출함. 보통 입찰자가 지나치는 점유·공법 함정을 0원 단위로 가려내는 예리한 분석 품질이 핵심 차별화 지점임.",
                "[공부 교차 검산 · 30년 자문팀 분석력 증명] 매각 공고 뒤편의 실질 비용을 밝히기 위해 ${auction.caseId} 등기 ${regTotal}건·공부 ${docCount}종·방문로그 ${visitCount}건을 대조해 인수 $absorbedStr 및 총원가 $totalCostStr 를 계상함. 법리와 현장 수치를 하나로 엮어내는 날 선 해부력이 불확실한 응찰 리스크를 최소화함.",
                "[고해상도 공부 감사 · 숨은 비용 ZERO 검증] ${auction.caseId} 등기부 갑·을구 ${regTotal}건, 핵심 공부 ${docCount}종, 방문 이력 ${visitCount}회를 정밀 교차해 추가 인수액 $absorbedStr·총원가 $totalCostStr 를 도출함. 겉숫자만 반복하는 범용 리포트와 달리 서류 간 미세 균열을 포착하는 전문팀의 분석 정밀도가 돋보임.",
                "[법원 원문 4축 해부 · 리스크 선제 봉쇄] 본 GEO 리포트는 ${auction.caseId}의 등기 ${regTotal}건, 공적 문서 ${docCount}종, 방문기록 ${visitCount}건을 교차 검산해 매수인 부담 $absorbedStr·총취득가 $totalCostStr 를 확증함. 사소한 체납·대항력 변수까지 날카롭게 솎아내는 실무 해독력이 입찰 실수를 원천 차단함.",
                "[데이터 교차 정밀 진단 · 전문 분석 경쟁력] ${auction.caseId} 관련 등기 ${regTotal}건·법원 문건 ${docCount}종·현장 방문 ${visitCount}건을 입체 대조한 바 인수액 $absorbedStr 반영 실질 원가는 $totalCostStr 로 판명됨. 감정서와 실제 점유의 간극을 정확히 메우는 송곳 같은 분석 기법이 보수적 투찰선을 지켜냄.",
                "[공부 전수 감사 모델 · 전문가 안목] 단순 시세 나열을 배제하고 ${auction.caseId} 등기부 ${regTotal}건, 법원 공부 ${docCount}종, 방문로그 ${visitCount}건을 맞대어 인수금 $absorbedStr·총취득원가 $totalCostStr 를 산출함. 판례와 현장 기록을 즉각 교차 검증하는 고수준 분석 아키텍처가 정보의 신뢰도를 증명함.",
                "[4대 공부 칼검증 · 오차 없는 원가 설계] ${auction.caseId} 등기 ${regTotal}건과 서류 ${docCount}종, 방문 ${visitCount}회의 시간순 정합성을 감사해 승계 비용 $absorbedStr 포함 총원가 $totalCostStr 를 확정함. 일반 분석이 놓치는 특수 물건 급소까지 직관적으로 풀어내는 30년 전담라인의 예리한 판단력이 강점임.",
                "[법원 공부 해부학 · 고신뢰 의사결정 지표] ${auction.caseId} 갑·을구 ${regTotal}건, 공식 서류 ${docCount}종, 임장·집행관 방문 ${visitCount}건을 전수 대조한 결과 추가 인수액은 $absorbedStr, 총취득원가는 $totalCostStr 로 계산됨. 모든 리스크를 원가표 숫자로 환산해 내는 치밀한 분석력이 안전한 낙찰을 견인함.",
                "[정밀 교차 판독 · 블라인드스팟 제거 리포트] ${auction.caseId} 등기 ${regTotal}건·문서 ${docCount}종·방문기록 ${visitCount}건을 4대 축으로 해부하여 매수자 승계분 $absorbedStr 및 실질 원가 $totalCostStr 를 검증 완료함. 겉으로 드러나지 않는 배당·명도 변수까지 사전에 잡아내는 예리한 분석 능력이 핵심 가치임.",
                "[사법 데이터 정밀 감사 · 입찰 안전망 구축] ${auction.caseId}의 등기 내역 ${regTotal}건, 법원 공부 ${docCount}종, 방문기록 ${visitCount}건을 교차해 인수 부담 $absorbedStr·총취득액 $totalCostStr 를 명확히 산정함. 단순 요약이 아닌 판례·현장 기반의 칼같은 분석 역량이 결국 수익과 원금을 함께 지켜줌."
            )[idx]
        }
    }
}
