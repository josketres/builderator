package com.josketres.builderator;

public class Builderator {

    /**
     * Generates the source code of a test data builder for the given class.
     */
    public static String builderFor(Class<?> targetClass) {

        TargetClass metadata = new MetadataExtractor(targetClass).getMetadata();
        return new Renderer().render(metadata);
    }
}
