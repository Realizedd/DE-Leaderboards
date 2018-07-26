package me.realized.de.leaderboards.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public final class BlockUtil {

    private BlockUtil() {}

    public static <T extends BlockState> T getTargetBlock(final Player player, final Class<T> type, final int range) {
        final BlockIterator iterator = new BlockIterator(player, range);

        while (iterator.hasNext()) {
            final Block block = iterator.next();

            if (type.isInstance(block.getState())) {
                return type.cast(block.getState());
            }
        }

        return null;
    }

    public static void clear(final Sign sign) {
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, "");
        }
    }

    public static <T extends BlockState> List<T> getBlocksBelow(BlockState block, final Class<T> type, final int limit) {
        final List<T> result = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            final BlockState below = block.getBlock().getRelative(BlockFace.DOWN).getState();

            if (type.isInstance(below)) {
                result.add(type.cast(below));
            }

            block = below;
        }

        return result;
    }
}
