package com.example;

import com.example.service.RollCallService;
import com.example.view.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("启动应用，准备初始化数据库与界面");
        RollCallService rollCallService = new RollCallService();
        rollCallService.ensureSchema();
        // 如需使用 CSV 覆盖样例数据，可启用下一行
        rollCallService.replaceStudentsFromCsv("config/students.csv");
        //rollCallService.seedStudentsIfEmpty();
        new MainFrame();
    }
}
