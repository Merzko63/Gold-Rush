package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldRush.GoldRush;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class JailManager {
    private final GoldRush plugin;
    private Location jailLocation;
    private final HashMap<UUID, Location> savedLocations = new HashMap<>();
    private final HashMap<UUID, GameMode> savedGameModes = new HashMap<>();
    private final HashMap<UUID, Long> jailTimes = new HashMap<>();
    private final HashMap<UUID, String> jailReasons = new HashMap<>();
    private File logFile;

    public JailManager(GoldRush plugin) {
        this.plugin = plugin;
        this.setupLog();
        this.loadJail();
        this.startJailCheckTask();
    }

    private void setupLog() {
        this.logFile = new File(plugin.getDataFolder(), "jail_log.txt");
        if (!this.logFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                this.logFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getTime() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
    }

    private void writeLog(String message) {
        try (FileWriter fw = new FileWriter(this.logFile, true)) {
            fw.write("[" + this.getTime() + "] " + message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadJail() {
        if (plugin.getConfig().contains("jail.location")) {
            World world = Bukkit.getWorld(plugin.getConfig().getString("jail.location.world"));
            if (world != null) {
                double x = plugin.getConfig().getDouble("jail.location.x");
                double y = plugin.getConfig().getDouble("jail.location.y");
                double z = plugin.getConfig().getDouble("jail.location.z");
                this.jailLocation = new Location(world, x, y, z);
            }
        }
    }

    public void setJailLocation(Location loc) {
        this.jailLocation = loc;
        plugin.getConfig().set("jail.location.world", loc.getWorld().getName());
        plugin.getConfig().set("jail.location.x", loc.getX());
        plugin.getConfig().set("jail.location.y", loc.getY());
        plugin.getConfig().set("jail.location.z", loc.getZ());
        plugin.saveConfig();
    }

    private void startJailCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new HashMap<>(jailTimes).keySet()) {
                    Long releaseTime = jailTimes.get(uuid);
                    if (releaseTime != null && releaseTime > 0 && System.currentTimeMillis() >= releaseTime) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            unjailPlayer(player);
                        } else {
                            jailTimes.remove(uuid);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void jailPlayer(Player target, Player sender, int minutes, String reason) {
        if (jailLocation == null) {
            sender.sendMessage(ChatColor.RED + "Тюрьма не установлена! Используйте /setjail");
            return;
        }

        UUID uuid = target.getUniqueId();

        // Сохраняем текущее состояние
        savedLocations.put(uuid, target.getLocation());
        savedGameModes.put(uuid, target.getGameMode());

        // Устанавливаем время освобождения (0 = пока не разбанят)
        long releaseTime = minutes > 0 ? System.currentTimeMillis() + (minutes * 60L * 1000L) : 0;
        jailTimes.put(uuid, releaseTime);
        jailReasons.put(uuid, reason);

        // Телепортируем в тюрьму
        target.teleport(jailLocation);
        target.setGameMode(GameMode.ADVENTURE);

        // Очищаем инвентарь (опционально)
        // target.getInventory().clear();

        String durationMsg = minutes > 0 ? minutes + " мин." : "навсегда";
        target.sendMessage(ChatColor.RED + "§l§k||§r §c§lВЫ В ТЮРЬМЕ§r §l§k||" + ChatColor.RESET);
        target.sendMessage(ChatColor.GRAY + "Причина: " + ChatColor.WHITE + reason);
        target.sendMessage(ChatColor.GRAY + "Срок: " + ChatColor.WHITE + durationMsg);
        target.sendMessage(ChatColor.GRAY + "Выпустит: " + ChatColor.WHITE + (minutes > 0 ? "Через " + minutes + " мин." : "Только админ"));

        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " посажен в тюрьму на " + durationMsg);

        writeLog("JAIL " + target.getName() + " by " + sender.getName() + " | Причина: " + reason + " | Срок: " + durationMsg);
    }

    public void unjailPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        // Возвращаем на старое место
        if (savedLocations.containsKey(uuid)) {
            player.teleport(savedLocations.get(uuid));
            savedLocations.remove(uuid);
        }

        // Возвращаем игровой режим
        if (savedGameModes.containsKey(uuid)) {
            player.setGameMode(savedGameModes.get(uuid));
            savedGameModes.remove(uuid);
        }

        // Удаляем из тюремного списка
        jailTimes.remove(uuid);
        String reason = jailReasons.remove(uuid);

        player.sendMessage(ChatColor.GREEN + "§lВы освобождены из тюрьмы!");

        writeLog("UNJAIL " + player.getName() + " | Причина была: " + (reason != null ? reason : "неизвестна"));
    }

    public boolean isJailed(UUID uuid) {
        return jailTimes.containsKey(uuid);
    }

    public long getRemainingTime(UUID uuid) {
        Long releaseTime = jailTimes.get(uuid);
        if (releaseTime == null) return 0;
        if (releaseTime == 0) return -1; // навсегда
        long remaining = releaseTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 / 60 : 0;
    }

    public String getJailReason(UUID uuid) {
        return jailReasons.getOrDefault(uuid, "Не указана");
    }

    public Location getJailLocation() {
        return jailLocation;
    }

    public void teleportToJail(Player player) {
        if (jailLocation != null) {
            player.teleport(jailLocation);
        }
    }
}