package com.josketres.builderator;

import com.josketres.builderator.model.Property;
import com.josketres.builderator.model.TargetClass;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;

class MetadataExtractor {
    private TargetClass data;
    private Class<?> targetClass;

    public MetadataExtractor(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public TargetClass getMetadata() {
        data = new TargetClass();
        data.setName(targetClass.getSimpleName());
        data.setQualifiedName(targetClass.getName());
        data.setPackageName(targetClass.getPackage().getName());
        addProperties();
        return data;
    }

    private void addProperties() {
        PropertyDescriptor[] desc = PropertyUtils
                .getPropertyDescriptors(targetClass);
        for (PropertyDescriptor descriptor : desc) {
            Property property = extractPropertyMetadata(descriptor);
            if (property != null) {
                data.addProperty(property);
            }
        }
    }

    private Property extractPropertyMetadata(PropertyDescriptor descriptor) {
        Property property = null;
        if (descriptor != null
                && !descriptor.getPropertyType().equals(Class.class)
                && descriptor.getWriteMethod() != null) {
            Class<?> type = descriptor.getPropertyType();
            String name = descriptor.getName();
            String simpleName = type.getSimpleName();
            String writeMethod = getSetterName(descriptor);
            String typeQualifiedName = getTypeQualifiedName(type);
            boolean shouldBeImported = shouldBeImported(type);
            property = createProperty(simpleName, name, writeMethod,
                    typeQualifiedName, shouldBeImported);
        }
        return property;
    }

    private boolean shouldBeImported(Class<?> type) {

        if (type.getPackage() != null
                && !type.getPackage().equals(targetClass.getPackage())
                && !type.getPackage().equals(String.class.getPackage())) {
            return true;
        }
        return false;
    }

    private String getTypeQualifiedName(Class<?> type) {
        return type.getName();
    }

    public Property createProperty(String type, String name,
                                   String setterName, String typeQualifiedName,
                                   boolean shouldBeImported) {
        Property property = new Property();
        property.setType(type);
        property.setName(name);
        property.setSetterName(setterName);
        property.setShouldBeImported(shouldBeImported);
        property.setQualifiedName(typeQualifiedName);
        return property;
    }

    private String getSetterName(PropertyDescriptor descriptor) {
        if (descriptor.getWriteMethod() != null) {
            return descriptor.getWriteMethod().getName();
        }
        return null;
    }
}
