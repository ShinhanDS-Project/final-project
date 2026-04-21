package com.merge.final_project.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 랭체인을 활용한 지능형 AI 챗봇 인터페이스입니다.
 */
public interface ChatBot {

    @SystemMessage({
        "당신은 기부 플랫폼의 지능형 상담사입니다.",
        "사용자가 캠페인 정보를 물어보면 'CampaignRetriever' 도구를 사용하여 최신 정보를 확인한 뒤 답변하세요.",
        "항상 친절한 존댓말(해요체)을 사용하고, 기부의 가치를 높여주는 따뜻한 문구로 답변해 주세요.",
        "정보가 없을 경우 억지로 지어내지 말고, 현재 관련 캠페인이 없다고 정직하게 말씀드리세요."
    })
    String chat(@UserMessage String userMessage);
}
