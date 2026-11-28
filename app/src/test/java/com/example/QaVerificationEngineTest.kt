package com.example

import com.example.engine.QaVerificationEngine
import com.example.model.ChannelPlatform
import com.example.model.ServerAutomationConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QaVerificationEngineTest {

    @Test
    fun daily5Slots_passCrossChannelSimilarityAndFactCheck() {
        val config = ServerAutomationConfig(enableExternalLinkNaver = false)
        val slots = TestFixtures.sampleSlots(config)
        assertEquals(5, slots.size)

        slots.forEach { slot ->
            val qa = slot.qaReport
            assertTrue("Expected similarity < 30% for ${slot.auctionCase.caseId} (was ${qa.maxCrossChannelCosinePct}%)", qa.similarityPassed)
            assertTrue("Expected 0% fact-check mismatch for ${slot.auctionCase.caseId}: ${qa.factCheckMismatches}", qa.factCheckPassed)
            assertTrue("Expected no forbidden terms for ${slot.auctionCase.caseId}", qa.forbiddenHits.isEmpty())
            assertTrue("Expected external link rule pass for ${slot.auctionCase.caseId}", qa.externalLinkRuleOk)
            assertTrue("Expected distinct image hashes & 4~7 cards for ${slot.auctionCase.caseId}", qa.imageCountAndHashOk)
            assertTrue("Expected JSON-LD valid for ${slot.auctionCase.caseId}", qa.jsonLdValid)
            assertTrue("Expected char counts 1200..2500 for ${slot.auctionCase.caseId} (was ${qa.charCountByChannel})", qa.charCountOkByChannel.values.all { it })
            assertTrue("Expected schedule jitter valid for ${slot.auctionCase.caseId} (was ${slot.scheduledTimes})", qa.scheduleJitterValid)
            assertTrue("Expected overall pass for ${slot.auctionCase.caseId}", qa.overallPassed)
        }
    }

    @Test
    fun duplicateTextBetweenChannels_isBlockedBySimilarityGate() {
        val config = ServerAutomationConfig(enableExternalLinkNaver = false)
        val slot = TestFixtures.sampleSlots(config).first()
        val cfVariant = slot.variants[ChannelPlatform.CLOUDFLARE]!!
        val duplicatedVariants = slot.variants.toMutableMap().apply {
            put(ChannelPlatform.TISTORY, slot.variants[ChannelPlatform.TISTORY]!!.copy(bodyText = cfVariant.bodyText))
        }
        val qa = QaVerificationEngine.auditSlot(slot.auctionCase, duplicatedVariants, slot.scheduledTimes, config)
        assertFalse("Duplicate body across channels must be rejected", qa.similarityPassed)
        assertFalse("Overall pass must be false when similarity >= 30%", qa.overallPassed)
    }
}
