package com.josketres.builderator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TargetClass {

    private String name;
    private String qualifiedName;
    private String packageName;
    private List<Property> properties;
    private List<String> superClasses;

    public TargetClass() {
        this.properties = new ArrayList<Property>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        properties.add(property);
        Collections.sort(properties);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getSuperClasses() {
        return superClasses;
    }

    public void setSuperClasses(List<String> superClasses) {
        this.superClasses = superClasses;
    }
}
