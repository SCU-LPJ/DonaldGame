package com.example.view;

import javax.swing.*;
import java.awt.*;
import com.example.controller.GameController;

public class MainFrame extends JFrame {

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private GamePanel gamePanel;

    private GameController controller;

    public MainFrame() {
        super("唐老鸭与小鸭们的互动世界");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 600);
        this.setLayout(new BorderLayout());

        // ===== 顶部提示信息 =====
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBackground(new Color(245, 245, 255)); // 背景颜色淡蓝

        JLabel titleLabel = new JLabel("✨ 欢迎来到唐老鸭与小鸭们的互动世界 ✨", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(50, 50, 120));
        infoPanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea featureText = new JTextArea(
            "具备的交互功能：\n" +
            "🐣 发红包\n" +
            "🐣 统计代码量\n" +
            "🐣 玩游戏\n" +
            "🐣 调用AI与唐老鸭对话互动\n" +
            "🐣 点击小鸭或在聊天框输入“唐老鸭/红色唐小鸭/蓝色唐小鸭/黄色唐小鸭”触发表演\n"
        );
        featureText.setEditable(false);
        featureText.setOpaque(false);
        featureText.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        featureText.setForeground(new Color(70, 70, 100));
        featureText.setFocusable(false);

        JPanel featureWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        featureWrapper.setOpaque(false);
        featureWrapper.add(featureText);
        infoPanel.add(featureWrapper, BorderLayout.CENTER);

        this.add(infoPanel, BorderLayout.NORTH);

        // ===== 游戏主画面（唐老鸭 + 小鸭们） =====
        gamePanel = new GamePanel();
        this.add(gamePanel, BorderLayout.CENTER);

        // ===== 底部输入区 =====
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("发送");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        this.add(inputPanel, BorderLayout.SOUTH);

        // ===== 聊天显示区（右侧） =====
        chatArea = new JTextArea(20, 25);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);          // 软换行
        chatArea.setWrapStyleWord(true);     // 按词换行
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("聊天记录"));
        this.add(scrollPane, BorderLayout.EAST);

        // ===== 控制器创建与绑定（关键！）=====
        controller = new GameController(this);
        gamePanel.bindController(controller);   // 让 GamePanel 的点击事件能回调控制器

        // 将“发送”设为默认按钮（回车发送）
        getRootPane().setDefaultButton(sendButton);

        // ===== 事件绑定（判空、清空、聚焦） =====
        sendButton.addActionListener(e -> sendCurrentInput());
        inputField.addActionListener(e -> sendCurrentInput());

        this.setVisible(true);
        inputField.requestFocusInWindow();
    }

    private void sendCurrentInput() {
        String text = inputField.getText();
        if (text != null) text = text.trim();
        if (text == null || text.isEmpty()) {
            return; // 不发送空消息
        }
        controller.handleUserInput(text);
        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    /** 供控制器输出聊天文本 */
    public void appendChat(String msg) {
        chatArea.append(msg + "\n");
        // 自动滚动到底部
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public JTextField getInputField() {
        return inputField;
    }
}
