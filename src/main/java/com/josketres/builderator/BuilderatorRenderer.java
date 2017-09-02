package com.josketres.builderator;

/**
 * A builder helping creating a Builderator.
 */
public class BuilderatorRenderer {
    private final Class<?> targetClass;

    public BuilderatorRenderer(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public String render() {
        TargetClass metadata = new MetadataExtractor(targetClass).getMetadata();
        return new Renderer().render(metadata);
    }
}
