package me.realized.de.leaderboards.leaderboard.leaderboards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.Duels;
import me.realized.duels.api.user.UserManager.TopData;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

public class HeadLeaderboard extends AbstractLeaderboard {

    private static final String NO_SIGN = "Leaderboard \'%s\' (type %s) requires a sign attached to block, but found no wall sign at %s.";
    private static final String NO_HEAD = "Leaderboard \'%s\' (type %s) requires a head placed above attached block, but found no head at %s.";

    private int rank;
    private final String headLoading;
    private final String headNoData;
    private final List<String> headSignFormat;

    public HeadLeaderboard(final Leaderboards extension, final Duels api, final String name, final String dataType, final Location location, final int rank) {
        super(extension, api, LeaderboardType.HEAD, name, dataType, location);
        this.rank = rank;
        this.headLoading = config.getHeadLoading();
        this.headNoData = config.getHeadNoData();
        this.headSignFormat = config.getHeadSignFormat();
    }

    private HeadLeaderboard(final Leaderboards extension, final Duels api, final File file, final String name) {
        super(extension, api, file, LeaderboardType.HEAD, name);
        this.rank = getConfiguration().getInt("rank", 1);
        this.headLoading = getConfiguration().getString("override.loading", config.getHeadLoading());
        this.headNoData = getConfiguration().getString("override.no-data", config.getHeadNoData());
        this.headSignFormat = getConfiguration().isList("override.sign-format") ? getConfiguration().getStringList("override.sign-format") : config.getHeadSignFormat();

        final Block block = getLocation().getBlock();

        if (!BlockUtil.isWallSign(block.getType())) {
            extension.warn(String.format(NO_SIGN, name, getType().name(), StringUtil.from(getLocation())));
            return;
        }

        final Sign sign = (Sign) block.getState();
        BlockUtil.clear(sign);
        sign.setLine(0, StringUtil.color(headLoading));
        sign.update(true);

        final Block skullBlock = block.getRelative(BlockUtil.getFacing(sign)).getRelative(BlockFace.UP);

        if (skullBlock.getState() instanceof Skull) {
            return;
        }

        extension.warn(String.format(NO_HEAD, name, getType().name(), StringUtil.from(skullBlock.getLocation())));
    }

    public void setRank(final int rank) {
        this.rank = rank;
        setChanged(true);
    }

    @Override
    protected void onUpdate(final TopEntry entry) {
        final Block block = getLocation().getBlock();

        if (!BlockUtil.isWallSign(block.getType())) {
            return;
        }

        final Sign sign = (Sign) block.getState();
        final List<TopData> data = entry.getData();

        if (data.isEmpty() || data.size() < rank) {
            BlockUtil.clear(sign);
            sign.setLine(0, StringUtil.color(headNoData));
            sign.update(true);
            return;
        }

        final TopData topData = data.get(rank - 1);
        final List<BlockState> blockStates = new ArrayList<>();
        final Block skullBlock = block.getRelative(BlockUtil.getFacing(sign)).getRelative(BlockFace.UP);

        if (skullBlock.getState() instanceof Skull) {
            final Skull skull = (Skull) skullBlock.getState();

            if (skull.getOwner() == null || !topData.getName().equals(skull.getOwner())) {
                skull.setOwner(topData.getName());
                blockStates.add(skull);
            }
        }

        final List<String> format = new ArrayList<>(headSignFormat);
        format.replaceAll(s -> s = StringUtil.color(s
            .replace("%rank%", String.valueOf(rank)).replace("%name%", topData.getName())
            .replace("%value%", String.valueOf(topData.getValue())).replace("%identifier%", entry.getIdentifier())
            .replace("%type%", entry.getType())));

        for (int i = 0; i < (format.size() > 4 ? 4 : format.size()); i++) {
            sign.setLine(i, format.get(i));
        }

        blockStates.add(sign);
        blockStates.forEach(blockState -> blockState.update(true));
    }

    @Override
    public void onRemove() {
        final Block block = getLocation().getBlock();

        if (!BlockUtil.isWallSign(block.getType())) {
            return;
        }

        final Sign sign = (Sign) block.getState();
        BlockUtil.clear(sign);
        sign.update(true);
    }

    @Override
    public void save() {
        getConfiguration().set("rank", rank);
        super.save();
    }

    public static HeadLeaderboard from(final Leaderboards extension, final Duels api, final String name, final File file) throws IllegalArgumentException {
        return new HeadLeaderboard(extension, api, file, name);
    }
}

