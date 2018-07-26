package me.realized.de.leaderboards.leaderboard.leaderboards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.user.UserManager.TopEntry;
import me.realized.duels.api.util.Pair;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class SignLeaderboard extends AbstractLeaderboard {

    private static final String NO_SIGN = "Leaderboard \'%s\' (type %s) requires a sign, but found no sign at %s.";

    public SignLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location) {
        super(extension, LeaderboardType.SIGN, name, dataType, location);
    }

    private SignLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.SIGN, name);

        final Block block = getLocation().getBlock();

        if (block.getState() instanceof Sign) {
            final List<Sign> signs = new ArrayList<>();
            Sign sign = (Sign) block.getState();
            signs.add(sign);
            signs.addAll(BlockUtil.getBlocksBelow(sign, Sign.class, 2));

            for (int i = 0; i < signs.size(); i++) {
                sign = signs.get(i);
                BlockUtil.clear(sign);

                if (i == 0) {
                    sign.setLine(0, StringUtil.color("&cLoading..."));
                }

                sign.update(true);
            }
            return;
        }

        extension.warn(String.format(NO_SIGN, name, getType().name(), StringUtil.from(getLocation())));
    }

    @Override
    public void update(final TopEntry entry) {
        if (entry == null) {
            return;
        }

        final Block block = getLocation().getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final List<Pair<String, Integer>> data = entry.getData();
        final List<Sign> signs = new ArrayList<>();
        Sign sign = (Sign) block.getState();
        signs.add(sign);

        int line = 1;
        sign.setLine(0, StringUtil.color(extension.getSignHeader().replace("%type%", entry.getType())));

        if (extension.isSignSpaceBetween()) {
            sign.setLine(1, " ");
            line++;
        }

        for (int i = 0; i < data.size(); i++) {
            final Pair<String, Integer> pair = data.get(i);
            final String text = StringUtil.color(extension.getSignLineFormat()
                .replace("%rank%", String.valueOf(i + 1)).replace("%name%", pair.getKey())
                .replace("%value%", String.valueOf(pair.getValue())).replace("%identifier%", entry.getIdentifier()));
            sign.setLine(line, text.length() > 15 ? text.substring(0, 15) : text);
            line++;

            if (line > 3) {
                final Block below = sign.getBlock().getRelative(BlockFace.DOWN);

                if (!(below.getState() instanceof Sign)) {
                    break;
                }

                sign = (Sign) below.getState();
                signs.add(sign);
                line = 0;
            }
        }

        signs.forEach(state -> state.update(true));
    }

    public static SignLeaderboard from(final Leaderboards extension, final String name, final File file) throws IllegalArgumentException {
        return new SignLeaderboard(extension, file, name);
    }
}
