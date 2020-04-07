package me.realized.de.leaderboards.hooks;

import java.util.UUID;
import lombok.Getter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.config.Config;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final Config configuration;
    @Getter
    private final Permission permission;

    public VaultHook(final Leaderboards extension) {
        this.configuration = extension.getConfiguration();

        final RegisteredServiceProvider<Permission> provider = extension.getApi().getServer().getServicesManager().getRegistration(Permission.class);
        permission = provider != null ? provider.getProvider() : null;
    }

    public String findPrefix(final UUID uuid) {
        if (permission == null) {
            return "";
        }

        final String group = permission.getPrimaryGroup(null, Bukkit.getOfflinePlayer(uuid));

        if (group == null) {
            return "";
        }

        final String prefix = configuration.getPrefixes().get(group.toLowerCase());
        return prefix != null ? prefix : "";
    }
}
