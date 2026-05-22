package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldRush.GoldRush;

import java.lang.reflect.Field;
import java.util.UUID;

public class Tab implements Listener {
    private final GoldRush plugin;

    public Tab(GoldRush plugin) {
        this.plugin = plugin;
        startUpdater();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayers();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void updateAllPlayers() {
        double tps = getTPS();
        int tpsInt = (int) Math.round(tps);
        String tpsStr = String.valueOf(tpsInt);

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayer(player, tpsStr);
        }
    }

    private void updatePlayer(Player player, String tpsStr) {
        int ping = getPing(player);

        String pingStr = (ping <= 1) ? "НИХУЯ СЕБЕ 1" : String.valueOf(ping);

        String pingColor = "§f";

        String tpsColor = "§f";

        String header = ChatColor.translateAlternateColorCodes('&',
                "\n&6&lGold Rush\n");

        String footer = ChatColor.translateAlternateColorCodes('&',
                "\n§x§D§0§0§0§0§0&lПинг§f: " + pingColor + pingStr + " §7мс §8| §x§D§0§0§0§0§0&lТпс§f: " + tpsColor + tpsStr +
                        "\n&7&ot.me/fallback_yt\n                        ");

        player.setPlayerListHeaderFooter(header, footer);
        updateDisplayName(player);
    }

    public void updateDisplayName(Player player) {
        String nameColor = getPlayerNameColor(player);
        String coloredName = nameColor + player.getName();
        String ratingStars = plugin.getRatingManager().getRatingStars(player.getUniqueId());
        String coloredNameWithStars = ratingStars + " " + coloredName;

        player.setDisplayName(coloredName);
        player.setCustomName(coloredName);
        player.setCustomNameVisible(true);
        player.setPlayerListName(coloredNameWithStars);
    }

    public String getPlayerNameColor(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isPremium = plugin.getDB().hasPremium(uuid);
        boolean isMod = plugin.getMods().isMod(player);
        String prefix = plugin.getDB().getPrefix(uuid);

        if (isMod) {
            return "§x§7§D§E§A§2§0§l";
        } else if (isPremium) {
            return "§x§F§F§1§2§1§2§l";
        } else if (prefix != null && !prefix.isEmpty()) {
            return ChatColor.translateAlternateColorCodes('&', prefix);
        } else {
            return "§7";
        }
    }

    private double getTPS() {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] tps = (double[]) server.getClass().getField("recentTps").get(server);
            return tps[0];
        } catch (Exception e) {
            return 20.0;
        }
    }

    private int getPing(Player player) {
        try {
            return player.getPing();
        } catch (Exception e) {
            return 0;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        updateDisplayName(p);
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateDisplayName(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        updateAllPlayers();
    }
}