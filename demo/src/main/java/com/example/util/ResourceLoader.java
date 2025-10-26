package com.example.util;

import javax.swing.*;
import java.awt.*;

public class ResourceLoader {
    public static Image loadImage(String path) {
        try {
            return new ImageIcon(ResourceLoader.class.getClassLoader().getResource(path)).getImage();
        } catch (Exception e) {
            System.err.println("图片加载失败：" + path);
            return null;
        }
    }
}
