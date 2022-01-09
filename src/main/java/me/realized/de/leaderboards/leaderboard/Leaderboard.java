package me.realized.de.leaderboards.leaderboard;

import org.bukkit.Location;

public interface Leaderboard {

    String getName();

    LeaderboardType getType();

    String getDataType();

    Location getLocation();

    void teleport(final Location location);

    void remove();

    void save();
}
