package com.example.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);
    public static Image loadImage(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            // 1) 先按 classpath 查找
            URL url = ResourceLoader.class.getClassLoader().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
            // 2) 兼容写了 "resources/..." 的路径，去掉前缀重试
            if (path.startsWith("resources/")) {
                String trimmed = path.substring("resources/".length());
                url = ResourceLoader.class.getClassLoader().getResource(trimmed);
                if (url != null) {
                    return new ImageIcon(url).getImage();
                }
            }
            // 3) 再尝试文件系统绝对/相对路径
            File f = new File(path);
            if (f.exists()) {
                return new ImageIcon(path).getImage();
            }
            log.error("图片加载失败：{} （未找到资源/文件）", path);
            return null;
        } catch (Exception e) {
            log.error("图片加载失败：{}", path, e);
            return null;
        }
    }
}
