package com.example.model.rollcall;

import java.time.LocalDateTime;

// 对应数据库表的一行记录 将sql查询结果映射成java对象实体类 以便于service用他处理业务逻辑 ui层用他显示数据
// 作为 DAO 和 Service 之间传递的数据结构

public class LeaveRequest {
    private long id;
    private long studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private String status;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
