package com.josketres.builderator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TargetClass {

    private String name;
    private String qualifiedName;
    private String packageName;
    private List<Property> properties;

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

}
