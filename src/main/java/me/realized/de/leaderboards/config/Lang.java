package me.realized.de.leaderboards.config;

import java.text.MessageFormat;
import me.realized.de.leaderboards.util.StringUtil;
import org.bukkit.command.CommandSender;

public enum Lang {

    INVALID_LEADERBOARD_TYPE("&c''{0}'' is not a valid leaderboard type. Available: {1}"),
    NOT_LOOKING_AT_WALL_SIGN("&cYou must be looking at a sign that is attached to a block."),
    NOT_LOOKING_AT_SIGN("&cYou must be looking at a sign."),
    ALREADY_EXISTS("&cA leaderboard with name ''{0}'' and type ''{1}'' already exists."),
    HEAD_LB_NOT_FOUND("&cNo head leaderboard is associated to that sign."),
    LB_NOT_FOUND("&cNo leaderboard found with name ''{0}'' and type ''{1}''."),
    NO_ACTIVE_LBS("&cNo leaderboards with type ''{0}'' are available."),

    CREATE_LEADERBOARD("Created a leaderboard with name &f{0}&7 and type &e{1} &7at &c{2}&7."),
    SET_RANK_INFO("To change the rank of Head Leaderboard, type &e/ds lb setrank [rank] &7while looking at the sign."),
    SET_RANK("Changed rank of Head Leaderboard &f{0} &7to &a#{1}&7!"),
    REMOVE_LEADERBOARD("Removed leaderboard with name &f{0}&7 and type &e{1} &7at &c{2}&7."),
    LIST_HEADER("List of Leaderboards with Type &e{0} &9-"),
    LIST_FORMAT("&bName: &c{0} &7- &bDataType: &c{1} &7- &bLocation: &c{2}", false),
    LIST_FOOTER("Total: &a{0}");

    private final MessageFormat message;

    Lang(final String message, final boolean prefix) {
        this.message = new MessageFormat(StringUtil.color((prefix ? "&9[Duels Leaderboards] &7" : "") + message));
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