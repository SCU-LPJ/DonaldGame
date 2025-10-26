package view;

import javax.swing.*;
import java.awt.*;
import controller.GameController;

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

        controller = new GameController(this);

        // 游戏主画面（展示角色）
        gamePanel = new GamePanel();
        this.add(gamePanel, BorderLayout.CENTER);

        // 底部输入区
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("发送");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        this.add(inputPanel, BorderLayout.SOUTH);

        // 聊天显示区
        chatArea = new JTextArea(8, 40);
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        this.add(scrollPane, BorderLayout.NORTH);

        // 事件绑定
        sendButton.addActionListener(e -> controller.handleUserInput(inputField.getText()));
        inputField.addActionListener(e -> controller.handleUserInput(inputField.getText()));

        this.setVisible(true);
    }

    public void appendChat(String msg) {
        chatArea.append(msg + "\n");
    }

    public JTextField getInputField() {
        return inputField;
    }
}
