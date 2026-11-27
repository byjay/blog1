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

        val archive = CourtEvidenceRecorder.recordFullArchive(
            auction = case,
            courtPhotoUrls = slot.courtPhotoUrls,
            customVisitRecords = slot.visitRecords
        )
        val registryTemplate = RegistryInfographicEngine.buildTemplate(case)
        val allowImgUrl = variant.platform != ChannelPlatform.NAVER || config.enableExternalLinkNaver

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

        val archive = CourtEvidenceRecorder.recordFullArchive(
            auction = case,
            courtPhotoUrls = slot.courtPhotoUrls,
            customVisitRecords = slot.visitRecords
        )
        val registryTemplate = RegistryInfographicEngine.buildTemplate(case)
        val allowImgUrl = variant.platform != ChannelPlatform.NAVER || config.enableExternalLinkNaver
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
        val archive = CourtEvidenceRecorder.recordFullArchive(
            auction = case,
            courtPhotoUrls = slot.courtPhotoUrls,
            customVisitRecords = slot.visitRecords
        )
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

    private fun buildCourtPhotosHtmlSection(
        archive: CourtFullArchiveRecord,
        allowExternalImg: Boolean,
        localPhotoDataUris: Map<Int, String>
    ): String {
        return buildString {
            appendLine("  <section class=\"court-photos\">")
            appendLine("    <h2>법원 현장사진 · 임장 체크 5컷 (${escapeHtml(archive.caseId)})</h2>")
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
            archive.registryGapGu.forEach { node ->
                appendLine("      <tr><td>${escapeHtml(node.rankOrder)} (갑구)</td><td>${escapeHtml(node.title)} (${escapeHtml(node.registeredDate)})<br /><small>${escapeHtml(node.plainExplanation)}</small></td><td>${escapeHtml(node.statusBadge)}</td></tr>")
            }
            archive.registryEulGu.forEach { node ->
                appendLine("      <tr><td>${escapeHtml(node.rankOrder)} (을구/임차)</td><td>${escapeHtml(node.title)} (${escapeHtml(node.registeredDate)})<br /><small>${escapeHtml(node.plainExplanation)}</small></td><td>${escapeHtml(node.statusBadge)}</td></tr>")
            }
            appendLine("    </table>")
            appendLine("  </section>")
        }.trimEnd()
    }

    private fun buildVisitLogsHtmlSection(archive: CourtFullArchiveRecord): String {
        if (archive.visitRecords.isEmpty()) return ""
        return buildString {
            appendLine("  <section class=\"visit-logs\">")
            appendLine("    <h2>법원 현황조사 방문기록 · 현장 임장 이력 (${archive.visitRecords.size}건)</h2>")
            appendLine("    <table class=\"metrics\">")
            appendLine("      <tr><th>회차 · 일자</th><th>조사 구분</th><th>점유상태 및 현장 확인 내용</th></tr>")
            archive.visitRecords.forEach { visit ->
                appendLine("      <tr><td>${escapeHtml(visit.visitRound)}<br /><small>${escapeHtml(visit.visitedAt)}</small></td><td>${escapeHtml(visit.source.label)}<br /><small>${escapeHtml(visit.visitorLabel)}</small></td><td><strong>${escapeHtml(visit.occupancyStatus)}</strong><br />${escapeHtml(visit.findingsNote)}</td></tr>")
            }
            appendLine("    </table>")
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

    private fun escapeHtml(raw: String): String = raw
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
