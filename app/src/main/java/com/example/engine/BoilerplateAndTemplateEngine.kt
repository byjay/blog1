package com.example.engine

import com.example.model.PropertyTemplateYaml
import com.example.model.PropertyType
import com.example.model.ServerAutomationConfig

object BoilerplateAndTemplateEngine {

    val introHookPool: List<String> = listOf(
        "오늘 입찰 현장에서 가장 먼저 숫자를 맞춰봐야 할 물건부터 짚어드립니다.",
        "유찰이 거듭되며 최저가가 시세 아래로 내려왔을 때가 진짜 옥석 가리기의 시작입니다.",
        "등기부상 말소기준권리 날짜 하나만 정확히 읽어도 불필요한 보증금 인수 리스크를 피할 수 있습니다.",
        "최근 일주일간 딱찰 실수요자 조회가 급증한 핵심 권역 사건을 법원 공부 원문 기준으로 정리했습니다.",
        "겉으로 보이는 할인율만 보고 들어가기 전에 배당요구종기와 점유 현황부터 대조해 봅니다.",
        "경매 수익률은 낙찰 순간이 아니라 공용관리비·명도·세무 비용을 뺀 뒤 확정됩니다.",
        "국토부 최근 실거래 흐름과 3개년 시군구 낙찰가율을 나란히 놓고 안전마진을 계산해 봤습니다.",
        "현황조사서와 매각물건명세서 사이 미묘한 단서 한 줄이 입찰가 전략을 완전히 바꿉니다.",
        "초보 투자자가 가장 자주 놓치는 법정지상권·선순위 대항력 체크포인트를 먼저 확인합니다.",
        "이번 회차는 보수적·적정·공격적 3단계 입찰 밴드를 실데이터로 역산해 드립니다.",
        "단순 감정평가액이 아닌 최근 12개월 실제 낙찰 사례와 비교해야 승산이 보입니다.",
        "법원 원문 기록에서 추출한 권리관계 팩트를 일반 투자자 눈높이로 알기 쉽게 풀었습니다.",
        "시세 대비 낙폭이 커진 지금, 인수 부담이 없는지 4대 공부 기준으로 차근히 검증해 봅니다.",
        "수익형·주거용을 가리지 않고 자금 조달과 명의별 세후 수익까지 계산한 브리핑입니다.",
        "현장 임장 전 반드시 출력해 가야 할 핵심 체크리스트만 압축해 정리했습니다."
    )

    val transitionPhrasePool: List<String> = listOf(
        "이어서 대법원 4대 공부에서 확인된 권리관계 핵심 수치를 대조해 드립니다.",
        "그렇다면 실제 낙찰 가능 구간과 안전마진은 어느 선일까요?",
        "이제 배당 워터폴 구조와 낙찰자 인수 위험 유무를 구체적으로 따져볼 차례입니다.",
        "다음으로 최근 36개월 시군구 매각가율 및 클릭 관심도 흐름을 교차 분석해 봅니다.",
        "마지막으로 개인·법인·공동명의 3안 세무 시뮬레이션 결과를 정리합니다."
    )

    val ctaClosingPool: List<String> = listOf(
        "프로필 링크 또는 고정 댓글에서 딱찰 무료 30배 VVIP 권리분석 리포트를 확인해 보세요.",
        "상세 등기부 배당 시뮬레이션과 전체 실거래 비교표는 딱찰 공식 서비스에서 바로 조회할 수 있습니다.",
        "보수·적정·공격 입찰가 상세 산출 내역은 딱찰 홈페이지에서 사건번호 검색으로 확인 가능합니다.",
        "법원 4대 공부 원문 요약과 명도 체크리스트는 프로필 안내를 통해 무료로 열람해 보세요.",
        "실시간 경쟁률 예측과 3안 세무 계산기는 딱찰 스튜디오에서 무료로 체험할 수 있습니다.",
        "매일 아침 업데이트되는 저평가 TOP5 경매 브리핑 알림을 받아보시려면 프로필을 참고해 주세요.",
        "본 브리핑의 상세 배당표와 추가 현장 체크 항목은 딱찰 플랫폼에서 즉시 확인할 수 있습니다.",
        "입찰 당일 변경사항 여부와 최종 매각물건명세서 대조는 딱찰 사건 상세 페이지에서 지원합니다.",
        "임차인 대항력 자동 판정 19규칙 결과는 딱찰 서비스 내 권리분석 탭에서 확인해 보세요.",
        "세금·대출 한도까지 반영한 나만의 맞춤 입찰가 계산은 딱찰 무료 리포트를 활용해 보세요.",
        "관심 물건 알림 설정과 7일 조회 급증 리포트는 딱찰 공식 웹사이트에서 이용하실 수 있습니다.",
        "추가로 궁금한 권리 인수 포인트가 있다면 댓글로 사건번호를 남겨주시거나 프로필을 확인해 주세요.",
        "더 많은 지역별 3개년 낙찰통계 데이터는 딱찰 아카이브에서 자유롭게 비교해 보실 수 있습니다.",
        "안전마진 자동 스캔 및 법원 공부 AI 요약은 딱찰 플랫폼에서 매일 새롭게 제공됩니다.",
        "현장 임장 전 필수 체크카드 저장과 VVIP 리포트 열람은 프로필 링크에서 바로 이어집니다."
    )

    fun defaultYamlTemplates(): List<PropertyTemplateYaml> = PropertyType.entries.map { type ->
        PropertyTemplateYaml(
            propertyType = type,
            yamlContent = buildYamlForType(type),
            lastEditedAt = "2026-11-22 08:17 KST"
        )
    }

    private fun buildYamlForType(type: PropertyType): String = when (type) {
        PropertyType.APT_OFFICETEL -> """
property_type: APT_OFFICETEL
blog_category: "주거용 경매 분석"
target_seo_keywords:
  - "{지역} 아파트 경매"
  - "{단지명} 낙찰가"
  - "{사건번호} 권리분석"
required_court_docs_points:
  - "국토부 실거래가 대비 시세차익 및 안전마진 계산"
  - "학군·역세권·생활 인프라 입지 평가"
  - "대법원 2001다8677 기준 공용관리비 3년 원금 승계 리스크"
  - "말소기준권리 대비 임차인 대항력 및 배당요구종기 준수 여부"
anti_low_quality:
  min_chars_no_space: 1200
  max_chars_no_space: 2500
  image_count_range: [4, 7]
  max_similarity_ratio: 0.25
  naver_direct_url: false
tone_by_channel:
  naver: "친근한 브리핑 + 모바일 2~3줄 줄바꿈 + Q1~Q5 스마트블록"
  tistory: "H2/H3 계층 리포트 + HTML 비교표 + rel=nofollow sponsored"
  cloudflare: "두괄식 TL;DR + FAQ 4문항 + Article/FAQPage JSON-LD"
""".trimIndent()

        PropertyType.VILLA_MULTIFAMILY -> """
property_type: VILLA_MULTIFAMILY
blog_category: "소액투자·빌라 경매"
target_seo_keywords:
  - "{지역} 빌라 경매"
  - "소액 경매 물건"
  - "{사건번호} 선순위 임차인"
required_court_docs_points:
  - "전세사기·선순위 임차인 미배당 보증금 인수 위험 정밀 검증"
  - "다가구 전입세대열람내역 호수별 대조 및 확정일자 확인"
  - "개별공시지가 대비 감정가·최저가 비율 분석"
  - "모아타운·신속통합기획·재개발 호재 및 위반건축물 여부"
anti_low_quality:
  min_chars_no_space: 1200
  max_chars_no_space: 2500
  image_count_range: [4, 7]
  max_similarity_ratio: 0.25
  naver_direct_url: false
tone_by_channel:
  naver: "리스크 경고 중심 친근한 체크리스트 + Q&A"
  tistory: "호수별 임차인 배당 워터폴 표 + 구글/다음 SEO"
  cloudflare: "GEO 요약 + 전세사기 회피 FAQPage 스키마"
""".trimIndent()

        PropertyType.COMMERCIAL_RETAIL -> """
property_type: COMMERCIAL_RETAIL
blog_category: "수익형·상가 경매"
target_seo_keywords:
  - "{지역} 상가 경매"
  - "수익형 부동산"
  - "{사건번호} 임대수익률"
required_court_docs_points:
  - "상가임대차보호법 환산보증금 및 대항력·갱신요구권 체크"
  - "예상 임대수익률(CapRate) 및 레버리지 세후 수익 분석"
  - "반경 500m 상권 유동인구 및 장기 공실 리스크"
  - "장기공실 체납 공용관리비·클릭/관심도 급증 지표·HBU 3안"
anti_low_quality:
  min_chars_no_space: 1200
  max_chars_no_space: 2500
  image_count_range: [4, 7]
  max_similarity_ratio: 0.25
  naver_direct_url: false
tone_by_channel:
  naver: "투자자 관점 상권·수익률 브리핑 + 짧은 문단"
  tistory: "CapRate 및 HBU 3안 비교표 + 개인/법인 세무표"
  cloudflare: "수익률 TL;DR + 상가임대차 FAQ + JSON-LD"
""".trimIndent()

        PropertyType.LAND_FARMLAND -> """
property_type: LAND_FARMLAND
blog_category: "토지·지분 경매"
target_seo_keywords:
  - "{지역} 토지 경매"
  - "농지취득자격증명"
  - "{사건번호} 맹지 확인"
required_court_docs_points:
  - "지목(전/답/임야/대) 및 용도지역(건폐율·용적률) 분석"
  - "맹지 여부 및 4m 이상 도로 접면 현황조사서 확인"
  - "농지취득자격증명원 매각결정기일 전 제출 필수(미제출 시 보증금 몰수)"
  - "법정지상권·분묘기지권·공유자 우선매수청구권 체크"
anti_low_quality:
  min_chars_no_space: 1200
  max_chars_no_space: 2500
  image_count_range: [4, 7]
  max_similarity_ratio: 0.25
  naver_direct_url: false
tone_by_channel:
  naver: "농취증·보증금 몰수 주의 강조 + 알기 쉬운 토지 해설"
  tistory: "토지이용계획·건폐율/용적률·공법 규제 상세 표"
  cloudflare: "지목/농취증 두괄식 답변 + 토지 Q&A 스키마"
""".trimIndent()

        PropertyType.FACTORY_SPECIAL -> """
property_type: FACTORY_SPECIAL
blog_category: "특수·지식산업센터"
target_seo_keywords:
  - "{지역} 공장 경매"
  - "유치권 분석"
  - "{사건번호} 기계기구"
required_court_docs_points:
  - "공장저당법 제6조 기계기구 목록 실물 존재 여부 현장 대조"
  - "유치권 신고 내역(공사대금·점유 적법성) 파훼 포인트"
  - "산업단지 입주자격 및 업종코드(KISC) 제한 확인"
  - "층고(호이스트)·계약전력(kW)·대형차량(40ft) 진입 가능 여부"
anti_low_quality:
  min_chars_no_space: 1200
  max_chars_no_space: 2500
  image_count_range: [4, 7]
  max_similarity_ratio: 0.25
  naver_direct_url: false
tone_by_channel:
  naver: "기계기구·유치권 실전 점검 스토리텔링"
  tistory: "물리 스펙(층고/전력/진입도로)·유치권 대조 리포트"
  cloudflare: "특수물건 핵심 답변 + 유치권 FAQ JSON-LD"
""".trimIndent()
    }

    fun generateD1MigrationSql(): String = """
-- DDAKCHAL Cloudflare D1 Migration (0010_blog_daily_publish_queue.sql)
CREATE TABLE IF NOT EXISTS generated_posts (
  post_id TEXT PRIMARY KEY,
  queue_id TEXT,
  case_id TEXT NOT NULL,
  property_type TEXT NOT NULL,
  blog_category TEXT NOT NULL,
  platform TEXT NOT NULL,
  title TEXT NOT NULL,
  body_html TEXT NOT NULL,
  body_markdown TEXT NOT NULL,
  meta_json TEXT NOT NULL DEFAULT '{}',
  processed_image_urls TEXT DEFAULT '[]',
  similarity_score REAL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'DRAFT',
  scheduled_for TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(case_id, platform, scheduled_for)
);

CREATE TABLE IF NOT EXISTS publish_logs (
  log_id TEXT PRIMARY KEY,
  post_id TEXT NOT NULL,
  platform TEXT NOT NULL,
  published_url TEXT,
  error_message TEXT,
  error_screenshot_path TEXT,
  retry_count INTEGER NOT NULL DEFAULT 0,
  published_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
""".trimIndent()

    fun generateWranglerToml(config: ServerAutomationConfig): String = """
name = "${config.cfWorkerScriptName.ifBlank { "ddakchal-blog-worker" }}"
main = "src/worker-cron.ts"
compatibility_date = "2026-11-01"

[triggers]
# 정각(:00) 회피 Anti-Bot 랜덤 분 오프셋 적용
crons = ["${config.cronExpression.ifBlank { "17 8 * * *" }}"]

[[d1_databases]]
binding = "DB"
database_name = "ddakchal-db"
database_id = "${config.cfD1DatabaseId.ifBlank { "<YOUR_D1_DATABASE_ID>" }}"

[[r2_buckets]]
binding = "DOCS_BUCKET"
bucket_name = "ddakchal-documents"

[vars]
DDAKCHAL_API_BASE = "${config.ddakchalApiBase}"
DAILY_POST_LIMIT = "${config.dailyPostLimit}"
MIN_DELAY_MINUTES = "${config.minDelayMinutes}"
MAX_DELAY_MINUTES = "${config.maxDelayMinutes}"
ENABLE_EXTERNAL_LINK_NAVER = "${config.enableExternalLinkNaver}"
""".trimIndent()

    fun generateWorkerScheduledTs(config: ServerAutomationConfig): String = """
export interface Env {
  DB: D1Database;
  DDAKCHAL_API_BASE: string;
  CRAWLER_SYNC_TOKEN: string;
  DAILY_POST_LIMIT: string;
  MIN_DELAY_MINUTES: string;
  MAX_DELAY_MINUTES: string;
  ENABLE_EXTERNAL_LINK_NAVER: string;
}

export default {
  async scheduled(controller: ScheduledController, env: Env, ctx: ExecutionContext): Promise<void> {
    const jitterMs = Math.floor(Math.random() * 45_000) + 5_000;
    await new Promise((r) => setTimeout(r, jitterMs));

    const limit = Math.min(Number(env.DAILY_POST_LIMIT || "2"), 5);
    const res = await fetch(`${'$'}{env.DDAKCHAL_API_BASE}/api/v1/blog/generate-daily-5`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${'$'}{env.CRAWLER_SYNC_TOKEN}`
      },
      body: JSON.stringify({
        limit,
        minDelayMinutes: Number(env.MIN_DELAY_MINUTES || "45"),
        maxDelayMinutes: Number(env.MAX_DELAY_MINUTES || "120"),
        enableExternalLinkNaver: env.ENABLE_EXTERNAL_LINK_NAVER === "true"
      })
    });

    if (!res.ok) {
      await env.DB.prepare(
        "INSERT INTO publish_logs (log_id, post_id, platform, error_message, retry_count) VALUES (?, ?, ?, ?, ?)"
      ).bind(crypto.randomUUID(), "CRON_DAILY_5", "CLOUDFLARE", `HTTP ${'$'}{res.status}`, 0).run();
      return;
    }

    await env.DB.prepare(
      "INSERT INTO publish_logs (log_id, post_id, platform, published_url, retry_count) VALUES (?, ?, ?, ?, ?)"
    ).bind(crypto.randomUUID(), "CRON_DAILY_5", "CLOUDFLARE", `${'$'}{env.DDAKCHAL_API_BASE}/blog`, 0).run();
  }
};
""".trimIndent()

    fun generateSelectorsJson(): String = """
{
  "naver_smarteditor_one": {
    "editor_frame": "iframe#mainFrame",
    "title_input": ".se-title-text .se-text-paragraph",
    "body_editable": ".se-main-container .se-text-paragraph",
    "image_upload_input": "input[type='file'][accept*='image']",
    "publish_open_btn": "button.publish_btn__m9KHH",
    "tag_input": "input.tag_input__r9R2y",
    "publish_confirm_btn": "button.confirm_btn__WEaBq"
  },
  "tistory_editor": {
    "title_input": "textarea#post-title-inp",
    "mode_html_btn": "button#editor-mode-layer-btn-open",
    "body_iframe": "iframe#editor-tistory_ifr",
    "image_upload_input": "input#attach-image-input",
    "publish_open_btn": "button#publish-layer-btn",
    "tag_input": "input#tagText",
    "publish_confirm_btn": "button#publish-btn"
  }
}
""".trimIndent()

    fun generateEnvBlogMasked(config: ServerAutomationConfig, masked: Boolean = true): String {
        fun sec(value: String, placeholder: String): String {
            if (value.isBlank()) return placeholder
            return if (masked) "****${value.takeLast(minOf(4, value.length))}" else value
        }
        return """
# .env.blog (SPEC_BLOG_AUTOMATION.md v13.4 표준)
NAVER_BLOG_ID=${config.naverBlogId.ifBlank { "your_naver_blog_id" }}
NAVER_ID=${config.naverId.ifBlank { "your_naver_id" }}
NAVER_PW=${sec(config.naverPw, "your_naver_pw")}
TISTORY_BLOG_URL=${config.tistoryBlogUrl.ifBlank { "https://your-blog.tistory.com" }}
TISTORY_ID=${config.tistoryId.ifBlank { "your_kakao_id" }}
TISTORY_PW=${sec(config.tistoryPw, "your_kakao_pw")}

# 딱찰 API & Cloudflare 연동
DDAKCHAL_API_BASE=${config.ddakchalApiBase}
CRAWLER_SYNC_TOKEN=${sec(config.crawlerSyncToken, "your_40char_sync_token")}
CF_ACCOUNT_ID=${config.cfAccountId.ifBlank { "your_cf_account_id" }}
CF_API_TOKEN=${sec(config.cfApiToken, "your_cf_api_token")}
CF_D1_DATABASE_ID=${config.cfD1DatabaseId.ifBlank { "your_d1_database_id" }}
R2_BUCKET_URL=${config.ddakchalApiBase}
DATABASE_URL=d1://ddakchal-db

# LLM API
LLM_API_KEY=${sec(config.llmApiKey, "your_llm_api_key")}

# 저품질 방지 제어 변수
DAILY_POST_LIMIT=${config.dailyPostLimit}
MIN_DELAY_MINUTES=${config.minDelayMinutes}
MAX_DELAY_MINUTES=${config.maxDelayMinutes}
ENABLE_EXTERNAL_LINK_NAVER=${config.enableExternalLinkNaver}
""".trimIndent()
    }
}
