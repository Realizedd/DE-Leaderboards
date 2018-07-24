package me.realized.de.leaderboards.util;

import java.lang.reflect.Method;

public final class ReflectionUtil {

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ignored) {}
        return null;
    }

    private ReflectionUtil() {}
}
