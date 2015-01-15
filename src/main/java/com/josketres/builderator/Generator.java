package com.josketres.builderator;

import com.josketres.builderator.model.TargetClass;

public class Generator {

    public String generate(Class<?> targetClass) {

        TargetClass metadata = new MetadataExtractor(targetClass).getMetadata();
        return new Renderer().render(metadata);
    }
}
