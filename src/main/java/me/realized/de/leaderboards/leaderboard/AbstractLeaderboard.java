package me.realized.de.leaderboards.leaderboard;

import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import me.realized.de.leaderboards.Leaderboards;
import org.bukkit.Location;

public abstract class AbstractLeaderboard implements Leaderboard {

    protected final Leaderboards extension;

    @Getter
    private final String name;
    @Getter
    private final LeaderboardType type;
    @Getter
    private final Location location;

    public AbstractLeaderboard(@NonNull final Leaderboards extension, @NonNull final LeaderboardType type, @NonNull final String name, @NonNull final Location location) {
        Objects.requireNonNull(extension, "extension");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(location, "location");
        this.extension = extension;
        this.type = type;
        this.name = name;
        this.location = location;
    }
}
