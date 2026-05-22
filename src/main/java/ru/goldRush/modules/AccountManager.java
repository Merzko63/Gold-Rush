package ru.goldRush.modules;

import org.bukkit.configuration.file.YamlConfiguration;
import ru.goldRush.GoldRush;
import java.io.File;
import java.util.UUID;

public class AccountManager {
    private final File file;
    private YamlConfiguration data;

    public AccountManager() {
        file = new File(GoldRush.getInstance().getDataFolder(), "players.yml");
        if (!file.exists()) GoldRush.getInstance().saveResource("players.yml", false);
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void registerPlayer(UUID uuid, String name, String ip, boolean premium) {
        String path = uuid.toString();
        data.set(path + ".name", name);
        data.set(path + ".ip", ip);
        data.set(path + ".premium", premium);
        save();
    }

    public boolean isPremium(UUID uuid) { return data.getBoolean(uuid + ".premium", false); }
    public String getLastIP(UUID uuid) { return data.getString(uuid + ".ip", "0.0.0.0"); }

    private void save() { try { data.save(file); } catch(Exception e) { e.printStackTrace(); } }
}