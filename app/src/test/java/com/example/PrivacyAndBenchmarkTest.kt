package com.example

import com.example.engine.Article30PointEvaluator
import com.example.engine.CourtEvidenceRecorder
import com.example.engine.DefaultSpecDataFactory
import com.example.engine.PrivacySanitizerEngine
import com.example.engine.ProAnalysisAndPhotoGuideEngine
import com.example.engine.QaVerificationEngine
import com.example.engine.RegistryAndMotionEngine
import com.example.engine.RegistryInfographicEngine
import com.example.model.ChannelPlatform
import com.example.model.GeneratedPostSlot
import com.example.model.PostStatus
import com.example.model.ProFunnelConfig
import com.example.model.ServerAutomationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyAndBenchmarkTest {

    @Test
    fun privacySanitizer_masksDebtorTenantPii_andPreservesLegalStatusPhrases() {
        val dirty = "채무자 홍길동 점유, 임차인 박OO (전입 2021-11-04 / 확정 2021-11-05), " +
            "OO기계설비 연락처 010-1234-5678 주민번호 850101-1234567 이메일 test@example.com"
        val cleaned = PrivacySanitizerEngine.sanitizeForPublicBlog(dirty)

        assertFalse(cleaned.contains("홍길동"))
        assertFalse(cleaned.contains("박OO"))
        assertFalse(cleaned.contains("2021-11-04"))
        assertFalse(cleaned.contains("2021-11-05"))
        assertFalse(cleaned.contains("010-1234-5678"))
        assertFalse(cleaned.contains("850101-1234567"))
        assertFalse(cleaned.contains("test@example.com"))
        assertTrue(PrivacySanitizerEngine.detectPersonalInfoLeaks(cleaned).isEmpty())

        // Verify legal phrases are NOT falsely flagged
        val legalPhrases = listOf(
            "채무자 겸 소유자 세대 점유(임차인 없음)",
            "선순위 임차세대 점유 (말소기준일 이전 대항력 요건 구비·세부일자 비공개)",
            "채무자 법인 자가 가동 중단 상태",
            "임차인 대항력 유지 및 배당요구 없음"
        )
        legalPhrases.forEach { phrase ->
            val leaks = PrivacySanitizerEngine.detectPersonalInfoLeaks(phrase)
            assertTrue("False positive leak on '$phrase': $leaks", leaks.isEmpty())
            assertEquals(phrase, PrivacySanitizerEngine.sanitizeForPublicBlog(phrase))
        }
    }

    @Test
    fun qaAnd30PointEvaluator_rejectPersonalInfoLeakAndMissingSignupFunnel() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val baseSlot = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg, funnel).first()

        // 1. Inject debtor name & masked tenant surname into Naver body
        val leakedNaver = baseSlot.variants[ChannelPlatform.NAVER]!!.copy(
            bodyText = baseSlot.variants[ChannelPlatform.NAVER]!!.bodyText + "\n채무자 김철수 실거주, 임차인 박OO 점유"
        )
        val leakedVariants = baseSlot.variants + (ChannelPlatform.NAVER to leakedNaver)
        val leakedQa = QaVerificationEngine.auditSlot(
            auctionCase = baseSlot.auctionCase,
            variants = leakedVariants,
            scheduledTimes = baseSlot.scheduledTimes,
            config = cfg
        )
        assertFalse("PII leak must fail QA overallPassed", leakedQa.overallPassed)
        assertTrue(leakedQa.forbiddenHits.any { it.contains("개인정보") })

        val leakedEval = Article30PointEvaluator.evaluate(
            slot = baseSlot.copy(variants = leakedVariants, qaReport = leakedQa),
            config = cfg,
            proFunnel = funnel
        )
        val eval24 = leakedEval.items.first { it.id == "eval-24" }
        assertFalse("eval-24 must fail on PII leak", eval24.passed)

        // 2. Strip signup / 딱찰 CTA from Naver body and verify eval-26 fails
        val noFunnelNaver = baseSlot.variants[ChannelPlatform.NAVER]!!.copy(
            bodyText = baseSlot.variants[ChannelPlatform.NAVER]!!.bodyText
                .replace("딱찰", "일반")
                .replace("회원가입", "안내")
                .replace("가입", "안내")
        )
        val noFunnelVariants = baseSlot.variants + (ChannelPlatform.NAVER to noFunnelNaver)
        val noFunnelEval = Article30PointEvaluator.evaluate(
            slot = baseSlot.copy(variants = noFunnelVariants),
            config = cfg,
            proFunnel = funnel
        )
        val eval26 = noFunnelEval.items.first { it.id == "eval-26" }
        assertFalse("eval-26 must fail when signup CTA is missing", eval26.passed)
    }

    @Test
    fun allDefaultSlotsAndBusanCases_haveZeroPii_exceedPaidApps_andIncludeDdakchalSignupFunnel() {
        val cfg = ServerAutomationConfig()
        val funnel = ProFunnelConfig()
        val slots = DefaultSpecDataFactory.buildInitialDaily5Slots(cfg, funnel).toMutableList()

        CourtEvidenceRecorder.findProfitableBusanLandUnder50M().forEachIndexed { idx, auction ->
            val variants = DefaultSpecDataFactory.buildDistinct3ChannelVariants(auction, idx + 5, cfg, proFunnel = funnel)
            val scheduled = mapOf(
                ChannelPlatform.CLOUDFLARE to "2026-11-22 08:17",
                ChannelPlatform.TISTORY to "2026-11-22 09:04",
                ChannelPlatform.NAVER to "2026-11-22 10:19"
            )
            val qa = QaVerificationEngine.auditSlot(auction, variants, scheduled, cfg)
            slots += GeneratedPostSlot(
                postId = "post-busan-$idx",
                queueId = "q-busan-$idx",
                slotIndex = slots.size + 1,
                publishDate = "2026-11-22",
                auctionCase = auction,
                variants = variants,
                scheduledTimes = scheduled,
                status = PostStatus.QUEUED,
                qaReport = qa
            )
        }

        slots.forEach { slot ->
            assertTrue("QA failed for ${slot.auctionCase.caseId}: mismatches=${slot.qaReport.factCheckMismatches}, hits=${slot.qaReport.forbiddenHits}, chars=${slot.qaReport.charCountByChannel}", slot.qaReport.overallPassed)

            // Verify zero PII leaks in variants, court archive, registry template, motion script
            slot.variants.values.forEach { v ->
                val leaks = PrivacySanitizerEngine.detectPersonalInfoLeaks("${v.title}\n${v.bodyText}\n${v.pinnedCommentSnippet}")
                assertTrue("PII leak found in ${slot.auctionCase.caseId} ${v.platform}: $leaks", leaks.isEmpty())
                assertTrue("${slot.auctionCase.caseId} ${v.platform} must include '딱찰'", v.bodyText.contains("딱찰"))
                assertTrue(
                    "${slot.auctionCase.caseId} ${v.platform} must guide to signup",
                    v.bodyText.contains("회원가입") || v.bodyText.contains("가입")
                )
            }

            // Naver URL-free guarantee
            val nv = slot.variants[ChannelPlatform.NAVER]!!
            assertFalse(nv.bodyText.contains("http://"))
            assertFalse(nv.bodyText.contains("https://"))
            assertTrue(nv.pinnedCommentSnippet.contains("딱찰"))
            assertTrue(nv.pinnedCommentSnippet.contains("회원가입"))

            // Deep analysis superiority checks
            val report = ProAnalysisAndPhotoGuideEngine.buildProReport(slot.auctionCase)
            assertTrue(report.breakevenResalePriceKrw > slot.auctionCase.balancedBidKrw)
            assertEquals((slot.auctionCase.minBidPriceKrw * 80L) / 100L, report.nextDropMinBidKrw)
            assertTrue(report.rateStressMonthlyDeltaKrw > 0L)
            assertTrue(report.privacyMaskedConfirmed)

            val archive = CourtEvidenceRecorder.recordFullArchive(slot.auctionCase)
            val archiveText = archive.documents.flatMap { it.fullRecordLines }.joinToString("\n")
            assertTrue(PrivacySanitizerEngine.detectPersonalInfoLeaks(archiveText).isEmpty())

            val reg = RegistryInfographicEngine.buildTemplate(slot.auctionCase)
            val motion = RegistryAndMotionEngine.build5SceneMotionGuide(slot.auctionCase, report, funnel)
            assertTrue(motion.last().narrationScript.contains("딱찰") && motion.last().narrationScript.contains("회원가입"))

            val eval = Article30PointEvaluator.evaluate(slot, cfg, funnel, reg)
            assertTrue(
                "30-point eval must pass >=95 for ${slot.auctionCase.caseId}, got ${eval.totalScore}. Failed: ${eval.items.filter { !it.passed }.map { it.id }}",
                eval.eligibleForRegistration && eval.totalScore >= 95
            )
        }

        val benchmarkRows = ProAnalysisAndPhotoGuideEngine.buildPaidAppVsBlogBenchmark()
        assertEquals(7, benchmarkRows.size)
        assertTrue(benchmarkRows.any { it.dimension.contains("개인정보") })
        assertTrue(benchmarkRows.any { it.ddakchalSignupProLevel.contains("딱찰") && it.ddakchalSignupProLevel.contains("회원가입") })
    }
}
