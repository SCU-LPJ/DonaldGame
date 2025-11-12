package com.example.strategy.sound;

import com.example.strategy.SoundStrategy;
import com.example.util.TTSUtil;

public class AngrySound implements SoundStrategy {
    @Override
    public void makeSound() {
        String txt = "呃啊我怒了我可不和你玩找唐小鸭去";
        System.out.println("===叫声：" + txt);
        TTSUtil.speak(txt);
    }
}
