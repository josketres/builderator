package com.josketres.builderator;

public class SingleSourceWriter implements Builderator.SourceWriter {
    private String source;

    @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName, String builderSource) {
        this.source = builderSource;
    }

    public String getSource() {
        return source;
    }
}
