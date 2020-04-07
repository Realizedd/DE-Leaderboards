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
import me.realized.duels.api.user.UserManager.TopEntry;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
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

    private final Leaderboards extension;
    private final Duels api;

    @Getter
    private final File folder;
    @Getter
    private final Map<LeaderboardType, Map<String, AbstractLeaderboard>> leaderboards = new HashMap<>();
    private final Map<Location, AbstractLeaderboard> locations = new HashMap<>();

    public LeaderboardManager(final Leaderboards extension) {
        this.extension = extension;
        this.api = extension.getApi();
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
        locations.clear();
    }

    public void update() {
        final Map<String, TopEntry> cache = new HashMap<>();

        leaderboards.values().forEach(value -> value.values().forEach(leaderboard -> {
            final String type = leaderboard.getDataType();
            leaderboard.update(cache.computeIfAbsent(type, result -> extension.getTopByType(type)));
        }));
    }

    public Leaderboard get(final LeaderboardType type, final String name) {
        final Map<String, AbstractLeaderboard> cache;
        return (cache = leaderboards.get(type)) != null ? cache.get(name) : null;
    }

    public AbstractLeaderboard get(final Block block) {
        return locations.get(block.getLocation());
    }

    public HeadLeaderboard get(final Sign sign) {
        final AbstractLeaderboard leaderboard;
        return (leaderboard = get(sign.getBlock())) instanceof HeadLeaderboard ? (HeadLeaderboard) leaderboard : null;
    }

    public Leaderboard remove(final LeaderboardType type, final String name) {
        final Map<String, AbstractLeaderboard> cache = leaderboards.get(type);

        if (cache == null) {
            return null;
        }

        final AbstractLeaderboard leaderboard = cache.remove(name);

        if (leaderboard == null) {
            return null;
        }

        locations.remove(leaderboard.getLocation());
        return leaderboard;
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
        locations.put(leaderboard.getLocation(), leaderboard);
        update();
        return true;
    }

    public void updateLocation(final AbstractLeaderboard leaderboard) {
        locations.put(leaderboard.getLocation(), leaderboard);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        final Leaderboard found = get(event.getBlock());

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
