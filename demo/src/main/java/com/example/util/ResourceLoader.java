package com.example.util;

import javax.swing.*;
import java.awt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);
    public static Image loadImage(String path) {
        try {
            return new ImageIcon(ResourceLoader.class.getClassLoader().getResource(path)).getImage();
        } catch (Exception e) {
            log.error("图片加载失败：{}", path, e);
            return null;
        }
    }
}
