package com.example.view.rollcall;

import com.example.model.rollcall.RollCallMode;
import com.example.model.rollcall.RollCallStrategy;
import com.example.model.rollcall.RollCallStatus;
import com.example.model.rollcall.StudentProfile;
import com.example.service.RollCallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 简单的点名界面：配置 + 展示当前学生 + 四个状态按钮。
 * 这里不做复杂调度，仅按列表顺序逐个处理。
 */
public class RollCallPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(RollCallPanel.class);

    private final RollCallService rollCallService;
    private List<StudentProfile> candidates;
    private int currentIndex = -1;
    private long sessionId;

    private final JLabel infoLabel = new JLabel("请选择点名模式后开始", SwingConstants.CENTER);
    private final JLabel studentLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel photoLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel roundLabel = new JLabel("当前点名轮次：-", SwingConstants.LEFT);
    private final JLabel maxAbsentLabel = new JLabel("缺勤次数最多：-", SwingConstants.LEFT);
    private final JLabel minCalledLabel = new JLabel("点到次数最少：-", SwingConstants.LEFT);

    private final JButton startBtn = new JButton("开始点名");
    private final JComboBox<RollCallMode> modeBox = new JComboBox<>(RollCallMode.values());
    private final JComboBox<RollCallStrategy> stratBox = new JComboBox<>(RollCallStrategy.values());
    private final JTextField countField = new JTextField("10", 5);
    private final JButton clearBtn = new JButton("清空数据");

    private final JButton presentBtn = new JButton("到");
    private final JButton leaveBtn = new JButton("请假");
    private final JButton lateBtn = new JButton("迟到");
    private final JButton absentBtn = new JButton("旷课");

    public RollCallPanel(RollCallService rollCallService) {
        this.rollCallService = rollCallService;
        // 为 CardLayout 提供一个合理的首选宽度，避免被右侧聊天区挤压
        setPreferredSize(new Dimension(800, 0));
        setLayout(new BorderLayout());
        buildTopForm();
        buildCenter();
        buildSideStats();
        bindActions();
    }

    private void buildTopForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("模式:"));
        form.add(modeBox);
        form.add(new JLabel("策略:"));
        form.add(stratBox);
        form.add(new JLabel("人数:"));
        form.add(countField);
        form.add(startBtn);
        form.add(clearBtn);
        add(form, BorderLayout.NORTH);
    }

    private void buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        infoLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        center.add(infoLabel, BorderLayout.NORTH);

        studentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        center.add(studentLabel, BorderLayout.SOUTH);

        photoLabel.setPreferredSize(new Dimension(220, 220));
        center.add(photoLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(presentBtn);
        buttons.add(leaveBtn);
        buttons.add(lateBtn);
        buttons.add(absentBtn);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        setButtonsEnabled(false);
    }

    private void buildSideStats() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createTitledBorder("统计信息"));

        roundLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        maxAbsentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        minCalledLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        roundLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        maxAbsentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        minCalledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(roundLabel);
        side.add(Box.createVerticalStrut(8));
        side.add(maxAbsentLabel);
        side.add(Box.createVerticalStrut(8));
        side.add(minCalledLabel);

        add(side, BorderLayout.WEST);
    }

    private void bindActions() {
        startBtn.addActionListener(e -> start());
        presentBtn.addActionListener(e -> mark(RollCallStatus.PRESENT));
        leaveBtn.addActionListener(e -> mark(RollCallStatus.LEAVE));
        lateBtn.addActionListener(e -> mark(RollCallStatus.LATE));
        absentBtn.addActionListener(e -> mark(RollCallStatus.ABSENT));
        clearBtn.addActionListener(e -> clearAllData());
    }

    private void start() {
        RollCallMode mode = (RollCallMode) modeBox.getSelectedItem();
        RollCallStrategy strat = (RollCallStrategy) stratBox.getSelectedItem();
        int count = parseCount();

        candidates = rollCallService.selectCandidates(mode, strat, count);
        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可点名的学生，请先导入数据。");
            return;
        }
        sessionId = rollCallService.startSession(mode, strat, count);
        currentIndex = -1;
        infoLabel.setText("本次点名人数：" + candidates.size());
        refreshStatsPanel();
        next();
        setButtonsEnabled(true);
    }

    private int parseCount() {
        try {
            return Integer.parseInt(countField.getText().trim());
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private void next() {
        currentIndex++;
        if (currentIndex >= candidates.size()) {
            rollCallService.endSession(sessionId);
            infoLabel.setText("点名完成！");
            studentLabel.setText("");
            photoLabel.setIcon(null);
            setButtonsEnabled(false);
            return;
        }
        StudentProfile s = candidates.get(currentIndex);
        studentLabel.setText("当前：" + s.getName() + " (" + s.getStudentNo() + ")");
        updatePhoto(s.getPhotoPath());
    }

    private void mark(RollCallStatus status) {
        if (currentIndex < 0 || currentIndex >= candidates.size()) return;
        StudentProfile s = candidates.get(currentIndex);
        long itemId = rollCallService.addItem(sessionId, s.getId(), status, null);
        // 10分钟内赶到逻辑：此处简单写 answeredAt=now，实际可在 UI 层等用户点击时再传
        rollCallService.updateItemStatus(itemId, status, LocalDateTime.now());
        log.info("学生 {}({}) 标记为 {}", s.getName(), s.getStudentNo(), status);
        next();
    }

    private void setButtonsEnabled(boolean enabled) {
        presentBtn.setEnabled(enabled);
        leaveBtn.setEnabled(enabled);
        lateBtn.setEnabled(enabled);
        absentBtn.setEnabled(enabled);
    }

    private void clearAllData() {
        int res = JOptionPane.showConfirmDialog(this,
                "确认清空学生/点名会话/明细全部数据吗？此操作不可恢复！",
                "危险操作确认", JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;
        rollCallService.clearAllRollCallData();
        candidates = Collections.emptyList();
        currentIndex = -1;
        infoLabel.setText("已清空所有数据，请重新导入学生后点名");
        studentLabel.setText("");
        photoLabel.setIcon(null);
        setButtonsEnabled(false);
        log.warn("已清空全部点名相关数据");
        // 清空统计区显示
        roundLabel.setText("当前点名轮次：-");
        maxAbsentLabel.setText("缺勤次数最多：-");
        minCalledLabel.setText("点到次数最少：-");
    }

    /** 刷新左侧统计信息：当前轮次 / 缺勤最多 / 点到最少 */
    private void refreshStatsPanel() {
        long rounds = rollCallService.getSessionCount();
        roundLabel.setText("当前点名轮次：" + rounds);

        StudentProfile maxAbsent = rollCallService.getMaxAbsenceStudent();
        if (maxAbsent != null) {
            maxAbsentLabel.setText("缺勤次数最多：" + maxAbsent.getName() + " (" + maxAbsent.getStudentNo() + ")");
        } else {
            maxAbsentLabel.setText("缺勤次数最多：-");
        }

        StudentProfile minCalled = rollCallService.getMinCalledStudent();
        if (minCalled != null) {
            minCalledLabel.setText("点到次数最少：" + minCalled.getName() + " (" + minCalled.getStudentNo() + ")");
        } else {
            minCalledLabel.setText("点到次数最少：-");
        }
    }

    private void updatePhoto(String path) {
        if (path == null || path.isBlank()) {
            photoLabel.setIcon(null);
            return;
        }
        Image img = ResourceLoader.loadImage(path);
        if (img != null) {
            // 缩放以适配显示区域
            Image scaled = img.getScaledInstance(photoLabel.getPreferredSize().width,
                    photoLabel.getPreferredSize().height, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaled));
        } else {
            photoLabel.setIcon(null);
        }
    }
}
