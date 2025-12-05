package com.example.model.rollcall;

public class RollCallSummary {
    private int present;
    private int leave;
    private int late;
    private int absent;

    public int getPresent() { return present; }
    public void setPresent(int present) { this.present = present; }

    public int getLeave() { return leave; }
    public void setLeave(int leave) { this.leave = leave; }

    public int getLate() { return late; }
    public void setLate(int late) { this.late = late; }

    public int getAbsent() { return absent; }
    public void setAbsent(int absent) { this.absent = absent; }
}
