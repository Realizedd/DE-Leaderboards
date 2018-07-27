package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.SignLeaderboard;
import me.realized.de.leaderboards.util.ReflectionUtil;

public enum LeaderboardType {

    HEAD(HeadLeaderboard.class),
    SIGN(SignLeaderboard.class),
    HOLOGRAM(HologramLeaderboard.class);

    @Getter
    private final Class<? extends AbstractLeaderboard> type;
    private final Method from;

    LeaderboardType(@NonNull final Class<? extends AbstractLeaderboard> type) {
        Objects.requireNonNull(type, "type");
        this.type = type;
        this.from = ReflectionUtil.getMethod(type, "from", Leaderboards.class, String.class, File.class);
    }

    public Leaderboard from(@NonNull final Leaderboards extension, @NonNull final String name, @NonNull final File file) {
        Objects.requireNonNull(extension, "extension");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(file, "file");

        try {
            return type.cast(from.invoke(null, extension, name, file));
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
