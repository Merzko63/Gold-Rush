package ru.goldRush.modules;

import ru.goldRush.GoldRush;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerUtil {
    private final File logFile;

    public LoggerUtil() {
        File folder = GoldRush.getInstance().getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        logFile = new File(folder, "actions.log");
    }

    public void log(String message) {
        try (FileWriter fw = new FileWriter(logFile, true)) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + time + "] " + message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}