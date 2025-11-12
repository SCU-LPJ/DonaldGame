package com.example.strategy.sound;

import com.example.strategy.SoundStrategy;
import com.example.util.TTSUtil;

public class QuackSound implements SoundStrategy {
    @Override
    public void makeSound() {
        String txt = "奶妈我可以和你玩吗邦邦邦邦";
        System.out.println("===叫声：" + txt);
        TTSUtil.speak(txt);
    }
}
