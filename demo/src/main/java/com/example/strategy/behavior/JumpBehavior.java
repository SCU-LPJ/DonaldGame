package com.example.strategy.behavior;

import com.example.strategy.BehaviorStrategy;

public class JumpBehavior implements BehaviorStrategy {
    @Override
    public void perform() { System.out.println("===行为：蹦蹦跳跳"); }
}
