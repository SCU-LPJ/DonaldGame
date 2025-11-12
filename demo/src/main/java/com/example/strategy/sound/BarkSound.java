package com.example.strategy.sound;

import com.example.strategy.SoundStrategy;
import com.example.util.TTSUtil;

public class BarkSound implements SoundStrategy {
    @Override
    public void makeSound() {
        String txt = "我是大帅比侯睿";
        System.out.println("===叫声：" + txt);
        TTSUtil.speak(txt);
    }
}
