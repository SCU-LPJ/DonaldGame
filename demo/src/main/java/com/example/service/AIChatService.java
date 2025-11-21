package com.example.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIChatService {

    private static final Logger log = LoggerFactory.getLogger(AIChatService.class);
    private final OpenAIClient client;

    public AIChatService() {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("环境变量 DASHSCOPE_API_KEY 未设置");
        }

        client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
        log.info("AIChatService 初始化完成，使用 dashscope 兼容模式");
    }

    public String askAI(String userInput) {
        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .addUserMessage(userInput)
                    .model("qwen-plus")  // qwen-plus / qwen-turbo / qwen-max
                    .build();

            ChatCompletion chat = client.chat().completions().create(params);

            if (chat.choices() != null && !chat.choices().isEmpty()) {
                return chat.choices().get(0).message().content().orElse("（AI没有返回文本内容）");

            } else {
                log.warn("AI 返回为空响应");
                return "AI 没有返回任何内容。";
            }

        } catch (Exception e) {
            log.error("请求 AI 失败", e);
            return "请求AI时出现错误：" + e.getMessage();
        }
    }
}
