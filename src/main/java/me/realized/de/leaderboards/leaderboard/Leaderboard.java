package me.realized.de.leaderboards.leaderboard;

import lombok.NonNull;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.Location;

public interface Leaderboard {

    @NonNull
    String getName();

    @NonNull
    LeaderboardType getType();

    @NonNull
    String getDataType();

    @NonNull
    Location getLocation();

    void teleport(final Location location);

    void update(final TopEntry entry);

    void remove();

    void save();
}
