package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldRush.GoldRush;
import ru.goldRush.core.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager implements Listener {
    private final GoldRush plugin;
    private final Database database;
    private final Map<UUID, Boolean> authenticated = new HashMap<>();
    private final Map<UUID, Location> freezeLocations = new HashMap<>();
    private final Map<UUID, GameMode> savedGameModes = new HashMap<>();
    private final Map<UUID, Integer> loginAttempts = new HashMap<>();
    private final Map<UUID, String> lastKnownIp = new HashMap<>();

    public AuthManager(GoldRush plugin) {
        this.plugin = plugin;
        this.database = plugin.getDB();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startUnfreezeTask();
    }

    private void startUnfreezeTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!isAuthenticated(p) && freezeLocations.containsKey(p.getUniqueId())) {
                        Location freezeLoc = freezeLocations.get(p.getUniqueId());
                        if (p.getLocation().distance(freezeLoc) > 0.5) {
                            p.teleport(freezeLoc);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String currentIp = p.getAddress().getAddress().getHostAddress();

        if (!database.exists(uuid)) {
            p.sendMessage(ChatColor.GOLD + "=== РЕГИСТРАЦИЯ ===");
            p.sendMessage(ChatColor.YELLOW + "Введите: §e/register <пароль>");
            p.sendMessage(ChatColor.GRAY + "Пароль должен содержать минимум 4 символа");
            freezePlayer(p);
        } else {
            String lastIp = database.getIp(p.getName());
            lastKnownIp.put(uuid, lastIp);

            if (lastIp != null && !lastIp.equals(currentIp)) {
                p.sendMessage(ChatColor.GOLD + "=== АВТОРИЗАЦИЯ ===");
                p.sendMessage(ChatColor.RED + "⚠ ВНИМАНИЕ! Изменился IP адрес!");
                p.sendMessage(ChatColor.YELLOW + "Старый IP: §7" + lastIp);
                p.sendMessage(ChatColor.YELLOW + "Новый IP: §7" + currentIp);
                p.sendMessage(ChatColor.YELLOW + "Введите: §e/login <пароль>");
                p.sendMessage(ChatColor.GRAY + "У вас 3 попытки");
                freezePlayer(p);
            } else {
                authenticated.put(uuid, true);
                unfreezePlayer(p);
                database.updateIp(uuid, currentIp, p.getName());
                p.sendMessage(ChatColor.GREEN + "§lДобро пожаловать обратно! §7(Автоматический вход)");
                plugin.getTab().updateDisplayName(p);
            }
        }

        database.exec("INSERT INTO ip_history VALUES (NULL, '" + currentIp + "', '" + uuid + "', '" + p.getName() + "', " + System.currentTimeMillis() + ")");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        authenticated.remove(uuid);
        freezeLocations.remove(uuid);
        savedGameModes.remove(uuid);
        loginAttempts.remove(uuid);
        lastKnownIp.remove(uuid);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!isAuthenticated(e.getPlayer())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "Вы не авторизованы! Используйте /register или /login");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!isAuthenticated(e.getPlayer())) {
            Location from = e.getFrom();
            Location to = e.getTo();
            if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!isAuthenticated(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String cmd = e.getMessage().toLowerCase();

        if (!isAuthenticated(p)) {
            if (!cmd.startsWith("/register") && !cmd.startsWith("/login") &&
                    !cmd.startsWith("/reg") && !cmd.startsWith("/l") &&
                    !cmd.startsWith("/changepassword") && !cmd.startsWith("/chpass")) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "Вы не авторизованы! Используйте /register или /login");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (!isAuthenticated(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    public void register(Player p, String password) {
        UUID uuid = p.getUniqueId();
        String ip = p.getAddress().getAddress().getHostAddress();

        if (password.length() < 4) {
            p.sendMessage(ChatColor.RED + "Пароль должен быть минимум 4 символа!");
            return;
        }

        if (database.exists(uuid)) {
            p.sendMessage(ChatColor.RED + "Вы уже зарегистрированы! Используйте /login");
            return;
        }

        database.register(uuid, p.getName(), password, ip);
        authenticated.put(uuid, true);
        unfreezePlayer(p);
        p.sendMessage(ChatColor.GREEN + "§lРегистрация успешна!");
        p.sendMessage(ChatColor.GRAY + "Добро пожаловать на сервер!");
        plugin.getTab().updateDisplayName(p);
    }

    public void login(Player p, String password) {
        UUID uuid = p.getUniqueId();
        String currentIp = p.getAddress().getAddress().getHostAddress();

        if (!database.exists(uuid)) {
            p.sendMessage(ChatColor.RED + "Вы не зарегистрированы! Используйте /register");
            return;
        }

        int attempts = loginAttempts.getOrDefault(uuid, 0);
        if (attempts >= 3) {
            p.kickPlayer(ChatColor.RED + "Слишком много неудачных попыток входа!\nПодождите и попробуйте снова.");
            return;
        }

        if (database.pass(uuid, password)) {
            authenticated.put(uuid, true);
            loginAttempts.remove(uuid);
            database.updateIp(uuid, currentIp, p.getName());
            unfreezePlayer(p);
            p.sendMessage(ChatColor.GREEN + "§lУспешный вход!");
            p.sendMessage(ChatColor.GRAY + "С возвращением!");
            plugin.getTab().updateDisplayName(p);
        } else {
            loginAttempts.put(uuid, attempts + 1);
            int remaining = 3 - (attempts + 1);
            p.sendMessage(ChatColor.RED + "Неверный пароль! Осталось попыток: " + remaining);
        }
    }

    public void changePassword(Player p, String oldPassword, String newPassword) {
        UUID uuid = p.getUniqueId();

        if (!isAuthenticated(p)) {
            p.sendMessage(ChatColor.RED + "Вы не авторизованы!");
            return;
        }

        if (!database.pass(uuid, oldPassword)) {
            p.sendMessage(ChatColor.RED + "Неверный старый пароль!");
            return;
        }

        if (newPassword.length() < 4) {
            p.sendMessage(ChatColor.RED + "Новый пароль должен быть минимум 4 символа!");
            return;
        }

        database.exec("UPDATE users SET pass='" + newPassword + "' WHERE uuid='" + uuid + "'");
        p.sendMessage(ChatColor.GREEN + "Пароль успешно изменён!");
    }

    private void freezePlayer(Player p) {
        freezeLocations.put(p.getUniqueId(), p.getLocation());
        savedGameModes.put(p.getUniqueId(), p.getGameMode());
        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(true);
        p.setFlying(true);
    }

    private void unfreezePlayer(Player p) {
        UUID uuid = p.getUniqueId();
        freezeLocations.remove(uuid);
        if (savedGameModes.containsKey(uuid)) {
            p.setGameMode(savedGameModes.get(uuid));
            savedGameModes.remove(uuid);
        }
        p.setAllowFlight(false);
        p.setFlying(false);
    }

    public boolean isAuthenticated(Player p) {
        return authenticated.getOrDefault(p.getUniqueId(), false);
    }
}