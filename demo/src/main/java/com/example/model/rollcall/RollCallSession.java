package com.example.model.rollcall;

import java.time.LocalDateTime;

public class RollCallSession {
    private long id;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private RollCallMode mode;
    private RollCallStrategy strategy;
    private Integer count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public RollCallMode getMode() {
        return mode;
    }

    public void setMode(RollCallMode mode) {
        this.mode = mode;
    }

    public RollCallStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(RollCallStrategy strategy) {
        this.strategy = strategy;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
