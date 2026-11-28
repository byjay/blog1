package com.example.engine

import com.example.model.ArticleEvaluationResult
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.CourtFullArchiveRecord
import com.example.model.GeneratedPostSlot
import com.example.model.HtmlBatchBundleState
import com.example.model.HtmlGeneratedPostItem
import com.example.model.RegistryInfographicTemplate
import com.example.model.ServerAutomationConfig

object HtmlExportEngine {

    private val externalUrlRegex = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    fun renderStandaloneHtmlDocument(
        slot: GeneratedPostSlot,
        variant: ChannelVariantContent,
        config: ServerAutomationConfig
    ): String {
        val case = slot.auctionCase
        val safeTitle = escapeHtml(variant.title)
        val safeMetaDesc = escapeHtml(variant.metaDescription)
        val tagsKeyword = escapeHtml(variant.tags.joinToString(", "))
        val appraisalText = QaVerificationEngine.formatKrwShort(case.appraisalPriceKrw)
        val minBidText = QaVerificationEngine.formatKrwShort(case.minBidPriceKrw)
        val realTradeText = QaVerificationEngine.formatKrwShort(case.recentRealTradeMedianKrw)
        val cleanBodyHtml = sanitizeChannelBodyHtml(variant, config)
        val faqSectionHtml = buildGeoFaqHtmlSection(slot)

        val archive = CourtEvidenceRecorder.recordFullArchiveForSlot(slot)
        val registryTemplate = RegistryInfographicEngine.buildTemplate(case)
        val allowImgUrl = variant.platform != ChannelPlatform.NAVER || config.enableExternalLinkNaver

        val sharpSection = buildSharpAnalysisHtmlSection(archive)
        val photosSection = buildCourtPhotosHtmlSection(archive, allowExternalImg = allowImgUrl, localPhotoDataUris = emptyMap())
        val registrySection = buildRegistryHtmlSection(archive, registryTemplate)
        val visitSection = buildVisitLogsHtmlSection(archive)

        val rawHtml = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ko\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\" />")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            appendLine("  <title>$safeTitle</title>")
            appendLine("  <meta name=\"description\" content=\"$safeMetaDesc\" />")
            appendLine("  <meta name=\"keywords\" content=\"$tagsKeyword\" />")
            if (variant.jsonLdSchema.isNotBlank() && (variant.platform != ChannelPlatform.NAVER || config.enableExternalLinkNaver)) {
                appendLine("  <script type=\"application/ld+json\">")
                appendLine("  ${variant.jsonLdSchema.trim()}")
                appendLine("  </script>")
            }
            appendLine("  <style>")
            appendLine("    body { font-family: -apple-system, BlinkMacSystemFont, 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; line-height: 1.72; color: #1f2937; max-width: 780px; margin: 24px auto; padding: 0 18px; background: #ffffff; }")
            appendLine("    h1 { font-size: 1.5rem; color: #0f172a; border-bottom: 2px solid #10b981; padding-bottom: 10px; }")
            appendLine("    h2 { font-size: 1.15rem; color: #0f172a; margin-top: 26px; border-left: 4px solid #10b981; padding-left: 10px; }")
            appendLine("    .meta-summary { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 10px; padding: 14px 16px; margin: 16px 0; }")
            appendLine("    table.metrics { width: 100%; border-collapse: collapse; margin: 14px 0; font-size: 0.92rem; }")
            appendLine("    table.metrics th, table.metrics td { border: 1px solid #cbd5e1; padding: 9px 12px; text-align: left; vertical-align: top; }")
            appendLine("    table.metrics th { background: #ecfdf5; color: #065f46; width: 28%; }")
            appendLine("    .photo-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin: 14px 0; }")
            appendLine("    .photo-card { border: 1px solid #cbd5e1; border-radius: 10px; padding: 10px; background: #f8fafc; }")
            appendLine("    .photo-card img { width: 100%; border-radius: 8px; max-height: 220px; object-fit: cover; margin-bottom: 8px; }")
            appendLine("    .faq-geo { background: #f0f9ff; border-left: 4px solid #0284c7; padding: 14px 16px; margin-top: 22px; border-radius: 6px; }")
            appendLine("    .tags { margin-top: 22px; color: #047857; font-weight: 600; font-size: 0.92rem; }")
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <article data-case-id=\"${escapeHtml(case.caseId)}\" data-channel=\"${variant.platform.code}\">")
            appendLine("    <h1>$safeTitle</h1>")
            appendLine("    <div class=\"meta-summary\">")
            appendLine("      <strong>${escapeHtml(case.courtName)} · ${escapeHtml(case.caseId)}</strong> | ${escapeHtml(case.propertyType.displayLabel)}<br />")
            appendLine("      <span>소재지: ${escapeHtml(case.address)}</span>")
            appendLine("    </div>")
            appendLine("    <table class=\"metrics\">")
            appendLine("      <tr><th>감정가</th><td>$appraisalText</td></tr>")
            appendLine("      <tr><th>최저매각가격</th><td>$minBidText (감정가 대비 ${case.discountRatePct}% · 유찰 ${case.bidCount}회)</td></tr>")
            appendLine("      <tr><th>최근 실거래 중위가</th><td>$realTradeText (3개년 낙찰가율 ${case.stats3yRatePct}%)</td></tr>")
            appendLine("      <tr><th>말소기준권리 / 기일</th><td>${escapeHtml(case.baseRightDate)} / 매각기일 ${escapeHtml(case.auctionDate)}</td></tr>")
            appendLine("      <tr><th>점유 · 임차 현황</th><td>${escapeHtml(case.possessionSummary)} · ${escapeHtml(case.tenantSummary)}</td></tr>")
            appendLine("      <tr><th>인수 및 특수 체크</th><td>${escapeHtml(case.assumedRightsText)} (${escapeHtml(case.specialCheckNotice)})</td></tr>")
            appendLine("    </table>")
            appendLine("    <section class=\"post-body\">")
            appendLine("      $cleanBodyHtml")
            appendLine("    </section>")
            if (sharpSection.isNotBlank()) {
                appendLine(sharpSection)
            }
            appendLine(photosSection)
            appendLine(registrySection)
            appendLine(visitSection)
            if (faqSectionHtml.isNotBlank()) {
                appendLine(faqSectionHtml)
            }
            if (variant.pinnedCommentSnippet.isNotBlank()) {
                appendLine("    <aside class=\"meta-summary\"><strong>고정댓글 가이드:</strong> ${escapeHtml(variant.pinnedCommentSnippet)}</aside>")
            }
            appendLine("    <p class=\"tags\">${variant.tags.joinToString(" ") { "#${escapeHtml(it.trimStart('#'))}" }}</p>")
            appendLine("  </article>")
            appendLine("</body>")
            appendLine("</html>")
        }.trim()

        return if (variant.platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
            rawHtml.replace(externalUrlRegex, "[프로필·고정댓글 확인]")
        } else {
            rawHtml
        }
    }

    fun renderArticleBodySnippet(
        slot: GeneratedPostSlot,
        variant: ChannelVariantContent,
        config: ServerAutomationConfig
    ): String {
        val case = slot.auctionCase
        val safeTitle = escapeHtml(variant.title)
        val appraisalText = QaVerificationEngine.formatKrwShort(case.appraisalPriceKrw)
        val minBidText = QaVerificationEngine.formatKrwShort(case.minBidPriceKrw)
        val realTradeText = QaVerificationEngine.formatKrwShort(case.recentRealTradeMedianKrw)
        val cleanBodyHtml = sanitizeChannelBodyHtml(variant, config)
        val faqSectionHtml = buildGeoFaqHtmlSection(slot)

        val archive = CourtEvidenceRecorder.recordFullArchiveForSlot(slot)
        val registryTemplate = RegistryInfographicEngine.buildTemplate(case)
        val allowImgUrl = variant.platform != ChannelPlatform.NAVER || config.enableExternalLinkNaver
        val sharpSection = buildSharpAnalysisHtmlSection(archive)
        val photosSection = buildCourtPhotosHtmlSection(archive, allowExternalImg = allowImgUrl, localPhotoDataUris = emptyMap())
        val registrySection = buildRegistryHtmlSection(archive, registryTemplate)
        val visitSection = buildVisitLogsHtmlSection(archive)

        val snippet = buildString {
            appendLine("<article data-case-id=\"${escapeHtml(case.caseId)}\" data-channel=\"${variant.platform.code}\">")
            appendLine("  <h1>$safeTitle</h1>")
            appendLine("  <div style=\"background:#f8fafc;border:1px solid #cbd5e1;border-radius:10px;padding:14px 16px;margin:16px 0;\">")
            appendLine("    <strong>${escapeHtml(case.courtName)} · ${escapeHtml(case.caseId)}</strong> | ${escapeHtml(case.propertyType.displayLabel)}<br />")
            appendLine("    <span>소재지: ${escapeHtml(case.address)}</span>")
            appendLine("  </div>")
            appendLine("  <table style=\"width:100%;border-collapse:collapse;margin:16px 0;font-size:0.95rem;\">")
            appendLine("    <tr><th style=\"border:1px solid #cbd5e1;padding:9px 12px;text-align:left;background:#ecfdf5;color:#065f46;width:32%;\">감정가</th><td style=\"border:1px solid #cbd5e1;padding:9px 12px;\">$appraisalText</td></tr>")
            appendLine("    <tr><th style=\"border:1px solid #cbd5e1;padding:9px 12px;text-align:left;background:#ecfdf5;color:#065f46;\">최저매각가격</th><td style=\"border:1px solid #cbd5e1;padding:9px 12px;\">$minBidText (감정가 대비 ${case.discountRatePct}% · 유찰 ${case.bidCount}회)</td></tr>")
            appendLine("    <tr><th style=\"border:1px solid #cbd5e1;padding:9px 12px;text-align:left;background:#ecfdf5;color:#065f46;\">최근 실거래 중위가</th><td style=\"border:1px solid #cbd5e1;padding:9px 12px;\">$realTradeText (3개년 낙찰가율 ${case.stats3yRatePct}%)</td></tr>")
            appendLine("    <tr><th style=\"border:1px solid #cbd5e1;padding:9px 12px;text-align:left;background:#ecfdf5;color:#065f46;\">말소기준권리 / 기일</th><td style=\"border:1px solid #cbd5e1;padding:9px 12px;\">${escapeHtml(case.baseRightDate)} / 매각기일 ${escapeHtml(case.auctionDate)}</td></tr>")
            appendLine("    <tr><th style=\"border:1px solid #cbd5e1;padding:9px 12px;text-align:left;background:#ecfdf5;color:#065f46;\">점유 · 임차 현황</th><td style=\"border:1px solid #cbd5e1;padding:9px 12px;\">${escapeHtml(case.possessionSummary)} · ${escapeHtml(case.tenantSummary)}</td></tr>")
            appendLine("    <tr><th style=\"border:1px solid #cbd5e1;padding:9px 12px;text-align:left;background:#ecfdf5;color:#065f46;\">인수 및 특수 체크</th><td style=\"border:1px solid #cbd5e1;padding:9px 12px;\">${escapeHtml(case.assumedRightsText)} (${escapeHtml(case.specialCheckNotice)})</td></tr>")
            appendLine("  </table>")
            appendLine("  <section class=\"post-body\">")
            appendLine("    $cleanBodyHtml")
            appendLine("  </section>")
            if (sharpSection.isNotBlank()) {
                appendLine(sharpSection)
            }
            appendLine(photosSection)
            appendLine(registrySection)
            appendLine(visitSection)
            if (faqSectionHtml.isNotBlank()) {
                appendLine(faqSectionHtml)
            }
            if (variant.pinnedCommentSnippet.isNotBlank()) {
                appendLine("  <aside style=\"background:#f8fafc;border:1px solid #cbd5e1;border-radius:10px;padding:12px 14px;margin:14px 0;\"><strong>고정댓글 가이드:</strong> ${escapeHtml(variant.pinnedCommentSnippet)}</aside>")
            }
            appendLine("  <p style=\"margin-top:20px;color:#047857;font-weight:600;\">${variant.tags.joinToString(" ") { "#${escapeHtml(it.trimStart('#'))}" }}</p>")
            appendLine("</article>")
        }.trim()

        return if (variant.platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
            snippet.replace(externalUrlRegex, "[프로필·고정댓글 확인]")
        } else {
            snippet
        }
    }

    /**
     * Produces a rich, readable HTML document specifically for in-app Android WebView previewing
     * so users can see exactly what their formatted article (including local/server photos,
     * registry gapGu/eulGu table, and visit logs) looks like before registering.
     */
    fun renderWebViewPreviewHtml(
        slot: GeneratedPostSlot,
        variant: ChannelVariantContent,
        config: ServerAutomationConfig,
        evaluation: ArticleEvaluationResult? = null,
        localPhotoDataUris: Map<Int, String> = emptyMap()
    ): String {
        val case = slot.auctionCase
        val eval = evaluation ?: Article30PointEvaluator.evaluate(slot, config)
        val archive = CourtEvidenceRecorder.recordFullArchiveForSlot(slot)
        val registryTemplate = RegistryInfographicEngine.buildTemplate(case)
        val appraisalText = QaVerificationEngine.formatKrwShort(case.appraisalPriceKrw)
        val minBidText = QaVerificationEngine.formatKrwShort(case.minBidPriceKrw)
        val realTradeText = QaVerificationEngine.formatKrwShort(case.recentRealTradeMedianKrw)
        val cleanBodyHtml = sanitizeChannelBodyHtml(variant, config)
        val faqSectionHtml = buildGeoFaqHtmlSection(slot)

        val channelAccent = when (variant.platform) {
            ChannelPlatform.NAVER -> "#03C75A"
            ChannelPlatform.TISTORY -> "#F59E0B"
            ChannelPlatform.CLOUDFLARE -> "#38BDF8"
        }
        val scoreColor = if (eval.eligibleForRegistration) "#059669" else "#D97706"
        val sharpSection = buildSharpAnalysisHtmlSection(archive)
        val photosSection = buildCourtPhotosHtmlSection(
            archive = archive,
            allowExternalImg = true,
            localPhotoDataUris = localPhotoDataUris
        )
        val registrySection = buildRegistryHtmlSection(archive, registryTemplate)
        val visitSection = buildVisitLogsHtmlSection(archive)

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ko\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\" />")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            appendLine("  <title>${escapeHtml(variant.title)}</title>")
            appendLine("  <style>")
            appendLine("    * { box-sizing: border-box; }")
            appendLine("    body { font-family: -apple-system, 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; margin: 0; padding: 14px; background: #f1f5f9; color: #0f172a; line-height: 1.68; font-size: 15px; }")
            appendLine("    .blog-card { background: #ffffff; border-radius: 14px; padding: 18px 16px; border: 1px solid #cbd5e1; box-shadow: 0 2px 10px rgba(15,23,42,0.06); }")
            appendLine("    .top-badges { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }")
            appendLine("    .badge { display: inline-block; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 999px; color: #fff; background: $channelAccent; }")
            appendLine("    .badge-score { background: $scoreColor; }")
            appendLine("    h1 { font-size: 1.32rem; line-height: 1.4; margin: 6px 0 12px; color: #0f172a; border-bottom: 2px solid $channelAccent; padding-bottom: 8px; }")
            appendLine("    h2 { font-size: 1.08rem; margin: 22px 0 10px; color: #0f172a; border-left: 4px solid $channelAccent; padding-left: 10px; }")
            appendLine("    .meta-summary { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; margin: 12px 0; font-size: 0.92rem; }")
            appendLine("    table.metrics { width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 0.86rem; }")
            appendLine("    table.metrics th, table.metrics td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: left; vertical-align: top; }")
            appendLine("    table.metrics th { background: #ecfdf5; color: #065f46; width: 32%; }")
            appendLine("    .photo-grid { display: grid; grid-template-columns: 1fr; gap: 10px; margin: 12px 0; }")
            appendLine("    .photo-card { border: 1px solid #cbd5e1; border-radius: 10px; padding: 10px; background: #f8fafc; }")
            appendLine("    .photo-card img { width: 100%; border-radius: 8px; max-height: 210px; object-fit: cover; margin-bottom: 8px; display: block; }")
            appendLine("    .photo-placeholder { background: linear-gradient(135deg,#0f172a,#1e293b); color: #e2e8f0; border-radius: 8px; padding: 14px 12px; margin-bottom: 8px; font-size: 0.85rem; }")
            appendLine("    .post-body p { margin: 10px 0; }")
            appendLine("    .faq-geo { background: #f0f9ff; border-left: 4px solid #0284c7; padding: 12px 14px; margin-top: 18px; border-radius: 8px; font-size: 0.92rem; }")
            appendLine("    .tags { margin-top: 18px; color: #047857; font-weight: 700; font-size: 0.9rem; word-break: keep-all; }")
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <article class=\"blog-card\">")
            appendLine("    <div class=\"top-badges\">")
            appendLine("      <span class=\"badge\">${escapeHtml(variant.platform.displayName)} 미리보기</span>")
            appendLine("      <span class=\"badge badge-score\">품질 ${eval.totalScore}/100점 (${eval.passedCount}/30)</span>")
            appendLine("      <span class=\"badge\" style=\"background:#334155;\">사진 ${archive.photos.size} · 등기 ${archive.registryGapGu.size + archive.registryEulGu.size} · 방문 ${archive.visitRecords.size}</span>")
            appendLine("    </div>")
            appendLine("    <h1>${escapeHtml(variant.title)}</h1>")
            appendLine("    <div class=\"meta-summary\">")
            appendLine("      <strong>${escapeHtml(case.courtName)} · ${escapeHtml(case.caseId)}</strong> (${escapeHtml(case.propertyType.displayLabel)})<br />")
            appendLine("      <span>소재지: ${escapeHtml(case.address)}</span>")
            appendLine("    </div>")
            appendLine("    <table class=\"metrics\">")
            appendLine("      <tr><th>감정가 / 최저가</th><td>$appraisalText → <strong>$minBidText</strong> (${case.discountRatePct}% · 유찰 ${case.bidCount}회)</td></tr>")
            appendLine("      <tr><th>실거래 중위 / 3년율</th><td>$realTradeText / ${case.stats3yRatePct}%</td></tr>")
            appendLine("      <tr><th>말소기준 / 매각기일</th><td>${escapeHtml(case.baseRightDate)} / ${escapeHtml(case.auctionDate)}</td></tr>")
            appendLine("      <tr><th>점유 · 임차 현황</th><td>${escapeHtml(case.possessionSummary)} · ${escapeHtml(case.tenantSummary)}</td></tr>")
            appendLine("    </table>")
            appendLine("    <section class=\"post-body\">$cleanBodyHtml</section>")
            if (sharpSection.isNotBlank()) {
                appendLine(sharpSection)
            }
            appendLine(photosSection)
            appendLine(registrySection)
            appendLine(visitSection)
            if (faqSectionHtml.isNotBlank()) {
                appendLine(faqSectionHtml)
            }
            if (variant.pinnedCommentSnippet.isNotBlank()) {
                appendLine("    <aside class=\"meta-summary\"><strong>고정댓글 가이드:</strong> ${escapeHtml(variant.pinnedCommentSnippet)}</aside>")
            }
            appendLine("    <p class=\"tags\">${variant.tags.joinToString(" ") { "#${escapeHtml(it.trimStart('#'))}" }}</p>")
            appendLine("  </article>")
            appendLine("</body>")
            appendLine("</html>")
        }.trim()
    }

    fun buildThreePureHtmlBatch(
        slots: List<GeneratedPostSlot>,
        threeCasesMode: Boolean,
        preferredChannel: ChannelPlatform,
        config: ServerAutomationConfig,
        nowText: String
    ): HtmlBatchBundleState {
        val items = if (threeCasesMode) {
            slots.take(3).mapIndexed { index, slot ->
                val variant = slot.variants[preferredChannel]
                    ?: slot.variants[ChannelPlatform.CLOUDFLARE]
                    ?: slot.variants.values.first()
                val eval = Article30PointEvaluator.evaluate(slot, config)
                HtmlGeneratedPostItem(
                    index = index + 1,
                    caseId = slot.auctionCase.caseId,
                    title = variant.title,
                    platform = variant.platform,
                    propertyTypeLabel = slot.auctionCase.propertyType.displayLabel,
                    pureHtml = renderStandaloneHtmlDocument(slot, variant, config),
                    score = eval.totalScore,
                    bodyOnlyHtml = renderArticleBodySnippet(slot, variant, config)
                )
            }
        } else {
            val slot = slots.first()
            val eval = Article30PointEvaluator.evaluate(slot, config)
            ChannelPlatform.entries.mapIndexed { index, platform ->
                val variant = slot.variants[platform] ?: slot.variants.values.first()
                HtmlGeneratedPostItem(
                    index = index + 1,
                    caseId = slot.auctionCase.caseId,
                    title = variant.title,
                    platform = platform,
                    propertyTypeLabel = slot.auctionCase.propertyType.displayLabel,
                    pureHtml = renderStandaloneHtmlDocument(slot, variant, config),
                    score = eval.totalScore,
                    bodyOnlyHtml = renderArticleBodySnippet(slot, variant, config)
                )
            }
        }

        return HtmlBatchBundleState(
            modeLabel = if (threeCasesMode) "서로 다른 경매 물건 3건 순수 HTML (${preferredChannel.displayName})" else "${slots.firstOrNull()?.auctionCase?.caseId.orEmpty()} 3채널(자체웹·티스토리·네이버) 순수 HTML",
            threeCasesMode = threeCasesMode,
            items = items,
            generatedAt = nowText
        )
    }

    private fun buildSharpAnalysisHtmlSection(archive: CourtFullArchiveRecord): String {
        val sharp = archive.sharpAnalysis ?: return ""
        return buildString {
            appendLine("  <section class=\"sharp-court-analysis\" style=\"background:#f0fdf4;border:1px solid #10b981;border-radius:10px;padding:14px 16px;margin:18px 0;\">")
            appendLine("    <h2>${escapeHtml(sharp.sharpGradeBadge)} (${escapeHtml(sharp.caseId)})</h2>")
            appendLine("    <p style=\"margin:6px 0 10px;\"><strong>종합 칼판정:</strong> ${escapeHtml(sharp.executiveVerdict)}</p>")
            appendLine("    <table class=\"metrics\">")
            appendLine("      <tr><th>교차 대조 축</th><th>판정</th><th>법원 원문 정밀 소견</th></tr>")
            sharp.checks.forEach { chk ->
                val statusColor = if (chk.isAlert) "#b91c1c" else "#047857"
                appendLine("      <tr><td><strong>${escapeHtml(chk.axisTitle)}</strong><br /><small>${escapeHtml(chk.documentsCompared)}</small></td><td style=\"color:$statusColor;font-weight:700;\">${escapeHtml(chk.verdictBadge)}</td><td>${escapeHtml(chk.findingDetail)}<br /><small style=\"color:#0f766e;\">실행: ${escapeHtml(chk.actionDirective)}</small></td></tr>")
            }
            appendLine("    </table>")
            appendLine("    <div style=\"margin-top:10px;font-size:0.9rem;\">")
            appendLine("      <strong>일반 입찰자가 놓치는 3대 급소:</strong>")
            sharp.killerTrapPoints.forEach { trap ->
                appendLine("      <p style=\"margin:4px 0;\">• ${escapeHtml(trap)}</p>")
            }
            appendLine("      <p style=\"margin:8px 0 0;color:#065f46;font-weight:600;\">${escapeHtml(sharp.authorityPromoHeadline)}</p>")
            appendLine("    </div>")
            appendLine("  </section>")
        }.trimEnd()
    }

    private fun buildCourtPhotosHtmlSection(
        archive: CourtFullArchiveRecord,
        allowExternalImg: Boolean,
        localPhotoDataUris: Map<Int, String>
    ): String {
        return buildString {
            appendLine("  <section class=\"court-photos\">")
            appendLine("    <h2>법원 현장사진 · 임장 실사 (${escapeHtml(archive.caseId)} · ${archive.photos.size}컷)</h2>")
            if (archive.photos.isEmpty()) {
                appendLine("    <div class=\"meta-summary\">딱찰 서버에서 수신된 법원 실사 사진이 아직 없습니다. 법원·딱찰 서버 재조회 후 자동 반영됩니다.</div>")
            } else {
                appendLine("    <div class=\"photo-grid\">")
                archive.photos.forEach { photo ->
                    val dataUri = localPhotoDataUris[photo.shotOrder].orEmpty()
                    val urlCandidate = dataUri.ifBlank { photo.imageUrl }
                    val canRenderImg = urlCandidate.startsWith("data:image/", ignoreCase = true) ||
                        (allowExternalImg && (urlCandidate.startsWith("https://", ignoreCase = true) || urlCandidate.startsWith("http://", ignoreCase = true)))
                    appendLine("      <div class=\"photo-card\">")
                    if (canRenderImg) {
                        appendLine("        <img src=\"${escapeHtml(urlCandidate)}\" alt=\"${escapeHtml(photo.blogCaption)}\" />")
                    } else {
                        appendLine("        <div class=\"photo-placeholder\"><strong>컷 #${photo.shotOrder} · ${escapeHtml(photo.courtSourceLabel)}</strong><br />화각: ${escapeHtml(photo.captureAngle)} (${escapeHtml(photo.exifStatusLabel)})</div>")
                    }
                    appendLine("        <strong>${photo.shotOrder}. ${escapeHtml(photo.title)}</strong><br />")
                    appendLine("        <span>${escapeHtml(photo.observationNote)}</span><br />")
                    appendLine("        <small style=\"color:#b45309;\">점검 포인트: ${escapeHtml(photo.redFlagCheck)}</small>")
                    appendLine("      </div>")
                }
                appendLine("    </div>")
            }
            appendLine("  </section>")
        }.trimEnd()
    }

    private fun buildRegistryHtmlSection(
        archive: CourtFullArchiveRecord,
        registryTemplate: RegistryInfographicTemplate
    ): String {
        return buildString {
            appendLine("  <section class=\"registry-table\">")
            appendLine("    <h2>등기부등본 갑구 · 을구 및 권리분석 요약</h2>")
            appendLine("    <div class=\"meta-summary\">")
            appendLine("      <strong>소유자 현황:</strong> ${escapeHtml(registryTemplate.ownershipSummaryCard.currentOwnerLabel)} (${escapeHtml(registryTemplate.ownershipSummaryCard.coOwnerOrShareNote)})<br />")
            appendLine("      <strong>말소·인수 판정:</strong> ${escapeHtml(archive.rightsVerdictSummary)}<br />")
            appendLine("      <strong>예상 부대비용:</strong> ${escapeHtml(archive.assumedCostSummary)}")
            appendLine("    </div>")
            appendLine("    <table class=\"metrics\">")
            appendLine("      <tr><th>구분 · 순위</th><th>권리 내용 · 설정일</th><th>말소/인수 판정</th></tr>")
            if (archive.registryGapGu.isEmpty() && archive.registryEulGu.isEmpty()) {
                appendLine("      <tr><td colspan=\"3\">딱찰 서버에서 수신된 등기부등본 갑구·을구 상세 항목이 없습니다 (원문 대조 대기).</td></tr>")
            } else {
                archive.registryGapGu.forEach { node ->
                    appendLine("      <tr><td>${escapeHtml(node.rankOrder)} (갑구)</td><td>${escapeHtml(node.title)} (${escapeHtml(node.registeredDate)})<br /><small>${escapeHtml(node.plainExplanation)}</small></td><td>${escapeHtml(node.statusBadge)}</td></tr>")
                }
                archive.registryEulGu.forEach { node ->
                    appendLine("      <tr><td>${escapeHtml(node.rankOrder)} (을구/임차)</td><td>${escapeHtml(node.title)} (${escapeHtml(node.registeredDate)})<br /><small>${escapeHtml(node.plainExplanation)}</small></td><td>${escapeHtml(node.statusBadge)}</td></tr>")
                }
            }
            appendLine("    </table>")
            appendLine("  </section>")
        }.trimEnd()
    }

    private fun buildVisitLogsHtmlSection(archive: CourtFullArchiveRecord): String {
        return buildString {
            appendLine("  <section class=\"visit-logs\">")
            appendLine("    <h2>법원 현황조사 방문기록 · 현장 임장 이력 (${archive.visitRecords.size}건)</h2>")
            if (archive.visitRecords.isEmpty()) {
                appendLine("    <div class=\"meta-summary\">수신된 법원 현황조사 방문기록 및 현장 임장 이력이 없습니다.</div>")
            } else {
                appendLine("    <table class=\"metrics\">")
                appendLine("      <tr><th>회차 · 일자</th><th>조사 구분</th><th>점유상태 및 현장 확인 내용</th></tr>")
                archive.visitRecords.forEach { visit ->
                    appendLine("      <tr><td>${escapeHtml(visit.visitRound)}<br /><small>${escapeHtml(visit.visitedAt)}</small></td><td>${escapeHtml(visit.source.label)}<br /><small>${escapeHtml(visit.visitorLabel)}</small></td><td><strong>${escapeHtml(visit.occupancyStatus)}</strong><br />${escapeHtml(visit.findingsNote)}</td></tr>")
                }
                appendLine("    </table>")
            }
            appendLine("  </section>")
        }.trimEnd()
    }

    private fun sanitizeChannelBodyHtml(
        variant: ChannelVariantContent,
        config: ServerAutomationConfig
    ): String {
        val rawHtml = variant.bodyHtml.ifBlank {
            variant.bodyText.lines().joinToString("") { line ->
                if (line.isBlank()) "<br />" else "<p>${escapeHtml(line)}</p>"
            }
        }
        return if (variant.platform == ChannelPlatform.NAVER && !config.enableExternalLinkNaver) {
            rawHtml.replace(externalUrlRegex, "[프로필·고정댓글 확인]")
        } else {
            rawHtml
        }
    }

    private fun buildGeoFaqHtmlSection(slot: GeneratedPostSlot): String {
        val qaBlocks = slot.ddakchalAnalysisData?.qaBlocks.orEmpty()
        if (qaBlocks.isEmpty()) return ""
        return buildString {
            appendLine("    <section class=\"faq-geo\">")
            appendLine("      <h2>핵심 권리·입찰 Q&amp;A (AI 검색 답변 요약)</h2>")
            qaBlocks.forEach { qa ->
                appendLine("      <p><strong>Q. ${escapeHtml(qa.question)}</strong><br />A. ${escapeHtml(qa.answer)}</p>")
            }
            appendLine("    </section>")
        }.trimEnd()
    }

    fun buildLinkedHtmlSlug(caseId: String, dedupKey: String, channel: ChannelPlatform): String {
        val raw = (dedupKey.ifBlank { caseId }).ifBlank { "post" }
        val safe = raw.replace(Regex("[^a-zA-Z0-9_\\-가-힣]"), "_")
        return "ddakchal_${safe}_${channel.code.lowercase()}.html"
    }

    fun buildLinkedHtmlCatalog(
        slots: List<GeneratedPostSlot>,
        warehouseArticles: List<com.example.model.WarehouseArticle>,
        publishLogs: List<com.example.model.PublishLogEntry>,
        config: ServerAutomationConfig,
        proFunnel: com.example.model.ProFunnelConfig = com.example.model.ProFunnelConfig(),
        preferredChannel: ChannelPlatform = ChannelPlatform.CLOUDFLARE
    ): List<com.example.model.LinkedHtmlArticleEntry> {
        val mergedByKey = linkedMapOf<String, GeneratedPostSlot>()
        slots.forEach { slot ->
            mergedByKey[slot.postId] = slot
        }
        warehouseArticles.forEach { wh ->
            if (!mergedByKey.containsKey(wh.slot.postId) &&
                mergedByKey.values.none { it.auctionCase.caseId == wh.slot.auctionCase.caseId }
            ) {
                mergedByKey[wh.slot.postId] = wh.slot
            }
        }

        val cfSiteBase = config.cloudflareSiteBase.trim().removeSuffix("/").ifBlank { "https://aution.codeb.app" }
        val cfPath = config.cloudflareBlogPath.trim().let { if (it.startsWith("/")) it else "/$it" }

        return mergedByKey.values.map { slot ->
            val whMatch = warehouseArticles.firstOrNull { it.slot.postId == slot.postId }
            val evalScore = whMatch?.evaluation?.totalScore
                ?: Article30PointEvaluator.evaluate(slot, config, proFunnel).totalScore

            val urlsByPlatform = linkedMapOf<ChannelPlatform, String>()
            slot.publishedUrls.forEach { (platform, url) ->
                if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
                    urlsByPlatform[platform] = url.trim()
                }
            }
            publishLogs
                .filter {
                    (it.postId == slot.postId || it.caseId == slot.auctionCase.caseId) &&
                        it.status == com.example.model.PostStatus.PUBLISHED &&
                        !it.publishedUrl.isNullOrBlank()
                }
                .forEach { log ->
                    val u = log.publishedUrl.orEmpty().trim()
                    if ((u.startsWith("http://", ignoreCase = true) || u.startsWith("https://", ignoreCase = true)) &&
                        !urlsByPlatform.containsKey(log.platform)
                    ) {
                        urlsByPlatform[log.platform] = u
                    }
                }

            val defaultChannel = when {
                urlsByPlatform.containsKey(preferredChannel) -> preferredChannel
                slot.variants.containsKey(preferredChannel) -> preferredChannel
                slot.variants.containsKey(ChannelPlatform.CLOUDFLARE) -> ChannelPlatform.CLOUDFLARE
                else -> slot.variants.keys.firstOrNull() ?: ChannelPlatform.CLOUDFLARE
            }
            val activeVariant = slot.variants[defaultChannel] ?: slot.variants.values.firstOrNull()
            val title = activeVariant?.title?.ifBlank { "${slot.auctionCase.caseId} ${slot.auctionCase.address}" }
                ?: "${slot.auctionCase.caseId} ${slot.auctionCase.address}"
            val archive = CourtEvidenceRecorder.recordFullArchiveForSlot(slot)

            com.example.model.LinkedHtmlArticleEntry(
                postId = slot.postId,
                caseId = slot.auctionCase.caseId,
                dedupKey = slot.auctionCase.dedupKey,
                title = title,
                courtName = slot.auctionCase.courtName,
                address = slot.auctionCase.address,
                propertyTypeLabel = slot.auctionCase.propertyType.displayLabel,
                status = if (urlsByPlatform.isNotEmpty()) com.example.model.PostStatus.PUBLISHED else slot.status,
                publishDate = slot.publishDate,
                score = evalScore,
                defaultChannel = defaultChannel,
                publishedUrls = urlsByPlatform,
                cloudflareExpectedUrl = "$cfSiteBase$cfPath/${slot.auctionCase.dedupKey}",
                inAppHubTargetUri = "ddakchal://post/${slot.postId}/${defaultChannel.code.lowercase()}",
                relativeHtmlFilename = buildLinkedHtmlSlug(slot.auctionCase.caseId, slot.auctionCase.dedupKey, defaultChannel),
                photoCount = archive.photos.size,
                registryCount = archive.registryGapGu.size + archive.registryEulGu.size,
                visitCount = archive.visitRecords.size
            )
        }
    }

    fun renderHubIndexHtml(
        entries: List<com.example.model.LinkedHtmlArticleEntry>,
        config: ServerAutomationConfig,
        generatedAt: String,
        forExternalFileExport: Boolean = false
    ): String {
        val webPublishedCount = entries.count { it.publishedUrls.isNotEmpty() }
        val highScoreCount = entries.count { it.score >= 95 }
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang=\"ko\">")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\" />")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />")
            appendLine("  <title>딱찰 컨트롤타워 · 생성 글 HTML 목차 허브</title>")
            appendLine("  <style>")
            appendLine("    * { box-sizing: border-box; }")
            appendLine("    body { font-family: -apple-system, 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; margin: 0; padding: 16px; background: #0b1324; color: #e2e8f0; line-height: 1.6; }")
            appendLine("    .hub-wrap { max-width: 820px; margin: 0 auto; }")
            appendLine("    .hero { background: linear-gradient(135deg, #0f172a, #102a43); border: 1px solid #38bdf8; border-radius: 14px; padding: 18px; margin-bottom: 16px; }")
            appendLine("    .hero h1 { margin: 0 0 6px; font-size: 1.32rem; color: #f8fafc; }")
            appendLine("    .hero p { margin: 0; font-size: 0.9rem; color: #94a3b8; }")
            appendLine("    .kpi-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }")
            appendLine("    .kpi-chip { background: #1e293b; border: 1px solid #334155; border-radius: 999px; padding: 5px 12px; font-size: 0.82rem; font-weight: 700; color: #38bdf8; }")
            appendLine("    .card { background: #111c33; border: 1px solid #26354d; border-radius: 12px; padding: 14px 16px; margin-bottom: 12px; }")
            appendLine("    .card-top { display: flex; justify-content: space-between; align-items: center; gap: 8px; flex-wrap: wrap; }")
            appendLine("    .case-badge { font-weight: 800; color: #34d399; font-size: 0.95rem; }")
            appendLine("    .score-badge { background: #064e3b; color: #6ee7b7; border-radius: 999px; padding: 3px 10px; font-size: 0.78rem; font-weight: 700; }")
            appendLine("    .card h2 { margin: 8px 0 4px; font-size: 1.04rem; color: #f8fafc; }")
            appendLine("    .card h2 a { color: #f8fafc; text-decoration: none; }")
            appendLine("    .card h2 a:hover { color: #38bdf8; text-decoration: underline; }")
            appendLine("    .meta { font-size: 0.84rem; color: #94a3b8; margin-bottom: 10px; }")
            appendLine("    .link-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }")
            appendLine("    .btn-link { display: inline-block; padding: 8px 12px; border-radius: 8px; font-size: 0.84rem; font-weight: 700; text-decoration: none; }")
            appendLine("    .btn-primary { background: #10b981; color: #06281e; }")
            appendLine("    .btn-ch { background: #1e293b; color: #38bdf8; border: 1px solid #38bdf8; }")
            appendLine("    .btn-web { background: #064e3b; color: #6ee7b7; border: 1px solid #10b981; }")
            appendLine("    .empty-box { background: #111c33; border: 1px dashed #475569; border-radius: 12px; padding: 28px 16px; text-align: center; color: #94a3b8; }")
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <main class=\"hub-wrap\">")
            appendLine("    <header class=\"hero\">")
            appendLine("      <h1>딱찰 컨트롤타워 · 생성 글 HTML 목차 허브</h1>")
            appendLine("      <p>생성된 경매 분석 글을 클릭하면 상세 HTML 페이지로 바로 이동합니다. (기준시각: ${escapeHtml(generatedAt)} · 서버: ${escapeHtml(config.ddakchalApiBase)})</p>")
            appendLine("      <div class=\"kpi-row\">")
            appendLine("        <span class=\"kpi-chip\">전체 생성 글 ${entries.size}건</span>")
            appendLine("        <span class=\"kpi-chip\" style=\"color:#6ee7b7;\">95점+ 통과 ${highScoreCount}건</span>")
            appendLine("        <span class=\"kpi-chip\" style=\"color:#fbbf24;\">웹URL 발행 ${webPublishedCount}건</span>")
            appendLine("      </div>")
            appendLine("    </header>")
            if (entries.isEmpty()) {
                appendLine("    <section class=\"empty-box\">")
                appendLine("      <strong>아직 생성된 글이 없습니다.</strong><br />")
                appendLine("      컨트롤타워 상단에서 법원 사건번호를 조회하거나 딱찰 DB 탐색·크롤링을 실행해 글을 생성하면 이곳에 HTML 링크가 자동으로 연결됩니다.")
                appendLine("    </section>")
            } else {
                entries.forEachIndexed { idx, entry ->
                    val primaryHref = if (forExternalFileExport) {
                        "./${buildLinkedHtmlSlug(entry.caseId, entry.dedupKey, entry.defaultChannel)}"
                    } else {
                        "ddakchal://post/${entry.postId}/${entry.defaultChannel.code.lowercase()}"
                    }
                    appendLine("    <article class=\"card\" data-case-id=\"${escapeHtml(entry.caseId)}\">")
                    appendLine("      <div class=\"card-top\">")
                    appendLine("        <span class=\"case-badge\">#${idx + 1} · ${escapeHtml(entry.caseId)} · ${escapeHtml(entry.propertyTypeLabel)}</span>")
                    appendLine("        <span class=\"score-badge\">${entry.score}점 · ${escapeHtml(entry.status.label)}</span>")
                    appendLine("      </div>")
                    appendLine("      <h2><a href=\"$primaryHref\">${escapeHtml(entry.title)}</a></h2>")
                    appendLine("      <div class=\"meta\">${escapeHtml(entry.courtName)} · ${escapeHtml(entry.address)}<br />예약일 ${escapeHtml(entry.publishDate)} · 법원사진 ${entry.photoCount}컷 · 등기 ${entry.registryCount}건 · 방문기록 ${entry.visitCount}건</div>")
                    appendLine("      <div class=\"link-row\">")
                    appendLine("        <a class=\"btn-link btn-primary\" href=\"$primaryHref\">상세 HTML 열기 →</a>")
                    ChannelPlatform.entries.forEach { ch ->
                        val chHref = if (forExternalFileExport) {
                            "./${buildLinkedHtmlSlug(entry.caseId, entry.dedupKey, ch)}"
                        } else {
                            "ddakchal://post/${entry.postId}/${ch.code.lowercase()}"
                        }
                        appendLine("        <a class=\"btn-link btn-ch\" href=\"$chHref\">${escapeHtml(ch.displayName)} HTML</a>")
                    }
                    entry.publishedUrls.forEach { (platform, liveUrl) ->
                        appendLine("        <a class=\"btn-link btn-web\" href=\"${escapeHtml(liveUrl)}\" target=\"_blank\" rel=\"noopener noreferrer\">웹 링크 (${escapeHtml(platform.displayName)}) ↗</a>")
                    }
                    appendLine("      </div>")
                    if (entry.publishedUrls.isEmpty()) {
                        appendLine("      <div class=\"meta\" style=\"margin-top:8px;margin-bottom:0;\">실제 웹URL 미발행 · 예상 자체웹 경로: ${escapeHtml(entry.cloudflareExpectedUrl)}</div>")
                    }
                    appendLine("    </article>")
                }
            }
            appendLine("  </main>")
            appendLine("</body>")
            appendLine("</html>")
        }.trim()
    }

    fun renderLinkedArticleDetailHtml(
        slot: GeneratedPostSlot,
        channel: ChannelPlatform,
        config: ServerAutomationConfig,
        evaluation: ArticleEvaluationResult? = null,
        localPhotoDataUris: Map<Int, String> = emptyMap(),
        forExternalFileExport: Boolean = false,
        hubHref: String = if (forExternalFileExport) "./ddakchal_hub.html" else "ddakchal://hub"
    ): String {
        val variant = slot.variants[channel]
            ?: slot.variants[ChannelPlatform.CLOUDFLARE]
            ?: slot.variants.values.first()
        val baseHtml = renderWebViewPreviewHtml(
            slot = slot,
            variant = variant,
            config = config,
            evaluation = evaluation,
            localPhotoDataUris = localPhotoDataUris
        )
        val liveUrl = slot.publishedUrls[channel].orEmpty().ifBlank {
            slot.publishedUrls.values.firstOrNull().orEmpty()
        }
        val channelLinks = ChannelPlatform.entries.joinToString("") { ch ->
            val href = if (forExternalFileExport) {
                "./${buildLinkedHtmlSlug(slot.auctionCase.caseId, slot.auctionCase.dedupKey, ch)}"
            } else {
                "ddakchal://post/${slot.postId}/${ch.code.lowercase()}"
            }
            val activeStyle = if (ch == variant.platform) {
                "background:#10b981;color:#06281e;border-color:#10b981;"
            } else {
                "background:#1e293b;color:#e2e8f0;border-color:#475569;"
            }
            "<a href=\"$href\" style=\"display:inline-block;padding:6px 10px;border-radius:8px;border:1px solid;font-size:12px;font-weight:700;text-decoration:none;$activeStyle\">${escapeHtml(ch.displayName)}</a>"
        }
        val liveUrlBadge = if (liveUrl.isNotBlank()) {
            "<a href=\"${escapeHtml(liveUrl)}\" target=\"_blank\" rel=\"noopener noreferrer\" style=\"display:inline-block;padding:6px 10px;border-radius:8px;background:#0284c7;color:#ffffff;font-size:12px;font-weight:700;text-decoration:none;\">실제 웹 게시물 열기 ↗</a>"
        } else {
            ""
        }
        val navStrip = buildString {
            appendLine("  <nav class=\"ddakchal-hub-nav\" style=\"background:#0f172a;color:#f8fafc;border-radius:12px;padding:10px 12px;margin-bottom:12px;display:flex;flex-wrap:wrap;gap:6px;align-items:center;justify-content:space-between;\">")
            appendLine("    <a href=\"$hubHref\" style=\"display:inline-block;padding:6px 11px;border-radius:8px;background:#38bdf8;color:#082f49;font-size:12px;font-weight:800;text-decoration:none;\">← 목록 허브 HTML</a>")
            appendLine("    <div style=\"display:flex;flex-wrap:wrap;gap:6px;align-items:center;\">$channelLinks$liveUrlBadge</div>")
            appendLine("  </nav>")
        }
        return baseHtml.replace("<body>", "<body>\n$navStrip")
    }

    private fun escapeHtml(raw: String): String = raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
