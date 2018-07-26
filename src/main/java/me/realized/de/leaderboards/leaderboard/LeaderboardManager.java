package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.block.Block;
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
    private final Map<LeaderboardType, Map<String, Leaderboard>> leaderboards = new HashMap<>();

    public LeaderboardManager(final Leaderboards extension) {
        this.extension = extension;
        this.userManager = extension.getUserManager();
        this.kitManager = extension.getKitManager();
        this.folder = new File(extension.getDataFolder(), "leaderboards");

        if (!folder.exists()) {
            folder.mkdir();
        }

        final File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") && name.split("-").length > 1);

        if (files != null) {
            for (final File file : files) {
                final String[] data = file.getName().replace(".yml", "").split("-");
                final LeaderboardType type = LeaderboardType.get(data[0]);

                if (type == null) {
                    continue;
                }

                final String name = data[1];

                try {
                    addLeaderboard(type.from(extension, name, file));
                } catch (Exception ex) {
                    extension.warn("Failed to load leaderboard '" + name + "' (" + type + "): " + ex.getMessage());
                }
            }
        }
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

    public void addLeaderboard(final Leaderboard leaderboard) {
        final LeaderboardType type = leaderboard.getType();

        if (!type.getType().isInstance(leaderboard)) {
            return;
        }

        final Map<String, Leaderboard> cache = this.leaderboards.computeIfAbsent(type, result -> new HashMap<>());
        final String name = leaderboard.getName();

        if (!cache.isEmpty() && cache.containsKey(name)) {
            return;
        }

        cache.put(name, leaderboard);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Leaderboard found = leaderboards.values()
            .stream().flatMap(map -> map.values().stream()).filter(leaderboard -> block.equals(leaderboard.getLocation().getBlock())).findFirst().orElse(null);

        if (found != null && !(found instanceof HologramLeaderboard)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Cannot destroy a leaderboard by hand. Please use /ds lb remove " + found.getType().name() + " " + found.getName());
        }
    }
}
