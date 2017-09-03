package com.josketres.builderator;

import java.io.StringWriter;

public class Builderator {

    /**
     * Generates the source code of a test data builder for the given class.
     */
    public static String builderFor(Class<?> targetClass) {
        final StringWriter writer = new StringWriter();
        BuilderatorRenderer.SourceWriter sourceWriter = new BuilderatorRenderer.SourceWriter() {
            @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName,
                                              String builderSource) {
                writer.write(builderSource);
            }
        };
        new BuilderatorRenderer().targetClass(targetClass).render(sourceWriter);
        return writer.toString();
    }
}
