package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.goldRush.GoldRush;

public class EconomyCommand implements org.bukkit.command.CommandExecutor, Listener {
    public EconomyCommand() { Bukkit.getPluginManager().registerEvents(this, GoldRush.getInstance()); }

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;
        var eco = GoldRush.getInstance().getEco();

        if (c.getName().equalsIgnoreCase("balance") || c.getName().equalsIgnoreCase("bal")) {
            if (a.length == 0) p.sendMessage("§6Баланс: §e" + eco.get(p.getUniqueId()) + " §7монет");
            else {
                Player t = Bukkit.getPlayer(a[0]); if (t != null) p.sendMessage("§6Баланс " + t.getName() + ": §e" + eco.get(t.getUniqueId()));
            }
        }
        else if (c.getName().equalsIgnoreCase("pay")) {
            if (a.length != 2) p.sendMessage("§c/pay <ник> <сумма>");
            else {
                Player t = Bukkit.getPlayer(a[0]); if (t == null) p.sendMessage("§cИгрок не найден");
            else try {
                int amt = Integer.parseInt(a[1]); if (eco.take(p.getUniqueId(), amt)) { eco.add(t.getUniqueId(), amt);
                p.sendMessage("§aПереведено " + amt); t.sendMessage("§aВы получили " + amt + " от " + p.getName());
            }
                else p.sendMessage("§cНедостаточно"); } catch(Exception e) { p.sendMessage("§cНеверная сумма");
            }
            }
        }
        return true;
    }

    @EventHandler
    public void onVillagerClick(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof org.bukkit.entity.Villager)) return;
        e.setCancelled(true);
        Inventory inv = Bukkit.createInventory(null, 9, "§8Обмен у жителя");
        inv.setItem(4, new ItemStack(Material.DIAMOND));
        e.getPlayer().openInventory(inv);
    }

    @EventHandler
    public void onSell(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§8Обмен у жителя")) return;
        e.setCancelled(true);
        if (e.getSlot() == 4 && e.getInventory().getItem(0) != null) {
            ItemStack sell = e.getInventory().getItem(0);
            int price = sell.getAmount() * 2;
            GoldRush.getInstance().getEco().add(e.getWhoClicked().getUniqueId(), price);
            e.getInventory().setItem(0, null);
            e.getWhoClicked().sendMessage("§aПродано за " + price + " монет");
        }
    }
}