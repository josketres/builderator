package com.josketres.builderator;

public class Utils {
    private Utils() {
    }

    public static Class<?> loadClass(String superClass) {
        try {
            return Class.forName(superClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
