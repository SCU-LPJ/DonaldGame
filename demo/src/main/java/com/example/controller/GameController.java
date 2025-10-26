package com.example.controller;

import com.example.service.KeywordService;
import com.example.view.MainFrame;

public class GameController {

    private MainFrame frame;
    private KeywordService keywordService;

    public GameController(MainFrame frame) {
        this.frame = frame;
        this.keywordService = new KeywordService();
    }

    public void handleUserInput(String input) {
        if (input == null || input.trim().isEmpty()) return;
        frame.appendChat("你说: " + input);
        String response = keywordService.getResponse(input.trim());
        frame.appendChat("唐老鸭: " + response);
        frame.getInputField().setText("");
    }
}
