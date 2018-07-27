package me.realized.de.leaderboards.command;

import java.util.LinkedHashMap;
import java.util.Map;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.commands.CreateCommand;
import me.realized.de.leaderboards.command.commands.ListCommand;
import me.realized.de.leaderboards.command.commands.RemoveCommand;
import me.realized.de.leaderboards.command.commands.SetrankCommand;
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
            new ListCommand(extension)
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
            sender.sendMessage("---------------------------------");
            commands.values().forEach(command -> sender.sendMessage("/" + label + " "  + args[0] + " " + command.getUsage()));
            sender.sendMessage("---------------------------------");
            return;
        }

        final LBCommand command = commands.get(args[1].toLowerCase());

        if (command != null) {
            if (command.isPlayerOnly() && !(sender instanceof Player)) {
                sender.sendMessage("Player only command");
                return;
            }

            if (args.length < command.getLength()) {
                sender.sendMessage("Usage: /" + label + " "  + args[0] + " " + command.getUsage());
                return;
            }

            command.execute(sender, label, args);
            return;
        }

        sender.sendMessage("'" + args[1] + "' is not a valid command. Type /duels leaderboard for help.");
    }
}
