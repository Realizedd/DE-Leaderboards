package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.config.Lang;
import me.realized.de.leaderboards.leaderboard.Leaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.SignLeaderboard;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.de.leaderboards.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends LBCommand {

    public CreateCommand(final Leaderboards extension) {
        super(extension, "create", "create [hologram|head|sign] [name] [wins|losses|kit]", 5, true);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final LeaderboardType type = EnumUtil.getByName(args[2].toUpperCase(), LeaderboardType.class);

        if (type == null) {
            Lang.INVALID_LEADERBOARD_TYPE.sendTo(sender, args[2], EnumUtil.getNames(LeaderboardType.class));
            return;
        }

        final Player player = (Player) sender;
        final String name = args[3].toLowerCase();
        final Leaderboard leaderboard;
        final Location location;
        final Sign sign;

        switch (type) {
            case SIGN:
                sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

                if (sign == null) {
                    Lang.NOT_LOOKING_AT_SIGN.sendTo(sender);
                    return;
                }

                leaderboard = new SignLeaderboard(extension, name, args[4], location = sign.getLocation().clone());
                break;
            case HOLOGRAM:
                location = player.getLocation().clone();
                leaderboard = new HologramLeaderboard(extension, name, args[4], location);
                break;
            case HEAD:
                sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

                if (sign == null || !((org.bukkit.material.Sign) sign.getData()).isWallSign()) {
                    Lang.NOT_LOOKING_AT_WALL_SIGN.sendTo(sender);
                    return;
                }

                leaderboard = new HeadLeaderboard(extension, name, args[4], location = sign.getLocation().clone(), 1);
                break;
            default:
                return;
        }

        if (!leaderboardManager.addLeaderboard(leaderboard)) {
            Lang.ALREADY_EXISTS.sendTo(sender, name, type.name());
            return;
        }

        Lang.CREATE_LEADERBOARD.sendTo(sender, name, type.name(), StringUtil.from(location));

        if (type == LeaderboardType.HEAD) {
            Lang.SET_RANK_INFO.sendTo(sender);
        }
    }
}
