package com.example.strategy.sound;

import com.example.strategy.SoundStrategy;
import com.example.util.TTSUtil;

public class MeowSound implements SoundStrategy {
    @Override
    public void makeSound() {
        String txt = "其实我是唐人";
        System.out.println("===叫声：" + txt);
        TTSUtil.speak(txt);
    }
}
