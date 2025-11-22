package com.example.util;

import com.example.model.rollcall.StudentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 classpath 下的 CSV 加载学生数据。
 * 格式：stu_no,name,photo_path
 */
public class StudentCsvLoader {

    private static final Logger log = LoggerFactory.getLogger(StudentCsvLoader.class);

    public static List<StudentProfile> load(String resourcePath) {
        List<StudentProfile> list = new ArrayList<>();
        try (InputStream in = StudentCsvLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("找不到资源文件: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) { // 跳过表头
                        isHeader = false;
                        continue;
                    }
                    if (line.isBlank()) continue;
                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) continue;
                    StudentProfile s = new StudentProfile();
                    s.setStudentNo(parts[0].trim());
                    s.setName(parts[1].trim());
                    s.setPhotoPath(parts[2].trim());
                    s.setAbsenceCount(0);
                    s.setCalledCount(0);
                    list.add(s);
                }
            }
            log.info("从 {} 加载学生数据 {} 条", resourcePath, list.size());
        } catch (IOException e) {
            throw new IllegalStateException("读取学生 CSV 失败: " + resourcePath, e);
        }
        return list;
    }
}
