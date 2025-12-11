package util;

import model.BirdSkin;

import java.io.*;
import java.util.Properties;
import java.util.List;

public class SaveManager {
    private static final String SAVE_FILE = "save.properties";

    public static class SaveData {
        public int money;
        public String unlockedCSV;
        public String currentSkinName;
        public int highScore = 0;
    }

    public static SaveData load() {
        SaveData data = new SaveData();
        data.money = 0;
        data.unlockedCSV = "";
        data.currentSkinName = "Normal";

        Properties props = new Properties();
        File f = new File(SAVE_FILE);
        if (!f.exists()) return data;
        try (FileInputStream in = new FileInputStream(f)) {
            props.load(in);
            data.money = Integer.parseInt(props.getProperty("money", "0"));
            data.unlockedCSV = props.getProperty("unlocked", "");
            data.currentSkinName = props.getProperty("current", "Normal");
        } catch (Exception e) {
            // ignore
        }
        return data;
    }

    public static void save(int money, List<BirdSkin> skins, String currentSkinName, int highScore) {
        Properties props = new Properties();
        props.setProperty("money", String.valueOf(money));
        props.setProperty("highScore", String.valueOf(highScore));
        StringBuilder sb = new StringBuilder();
        for (BirdSkin s : skins) {
            if (s.isUnlocked()) {
                if (sb.length() > 0) sb.append(',');
                sb.append(s.getName());
            }
        }
        props.setProperty("unlocked", sb.toString());
        props.setProperty("current", currentSkinName == null ? "Normal" : currentSkinName);


        try (FileOutputStream out = new FileOutputStream(new File(SAVE_FILE))) {
            props.store(out, "Flappy Bird Save Data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
