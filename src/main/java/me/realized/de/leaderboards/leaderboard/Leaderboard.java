package me.realized.de.leaderboards.leaderboard;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public interface Leaderboard {

    @NonNull
    String getName();

    @NonNull
    LeaderboardType getType();

    @NonNull
    Location getLocation();

    void save(final ConfigurationSection section);
}
