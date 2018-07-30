package me.realized.de.leaderboards;

import lombok.Getter;
import me.realized.de.leaderboards.command.LeaderboardCommand;
import me.realized.de.leaderboards.config.Config;
import me.realized.de.leaderboards.leaderboard.LeaderboardManager;
import me.realized.de.leaderboards.util.CompatUtil;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;

public class Leaderboards extends DuelsExtension {

    @Getter
    private UserManager userManager;
    @Getter
    private KitManager kitManager;
    @Getter
    private ArenaManager arenaManager;

    @Getter
    private Config configuration;
    @Getter
    private LeaderboardManager leaderboardManager;

    @Override
    public void onEnable() {
        if (CompatUtil.isPre1_8()) {
            warn("ArmorStand is not supported on this server version. Hologram Leaderboard will be disabled.");
        }

        this.userManager = api.getUserManager();
        this.kitManager = api.getKitManager();
        this.arenaManager = api.getArenaManager();

        this.configuration = new Config(this);
        this.leaderboardManager = new LeaderboardManager(this, api);
        api.registerSubCommand("duels", new LeaderboardCommand(this));
        api.getServer().getPluginManager().registerEvents(leaderboardManager, api);
        api.doSyncRepeat(() -> leaderboardManager.update(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        leaderboardManager.save();
    }

    @Override
    public String getRequiredVersion() {
        return "3.1.0";
    }

    public void warn(final String s) {
        api.warn("[" + getName()  + " Extension] " + s);
    }

    public void error(final String s, final Throwable thrown) {
        api.error("[" + getName()  + " Extension] " + s, thrown);
    }
}
