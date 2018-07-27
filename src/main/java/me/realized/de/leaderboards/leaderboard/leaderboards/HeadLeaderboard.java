package me.realized.de.leaderboards.leaderboard.leaderboards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.CompatUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.user.UserManager.TopData;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.Bukkit;
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

    @Setter
    private int rank;
    private final String headLoading;
    private final String headNoData;
    private final List<String> headSignFormat;

    public HeadLeaderboard(final Leaderboards extension, final String name, final String dataType, final Location location, final int rank) {
        super(extension, LeaderboardType.HEAD, name, dataType, location);
        this.rank = rank;
        this.headLoading = config.getHeadLoading();
        this.headNoData = config.getHeadNoData();
        this.headSignFormat = config.getHeadSignFormat();
    }

    private HeadLeaderboard(final Leaderboards extension, final File file, final String name) {
        super(extension, file, LeaderboardType.HEAD, name);
        this.rank = getConfiguration().getInt("rank", 1);
        this.headLoading = getConfiguration().getString("override.loading", config.getHeadLoading());
        this.headNoData = getConfiguration().getString("override.no-data", config.getHeadNoData());
        this.headSignFormat = getConfiguration().isList("override.sign-format") ? getConfiguration().getStringList("override.sign-format") : config.getHeadSignFormat();

        final Block block = getLocation().getBlock();

        if (block.getType() != Material.WALL_SIGN) {
            extension.warn(String.format(NO_SIGN, name, getType().name(), StringUtil.from(getLocation())));
            return;
        }

        final Sign sign = (Sign) block.getState();
        BlockUtil.clear(sign);
        sign.setLine(0, StringUtil.color(headLoading));
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
        // If null, top is loading
        if (entry == null) {
            return;
        }

        final Block block = getLocation().getBlock();

        if (block.getType() != Material.WALL_SIGN) {
            return;
        }

        final Sign sign = (Sign) block.getState();
        final List<TopData> data = entry.getData();

        if (data.isEmpty()) {
            BlockUtil.clear(sign);
            sign.setLine(0, StringUtil.color(headNoData));
            sign.update(true);
            return;
        }

        if (data.size() <= rank) {
            return;
        }

        final org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) sign.getData();
        final TopData topData = data.get(rank - 1);
        final List<BlockState> blockStates = new ArrayList<>();
        final Block skullBlock = block.getRelative(materialSign.getAttachedFace()).getRelative(BlockFace.UP);

        if (skullBlock.getState() instanceof Skull) {
            final Skull skull = (Skull) skullBlock.getState();

            if (CompatUtil.isPre_1_10()) {
                if (skull.getOwner() == null || !topData.getName().equals(skull.getOwner())) {
                    skull.setOwner(topData.getName());
                    blockStates.add(skull);
                }
            } else {
                if (skull.getOwningPlayer() == null || !topData.getUuid().equals(skull.getOwningPlayer().getUniqueId())) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(topData.getUuid()));
                    System.out.println(Bukkit.getOfflinePlayer(topData.getUuid()));
                    blockStates.add(skull);
                }
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

        if (block.getType() != Material.WALL_SIGN) {
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

    public static HeadLeaderboard from(final Leaderboards extension, final String name, final File file) throws IllegalArgumentException {
        return new HeadLeaderboard(extension, file, name);
    }
}

