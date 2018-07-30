package me.realized.de.leaderboards.leaderboard;

import javax.annotation.Nonnull;
import org.bukkit.Location;

public interface Leaderboard {

    @Nonnull
    String getName();

    @Nonnull
    LeaderboardType getType();

    @Nonnull
    String getDataType();

    @Nonnull
    Location getLocation();

    void teleport(final Location location);

    void remove();

    void save();
}
