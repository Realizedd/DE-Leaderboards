package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Lang;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.Leaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.SignLeaderboard;
import me.realized.de.leaderboards.util.BlockUtil;
import me.realized.de.leaderboards.util.CompatUtil;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.kit.Kit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends LBCommand {

    public CreateCommand(final Leaderboards extension) {
        super(extension, "create", "create [hologram|head|sign] [name] [wins|losses|kit]", "Creates a leaderboard with type and name.", 5, true);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final LeaderboardType type = EnumUtil.getByName(args[2].toUpperCase(), LeaderboardType.class);

        if (type == null) {
            Lang.INVALID_LEADERBOARD_TYPE.sendTo(sender, args[2], EnumUtil.getNames(LeaderboardType.class));
            return;
        }

        if (CompatUtil.isPre1_8() && type == LeaderboardType.HOLOGRAM) {
            Lang.UNSUPPORTED.sendTo(sender);
            return;
        }

        final Player player = (Player) sender;
        final String name = args[3].toLowerCase();
        final String dataType = StringUtils.join(args, " ", 4, args.length).replace("-", " ");

        if (!dataType.equalsIgnoreCase("wins") && !dataType.equalsIgnoreCase("losses")) {
            final Kit kit = extension.getKitManager().get(dataType);

            if (kit == null) {
                Lang.KIT_NOT_FOUND.sendTo(sender, dataType);
                return;
            }
        }

        final AbstractLeaderboard leaderboard;
        final Location location;
        final Sign sign;

        switch (type) {
            case SIGN:
                sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

                if (sign == null) {
                    Lang.NOT_LOOKING_AT_SIGN.sendTo(sender);
                    return;
                }

                leaderboard = new SignLeaderboard(extension, api, name, dataType, location = sign.getLocation().clone());
                break;
            case HOLOGRAM:
                location = player.getLocation().clone();
                leaderboard = new HologramLeaderboard(extension, api, name, dataType, location);
                break;
            case HEAD:
                sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

                if (sign == null || !((org.bukkit.material.Sign) sign.getData()).isWallSign()) {
                    Lang.NOT_LOOKING_AT_WALL_SIGN.sendTo(sender);
                    return;
                }

                leaderboard = new HeadLeaderboard(extension, api, name, dataType, location = sign.getLocation().clone(), 1);
                break;
            default:
                return;
        }

        final Leaderboard found = leaderboardManager.get(location.getBlock());

        if (found != null) {
            Lang.ALREADY_EXISTS_LOCATION.sendTo(sender, found.getName(), found.getType().name(), StringUtil.from(location));
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
