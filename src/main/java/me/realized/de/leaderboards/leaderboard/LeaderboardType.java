package me.realized.de.leaderboards.leaderboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.SignLeaderboard;
import me.realized.de.leaderboards.util.ReflectionUtil;
import org.bukkit.configuration.ConfigurationSection;

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
        this.from = ReflectionUtil.getMethod(type, "from", ConfigurationSection.class);
    }

    public Leaderboard from(@NonNull final ConfigurationSection section) {
        Objects.requireNonNull(section, "section");

        try {
            return type.cast(from.invoke(null, section));
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static LeaderboardType get(final String name) {
        return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
