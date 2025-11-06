package com.example.view;

import javax.swing.*;
import java.awt.*;
import com.example.util.ResourceLoader;

public class GamePanel extends JPanel {

    private Image donald;
    private Image[] babyDucks;

    public GamePanel() {
        this.setBackground(new Color(220, 245, 255));
        donald = ResourceLoader.loadImage("images/donald.png");

        babyDucks = new Image[]{
            ResourceLoader.loadImage("images/dunk_red.png"),
            ResourceLoader.loadImage("images/dunk_bule.png"),
            ResourceLoader.loadImage("images/dunk_yellow.png")
        };
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制唐老鸭（左下角）
        if (donald != null)
            g.drawImage(donald, 45, getHeight() - 220, 150, 150, this);

        // 绘制三只唐小鸭（右侧）
        int startX = getWidth() - 400;
        for (int i = 0; i < babyDucks.length; i++) {
            if (babyDucks[i] != null)
                g.drawImage(babyDucks[i], startX + i * 100, getHeight() - 200, 100, 100, this);
        }
    }
}
