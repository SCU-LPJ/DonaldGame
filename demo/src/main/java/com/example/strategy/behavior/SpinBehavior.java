package com.example.strategy.behavior;

import com.example.strategy.BehaviorStrategy;

public class SpinBehavior implements BehaviorStrategy {
    @Override
    public void perform() { System.out.println("===行为：转圈圈"); }
}
