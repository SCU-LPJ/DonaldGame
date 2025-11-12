package com.example.strategy.behavior;

import com.example.strategy.BehaviorStrategy;

public class FlyBehavior implements BehaviorStrategy {
    @Override
    public void perform() { System.out.println("===行为：飞起来了"); }
}
