package me.realized.de.leaderboards.config;

import java.util.List;
import lombok.Getter;
import me.realized.de.leaderboards.Leaderboards;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    @Getter
    private final boolean hookHD;

    @Getter
    private final String headLoading;
    @Getter
    private final String headNoData;
    @Getter
    private final List<String> headSignFormat;

    @Getter
    private final String hologramLoading;
    @Getter
    private final String hologramNoData;
    @Getter
    private final String hologramHeader;
    @Getter
    private final String hologramLineFormat;
    @Getter
    private final String hologramFooter;
    @Getter
    private final double spaceBetweenLines;

    @Getter
    private final String signLoading;
    @Getter
    private final String signNoData;
    @Getter
    private final String signHeader;
    @Getter
    private final boolean signSpaceBetween;
    @Getter
    private final String signLineFormat;

    @Getter
    private final String placeholderNoRank;
    @Getter
    private final String placeholderLoading;
    @Getter
    private final String placeholderNoData;

    public Config(final Leaderboards extension) {
        final FileConfiguration config = extension.getConfig();
        this.hookHD = config.getBoolean("hook-into-holographicdisplays", true);

        this.headLoading = config.getString("types.HEAD.loading");
        this.headNoData = config.getString("types.HEAD.no-data");
        this.headSignFormat = config.getStringList("types.HEAD.sign-format");

        this.hologramLoading = config.getString("types.HOLOGRAM.loading");
        this.hologramNoData = config.getString("types.HOLOGRAM.no-data");
        this.hologramHeader = config.getString("types.HOLOGRAM.header");
        this.hologramLineFormat = config.getString("types.HOLOGRAM.line-format");
        this.hologramFooter = config.getString("types.HOLOGRAM.footer");
        this.spaceBetweenLines = config.getDouble("types.HOLOGRAM.space-between-lines");

        this.signLoading = config.getString("types.SIGN.loading");
        this.signNoData = config.getString("types.SIGN.no-data");
        this.signHeader = config.getString("types.SIGN.header");
        this.signSpaceBetween = config.getBoolean("types.SIGN.space-between");
        this.signLineFormat = config.getString("types.SIGN.sign-line-format");

        this.placeholderNoRank = config.getString("placeholders.no-rank");
        this.placeholderLoading = config.getString("placeholders.loading");
        this.placeholderNoData = config.getString("placeholders.no-data");
    }
}
