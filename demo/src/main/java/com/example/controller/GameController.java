package com.example.controller;

import com.example.model.*;
import com.example.service.KeywordService;
import com.example.service.AIChatService;
import com.example.view.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final MainFrame frame;
    private final KeywordService keywordService;
    private final AIChatService aiService;

    // 新增：鸭子注册表（名称 -> Duck 实例）
    private final Map<String, Duck> ducks = new HashMap<>();

    public GameController(MainFrame frame) {
        this.frame = frame;
        this.keywordService = new KeywordService();
        this.aiService = new AIChatService();
        initDucks();
    }

    private void initDucks() {
        ducks.put("唐老鸭", new DonaldDuck());
        ducks.put("红色唐小鸭", new RedDuck());
        ducks.put("蓝色唐小鸭", new BlueDuck());
        ducks.put("黄色唐小鸭", new YellowDuck());
        log.info("初始化鸭子注册表：{}", ducks.keySet());
    }

    /** 提供给 GamePanel 点击触发 */
    public void triggerDuckByName(String name) {
        Duck d = ducks.get(name);
        if (d == null) return;

        log.info("触发表演：{}", name);
        frame.appendChat(name + "：开始表演！\n");
        d.act(); // 控制台会打印动作和叫声，叫声会通过TTS播放
        frame.appendChat(renderActText(d));
        frame.appendChat(name + "：表演完成！\n");
    }

    /** 根据默认策略渲染简洁文本（写入聊天区） */
    private String renderActText(Duck d) {
        // 简单的文本反馈；如果后续需要细粒度映射，可引入事件总线或回调
        if (d instanceof RedDuck) return "===行为：蹦蹦跳跳 ｜ ===叫声：难道他真的是赋能哥？";
        if (d instanceof BlueDuck) return "===行为：转圈圈 ｜ ===叫声：嘿刀马刀马嘿刀马刀马";
        if (d instanceof YellowDuck) return "===行为：飞起来了 ｜ ===叫声：奶妈我可以和你玩吗颗秒邦邦邦邦";
        return "===行为：双手抱拳很生气 ｜ ===叫声：呃啊我怒了我可不和你玩找唐小鸭去";
    }

    /** 聊天输入入口（原有方法基础上增强“鸭子指令”识别） */
    public void handleUserInput(String input) {
        if (input == null || input.trim().isEmpty()) return;

        log.debug("收到用户输入：{}", input.trim());
        frame.appendChat("你说：" + input.trim());

        // 1) 先识别鸭子：输入包含鸭子名称，直接触发
        for (String name : ducks.keySet()) {
            if (input.contains(name)) {
                triggerDuckByName(name);
                return;
            }
        }

        // 2) 然后走关键字回复
        String response = keywordService.getResponse(input.trim());

        // 关键字没命中或是“不懂”兜底 -> 走 AI
        if (response == null || response.isEmpty()
                || response.contains("听不懂")
                || response.contains("我听不懂哦~")) {

            frame.appendChat("唐老鸭：让我想想... 🦆");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return aiService.askAI(input);
                }

                @Override
                protected void done() {
                    try {
                        String ai = get();
                        frame.appendChat("唐老鸭：" + (ai == null ? "咱们换种说法试试？" : ai));
                    } catch (Exception e) {
                        log.error("AI 接口调用失败", e);
                        frame.appendChat("【错误】AI接口调用失败。");
                    }
                }
            }.execute();
        } else {
            frame.appendChat("唐老鸭：" + response + "\n");
        }
    }
}
