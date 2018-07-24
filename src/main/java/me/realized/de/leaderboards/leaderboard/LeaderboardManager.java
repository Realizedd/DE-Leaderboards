package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import me.realized.de.leaderboards.Leaderboards;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LeaderboardManager {

    private final Leaderboards extension;
    private final File file;
    private final Map<LeaderboardType, Map<String, Leaderboard>> leaderboards = new HashMap<>();

    public LeaderboardManager(final Leaderboards extension) {
        this.extension = extension;
        this.file = new File(extension.getDataFolder(), "extensions.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        final ConfigurationSection leaderboards = config.getConfigurationSection("leaderboards");

        if (leaderboards == null) {
            return;
        }

        for (final String key : leaderboards.getKeys(false)) {
            final LeaderboardType type = LeaderboardType.get(key);

            if (type == null) {
                continue;
            }

            final ConfigurationSection typeSection = leaderboards.getConfigurationSection(key);

            for (final String name : typeSection.getKeys(false)) {
                try {
                    addLeaderboard(type, name, type.from(typeSection.getConfigurationSection(name)));
                } catch (IllegalArgumentException ex) {
                    // Log error
                }
            }
        }
    }

    public void addLeaderboard(final LeaderboardType type, final String name, final Leaderboard leaderboard) {
        if (!type.getType().isInstance(leaderboard)) {
            return;
        }

        this.leaderboards.computeIfAbsent(type, result -> new HashMap<>()).put(name, leaderboard);
    }
}
