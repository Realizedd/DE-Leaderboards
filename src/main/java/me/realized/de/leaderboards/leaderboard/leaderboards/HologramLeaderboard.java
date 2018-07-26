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

public class HologramLeaderboard extends AbstractLeaderboard {

    private static final double LINE_SPACING = 0.05;

    private final List<ArmorStand> lines = new ArrayList<>();

    public HologramLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location) {
        super(extension, LeaderboardType.HOLOGRAM, name, dataType, location);
    }

    private HologramLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.HOLOGRAM, name);
        getLocation().getWorld().getNearbyEntities(getLocation(), 0.5, 5, 0.5).forEach(entity -> {
            if (entity instanceof ArmorStand && ((ArmorStand) entity).isVisible() && entity.isCustomNameVisible()) {
                entity.remove();
            }
        });
        showLine(0, getLocation(), StringUtil.color("&cLeaderboard is loading..."));
    }

    @Override
    public void update(final TopEntry entry) {
        if (entry == null) {
            return;
        }

        final List<Pair<String, Integer>> data = entry.getData();
        final Location location = getLocation().clone();
        final double space = 0.23 + LINE_SPACING;
        showLine(0, location, StringUtil.color(extension.getHologramHeader().replace("%type%", entry.getType())));

        for (int i = 0; i < data.size(); i++) {
            final Pair<String, Integer> pair = data.get(i);
            showLine(i + 1, location.subtract(0, space, 0), StringUtil.color(extension.getHologramLineFormat()
                .replace("%rank%", String.valueOf(i + 1)).replace("%name%", pair.getKey())
                .replace("%value%", String.valueOf(pair.getValue())).replace("%identifier%", entry.getIdentifier())));
        }

        showLine(data.size() + 1, location.subtract(0, space, 0), StringUtil.color(extension.getHologramFooter().replace("%type%", entry.getType())));
    }

    private void showLine(final int index, final Location location, final String text) {
        ArmorStand armorStand;

        if (lines.size() <= index) {
            armorStand = location.getWorld().spawn(location, ArmorStand.class);
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
