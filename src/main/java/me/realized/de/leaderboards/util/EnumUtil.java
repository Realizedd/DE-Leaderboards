package me.realized.de.leaderboards.util;

import java.util.Arrays;
import org.apache.commons.lang.StringUtils;

public final class EnumUtil {

    private EnumUtil() {}

    public static <E extends Enum> E getByName(final String name, Class<E> clazz) {
        return clazz.cast(Arrays.stream(clazz.getEnumConstants()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    public static <E extends Enum> String getNames(final Class<E> clazz) {
        return StringUtils.join(clazz.getEnumConstants(), ", ");
    }
}
