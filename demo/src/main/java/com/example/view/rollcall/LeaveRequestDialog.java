package com.example.view.rollcall;

import com.example.service.RollCallService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * 请假窗口：输入学号与原因，提交写入 leave_request。
 */
public class LeaveRequestDialog extends JDialog {

    private final JTextField stuNoField = new JTextField(15);
    private final JTextField reasonField = new JTextField(15);
    private final JButton submitBtn = new JButton("提交请假信息");
    private final JButton exitBtn = new JButton("退出");

    public LeaveRequestDialog(JFrame owner, RollCallService rollCallService, Runnable onClose) {
        super(owner, "请假登记", true);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("学号："));
        form.add(stuNoField);
        form.add(new JLabel("原因："));
        form.add(reasonField);
        form.add(submitBtn);
        form.add(exitBtn);
        add(form, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> {
            String stuNo = stuNoField.getText().trim();
            String reason = reasonField.getText().trim();
            if (stuNo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写学号");
                return;
            }
            try {
                LocalDateTime start = LocalDateTime.now();
                LocalDateTime end = start.plusHours(3); // 简化：默认3小时
                rollCallService.submitLeaveRequest(stuNo, start, end, reason);
                JOptionPane.showMessageDialog(this, "请假已登记成功");
                stuNoField.setText("");
                reasonField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "提交失败：" + ex.getMessage());
            }
        });

        exitBtn.addActionListener(e -> {
            dispose();
            if (onClose != null) onClose.run();
        });

        pack();
        setLocationRelativeTo(owner);
    }
}
