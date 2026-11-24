package com.example.engine

import com.example.model.AuctionCaseIntel
import com.example.model.LegalStudyCategory
import com.example.model.LegalStudyTerm
import com.example.model.PropertyType

/**
 * Structured Korean Real Estate LegalTech Study Engine covering Civil Law,
 * Special Civil Statutes, Auction Rights & Dividend Priority, Public Registry,
 * Urban Redevelopment, and Real Estate Tax law.
 */
object LegalTermStudyEngine {

    val masterTerms: List<LegalStudyTerm> = listOf(
        LegalStudyTerm(
            codeId = "DB-CIV-001",
            category = LegalStudyCategory.CIVIL_GENERAL,
            termTitle = "비진의표시(제107조) · 통정허위표시(제108조) · 도달주의(제111조)",
            keywords = listOf("비진의표시", "통정허위표시", "선의의 제3자", "도달주의"),
            statuteRef = "민법 제107조, 제108조, 제111조, 제146조",
            coreDoctrine = "비진의표시는 원칙적으로 유효하나 상대방이 알았거나 알 수 있었을 때 무효가 됩니다. 통정허위표시(가장 매매·가장 임차권)는 당사자 사이에서 언제나 무효이지만 선의의 제3자에게는 대항하지 못합니다. 취소권은 추인할 수 있는 날로부터 3년, 법률행위일로부터 10년 내 행사해야 합니다.",
            practicalTrap = "경매 배당에서 가장 임차인 여부를 가릴 때 실제 보증금 이체 내역과 통정허위표시 정황을 대조하지 않으면 배당이의 소송 대상이 됩니다.",
            userQuestionExample = "가짜로 작성한 매매·임대차계약서의 법적 효력은?",
            blogStudyOneLiner = "[법리 용어 공부] 통정허위표시(민법 제108조)로 만든 가장 임대차계약은 당사자 간 항상 무효이므로 실제 금융거래 이체 내역이 없으면 대항력 있는 임차인으로 인정받기 어렵습니다.",
            searchTags = listOf("#민법총칙", "#통정허위표시", "#비진의표시", "#도달주의"),
            apiJsonSnippet = """{"code_id":"DB-CIV-001","category":"CIVIL_GENERAL","statute":"민법 제107조·제108조","flag":"is_collusive_false_intent"}"""
        ),
        LegalStudyTerm(
            codeId = "DB-SPC-001",
            category = LegalStudyCategory.CIVIL_SPECIAL,
            termTitle = "주임법 대항력 특례 · 세대합가 · 대위변제 승격 · 법인 임차인",
            keywords = listOf("대항력 익일 0시", "세대합가", "대위변제", "LH/중소기업 법인", "무상임차확인서"),
            statuteRef = "주택임대차보호법 제3조, 대법원 세대합가·대위변제 판례",
            coreDoctrine = "주택 인도와 전입신고 다음 날 0시에 대항력이 생깁니다. 동거 가족 전입이 앞서는 세대합가는 최초 가족 전입일로 소급되며, 전소유자가 매도 후 임차인으로 전환한 경우 매수인 등기 다음 날 0시 기준입니다. 후순위 임차인이 선순위 근저당을 대위변제해 말소시키면 최선순위로 승격됩니다.",
            practicalTrap = "무상거주확인서를 제출한 임차인은 경매에서 신의칙상 대항력을 주장할 수 없으며, LH·SH·중소기업 법인이 아닌 일반 법인은 주임법 대항력이 배제됩니다.",
            userQuestionExample = "집을 매도하고 전세로 남거나 후순위가 선순위 근저당을 대신 갚으면 어떻게 되나요?",
            blogStudyOneLiner = "[법리 용어 공부] 주임법 대위변제 승격(risk_upgraded)은 후순위 임차인이 앞선 근저당을 대신 갚고 말소시켜 대항력이 최선순위로 올라서는 구조이므로 말소예정 근저당 소액 잔액 사건은 입찰 전 대위변제 여부를 꼭 확인해야 합니다.",
            searchTags = listOf("#주택임대차보호법", "#대항력", "#세대합가", "#대위변제"),
            apiJsonSnippet = """{"code_id":"DB-SPC-001","category":"CIVIL_SPECIAL","opposability":"max(move_in,reg)+1day","flags":["is_family_cohabitation","is_subrogated_payment"]}"""
        ),
        LegalStudyTerm(
            codeId = "DB-SPC-002",
            category = LegalStudyCategory.CIVIL_SPECIAL,
            termTitle = "상임법 환산보증금 · 가등기담보 · 계약명의신탁 · 집합건물 대지사용권",
            keywords = listOf("환산보증금", "담보가등기", "계약명의신탁", "대지사용권 일체성"),
            statuteRef = "상임법 제2·3조, 가등기담보법 제3·13조, 부실법 제4조, 집합건물법 제20조",
            coreDoctrine = "상가는 건물 인도와 사업자등록 신청 익일 0시 대항력이 생기며 환산보증금(보증금+월세x100)으로 보호 구간을 가릅니다. 담보가등기는 경매 시 저당권으로 보아 말소기준이 되어 소멸하지만, 보전가등기가 선순위면 매수인이 인수합니다. 구분건물과 대지사용권은 분리 처분이 금지됩니다.",
            practicalTrap = "매각물건명세서에 '대지권 미등기'로 기재된 집합건물은 분양대금 완납 여부와 대지권 비율이 감정가에 포함됐는지 감정평가서를 반드시 확인해야 합니다.",
            userQuestionExample = "상가 임차인의 대항력과 대지권 미등기 아파트 경매의 위험은 무엇인가요?",
            blogStudyOneLiner = "[법리 용어 공부] 집합건물법 제20조 대지사용권 일체성 원칙상 구분건물 낙찰자는 전유부분과 함께 대지사용권을 취득하지만, 대지권 미등기 물건은 토지 지분 감정 포함 여부를 별도로 대조해야 안전합니다.",
            searchTags = listOf("#상가임대차", "#가등기담보", "#부실법", "#집합건물법"),
            apiJsonSnippet = """{"code_id":"DB-SPC-002","category":"CIVIL_SPECIAL","statutes":["SPL-02","SPL-03","SPL-04","SPL-05"]}"""
        ),
        LegalStudyTerm(
            codeId = "DB-AUC-001",
            category = LegalStudyCategory.AUCTION_DIVIDEND,
            termTitle = "말소기준권리 6종 판별 & 최선순위 전세권 승격 조건",
            keywords = listOf("말소기준권리", "전세권 승격", "선순위 가처분", "인수 vs 소멸"),
            statuteRef = "민사집행법 제91조(인수주의·소멸주의)",
            coreDoctrine = "근저당·저당·압류·가압류·담보가등기·경매개시결정기입등기 중 가장 앞선 권리가 말소기준권리가 됩니다. 최선순위 전세권은 건물 전부에 설정되고 전세권자가 직접 경매를 신청하거나 배당요구종기 내 배당요구를 한 경우에만 말소기준권리로 승격해 소멸하며, 그렇지 않으면 매수인이 인수합니다.",
            practicalTrap = "선순위 가처분(처분금지가처분)이 토지 건물 지분에 걸려 있으면 말소되지 않고 매수인이 인수해 향후 소유권을 잃을 수 있습니다.",
            userQuestionExample = "선순위 전세권은 낙찰받으면 소멸하나요, 인수하나요?",
            blogStudyOneLiner = "[법리 용어 공부] 최선순위 전세권은 건물 전부에 설정되고 경매신청 또는 배당요구한 경우에만 소멸(말소기준권리 승격)하며, 배당요구가 없으면 낙찰자가 전세금을 그대로 인수합니다.",
            searchTags = listOf("#말소기준권리", "#전세권", "#민사집행법91조", "#인수소멸"),
            apiJsonSnippet = """{"code_id":"DB-AUC-001","category":"AUCTION_DIVIDEND","jeonsegwon_malso":"applicant||demand||whole_building"}"""
        ),
        LegalStudyTerm(
            codeId = "DB-AUC-002",
            category = LegalStudyCategory.AUCTION_DIVIDEND,
            termTitle = "실무 6순위 배당표 & 개정 국세기본법 당해세 배제(확정일자 > 국세)",
            keywords = listOf("6순위 배당", "최우선변제 1/2", "당해세 배제", "안분배당"),
            statuteRef = "국세기본법 제35조 개정 조항, 주임법 제8조",
            coreDoctrine = "경매 배당은 ①경매실행비용→②소액임차인·최종 3개월 임금·3년 퇴직금(낙찰가 1/2 한도, 담보물권 설정일 기준)→③당해세(국세·지방세)→④담보물권·확정일자 임차인(시간순)→⑤공과금→⑥일반채권(안분배당) 순서입니다. 개정 국세기본법 제35조로 임차인의 확정일자가 국세 당해세 법정기일보다 빠르면 국세 당해세보다 임차인이 우선 배당받습니다(지방세 당해세는 예외).",
            practicalTrap = "지방세 당해세(재산세 등)는 기존 우선원칙이 유지되므로 주택·상가 체납 지방세 고지 여부를 배당 계산표에서 분리해 반영해야 합니다.",
            userQuestionExample = "집주인의 세금 체납(당해세) 때문에 세입자가 보증금을 떼이게 되나요?",
            blogStudyOneLiner = "[법리 용어 공부] 개정 국세기본법 제35조에 따라 임차인의 확정일자가 국세 당해세 법정기일보다 빠르면 국세 당해세가 후순위로 밀려 임차보증금이 보호되지만, 지방세 당해세는 우선원칙이 유지됩니다.",
            searchTags = listOf("#배당순위", "#당해세배제", "#확정일자", "#최우선변제금"),
            apiJsonSnippet = """{"code_id":"DB-AUC-002","category":"AUCTION_DIVIDEND","rule":"fixed_date > national_target_tax"}"""
        ),
        LegalStudyTerm(
            codeId = "DB-REG-001",
            category = LegalStudyCategory.PUBLIC_REGISTRY,
            termTitle = "28개 법정 지목 · 토지이동 · 건물 멸실등기 및 등기부 폐쇄",
            keywords = listOf("28개 지목", "지목변경", "멸실등기", "등기부 폐쇄"),
            statuteRef = "공간정보의 구축 및 관리 등에 관한 법률 제67조, 부동산등기법 제101조",
            coreDoctrine = "토지는 전·답·과수원·임야·대·공장용지·도로·잡종지 등 28개 지목과 7종 지적공부로 관리됩니다. 건물이 철거·소실되어 멸실등기가 수리되면 건물등기부는 폐쇄되며, 폐쇄된 등기부에는 경매개시결정, 가압류 등 처분제한이나 소유권이전등기 촉탁이 법적으로 불가능합니다.",
            practicalTrap = "공부상 지목이 '전·답'이라도 현황이 임야화되었거나 도로가 없는 맹지라면 지자체 농지취득자격증명 발급 또는 반려 확인서 확보 가능 여부가 보증금 몰수를 가릅니다.",
            userQuestionExample = "철거된 건물의 등기부에 경매나 가압류를 걸 수 있나요?",
            blogStudyOneLiner = "[법리 용어 공부] 부동산등기법 제101조상 건물이 철거되어 멸실등기로 등기부가 폐쇄되면 이후 압류·경매기입·소유권이전등기 촉탁이 법적으로 불가능해지므로 현황과 등기부 폐쇄 여부를 함께 확인해야 합니다.",
            searchTags = listOf("#공간정보법", "#28개지목", "#멸실등기", "#등기부폐쇄"),
            apiJsonSnippet = """{"code_id":"DB-REG-001","category":"PUBLIC_REGISTRY","field":"is_building_register_closed","effect":"REJECTION_BY_LAW"}"""
        ),
        LegalStudyTerm(
            codeId = "DB-URB-001",
            category = LegalStudyCategory.URBAN_REDEVELOPMENT,
            termTitle = "도시정비법 환권 · '토지만의 경매' 조합원지위 분리 딜레마 (2024-18)",
            keywords = listOf("관리처분인가", "이전고시", "토지만의 경매", "조합원입주권"),
            statuteRef = "도시 및 주거환경정비법 제39·74·86조, 사법정책연구원 연구보고서 2024-18",
            coreDoctrine = "정비사업은 조합설립→사업시행인가→관리처분인가(입주권 화체)→철거·멸실→이전고시(종후 신축으로 소급 환권) 단계를 거칩니다. 관리처분인가 후 종전 건물이 철거돼 등기부가 폐쇄된 상태에서 토지만 경매가 진행되면, 낙찰자는 토지 소유권만 취득하고 기존 조합원의 입주권 지위와 분리되는 고위험 분쟁이 생길 수 있습니다.",
            practicalTrap = "투기과열지구 재건축·관리처분인가 이후 재개발 등 조합원 지위 양도 제한 구역의 경매는 현금청산 대상 여부를 조합 정관·구청으로 반드시 확인해야 합니다.",
            userQuestionExample = "재개발 구역에서 건물이 철거된 뒤 토지만 경매로 낙찰받으면 입주권이 나오나요?",
            blogStudyOneLiner = "[법리 용어 공부] 정비사업 구역 내 건물 멸실 후 진행되는 '토지만의 경매'는 토지 소유권과 조합원 입주권이 분리될 위험(도시정비법 제39조·사법정책연구원 2024-18)이 있으므로 조합원 지위 승계 가능 여부를 반드시 사전 조회해야 합니다.",
            searchTags = listOf("#도시정비법", "#토지만의경매", "#조합원입주권", "#환권"),
            apiJsonSnippet = """{"code_id":"DB-URB-001","category":"URBAN_REDEVELOPMENT","is_land_only_auction":true,"reform":"사법정책연구원 2024-18"}"""
        ),
        LegalStudyTerm(
            codeId = "DB-TAX-001",
            category = LegalStudyCategory.REAL_ESTATE_TAX,
            termTitle = "1세대 1주택 비과세 귀속(계약일 기준) · 재산세 토지3구분 · 양도세 산식",
            keywords = listOf("계약일 기준", "재산세 종합합산", "양도소득세", "취득세 중과"),
            statuteRef = "소득세법 제89·95·104조, 지방세법 제106조, 조세심판원 심판례",
            coreDoctrine = "조세심판원 결정으로 1세대 1주택 비과세 판단 귀속시점은 잔금일이 아닌 계약일 기준으로 회귀했습니다. 토지 재산세는 나대지·비사업용 토지(종합합산 고율), 상가·사무실 부속토지(별도합산), 농지·공장용지(분리과세)로 나뉩니다. 양도소득세는 양도차익에서 장기보유특별공제와 기본공제 250만원을 뺀 과세표준에 세율(6~45%)을 적용합니다.",
            practicalTrap = "경매로 나대지나 비사업용 농지를 취득해 보유하면 재산세 종합합산 누진세와 양도세 비사업용 중과(+10%p) 대상이 될 수 있습니다.",
            userQuestionExample = "1세대 1주택 비과세 판단은 계약일 기준인가요, 잔금일 기준인가요?",
            blogStudyOneLiner = "[법리 용어 공부] 조세심판원 결정에 따라 1세대 1주택 비과세 판단 시점은 잔금일이 아닌 계약일 기준을 따르며, 나대지·비사업용 토지는 지방세법 제106조상 종합합산 고율 재산세 대상에 해당합니다.",
            searchTags = listOf("#부동산세법", "#1세대1주택", "#계약일기준", "#종합합산"),
            apiJsonSnippet = """{"code_id":"DB-TAX-001","category":"REAL_ESTATE_TAX","is_contract_date_attribution":true,"land_tax":["COMPREHENSIVE","SEPARATE_AGG","SEPARATE_FLAT"]}"""
        )
    )

    fun selectContextualTerm(auction: AuctionCaseIntel): LegalStudyTerm {
        val text = "${auction.address} ${auction.possessionSummary} ${auction.assumedRightsText} ${auction.specialCheckNotice}"
        return when {
            text.contains("멸실") || text.contains("재개발") || text.contains("재건축") || text.contains("정비") ->
                masterTerms.first { it.codeId == "DB-URB-001" }
            text.contains("대위변제") || text.contains("세대합가") || text.contains("선순위") ->
                masterTerms.first { it.codeId == "DB-SPC-001" }
            text.contains("전세권") || text.contains("가처분") ->
                masterTerms.first { it.codeId == "DB-AUC-001" }
            auction.propertyType == PropertyType.LAND_FARMLAND ->
                if (text.contains("지목") || text.contains("맹지") || text.contains("농취증")) {
                    masterTerms.first { it.codeId == "DB-REG-001" }
                } else {
                    masterTerms.first { it.codeId == "DB-TAX-001" }
                }
            auction.propertyType == PropertyType.COMMERCIAL_RETAIL ->
                masterTerms.first { it.codeId == "DB-SPC-002" }
            auction.propertyType == PropertyType.FACTORY_SPECIAL ->
                masterTerms.first { it.codeId == "DB-AUC-002" }
            else -> masterTerms.first { it.codeId == "DB-AUC-002" }
        }
    }

    fun buildInlineStudyNote(auction: AuctionCaseIntel): String =
        selectContextualTerm(auction).blogStudyOneLiner

    fun filterTerms(category: LegalStudyCategory? = null, query: String = ""): List<LegalStudyTerm> {
        val q = query.trim().lowercase()
        return masterTerms.filter { term ->
            val catOk = category == null || term.category == category
            val qOk = q.isBlank() ||
                term.termTitle.lowercase().contains(q) ||
                term.coreDoctrine.lowercase().contains(q) ||
                term.keywords.any { it.lowercase().contains(q) } ||
                term.statuteRef.lowercase().contains(q)
            catOk && qOk
        }
    }
}
