package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Lang;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.leaderboard.Leaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.duels.api.Duels;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCommand extends LBCommand {

    public TpCommand(final Leaderboards extension, final Duels api) {
        super(extension, api, "tp", "tp [type] [name]", "Teleports to the leaderboard with name and type.", 4, true);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final LeaderboardType type = EnumUtil.getByName(args[2].toUpperCase(), LeaderboardType.class);

        if (type == null) {
            Lang.INVALID_LEADERBOARD_TYPE.sendTo(sender, args[2], EnumUtil.getNames(LeaderboardType.class));
            return;
        }

        final Leaderboard leaderboard = leaderboardManager.get(type, args[3]);

        if (leaderboard == null) {
            Lang.LB_NOT_FOUND.sendTo(sender, args[3], type.name());
            return;
        }

        ((Player) sender).teleport(leaderboard.getLocation());
        Lang.TELEPORT.sendTo(sender, leaderboard.getName(), leaderboard.getType().name());
    }
}
