package com.example.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class AIChatService {

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
                return "AI 没有返回任何内容。";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "请求AI时出现错误：" + e.getMessage();
        }
    }
}
