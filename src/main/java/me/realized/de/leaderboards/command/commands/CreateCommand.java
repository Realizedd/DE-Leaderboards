package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.leaderboard.Leaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.leaderboard.leaderboards.HeadLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.HologramLeaderboard;
import me.realized.de.leaderboards.leaderboard.leaderboards.SignLeaderboard;
import me.realized.de.leaderboards.util.BlockUtil;
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
        final LeaderboardType type = LeaderboardType.get(args[2].toUpperCase());

        if (type == null) {
            sender.sendMessage("invalid type");
            return;
        }

        final Player player = (Player) sender;
        final Location location;

        if (type == LeaderboardType.HOLOGRAM) {
            location = player.getLocation().clone();
        } else {
            final Sign sign = BlockUtil.getTargetBlock(player, Sign.class, 6);

            if (sign == null) {
                sender.sendMessage("you must be looking at a sign");
                return;
            }

            location = sign.getLocation().clone();
        }

        final Leaderboard leaderboard;

        switch (type) {
            case SIGN:
                leaderboard = new SignLeaderboard(extension, args[3], args[4], location);
                break;
            case HOLOGRAM:
                leaderboard = new HologramLeaderboard(extension, args[3], args[4], location);
                break;
            case HEAD:
                leaderboard = new HeadLeaderboard(extension, args[3], args[4], location, 1);
                // Send extra message to do /duels lb setpos [position]
                break;
            default:
                return;
        }

        leaderboardManager.addLeaderboard(leaderboard);
    }
}
