package ru.goldRush.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.goldRush.GoldRush;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionCommand implements org.bukkit.command.CommandExecutor, Listener {
    private final Map<Integer, AuctionItem> auctions = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerListings = new HashMap<>();
    private int nextId = 1;
    private final Set<UUID> viewing = new HashSet<>();
    private final File dataFile;
    private YamlConfiguration data;

    public AuctionCommand() {
        dataFile = new File(GoldRush.getInstance().getDataFolder(), "auction.yml");
        loadFromFile();
        Bukkit.getPluginManager().registerEvents(this, GoldRush.getInstance());
        startExpiryTask();
        startSaveTask();
    }

    private void loadFromFile() {
        if (!dataFile.exists()) {
            saveToFile();
            return;
        }

        data = YamlConfiguration.loadConfiguration(dataFile);

        if (data.contains("nextId")) {
            nextId = data.getInt("nextId", 1);
        }

        if (data.contains("auctions")) {
            for (String key : data.getConfigurationSection("auctions").getKeys(false)) {
                try {
                    int id = Integer.parseInt(key);
                    UUID seller = UUID.fromString(data.getString("auctions." + key + ".seller"));
                    ItemStack item = data.getItemStack("auctions." + key + ".item");
                    int price = data.getInt("auctions." + key + ".price");
                    long expiry = data.getLong("auctions." + key + ".expiry");

                    if (expiry < System.currentTimeMillis()) {
                        continue;
                    }

                    AuctionItem auctionItem = new AuctionItem(id, seller, item, price, expiry);
                    auctions.put(id, auctionItem);
                    playerListings.put(seller, id);

                    if (id >= nextId) {
                        nextId = id + 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        GoldRush.getInstance().getLogger().info("Загружено " + auctions.size() + " лотов с аукциона");
    }

    private void saveToFile() {
        data = new YamlConfiguration();

        data.set("nextId", nextId);

        for (Map.Entry<Integer, AuctionItem> entry : auctions.entrySet()) {
            int id = entry.getKey();
            AuctionItem item = entry.getValue();
            String path = "auctions." + id + ".";
            data.set(path + "seller", item.seller.toString());
            data.set(path + "item", item.item);
            data.set(path + "price", item.price);
            data.set(path + "expiry", item.expiry);
        }

        try {
            data.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSaveTask() {
        Bukkit.getScheduler().runTaskTimer(GoldRush.getInstance(), () -> {
            saveToFile();
        }, 0L, 1200L);
    }

    private void startExpiryTask() {
        Bukkit.getScheduler().runTaskTimer(GoldRush.getInstance(), () -> {
            boolean changed = false;
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<Integer, AuctionItem>> it = auctions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, AuctionItem> entry = it.next();
                if (entry.getValue().expiry < now) {
                    AuctionItem item = entry.getValue();
                    Player seller = Bukkit.getPlayer(item.seller);
                    if (seller != null && seller.isOnline()) {
                        seller.getInventory().addItem(item.item.clone());
                        seller.sendMessage(ChatColor.RED + "Ваш лот #" + entry.getKey() + " истёк, предмет возвращён!");
                    } else {
                        saveReturnItem(item.seller, item.item);
                    }
                    playerListings.remove(item.seller);
                    it.remove();
                    changed = true;
                }
            }
            if (changed) {
                saveToFile();
            }
        }, 0L, 1200L);
    }

    private void saveReturnItem(UUID seller, ItemStack item) {
        File returnFile = new File(GoldRush.getInstance().getDataFolder(), "auction_returns.yml");
        YamlConfiguration returnData;
        if (returnFile.exists()) {
            returnData = YamlConfiguration.loadConfiguration(returnFile);
        } else {
            returnData = new YamlConfiguration();
        }

        List<String> returns = returnData.getStringList(seller.toString());
        returns.add(serializeItem(item));
        returnData.set(seller.toString(), returns);

        try {
            returnData.save(returnFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String serializeItem(ItemStack item) {
        return item.getType().name() + ":" + item.getAmount() + ":" + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "");
    }

    public void giveReturnItems(Player p) {
        File returnFile = new File(GoldRush.getInstance().getDataFolder(), "auction_returns.yml");
        if (!returnFile.exists()) return;

        YamlConfiguration returnData = YamlConfiguration.loadConfiguration(returnFile);
        List<String> returns = returnData.getStringList(p.getUniqueId().toString());

        if (returns.isEmpty()) return;

        for (String itemStr : returns) {
            String[] parts = itemStr.split(":");
            Material mat = Material.getMaterial(parts[0]);
            if (mat != null) {
                ItemStack item = new ItemStack(mat, Integer.parseInt(parts[1]));
                p.getInventory().addItem(item);
            }
        }

        returnData.set(p.getUniqueId().toString(), null);
        try {
            returnData.save(returnFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        p.sendMessage(ChatColor.GREEN + "Вам возвращены предметы с истекших лотов аукциона!");
    }

    public boolean onCommand(org.bukkit.command.CommandSender s, org.bukkit.command.Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;

        if (a.length == 0) {
            openAuctionMenu(p);
            return true;
        }

        if (a[0].equalsIgnoreCase("sell")) {
            if (a.length != 2) {
                p.sendMessage(ChatColor.RED + "Использование: /auc sell <цена>");
                return true;
            }
            int price;
            try {
                price = Integer.parseInt(a[1]);
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                p.sendMessage(ChatColor.RED + "Цена должна быть положительным числом!");
                return true;
            }

            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                p.sendMessage(ChatColor.RED + "Держите предмет в руке!");
                return true;
            }

            if (playerListings.containsKey(p.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "У вас уже выставлен предмет! Сначала снимите его с продажи (/auc cancel)");
                return true;
            }

            AuctionItem item = new AuctionItem(nextId++, p.getUniqueId(), hand.clone(), price, System.currentTimeMillis() + 86400000);
            auctions.put(item.id, item);
            playerListings.put(p.getUniqueId(), item.id);
            p.getInventory().setItemInMainHand(null);
            p.sendMessage(ChatColor.GREEN + "Предмет выставлен на аукцион за " + price + " монет! ID: " + item.id);
            saveToFile();

        } else if (a[0].equalsIgnoreCase("buy")) {
            if (a.length != 2) {
                p.sendMessage(ChatColor.RED + "Использование: /auc buy <id>");
                return true;
            }
            int id;
            try {
                id = Integer.parseInt(a[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(ChatColor.RED + "Неверный ID!");
                return true;
            }

            AuctionItem item = auctions.get(id);
            if (item == null) {
                p.sendMessage(ChatColor.RED + "Лот не найден!");
                return true;
            }

            if (item.seller.equals(p.getUniqueId())) {
                p.sendMessage(ChatColor.RED + "Нельзя купить свой же предмет!");
                return true;
            }

            var eco = GoldRush.getInstance().getEco();
            if (!eco.take(p.getUniqueId(), item.price)) {
                p.sendMessage(ChatColor.RED + "Недостаточно монет!");
                return true;
            }

            eco.add(item.seller, item.price);
            p.getInventory().addItem(item.item.clone());
            auctions.remove(id);
            playerListings.remove(item.seller);
            p.sendMessage(ChatColor.GREEN + "Вы купили предмет за " + item.price + " монет!");
            saveToFile();

            Player seller = Bukkit.getPlayer(item.seller);
            if (seller != null && seller.isOnline()) {
                seller.sendMessage(ChatColor.GREEN + "Ваш предмет #" + id + " продан за " + item.price + " монет!");
            }

        } else if (a[0].equalsIgnoreCase("cancel")) {
            if (playerListings.containsKey(p.getUniqueId())) {
                int id = playerListings.get(p.getUniqueId());
                AuctionItem item = auctions.remove(id);
                if (item != null) {
                    p.getInventory().addItem(item.item.clone());
                    playerListings.remove(p.getUniqueId());
                    p.sendMessage(ChatColor.GREEN + "Вы сняли предмет с продажи!");
                    saveToFile();
                } else {
                    playerListings.remove(p.getUniqueId());
                    p.sendMessage(ChatColor.RED + "Лот не найден!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "У вас нет выставленных предметов!");
            }

        } else if (a[0].equalsIgnoreCase("list")) {
            if (auctions.isEmpty()) {
                p.sendMessage(ChatColor.GRAY + "Аукцион пуст!");
                return true;
            }
            p.sendMessage(ChatColor.GOLD + "=== Аукцион ===");
            for (AuctionItem item : auctions.values()) {
                Player seller = Bukkit.getPlayer(item.seller);
                String sellerName = seller != null ? seller.getName() : "Неизвестный";
                String itemName = item.item.hasItemMeta() && item.item.getItemMeta().hasDisplayName() ?
                        item.item.getItemMeta().getDisplayName() : item.item.getType().name().toLowerCase().replace("_", " ");
                p.sendMessage(ChatColor.YELLOW + "#" + item.id + " " + itemName + " §7- §e" + item.price + " мон. §7(продавец: " + sellerName + ")");
            }

        } else if (a[0].equalsIgnoreCase("claim")) {
            giveReturnItems(p);

        } else {
            openAuctionMenu(p);
        }
        return true;
    }

    private void openAuctionMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Аукцион");
        for (AuctionItem item : auctions.values()) {
            ItemStack display = item.item.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "Цена: " + ChatColor.GOLD + item.price + " монет");
            lore.add(ChatColor.YELLOW + "ID: " + ChatColor.WHITE + item.id);
            lore.add("");
            lore.add(ChatColor.GREEN + "Нажмите для покупки!");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.addItem(display);
        }
        p.openInventory(inv);
        viewing.add(p.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(ChatColor.GOLD + "Аукцион")) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        int slot = e.getSlot();
        AuctionItem target = null;
        int idx = 0;
        for (AuctionItem item : auctions.values()) {
            if (idx == slot) {
                target = item;
                break;
            }
            idx++;
        }
        if (target == null) return;

        if (target.seller.equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "Нельзя купить свой предмет!");
            return;
        }

        p.closeInventory();
        viewing.remove(p.getUniqueId());

        var eco = GoldRush.getInstance().getEco();
        if (!eco.take(p.getUniqueId(), target.price)) {
            p.sendMessage(ChatColor.RED + "Недостаточно монет!");
            return;
        }

        eco.add(target.seller, target.price);
        p.getInventory().addItem(target.item.clone());
        auctions.remove(target.id);
        playerListings.remove(target.seller);
        saveToFile();
        p.sendMessage(ChatColor.GREEN + "Вы купили предмет за " + target.price + " монет!");

        Player seller = Bukkit.getPlayer(target.seller);
        if (seller != null && seller.isOnline()) {
            seller.sendMessage(ChatColor.GREEN + "Ваш предмет #" + target.id + " продан за " + target.price + " монет!");
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        viewing.remove(e.getPlayer().getUniqueId());
    }

    static class AuctionItem {
        int id;
        UUID seller;
        ItemStack item;
        int price;
        long expiry;
        AuctionItem(int id, UUID seller, ItemStack item, int price, long expiry) {
            this.id = id;
            this.seller = seller;
            this.item = item;
            this.price = price;
            this.expiry = expiry;
        }
    }
}