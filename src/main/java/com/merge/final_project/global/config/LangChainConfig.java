package com.merge.final_project.global.config;

import com.merge.final_project.ai.service.CampaignRetriever;
import com.merge.final_project.ai.service.ChatBot;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Value("${openapi.service.key}")
    private String apiKey;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .strictJsonSchema(true)
                .build();
    }

    @Bean
    public ChatBot chatBot(OpenAiChatModel openAiChatModel, CampaignRetriever campaignRetriever) {
        return AiServices.builder(ChatBot.class)
                .chatLanguageModel(openAiChatModel)
                .tools(campaignRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}
