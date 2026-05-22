package ru.goldRush;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.goldRush.commands.*;
import ru.goldRush.core.*;
import ru.goldRush.modules.*;

public class GoldRush extends JavaPlugin {
    private static GoldRush instance;
    private Database db;
    private QuestManager quests;
    private Economy eco;
    private Jail jail;
    private ModManager mods;
    private RatingManager ratingManager;
    private Tab tab;
    private AuthManager authManager;
    private VanishCommand vanish;
    private NameTagManager nameTagManager;
    private ServerBrandManager serverBrandManager;
    private BattlePassManager battlePass;
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        new ChatManager();
        db = new Database();
        quests = new QuestManager();
        eco = new Economy();
        jail = new Jail();
        mods = new ModManager();
        ratingManager = new RatingManager(this);
        tab = new Tab(this);
        vanish = new VanishCommand();
        authManager = new AuthManager(this);
        nameTagManager = new NameTagManager(this);
        serverBrandManager = new ServerBrandManager(this);
        battlePass = new BattlePassManager(this);  // ДОБАВИТЬ

        getCommand("register").setExecutor(new AuthCommand());
        getCommand("login").setExecutor(new AuthCommand());
        getCommand("changepassword").setExecutor(new AuthCommand());
        getCommand("balance").setExecutor(new EconomyCommand());
        getCommand("pay").setExecutor(new EconomyCommand());
        getCommand("admin").setExecutor(new AdminCommand());
        getCommand("mod").setExecutor(new AdminCommand());
        getCommand("invsee").setExecutor(new AdminCommand());
        getCommand("ip").setExecutor(new AdminCommand());
        getCommand("setjail").setExecutor(new JailCommand());
        getCommand("jail").setExecutor(new JailCommand());
        getCommand("unjail").setExecutor(new JailCommand());
        getCommand("jailinfo").setExecutor(new JailCommand());
        getCommand("quest").setExecutor(new QuestCommand());
        getCommand("pr").setExecutor(new PrefixCommand());
        getCommand("premiumgive").setExecutor(new PremiumGiveCommand());
        getCommand("rating").setExecutor(ratingManager);
        getCommand("rating").setTabCompleter(ratingManager);
        getCommand("battlepass").setExecutor(new BattlePassCommand());
        getCommand("auction").setExecutor(new AuctionCommand());
        Bukkit.getPluginManager().registerEvents(new EconomyCommand(), this);
        Bukkit.getPluginManager().registerEvents(new Events(), this);

        quests.startDailyReset();
        getLogger().info("GoldRush включён!");
        Bukkit.getServer().setMotd(ChatColor.translateAlternateColorCodes('&', "&x&7&D&E&A&2&0&lGoldRush by merzko63"));
    }

    public void onDisable() {
        if (db != null) db.close();
        getLogger().info("GoldRush выключён!");
    }

    public static GoldRush getInstance() { return instance; }
    public Database getDB() { return db; }
    public QuestManager getQuests() { return quests; }
    public Economy getEco() { return eco; }
    public Jail getJail() { return jail; }
    public ModManager getMods() { return mods; }
    public RatingManager getRatingManager() { return ratingManager; }
    public Tab getTab() { return tab; }
    public VanishCommand getVanish() { return vanish; }
    public AuthManager getAuthManager() { return authManager; }
    public NameTagManager getNameTagManager() { return nameTagManager; }
    public ServerBrandManager getServerBrandManager() { return serverBrandManager; }
    public BattlePassManager getBattlePass() { return battlePass; }
}