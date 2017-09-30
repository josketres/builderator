package com.josketres.builderator;

import java.util.*;

class TargetClass {
    private final Set<PropertyGroup> propertyGroups = new HashSet<PropertyGroup>();

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

    public void groupSetters(String groupName, String... properties) {
        propertyGroups.add(new PropertyGroup(groupName, properties));
    }

    public Set<PropertyGroup> getPropertyGroups() {
        return propertyGroups;
    }

    class PropertyGroup {
        private final String groupName;
        private final String[] properties;

        private PropertyGroup(String groupName, String... properties) {
            this.groupName = groupName;
            this.properties = properties;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyGroup that = (PropertyGroup) o;

            return groupName != null ? groupName.equals(that.groupName) : that.groupName == null;
        }

        @Override public int hashCode() {
            return groupName != null ? groupName.hashCode() : 0;
        }

        public String getGroupName() {
            return groupName;
        }

        public String[] getProperties() {
            return properties;
        }
    }
}
