package com.example.model;

import com.example.strategy.behavior.FlyBehavior;
import com.example.strategy.sound.AngrySound;

public class DonaldDuck extends Duck {
    public DonaldDuck() {
        super("唐老鸭");
        setBehavior(new FlyBehavior());
        setSound(new AngrySound());
    }
}
