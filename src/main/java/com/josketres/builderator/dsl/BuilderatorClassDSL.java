package com.josketres.builderator.dsl;

import com.josketres.builderator.Converters.Converter;

public interface BuilderatorClassDSL extends BuilderatorDSL {
    BuilderatorDSL converter(Converter<?, ?>... converters);

    BuilderatorClassDSL targetClass(Class<?>... targetClasses);

    BuilderatorClassDSL groupSetters(String groupName, String... properties);

    BuilderatorClassDSL defaultValue(String property, String valueStatement);
}