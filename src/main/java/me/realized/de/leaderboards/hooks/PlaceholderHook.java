package me.realized.de.leaderboards.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.util.Updatable;
import me.realized.duels.api.Duels;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;

public class PlaceholderHook implements Updatable<Kit> {

    private final Leaderboards extension;
    private final Duels api;

    private me.clip.placeholderapi.PlaceholderHook previous;

    public PlaceholderHook(final Leaderboards extension, final Duels api) {
        this.extension = extension;
        this.api = api;
        this.previous = PlaceholderAPI.getPlaceholders().get("duels");
        PlaceholderAPI.unregisterPlaceholderHook(api);
        new PlaceholdersExpansion().register();
    }

    @Override
    public void update(final Kit value) {}

    public class PlaceholdersExpansion extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "duels";
        }

        @Override
        public String getPlugin() {
            return api.getName();
        }

        @Override
        public String getAuthor() {
            return "Realized";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String s) {
            final String result = extension.find(player, s);

            if (result != null) {
                return result;
            }

            return previous != null ? previous.onPlaceholderRequest(player, s) : null;
        }
    }
}
