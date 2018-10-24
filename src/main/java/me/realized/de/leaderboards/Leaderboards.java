package me.realized.de.leaderboards;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import me.realized.de.leaderboards.command.LeaderboardCommand;
import me.realized.de.leaderboards.config.Config;
import me.realized.de.leaderboards.hooks.MVdWPlaceholderHook;
import me.realized.de.leaderboards.hooks.PlaceholderHook;
import me.realized.de.leaderboards.leaderboard.LeaderboardManager;
import me.realized.de.leaderboards.util.CompatUtil;
import me.realized.de.leaderboards.util.NumberUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.de.leaderboards.util.Updatable;
import me.realized.duels.api.Duels;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.event.kit.KitCreateEvent;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import me.realized.duels.api.user.UserManager.TopData;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Leaderboards extends DuelsExtension implements Listener {

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

    private final List<Updatable<Kit>> updatables = new ArrayList<>();
    private int updateTask;

    @Override
    public void onEnable() {
        if (CompatUtil.isPre1_8()) {
            warn("ArmorStand is not supported on this server version. Hologram Leaderboard will be disabled.");
        }

        this.userManager = api.getUserManager();
        this.kitManager = api.getKitManager();
        this.arenaManager = api.getArenaManager();

        this.configuration = new Config(this);
        this.leaderboardManager = new LeaderboardManager(this);
        api.registerSubCommand("duels", new LeaderboardCommand(this));
        api.registerListener(leaderboardManager);
        api.registerListener(this);
        this.updateTask = api.doSyncRepeat(() -> leaderboardManager.update(), 20L, 20L).getTaskId();
        doIfFound("MVdWPlaceholderAPI", () -> register(MVdWPlaceholderHook.class));
        doIfFound("PlaceholderAPI", () -> register(PlaceholderHook.class));
    }

    @Override
    public void onDisable() {
        api.cancelTask(updateTask);
        leaderboardManager.save();
        updatables.clear();
    }

    @Override
    public String getRequiredVersion() {
        return "3.2.0";
    }

    public void info(final String s) {
        api.info("[" + getName()  + " Extension] " + s);
    }

    public void warn(final String s) {
        api.warn("[" + getName()  + " Extension] " + s);
    }

    public void error(final String s, final Throwable thrown) {
        api.error("[" + getName()  + " Extension] " + s, thrown);
    }

    @SuppressWarnings("unchecked")
    private void register(final Class<? extends Updatable<Kit>> clazz) {
        try {
            updatables.add(clazz.getConstructor(Leaderboards.class, Duels.class).newInstance(this, api));
        } catch (Exception ignored) {}
    }

    public boolean isEnabled(final String name) {
        return api.getServer().getPluginManager().isPluginEnabled(name);
    }

    private void doIfFound(final String name, final Runnable action) {
        if (!isEnabled(name)) {
            return;
        }

        action.run();
    }

    public String find(final Player player, final String identifier) {
        if (identifier.startsWith("rank_")) {
            if (player == null) {
                return "Player is required";
            }

            final TopEntry entry = getTopByType(identifier.replace("rank_", "").replace("-", " "));

            if (entry == null) {
                return StringUtil.color(configuration.getPlaceholderLoading());
            }

            for (int i = 0; i < entry.getData().size(); i++) {
                final TopData data = entry.getData().get(i);

                if (data.getUuid().equals(player.getUniqueId())) {
                    return String.valueOf(i + 1);
                }
            }

            return StringUtil.color(configuration.getPlaceholderNoRank());
        }

        if (identifier.startsWith("top_")) {
            final String[] args = identifier.replace("top_", "").split("_");

            if (args.length < 3) {
                return null;
            }

            final TopEntry entry = getTopByType(args[0].replace("-", " "));

            if (entry == null) {
                return StringUtil.color(configuration.getPlaceholderLoading());
            }

            final List<TopData> data = entry.getData();

            if (data.isEmpty()) {
                return StringUtil.color(configuration.getPlaceholderNoData());
            }

            final int rank = Math.min(Math.max(1, NumberUtil.parseInt(args[2]).orElse(1)), 10);

            if (rank > data.size()) {
                return StringUtil.color(configuration.getPlaceholderNoData());
            }

            final TopData topData = data.get(Math.min(Math.max(1, NumberUtil.parseInt(args[2]).orElse(1)), 10) - 1);

            if (args[1].equalsIgnoreCase("name")) {
                return topData.getName();
            } else if (args[1].equalsIgnoreCase("value")) {
                return String.valueOf(topData.getValue());
            }
        }

        return null;
    }

    private TopEntry getTopByType(final String type) {
        if (type.equalsIgnoreCase("wins")) {
            return userManager.getTopWins();
        } else if (type.equalsIgnoreCase("losses")) {
            return userManager.getTopLosses();
        } else {
            final Kit kit = kitManager.get(type);

            if (kit == null) {
                return null;
            }

            return userManager.getTopRatings(kit);
        }
    }

    @EventHandler
    public void on(final KitCreateEvent event) {
        updatables.forEach(updatable -> updatable.update(event.getKit()));
    }
}
