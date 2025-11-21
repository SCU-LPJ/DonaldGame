package com.example.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 简单跨平台 TTS 调用（Windows / macOS / Linux） */
public class TTSUtil {

    private static final Logger log = LoggerFactory.getLogger(TTSUtil.class);

    /** 默认：在 macOS 上使用中文女声 “Ting-Ting”，语速 180；其他平台沿用原方案 */
    public static void speak(String text) {
        speak(text, null, null);
    }

    /**
     * 可选指定 voice / rate（仅 macOS 生效）
     * @param text  要朗读的文本
     * @param voice macOS 的语音名称，例如 "Ting-Ting"(简中) / "Mei-Jia"(台普) / "Sin-Ji"(粤语)；null 则走默认
     * @param rate  语速（每分钟词数，大致范围 100~400），null 则走默认
     */
    public static void speak(String text, String voice, Integer rate) {
        if (text == null || text.isEmpty()) return;

        // 基础转义：避免被命令行截断
        String safe = sanitize(text);

        String os = System.getProperty("os.name").toLowerCase();
        List<String> cmd = new ArrayList<>();

        try {
            if (os.contains("mac")) {
                // macOS: say [-v VoiceName] [-r Rate] "text"
                cmd.add("say");

                // 语音与语速：给出更适合中文的默认
                String v = (voice == null || voice.isBlank()) ? "Ting-Ting" : voice; // 中文普通话
                int r = (rate == null) ? 180 : rate; // 适中语速

                // 仅当 voice/rate 合法时添加参数，避免某些系统没有对应 voice 报错
                if (!v.isBlank()) { cmd.add("-v"); cmd.add(v); }
                cmd.add("-r"); cmd.add(String.valueOf(r));

                cmd.add(safe);
            } else if (os.contains("win")) {
                // Windows：PowerShell + SAPI
                cmd.add("powershell");
                cmd.add("-ExecutionPolicy"); cmd.add("Bypass");
                cmd.add("-Command");

                // 单引号转义为两次单引号
                String ps = "Add-Type -AssemblyName System.Speech; " +
                        "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" +
                        safe.replace("'", "''") + "');";
                cmd.add(ps);
            } else {
                // Linux：espeak（若系统未装 espeak，需要用户自行安装）
                cmd.add("espeak");
                cmd.add(safe);
            }

            new ProcessBuilder(cmd).inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("TTS 失败", e);
        }
    }

    /** 基础转义：避免换行、引号对命令造成影响 */
    private static String sanitize(String text) {
        // say 对双引号比较敏感，这里用全角引号替换，避免参数截断
        String t = text.replace('"', '“');
        // 去掉可能的换行，避免被当作多个参数
        t = t.replace("\r", " ").replace("\n", " ");
        return t.trim();
    }
}
