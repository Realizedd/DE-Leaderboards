package me.realized.de.leaderboards.config;

import java.util.List;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    @Getter
    private String headLoading;
    @Getter
    private String headNoData;
    @Getter
    private List<String> headSignFormat;

    @Getter
    private String hologramLoading;
    @Getter
    private String hologramNoData;
    @Getter
    private String hologramHeader;
    @Getter
    private String hologramLineFormat;
    @Getter
    private String hologramFooter;
    @Getter
    private double spaceBetweenLines;

    @Getter
    private String signLoading;
    @Getter
    private String signNoData;
    @Getter
    private String signHeader;
    @Getter
    private boolean signSpaceBetween;
    @Getter
    private String signLineFormat;

    public Config(final FileConfiguration config) {
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
    }
}
