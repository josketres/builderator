package com.josketres.builderator;

import java.util.ArrayList;
import java.util.List;

class ConstructorSignature {

    private final List<String> types;
    private final List<String> names;
    private final List<String> generics;
    private final List<Class<?>> classTypes;

    public ConstructorSignature() {
        types = new ArrayList<String>();
        names = new ArrayList<String>();
        generics = new ArrayList<String>();
        classTypes = new ArrayList<Class<?>>();
    }

    public void addArgument(String type, String name, String generic,
                            Class<?> argumentType) {
        types.add(type);
        names.add(name);
        generics.add(generic);
        classTypes.add(argumentType);
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getGenerics() {
        return generics;
    }

    public List<Class<?>> getClassTypes() {
        return classTypes;
    }
}
