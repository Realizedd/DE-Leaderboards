package me.realized.de.leaderboards.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.util.Updatable;
import me.realized.duels.api.Duels;
import me.realized.duels.api.kit.Kit;

public class MVdWPlaceholderHook implements Updatable<Kit> {

    private final Leaderboards extension;
    private final Duels api;
    private final PlaceholdersReplacer replacer;

    public MVdWPlaceholderHook(final Leaderboards extension, final Duels api) {
        this.extension = extension;
        this.api = api;
        this.replacer = new PlaceholdersReplacer();
        PlaceholderAPI.registerPlaceholder(api, "duels_rank_wins", replacer);
        PlaceholderAPI.registerPlaceholder(api, "duels_rank_losses", replacer);
        extension.getKitManager().getKits().forEach(kit -> PlaceholderAPI.registerPlaceholder(api, "duels_rank_" + kit.getName().replace(" ", "-"), replacer));
    }

    @Override
    public void update(final Kit kit) {
        PlaceholderAPI.registerPlaceholder(api, "duels_rank_" + kit.getName().replace(" ", "-"), replacer);
    }

    public class PlaceholdersReplacer implements PlaceholderReplacer {

        @Override
        public String onPlaceholderReplace(final PlaceholderReplaceEvent event) {
            return extension.find(event.getPlayer(), event.getPlaceholder().replace("duels_", ""));
        }
    }
}
