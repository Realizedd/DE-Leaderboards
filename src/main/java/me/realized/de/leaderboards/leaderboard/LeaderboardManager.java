package me.realized.de.leaderboards.leaderboard;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.realized.de.leaderboards.Lang;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.util.CompatUtil;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.de.leaderboards.util.TextBuilder;
import me.realized.duels.api.Duels;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.user.UserManager;
import me.realized.duels.api.user.UserManager.TopEntry;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class LeaderboardManager implements Listener {

    private final Duels api;
    private final UserManager userManager;
    private final KitManager kitManager;

    @Getter
    private final File folder;
    @Getter
    private final Map<LeaderboardType, Map<String, AbstractLeaderboard>> leaderboards = new HashMap<>();

    public LeaderboardManager(final Leaderboards extension) {
        this.api = extension.getApi();
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

                    if (type == LeaderboardType.HOLOGRAM && CompatUtil.isPre1_8()) {
                        extension.info("Skipping " + file.getName() + " from load - Hologram Leaderboards are not supported on this server version.");
                        continue;
                    }

                    final String name = data[1].toLowerCase();

                    try {
                        addLeaderboard(type.from(extension, api, name, file));
                    } catch (Exception ex) {
                        extension.warn("Failed to load leaderboard '" + name + "' (" + type + "): " + ex.getMessage());
                    }
                }
            }
        }, 10L);
    }

    public void save() {
        leaderboards.forEach((key, value) -> value.forEach((name, leaderboard) -> leaderboard.save()));
        leaderboards.clear();
    }

    public void update() {
        final Map<String, TopEntry> localCache = new HashMap<>();

        leaderboards.values().forEach(value -> value.values().forEach(leaderboard -> {
            final String dataType = leaderboard.getDataType();
            TopEntry localCached = localCache.get(dataType);

            if (localCached == null) {
                if (dataType.equalsIgnoreCase("wins")) {
                    localCached = userManager.getTopWins();
                } else if (dataType.equalsIgnoreCase("losses")) {
                    localCached = userManager.getTopLosses();
                } else {
                    final Kit kit = kitManager.get(dataType);

                    if (kit != null) {
                        localCached = userManager.getTopRatings(kit);
                    }
                }

                localCache.put(dataType, localCached);
            }

            leaderboard.update(localCached);
        }));
    }

    public Leaderboard get(final LeaderboardType type, final String name) {
        final Map<String, AbstractLeaderboard> cache;
        return (cache = leaderboards.get(type)) != null ? cache.get(name) : null;
    }

    public Leaderboard get(final Block block) {
        return leaderboards.values()
            .stream().flatMap(map -> map.values().stream()).filter(leaderboard -> leaderboard.getLocation().getBlock().equals(block)).findFirst().orElse(null);
    }

    public HeadLeaderboard get(final Sign sign) {
        final Map<String, AbstractLeaderboard> cache = leaderboards.get(LeaderboardType.HEAD);

        if (cache == null || cache.isEmpty()) {
            return null;
        }

        final Leaderboard result = cache.values().stream().filter(lb -> lb.getLocation().getBlock().getState().equals(sign)).findFirst().orElse(null);
        return result != null ? (HeadLeaderboard) result : null;
    }

    public Leaderboard remove(final LeaderboardType type, final String name) {
        final Map<String, AbstractLeaderboard> cache;
        return (cache = leaderboards.get(type)) != null ? cache.remove(name) : null;
    }

    public boolean addLeaderboard(final AbstractLeaderboard leaderboard) {
        final LeaderboardType type = leaderboard.getType();

        if (!type.getType().isInstance(leaderboard)) {
            return false;
        }

        final Map<String, AbstractLeaderboard> cache = this.leaderboards.computeIfAbsent(type, result -> new HashMap<>());
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
                .of(Lang.LEADERBOARD_BLOCK_BREAK.format(removeCommand), Action.RUN_COMMAND, removeCommand, null, null)
                .send(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();

        if (Bukkit.isPrimaryThread()) {
            handleChunkLoad(chunk);
        } else {
            api.doSync(() -> handleChunkLoad(chunk));
        }
    }

    private void handleChunkLoad(final Chunk chunk) {
        final Map<String, AbstractLeaderboard> hologramLeaderboards = leaderboards.get(LeaderboardType.HOLOGRAM);

        if (hologramLeaderboards == null) {
            return;
        }

        hologramLeaderboards.values().forEach(leaderboard -> {
            final HologramLeaderboard hologramLeaderboard = (HologramLeaderboard) leaderboard;

            if (hologramLeaderboard.isInChunk(chunk)) {
                hologramLeaderboard.onLoad();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final ChunkUnloadEvent event) {
        final Map<String, AbstractLeaderboard> hologramLeaderboards = leaderboards.get(LeaderboardType.HOLOGRAM);

        if (hologramLeaderboards == null) {
            return;
        }

        final Chunk chunk = event.getChunk();
        final List<Entity> entities = Lists.newArrayList(chunk.getEntities());
        hologramLeaderboards.values().forEach(leaderboard -> {
            final HologramLeaderboard hologramLeaderboard = (HologramLeaderboard) leaderboard;

            if (hologramLeaderboard.isInChunk(chunk)) {
                hologramLeaderboard.onUnload(entities);
            }
        });
    }
}
