package me.realized.de.leaderboards.leaderboard.leaderboards;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;

public class HologramLeaderboard extends AbstractLeaderboard {

    public HologramLeaderboard(final Leaderboards extension, final String name) {
        super(extension, name, LeaderboardType.HOLOGRAM);
    }
}
