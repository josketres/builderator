package com.josketres.builderator.dsl;

public interface BuilderatorClassDSL extends BuilderatorDSL {
    BuilderatorClassDSL targetClass(Class<?>... targetClasses);

    BuilderatorClassDSL groupSetters(String groupName, String... properties);

    BuilderatorClassDSL defaultValue(String property, String valueStatement);
}