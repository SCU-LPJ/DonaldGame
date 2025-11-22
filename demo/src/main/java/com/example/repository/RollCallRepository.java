package com.example.repository;

import com.example.model.rollcall.RollCallItem;
import com.example.model.rollcall.RollCallStatus;
import com.example.model.rollcall.RollCallStrategy;
import com.example.model.rollcall.StudentProfile;
import com.example.util.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDBC helper for roll-call related tables. Keeps SQL in one place and
 * hides DataSource details from upper layers.
 * 即与点名相关的数据库访问封装 统一管理与点名相关的数据库表
 */
// 操作数据库的唯一地方 DAO层 写SQL语句 获取连接池 执行sql 返回对象
public class RollCallRepository {

    private static final Logger log = LoggerFactory.getLogger(RollCallRepository.class);
    private final DataSource dataSource;

    public RollCallRepository() {
        this(DataSourceFactory.getDataSource());
    }

    public RollCallRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Create base tables if missing; safe to call at startup. */
    // 运行时自动创建数据库表
    public void initSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            log.info("建表检查：student / leave_request / roll_call_session / roll_call_item");
            // 学生表
            st.execute("""
                CREATE TABLE IF NOT EXISTS student (
                  id BIGSERIAL PRIMARY KEY,
                  stu_no VARCHAR(32) UNIQUE NOT NULL,
                  name VARCHAR(64) NOT NULL,
                  photo_path VARCHAR(255),
                  absence_count INT DEFAULT 0,
                  called_count INT DEFAULT 0,
                  created_at TIMESTAMP DEFAULT NOW(),
                  updated_at TIMESTAMP DEFAULT NOW()
                )
                """);
            // 请假表
            st.execute("""
                CREATE TABLE IF NOT EXISTS leave_request (
                  id BIGSERIAL PRIMARY KEY,
                  student_id BIGINT REFERENCES student(id),
                  start_time TIMESTAMP NOT NULL,
                  end_time TIMESTAMP NOT NULL,
                  reason VARCHAR(255),
                  status VARCHAR(16) NOT NULL,
                  created_at TIMESTAMP DEFAULT NOW()
                )
                """);

            // 点名会话表   
            st.execute("""
                CREATE TABLE IF NOT EXISTS roll_call_session (
                  id BIGSERIAL PRIMARY KEY,
                  started_at TIMESTAMP NOT NULL DEFAULT NOW(),
                  ended_at TIMESTAMP,
                  mode VARCHAR(16) NOT NULL,
                  strategy VARCHAR(32),
                  count INT,
                  created_at TIMESTAMP DEFAULT NOW()
                )
                """);

            // 点名明细表
            st.execute("""
                CREATE TABLE IF NOT EXISTS roll_call_item (
                  id BIGSERIAL PRIMARY KEY,
                  session_id BIGINT REFERENCES roll_call_session(id),
                  student_id BIGINT REFERENCES student(id),
                  called_at TIMESTAMP NOT NULL DEFAULT NOW(),
                  answered_at TIMESTAMP,
                  status VARCHAR(16) NOT NULL,
                  note VARCHAR(255)
                )
                """);

            st.execute("CREATE INDEX IF NOT EXISTS idx_leave_request_range ON leave_request(student_id, start_time, end_time)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_roll_call_item_session ON roll_call_item(session_id)");
            log.info("建表检查完成");
        }
    }

    // 查询所有学生并返回java对象
    public List<StudentProfile> listAllStudents() throws SQLException {
        String sql = """
                SELECT id, stu_no, name, photo_path, absence_count, called_count
                  FROM student
                 ORDER BY id
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<StudentProfile> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
            return list;
        }
    }

    /** Pick students by strategy; limit ignored if < 1. */
    // 根据策略选择学生；如果限制小于1，则忽略限制。
    public List<StudentProfile> pickStudents(RollCallStrategy strategy, int limit) throws SQLException {
        if (limit < 1) {
            return Collections.emptyList();
        }

        // 策略映射到sql排序条件
        String orderBy = switch (strategy) {
            case MAX_ABSENCE -> "ORDER BY absence_count DESC, called_count ASC";
            case MIN_CALLED -> "ORDER BY called_count ASC, absence_count DESC";
            default -> "ORDER BY random()";
        };

        String sql = """
                SELECT id, stu_no, name, photo_path, absence_count, called_count
                  FROM student
                 %s
                 LIMIT ?
                """.formatted(orderBy);

        log.info("按策略抽取学生 strategy={} limit={}", strategy, limit);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<StudentProfile> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapStudent(rs));
                }
                log.debug("抽取完成，返回 {} 人", list.size());
                return list;
            }
        }
    }

    // 将ResultSet映射为StudentProfile对象  即把数据库行转化成StudentProfile对象
    private StudentProfile mapStudent(ResultSet rs) throws SQLException {
        StudentProfile s = new StudentProfile();
        s.setId(rs.getLong("id"));
        s.setStudentNo(rs.getString("stu_no"));
        s.setName(rs.getString("name"));
        s.setPhotoPath(rs.getString("photo_path"));
        s.setAbsenceCount(rs.getInt("absence_count"));
        s.setCalledCount(rs.getInt("called_count"));
        return s;
    }

    /** 创建点名会话，返回生成的 id */
    public long createSession(String mode, String strategy, Integer count) throws SQLException {
        String sql = """
                INSERT INTO roll_call_session (mode, strategy, count)
                VALUES (?,?,?)
                RETURNING id
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mode);
            ps.setString(2, strategy);
            if (count == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, count);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    log.info("创建点名会话成功，id={}", id);
                    return id;
                }
                throw new SQLException("创建点名会话失败，无返回 id");
            }
        }
    }

    /** 结束会话，写入结束时间 */
    public void endSession(long sessionId) throws SQLException {
        String sql = "UPDATE roll_call_session SET ended_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.executeUpdate();
            log.info("结束点名会话：{}", sessionId);
        }
    }

    /** 插入点名明细，返回 id */
    public long insertItem(long sessionId, long studentId, RollCallStatus status, String note) throws SQLException {
        String sql = """
                INSERT INTO roll_call_item (session_id, student_id, status, note)
                VALUES (?,?,?,?)
                RETURNING id
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            ps.setLong(2, studentId);
            ps.setString(3, status.name());
            ps.setString(4, note);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    log.debug("插入点名明细成功 id={}", id);
                    return id;
                }
                throw new SQLException("插入点名明细失败");
            }
        }
    }

    /** 更新明细状态与 answeredAt（可 null） */
    public void updateItemStatus(long itemId, RollCallStatus status, LocalDateTime answeredAt) throws SQLException {
        String sql = "UPDATE roll_call_item SET status=?, answered_at=? WHERE id=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (answeredAt == null) {
                ps.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(answeredAt));
            }
            ps.setLong(3, itemId);
            ps.executeUpdate();
            log.debug("更新点名明细状态 itemId={} status={}", itemId, status);
        }
    }

    public List<RollCallItem> findItemsBySession(long sessionId) throws SQLException {
        String sql = """
                SELECT id, session_id, student_id, called_at, answered_at, status, note
                  FROM roll_call_item
                 WHERE session_id = ?
                 ORDER BY called_at
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                List<RollCallItem> list = new ArrayList<>();
                while (rs.next()) {
                    RollCallItem item = new RollCallItem();
                    item.setId(rs.getLong("id"));
                    item.setSessionId(rs.getLong("session_id"));
                    item.setStudentId(rs.getLong("student_id"));
                    item.setCalledAt(rs.getTimestamp("called_at").toLocalDateTime());
                    Timestamp ans = rs.getTimestamp("answered_at");
                    if (ans != null) item.setAnsweredAt(ans.toLocalDateTime());
                    item.setStatus(RollCallStatus.valueOf(rs.getString("status")));
                    item.setNote(rs.getString("note"));
                    list.add(item);
                }
                return list;
            }
        }
    }

    /** 简单样例插入，便于测试 */
    public void seedStudentsIfEmpty() throws SQLException {
        String countSql = "SELECT COUNT(1) FROM student";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery(countSql)) {
                if (rs.next() && rs.getLong(1) > 0) {
                    return;
                }
            }

            log.info("student 表为空，写入样例数据");
            st.execute("""
                INSERT INTO student(stu_no, name, photo_path, absence_count, called_count) VALUES
                ('2023001','刘畅','resources/images/donald.png',0,0),
                ('2023002','侯睿','resources/images/student2.png',0,0),
                ('2023003','Jett','resources/images/student3.png',0,0),
                ('2023004','mom','resources/images/student4.png',0,0),
                ('2023005','dad','resources/images/student5.png',0,0)
                """);
        }
    }

    /**
     * 清空 student 表并插入给定列表，用于随时替换样例或重导全量数据。
     */
    public void replaceStudents(List<StudentProfile> students) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.execute("TRUNCATE TABLE student RESTART IDENTITY CASCADE");
            }

            String sql = """
                    INSERT INTO student(stu_no, name, photo_path, absence_count, called_count)
                    VALUES (?,?,?,?,?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (StudentProfile s : students) {
                    ps.setString(1, s.getStudentNo());
                    ps.setString(2, s.getName());
                    ps.setString(3, s.getPhotoPath());
                    ps.setInt(4, s.getAbsenceCount());
                    ps.setInt(5, s.getCalledCount());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
            log.info("已替换 student 表，共写入 {} 条", students.size());
        }
    }
}
