package com.example.view;


import com.example.controller.GameController;
import com.example.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** 场景画布：渲染 4 只鸭并支持点击触发（适配 ResourceLoader.loadImage） */
public class GamePanel extends JPanel {

    private GameController controller;

    // 统一的缩放尺寸（按需调整）
    private static final int DUCK_WIDTH  = 150;
    private static final int DUCK_HEIGHT = 150;

    private JLabel donaldLabel;
    private JLabel redLabel;
    private JLabel blueLabel;
    private JLabel yellowLabel;

    private final List<DuckSprite> duckSprites = new ArrayList<>();
    private SkinTheme currentSkin = SkinTheme.ORIGINAL;

    public GamePanel() {
        // 用绝对布局摆位最直观；
        setLayout(null);
        setBackground(new Color(240, 248, 255));
        initDuckLabels();
        initSkinButtons();
        applySkin(currentSkin);
    }

    /** 由 MainFrame 在创建 controller 后注入 */
    public void bindController(GameController controller) {
        this.controller = controller;
    }

    private void initDuckLabels() {
        donaldLabel = registerDuckLabel("唐老鸭", "donald", 40, 40);
        redLabel    = registerDuckLabel("红色唐小鸭", "dunk_red", 260, 60);
        blueLabel   = registerDuckLabel("蓝色唐小鸭", "dunk_bule", 140, 180);
        yellowLabel = registerDuckLabel("黄色唐小鸭", "dunk_yellow", 340, 200);
    }

    private void initSkinButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(SkinTheme.values().length, 1, 0, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("皮肤变装"));
        buttonPanel.setBounds(500, 20, 200, 240);

        for (SkinTheme theme : SkinTheme.values()) {
            JButton button = new JButton(theme.displayName);
            button.setBackground(theme.buttonColor);
            button.setForeground(theme.textColor);
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.addActionListener(e -> applySkin(theme));
            buttonPanel.add(button);
        }

        add(buttonPanel);
    }

    private JLabel registerDuckLabel(String duckName, String baseImageKey, int x, int y) {
        JLabel label = createDuckLabel(duckName, x, y);
        duckSprites.add(new DuckSprite(label, baseImageKey, duckName));
        add(label);
        return label;
    }

    private JLabel createDuckLabel(String duckName, int x, int y) {
        JLabel label = new JLabel();
        label.setBounds(x, y, DUCK_WIDTH, DUCK_HEIGHT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        label.setToolTipText(duckName);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (controller != null) {
                    controller.triggerDuckByName(duckName);
                }
            }
        });

        return label;
    }

    private void applySkin(SkinTheme theme) {
        if (theme == null) {
            return;
        }
        currentSkin = theme;
        for (DuckSprite sprite : duckSprites) {
            updateDuckIcon(sprite, currentSkin);
        }
        repaint();
    }

    private void updateDuckIcon(DuckSprite sprite, SkinTheme theme) {
        String resourcePath = theme.resolve(sprite.baseKey);
        Image img = ResourceLoader.loadImage(resourcePath);
        if (img != null) {
            ImageIcon icon = scaleIcon(img, DUCK_WIDTH, DUCK_HEIGHT);
            sprite.label.setIcon(icon);
            resetFallbackStyle(sprite.label);
        } else {
            applyFallbackStyle(sprite.label, sprite.displayName);
        }
    }

    /** 将 Image 按比例缩放到不超过 maxW x maxH 的尺寸 */
    private ImageIcon scaleIcon(Image src, int maxW, int maxH) {
        int w = src.getWidth(null);
        int h = src.getHeight(null);
        if (w <= 0 || h <= 0) {
            // 异常尺寸兜底
            Image scaled = src.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        double scale = Math.min(maxW / (double) w, maxH / (double) h);
        int nw = (int) Math.round(w * scale);
        int nh = (int) Math.round(h * scale);
        Image scaled = src.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void applyFallbackStyle(JLabel label, String text) {
        label.setText(text);
        label.setIcon(null);
        label.setOpaque(true);
        label.setBackground(new Color(255, 245, 230));
        label.setBorder(BorderFactory.createLineBorder(new Color(255, 170, 120)));
    }

    private void resetFallbackStyle(JLabel label) {
        label.setText(null);
        label.setOpaque(false);
        label.setBorder(null);
    }

    private static class DuckSprite {
        final JLabel label;
        final String baseKey;
        final String displayName;

        DuckSprite(JLabel label, String baseKey, String displayName) {
            this.label = label;
            this.baseKey = baseKey;
            this.displayName = displayName;
        }
    }

    private enum SkinTheme {
        ORIGINAL("原皮", "", new Color(234, 234, 234), new Color(70, 70, 70)),
        SPRING("春日稀有皮", "1", new Color(205, 255, 207), new Color(34, 87, 44)),
        SUMMER("夏季史诗皮", "2", new Color(178, 220, 255), new Color(25, 70, 120)),
        AUTUMN("秋日传说皮", "3", new Color(255, 210, 170), new Color(120, 62, 20)),
        WINTER("冬季神话皮", "4", new Color(210, 205, 255), new Color(62, 44, 122));

        final String displayName;
        final String suffix;
        final Color buttonColor;
        final Color textColor;

        SkinTheme(String displayName, String suffix, Color buttonColor, Color textColor) {
            this.displayName = displayName;
            this.suffix = suffix;
            this.buttonColor = buttonColor;
            this.textColor = textColor;
        }

        String resolve(String baseKey) {
            if (suffix == null || suffix.isEmpty()) {
                return "images/" + baseKey + ".png";
            }
            return "images/" + baseKey + suffix + ".png";
        }
    }
}
