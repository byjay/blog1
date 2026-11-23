package com.example.engine

import com.example.model.GeneratedPostSlot
import com.example.model.LowQualityPolicyGuide
import com.example.model.SeoChecklistItem
import com.example.model.SeoRuleCategory
import com.example.model.SeoRulePriority
import com.example.model.ServerAutomationConfig

object SeoGuideEngine {

    fun policyGuides(): List<LowQualityPolicyGuide> = listOf(
        LowQualityPolicyGuide(
            id = "guide-naver-crank-dia",
            title = "네이버 C-Rank · D.I.A.+ · 스마트블록 지수 유지 정책",
            engineFocus = "주제 집중도(C-Rank) · 경험/정보 밀도(D.I.A.+) · 체류시간",
            summary = "단일 니치(법원 경매·권리분석) 주제 일관성을 유지하고 외부 이탈 URL·상업적 스팸 어휘를 원천 배제하여 스마트블록 상위 노출 지수를 방어합니다.",
            keyPolicies = listOf(
                "본문 내 외부 도메인 직접 링크 차단(ENABLE_EXTERNAL_LINK_NAVER=false) — 하단 배너 이미지 및 프로필/고정 댓글로 전환 유도",
                "모바일 가독성 기준 2~3줄 단위 짧은 줄바꿈, Q1~Q5 구조화 블록 및 공백 제외 1,200~2,500자 정보 밀도 유지",
                "매 발행마다 15종 이상 도입 훅·전환구·CTA 풀을 순환시켜 기계적 반복 문구(템플릿 패턴) 탐지 회피",
                "동일 계정 내 부동산 경매 외 무관한 주제(잡블로그화) 혼재 금지로 C-Rank 신뢰도 유지"
            ),
            penaltyTriggers = listOf(
                "원금 보장, 100% 무조건 수익, 확정 수익 등 과장·투자유도 금칙어 사용",
                "동일 제목·동일 이미지 반복 업로드 또는 1일 과다 발행(초기 계정 1~2건 초과)",
                "타 플랫폼(티스토리·자체웹) 본문과 30% 이상 문장 중복 시 누락(저품질) 처리"
            ),
            category = SeoRuleCategory.NAVER
        ),
        LowQualityPolicyGuide(
            id = "guide-tistory-daum",
            title = "티스토리 · 다음(Daum) · 카카오 검색 SEO 정책",
            engineFocus = "시맨틱 HTML 구조 · 수익형 링크 속성 · 데이터 테이블",
            summary = "다음/구글 유입에 최적화된 H2/H3 계층형 리포트 구조와 HTML 비교표를 구성하고, 외부 전환 링크에는 반드시 rel=\"nofollow sponsored\"를 부여합니다.",
            keyPolicies = listOf(
                "H1(1회) → H2 → H3 시맨틱 헤딩 계층 준수 및 사건 제원·3전략 입찰가 HTML <table> 삽입",
                "모든 외부 서비스/제휴 전환 링크에 rel=\"nofollow sponsored\" 속성 적용으로 어뷰징 페널티 차단",
                "120~155자 핵심 요약 메타 설명(meta description) 및 1200x675(16:9) 전용 썸네일 카드 배치",
                "단순 요약 대신 3개년 낙찰통계, LTV·ROE·세무 3안(개인/부부공동/법인) 등 차별화된 수치 분석 제공"
            ),
            penaltyTriggers = listOf(
                "nofollow/sponsored 누락된 무분별한 외부 홍보 링크 대량 삽입",
                "텍스트 빈약(1,000자 미만) 상태에서 태그만 과다 나열하는 태그 스패밍",
                "렌더링 오류 문자(undefined, NaN, null) 노출"
            ),
            category = SeoRuleCategory.TISTORY
        ),
        LowQualityPolicyGuide(
            id = "guide-google-geo-eeat",
            title = "구글 E-E-A-T · Cloudflare GEO/AIO(AI 검색) 정책",
            engineFocus = "Answer-First 요약 · Schema.org JSON-LD · 0% 팩트 정합성",
            summary = "구글 스팸 업데이트(Helpful Content) 및 AI Overview 인용 기준에 맞춰 대법원 4대 공부 1차 출처를 명확히 인용하고 구조화 데이터를 제공합니다.",
            keyPolicies = listOf(
                "상단 TL;DR(핵심 요약 박스) 선배치 후 공시 원문·실무 FAQ를 제공하는 Answer-First 구조 채택",
                "페이지마다 Schema.org Article + FAQPage JSON-LD 스키마를 유효한 JSON으로 동시 삽입",
                "사건번호·감정가·최저매각가·말소기준권리 0% 팩트체크 및 데이터 기준일·출처 명시(E-E-A-T 신뢰성)",
                "Cloudflare 자체웹(/blog/:id)을 1순위(T+0분)로 선색인시켜 원본 저작권(Canonical) 우선권 확보"
            ),
            penaltyTriggers = listOf(
                "팩트 오류(감정가·최저가·권리일 불일치) 또는 출처 불명확한 자동생성 대량 양산(Scaled Content Abuse)",
                "구조화 데이터(JSON-LD) 내 본문과 불일치하는 허위 FAQ 삽입",
                "이미지 alt 텍스트 누락 및 웹표준(1200x630 OG 이미지) 미준수"
            ),
            category = SeoRuleCategory.GOOGLE_GEO
        ),
        LowQualityPolicyGuide(
            id = "guide-antibot-dedup",
            title = "3채널 유사문서 차단 & 안티-봇(Anti-Bot) 안전 수칙",
            engineFocus = "유사도 <30% 게이트 · 해시/EXIF 변형 · 정각(:00) 회피 지터",
            summary = "자동화 봇 패턴 탐지를 피하기 위해 발행 시각·이미지 바이너리·문서 벡터를 채널별로 완전히 분리하고 보안문자 발생 시 즉시 중단합니다.",
            keyPolicies = listOf(
                "3채널 상호 유사도 및 최근 7일 발행 코퍼스 대비 코사인·5-gram Jaccard 30% 미만 강제(권장 <25%)",
                "채널별 해상도 변형(1200x630 / 1200x675 / 800x800) + 미세 크롭 + EXIF 메타데이터 100% 삭제로 MD5/SHA-256 해시 분리",
                "정각(:00:00) 발행 금지 및 랜덤 지터(T+0 → T+35~60분 → T+100~160분) 스케줄링 적용",
                "CAPTCHA/2FA 감지 시 무한 재시도를 금지하고 handOff() 알림 후 1회 수동 세션 갱신"
            ),
            penaltyTriggers = listOf(
                "매시 정각(:00) 고정 크론으로 매일 같은 초·분에 기계적 발행",
                "동일 이미지 파일을 리사이즈/EXIF 제거 없이 3개 블로그에 중복 업로드",
                "로그인 실패/캡차 상태에서 반복 로그인 시도로 계정 영구 보호조치 유발"
            ),
            category = SeoRuleCategory.ANTI_BOT
        )
    )

    fun defaultChecklistItems(): List<SeoChecklistItem> = listOf(
        SeoChecklistItem(
            id = "chk-nv-no-ext-link",
            title = "네이버 본문 직접 외부 URL 삽입 차단",
            description = "ENABLE_EXTERNAL_LINK_NAVER=false 유지, 하단 배너 이미지 및 프로필/고정 댓글로 유도합니다.",
            category = SeoRuleCategory.NAVER,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "NAVER_NO_DIRECT_URL",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-nv-char-range",
            title = "공백 제외 본문 1,200~2,500자 & 2~3줄 모바일 줄바꿈",
            description = "네이버 D.I.A.+ 체류시간 확보를 위해 과소(1,200자 미만)·과다(2,500자 초과) 분량을 방지합니다.",
            category = SeoRuleCategory.NAVER,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "CHAR_COUNT_RANGE",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-nv-boilerplate-pool",
            title = "15종 이상 도입부·전환구·CTA 문구 동적 순환",
            description = "고정된 상용구/면책문구 반복으로 인한 AI 스팸 패턴 탐지를 차단합니다.",
            category = SeoRuleCategory.NAVER,
            priority = SeoRulePriority.RECOMMENDED,
            isChecked = true,
            ruleCode = "DYNAMIC_BOILERPLATE_POOL",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-ts-nofollow",
            title = "티스토리 전환 링크 rel=\"nofollow sponsored\" 필수 적용",
            description = "외부 링크가 포함된 티스토리 원고에 검색엔진 권장 링크 속성을 누락 없이 삽입합니다.",
            category = SeoRuleCategory.TISTORY,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "TISTORY_NOFOLLOW_SPONSORED",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-ts-semantic-table",
            title = "H2/H3 계층 구조 & HTML 비교표(4대공부·세무3안) 포함",
            description = "단순 나열 대신 사건 스펙 표와 개인/부부공동/법인 3안 세무 비교표를 포함해 체류시간을 높입니다.",
            category = SeoRuleCategory.TISTORY,
            priority = SeoRulePriority.RECOMMENDED,
            isChecked = true,
            ruleCode = "TISTORY_SEMANTIC_TABLE",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-geo-jsonld",
            title = "Cloudflare 자체웹 JSON-LD(Article + FAQPage) 동시 검증",
            description = "구글 리치 결과 및 AI Overview 인용을 위해 유효한 Article·FAQPage 스키마를 삽입합니다.",
            category = SeoRuleCategory.GOOGLE_GEO,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "CF_JSON_LD_VALID",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-geo-fact-0pct",
            title = "사건번호·감정가·최저가·말소기준권리 0% 팩트체크",
            description = "구글 Scaled Content Abuse 방지를 위해 법원 4대 공부 핵심 수치 오차를 0%로 검증합니다.",
            category = SeoRuleCategory.GOOGLE_GEO,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "FACT_CHECK_ZERO_ERROR",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-geo-canonical-first",
            title = "1순위 Cloudflare(T+0분) 선색인 후 포털 시간차 발행",
            description = "자체웹 원본이 먼저 색인되도록 Cloudflare → 티스토리 → 네이버 순서를 엄수합니다.",
            category = SeoRuleCategory.GOOGLE_GEO,
            priority = SeoRulePriority.RECOMMENDED,
            isChecked = true,
            ruleCode = "STAGGERED_CANONICAL_ORDER",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-bot-similarity-30",
            title = "3채널 및 최근 7일 글 대비 유사도 < 30% (권장 < 25%)",
            description = "Cosine 유사도와 5-gram Jaccard 모두 30% 미만일 때만 발행 큐 진입을 허용합니다.",
            category = SeoRuleCategory.ANTI_BOT,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "SIMILARITY_UNDER_30",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-bot-image-hash",
            title = "채널당 4~7장 인포그래픽 · EXIF 100% 삭제 · 해시 변형",
            description = "동일 파일 재사용을 막기 위해 채널별 해상도·미세 크롭을 적용해 MD5/SHA-256을 분리합니다.",
            category = SeoRuleCategory.ANTI_BOT,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "IMAGE_EXIF_HASH_DISTINCT",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-bot-no-exact-hour",
            title = "정각(:00) 크론·예약 금지 및 일일 한도(1~2건 권장) 준수",
            description = "Cron 및 예약 시각의 분·초가 :00으로 떨어지지 않도록 랜덤 지터와 DAILY_POST_LIMIT을 적용합니다.",
            category = SeoRuleCategory.ANTI_BOT,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "NO_EXACT_HOUR_JITTER",
            updatedAt = "2026-11-22 08:17"
        ),
        SeoChecklistItem(
            id = "chk-bot-forbidden-terms",
            title = "금칙어('100% 무조건 수익', undefined, NaN) 필터 점검",
            description = "투자 과장 표현 및 템플릿 바인딩 오류 문자열 노출 여부를 발행 전 최종 차단합니다.",
            category = SeoRuleCategory.ANTI_BOT,
            priority = SeoRulePriority.CRITICAL,
            isChecked = true,
            ruleCode = "FORBIDDEN_TERMS_FILTER",
            updatedAt = "2026-11-22 08:17"
        )
    )

    fun evaluateLiveAutoNote(
        item: SeoChecklistItem,
        config: ServerAutomationConfig,
        slots: List<GeneratedPostSlot>
    ): String? = when (item.ruleCode) {
        "NAVER_NO_DIRECT_URL" ->
            if (!config.enableExternalLinkNaver) "현재 설정: 차단 적용 중 (안전)"
            else "현재 설정: 하단 1회 허용 (주의 필요)"
        "CHAR_COUNT_RANGE" -> {
            val allOk = slots.isNotEmpty() && slots.all { s -> s.qaReport.charCountOkByChannel.values.all { it } }
            if (allOk) "현재 큐 ${slots.size}건 전 채널 1,200~2,500자 통과" else "일부 슬롯 분량 확인 필요"
        }
        "TISTORY_NOFOLLOW_SPONSORED" -> {
            val allOk = slots.isNotEmpty() && slots.all { it.qaReport.externalLinkRuleOk }
            if (allOk) "현재 큐 ${slots.size}건 rel=\"nofollow sponsored\" 확인" else "외부링크 규칙 미통과 슬롯 있음"
        }
        "CF_JSON_LD_VALID" -> {
            val allOk = slots.isNotEmpty() && slots.all { it.qaReport.jsonLdValid }
            if (allOk) "Article + FAQPage JSON-LD 전수 검증 통과" else "JSON-LD 누락 슬롯 확인 필요"
        }
        "FACT_CHECK_ZERO_ERROR" -> {
            val allOk = slots.isNotEmpty() && slots.all { it.qaReport.factCheckPassed }
            if (allOk) "현재 큐 ${slots.size}건 금액·권리일 0% 오차" else "팩트 불일치 슬롯 감지"
        }
        "SIMILARITY_UNDER_30" -> {
            val maxSim = slots.maxOfOrNull { it.qaReport.maxCrossChannelCosinePct } ?: 0.0
            if (maxSim < 30.0) "현재 큐 최대 유사도 ${maxSim}% (<30% 통과)" else "유사도 ${maxSim}% 초과 경고"
        }
        "IMAGE_EXIF_HASH_DISTINCT" -> {
            val allOk = slots.isNotEmpty() && slots.all { it.qaReport.imageCountAndHashOk }
            if (allOk) "전 채널 4~7장 · EXIF 삭제 · 해시 분리 통과" else "이미지 해시/수량 점검 필요"
        }
        "NO_EXACT_HOUR_JITTER" -> {
            val cronOk = !config.cronExpression.trim().startsWith("0 ")
            val quotaNote = "일일 한도 ${config.dailyPostLimit}건 · 지터 ${config.minDelayMinutes}~${config.maxDelayMinutes}분"
            if (cronOk) "Cron '${config.cronExpression}' 정각 회피 · $quotaNote" else "경고: Cron 정각(:00) 수정 필요"
        }
        "FORBIDDEN_TERMS_FILTER" -> {
            val hits = slots.sumOf { it.qaReport.forbiddenHits.size }
            if (hits == 0) "금칙어·렌더오류 문자열 0건" else "금칙어 ${hits}건 탐지됨"
        }
        else -> null
    }
}
