package com.example.strategy.sound;

import com.example.strategy.SoundStrategy;
import com.example.util.TTSUtil;

public class MeowSound implements SoundStrategy {
    @Override
    public void makeSound() {
        String txt = "嘿刀马刀马嘿刀马刀马";
        System.out.println("===叫声：" + txt);
        TTSUtil.speak(txt);
    }
}
