package me.realized.de.leaderboards;

import java.util.List;
import java.util.logging.Level;
import lombok.Getter;
import me.realized.de.leaderboards.command.LeaderboardCommand;
import me.realized.de.leaderboards.leaderboard.LeaderboardManager;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import org.bukkit.configuration.file.FileConfiguration;

public class Leaderboards extends DuelsExtension {

    @Getter
    private List<String> headSignFormat;
    @Getter
    private String hologramHeader;
    @Getter
    private String hologramLineFormat;
    @Getter
    private String hologramFooter;
    @Getter
    private double spaceBetweenLines;
    @Getter
    private String signHeader;
    @Getter
    private boolean signSpaceBetween;
    @Getter
    private String signLoading;
    @Getter
    private String signNoData;
    @Getter
    private String signLineFormat;

    @Getter
    private UserManager userManager;
    @Getter
    private KitManager kitManager;
    @Getter
    private ArenaManager arenaManager;
    @Getter
    private LeaderboardManager leaderboardManager;

    @Override
    public void onEnable() {
        final FileConfiguration config = getConfig();
        this.headSignFormat = config.getStringList("types.HEAD.sign-format");
        this.hologramHeader = config.getString("types.HOLOGRAM.header");
        this.hologramLineFormat = config.getString("types.HOLOGRAM.line-format");
        this.hologramFooter = config.getString("types.HOLOGRAM.footer");
        this.spaceBetweenLines = config.getDouble("types.HOLOGRAM.space-between-lines");
        this.signHeader = config.getString("types.SIGN.header");
        this.signSpaceBetween = config.getBoolean("types.SIGN.space-between");
        this.signLoading = config.getString("types.SIGN.loading");
        this.signNoData = config.getString("types.SIGN.no-data");
        this.signLineFormat = config.getString("types.SIGN.sign-line-format");

        try {
            Class.forName("org.bukkit.entity.ArmorStand");
        } catch (ClassNotFoundException ex) {
            api.getLogger().log(Level.WARNING, getName() + ": ArmorStand is not supported on this server version. HologramLeaderboard will not function properly.");
        }

        this.userManager = api.getUserManager();
        this.kitManager = api.getKitManager();
        this.arenaManager = api.getArenaManager();
        this.leaderboardManager = new LeaderboardManager(this);
        api.registerSubCommand("duels", new LeaderboardCommand(this));
        api.getServer().getPluginManager().registerEvents(leaderboardManager, api);
        api.getServer().getScheduler().runTaskTimer(api, () -> leaderboardManager.update(), 20L, 20L);
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
        api.getLogger().warning("[" + getName()  + " Extension] " + s);
    }
}
