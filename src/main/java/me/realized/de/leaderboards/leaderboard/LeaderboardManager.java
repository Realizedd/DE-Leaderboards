package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.de.leaderboards.util.TextBuilder;
import me.realized.duels.api.Duels;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import me.realized.duels.api.user.UserManager.TopEntry;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class LeaderboardManager implements Listener {

    private final Leaderboards extension;
    private final UserManager userManager;
    private final KitManager kitManager;

    @Getter
    private final File folder;

    // TODO: 26/07/2018 Cache previous update data and only update if there's a change in data list
    private final Map<String, TopEntry> cache = new HashMap<>();

    @Getter
    private final Map<LeaderboardType, Map<String, Leaderboard>> leaderboards = new HashMap<>();

    public LeaderboardManager(final Leaderboards extension, final Duels api) {
        this.extension = extension;
        this.userManager = extension.getUserManager();
        this.kitManager = extension.getKitManager();
        this.folder = new File(extension.getDataFolder(), "leaderboards");

        if (!folder.exists()) {
            folder.mkdir();
        }

        // Load late to prevent ArmorStand not spawning on first startup
        api.doSyncAfter(() -> {
            final File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") && name.split("-").length > 1);

            if (files != null) {
                for (final File file : files) {
                    final String[] data = file.getName().replace(".yml", "").split("-");
                    final LeaderboardType type = EnumUtil.getByName(data[0].toUpperCase(), LeaderboardType.class);

                    if (type == null) {
                        continue;
                    }

                    final String name = data[1].toLowerCase();

                    try {
                        addLeaderboard(type.from(extension, name, file));
                    } catch (Exception ex) {
                        extension.warn("Failed to load leaderboard '" + name + "' (" + type + "): " + ex.getMessage());
                    }
                }
            }
        }, 10L);
    }

    public void save() {
        leaderboards.forEach((key, value) -> value.forEach((name, leaderboard) -> leaderboard.save()));
    }

    public void update() {
        final Map<String, TopEntry> data = new HashMap<>();

        leaderboards.values().forEach(value -> value.values().forEach(leaderboard -> {
            final String dataType = leaderboard.getDataType();
            TopEntry cached = data.get(dataType);

            if (cached == null) {
                if (dataType.equalsIgnoreCase("wins")) {
                    cached = userManager.getTopWins();
                } else if (dataType.equalsIgnoreCase("losses")) {
                    cached = userManager.getTopLosses();
                } else {
                    final Kit kit = kitManager.get(dataType);

                    if (kit != null) {
                        cached = userManager.getTopRatings(kit);
                    }
                }

                data.put(dataType, cached);
            }

            leaderboard.update(cached);
        }));
    }

    public Leaderboard get(final LeaderboardType type, final String name) {
        final Map<String, Leaderboard> cache;
        return (cache = leaderboards.get(type)) != null ? cache.get(name) : null;
    }

    public HeadLeaderboard get(final Sign sign) {
        final Map<String, Leaderboard> cache = leaderboards.get(LeaderboardType.HEAD);

        if (cache == null || cache.isEmpty()) {
            return null;
        }

        final Leaderboard result = cache.values().stream().filter(lb -> lb.getLocation().getBlock().getState().equals(sign)).findFirst().orElse(null);
        return result != null ? (HeadLeaderboard) result : null;
    }

    public Leaderboard remove(final LeaderboardType type, final String name) {
        final Map<String, Leaderboard> cache;
        return (cache = leaderboards.get(type)) != null ? cache.remove(name) : null;
    }

    public boolean addLeaderboard(final Leaderboard leaderboard) {
        final LeaderboardType type = leaderboard.getType();

        if (!type.getType().isInstance(leaderboard)) {
            return false;
        }

        final Map<String, Leaderboard> cache = this.leaderboards.computeIfAbsent(type, result -> new HashMap<>());
        final String name = leaderboard.getName();

        if (!cache.isEmpty() && cache.containsKey(name)) {
            return false;
        }

        cache.put(name, leaderboard);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Leaderboard found = leaderboards.values()
            .stream().flatMap(map -> map.values().stream()).filter(leaderboard -> block.equals(leaderboard.getLocation().getBlock())).findFirst().orElse(null);

        if (found != null && !(found instanceof HologramLeaderboard)) {
            event.setCancelled(true);

            final String removeCommand = "/ds lb remove " + found.getType().name() + " " + found.getName();
            TextBuilder
                .of("Cannot destroy a leaderboard by hand. Type '" + removeCommand + "' or ")
                .add(StringUtil.color("&b&nClick Me to Remove!"), Action.RUN_COMMAND, removeCommand)
                .send(event.getPlayer());
        }
    }
}
