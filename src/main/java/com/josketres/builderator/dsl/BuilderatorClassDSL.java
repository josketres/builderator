package com.josketres.builderator.dsl;

public interface BuilderatorClassDSL extends BuilderatorDSL {
    BuilderatorClassDSL targetClass(Class<?>... targetClasses);
}