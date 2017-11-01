package com.josketres.builderator;

import com.google.common.reflect.TypeToken;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.reflect.TypeToken.of;
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
            String name = descriptor.getName();
            String writeMethod = getSetterName(descriptor);
            property = createProperty(getSetterParameterType(descriptor), name, writeMethod);
        }
        return property;
    }

    private Property createProperty(TypeToken<?> typeToken, String name, String setterName) {
        Property property = new Property(typeToken);
        property.setName(name);
        property.setSetterName(setterName);
        return property;
    }

    private String getSetterName(PropertyDescriptor descriptor) {
        if (descriptor.getWriteMethod() != null) {
            return descriptor.getWriteMethod().getName();
        }
        return null;
    }

    private TypeToken<?> getSetterParameterType(PropertyDescriptor descriptor) {
        if (descriptor.getWriteMethod() != null) {
            Type[] parameterTypes = descriptor.getWriteMethod().getGenericParameterTypes();
            return of(parameterTypes[0]);
        }
        return null;
    }
}
