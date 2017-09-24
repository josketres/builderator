package com.josketres.builderator;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import static com.josketres.builderator.Utils.loadClass;
import static java.util.Arrays.asList;
import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;

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
        addSuperClasses();
        addProperties();
        return data;
    }

    private void addSuperClasses() {
        List<String> superClasses = new ArrayList<String>();
        Class<?> currentClass = targetClass;
        while (true) {
            currentClass = currentClass.getSuperclass();
            if (currentClass == null) {
                break;
            }

            superClasses.add(currentClass.getName());
        }
        data.setSuperClasses(superClasses);
    }

    private void addProperties() {
        List<PropertyDescriptor> desc = new ArrayList<PropertyDescriptor>(asList(getPropertyDescriptors(targetClass)));
        for (String superClass : data.getSuperClasses()) {
            desc.removeAll(asList(getPropertyDescriptors(loadClass(superClass))));
        }
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
            property = createProperty(type, simpleName, name, writeMethod,
                    typeQualifiedName);
        }
        return property;
    }

    private String getTypeQualifiedName(Class<?> type) {
        return type.getName();
    }

    public Property createProperty(Class<?> typeClass, String type, String name,
                                   String setterName, String typeQualifiedName) {
        Property property = new Property();
        property.setTypeClass(typeClass);
        property.setType(type);
        property.setName(name);
        property.setSetterName(setterName);
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
