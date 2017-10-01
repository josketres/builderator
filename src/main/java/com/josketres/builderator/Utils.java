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

    public static String simpleName(String qualifiedClassName) {
        int lowerOrEqualIndex = qualifiedClassName.indexOf('<');
        int lastDotIndex = qualifiedClassName
            .lastIndexOf('.', (lowerOrEqualIndex >= 0) ? lowerOrEqualIndex : qualifiedClassName.length());
        if (lastDotIndex >= 0) {
            qualifiedClassName = qualifiedClassName.substring(lastDotIndex + 1);
        }
        return qualifiedClassName;
    }
}
