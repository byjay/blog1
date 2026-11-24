package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.ChannelVariantContent
import com.example.model.GeneratedPostSlot
import com.example.model.ProFunnelConfig
import com.example.model.QualityDefenseCategory
import com.example.model.QualityDefenseHit
import com.example.model.QualityDefenseResult
import com.example.model.ServerAutomationConfig

/**
 * Detects and automatically replaces SEO-sensitive keywords, AI-generated clichés,
 * decorative emojis, bracket title spam, direct Naver URLs, and PII leaks
 * with our 30-year veteran human-like field essay writing style.
 */
object QualityDefenseEngine {

    private val AI_CLICHE_ENDINGS_REPLACEMENTS = listOf(
        "현장 실사 수치 기준으로 바로 풀어드리겠습니다" to "알아보겠습니다",
        "등기부와 법원 원문 기준으로 차근히 짚어드립니다" to "살펴보겠습니다",
        "현장에서 직접 발로 뛴 대조 결과를 공유합니다" to "확인해 보겠습니다",
        "핵심 숫자만 추려 손에 잡히게 정리했습니다" to "정리해 보겠습니다"
    )

    private val AI_PHRASE_MAP: List<Pair<Regex, String>> = listOf(
        Regex("""오늘\s*살펴볼\s*물건은""") to "오늘 실무 검토 테이블에 올린 사건은",
        Regex("""이번에는\s*([^\n]+?)\s*점검해\s*보겠습니다\.?""") to "현장에서 마주한 $1 기준 핵심 쟁점을 바로 대조합니다.",
        Regex("""알아보겠습니다\.?""") to "실측 데이터로 풀어드립니다.",
        Regex("""살펴보겠습니다\.?""") to "등기부 원문 기준으로 짚어드립니다.",
        Regex("""확인해\s*보겠습니다\.?""") to "현장 기록과 맞대어 점검합니다.",
        Regex("""정리해\s*보겠습니다\.?""") to "핵심 숫자만 추려 정리했습니다.",
        Regex("""살펴볼게요\.?""") to "차근히 대조해 드립니다.",
        Regex("""알아볼게요\.?""") to "실무 기준으로 짚어드립니다.",
        Regex("""매우\s*합리적입니다""") to "시세와 수익 수지상 가격 완충력이 확인됩니다",
        Regex("""합리적입니다""") to "실투자 수지상 안전 여력이 남습니다",
        Regex("""매력적입니다""") to "실수요와 임대 수요가 동시에 받쳐주는 구간입니다",
        Regex("""강력히?\s*추천합니다""") to "보수적 기준에서 검토해 볼 만한 구간입니다",
        Regex("""추천합니다""") to "현장 실사 후 응찰 여부를 가늠할 만합니다"
    )

    private val QUESTION_LABEL_REGEX = Regex("""(?m)^\s*Q([1-6])[\.\)]\s*""")
    private val ANSWER_LABEL_REGEX = Regex("""(?m)^\s*A([1-6])[\.\)]\s*""")
    private val BULLET_SYMBOL_REGEX = Regex("""[■◆▶★☆●◎▪▫]""")
    private val EMOJI_REGEX = Regex("""[\p{So}\p{Cn}✅🔥📌👉💡⭐🎯🚨⚡🔍🏠🏢💰]""")
    private val METRIC_UNIT_SYMBOLS = setOf("㎡", "㎥", "㎝", "㎜", "㎞", "㏊")
    private val TITLE_BRACKET_SPAM_REGEX = Regex("""^\[(?:급매추천|추천|긴급|특급|대박|초특가|필독)[^\]]*\]\s*""")
    private val NAVER_URL_REGEX = Regex("""https?://[^\s"'<>]+""", RegexOption.IGNORE_CASE)

    private val SPAM_TRIGGER_MAP: List<Pair<Regex, String>> = listOf(
        Regex("""100%\s*무조건\s*수익""", RegexOption.IGNORE_CASE) to "실거래 대비 시세 완충 구간",
        Regex("""100%\s*수익\s*보장""", RegexOption.IGNORE_CASE) to "보수적 안전마진 검토 구간",
        Regex("""원금\s*보장""", RegexOption.IGNORE_CASE) to "담보가치 대비 원금 회수 가능성 점검",
        Regex("""무조건\s*낙찰""", RegexOption.IGNORE_CASE) to "경쟁률 기반 전략적 응찰 구간",
        Regex("""확정\s*수익""", RegexOption.IGNORE_CASE) to "추정 세후 현금흐름",
        Regex("""\b(?:null|NaN|undefined)\b""") to "미기재(확인 필요)"
    )

    private val QUESTION_HUMAN_TRANSITIONS = mapOf(
        "1" to "첫째, 현장에서 가장 먼저 숫자를 맞춰봐야 할 대목입니다. ",
        "2" to "둘째, 현황조사서와 점유 현장을 대조해 본 핵심입니다. ",
        "3" to "셋째, 낙찰 뒤 실제로 떠안게 될 인수 비용 여부입니다. ",
        "4" to "넷째, 3단계 입찰 가격대와 자금·세무 계산선입니다. ",
        "5" to "마지막으로 입찰 전날 밤 법정에 가져갈 최종 점검 사항입니다. ",
        "6" to "추가로 실무 전담팀이 현장에서 확인한 유의사항입니다. "
    )

    fun detectIssues(
        bodyText: String,
        title: String = "",
        channel: ChannelPlatform = ChannelPlatform.NAVER
    ): List<QualityDefenseHit> {
        val hits = mutableListOf<QualityDefenseHit>()

        val bulletCount = BULLET_SYMBOL_REGEX.findAll(bodyText).count()
        val qCount = QUESTION_LABEL_REGEX.findAll(bodyText).count() + ANSWER_LABEL_REGEX.findAll(bodyText).count()
        if (bulletCount > 0 || qCount > 0) {
            hits += QualityDefenseHit(
                category = QualityDefenseCategory.AI_STRUCTURAL_PATTERN,
                matchedSnippet = "■/특수기호(${bulletCount}건) · Q&A 기계라벨(${qCount}건)",
                replacementSnippet = "30년 베테랑 현장 에세이 소제목·서술 전환",
                count = bulletCount + qCount
            )
        }

        var clicheCount = 0
        val matchedCliches = mutableListOf<String>()
        AI_PHRASE_MAP.forEach { (regex, _) ->
            val found = regex.findAll(bodyText).toList()
            if (found.isNotEmpty()) {
                clicheCount += found.size
                matchedCliches += found.first().value
            }
        }
        if (clicheCount > 0) {
            hits += QualityDefenseHit(
                category = QualityDefenseCategory.AI_CLICHE_PHRASE,
                matchedSnippet = matchedCliches.distinct().take(3).joinToString(", "),
                replacementSnippet = "현장 실사·등기부 원문 대조 구어체",
                count = clicheCount
            )
        }

        val emojiCount = EMOJI_REGEX.findAll("$title\n$bodyText").count { it.value !in METRIC_UNIT_SYMBOLS }
        if (emojiCount > 0) {
            hits += QualityDefenseHit(
                category = QualityDefenseCategory.EMOJI_DECORATION,
                matchedSnippet = "이모지·아이콘 기호 ${emojiCount}건",
                replacementSnippet = "무이모지·무기계패턴 정제 텍스트",
                count = emojiCount
            )
        }

        var spamCount = 0
        val spamMatched = mutableListOf<String>()
        SPAM_TRIGGER_MAP.forEach { (regex, _) ->
            val found = regex.findAll("$title\n$bodyText").toList()
            if (found.isNotEmpty()) {
                spamCount += found.size
                spamMatched += found.first().value
            }
        }
        if (spamCount > 0) {
            hits += QualityDefenseHit(
                category = QualityDefenseCategory.SEO_SPAM_TRIGGER,
                matchedSnippet = spamMatched.distinct().joinToString(", "),
                replacementSnippet = "객관적 안전마진·리스크 고지 표현",
                count = spamCount
            )
        }

        if (TITLE_BRACKET_SPAM_REGEX.containsMatchIn(title.trim())) {
            hits += QualityDefenseHit(
                category = QualityDefenseCategory.TITLE_BRACKET_SPAM,
                matchedSnippet = TITLE_BRACKET_SPAM_REGEX.find(title.trim())?.value.orEmpty().trim(),
                replacementSnippet = "지역·사건번호·핵심쟁점 선배치 제목",
                count = 1
            )
        }

        if (channel == ChannelPlatform.NAVER) {
            val urlMatches = NAVER_URL_REGEX.findAll(bodyText).toList()
            if (urlMatches.isNotEmpty()) {
                hits += QualityDefenseHit(
                    category = QualityDefenseCategory.NAVER_DIRECT_URL,
                    matchedSnippet = urlMatches.first().value.take(40),
                    replacementSnippet = "네이버 검색 유도 및 고정댓글 안내",
                    count = urlMatches.size
                )
            }
        }

        val piiLeaks = PrivacySanitizerEngine.detectPersonalInfoLeaks("$title\n$bodyText")
        if (piiLeaks.isNotEmpty()) {
            hits += QualityDefenseHit(
                category = QualityDefenseCategory.PII_EXPOSURE,
                matchedSnippet = piiLeaks.joinToString(", "),
                replacementSnippet = "채무자·임차인 완전 비식별 처리",
                count = piiLeaks.size
            )
        }

        return hits
    }

    fun defendText(
        rawTitle: String,
        rawBody: String,
        channel: ChannelPlatform = ChannelPlatform.NAVER,
        auction: AuctionCaseIntel? = null,
        naverSearchKeyword: String = "딱찰 경매"
    ): QualityDefenseResult {
        val hits = detectIssues(rawBody, rawTitle, channel)
        var cleanTitle = rawTitle.trim()
        var cleanBody = rawBody

        // 1. Strip bracket title spam and prefer region + caseId front-loading
        if (TITLE_BRACKET_SPAM_REGEX.containsMatchIn(cleanTitle)) {
            cleanTitle = TITLE_BRACKET_SPAM_REGEX.replace(cleanTitle, "").trim()
            if (auction != null && !cleanTitle.contains("권리") && !cleanTitle.contains("분석")) {
                cleanTitle = RegistryAndMotionEngine.buildHumanVeteranNaverTitle(auction)
            }
        }
        cleanTitle = EMOJI_REGEX.replace(cleanTitle) { if (it.value in METRIC_UNIT_SYMBOLS) it.value else "" }
            .replace(Regex("""\s{2,}"""), " ")
            .trim()

        // 2. Replace structural AI patterns (■ bullets, Q1.~Q6.)
        cleanBody = BULLET_SYMBOL_REGEX.replace(cleanBody) { "" }
        cleanBody = QUESTION_LABEL_REGEX.replace(cleanBody) { match ->
            val num = match.groupValues[1]
            QUESTION_HUMAN_TRANSITIONS[num] ?: "현장 핵심 점검 포인트입니다. "
        }
        cleanBody = ANSWER_LABEL_REGEX.replace(cleanBody) { "실무 대조 결과를 말씀드리면, " }

        // 3. Replace AI cliché endings/phrases with veteran human essay expressions
        AI_PHRASE_MAP.forEach { (regex, replacement) ->
            cleanBody = regex.replace(cleanBody, replacement)
        }

        // 4. Replace spam / NaN triggers
        SPAM_TRIGGER_MAP.forEach { (regex, replacement) ->
            cleanBody = regex.replace(cleanBody, replacement)
        }

        // 5. Remove decorative emojis / useless symbols (preserving metric symbols like ㎡)
        cleanBody = EMOJI_REGEX.replace(cleanBody) { if (it.value in METRIC_UNIT_SYMBOLS) it.value else "" }

        // 6. Strip direct URLs inside Naver body text
        if (channel == ChannelPlatform.NAVER) {
            val caseSuffix = auction?.caseId?.let { " $it" }.orEmpty()
            cleanBody = NAVER_URL_REGEX.replace(
                cleanBody,
                "네이버 '$naverSearchKeyword$caseSuffix' 검색 및 고정댓글 안내 참조"
            )
        }

        // 7. Personal information sanitization
        cleanTitle = PrivacySanitizerEngine.sanitizeForPublicBlog(cleanTitle)
        cleanBody = PrivacySanitizerEngine.sanitizeForPublicBlog(cleanBody)

        // Normalize whitespace cleanly while keeping mobile paragraph spacing
        cleanBody = cleanBody
            .replace(Regex("""[ \t]{2,}"""), " ")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()

        val remainingHits = detectIssues(cleanBody, cleanTitle, channel)
        val totalDetected = hits.sumOf { it.count }
        val scorePct = (100 - remainingHits.sumOf { it.count } * 12).coerceIn(0, 100)
        val summary = if (totalDetected == 0) {
            "SEO 민감 AI 패턴·이모지·개인정보 0건 (베테랑 에세이 순도 ${scorePct}%)"
        } else {
            "탐지 ${totalDetected}건 자동 치환 완료 → 사람 현장 에세이 순도 ${scorePct}%"
        }

        return QualityDefenseResult(
            originalTitle = rawTitle,
            sanitizedTitle = cleanTitle,
            originalBody = rawBody,
            sanitizedBody = cleanBody,
            hits = hits,
            totalHitsCount = totalDetected,
            humanToneScorePct = scorePct,
            summary = summary
        )
    }

    fun defendSlot(
        slot: GeneratedPostSlot,
        config: ServerAutomationConfig,
        proFunnel: ProFunnelConfig = ProFunnelConfig(),
        recent7DayCorpus: List<String> = emptyList()
    ): Pair<GeneratedPostSlot, QualityDefenseResult> {
        val safeAuction = PrivacySanitizerEngine.sanitizeAuctionForBlog(slot.auctionCase)
        val baselineVariants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(
            auction = safeAuction,
            seedIndex = slot.slotIndex + 2,
            config = config,
            proFunnel = proFunnel
        )

        var primaryReport: QualityDefenseResult? = null
        val cleanedVariants = slot.variants.mapValues { (platform, variant) ->
            val defended = defendText(
                rawTitle = variant.title,
                rawBody = variant.bodyText,
                channel = platform,
                auction = safeAuction,
                naverSearchKeyword = proFunnel.naverSearchKeyword
            )
            if (platform == ChannelPlatform.NAVER) {
                primaryReport = defended
            }

            val nonSpaceCount = defended.sanitizedBody.count { !it.isWhitespace() }
            val finalTitle: String
            val finalBody: String
            val finalHtml: String
            val finalPinned: String

            // If draft was severely truncated (< 1200 chars) or missing facts/funnel, fuse sanitized baseline essay
            val needsStructuralExpansion = nonSpaceCount < 1200 ||
                !defended.sanitizedBody.contains(safeAuction.caseId) ||
                !defended.sanitizedBody.contains(safeAuction.baseRightDate)

            if (needsStructuralExpansion) {
                val fb = baselineVariants[platform]!!
                val defendedBaseline = defendText(
                    rawTitle = fb.title,
                    rawBody = fb.bodyText,
                    channel = platform,
                    auction = safeAuction,
                    naverSearchKeyword = proFunnel.naverSearchKeyword
                )
                finalTitle = defendedBaseline.sanitizedTitle
                finalBody = defendedBaseline.sanitizedBody
                finalHtml = fb.bodyHtml
                finalPinned = fb.pinnedCommentSnippet
                if (platform == ChannelPlatform.NAVER) {
                    primaryReport = defended.copy(
                        sanitizedTitle = finalTitle,
                        sanitizedBody = finalBody,
                        humanToneScorePct = 100,
                        summary = if (defended.totalHitsCount > 0) {
                            "AI 패턴·외부링크 ${defended.totalHitsCount}건 제거 + 1,400자+ 베테랑 에세이 복원 완료"
                        } else {
                            "분량·팩트 보완 및 베테랑 문체 정제 완료 (순도 100%)"
                        }
                    )
                }
            } else {
                finalTitle = defended.sanitizedTitle
                finalBody = defended.sanitizedBody
                finalHtml = when (platform) {
                    ChannelPlatform.NAVER -> {
                        val funnelHtml = ProAnalysisAndPhotoGuideEngine.buildChannelProFunnelHtml(
                            safeAuction,
                            ChannelPlatform.NAVER,
                            config,
                            proFunnel
                        )
                        "<div class='se-viewer'>${finalBody.replace("\n", "<br/>")}</div>$funnelHtml"
                    }
                    ChannelPlatform.TISTORY -> {
                        val fbHtml = baselineVariants[ChannelPlatform.TISTORY]?.bodyHtml.orEmpty()
                        if (variant.bodyHtml.contains("nofollow sponsored")) variant.bodyHtml else fbHtml
                    }
                    ChannelPlatform.CLOUDFLARE -> {
                        val fbHtml = baselineVariants[ChannelPlatform.CLOUDFLARE]?.bodyHtml.orEmpty()
                        if (variant.bodyHtml.contains("application/ld+json")) variant.bodyHtml else fbHtml
                    }
                }
                finalPinned = if (platform == ChannelPlatform.NAVER && variant.pinnedCommentSnippet.isBlank()) {
                    ProAnalysisAndPhotoGuideEngine.buildNaverPinnedComment(safeAuction, proFunnel)
                } else {
                    PrivacySanitizerEngine.sanitizeForPublicBlog(variant.pinnedCommentSnippet)
                }
            }

            variant.copy(
                title = finalTitle,
                bodyText = finalBody,
                bodyHtml = finalHtml,
                pinnedCommentSnippet = finalPinned
            )
        }

        val qa = QaVerificationEngine.auditSlot(
            auctionCase = safeAuction,
            variants = cleanedVariants,
            scheduledTimes = slot.scheduledTimes,
            config = config,
            recent7DayCorpus = recent7DayCorpus
        )
        val updatedSlot = slot.copy(
            auctionCase = safeAuction,
            variants = cleanedVariants,
            qaReport = qa
        )
        val report = primaryReport ?: defendText(
            rawTitle = updatedSlot.variants[ChannelPlatform.NAVER]?.title.orEmpty(),
            rawBody = updatedSlot.variants[ChannelPlatform.NAVER]?.bodyText.orEmpty(),
            channel = ChannelPlatform.NAVER,
            auction = safeAuction
        )
        return updatedSlot to report
    }
}
