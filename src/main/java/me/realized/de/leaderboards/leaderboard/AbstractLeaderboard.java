package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import lombok.Getter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class AbstractLeaderboard implements Leaderboard {

    protected final Leaderboards extension;
    protected final Config config;
    protected final LeaderboardManager leaderboardManager;

    @Getter
    private final File file;
    @Getter
    private final String name;
    @Getter
    private final LeaderboardType type;
    @Getter
    private final String dataType;
    @Getter
    private final Location location;

    @Getter
    private final FileConfiguration configuration;

    public AbstractLeaderboard(final Leaderboards extension, final LeaderboardType type, final String name, final String dataType, final Location location) {
        this.extension = extension;
        this.config = extension.getConfiguration();
        this.leaderboardManager = extension.getLeaderboardManager();
        this.type = type;
        this.name = name;
        this.dataType = dataType;
        this.location = location;
        this.file = new File(leaderboardManager.getFolder(), type + "-" + name + ".yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public AbstractLeaderboard(final Leaderboards extension, final File file, final LeaderboardType type, final String name) {
        this.extension = extension;
        this.config = extension.getConfiguration();
        this.leaderboardManager = extension.getLeaderboardManager();
        this.file = file;
        this.type = type;
        this.name = name;
        this.configuration = YamlConfiguration.loadConfiguration(file);

        final String dataType = configuration.getString("data-type");
        Objects.requireNonNull(dataType, "data-type is null");

        final ConfigurationSection locationSection = configuration.getConfigurationSection("location");
        Objects.requireNonNull(type, "location is null");

        final String worldName = locationSection.getString("world");
        final World world;

        if (worldName == null || (world = Bukkit.getWorld(worldName)) == null) {
            throw new NullPointerException("worldName or world is null");
        }

        this.dataType = dataType;
        this.location = new Location(world, locationSection.getDouble("x"), locationSection.getDouble("y"), locationSection.getDouble("z"));
    }

    @Override
    public void save() {
        configuration.set("type", type.name());
        configuration.set("name", name);
        configuration.set("data-type", dataType);
        configuration.set("location.world", location.getWorld().getName());
        configuration.set("location.x", location.getX());
        configuration.set("location.y", location.getY());
        configuration.set("location.z", location.getZ());

        try {
            configuration.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
