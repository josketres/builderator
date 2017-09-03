package com.josketres.builderator;

import com.josketres.builderator.Builderator.SourceWriter;

import java.io.StringWriter;

/**
 * A facade class handling cases simpler than using {@link Builderator}.
 */
public class BuilderatorFacade {

    /**
     * Generates the source code of a test data builder for the given class.
     */
    public static String builderFor(Class<?> targetClass) {
        final StringWriter writer = new StringWriter();
        SourceWriter sourceWriter = new SourceWriter() {
            @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName,
                                              String builderSource) {
                writer.write(builderSource);
            }
        };
        new Builderator().targetClass(targetClass).render(sourceWriter);
        return writer.toString();
    }
}
