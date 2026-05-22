package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.goldRush.GoldRush;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModeratorManager implements Listener {
    private final GoldRush plugin;
    private final Set<UUID> moderators = new HashSet<>();
    private final Set<UUID> adminMode = new HashSet<>();
    private final Map<UUID, String> originalNames = new HashMap<>();
    private File logFile;

    public ModeratorManager(GoldRush plugin) {
        this.plugin = plugin;
        setupLog();
        loadModerators();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void setupLog() {
        logFile = new File(plugin.getDataFolder(), "moderator_log.txt");
        if (!logFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeLog(String message) {
        try (FileWriter fw = new FileWriter(logFile, true)) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write("[" + time + "] " + message + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadModerators() {
        if (plugin.getConfig().contains("moderators")) {
            for (String uuidStr : plugin.getConfig().getStringList("moderators")) {
                moderators.add(UUID.fromString(uuidStr));
            }
        }
    }

    private void saveModerators() {
        List<String> list = new ArrayList<>();
        for (UUID uuid : moderators) {
            list.add(uuid.toString());
        }
        plugin.getConfig().set("moderators", list);
        plugin.saveConfig();
    }

    public void addModerator(Player target) {
        UUID uuid = target.getUniqueId();
        moderators.add(uuid);
        saveModerators();
        writeLog("Модератор добавлен: " + target.getName() + " (" + uuid + ")");
        target.sendMessage(ChatColor.GREEN + "§lВы назначены модератором!");
        Bukkit.broadcast(ChatColor.GOLD + "★ " + target.getName() + " назначен модератором!", "goldrush.notify");
    }

    public void removeModerator(Player target) {
        UUID uuid = target.getUniqueId();
        moderators.remove(uuid);
        adminMode.remove(uuid);
        saveModerators();
        writeLog("Модератор удалён: " + target.getName() + " (" + uuid + ")");
        target.sendMessage(ChatColor.RED + "§lВы больше не модератор!");
        Bukkit.broadcast(ChatColor.GOLD + "★ " + target.getName() + " больше не модератор.", "goldrush.notify");
    }

    public boolean isModerator(Player player) {
        return moderators.contains(player.getUniqueId()) || player.isOp();
    }

    public boolean isInAdminMode(Player player) {
        return adminMode.contains(player.getUniqueId());
    }

    public boolean isModerator(CommandSender sender) {
        if (sender.isOp()) return true;
        if (!(sender instanceof Player)) return true;
        return isModerator((Player) sender);
    }

    public void toggleAdminMode(Player player) {
        UUID uuid = player.getUniqueId();

        if (!isModerator(player)) {
            player.sendMessage(ChatColor.RED + "Вы не модератор!");
            return;
        }

        if (adminMode.contains(uuid)) {
            adminMode.remove(uuid);
            originalNames.remove(uuid);

            // Восстанавливаем ник
            player.setDisplayName(originalNames.getOrDefault(uuid, player.getName()));
            player.setPlayerListName(originalNames.getOrDefault(uuid, player.getName()));

            player.sendMessage(ChatColor.RED + "§lАдмин-режим ВЫКЛЮЧЕН");
            player.sendMessage(ChatColor.GRAY + "Вы снова видите всех игроков");
            writeLog(player.getName() + " выключил админ-режим");
        } else {
            adminMode.add(uuid);
            originalNames.put(uuid, player.getDisplayName());

            // Меняем ник
            String adminName = ChatColor.DARK_RED + "[ADMIN] " + ChatColor.RED + player.getName();
            player.setDisplayName(adminName);
            player.setPlayerListName(adminName);

            player.sendMessage(ChatColor.GREEN + "§lАдмин-режим ВКЛЮЧЕН");
            player.sendMessage(ChatColor.GRAY + "Вы невидимы для обычных игроков");
            writeLog(player.getName() + " включил админ-режим");

            // Прячем от обычных игроков
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!isModerator(p)) {
                    p.hidePlayer(plugin, player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Скрываем модераторов в админ-режиме от обычных игроков
        for (Player mod : Bukkit.getOnlinePlayers()) {
            if (isInAdminMode(mod) && !isModerator(p)) {
                p.hidePlayer(plugin, mod);
            }
        }

        // Если зашёл модератор, показываем ему скрытых
        if (isModerator(p)) {
            for (Player mod : Bukkit.getOnlinePlayers()) {
                if (isInAdminMode(mod)) {
                    p.showPlayer(plugin, mod);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        adminMode.remove(uuid);
        originalNames.remove(uuid);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String cmd = e.getMessage().toLowerCase();

        // Блокировка команд для обычных игроков, если модератор в админ-режиме
        if (!isModerator(p)) {
            for (Player mod : Bukkit.getOnlinePlayers()) {
                if (isInAdminMode(mod) && cmd.contains(mod.getName().toLowerCase())) {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.RED + "Игрок не найден!");
                    return;
                }
            }
        }
    }

    public List<String> getModeratorList() {
        List<String> list = new ArrayList<>();
        for (UUID uuid : moderators) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                list.add(p.getName() + " §7(онлайн)");
            } else {
                list.add(uuid.toString().substring(0, 8) + " §7(оффлайн)");
            }
        }
        return list;
    }
}