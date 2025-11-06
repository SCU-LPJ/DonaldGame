package com.example.controller;

import com.example.service.KeywordService;
import com.example.service.AIChatService;
import com.example.view.MainFrame;

import javax.swing.*;

public class GameController {

    private MainFrame frame;
    private KeywordService keywordService;
    private AIChatService aiService;

    public GameController(MainFrame frame) {
        this.frame = frame;
        this.keywordService = new KeywordService();
        this.aiService = new AIChatService();
    }

    public void handleUserInput(String input) {
        if (input == null || input.trim().isEmpty()) return;

        frame.appendChat("你说： " + input);
        frame.getInputField().setText("");

        // 优先尝试关键字匹配
        String response = keywordService.getResponse(input.trim());

        if (response == null || response.isEmpty() || response.equals("我听不懂哦~")) {
            // 当关键字匹配不到时，调用 AI
            frame.appendChat("唐老鸭：让我想想... 🦆");

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return aiService.askAI(input);
                }

                @Override
                protected void done() {
                    try {
                        String aiReply = get();
                        frame.appendChat("唐老鸭AI： " + aiReply);
                    } catch (Exception e) {
                        frame.appendChat("【错误】AI接口调用失败。");
                    }
                }
            }.execute();
        } else {
            // 本地关键字匹配成功
            frame.appendChat("唐老鸭： " + response);
        }
    }
}
