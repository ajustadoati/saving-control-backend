package com.ajustadoati.sc.config;

import com.ajustadoati.sc.ai.AsistenteFinanciero;
import com.ajustadoati.sc.ai.ConsultaPagoTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfiguration {

    @Bean
    ChatLanguageModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.1")
                .logRequests(true)
                .logResponses(true)
                .temperature(0.1)
                .build();
    }

    @Bean
    public AsistenteFinanciero asistenteFinanciero(
            ChatLanguageModel chatModel,
            ConsultaPagoTool consultaPagoTool
    ) {

        return AiServices.builder(AsistenteFinanciero.class)
                .chatLanguageModel(chatModel)
                .tools(consultaPagoTool)
                .build();
    }
}
