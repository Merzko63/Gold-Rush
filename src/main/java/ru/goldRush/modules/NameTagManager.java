package ru.goldRush.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.goldRush.GoldRush;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NameTagManager implements Listener {
    private final GoldRush plugin;
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private Scoreboard globalScoreboard;

    public NameTagManager(GoldRush plugin) {
        this.plugin = plugin;

        if (Bukkit.getScoreboardManager().getMainScoreboard() != null) {
            this.globalScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        } else {
            this.globalScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        startUpdater();
    }

    private void startUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateNameTag(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void updateNameTag(Player player) {
        String nameColor = getPlayerNameColor(player);
        String ratingStars = plugin.getRatingManager().getRatingStars(player.getUniqueId());

        String coloredNameOnly = nameColor + player.getName();
        String coloredNameWithStars = ratingStars + " " + coloredNameOnly;

        player.setDisplayName(coloredNameOnly);
        player.setPlayerListName(coloredNameWithStars);

        updatePlayerTeam(player, nameColor);
        updateNameTagAboveHead(player);
    }

    private void updateNameTagAboveHead(Player player) {
        try {
            String teamName = "nt_" + player.getUniqueId().toString().substring(0, 8);

            Team team = globalScoreboard.getTeam(teamName);
            if (team == null) {
                team = globalScoreboard.registerNewTeam(teamName);
            }

            String prefix = getPrefixForPlayer(player);
            team.setPrefix(prefix);

            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.setScoreboard(globalScoreboard);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPrefixForPlayer(Player player) {
        boolean isMod = plugin.getMods().isMod(player);
        boolean isPremium = plugin.getDB().hasPremium(player.getUniqueId());
        String prefix = plugin.getDB().getPrefix(player.getUniqueId());

        if (isMod) {
            return "§x§7§D§E§A§2§0";
        } else if (isPremium) {
            return "§x§F§F§1§2§1§2";
        } else if (prefix != null && !prefix.isEmpty() && !prefix.equals("§7")) {
            // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: конвертируем & в §
            return ChatColor.translateAlternateColorCodes('&', prefix);
        } else {
            return "§7";
        }
    }

    private void updatePlayerTeam(Player player, String colorCode) {
        String teamName = "team_" + player.getUniqueId().toString().substring(0, 8);

        Team oldTeam = globalScoreboard.getTeam(teamName);
        if (oldTeam != null) {
            oldTeam.unregister();
        }

        Team team = globalScoreboard.registerNewTeam(teamName);
        team.setPrefix(colorCode);
        team.addEntry(player.getName());

        playerTeams.put(player.getUniqueId(), teamName);
    }

    public String getPlayerNameColor(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isPremium = plugin.getDB().hasPremium(uuid);
        boolean isMod = plugin.getMods().isMod(player);
        String prefix = plugin.getDB().getPrefix(uuid);

        if (isMod) {
            return "§x§7§D§E§A§2§0";
        } else if (isPremium) {
            return "§x§F§F§1§2§1§2";
        } else if (prefix != null && !prefix.isEmpty() && !prefix.equals("§7")) {
            // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: конвертируем & в §
            return ChatColor.translateAlternateColorCodes('&', prefix);
        } else {
            return "§7";
        }
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateNameTag(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setScoreboard(globalScoreboard);
        updateNameTag(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        String teamName = playerTeams.remove(e.getPlayer().getUniqueId());
        if (teamName != null) {
            Team team = globalScoreboard.getTeam(teamName);
            if (team != null) {
                team.unregister();
            }
        }
    }
}