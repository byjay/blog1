package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.OwnershipStatusCard
import com.example.model.PropertyType
import com.example.model.RegistryInfographicTemplate
import com.example.model.RegistryInfographicTemplateType
import com.example.model.RegistryNodeTone
import com.example.model.RegistrySection
import com.example.model.RegistryVisualNode

object RegistryInfographicEngine {

    fun buildTemplate(
        auction: AuctionCaseIntel,
        templateType: RegistryInfographicTemplateType = RegistryInfographicTemplateType.COMBINED_EXPERT_BOARD
    ): RegistryInfographicTemplate = buildSanitizedTemplate(
        auction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction),
        templateType = templateType
    )

    private fun buildSanitizedTemplate(
        auction: AuctionCaseIntel,
        templateType: RegistryInfographicTemplateType
    ): RegistryInfographicTemplate {
        val hasSeniorTenantRisk = auction.assumedRightsText.contains("선순위") && auction.assumedRightsText.contains("인수")
        val hasLienRisk = auction.assumedRightsText.contains("유치권")
        val claimMaxKrw = (auction.claimAmountKrw * 12L) / 10L
        val claimStr = QaVerificationEngine.formatKrwShort(auction.claimAmountKrw)
        val claimMaxStr = QaVerificationEngine.formatKrwShort(claimMaxKrw)
        val appraisalStr = QaVerificationEngine.formatKrwShort(auction.appraisalPriceKrw)
        val minBidStr = QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw)

        val isCorporateOwner = auction.propertyType == PropertyType.FACTORY_SPECIAL ||
            auction.possessionSummary.contains("법인")

        val ownershipCard = OwnershipStatusCard(
            currentOwnerLabel = if (isCorporateOwner) "주식회사 채무자 법인 (단독 소유)" else "채무자 겸 소유자 (단독 명의)",
            acquisitionCause = "매매 원인증서 등기 완료 · 현 경매 목적물 지분 100%",
            ownershipRiskNote = when {
                auction.propertyType == PropertyType.LAND_FARMLAND -> "공유자 우선매수신고 및 제시외 건물(비닐하우스) 매각 포함 여부 확인"
                hasLienRisk -> "공장저당법 제6조 기계기구 일괄 매각 목록 일치 여부 현장 대조 필수"
                else -> "소유권 이전과 동시에 기존 채무자·압류·경매개시 등기는 일괄 말소"
            },
            coOwnerOrShareNote = if (auction.propertyType == PropertyType.LAND_FARMLAND) "단독 또는 일괄매각 지분 점검 완료" else "지분 100% 전부 매각 (공유자 없음)"
        )

        val gapGuNodes = listOf(
            RegistryVisualNode(
                nodeId = "${auction.caseId}-gap-1",
                section = RegistrySection.GAP_GU,
                rankOrder = "갑구 1번",
                registeredDate = "선행 취득",
                title = "소유권 이전등기",
                holderOrCreditor = if (isCorporateOwner) "채무자 법인" else "현 소유자(채무자)",
                amountOrShare = "지분 1/1 (감정 $appraisalStr)",
                statusBadge = "매각으로 소유권 이전",
                plainExplanation = "현재 집·토지 주인의 이름표로, 잔금을 내면 낙찰자 명의로 깔끔하게 바뀝니다.",
                nodeTone = RegistryNodeTone.NEUTRAL_OWNERSHIP,
                isBaseRight = false,
                isAssumedRisk = false
            ),
            RegistryVisualNode(
                nodeId = "${auction.caseId}-gap-2",
                section = RegistrySection.GAP_GU,
                rankOrder = "갑구 2번",
                registeredDate = "기준일 후순위",
                title = "세무서·지자체 압류 / 가압류",
                holderOrCreditor = "조세채권자 · 일반채권자",
                amountOrShare = "법정기일·당해세 배당순서 대조",
                statusBadge = "배당 후 말소",
                plainExplanation = "기준 권리보다 늦게 들어온 압류는 낙찰자가 갚을 필요 없이 지워집니다(당해세는 배당금에서 공제).",
                nodeTone = RegistryNodeTone.EXTINGUISHED_SAFE,
                isBaseRight = false,
                isAssumedRisk = false
            ),
            RegistryVisualNode(
                nodeId = "${auction.caseId}-gap-3",
                section = RegistrySection.GAP_GU,
                rankOrder = "갑구 3번",
                registeredDate = "배당종기 ${auction.dividendDeadline}",
                title = "임의경매개시결정 등기",
                holderOrCreditor = "${auction.courtName} 경매계",
                amountOrShare = "청구금액 $claimStr",
                statusBadge = "낙찰 시 자동 소멸",
                plainExplanation = "경매가 시작됐다는 공식 알림 딱지로, 매각 완료 시 법원이 직권으로 지워줍니다.",
                nodeTone = RegistryNodeTone.EXTINGUISHED_SAFE,
                isBaseRight = false,
                isAssumedRisk = false
            )
        )

        val eulGuNodes = mutableListOf(
            RegistryVisualNode(
                nodeId = "${auction.caseId}-eul-1",
                section = RegistrySection.EUL_GU,
                rankOrder = "을구 1번",
                registeredDate = auction.baseRightDate,
                title = "1순위 근저당권 설정",
                holderOrCreditor = "1금융권 시중은행",
                amountOrShare = "채권최고액 $claimMaxStr",
                statusBadge = "말소기준권리",
                plainExplanation = "등기부를 가르는 기준선입니다. 이 날짜보다 늦은 빚과 권리는 낙찰 후 전부 사라집니다.",
                nodeTone = RegistryNodeTone.BASE_RIGHT_ANCHOR,
                isBaseRight = true,
                isAssumedRisk = false
            ),
            RegistryVisualNode(
                nodeId = "${auction.caseId}-eul-2",
                section = RegistrySection.EUL_GU,
                rankOrder = "을구 2번",
                registeredDate = "기준일 후순위",
                title = "후순위 근저당·질권 / 전세권",
                holderOrCreditor = "후순위 대부·금융기관",
                amountOrShare = "배당 잔액 범위 내 배분",
                statusBadge = "전액 소멸",
                plainExplanation = "말소기준권리 뒤에 줄 선 채권이므로 배당을 못 받아도 낙찰자에게 넘어오지 않습니다.",
                nodeTone = RegistryNodeTone.EXTINGUISHED_SAFE,
                isBaseRight = false,
                isAssumedRisk = false
            )
        )

        if (hasSeniorTenantRisk || hasLienRisk) {
            eulGuNodes += RegistryVisualNode(
                nodeId = "${auction.caseId}-eul-risk",
                section = RegistrySection.TENANT_COURT,
                rankOrder = "인수주의",
                registeredDate = if (hasSeniorTenantRisk) "기준일 선행 전입" else "현장 점유 신고",
                title = if (hasSeniorTenantRisk) "선순위 대항력 임차인 (미배당 위험)" else "현장 유치권 신고 건",
                holderOrCreditor = if (hasSeniorTenantRisk) "임차인 대항력 유지" else "시공·설비 유치권 신고자",
                amountOrShare = if (hasSeniorTenantRisk) "보증금 전액 인수 검토" else "점유개시일·공사대금 진위 확인",
                statusBadge = if (hasSeniorTenantRisk) "낙찰자 인수 주의" else "현장 진위 검증",
                plainExplanation = if (hasSeniorTenantRisk) {
                    "말소기준일보다 먼저 이사·전입하고 배당요구를 안 한 세입자 보증금은 낙찰자가 떠안아야 하므로 입찰가에서 꼭 빼야 합니다."
                } else {
                    "등기부에 안 나오지만 현장에 걸린 유치권 주장입니다. 기준일 이후 점유라면 대항력이 약해 현장 증거 확보가 핵심입니다."
                },
                nodeTone = RegistryNodeTone.ASSUMED_DANGER,
                isBaseRight = false,
                isAssumedRisk = true
            )
        }

        val headline = when (templateType) {
            RegistryInfographicTemplateType.COMBINED_EXPERT_BOARD ->
                "${auction.caseId} 등기부등본 갑구·을구·소유권 한눈에 보기"
            RegistryInfographicTemplateType.TIMELINE_CANCELLATION ->
                "${auction.caseId} 말소기준(${auction.baseRightDate}) 소멸·인수 판정 도식"
            RegistryInfographicTemplateType.OWNERSHIP_CHAIN_AND_GAPGU ->
                "${auction.caseId} 갑구 소유권 현황 & 압류·경매개시 해부"
            RegistryInfographicTemplateType.EULGU_MORTGAGE_WATERFALL ->
                "${auction.caseId} 을구 근저당($claimMaxStr) 배당·인수 검증"
        }

        val subHeadline = "최저가 $minBidStr (${auction.discountRatePct}%) · ${auction.propertyType.displayLabel} · 독자용 쉬운 등기부 인포그래픽"

        val cancellationBaselineSummary =
            "말소기준권리: ${auction.baseRightDate} 을구 1번 근저당 ($claimMaxStr) — 이 날짜 이후 등기·가압류·경매개시는 낙찰 시 전부 소멸"

        val buyerTakeoverSummary = when {
            hasSeniorTenantRisk -> "주의 판정: 선순위 임차인 대항력 보증금 인수 위험 존재 — 입찰가에서 보증금 전액 차감 필수"
            hasLienRisk -> "검증 판정: 유치권 신고 접수 — 말소기준(${auction.baseRightDate}) 이후 점유 여부 현장 사진으로 파훼 검증"
            else -> "안전 판정: 등기부상 낙찰자 추가 인수 권리 0건 (단, 공용관리비 체납 여부 입찰 전 관리실 확인)"
        }

        val readerChecklist = when (templateType) {
            RegistryInfographicTemplateType.COMBINED_EXPERT_BOARD -> listOf(
                "1단계 소유권(갑구): 현재 소유자 지분 100% 매각이며 경매개시결정 등기는 잔금 후 말소됩니다.",
                "2단계 기준선(을구): ${auction.baseRightDate} 1순위 근저당이 모든 권리를 지우는 기준선입니다.",
                "3단계 인수검증: ${if (hasSeniorTenantRisk || hasLienRisk) "기준선 밖 특수·선순위 리스크 금액을 입찰가에서 차감하세요." else "기준일 이후 권리는 소멸하므로 공용관리비·명도비만 준비하면 됩니다."}"
            )
            RegistryInfographicTemplateType.TIMELINE_CANCELLATION -> listOf(
                "기준일 이전: 소유권 이전등기 및 선순위 대항력 여부를 가장 먼저 대조합니다.",
                "기준일 당일(${auction.baseRightDate}): 을구 근저당 설정이 말소기준권리 역할을 수행합니다.",
                "기준일 이후: 후순위 근저당·세무 압류·경매개시결정은 법원 촉탁으로 깨끗이 말소됩니다."
            )
            RegistryInfographicTemplateType.OWNERSHIP_CHAIN_AND_GAPGU -> listOf(
                "소유자 현황: ${ownershipCard.currentOwnerLabel} (${ownershipCard.coOwnerOrShareNote}).",
                "갑구 처분제한: 압류 및 임의경매개시결정(청구 $claimStr)은 매각으로 일괄 말소됩니다.",
                "체크 포인트: ${ownershipCard.ownershipRiskNote}"
            )
            RegistryInfographicTemplateType.EULGU_MORTGAGE_WATERFALL -> listOf(
                "1순위 채권최고액: $claimMaxStr (원금 청구액 $claimStr 기준 배당 흡수 예상).",
                "후순위 채권: 최저가 $minBidStr 이상 구간에서 선순위 배당 후 남는 채권은 전액 소멸합니다.",
                "인수 결론: $buyerTakeoverSummary"
            )
        }

        return RegistryInfographicTemplate(
            caseId = auction.caseId,
            templateType = templateType,
            headline = headline,
            subHeadline = subHeadline,
            ownershipSummaryCard = ownershipCard,
            gapGuNodes = gapGuNodes,
            eulGuNodes = eulGuNodes,
            cancellationBaselineSummary = cancellationBaselineSummary,
            buyerTakeoverSummary = buyerTakeoverSummary,
            readerChecklist = readerChecklist,
            renderAspectRatio = "1200x800"
        )
    }
}
