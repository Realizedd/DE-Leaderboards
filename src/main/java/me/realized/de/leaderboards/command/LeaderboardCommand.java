package me.realized.de.leaderboards.command;

import java.util.LinkedHashMap;
import java.util.Map;
import me.realized.de.leaderboards.Lang;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.commands.CreateCommand;
import me.realized.de.leaderboards.command.commands.ListCommand;
import me.realized.de.leaderboards.command.commands.RemoveCommand;
import me.realized.de.leaderboards.command.commands.SetrankCommand;
import me.realized.de.leaderboards.command.commands.TpCommand;
import me.realized.de.leaderboards.command.commands.TphereCommand;
import me.realized.duels.api.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaderboardCommand extends SubCommand {

    private final Map<String, LBCommand> commands = new LinkedHashMap<>();

    public LeaderboardCommand(final Leaderboards extension) {
        super("leaderboard", null, null, null, false, 1, "lb");
        register(
            new CreateCommand(extension),
            new SetrankCommand(extension),
            new RemoveCommand(extension),
            new ListCommand(extension),
            new TpCommand(extension),
            new TphereCommand(extension)
        );
    }

    private void register(final LBCommand... commands) {
        for (final LBCommand command : commands) {
            this.commands.put(command.getName(), command);
        }
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        if (args.length == getLength()) {
            Lang.HELP_HEADER.sendTo(sender);
            commands.values().forEach(command -> Lang.HELP_FORMAT.sendTo(sender, label + " " + args[0] + " " + command.getUsage(), command.getDescription()));
            Lang.HELP_FOOTER.sendTo(sender);
            return;
        }

        final LBCommand command = commands.get(args[1].toLowerCase());

        if (command != null) {
            if (command.isPlayerOnly() && !(sender instanceof Player)) {
                Lang.PLAYER_ONLY.sendTo(sender);
                return;
            }

            if (args.length < command.getLength()) {
                Lang.USAGE_FORMAT.sendTo(sender, label + " " + args[0] + " " + command.getUsage(), command.getDescription());
                return;
            }

            command.execute(sender, label, args);
            return;
        }

        Lang.INVALID_COMMAND.sendTo(sender, args[1], label + " " + args[0]);
    }
}
