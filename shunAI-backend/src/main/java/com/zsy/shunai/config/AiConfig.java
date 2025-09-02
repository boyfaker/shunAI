package com.zsy.shunai.config;


import ai.z.openapi.ZhipuAiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    /**
     * apikey 需要从平台获取
     */
    private String apiKey;

    /**
     * @return ai客户端
     */
    @Bean
    public ZhipuAiClient getClient() {
        return ZhipuAiClient.builder()
                .apiKey(apiKey)
                .build();
    }


}
