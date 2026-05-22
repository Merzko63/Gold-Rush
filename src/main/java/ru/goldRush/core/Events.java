package ru.goldRush.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.goldRush.GoldRush;
import ru.goldRush.commands.AuctionCommand;

public class Events implements Listener {

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            GoldRush.getInstance().getQuests().handle(e.getEntity().getKiller(), "kill", e.getEntity().getType().name(), 1);
            GoldRush.getInstance().getBattlePass().handle(e.getEntity().getKiller(), "kill", e.getEntity().getType().name(), 1);
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent e) {
        GoldRush.getInstance().getQuests().handle(e.getPlayer(), "mine", e.getBlock().getType().name(), 1);
        GoldRush.getInstance().getBattlePass().handle(e.getPlayer(), "mine", e.getBlock().getType().name(), 1);
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        if (e.getCaught() != null) {
            GoldRush.getInstance().getQuests().handle(e.getPlayer(), "fish", "ANY", 1);
            GoldRush.getInstance().getBattlePass().handle(e.getPlayer(), "fish", "ANY", 1);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().distance(e.getTo()) > 0) {
            int distance = (int) e.getFrom().distance(e.getTo());
            GoldRush.getInstance().getQuests().handle(e.getPlayer(), "walk", "ANY", distance);
            GoldRush.getInstance().getBattlePass().handle(e.getPlayer(), "walk", "ANY", distance);
        }
        if (GoldRush.getInstance().getJail().isJailed(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(org.bukkit.event.inventory.CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (e.getRecipe().getResult() != null) {
                GoldRush.getInstance().getBattlePass().handle(p, "craft", e.getRecipe().getResult().getType().name(), 1);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(GoldRush.getInstance(), () -> {
            AuctionCommand auctionCommand = new AuctionCommand();
            auctionCommand.giveReturnItems(p);
        }, 20L);
    }
}