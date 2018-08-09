package me.realized.de.leaderboards;

import java.text.MessageFormat;
import me.realized.de.leaderboards.util.StringUtil;
import org.bukkit.command.CommandSender;

public enum Lang {

    UNSUPPORTED("Hologram Leaderboards are not supported on &f1.7&7."),
    INVALID_LEADERBOARD_TYPE("&c''{0}'' is not a valid leaderboard type. Available: {1}"),
    NOT_LOOKING_AT_WALL_SIGN("&cYou must be looking at a sign that is attached to a block."),
    NOT_LOOKING_AT_SIGN("&cYou must be looking at a sign."),
    ALREADY_EXISTS("&cA leaderboard with name ''{0}'' and type ''{1}'' already exists."),
    ALREADY_EXISTS_LOCATION("&cA leaderboard with name ''{0}'' and type ''{1}'' already exists at {2}."),
    HEAD_LB_NOT_FOUND("&cNo head leaderboard is associated to that sign."),
    LB_NOT_FOUND("&cNo leaderboard found with name ''{0}'' and type ''{1}''."),
    NO_ACTIVE_LBS("&cNo leaderboards with type ''{0}'' are available."),
    KIT_NOT_FOUND("&c''{0}'' is not an existing kit."),
    PLAYER_ONLY("&cThis command is player only!"),
    INVALID_COMMAND("&c''{0}'' is not a valid command. Please type &f/{1} &cfor help."),

    HELP_HEADER("&9&m------------- &fLeaderboards &9&m-------------", false),
    HELP_FORMAT("&f/{0} &e- &7{1}", false),
    HELP_FOOTER("&9&m----------------------------------------", false),
    USAGE_FORMAT("&f/{0} &e- &7{1}"),
    CREATE_LEADERBOARD("Created a leaderboard with name &f{0}&7 and type &e{1} &7at &b{2}&7."),
    SET_RANK_INFO("To change the rank of Head Leaderboard, type &e/ds lb setrank [rank] &7while looking at the sign."),
    SET_RANK("Changed rank of Head Leaderboard &f{0} &7to &a#{1}&7!"),
    REMOVE_LEADERBOARD("Removed leaderboard with name &f{0}&7 and type &e{1} &7at &b{2}&7."),
    LIST_HEADER("List of Leaderboards with Type &e{0} &9-"),
    LIST_FORMAT("&bName: &c{0} &7- &bDataType: &c{1} &7- &bLocation: &c{2}", false),
    LIST_FOOTER("Total: &a{0}"),
    TELEPORT("Teleported to leaderboard &f{0} &7and type &e{1}&7!"),
    TELEPORT_HERE("Teleporting &f{0} &7to your location..."),
    LEADERBOARD_BLOCK_BREAK("&7Cannot destroy a leaderboard by hand. Please type &f{0} &7or &bclick this message &7to remove.");
    private final MessageFormat message;

    Lang(final String message, final boolean prefix) {
        this.message = new MessageFormat(StringUtil.color((prefix ? "&9[Duels] &7" : "") + message));
    }

    Lang(final String message) {
        this(message, true);
    }

    public String format(final Object... parameters) {
        return message.format(parameters);
    }

    public void sendTo(final CommandSender sender, final Object... parameters) {
        sender.sendMessage(format(parameters));
    }
}