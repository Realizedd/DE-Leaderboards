package me.realized.de.leaderboards.command.commands;

import java.util.Map;
import me.realized.de.leaderboards.Lang;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.command.LBCommand;
import me.realized.de.leaderboards.leaderboard.AbstractLeaderboard;
import me.realized.de.leaderboards.leaderboard.LeaderboardType;
import me.realized.de.leaderboards.util.EnumUtil;
import me.realized.de.leaderboards.util.StringUtil;
import me.realized.duels.api.Duels;
import org.bukkit.command.CommandSender;

public class ListCommand extends LBCommand {

    public ListCommand(final Leaderboards extension, final Duels api) {
        super(extension, api, "list", "list [hologram|head|sign]", "Lists available leaderboards with given type.", 3, false);
    }

    @Override
    public void execute(final CommandSender sender, final String label, final String[] args) {
        final LeaderboardType type = EnumUtil.getByName(args[2].toUpperCase(), LeaderboardType.class);

        if (type == null) {
            Lang.INVALID_LEADERBOARD_TYPE.sendTo(sender, args[2], EnumUtil.getNames(LeaderboardType.class));
            return;
        }

        final Map<String, AbstractLeaderboard> leaderboards = leaderboardManager.getLeaderboards().get(type);

        if (leaderboards == null || leaderboards.isEmpty()) {
            Lang.NO_ACTIVE_LBS.sendTo(sender, args[2]);
            return;
        }

        Lang.LIST_HEADER.sendTo(sender, type.name());
        leaderboards.values().forEach(leaderboard ->
            Lang.LIST_FORMAT.sendTo(sender, leaderboard.getName(), leaderboard.getDataType(), StringUtil.from(leaderboard.getLocation())));
        Lang.LIST_FOOTER.sendTo(sender, leaderboards.size());
    }
}
