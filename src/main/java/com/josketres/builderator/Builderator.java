package com.josketres.builderator;

public class Builderator {

    /**
     * Generates the source code of a test data builder for the given class.
     */
    public static String builderFor(Class<?> targetClass) {
        return new BuilderatorRenderer(targetClass).render();
    }
}
