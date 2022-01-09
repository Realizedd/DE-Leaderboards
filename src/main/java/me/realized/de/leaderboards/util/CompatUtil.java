package me.realized.de.leaderboards.util;

import org.bukkit.Bukkit;

public final class CompatUtil {

    private static final int SUB_VERSION;
    private static final boolean MARKER;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        SUB_VERSION = NumberUtil.parseInt(packageName.substring(packageName.lastIndexOf('.') + 1).split("_")[1]).orElse(0);

        final Class<?> ARMORSTAND = ReflectionUtil.getClassUnsafe("org.bukkit.entity.ArmorStand");
        MARKER = ARMORSTAND != null && ReflectionUtil.getMethod(ARMORSTAND, "setMarker", Boolean.TYPE) != null;
    }

    private CompatUtil() {}

    public static boolean isPre1_14() {
        return SUB_VERSION < 14;
    }

    public static boolean hasMarker() {
        return MARKER;
    }
}
