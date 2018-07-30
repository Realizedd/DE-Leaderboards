package me.realized.de.leaderboards.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import me.realized.de.leaderboards.Leaderboards;
import me.realized.de.leaderboards.config.Config;
import me.realized.duels.api.user.UserManager.TopEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class AbstractLeaderboard implements Leaderboard {

    protected final Leaderboards extension;
    protected final Config config;

    @Getter
    private final File file;
    @Getter
    private final String name;
    @Getter
    private final LeaderboardType type;
    @Getter
    private final String dataType;
    @Getter
    @Setter
    private Location location;
    @Getter
    @Setter
    private boolean changed;
    @Getter
    @Setter
    private TopEntry cached;
    @Getter
    private final FileConfiguration configuration;


    public AbstractLeaderboard(final Leaderboards extension, final LeaderboardType type, final String name, final String dataType, final Location location) {
        this.extension = extension;
        this.config = extension.getConfiguration();
        this.type = type;
        this.name = name;
        this.dataType = dataType;
        this.location = location;
        this.file = new File(extension.getLeaderboardManager().getFolder(), type + "-" + name + ".yml");
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public AbstractLeaderboard(final Leaderboards extension, final File file, final LeaderboardType type, final String name) {
        this.extension = extension;
        this.config = extension.getConfiguration();
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

    protected abstract void onUpdate(final TopEntry entry);

    void update(final TopEntry entry) {
        if (entry == null) {
            return;
        }

        if (changed || cached == null || !cached.equals(entry)) {
            cached = entry;
            changed = false;
            onUpdate(entry);
        }
    }

    protected void onRemove() {}

    @Override
    public void teleport(final Location location) {}

    @Override
    public void remove() {
        file.delete();
        onRemove();
    }

    @Override
    public void save() {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            configuration.set("type", type.name());
            configuration.set("name", name);
            configuration.set("data-type", dataType);
            configuration.set("location.world", location.getWorld().getName());
            configuration.set("location.x", location.getX());
            configuration.set("location.y", location.getY());
            configuration.set("location.z", location.getZ());
            configuration.save(file);
        } catch (IOException ex) {
            extension.error("Failed to save leaderboard '" + name + "' (type " + type.name() + ")!", ex);
        }
    }
}
