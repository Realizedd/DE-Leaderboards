package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.Getter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.SignLeaderboard;
import me.realized.de.leaderboards.util.ReflectionUtil;
import me.realized.duels.api.Duels;

public enum LeaderboardType {

    HEAD(HeadLeaderboard.class),
    SIGN(SignLeaderboard.class),
    HOLOGRAM(HologramLeaderboard.class);

    @Getter
    private final Class<? extends AbstractLeaderboard> type;
    private final Method from;

    LeaderboardType(final Class<? extends AbstractLeaderboard> type) {
        Objects.requireNonNull(type, "type");
        this.type = type;
        this.from = ReflectionUtil.getMethod(type, "from", Leaderboards.class, Duels.class, String.class, File.class);
    }

    public AbstractLeaderboard from(final Leaderboards extension, final Duels api, final String name, final File file)
        throws InvocationTargetException, IllegalAccessException {
        Objects.requireNonNull(extension, "extension");
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(file, "file");
        return type.cast(from.invoke(null, extension, api, name, file));
    }
}
