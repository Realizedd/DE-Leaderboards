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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

public class HeadLeaderboard extends AbstractLeaderboard {

    private static final String NO_SIGN = "Leaderboard \'%s\' (type %s) requires a sign attached to block, but found no wall sign at %s.";
    private static final String NO_HEAD = "Leaderboard \'%s\' (type %s) requires a head placed above attached block, but found no head at %s.";

    private final int position;

    public HeadLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location, final int position) {
        super(extension, LeaderboardType.HEAD, name, dataType, location);
        this.position = position;
    }

    private HeadLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.HEAD, name);
        this.position = getConfiguration().getInt("position");

        final Block block = getLocation().getBlock();

        if (block.getType() != Material.WALL_SIGN) {
            extension.warn(String.format(NO_SIGN, name, getType().name(), StringUtil.from(getLocation())));
            return;
        }

        final Sign sign = (Sign) block.getState();
        BlockUtil.clear(sign);
        sign.setLine(0, StringUtil.color("&cLoading..."));
        sign.update(true);

        final org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) block.getState().getData();
        final Block skullBlock = block.getRelative(materialSign.getAttachedFace()).getRelative(BlockFace.UP);

        if (skullBlock.getState() instanceof Skull) {
            return;
        }

        extension.warn(String.format(NO_HEAD, name, getType().name(), StringUtil.from(skullBlock.getLocation())));
    }

    @Override
    public void update(final TopEntry entry) {
        if (entry == null) {
            return;
        }

        final List<Pair<String, Integer>> data = entry.getData();

        if (data.size() <= position) {
            return;
        }

        final Block block = getLocation().getBlock();

        if (block.getType() != Material.WALL_SIGN) {
            return;
        }

        final Sign sign = (Sign) block.getState();
        final org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) sign.getData();
        final Block skullBlock = block.getRelative(materialSign.getAttachedFace()).getRelative(BlockFace.UP);

        if (!(skullBlock.getState() instanceof Skull)) {
            return;
        }

        final Skull skull = (Skull) skullBlock.getState();

        final Pair<String, Integer> pair = data.get(position - 1);
        final List<BlockState> blockStates = new ArrayList<>();

        if (!pair.getKey().equals(skull.getOwner())) {
            skull.setOwner(pair.getKey());
            blockStates.add(skull);
        }

        final List<String> format = new ArrayList<>(extension.getHeadSignFormat());
        format.replaceAll(s -> s = StringUtil.color(s
            .replace("%rank%", String.valueOf(position)).replace("%name%", pair.getKey())
            .replace("%value%", String.valueOf(pair.getValue())).replace("%identifier%", entry.getIdentifier())
            .replace("%type%", entry.getType())));

        for (int i = 0; i < (format.size() > 4 ? 4 : format.size()); i++) {
            sign.setLine(i, format.get(i));
        }

        blockStates.add(sign);
        blockStates.forEach(blockState -> blockState.update(true));
    }

    @Override
    public void save() {
        getConfiguration().set("position", position);
        super.save();
    }

    public static HeadLeaderboard from(final Leaderboards extension, final String name, final File file) throws IllegalArgumentException {
        return new HeadLeaderboard(extension, file, name);
    }
}

