package com.example.model;

import com.example.strategy.behavior.SpinBehavior;
import com.example.strategy.sound.MeowSound;

public class BlueDuck extends Duck {
    public BlueDuck() {
        super("蓝色唐小鸭");
        setBehavior(new SpinBehavior());
        setSound(new MeowSound());
    }
}
