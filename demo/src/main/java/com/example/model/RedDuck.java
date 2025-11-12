package com.example.model;

import com.example.strategy.behavior.JumpBehavior;
import com.example.strategy.sound.BarkSound;

public class RedDuck extends Duck {
    public RedDuck() {
        super("红色唐小鸭");
        setBehavior(new JumpBehavior());
        setSound(new BarkSound());
    }
}
