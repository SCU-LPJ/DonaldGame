package com.example.view;

import com.example.controller.GameController;
import com.example.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** 场景画布：渲染 4 只鸭并支持点击触发（适配 ResourceLoader.loadImage） */
public class GamePanel extends JPanel {

    private GameController controller;

    // 统一的缩放尺寸（按需调整）
    private static final int DUCK_WIDTH  = 140;
    private static final int DUCK_HEIGHT = 140;

    private JLabel donaldLabel;
    private JLabel redLabel;
    private JLabel blueLabel;
    private JLabel yellowLabel;

    public GamePanel() {
        // 用绝对布局摆位最直观；如果你已有布局，可自行替换
        setLayout(null);
        setBackground(new Color(240, 248, 255));
        initDuckLabels();
    }

    /** 由 MainFrame 在创建 controller 后注入 */
    public void bindController(GameController controller) {
        this.controller = controller;
    }

    private void initDuckLabels() {
        donaldLabel = createDuckLabel("唐老鸭",       "images/donald.png",   40,  40);
        redLabel    = createDuckLabel("红色唐小鸭",   "images/dunk_red.png",   260,  60);
        blueLabel   = createDuckLabel("蓝色唐小鸭",   "images/dunk_bule.png",  140, 180);
        yellowLabel = createDuckLabel("黄色唐小鸭",   "images/dunk_yellow.png",340, 200);

        add(donaldLabel);
        add(redLabel);
        add(blueLabel);
        add(yellowLabel);
    }

    private JLabel createDuckLabel(String duckName, String resourcePath, int x, int y) {
        Image img = ResourceLoader.loadImage(resourcePath);
        JLabel label;

        if (img != null) {
            // 缩放到统一尺寸并居中显示
            ImageIcon icon = scaleIcon(img, DUCK_WIDTH, DUCK_HEIGHT);
            label = new JLabel(icon);
            // 用 icon 实际大小作为组件大小
            label.setBounds(x, y, icon.getIconWidth(), icon.getIconHeight());
        } else {
            // 兜底：资源丢失时给一个可见占位
            label = fallbackLabel(duckName);
            label.setBounds(x, y, DUCK_WIDTH, DUCK_HEIGHT);
        }

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

    /** 图片缺失时的可见占位组件 */
    private JLabel fallbackLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(new Color(255, 245, 230));
        l.setBorder(BorderFactory.createLineBorder(new Color(255, 170, 120)));
        return l;
    }
}
