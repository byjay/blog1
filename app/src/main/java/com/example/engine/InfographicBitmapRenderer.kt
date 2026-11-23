package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.AuctionCaseIntel
import com.example.model.ChannelPlatform
import com.example.model.CourtPhotoEvidence
import com.example.model.InfographicCardSpec
import com.example.model.ProAuctionAnalysisReport
import com.example.model.ProFunnelConfig
import com.example.model.PropertyType
import com.example.model.RegistryInfographicTemplate
import java.io.File
import java.io.FileOutputStream

object InfographicBitmapRenderer {

    fun renderCardToShareUri(
        context: Context,
        card: InfographicCardSpec,
        auction: AuctionCaseIntel,
        platform: ChannelPlatform,
        report: ProAuctionAnalysisReport,
        funnel: ProFunnelConfig
    ): Uri {
        val (width, height) = when (platform) {
            ChannelPlatform.CLOUDFLARE -> 1200 to 630
            ChannelPlatform.TISTORY -> 1200 to 675
            ChannelPlatform.NAVER -> 800 to 800
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawInfographicCard(canvas, width.toFloat(), height.toFloat(), card, auction, platform, report, funnel)

        val dir = File(context.cacheDir, "infographics").apply { mkdirs() }
        val safeName = card.seoFilename.replace(".webp", ".png").ifBlank { "${auction.caseId}_${card.cardIndex}.png" }
        val outFile = File(dir, safeName)
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        bitmap.recycle()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }

    fun drawableResForPropertyType(type: PropertyType): Int = when (type) {
        PropertyType.LAND_FARMLAND -> R.drawable.img_court_land_site
        PropertyType.APT_OFFICETEL, PropertyType.VILLA_MULTIFAMILY -> R.drawable.img_court_apt_exterior
        PropertyType.COMMERCIAL_RETAIL, PropertyType.FACTORY_SPECIAL -> R.drawable.img_court_commercial_site
    }

    fun renderCourtEvidencePhotoToShareUri(
        context: Context,
        auction: AuctionCaseIntel,
        photo: CourtPhotoEvidence
    ): Uri {
        val w = 1200
        val h = 900
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val srcRes = drawableResForPropertyType(auction.propertyType)
        val rawPhoto = BitmapFactory.decodeResource(context.resources, srcRes)
        if (rawPhoto != null) {
            val cropRect = when (photo.shotOrder) {
                2 -> Rect(rawPhoto.width / 8, rawPhoto.height / 6, rawPhoto.width, rawPhoto.height)
                3 -> Rect(0, rawPhoto.height / 5, (rawPhoto.width * 0.85f).toInt(), rawPhoto.height)
                4 -> Rect(rawPhoto.width / 10, 0, rawPhoto.width, (rawPhoto.height * 0.85f).toInt())
                5 -> Rect(0, 0, (rawPhoto.width * 0.9f).toInt(), (rawPhoto.height * 0.9f).toInt())
                else -> Rect(0, 0, rawPhoto.width, rawPhoto.height)
            }
            canvas.drawBitmap(rawPhoto, cropRect, Rect(0, 0, w, h - 180), null)
            rawPhoto.recycle()
        } else {
            canvas.drawColor(0xFF0F172A.toInt())
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF080D16.toInt() }
        canvas.drawRect(0f, (h - 180).toFloat(), w.toFloat(), h.toFloat(), footerPaint)

        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF34D399.toInt()
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("[${photo.courtSourceLabel}] ${auction.caseId}", 36f, (h - 126).toFloat(), accentPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("컷 ${photo.shotOrder}. ${photo.title} (${photo.captureAngle})", 36f, (h - 80).toFloat(), titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            textSize = 21f
        }
        canvas.drawText("${photo.observationNote.take(54)} · ${photo.exifStatusLabel}", 36f, (h - 34).toFloat(), subPaint)

        val dir = File(context.cacheDir, "infographics").apply { mkdirs() }
        val outFile = File(dir, "${auction.caseId}_court_photo_${photo.shotOrder}.png")
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        bitmap.recycle()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }

    fun renderRegistryTemplateToShareUri(
        context: Context,
        template: RegistryInfographicTemplate
    ): Uri {
        val w = 1200f
        val h = 800f
        val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w, h,
                intArrayOf(0xFF070C15.toInt(), 0xFF0F172A.toInt(), 0xFF092320.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0xFFF59E0B.toInt()
            alpha = 130
        }
        canvas.drawRoundRect(RectF(20f, 20f, w - 20f, h - 20f), 26f, 26f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(template.headline.take(42), 48f, 84f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF34D399.toInt()
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("[${template.templateType.label}] ${template.subHeadline.take(54)}", 48f, 124f, subPaint)

        // Ownership Box
        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1E293B.toInt() }
        canvas.drawRoundRect(RectF(48f, 148f, w - 48f, 240f), 18f, 18f, panelPaint)
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            textSize = 22f
        }
        canvas.drawText("소유권 현황: ${template.ownershipSummaryCard.currentOwnerLabel}", 72f, 186f, subPaint)
        canvas.drawText(template.ownershipSummaryCard.ownershipRiskNote.take(58), 72f, 222f, bodyPaint)

        // Gap-gu & Eul-gu split panels
        canvas.drawRoundRect(RectF(48f, 260f, 584f, 640f), 18f, 18f, panelPaint)
        canvas.drawRoundRect(RectF(616f, 260f, w - 48f, 640f), 18f, 18f, panelPaint)

        val colHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF38BDF8.toInt()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("갑구 (소유권 · 압류 · 경매)", 72f, 304f, colHeaderPaint)
        var gy = 350f
        template.gapGuNodes.take(3).forEach { node ->
            canvas.drawText("${node.rankOrder} ${node.title.take(16)}", 72f, gy, subPaint)
            canvas.drawText("→ ${node.statusBadge} (${node.registeredDate})", 72f, gy + 30f, bodyPaint)
            gy += 92f
        }

        val eulHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF59E0B.toInt()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("을구 (근저당 · 말소기준 · 인수)", 640f, 304f, eulHeaderPaint)
        var ey = 350f
        template.eulGuNodes.take(3).forEach { node ->
            val p = if (node.isAssumedRisk) Paint(subPaint).apply { color = 0xFFF87171.toInt() } else subPaint
            canvas.drawText("${node.rankOrder} ${node.title.take(16)}", 640f, ey, p)
            canvas.drawText("→ ${node.statusBadge} · ${node.amountOrShare.take(18)}", 640f, ey + 30f, bodyPaint)
            ey += 92f
        }

        // Bottom Conclusion Bar
        canvas.drawRoundRect(RectF(48f, 658f, w - 48f, 756f), 18f, 18f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF064E3B.toInt() })
        canvas.drawText(template.cancellationBaselineSummary.take(62), 72f, 698f, bodyPaint)
        canvas.drawText(template.buyerTakeoverSummary.take(62), 72f, 734f, subPaint)

        val dir = File(context.cacheDir, "infographics").apply { mkdirs() }
        val outFile = File(dir, "${template.caseId}_registry_${template.templateType.name.lowercase()}.png")
        FileOutputStream(outFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        bitmap.recycle()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
    }

    private fun drawInfographicCard(
        canvas: Canvas,
        w: Float,
        h: Float,
        card: InfographicCardSpec,
        auction: AuctionCaseIntel,
        platform: ChannelPlatform,
        report: ProAuctionAnalysisReport,
        funnel: ProFunnelConfig
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w, h,
                intArrayOf(0xFF080D16.toInt(), 0xFF0F172A.toInt(), 0xFF092320.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        val accentColor = when (platform) {
            ChannelPlatform.CLOUDFLARE -> 0xFF38BDF8.toInt()
            ChannelPlatform.TISTORY -> 0xFFF59E0B.toInt()
            ChannelPlatform.NAVER -> 0xFF03C75A.toInt()
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = accentColor
            alpha = 110
        }
        canvas.drawRoundRect(RectF(20f, 20f, w - 20f, h - 20f), 28f, 28f, borderPaint)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF064E3B.toInt()
        }
        canvas.drawRoundRect(RectF(48f, 44f, w * 0.62f, 96f), 24f, 24f, badgePaint)

        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF34D399.toInt()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("EST.1996 · 30년+ · 100인+ 딱찰 경매법인 | ${platform.displayName}", 68f, 79f, badgeTextPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            textSize = if (w >= 1100f) 40f else 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("${auction.caseId} · ${auction.propertyType.displayLabel}", 48f, 156f, titlePaint)

        val headlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = if (w >= 1100f) 28f else 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(card.headline.take(44), 48f, 206f, headlinePaint)

        val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
        }
        canvas.drawRoundRect(RectF(48f, 236f, w - 48f, h - 108f), 22f, 22f, panelPaint)

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            textSize = if (w >= 1100f) 26f else 22f
        }
        val subAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF34D399.toInt()
            textSize = if (w >= 1100f) 26f else 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val lines = when (card.cardType) {
            "THUMBNAIL_SUMMARY" -> listOf(
                "감정가: ${QaVerificationEngine.formatKrwShort(auction.appraisalPriceKrw)}",
                "최저가: ${QaVerificationEngine.formatKrwShort(auction.minBidPriceKrw)} (${auction.discountRatePct}%)",
                "시세 안전마진: ${QaVerificationEngine.formatKrwShort(report.safetyMarginKrw)} (${report.safetyMarginPct}%)",
                "매각기일: ${auction.auctionDate} · 말소기준: ${auction.baseRightDate}"
            )
            "COURT_DOCS_RIGHTS" -> RegistryAndMotionEngine.buildRegistryFacts(auction).take(4).map { entry ->
                "[${entry.orderNo}] ${entry.registeredDate} ${entry.rightTitle.take(16)} → ${entry.outcomeLabel}"
            }
            "STATS_3Y_AND_TRADES" -> listOf(
                "실거래 중위가: ${QaVerificationEngine.formatKrwShort(auction.recentRealTradeMedianKrw)}",
                "3개년 평균 낙찰가율: ${auction.stats3yRatePct}% · 7일 클릭 ${auction.clicksLast7d}건",
                "임대 CapRate: 연 ${report.estimatedCapRatePct}% · 12개월 ROE ${report.estimated12mRoiPct}%",
                "LTV 70% 대출 시 실투자금: ${QaVerificationEngine.formatKrwShort(report.netEquityRequiredKrw)}"
            )
            "BID_TAX_WATERFALL" -> listOf(
                "보수 입찰가: ${QaVerificationEngine.formatKrwShort(auction.conservativeBidKrw)}",
                "균형 입찰가: ${QaVerificationEngine.formatKrwShort(auction.balancedBidKrw)}",
                "공격 입찰가: ${QaVerificationEngine.formatKrwShort(auction.aggressiveBidKrw)}",
                "세무 3안 비교: 개인단독 · 부부공동(5:5 분산) · 법인(경비처리)"
            )
            "FIELD_PHOTO_GUIDE" -> report.photoGuideShots.take(4).map { s ->
                "[촬영 ${s.shotNumber}] ${s.targetTitle} — ${s.checkPoint.take(26)}"
            }
            else -> listOf(
                funnel.teaserHeadline.take(40),
                "검색 안내: '${funnel.naverSearchKeyword} ${auction.caseId}'",
                funnel.proLockedItems.getOrElse(0) { "실시간 경쟁률 히트맵" },
                funnel.proLockedItems.getOrElse(1) { "등기부 변동 실시간 푸시" }
            )
        }

        var y = 300f
        lines.forEachIndexed { idx, line ->
            val p = if (idx == 1) subAccentPaint else bodyPaint
            canvas.drawText(line, 80f, y, p)
            y += if (h >= 750f) 90f else 66f
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            textSize = 20f
        }
        canvas.drawText(
            "EXIF 100% 삭제 · MD5:${card.md5Hash} · SHA256:${card.sha256Hash} · ${card.aspectRatio}",
            48f,
            h - 48f,
            footerPaint
        )
    }
}
