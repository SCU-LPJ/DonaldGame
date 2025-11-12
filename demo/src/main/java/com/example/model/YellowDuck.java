package com.example.model;

import com.example.strategy.behavior.FlyBehavior;
import com.example.strategy.sound.QuackSound;

public class YellowDuck extends Duck {
    public YellowDuck() {
        super("黄色唐小鸭");
        setBehavior(new FlyBehavior());
        setSound(new QuackSound());
    }
}
