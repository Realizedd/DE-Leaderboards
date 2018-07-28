package me.realized.de.leaderboards.command.commands;

import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.config.Lang;
import me.realized.de.leaderboards.leaderboard.Leaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TphereCommand extends LBCommand {

    public TphereCommand(final Leaderboards extension) {
        super(extension, "tphere", "tphere [name]", "Teleports the Hologram Leaderboard with given name to your location.", 3, true);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final Leaderboard leaderboard = leaderboardManager.get(LeaderboardType.HOLOGRAM, args[2]);

        if (leaderboard == null) {
            Lang.LB_NOT_FOUND.sendTo(sender, args[2], LeaderboardType.HOLOGRAM.name());
            return;
        }

        leaderboard.teleport(((Player) sender).getLocation());
        Lang.TELEPORT_HERE.sendTo(sender, leaderboard.getName());
    }
}
