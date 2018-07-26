package me.realized.de.leaderboards.leaderboard.leaderboards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.user.UserManager.TopEntry;
import me.realized.duels.api.util.Pair;
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

    private final List<ArmorStand> lines = new ArrayList<>();

    public HologramLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location) {
        super(extension, LeaderboardType.HOLOGRAM, name, dataType, location);
        this.hologramLoading = config.getHologramLoading();
        this.hologramNoData = config.getHologramNoData();
        this.hologramHeader = config.getHologramHeader();
        this.hologramLineFormat = config.getHologramLineFormat();
        this.hologramFooter = config.getHologramFooter();
        this.spaceBetweenLines = config.getSpaceBetweenLines();
    }

    private HologramLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.HOLOGRAM, name);
        this.hologramLoading = getConfiguration().getString("override.loading", config.getHologramLoading());
        this.hologramNoData = getConfiguration().getString("override.no-data", config.getHologramNoData());
        this.hologramHeader = getConfiguration().getString("override.header", config.getHologramHeader());
        this.hologramLineFormat = getConfiguration().getString("override.line-format", config.getHologramLineFormat());
        this.hologramFooter = getConfiguration().getString("override.footer", config.getHologramFooter());
        this.spaceBetweenLines = getConfiguration().getDouble("override.space-between-lines", config.getSpaceBetweenLines());
        getLocation().getWorld().getNearbyEntities(getLocation(), 0.5, 10, 0.5).forEach(entity -> {
            if (entity instanceof ArmorStand && !((ArmorStand) entity).isVisible() && entity.isCustomNameVisible()) {
                entity.remove();
            }
        });
        showLine(0, getLocation(), StringUtil.color(hologramLoading));
    }

    @Override
    public void update(final TopEntry entry) {
        if (entry == null) {
            return;
        }

        final List<Pair<String, Integer>> data = entry.getData();
        final Location location = getLocation().clone();

        if (data.isEmpty()) {
            showLine(0, location, StringUtil.color(hologramNoData));
            return;
        }

        final double space = 0.23 + spaceBetweenLines;
        showLine(0, location, StringUtil.color(hologramHeader.replace("%type%", entry.getType())));

        for (int i = 0; i < data.size(); i++) {
            final Pair<String, Integer> pair = data.get(i);
            showLine(i + 1, location.subtract(0, space, 0), StringUtil.color(hologramLineFormat
                .replace("%rank%", String.valueOf(i + 1)).replace("%name%", pair.getKey())
                .replace("%value%", String.valueOf(pair.getValue())).replace("%identifier%", entry.getIdentifier())));
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
            lines.add(armorStand);
        } else {
            armorStand = lines.get(index);
        }

        armorStand.setCustomName(text);
    }

    @Override
    public void save() {
        super.save();
        lines.forEach(Entity::remove);
    }

    public static HologramLeaderboard from(final Leaderboards extension, final String name, final File file) throws IllegalArgumentException {
        return new HologramLeaderboard(extension, file, name);
    }
}
