package com.josketres.builderator;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

class Property implements Comparable<Property>, IProperty {

    private final String type;
    private final String qualifiedName;

    private String name;
    private String setterName;
    private boolean shouldBeImported;
    private TypeToken<?> typeClass;
    private String defaultValue;

    public Property(TypeToken<?> typeToken) {
        this.typeClass = typeToken;
        this.type = Utils.simpleName(typeToken.toString());
        this.qualifiedName = typeToken.toString();
    }

    public int compareTo(Property o) {
        return getName().compareTo(o.getName());
    }

    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSetterName() {
        return setterName;
    }

    public void setSetterName(String setterName) {
        this.setterName = setterName;
    }

    public boolean isShouldBeImported() {
        return shouldBeImported;
    }

    public void setShouldBeImported(boolean shouldBeImported) {
        this.shouldBeImported = shouldBeImported;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public Type getTypeClass() {
        return typeClass.getType();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override public String toString() {
        return "Property{" +
               "type='" + type + '\'' +
               ", name='" + name + '\'' +
               ", setterName='" + setterName + '\'' +
               ", shouldBeImported=" + shouldBeImported +
               ", qualifiedName='" + qualifiedName + '\'' +
               ", typeClass=" + typeClass +
               ", defaultValue=" + defaultValue +
               '}';
    }
}
