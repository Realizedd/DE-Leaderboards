package me.realized.de.leaderboards.leaderboard.leaderboards;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;

public class HeadLeaderboard extends AbstractLeaderboard {

    public HeadLeaderboard(final Leaderboards extension, final String name) {
        super(extension, name, LeaderboardType.HEAD);
        // Don't load if location is not sign or block behind sign is air or there's no head above
    }
}
