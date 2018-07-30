package me.realized.de.leaderboards.leaderboard.leaderboards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.StringUtil;
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

    private boolean loaded;
    private int x, z;

    private final List<ArmorStand> lines = new ArrayList<>();

    public HologramLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location) {
        super(extension, LeaderboardType.HOLOGRAM, name, dataType, location);
        this.hologramLoading = config.getHologramLoading();
        this.hologramNoData = config.getHologramNoData();
        this.hologramHeader = config.getHologramHeader();
        this.hologramLineFormat = config.getHologramLineFormat();
        this.hologramFooter = config.getHologramFooter();
        this.spaceBetweenLines = config.getSpaceBetweenLines();

        final Chunk chunk = location.getChunk();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.loaded = true;
    }

    private HologramLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.HOLOGRAM, name);
        this.hologramLoading = getConfiguration().getString("override.loading", config.getHologramLoading());
        this.hologramNoData = getConfiguration().getString("override.no-data", config.getHologramNoData());
        this.hologramHeader = getConfiguration().getString("override.header", config.getHologramHeader());
        this.hologramLineFormat = getConfiguration().getString("override.line-format", config.getHologramLineFormat());
        this.hologramFooter = getConfiguration().getString("override.footer", config.getHologramFooter());
        this.spaceBetweenLines = getConfiguration().getDouble("override.space-between-lines", config.getSpaceBetweenLines());

        final Chunk chunk = getLocation().getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.loaded = true;
        getLocation().getWorld().getNearbyEntities(getLocation(), 0.5, 10, 0.5).forEach(entity -> {
            if (entity instanceof ArmorStand && !((ArmorStand) entity).isVisible() && entity.isCustomNameVisible()) {
                entity.remove();
            }
        });
        showLine(0, getLocation().clone(), StringUtil.color(hologramLoading));
    }

    @Override
    protected void onUpdate(final TopEntry entry) {
        final List<TopData> data = entry.getData();
        final Location location = getLocation().clone();

        if (!loaded) {
            return;
        }

        if (data.isEmpty()) {
            showLine(0, location, StringUtil.color(hologramNoData));
            return;
        }

        final double space = 0.23 + spaceBetweenLines;
        showLine(0, location, StringUtil.color(hologramHeader.replace("%type%", entry.getType())));

        for (int i = 0; i < data.size(); i++) {
            final TopData topData = data.get(i);
            showLine(i + 1, location.subtract(0, space, 0), StringUtil.color(hologramLineFormat
                .replace("%rank%", String.valueOf(i + 1)).replace("%name%", topData.getName())
                .replace("%value%", String.valueOf(topData.getValue())).replace("%identifier%", entry.getIdentifier())));
        }

        showLine(data.size() + 1, location.subtract(0, space, 0), StringUtil.color(hologramFooter.replace("%type%", entry.getType())));
    }

    private void showLine(final int index, final Location location, final String text) {
        ArmorStand armorStand;

        if (lines.size() <= index) {
            armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setRemoveWhenFarAway(false);
            lines.add(armorStand);
        } else {
            armorStand = lines.get(index);
        }

        armorStand.setCustomName(text);
    }

    @Override
    public void teleport(final Location location) {
        setLocation(location);
        removeAll();
        lines.clear();
        this.x = location.getChunk().getX();
        this.z = location.getChunk().getZ();
        loaded = true;
        setChanged(true);
    }

    @Override
    public void save() {
        removeAll();
        lines.clear();
        super.save();
    }

    @Override
    public void onRemove() {
        removeAll();
        lines.clear();
    }

    public void onLoad(final Chunk chunk) {
        if (chunk.getX() == x && chunk.getZ() == z) {
            loaded = true;
            setChanged(true);
        }
    }

    public void onUnload(final Chunk chunk) {
        if (chunk.getX() == x && chunk.getZ() == z) {
            removeAll();
            lines.clear();
            loaded = false;
        }
    }

    private void removeAll() {
        lines.forEach(Entity::remove);
    }

    public static HologramLeaderboard from(final Leaderboards extension, final String name, final File file) throws IllegalArgumentException {
        return new HologramLeaderboard(extension, file, name);
    }
}
