package me.realized.de.leaderboards.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;

public final class CompatUtil {

    private static final int SUB_VERSION;

    @Getter
    private static final boolean MARKER;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SUB_VERSION = NumberUtil.parseInt(packageName.substring(packageName.lastIndexOf('.') + 1).split("_")[1]).orElse(0);
        MARKER = ReflectionUtil.getMethod(ArmorStand.class, "setMarker", Boolean.TYPE) != null;
    }

    private CompatUtil() {}

    public static boolean isPre1_8() {
        return SUB_VERSION < 8;
    }

    public static boolean hasMarker() {
        return MARKER;
    }
}
