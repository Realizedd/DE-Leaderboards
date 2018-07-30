package me.realized.de.leaderboards.leaderboard.leaderboards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.user.UserManager.TopData;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class SignLeaderboard extends AbstractLeaderboard {

    private static final String NO_SIGN = "Leaderboard \'%s\' (type %s) requires a sign, but found no sign at %s.";

    private final String signLoading;
    private final String signNoData;
    private final String signHeader;
    private final boolean signSpaceBetween;
    private final String signLineFormat;

    public SignLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location) {
        super(extension, LeaderboardType.SIGN, name, dataType, location);
        this.signLoading = config.getSignLoading();
        this.signNoData = config.getSignNoData();
        this.signHeader = config.getSignHeader();
        this.signSpaceBetween = config.isSignSpaceBetween();
        this.signLineFormat = config.getSignLineFormat();
    }

    private SignLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.SIGN, name);
        this.signLoading = getConfiguration().getString("override.loading", config.getSignLoading());
        this.signNoData = getConfiguration().getString("override.no-data", config.getSignNoData());
        this.signHeader = getConfiguration().getString("override.header", config.getSignHeader());
        this.signSpaceBetween = getConfiguration().getBoolean("override.space-between", config.isSignSpaceBetween());
        this.signLineFormat = getConfiguration().getString("override.sign-line-format", config.getSignLineFormat());

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
                    sign.setLine(0, StringUtil.color(signLoading));
                }

                sign.update(true);
            }
            return;
        }

        extension.warn(String.format(NO_SIGN, name, getType().name(), StringUtil.from(getLocation())));
    }

    @Override
    protected void onUpdate(final TopEntry entry) {
        final Block block = getLocation().getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final List<TopData> data = entry.getData();
        Sign sign = (Sign) block.getState();

        if (data.isEmpty()) {
            BlockUtil.clear(sign);
            sign.setLine(0, StringUtil.color(signNoData));
            sign.update(true);
            return;
        }

        final List<Sign> signs = new ArrayList<>();
        signs.add(sign);

        int line = 1;
        sign.setLine(0, StringUtil.color(signHeader.replace("%type%", entry.getType())));

        if (signSpaceBetween) {
            sign.setLine(1, " ");
            line++;
        }

        for (int i = 0; i < data.size(); i++) {
            final TopData topData = data.get(i);
            final String text = StringUtil.color(signLineFormat
                .replace("%rank%", String.valueOf(i + 1)).replace("%name%", topData.getName())
                .replace("%value%", String.valueOf(topData.getValue())).replace("%identifier%", entry.getIdentifier()));
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

    @Override
    public void onRemove() {
        final Block block = getLocation().getBlock();

        if (block.getState() instanceof Sign) {
            final List<Sign> signs = new ArrayList<>();
            signs.add((Sign) block.getState());
            signs.addAll(BlockUtil.getBlocksBelow(block.getState(), Sign.class, 2));
            signs.forEach(sign -> {
                BlockUtil.clear(sign);
                sign.update(true);
            });
        }
    }

    public static SignLeaderboard from(final Leaderboards extension, final String name, final File file) throws IllegalArgumentException {
        return new SignLeaderboard(extension, file, name);
    }
}
