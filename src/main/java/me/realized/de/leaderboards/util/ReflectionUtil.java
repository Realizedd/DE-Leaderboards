package me.realized.de.leaderboards.util;

import java.lang.reflect.Method;

public final class ReflectionUtil {

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameters) {
        try {
            return clazz.getMethod(name, parameters);
        } catch (NoSuchMethodException ignored) {}
        return null;
    }

    public static Class<?> getClassUnsafe(final String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private ReflectionUtil() {}
}
