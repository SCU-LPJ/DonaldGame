package com.example.repository;

import com.example.model.rollcall.RollCallStrategy;
import com.example.model.rollcall.StudentProfile;
import com.example.util.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<StudentProfile> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapStudent(rs));
                }
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
}
