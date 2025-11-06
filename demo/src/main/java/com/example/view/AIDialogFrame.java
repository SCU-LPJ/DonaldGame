package com.example.view;

import javax.swing.*;
import java.awt.*;
import com.example.service.AIChatService;

public class AIDialogFrame extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private AIChatService aiService;

    public AIDialogFrame() {
        super("唐老鸭的AI助手");
        this.setSize(600, 500);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("发送");

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(inputPanel, BorderLayout.SOUTH);

        aiService = new AIChatService();

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        this.setVisible(true);
    }

    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) return;

        chatArea.append("你: " + userInput + "\n");
        inputField.setText("");
        sendButton.setEnabled(false);
        inputField.setEnabled(false);
        chatArea.append("唐老鸭AI 正在思考中...\n");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return aiService.askAI(userInput);
            }

            @Override
            protected void done() {
                try {
                    String reply = get();
                    chatArea.append("唐老鸭AI: " + reply + "\n\n");
                } catch (Exception e) {
                    chatArea.append("【错误】AI接口调用失败。\n\n");
                } finally {
                    sendButton.setEnabled(true);
                    inputField.setEnabled(true);
                    inputField.requestFocus();
                }
            }
        }.execute();
    }
}
