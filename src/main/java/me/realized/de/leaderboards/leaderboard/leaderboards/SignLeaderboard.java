package me.realized.de.leaderboards.leaderboard.leaderboards;

import lombok.NonNull;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class SignLeaderboard extends AbstractLeaderboard {

    public SignLeaderboard(final Leaderboards extension, final String name, final Location location) {
        super(extension, LeaderboardType.SIGN, name, location);
    }

    @Override
    public void save(final ConfigurationSection section) {

    }

    public static SignLeaderboard from(@NonNull final ConfigurationSection section) throws IllegalArgumentException {
        if (section == null) {
            throw new IllegalArgumentException("section is null");
        }


        // throw illegal if location is null or isnt a sign or something...
    }
}
