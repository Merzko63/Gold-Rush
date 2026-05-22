package ru.goldRush.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.goldRush.GoldRush;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BattlePassManager {
    private final GoldRush plugin;
    private final Map<UUID, BattlePassData> playerData = new ConcurrentHashMap<>();

    private final List<Quest> allQuests = new ArrayList<>();
    private final Map<Integer, Quest> questById = new HashMap<>();
    private final Map<Integer, LevelReward> levelRewards = new HashMap<>();

    public BattlePassManager(GoldRush plugin) {
        this.plugin = plugin;
        initTables();
        initQuests();
        initLevelRewards();
        loadAllData();
        startSaveTask();
    }

    private void initTables() {
        plugin.getDB().exec("CREATE TABLE IF NOT EXISTS battlepass (" +
                "uuid TEXT PRIMARY KEY, " +
                "level INTEGER DEFAULT 1, " +
                "current_quest_id INTEGER DEFAULT 0, " +
                "current_progress INTEGER DEFAULT 0, " +
                "completed_quests TEXT DEFAULT '', " +
                "claimed_rewards TEXT DEFAULT '')");
    }

    private void initQuests() {
        allQuests.add(new Quest(1, "kill", "ZOMBIE", 5, 10, "Ubij zomby"));
        allQuests.add(new Quest(2, "kill", "SKELETON", 5, 10, "Ubij skeletonov"));
        allQuests.add(new Quest(3, "kill", "SPIDER", 5, 10, "Ubij paukov"));
        allQuests.add(new Quest(4, "mine", "COAL_ORE", 20, 10, "Dobyt uglya"));
        allQuests.add(new Quest(5, "mine", "COBBLESTONE", 64, 10, "Dobyt bulizhnika"));
        allQuests.add(new Quest(6, "break", "OAK_LOG", 20, 10, "Slomat duba"));
        allQuests.add(new Quest(7, "break", "OAK_LEAVES", 30, 10, "Slomat listvy"));
        allQuests.add(new Quest(8, "kill", "ZOMBIE", 10, 10, "Ubij zomby"));
        allQuests.add(new Quest(9, "kill", "SKELETON", 10, 10, "Ubij skeletonov"));
        allQuests.add(new Quest(10, "craft", "WOODEN_PICKAXE", 3, 15, "Skraftit derevyannyh kirok"));

        allQuests.add(new Quest(11, "mine", "IRON_ORE", 15, 15, "Dobyt zheleznoj rudy"));
        allQuests.add(new Quest(12, "kill", "CREEPER", 8, 15, "Ubij kriperov"));
        allQuests.add(new Quest(13, "kill", "WITCH", 3, 15, "Ubij vedm"));
        allQuests.add(new Quest(14, "break", "BIRCH_LOG", 30, 15, "Slomat berezy"));
        allQuests.add(new Quest(15, "craft", "IRON_PICKAXE", 3, 15, "Skraftit zheleznyh kirok"));
        allQuests.add(new Quest(16, "mine", "GOLD_ORE", 10, 15, "Dobyt zolotoj rudy"));
        allQuests.add(new Quest(17, "kill", "ENDERMAN", 5, 20, "Ubij endermenov"));
        allQuests.add(new Quest(18, "fish", "ANY", 15, 15, "Pojmat ryby"));
        allQuests.add(new Quest(19, "kill", "SPIDER", 15, 15, "Ubij paukov"));
        allQuests.add(new Quest(20, "mine", "REDSTONE_ORE", 20, 15, "Dobyt redstouna"));

        allQuests.add(new Quest(21, "kill", "BLAZE", 5, 20, "Ubij ifritov"));
        allQuests.add(new Quest(22, "mine", "DIAMOND_ORE", 5, 25, "Dobyt almazov"));
        allQuests.add(new Quest(23, "kill", "WITHER_SKELETON", 10, 25, "Ubij skeletonov-issushitelej"));
        allQuests.add(new Quest(24, "break", "DARK_OAK_LOG", 40, 20, "Slomat temnogo duba"));
        allQuests.add(new Quest(25, "craft", "DIAMOND_PICKAXE", 2, 25, "Skraftit almaznyh kirok"));
        allQuests.add(new Quest(26, "kill", "PIGLIN_BRUTE", 5, 25, "Ubij piglinov-grubiyanov"));
        allQuests.add(new Quest(27, "mine", "NETHER_QUARTZ_ORE", 30, 20, "Dobyt kvarca"));
        allQuests.add(new Quest(28, "kill", "GHAST", 4, 25, "Ubij gastov"));
        allQuests.add(new Quest(29, "break", "ACACIA_LOG", 35, 20, "Slomat akacii"));
        allQuests.add(new Quest(30, "mine", "LAPIS_ORE", 25, 20, "Dobyt lazurita"));

        allQuests.add(new Quest(31, "kill", "HOGLIN", 15, 25, "Ubij hoglinov"));
        allQuests.add(new Quest(32, "mine", "ANCIENT_DEBRIS", 3, 35, "Dobyt drevnego oblonka"));
        allQuests.add(new Quest(33, "kill", "WARDEN", 1, 40, "Ubij vardena"));
        allQuests.add(new Quest(34, "break", "JUNGLE_LOG", 50, 25, "Slomat tropicheskogo dereva"));
        allQuests.add(new Quest(35, "craft", "NETHERITE_PICKAXE", 1, 40, "Skraftit nezeritovuyu kirku"));
        allQuests.add(new Quest(36, "kill", "RAVAGER", 3, 35, "Ubij razoritelej"));
        allQuests.add(new Quest(37, "mine", "EMERALD_ORE", 8, 30, "Dobyt izumrudov"));
        allQuests.add(new Quest(38, "kill", "EVOKER", 5, 35, "Ubij zaklinatelej"));
        allQuests.add(new Quest(39, "break", "CRIMSON_STEM", 40, 25, "Slomat bagrovogo steblya"));
        allQuests.add(new Quest(40, "mine", "NETHER_GOLD_ORE", 40, 25, "Dobyt zolota v adu"));

        allQuests.add(new Quest(41, "kill", "ENDER_DRAGON", 1, 50, "Ubij Ender-drakona"));
        allQuests.add(new Quest(42, "mine", "DIAMOND_ORE", 15, 35, "Dobyt almazov"));
        allQuests.add(new Quest(43, "kill", "WITHER", 1, 50, "Ubij Vizera"));
        allQuests.add(new Quest(44, "break", "WARPED_STEM", 45, 30, "Slomat iskazhjonnogo steblya"));
        allQuests.add(new Quest(45, "craft", "ENCHANTING_TABLE", 3, 30, "Skraftit stolov zacharovaniya"));
        allQuests.add(new Quest(46, "kill", "PIGLIN_BRUTE", 15, 35, "Ubij piglinov-grubiyanov"));
        allQuests.add(new Quest(47, "mine", "ANCIENT_DEBRIS", 8, 45, "Dobyt drevnego oblonka"));
        allQuests.add(new Quest(48, "kill", "WARDEN", 2, 50, "Ubij vardenov"));
        allQuests.add(new Quest(49, "break", "MUSHROOM_STEM", 60, 35, "Slomat gribnogo steblya"));
        allQuests.add(new Quest(50, "kill", "ENDER_DRAGON", 2, 100, "Ubij Ender-drakonov"));

        for (Quest q : allQuests) {
            questById.put(q.id, q);
        }
    }

    private void initLevelRewards() {
        levelRewards.put(1, new LevelReward(1, 50, Material.IRON_INGOT, 5));
        levelRewards.put(2, new LevelReward(2, 60, Material.GOLD_INGOT, 3));
        levelRewards.put(3, new LevelReward(3, 70, Material.IRON_INGOT, 8));
        levelRewards.put(4, new LevelReward(4, 80, Material.LAPIS_LAZULI, 16));
        levelRewards.put(5, new LevelReward(5, 100, Material.DIAMOND, 1));
        levelRewards.put(6, new LevelReward(6, 110, Material.GOLD_INGOT, 5));
        levelRewards.put(7, new LevelReward(7, 120, Material.IRON_INGOT, 12));
        levelRewards.put(8, new LevelReward(8, 130, Material.REDSTONE, 20));
        levelRewards.put(9, new LevelReward(9, 140, Material.EMERALD, 2));
        levelRewards.put(10, new LevelReward(10, 200, Material.DIAMOND, 2));

        levelRewards.put(11, new LevelReward(11, 150, Material.IRON_INGOT, 15));
        levelRewards.put(12, new LevelReward(12, 160, Material.GOLD_INGOT, 8));
        levelRewards.put(13, new LevelReward(13, 170, Material.DIAMOND, 2));
        levelRewards.put(14, new LevelReward(14, 180, Material.EMERALD, 4));
        levelRewards.put(15, new LevelReward(15, 250, Material.NETHERITE_SCRAP, 2));
        levelRewards.put(16, new LevelReward(16, 190, Material.GOLD_INGOT, 10));
        levelRewards.put(17, new LevelReward(17, 200, Material.DIAMOND, 3));
        levelRewards.put(18, new LevelReward(18, 210, Material.IRON_INGOT, 20));
        levelRewards.put(19, new LevelReward(19, 220, Material.EMERALD, 6));
        levelRewards.put(20, new LevelReward(20, 350, Material.DIAMOND, 4));

        levelRewards.put(21, new LevelReward(21, 250, Material.NETHERITE_SCRAP, 3));
        levelRewards.put(22, new LevelReward(22, 260, Material.GOLD_INGOT, 15));
        levelRewards.put(23, new LevelReward(23, 270, Material.DIAMOND, 4));
        levelRewards.put(24, new LevelReward(24, 280, Material.EMERALD, 8));
        levelRewards.put(25, new LevelReward(25, 400, Material.NETHERITE_INGOT, 1));
        levelRewards.put(26, new LevelReward(26, 290, Material.IRON_INGOT, 25));
        levelRewards.put(27, new LevelReward(27, 300, Material.DIAMOND, 5));
        levelRewards.put(28, new LevelReward(28, 310, Material.GOLD_INGOT, 20));
        levelRewards.put(29, new LevelReward(29, 320, Material.NETHERITE_SCRAP, 4));
        levelRewards.put(30, new LevelReward(30, 500, Material.DIAMOND, 6));

        levelRewards.put(31, new LevelReward(31, 350, Material.EMERALD, 12));
        levelRewards.put(32, new LevelReward(32, 360, Material.NETHERITE_SCRAP, 5));
        levelRewards.put(33, new LevelReward(33, 370, Material.DIAMOND, 7));
        levelRewards.put(34, new LevelReward(34, 380, Material.GOLD_INGOT, 25));
        levelRewards.put(35, new LevelReward(35, 600, Material.NETHERITE_INGOT, 2));
        levelRewards.put(36, new LevelReward(36, 390, Material.EMERALD, 15));
        levelRewards.put(37, new LevelReward(37, 400, Material.DIAMOND, 8));
        levelRewards.put(38, new LevelReward(38, 410, Material.NETHERITE_SCRAP, 6));
        levelRewards.put(39, new LevelReward(39, 420, Material.IRON_INGOT, 35));
        levelRewards.put(40, new LevelReward(40, 750, Material.DIAMOND, 10));

        levelRewards.put(41, new LevelReward(41, 450, Material.NETHERITE_SCRAP, 8));
        levelRewards.put(42, new LevelReward(42, 460, Material.EMERALD, 20));
        levelRewards.put(43, new LevelReward(43, 470, Material.DIAMOND, 12));
        levelRewards.put(44, new LevelReward(44, 480, Material.NETHERITE_INGOT, 2));
        levelRewards.put(45, new LevelReward(45, 1000, Material.NETHERITE_INGOT, 3));
        levelRewards.put(46, new LevelReward(46, 500, Material.DIAMOND, 15));
        levelRewards.put(47, new LevelReward(47, 520, Material.EMERALD, 25));
        levelRewards.put(48, new LevelReward(48, 540, Material.NETHERITE_INGOT, 3));
        levelRewards.put(49, new LevelReward(49, 560, Material.DIAMOND, 20));
        levelRewards.put(50, new LevelReward(50, 1500, Material.NETHERITE_INGOT, 5));
    }

    private void loadAllData() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            loadPlayer(p.getUniqueId());
        }
    }

    public void loadPlayer(UUID uuid) {
        try {
            ResultSet rs = plugin.getDB().queryResult("SELECT * FROM battlepass WHERE uuid='" + uuid + "'");
            if (rs != null && rs.next()) {
                BattlePassData data = new BattlePassData();
                data.level = rs.getInt("level");
                data.currentQuestId = rs.getInt("current_quest_id");
                data.currentProgress = rs.getInt("current_progress");

                String completedStr = rs.getString("completed_quests");
                if (completedStr != null && !completedStr.isEmpty()) {
                    for (String id : completedStr.split(",")) {
                        if (!id.isEmpty()) data.completedQuests.add(Integer.parseInt(id));
                    }
                }

                String claimedStr = rs.getString("claimed_rewards");
                if (claimedStr != null && !claimedStr.isEmpty()) {
                    for (String id : claimedStr.split(",")) {
                        if (!id.isEmpty()) data.claimedRewards.add(Integer.parseInt(id));
                    }
                }

                playerData.put(uuid, data);
                rs.close();
            } else {
                if (rs != null) rs.close();
                createNewPlayer(uuid);
            }
        } catch (Exception e) {
            createNewPlayer(uuid);
        }
    }

    private void createNewPlayer(UUID uuid) {
        BattlePassData data = new BattlePassData();
        data.level = 1;
        data.currentQuestId = 1;
        data.currentProgress = 0;
        data.completedQuests = new HashSet<>();
        data.claimedRewards = new HashSet<>();
        playerData.put(uuid, data);
        savePlayer(uuid);

        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline()) {
            sendNewQuestMessage(p, getCurrentQuest(p));
        }
    }

    public void handle(Player p, String type, String target, int amount) {
        UUID uuid = p.getUniqueId();
        BattlePassData data = playerData.get(uuid);
        if (data == null) {
            loadPlayer(uuid);
            data = playerData.get(uuid);
        }
        if (data == null) return;

        if (data.level >= 50) return;

        Quest currentQuest = questById.get(data.currentQuestId);
        if (currentQuest == null) return;

        if (!currentQuest.type.equals(type)) return;
        if (!currentQuest.target.equals(target) && !currentQuest.target.equals("ANY")) return;

        data.currentProgress += amount;

        if (data.currentProgress >= currentQuest.amount) {
            completeQuest(p, data, currentQuest);
        } else {
            int remaining = currentQuest.amount - data.currentProgress;
            if (remaining == 5 || remaining == 1) {
                p.sendMessage(ChatColor.YELLOW + currentQuest.name + ": " +
                        data.currentProgress + "/" + currentQuest.amount + " (ostalos " + remaining + ")");
            }
        }

        savePlayer(uuid);
    }

    private void completeQuest(Player p, BattlePassData data, Quest quest) {
        data.completedQuests.add(quest.id);

        int rewardMoney = quest.rewardMoney;
        if (plugin.getDB().hasPremium(p.getUniqueId())) {
            rewardMoney = rewardMoney * 2;
        }

        plugin.getEco().add(p.getUniqueId(), rewardMoney);

        p.sendTitle(ChatColor.GOLD + "ZADANIE VYPOLNENO!",
                ChatColor.YELLOW + "+" + rewardMoney + " monet",
                10, 60, 20);
        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        p.sendMessage(ChatColor.GREEN + quest.name + " vypolnen! +" + rewardMoney + " monet");

        int completedCount = data.completedQuests.size();
        int newLevel = (completedCount / 10) + 1;

        if (newLevel > data.level && newLevel <= 50) {
            data.level = newLevel;
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD + "====================================");
            p.sendMessage(ChatColor.GOLD + "UROVEN BOEVOGO PROPUSKA POVYSHEN!");
            p.sendMessage(ChatColor.YELLOW + "Novyj uroven: " + ChatColor.GOLD + data.level);
            p.sendMessage(ChatColor.GREEN + "Nagrada za uroven zhdyot vas v menu!");
            p.sendMessage(ChatColor.GOLD + "====================================");
            p.sendMessage("");
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        }

        int nextQuestId = getNextUncompletedQuest(data.completedQuests);
        if (nextQuestId != -1 && nextQuestId <= 50) {
            data.currentQuestId = nextQuestId;
            data.currentProgress = 0;
            sendNewQuestMessage(p, questById.get(nextQuestId));
        } else if (data.level >= 50) {
            p.sendMessage(ChatColor.GOLD + "POZDRAVLYaEM! VY ZAVERSHILI BOEVOJ PROPUSK!");
        }

        savePlayer(p.getUniqueId());
    }

    private int getNextUncompletedQuest(Set<Integer> completed) {
        for (int i = 1; i <= 50; i++) {
            if (!completed.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    private void sendNewQuestMessage(Player p, Quest quest) {
        if (quest == null) return;
        p.sendMessage("");
        p.sendMessage(ChatColor.GOLD + "====================================");
        p.sendMessage(ChatColor.GOLD + "NOVOE ZADANIE!");
        p.sendMessage(ChatColor.YELLOW + quest.name);
        p.sendMessage(ChatColor.GRAY + "Trebuetsya: " + getQuestDescription(quest));
        p.sendMessage(ChatColor.GREEN + "Nagrada: +" + quest.rewardMoney + " monet");
        p.sendMessage(ChatColor.GOLD + "====================================");
        p.sendMessage("");
    }

    private String getQuestDescription(Quest q) {
        switch (q.type) {
            case "kill": return "ubit " + q.target.toLowerCase().replace("_", " ") + " x" + q.amount;
            case "mine": return "dobyt " + q.target.toLowerCase().replace("_", " ") + " x" + q.amount;
            case "break": return "slomat " + q.target.toLowerCase().replace("_", " ") + " x" + q.amount;
            case "craft": return "skraftit " + q.target.toLowerCase().replace("_", " ") + " x" + q.amount;
            case "fish": return "pojmat ryby x" + q.amount;
            default: return "vypolnit zadanie";
        }
    }

    public void show(Player p) {
        BattlePassData data = playerData.get(p.getUniqueId());
        if (data == null) {
            loadPlayer(p.getUniqueId());
            data = playerData.get(p.getUniqueId());
        }
        if (data == null) return;

        Quest currentQuest = questById.get(data.currentQuestId);
        boolean isPremium = plugin.getDB().hasPremium(p.getUniqueId());

        p.sendMessage(ChatColor.GOLD + "========== BOEVOJ PROPUSK ==========");
        p.sendMessage(ChatColor.YELLOW + "Uroven: " + ChatColor.GOLD + data.level + ChatColor.GRAY + "/50");

        int completedPercent = (data.completedQuests.size() * 100) / 50;
        String bar = getProgressBar(completedPercent, 20);
        p.sendMessage(ChatColor.GRAY + "Progress: " + bar + " " + completedPercent + "%");
        p.sendMessage("");

        if (currentQuest != null) {
            p.sendMessage(ChatColor.AQUA + "TEKUShEE ZADANIE:");
            p.sendMessage(ChatColor.YELLOW + currentQuest.name);
            p.sendMessage(ChatColor.GRAY + "  " + getQuestDescription(currentQuest));
            p.sendMessage(ChatColor.GRAY + "  Progress: " + data.currentProgress + "/" + currentQuest.amount);
            int reward = isPremium ? currentQuest.rewardMoney * 2 : currentQuest.rewardMoney;
            p.sendMessage(ChatColor.GREEN + "  Nagrada: +" + reward + " monet" + (isPremium ? " (x2 premium)" : ""));
            p.sendMessage("");
        }

        p.sendMessage(ChatColor.LIGHT_PURPLE + "VYPOLNENO ZADANIJ: " + data.completedQuests.size() + "/50");

        LevelReward nextReward = levelRewards.get(data.level);
        if (nextReward != null && !data.claimedRewards.contains(data.level)) {
            p.sendMessage("");
            p.sendMessage(ChatColor.GREEN + "Dostupna nagrada za " + data.level + " uroven!");
            p.sendMessage(ChatColor.GRAY + "  Ispolzujte /battlepass claim chtoby poluchit");
        }

        p.sendMessage(ChatColor.GOLD + "====================================");
    }

    public void claimReward(Player p) {
        BattlePassData data = playerData.get(p.getUniqueId());
        if (data == null) {
            loadPlayer(p.getUniqueId());
            data = playerData.get(p.getUniqueId());
        }
        if (data == null) return;

        if (data.claimedRewards.contains(data.level)) {
            p.sendMessage(ChatColor.RED + "Vy uzhe poluchili nagradu za etot uroven!");
            return;
        }

        LevelReward reward = levelRewards.get(data.level);
        if (reward == null) {
            p.sendMessage(ChatColor.RED + "Nagrada ne najdena!");
            return;
        }

        if (data.completedQuests.size() < data.level * 10) {
            p.sendMessage(ChatColor.RED + "Vypolnite vse zadaniya dlya polucheniya nagrady!");
            return;
        }

        ItemStack item = new ItemStack(reward.item, reward.amount);
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), item);
            p.sendMessage(ChatColor.YELLOW + "Inventar polon, nagrada upala na zemlu");
        } else {
            p.getInventory().addItem(item);
        }

        plugin.getEco().add(p.getUniqueId(), reward.money);

        data.claimedRewards.add(data.level);
        savePlayer(p.getUniqueId());

        p.sendMessage(ChatColor.GREEN + "Vy poluchili nagradu za " + data.level + " uroven!");
        p.sendMessage(ChatColor.GREEN + "+" + reward.money + " monet, " + reward.amount + "x " + reward.item.name());
        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    private String getProgressBar(int percent, int length) {
        int filled = (percent * length) / 100;
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN + "#");
            } else {
                bar.append(ChatColor.GRAY + "#");
            }
        }
        return bar.toString();
    }

    private void savePlayer(UUID uuid) {
        BattlePassData data = playerData.get(uuid);
        if (data == null) return;

        StringBuilder completedSb = new StringBuilder();
        for (int id : data.completedQuests) {
            if (completedSb.length() > 0) completedSb.append(",");
            completedSb.append(id);
        }

        StringBuilder claimedSb = new StringBuilder();
        for (int id : data.claimedRewards) {
            if (claimedSb.length() > 0) claimedSb.append(",");
            claimedSb.append(id);
        }

        plugin.getDB().exec("INSERT OR REPLACE INTO battlepass VALUES ('" + uuid + "', " +
                data.level + ", " + data.currentQuestId + ", " + data.currentProgress + ", '" +
                completedSb.toString() + "', '" + claimedSb.toString() + "')");
    }

    private void startSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : playerData.keySet()) {
                    savePlayer(uuid);
                }
            }
        }.runTaskTimer(plugin, 0L, 6000L);
    }

    public Quest getCurrentQuest(Player p) {
        BattlePassData data = playerData.get(p.getUniqueId());
        if (data == null) return null;
        return questById.get(data.currentQuestId);
    }

    public BattlePassData getData(UUID uuid) {
        return playerData.get(uuid);
    }

    static class Quest {
        int id;
        String type;
        String target;
        int amount;
        int rewardMoney;
        String name;

        Quest(int id, String type, String target, int amount, int rewardMoney, String name) {
            this.id = id;
            this.type = type;
            this.target = target;
            this.amount = amount;
            this.rewardMoney = rewardMoney;
            this.name = name;
        }
    }

    static class LevelReward {
        int level;
        int money;
        Material item;
        int amount;

        LevelReward(int level, int money, Material item, int amount) {
            this.level = level;
            this.money = money;
            this.item = item;
            this.amount = amount;
        }
    }

    static class BattlePassData {
        int level = 1;
        int currentQuestId = 1;
        int currentProgress = 0;
        Set<Integer> completedQuests = new HashSet<>();
        Set<Integer> claimedRewards = new HashSet<>();
    }
}