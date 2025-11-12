package com.example.model;

import com.example.strategy.BehaviorStrategy;
import com.example.strategy.SoundStrategy;

public abstract class Duck {
    protected BehaviorStrategy behavior;
    protected SoundStrategy sound;
    protected final String name;

    public Duck(String name) { this.name = name; }

    public String getName() { return name; }

    public void setBehavior(BehaviorStrategy b) { this.behavior = b; }
    public void setSound(SoundStrategy s) { this.sound = s; }

    /** 执行动作（控制台打印），聊天区文本由上层控制器追加 */
    public void performBehavior() { if (behavior != null) behavior.perform(); }
    /** 发出叫声 + TTS */
    public void makeSound() { if (sound != null) sound.makeSound(); }

    /** 常用组合：先动作再叫 */
    public void act() {
        performBehavior();
        makeSound();
    }
}
