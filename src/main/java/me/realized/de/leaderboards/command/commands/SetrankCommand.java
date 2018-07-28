package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.config.Lang;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.NumberUtil;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetrankCommand extends LBCommand {

    public SetrankCommand(final Leaderboards extension) {
        super(extension, "setrank", "setrank [rank]", "Sets the rank of a Head Leaderboard.", 3, true);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Sign sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

        if (sign == null || !((org.bukkit.material.Sign) sign.getData()).isWallSign()) {
            Lang.NOT_LOOKING_AT_WALL_SIGN.sendTo(sender);
            return;
        }

        final HeadLeaderboard leaderboard = leaderboardManager.get(sign);

        if (leaderboard == null) {
            Lang.HEAD_LB_NOT_FOUND.sendTo(sender);
            return;
        }

        final int rank = Math.min(Math.max(1, NumberUtil.parseInt(args[2]).orElse(1)), 10);
        leaderboard.setRank(NumberUtil.parseInt(args[2]).orElse(1));
        Lang.SET_RANK.sendTo(sender, leaderboard.getName(), rank);
    }
}
