package com.example.service;

import com.example.model.rollcall.RollCallMode;
import com.example.model.rollcall.RollCallStrategy;
import com.example.model.rollcall.RollCallStatus;
import com.example.model.rollcall.StudentProfile;
import com.example.repository.RollCallRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.util.StudentCsvLoader;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thin service facade for roll-call domain so UI/controller
 * does not touch JDBC directly.
 */
// ui层永远调用该service服务层 调用 DAO方法 执行业务逻辑
public class RollCallService {

    private static final Logger log = LoggerFactory.getLogger(RollCallService.class);
    private final RollCallRepository repository;

    public RollCallService() {
        this(new RollCallRepository());
    }

    public RollCallService(RollCallRepository repository) {
        this.repository = repository;
    }

    /** Safe to call on startup; wraps SQL exceptions into runtime to simplify UI code. */
    // 初始化数据库表结构
    public void ensureSchema() {
        try {
            log.info("开始执行点名相关表的建表检查");
            repository.initSchema();
            log.info("点名相关表建表检查完成");
        } catch (SQLException e) {
            log.error("初始化数据库表失败", e);
            throw new IllegalStateException("初始化数据库表失败", e);
        }
    }

    // 点名系统核心逻辑
    public List<StudentProfile> selectCandidates(RollCallMode mode, RollCallStrategy strategy, int count) {
        if (mode == RollCallMode.ALL) {
            return listAllStudents();
        }
        int limit = Math.max(1, count);
        try {
            return repository.pickStudents(strategy, limit);
        } catch (SQLException e) {
            log.error("抽取学生失败 mode={} strategy={} count={}", mode, strategy, count, e);
            throw new IllegalStateException("抽取学生失败", e);
        }
    }

    public List<StudentProfile> listAllStudents() {
        try {
            return repository.listAllStudents();
        } catch (SQLException e) {
            log.error("加载学生列表失败", e);
            throw new IllegalStateException("加载学生列表失败", e);
        }
    }

    public long startSession(RollCallMode mode, RollCallStrategy strategy, Integer count) {
        try {
            return repository.createSession(mode.name(), strategy.name(), count);
        } catch (SQLException e) {
            log.error("创建点名会话失败", e);
            throw new IllegalStateException("创建点名会话失败", e);
        }
    }

    public void endSession(long sessionId) {
        try {
            repository.endSession(sessionId);
        } catch (SQLException e) {
            log.error("结束点名会话失败 id={}", sessionId, e);
            throw new IllegalStateException("结束点名会话失败", e);
        }
    }

    public long addItem(long sessionId, long studentId, RollCallStatus status, String note) {
        try {
            return repository.insertItem(sessionId, studentId, status, note);
        } catch (SQLException e) {
            log.error("插入点名明细失败 sessionId={} studentId={}", sessionId, studentId, e);
            throw new IllegalStateException("插入点名明细失败", e);
        }
    }

    public void updateItemStatus(long itemId, RollCallStatus status, LocalDateTime answeredAt) {
        try {
            repository.updateItemStatus(itemId, status, answeredAt);
        } catch (SQLException e) {
            log.error("更新点名明细失败 itemId={} status={}", itemId, status, e);
            throw new IllegalStateException("更新点名明细失败", e);
        }
    }

    public void seedStudentsIfEmpty() {
        try {
            repository.seedStudentsIfEmpty();
        } catch (SQLException e) {
            log.error("写入学生样例数据失败", e);
            throw new IllegalStateException("写入学生样例数据失败", e);
        }
    }

    /** 清空并重写 student 表，便于随时替换学生信息。 */
    public void replaceStudents(List<StudentProfile> students) {
        try {
            repository.replaceStudents(students);
        } catch (SQLException e) {
            log.error("替换学生表失败", e);
            throw new IllegalStateException("替换学生表失败", e);
        }
    }

    /** 从 classpath CSV 导入学生并替换表数据 */
    public void replaceStudentsFromCsv(String resourcePath) {
        List<StudentProfile> list = StudentCsvLoader.load(resourcePath);
        replaceStudents(list);
    }
}
