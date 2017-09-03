package com.josketres.builderator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A builder helping creating a Builderator.
 */
public class BuilderatorRenderer {
    private final Set<Class<?>> targetClasses = new HashSet<Class<?>>();
    private final Map<Class<?>, TargetClass> metadataCache = new HashMap<Class<?>, TargetClass>();

    public BuilderatorRenderer targetClass(Class<?>... targetClasses) {
        for (Class<?> targetClass : targetClasses) {
            this.targetClasses.add(targetClass);
        }
        return this;
    }

    public void render(SourceWriter sourceWriter) {
        Renderer renderer = new Renderer();
        for (Class<?> targetClass : targetClasses) {
            TargetClass metadata = getMetadata(targetClass);
            String builderClassName = metadata.getPackageName() + '.' + renderer.getBuilderClassName(metadata);
            String render = renderer.render(metadata);
            sourceWriter.writeSource(targetClass, builderClassName, render);
        }
    }

    private TargetClass getMetadata(Class<?> targetClass) {
        TargetClass metadata = metadataCache.get(targetClass);
        if (metadata == null) {
            metadata = new MetadataExtractor(targetClass).getMetadata();
            metadataCache.put(targetClass, metadata);
        }
        return metadata;
    }

    public interface SourceWriter {
        void writeSource(Class<?> targetClass, String builderClassQualifiedName, String builderSource);
    }
}
