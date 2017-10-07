package com.josketres.builderator;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.Builder;

import java.util.HashSet;
import java.util.Set;

import static com.josketres.builderator.Utils.loadClass;
import static com.josketres.builderator.Utils.simpleName;
import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.JavaFile.builder;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.ParameterizedTypeName.get;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static com.squareup.javapoet.TypeVariableName.get;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.*;

class Renderer {
    static final String BUILDER_SUFFIX = "Builder";

    static final String BUILD_METHOD = "build";
    static final String INIT_METHOD = "init";
    static final Set<String> RESERVED_METHODS = new HashSet<String>(asList(BUILD_METHOD, INIT_METHOD));

    private static final String MYSELF = "myself";
    private static final String SELF_TYPE = "selfType";

    public String render(final TargetClass target, String parentBuilderClass, boolean concreteClass) {
        boolean abstractModifier = isAbstract(loadClass(target.getQualifiedName()).getModifiers());
        if (abstractModifier) {
            concreteClass = false;
        }

        String builderClassName = getBuilderClassName(target);
        ClassName builderType = get(target.getPackageName(), builderClassName);

        TypeVariableName typeVariableT = get("T", get(target.getPackageName(), target.getName()));
        TypeVariableName typeVariableS = get("S", get(format("%s<T,S>", builderClassName)));

        TypeSpec.Builder builderBuilder = classBuilder(builderClassName).addModifiers(PUBLIC);
        if (abstractModifier) {
            builderBuilder.addModifiers(ABSTRACT);
        }

        addConstructor(builderBuilder, target, parentBuilderClass, concreteClass, builderClassName, typeVariableS);
        builderBuilder.addMethod(
            createInitMethod(target, get(target.getPackageName(), target.getName()), (parentBuilderClass != null)));
        if (concreteClass) {
            builderBuilder.addMethod(createFactoryMethod(target, builderType));
            builderBuilder.addMethod(createBuildMethod(get(target.getPackageName(), target.getName())));
        } else {
            builderBuilder.addTypeVariable(typeVariableT).addTypeVariable(typeVariableS);
        }

        for (Property property : target.getProperties()) {
            builderBuilder.addField(createField(property));
            builderBuilder.addMethod(createSetter(concreteClass, builderType, typeVariableS, property));
        }

        for (TargetClass.PropertyGroup property : target.getPropertyGroups()) {
            builderBuilder.addMethod(createGroupSetter(concreteClass, builderType, typeVariableS, property, target));
        }

        return builder(target.getPackageName(), builderBuilder.build()).build().toString();
    }

    private FieldSpec createField(Property property) {
        FieldSpec.Builder builder = FieldSpec.builder(property.getTypeClass(), property.getName())
                                             .addModifiers(PRIVATE);
        if (property.getDefaultValue() != null) {
            builder.initializer("$L", property.getDefaultValue());
        }
        return builder.build();
    }

    private MethodSpec createSetter(boolean concreteClass, ClassName builderType, TypeVariableName typeVariableS,
                                    Property property) {
        return methodBuilder(property.getName())
            .addModifiers(PUBLIC)
            .addParameter(property.getTypeClass(), property.getName())
            .returns(concreteClass ? builderType : typeVariableS)
            .addStatement("this.$N = $N", property.getName(), property.getName())
            .addStatement("return $N", concreteClass ? "this" : MYSELF)
            .build();
    }

    private MethodSpec createGroupSetter(boolean concreteClass, ClassName builderType, TypeVariableName typeVariableS,
                                         TargetClass.PropertyGroup propertyGroup,
                                         TargetClass target) {
        Builder builder = methodBuilder(propertyGroup.getGroupName())
            .addModifiers(PUBLIC)
            .returns(concreteClass ? builderType : typeVariableS);
        StringBuilder statement = new StringBuilder();
        for (String setter : propertyGroup.getProperties()) {
            for (Property property : target.getProperties()) {
                if (property.getName().equals(setter)) {
                    builder.addParameter(property.getTypeClass(), property.getName());
                    if (statement.length() > 0) {
                        statement.append('.');
                    }
                    statement.append(property.getName()).append('(').append(property.getName()).append(')');
                    break;
                }
            }
        }
        return builder.addStatement("return " + statement.toString()).build();
    }

    private MethodSpec createFactoryMethod(TargetClass target, ClassName builderType) {
        return methodBuilder(getFactoryMethod(target))
            .addModifiers(PUBLIC, STATIC)
            .returns(builderType)
            .addStatement("return new $T()", builderType)
            .build();
    }

    private void addConstructor(TypeSpec.Builder builderBuilder, TargetClass target, String parentBuilderClass,
                                boolean concreteClass, String builderClassName, TypeVariableName typeVariableS) {
        Builder constructorBuilder = constructorBuilder().addModifiers(concreteClass ? PUBLIC : PROTECTED);
        ParameterizedTypeName myselfType = get(get(Class.class), typeVariableS);
        if (parentBuilderClass == null) {
            if (concreteClass) {
                // examples : NormalJavaBeanBuilder, AddressBuilder (see test classes)
            } else {
                // example : BaseClassBuilder (see test classes)
                FieldSpec myselfField = FieldSpec.builder(typeVariableS, MYSELF).addModifiers(PROTECTED, FINAL).build();
                builderBuilder.addField(myselfField).build();

                ParameterSpec constructorParameter = builder(myselfType, SELF_TYPE).build();
                constructorBuilder.addParameter(constructorParameter);

                constructorBuilder.addStatement("this.$N = $N.cast(this)", MYSELF, SELF_TYPE);
            }
        } else {
            if (concreteClass) {
                // example : ConcreteClassBuilder (see test classes)
                builderBuilder.superclass(get(format("%s<%s,%s>", parentBuilderClass, target.getName(),
                                                     simpleName(builderClassName))));

                constructorBuilder.addStatement("super($L.class)", builderClassName);
            } else {
                // example : IntermediateClassBuilder (see test classes)
                builderBuilder.superclass(get(format("%s<T,S>", parentBuilderClass)));

                ParameterSpec constructorParameter = builder(myselfType, SELF_TYPE).build();
                constructorBuilder.addParameter(constructorParameter);

                constructorBuilder.addStatement("super($N)", SELF_TYPE);
            }
        }
        builderBuilder.addMethod(constructorBuilder.build());
    }

    static String getFactoryMethod(TargetClass target) {
        return "a" + target.getName();
    }

    static String getBuilderClassName(TargetClass target) {
        return target.getName() + BUILDER_SUFFIX;
    }

    private MethodSpec createBuildMethod(TypeName objectType) {
        Builder method = methodBuilder(BUILD_METHOD).addModifiers(PUBLIC).returns(objectType);
        method.addStatement("$T $N = new $T()", objectType, "object", objectType);
        method.addStatement("$N($N)", INIT_METHOD, "object");
        method.addStatement("return object");
        return method.build();
    }

    private MethodSpec createInitMethod(TargetClass target, TypeName objectType, boolean parentBuilderClass) {
        Builder method = methodBuilder(INIT_METHOD)
            .addModifiers(PROTECTED)
            .addParameter(ParameterSpec.builder(objectType, "object").build());
        if (parentBuilderClass) {
            method.addStatement("super.$N($N)", INIT_METHOD, "object");
        }

        for (Property property : target.getProperties()) {
            method.addStatement("$N.$N($N)", "object", property.getSetterName(), property.getName());
        }
        return method.build();
    }
}
