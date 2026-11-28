package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.BlogPostTemplate
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.ServerAutomationConfig

data class TemplatePlaceholderToken(
    val token: String,
    val label: String
)

object BlogPostTemplateEngine {

    val SUPPORTED_PLACEHOLDERS: List<TemplatePlaceholderToken> = listOf(
        TemplatePlaceholderToken("{사건번호}", "사건번호"),
        TemplatePlaceholderToken("{지역}", "지역명"),
        TemplatePlaceholderToken("{물건유형}", "물건유형"),
        TemplatePlaceholderToken("{법원}", "관할법원"),
        TemplatePlaceholderToken("{주소}", "소재지"),
        TemplatePlaceholderToken("{감정가}", "감정가"),
        TemplatePlaceholderToken("{최저가}", "최저매각가"),
        TemplatePlaceholderToken("{할인율}", "최저가비율(%)"),
        TemplatePlaceholderToken("{말소기준권리}", "말소기준권리"),
        TemplatePlaceholderToken("{매각기일}", "매각기일"),
        TemplatePlaceholderToken("{배당요구종기}", "배당요구종기"),
        TemplatePlaceholderToken("{실거래중위가}", "실거래중위가"),
        TemplatePlaceholderToken("{3개년낙찰률}", "3개년낙찰률"),
        TemplatePlaceholderToken("{보증금}", "입찰보증금"),
        TemplatePlaceholderToken("{보수입찰가}", "보수적입찰가"),
        TemplatePlaceholderToken("{적정입찰가}", "적정입찰가"),
        TemplatePlaceholderToken("{공격입찰가}", "공격적입찰가"),
        TemplatePlaceholderToken("{점유현황}", "점유현황"),
        TemplatePlaceholderToken("{임차현황}", "임차현황"),
        TemplatePlaceholderToken("{인수권리}", "인수권리"),
        TemplatePlaceholderToken("{특수체크}", "유형별체크"),
        TemplatePlaceholderToken("{안전마진}", "시세안전마진"),
        TemplatePlaceholderToken("{LTV70자기자본}", "LTV70실투자금"),
        TemplatePlaceholderToken("{예상수익률}", "CapRate/ROE"),
        TemplatePlaceholderToken("{명도예상}", "명도난이도·기간"),
        TemplatePlaceholderToken("{사진가이드}", "임장사진5컷"),
        TemplatePlaceholderToken("{프로티저}", "딱찰PRO유도")
    )

    fun defaultBlogPostTemplates(): List<BlogPostTemplate> = listOf(
        BlogPostTemplate(
            id = "preset-apt-naver",
            name = "아파트·오피스텔 30년 베테랑 현장 에세이 템플릿",
            propertyType = PropertyType.APT_OFFICETEL,
            targetChannel = ChannelPlatform.NAVER,
            titleTemplate = "{지역} 아파트·오피스텔 경매 {사건번호} 최저가 {최저가}({할인율}%) 등기부·관리비 핵심 정리",
            bodyTemplate = """
오늘 실무 검토 테이블에 올린 사건은 {법원} {사건번호}({주소}) {물건유형} 매물입니다.
현장에서 30년 넘게 권리분석을 해오면서 가장 먼저 맞춰보는 핵심 숫자부터 차근히 짚어드립니다.

[경매 기본 제원]
최초 감정가 {감정가}에서 현재 최저매각가 {최저가}({할인율}% 구간)까지 내려온 상태이며, 입찰 당일 지참할 보증금은 {보증금}입니다.
등기부 말소기준권리는 {말소기준권리}, 배당요구종기는 {배당요구종기}로 기록되어 있습니다.

[시세·안전마진·낙찰가율 대조]
국토교통부 최근 실거래 중위가는 {실거래중위가} 선이고 해당 권역 3개년 평균 낙찰가율은 {3개년낙찰률}%를 기록 중입니다.
시세 대비 안전마진은 {안전마진} 구간으로 실거주와 투자 수요가 함께 받쳐주는 가격대입니다.

[등기부 갑구·을구 및 현황·체납 관리비]
집행관 현황조사서상 '{점유현황}', 임차 명세는 '{임차현황}'이며 인수 요약은 '{인수권리}'입니다.
대법원 2001다8677 전원합의체 판례에 따라 전유부분과 연체료는 제외되지만 공용부분 체납 관리비(3년 원금)는 승계되므로 입찰 전 관리사무소 확인이 필수입니다.
'{특수체크}' 항목까지 현장에서 발로 대조하면 잔금 뒤 불필요한 비용 누수를 막을 수 있습니다.

[3전략 입찰 밴드 · 현장 체크]
보수적 안전선은 {보수입찰가}, 평균 흐름 기준선은 {적정입찰가}, 적극 확보 상단은 {공격입찰가}로 산출됩니다.
LTV 70% 적용 시 실투자 자본({LTV70자기자본})과 명의별 세금을 미리 맞춘 뒤, {매각기일} 전날 변경 공고가 없는지 재확인하시기 바랍니다.
{프로티저}
            """.trimIndent(),
            tagsTemplate = "딱찰,{지역}아파트경매,{사건번호},아파트경매,권리분석,안전마진",
            isPreset = true,
            isActiveForGeneration = true,
            updatedAt = "2026-11-22 08:17"
        ),
        BlogPostTemplate(
            id = "preset-villa-naver",
            name = "빌라·다세대 선순위 임차인·보증금 해부 템플릿",
            propertyType = PropertyType.VILLA_MULTIFAMILY,
            targetChannel = ChannelPlatform.NAVER,
            titleTemplate = "{지역} 빌라·다세대 경매 {사건번호} 선순위 임차인·보증금 인수 집중 점검",
            bodyTemplate = """
{법원} {사건번호}({주소}) {물건유형} 사건을 선순위 임차인 대항력과 보증금 인수 리스크 중심으로 대조해 드립니다.

[등기부 및 매각 핵심 수치]
감정평가액은 {감정가}, 최저매각가는 {최저가}({할인율}% 구간)이며 입찰보증금은 {보증금}, 매각기일은 {매각기일}입니다.
말소기준권리는 {말소기준권리}, 배당요구종기는 {배당요구종기}로 확정되어 있습니다.

[선순위 임차인·최우선변제 판례 대조]
빌라·다가구 경매에서 가장 뼈아픈 실수는 말소기준권리({말소기준권리})보다 앞선 전입세대의 대항력을 놓치는 것입니다.
현황은 '{점유현황}', 임차인 권리는 '{임차현황}', 인수 요약은 '{인수권리}'입니다.
배당요구를 하지 않은 선순위 임차인이 있다면 보증금이 낙찰자 부담으로 남으므로 실투자 원가에서 즉시 차감해야 합니다.

[주변 실거래·공시지가·3전략 입찰선]
최근 실거래 중위가 {실거래중위가}, 권역 3개년 낙찰가율 {3개년낙찰률}%를 놓고 보면 보수 입찰가 {보수입찰가}, 적정 입찰가 {적정입찰가}, 공격 상한 {공격입찰가}로 정리됩니다.
'{특수체크}' 및 건축물대장 위반건축물 여부까지 확인한 뒤 안전마진이 남는 가격에만 응찰하시기 바랍니다.
{프로티저}
            """.trimIndent(),
            tagsTemplate = "딱찰,{지역}빌라경매,{사건번호},선순위임차인,전세사기예방,소액경매",
            isPreset = true,
            isActiveForGeneration = true,
            updatedAt = "2026-11-22 08:17"
        ),
        BlogPostTemplate(
            id = "preset-commercial-tistory",
            name = "상가·근린시설 수익률(CapRate)·세무 리포트 템플릿",
            propertyType = PropertyType.COMMERCIAL_RETAIL,
            targetChannel = ChannelPlatform.TISTORY,
            titleTemplate = "[{지역} 상가·근린 투자 리포트] {사건번호} CapRate·공실·세무 3안 정밀 분석",
            bodyTemplate = """
[{지역} {물건유형} 수익형 자산 분석] {법원} {사건번호}

1. 공시 가격 지표 및 상권 할인 포지션
소재지 {주소}에 위치한 {사건번호} 물건은 최초 감정가 {감정가} 대비 최저매각가 {최저가}({할인율}%)로 {매각기일} 매각이 진행됩니다.
등기부상 말소기준권리는 {말소기준권리}, 배당요구종기는 {배당요구종기}이며, 인근 실거래 중위값 {실거래중위가} 및 권역 3개년 평균 낙찰가율 {3개년낙찰률}%와 대조 시 초기 매입 가격 완충력이 확보된 구간입니다.

2. 상임법 권리관계 및 장기공실 CAPEX 점검
매각물건명세서상 인수 요약은 '{인수권리}'이며 현장 점유는 '{점유현황}', 임차 현황은 '{임차현황}'입니다.
상가 경매 핵심 검토 사항('{특수체크}')을 반영하여 상가임대차보호법 환산보증금 초과 여부, 묵시적 갱신, 그리고 장기 공실에 따른 체납 공용관리비 승계분을 매입 원가에 가산해야 실질 순수익률(CapRate)이 왜곡되지 않습니다.

3. 3전략 입찰 밴드 및 HBU·세무 3안 비교
계량 분석상 보수적 입찰가는 {보수입찰가}, 적정 균형가는 {적정입찰가}, 공격적 상한선은 {공격입찰가}입니다.
경락잔금대출 레버리지 활용 시 자기자본 수익률(ROE)을 극대화하려면 개인 단독명의, 부부 공동명의, 법인 명의(과세표준 9~19% 및 비용 처리) 중 보유 기간과 임대 소득 구간에 가장 유리한 명의를 사전에 확정해야 합니다.
입찰보증금 {보증금} 준비 및 반경 500m 상권 공실률 실사를 거쳐 목표 수익률을 상회하는 구간에서만 응찰하시기 바랍니다.
            """.trimIndent(),
            tagsTemplate = "딱찰,{지역}상가경매,{사건번호},상가경매,수익률분석,CapRate",
            isPreset = true,
            isActiveForGeneration = true,
            updatedAt = "2026-11-22 08:17"
        ),
        BlogPostTemplate(
            id = "preset-land-cloudflare",
            name = "토지·임야·농지 GEO/FAQ 브리핑 템플릿",
            propertyType = PropertyType.LAND_FARMLAND,
            targetChannel = ChannelPlatform.CLOUDFLARE,
            titleTemplate = "[GEO 브리핑] {사건번호} {지역} 토지·농지 경매 · 맹지·농취증·권리분석",
            bodyTemplate = """
[결론 요약] {법원} {사건번호}({주소}, {물건유형})은 감정가 {감정가} 대비 {할인율}% 구간인 {최저가}에 {매각기일} 매각 예정인 사건입니다.
등기부상 말소기준권리는 {말소기준권리}(배당요구종기 {배당요구종기})이며, 입찰보증금은 {보증금}입니다.

[법원 4대 공부 및 토지 인허가 대조]
- 매각물건명세서 인수 요약: {인수권리}
- 현황조사 및 이용 상태: {점유현황}
- 임차권·지상권 현황: {임차현황}
- 토지·농지 특수 체크: {특수체크}

[시세 비교 및 3전략 입찰가]
최근 실거래 중위가 {실거래중위가} 및 권역 3개년 낙찰률 {3개년낙찰률}%를 반영한 입찰 밴드는 보수 {보수입찰가} · 적정 {적정입찰가} · 공격 {공격입찰가}입니다.

[자주 묻는 질문 FAQ]
Q1. {사건번호} 농지취득자격증명원(농취증) 미제출 시 어떻게 되나요?
A1. 농지(전·답·과수원)에 해당하는 경우 매각결정기일까지 관할 지자체의 농취증을 제출하지 못하면 매각 불허가와 함께 입찰보증금 {보증금} 전액이 몰수됩니다.

Q2. 맹지 여부와 법정지상권·분묘기지권은 어떻게 확인하나요?
A2. 지적도와 건축행정시스템으로 4m 이상 도로 접면 여부를 확인하고, 현장 임장을 통해 제시외 수목·비닐하우스·분묘 소재 유무를 반드시 실측 대조해야 합니다.
            """.trimIndent(),
            tagsTemplate = "딱찰,{지역}토지경매,{사건번호},농지취득자격증명,토지경매,맹지확인",
            isPreset = true,
            isActiveForGeneration = true,
            updatedAt = "2026-11-22 08:17"
        ),
        BlogPostTemplate(
            id = "preset-factory-tistory",
            name = "공장·창고·특수물건 기계기구·유치권 리포트 템플릿",
            propertyType = PropertyType.FACTORY_SPECIAL,
            targetChannel = ChannelPlatform.TISTORY,
            titleTemplate = "[{지역} 공장·특수 경매] {사건번호} 공장저당 기계기구·유치권·전력 실사 리포트",
            bodyTemplate = """
[{지역} {물건유형} 기술·권리 정밀 리포트] {법원} {사건번호}

1. 매각 개요 및 감정평가 구조
{주소} 소재 {사건번호} 사건은 최초 감정가 {감정가}에서 최저매각가 {최저가}({할인율}%)로 {매각기일} 입찰이 진행됩니다.
말소기준권리는 {말소기준권리}, 배당요구종기는 {배당요구종기}이며 인근 실거래 기준 {실거래중위가} 및 권역 3개년 낙찰률 {3개년낙찰률}% 데이터를 참고해 적정 매입 단가를 역산해야 합니다.

2. 공장저당법 제6조 기계기구 및 유치권 파훼 검증
공부상 점유 현황은 '{점유현황}', 신고 권리는 '{임차현황}', 인수 검토 항목은 '{인수권리}'로 파악됩니다.
특히 공장·창고 물건은 '{특수체크}' 점검과 함께 감정평가서에 포함된 기계기구·호이스트·설비 목록의 현장 실물 존재 여부를 일일이 대조해야 멸실 손실을 방지할 수 있습니다.
유치권 신고가 접수된 경우에는 점유 개시 시점이 말소기준권리({말소기준권리})보다 선행하는지, 실제 공사대금 채권 증빙이 존재하는지를 검증하여 대항력 유무를 가려내야 합니다.

3. 3전략 입찰 밴드 및 산업단지 입주 요건
모델 산출 기준 보수적 입찰가는 {보수입찰가}, 적정 입찰가는 {적정입찰가}, 공격적 입찰가는 {공격입찰가}입니다.
계약전력(kW), 40ft 대형 화물차 진입 도로폭, 산업단지 관리공단 입주 업종 코드 적합 여부를 사전 승인받은 뒤 보증금 {보증금}을 준비해 응찰하시기 바랍니다.
            """.trimIndent(),
            tagsTemplate = "딱찰,{지역}공장경매,{사건번호},공장경매,유치권분석,기계기구",
            isPreset = true,
            isActiveForGeneration = true,
            updatedAt = "2026-11-22 08:17"
        )
    )

    fun renderText(
        rawTemplate: String,
        auction: AuctionCaseIntel,
        proFunnel: ProFunnelConfig = ProFunnelConfig()
    ): String {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction)
        val region = safeAuction.address.split(" ").take(2).joinToString(" ")
        val proReport = ProAnalysisAndPhotoGuideEngine.buildProReport(safeAuction)
        val appraisalStr = QaVerificationEngine.formatKrwShort(safeAuction.appraisalPriceKrw)
        val minBidStr = QaVerificationEngine.formatKrwShort(safeAuction.minBidPriceKrw)
        val realTradeStr = QaVerificationEngine.formatKrwShort(safeAuction.recentRealTradeMedianKrw)
        val depositStr = QaVerificationEngine.formatKrwShort((safeAuction.minBidPriceKrw * safeAuction.depositRatePct) / 100L)
        val consStr = QaVerificationEngine.formatKrwShort(safeAuction.conservativeBidKrw)
        val balStr = QaVerificationEngine.formatKrwShort(safeAuction.balancedBidKrw)
        val aggStr = QaVerificationEngine.formatKrwShort(safeAuction.aggressiveBidKrw)
        val marginStr = QaVerificationEngine.formatKrwShort(proReport.safetyMarginKrw)
        val equityStr = QaVerificationEngine.formatKrwShort(proReport.netEquityRequiredKrw)
        val photoGuideText = proReport.photoGuideShots.joinToString("\n") { s ->
            "- [사진 ${s.shotNumber}] ${s.targetTitle}: ${s.checkPoint} (주의: ${s.redFlagSignal})"
        }

        val replacements = mapOf(
            "{사건번호}" to safeAuction.caseId,
            "{지역}" to region,
            "{물건유형}" to safeAuction.propertyType.displayLabel,
            "{법원}" to safeAuction.courtName,
            "{주소}" to safeAuction.address,
            "{감정가}" to appraisalStr,
            "{최저가}" to minBidStr,
            "{할인율}" to safeAuction.discountRatePct.toString(),
            "{말소기준권리}" to safeAuction.baseRightDate,
            "{매각기일}" to safeAuction.auctionDate,
            "{배당요구종기}" to safeAuction.dividendDeadline,
            "{실거래중위가}" to realTradeStr,
            "{3개년낙찰률}" to safeAuction.stats3yRatePct.toString(),
            "{보증금}" to depositStr,
            "{보수입찰가}" to consStr,
            "{적정입찰가}" to balStr,
            "{공격입찰가}" to aggStr,
            "{점유현황}" to safeAuction.possessionSummary,
            "{임차현황}" to safeAuction.tenantSummary,
            "{인수권리}" to safeAuction.assumedRightsText,
            "{특수체크}" to safeAuction.specialCheckNotice,
            "{안전마진}" to "$marginStr (${proReport.safetyMarginPct}%)",
            "{LTV70자기자본}" to equityStr,
            "{예상수익률}" to "CapRate ${proReport.estimatedCapRatePct}% / 12개월 ROE ${proReport.estimated12mRoiPct}%",
            "{명도예상}" to "${proReport.evictionRiskLevel} (약 ${proReport.estimatedEvictionDays}일)",
            "{사진가이드}" to photoGuideText,
            "{프로티저}" to proFunnel.teaserHeadline
        )

        var output = rawTemplate
        replacements.forEach { (token, value) ->
            output = output.replace(token, value)
        }
        return PrivacySanitizerEngine.sanitizeForPublicBlog(output).trim()
    }

    fun applyTemplateToVariant(
        baseVariant: ChannelVariantContent,
        template: BlogPostTemplate,
        auction: AuctionCaseIntel,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig = ProFunnelConfig()
    ): ChannelVariantContent {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(auction)
        val renderedTitle = renderText(template.titleTemplate, safeAuction, proFunnel).ifBlank { baseVariant.title }
        var renderedBody = renderText(template.bodyTemplate, safeAuction, proFunnel).ifBlank { baseVariant.bodyText }

        // Enforce Naver anti-low-quality external URL rule if disabled
        if (template.targetChannel == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
            renderedBody = renderedBody.replace(Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE), "[프로필 링크 참조]")
        }

        // Ensure every generated post appends the channel-safe PRO signup conversion funnel if enabled
        val needsFunnelTail = proFunnel.funnelEnabled &&
            (!renderedBody.contains("딱찰") || (!renderedBody.contains("회원가입") && !renderedBody.contains("가입")))
        if (needsFunnelTail) {
            val funnelTail = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelText(
                auction = safeAuction,
                platform = template.targetChannel,
                config = config,
                funnel = proFunnel
            )
            renderedBody = "$renderedBody\n$funnelTail".trim()
        }
        val defended = QualityDefenseEngine.defendText(
            rawTitle = renderedTitle,
            rawBody = renderedBody,
            channel = template.targetChannel,
            auction = safeAuction,
            naverSearchKeyword = proFunnel.naverSearchKeyword
        )
        val cleanTitle = defended.sanitizedTitle.ifBlank { renderedTitle }
        renderedBody = defended.sanitizedBody

        val renderedTags = renderText(template.tagsTemplate, safeAuction, proFunnel)
            .split(",")
            .map { it.trim().removePrefix("#") }
            .filter { it.isNotBlank() }
            .ifEmpty { baseVariant.tags }

        val funnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(
            auction = safeAuction,
            platform = template.targetChannel,
            config = config,
            funnel = proFunnel
        )
        val renderedHtml = when (template.targetChannel) {
            ChannelPlatform.NAVER -> "<div class='se-viewer'>${renderedBody.replace("\n", "<br/>")}</div>$funnelHtml"
            ChannelPlatform.TISTORY -> """
<article>
  <h1>$renderedTitle</h1>
  <p>${renderedBody.replace("\n", "<br/>")}</p>
  $funnelHtml
</article>
            """.trimIndent()
            ChannelPlatform.CLOUDFLARE -> """
<article><h1>$renderedTitle</h1><p>${renderedBody.replace("\n", "<br/>")}</p></article>
$funnelHtml
<script type="application/ld+json">${baseVariant.jsonLdSchema}</script>
            """.trimIndent()
        }

        val pinnedComment = if (template.targetChannel == ChannelPlatform.NAVER) {
            PrivacySanitizerEngine.sanitizeForPublicBlog(
                ProAnalysisAndPhotoGuideEngine.buildNaverPinnedComment(safeAuction, proFunnel)
            )
        } else ""

        val finalHtml = when (template.targetChannel) {
            ChannelPlatform.NAVER -> "<div class='se-viewer'>${renderedBody.replace("\n", "<br/>")}</div>$funnelHtml"
            ChannelPlatform.TISTORY -> """
<article>
  <h1>$cleanTitle</h1>
  <p>${renderedBody.replace("\n", "<br/>")}</p>
  $funnelHtml
</article>
            """.trimIndent()
            ChannelPlatform.CLOUDFLARE -> """
<article><h1>$cleanTitle</h1><p>${renderedBody.replace("\n", "<br/>")}</p></article>
$funnelHtml
<script type="application/ld+json">${baseVariant.jsonLdSchema}</script>
            """.trimIndent()
        }

        return baseVariant.copy(
            title = cleanTitle,
            bodyText = renderedBody,
            bodyHtml = finalHtml,
            tags = renderedTags,
            pinnedCommentSnippet = pinnedComment
        )
    }

    fun sampleAuctionForType(type: PropertyType): AuctionCaseIntel = AuctionCaseIntel(
        dedupKey = "template-preview-${type.name.lowercase()}",
        caseId = "사건번호-미리보기",
        courtName = "관할지방법원",
        address = "템플릿 치환 미리보기 소재지 (${type.displayLabel})",
        propertyType = type,
        appraisalPriceKrw = 350_000_000L,
        minBidPriceKrw = 245_000_000L,
        recentRealTradeMedianKrw = 365_000_000L,
        stats3yRatePct = 82.4,
        bidCount = 1,
        discountRatePct = 70,
        depositRatePct = 10,
        clicksLast7d = 0,
        baseRightDate = "기준일-미리보기",
        dividendDeadline = "배당종기-미리보기",
        claimAmountKrw = 180_000_000L,
        assumedRightsText = "템플릿 권리분석 치환 영역",
        possessionSummary = "템플릿 점유현황 치환 영역",
        tenantSummary = "템플릿 임차현황 치환 영역",
        conservativeBidKrw = 252_000_000L,
        balancedBidKrw = 274_000_000L,
        aggressiveBidKrw = 291_000_000L,
        auctionDate = "매각기일-미리보기",
        specialCheckNotice = "${type.displayLabel} 전용 특수 점검 항목"
    )
}
