package service;

import java.util.HashMap;
import java.util.Map;

public class KeywordService {

    private Map<String, String> responses;

    public KeywordService() {
        responses = new HashMap<>();
        responses.put("发红包", "听起来不错，准备叫上红色唐小鸭吧！");
        responses.put("统计代码量", "让我看看……tokei 正在准备统计中！");
        responses.put("ai对话", "马上调用AI对话模块！");
    }

    public String getResponse(String input) {
        for (String key : responses.keySet()) {
            if (input.contains(key)) {
                return responses.get(key);
            }
        }
        return "嗯？唐老鸭没听懂你说什么~";
    }
}
