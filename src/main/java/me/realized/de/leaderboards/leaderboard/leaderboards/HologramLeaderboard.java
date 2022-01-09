package me.realized.de.leaderboards.leaderboard.leaderboards;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.CompatUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.Duels;
import me.realized.duels.api.user.UserManager.TopData;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class HologramLeaderboard extends AbstractLeaderboard {

    private final String hologramLoading;
    private final String hologramNoData;
    private final String hologramHeader;
    private final String hologramLineFormat;
    private final String hologramFooter;
    private final double spaceBetweenLines;

    private Hologram hologram;
    private boolean init;

    private final List<ArmorStand> lines = new ArrayList<>();
    private int x, z;

    public HologramLeaderboard(final Leaderboards extension, final Duels api, final String name, final String dataType, final Location location) {
        super(extension, api, LeaderboardType.HOLOGRAM, name, dataType, location);
        this.hologramLoading = config.getHologramLoading();
        this.hologramNoData = config.getHologramNoData();
        this.hologramHeader = config.getHologramHeader();
        this.hologramLineFormat = config.getHologramLineFormat();
        this.hologramFooter = config.getHologramFooter();
        this.spaceBetweenLines = config.getSpaceBetweenLines();

        final Chunk chunk = location.getChunk();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.init = true;
    }

    private HologramLeaderboard(final Leaderboards extension, final Duels api, final File file, final String name) {
        super(extension, api, file, LeaderboardType.HOLOGRAM, name);
        this.hologramLoading = getConfiguration().getString("override.loading", config.getHologramLoading());
        this.hologramNoData = getConfiguration().getString("override.no-data", config.getHologramNoData());
        this.hologramHeader = getConfiguration().getString("override.header", config.getHologramHeader());
        this.hologramLineFormat = getConfiguration().getString("override.line-format", config.getHologramLineFormat());
        this.hologramFooter = getConfiguration().getString("override.footer", config.getHologramFooter());
        this.spaceBetweenLines = getConfiguration().getDouble("override.space-between-lines", config.getSpaceBetweenLines());

        this.x = floor(getLocation().getX()) >> 4;
        this.z = floor(getLocation().getZ()) >> 4;

        api.doSyncAfter(() -> {
            if (!getLocation().getWorld().isChunkLoaded(x, z)) {
                return;
            }

            initiate();
        }, 10L);
    }

    private int floor(final double num) {
        final int floor = (int) num;
        return (floor == num) ? floor : (floor - (int) (Double.doubleToRawLongBits(num) >>> 63));
    }

    private double distance2D(Location first, Location second) {
        final double dX = first.getX() - second.getX();
        final double dZ = first.getZ() - second.getZ();
        return Math.sqrt(dX * dX + dZ * dZ);
    }

    private void initiate() {
        getLocation().getWorld().getEntitiesByClass(ArmorStand.class).stream()
            .filter(armorStand -> !armorStand.isVisible() && armorStand.isCustomNameVisible() && distance2D(armorStand.getLocation(), getLocation()) <= 1.0)
            .forEach(Entity::remove);

        if (config.isHookHD() && extension.isEnabled("HolographicDisplays") && hologram == null) {
            this.hologram = HologramsAPI.createHologram(api, getLocation().clone());
        }

        showLine(0, getLocation().clone(), StringUtil.color(hologramLoading));
        setChanged(true);
        init = true;
    }

    private void showLine(final int index, final Location location, final String text) {
        if (config.isHookHD() && hologram != null) {
            hologram.insertTextLine(index, text);

            final int last = hologram.size() - 1;

            if (last > 0 && ((TextLine) hologram.getLine(last)).getText().equals(StringUtil.color(hologramLoading))) {
                hologram.removeLine(last);
            }
            return;
        }

        ArmorStand armorStand;

        if (lines.size() <= index) {
            armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);

            if (CompatUtil.hasMarker()) {
                armorStand.setMarker(true);
            }

            armorStand.setRemoveWhenFarAway(false);
            lines.add(armorStand);
        } else {
            armorStand = lines.get(index);
        }

        armorStand.setCustomName(text);
    }

    private String getPrefix(final UUID uuid) {
        return extension.getVaultHook() != null ? extension.getVaultHook().findPrefix(uuid) : "";
    }

    private void showLines(final TopEntry entry, final Location location, final List<PrefixedTopData> data) {
        final double space = 0.23 + spaceBetweenLines;
        showLine(0, location, StringUtil.color(hologramHeader.replace("%type%", entry.getType())));

        for (int i = 0; i < data.size(); i++) {
            final PrefixedTopData topData = data.get(i);
            final int rank = i + 1;

            showLine(rank, location.subtract(0, space, 0), StringUtil.color(hologramLineFormat
                .replace("%rank%", String.valueOf(rank))
                .replace("%name%", (topData.prefix != null ? topData.prefix : "") + topData.name)
                .replace("%value%", String.valueOf(topData.value))
                .replace("%identifier%", entry.getIdentifier())));
        }

        showLine(data.size() + 1, location.subtract(0, space, 0), StringUtil.color(hologramFooter.replace("%type%", entry.getType())));
    }

    @Override
    protected void onUpdate(final TopEntry entry) {
        if (!init || !getLocation().getWorld().isChunkLoaded(x, z)) {
            return;
        }

        if (config.isHookHD() && hologram != null) {
            hologram.clearLines();
        }

        final List<PrefixedTopData> data = entry.getData().stream().map(PrefixedTopData::new).collect(Collectors.toList());
        final Location location = getLocation().clone();

        if (data.isEmpty()) {
            showLine(0, location, StringUtil.color(hologramNoData));
            return;
        }

        if (config.isPrefixesEnabled()) {
            api.doAsync(() -> {
                data.forEach(topData -> topData.prefix = getPrefix(topData.uuid));
                api.doSync(() -> showLines(entry, location, data));
            });
        } else {
            showLines(entry, location, data);
        }
    }

    @Override
    public void teleport(final Location location) {
        if (config.isHookHD() && hologram != null) {
            hologram.teleport(location);
        }

        setLocation(location);
        extension.getLeaderboardManager().updateLocation(this);
        removeAll();
        this.x = location.getChunk().getX();
        this.z = location.getChunk().getZ();
        setChanged(true);
    }

    @Override
    public void save() {
        removeAll();
        super.save();
    }

    @Override
    public void onRemove() {
        removeAll();
    }

    public boolean isInChunk(final Chunk chunk) {
        return chunk.getX() == x && chunk.getZ() == z;
    }

    public void onLoad() {
        initiate();
    }

    public void onUnload(final List<Entity> entities) {
        if (lines.stream().anyMatch(entities::contains)) {
            removeAll();
        }
    }

    private void removeAll() {
        if (config.isHookHD() && hologram != null) {
            hologram.delete();
        }

        lines.forEach(Entity::remove);
        lines.clear();
    }

    public static HologramLeaderboard from(final Leaderboards extension, final Duels api, final String name, final File file) throws IllegalArgumentException {
        return new HologramLeaderboard(extension, api, file, name);
    }

    private static class PrefixedTopData {

        private final UUID uuid;
        private final String name;
        private final int value;

        private String prefix;

        public PrefixedTopData(final TopData data) {
            this.uuid = data.getUuid();
            this.name = data.getName();
            this.value = data.getValue();
        }
    }
}
