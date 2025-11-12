package com.example.strategy;

public interface BehaviorStrategy {
    /** 执行小鸭的“动作”表现（文本反馈由调用方决定如何显示） */
    void perform();
}
