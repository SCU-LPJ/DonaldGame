package com.example.strategy.sound;

import com.example.strategy.SoundStrategy;
import com.example.util.TTSUtil;

public class BarkSound implements SoundStrategy {
    @Override
    public void makeSound() {
        String txt = "难道他真的是赋能哥？";
        System.out.println("===叫声：" + txt);
        TTSUtil.speak(txt);
    }
}
