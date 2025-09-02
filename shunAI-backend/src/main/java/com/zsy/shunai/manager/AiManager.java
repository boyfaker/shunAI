package com.zsy.shunai.manager;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通用 AI 调用能力
 */
@Component
public class AiManager {

    @Resource
    private ZhipuAiClient zhipuAiClient;

    // 稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;


    /**
     * 通用请求
     *
     * @param messages
     * @param stream
     * @param temperature
     * @return String
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature) {
        // 构建请求
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model("glm-4.5")
                .stream(stream)
                .temperature(temperature)
                .messages(messages)
                .build();

        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);
        return response.getData().getChoices().get(0).getMessage().toString();
    }

    /**
     * 通用流式请求
     *
     * @param messages 信息
     * @return Flowable<ModelData> 流式响应对象
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature) {
        // 构建请求
        ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                .model("glm-4.5")
                .stream(true)
                .temperature(temperature)
                .messages(messages)
                .build();

        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(request);

        return response.getFlowable();
    }

    /**
     * 流式稳定请求 简化消息传递
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return Flowable<ModelData> 流式响应对象
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage,String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(),userMessage);
        messages.add(systemChatMessage);
        messages.add(userChatMessage);
        return doStreamRequest(messages, STABLE_TEMPERATURE);
    }

    /**
     * 流式稳定请求
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return Flowable<ModelData> 流式响应对象
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage,String userMessage,Float temperature) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(),userMessage);
        messages.add(systemChatMessage);
        messages.add(userChatMessage);
        return doStreamRequest(messages, temperature);
    }




    /**
     * 默认请求 系统 + 用户
     *
     * @param systemMessage 系统信息
     * @param userMessage 用户信息
     * @return
     */
    public String doRequest(String systemMessage,String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),systemMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(),userMessage);
        messages.add(systemChatMessage);
        messages.add(userChatMessage);
        return doRequest(messages, false, STABLE_TEMPERATURE);
    }


    /**
     * 默认请求 稳定温度 非流式
     *
     * @param messages
     * @return
     */
    public String doRequest(List<ChatMessage> messages) {
        return doRequest(messages, false, STABLE_TEMPERATURE);
    }

    /**
     * 默认非流式 可指定温度
     *
     * @param messages
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Float temperature) {
        return doRequest(messages, false, temperature);
    }

    /**
     * 默认稳定温度 可指定是否流式
     *
     * @param messages
     * @param stream
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream) {
        return doRequest(messages, stream, STABLE_TEMPERATURE);
    }

    /**
     * 快问快答
     *
     * @param question
     * @return
     */
    public String ask(String question) {
        List<ChatMessage> chatMessages = Arrays.asList(
                ChatMessage.builder()
                        .role(ChatMessageRole.USER.value())
                        .content(question)
                        .build()
        );
        return doRequest(chatMessages, false, STABLE_TEMPERATURE);
    }


}
