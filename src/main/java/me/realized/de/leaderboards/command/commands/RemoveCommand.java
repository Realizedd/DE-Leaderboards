package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.config.Lang;
import me.realized.de.leaderboards.leaderboard.Leaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.de.leaderboards.util.StringUtil;
import org.bukkit.command.CommandSender;

public class RemoveCommand extends LBCommand {

    public RemoveCommand(final Leaderboards extension) {
        super(extension, "remove", "remove [hologram|head|sign] [name]", 4, false);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final LeaderboardType type = EnumUtil.getByName(args[2].toUpperCase(), LeaderboardType.class);

        if (type == null) {
            Lang.INVALID_LEADERBOARD_TYPE.sendTo(sender, args[2], EnumUtil.getNames(LeaderboardType.class));
            return;
        }

        final Leaderboard leaderboard = leaderboardManager.remove(type, args[3]);

        if (leaderboard == null) {
            Lang.LB_NOT_FOUND.sendTo(sender, args[3], type.name());
            return;
        }

        leaderboard.remove();
        Lang.REMOVE_LEADERBOARD.sendTo(sender, leaderboard.getName(), type.name(), StringUtil.from(leaderboard.getLocation()));
    }
}
