package com.josketres.builderator;

import com.squareup.javapoet.*;
import com.squareup.javapoet.MethodSpec.Builder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Lists.cartesianProduct;
import static com.josketres.builderator.Utils.loadClass;
import static com.josketres.builderator.Utils.simpleName;
import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.CodeBlock.builder;
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
import static java.util.Collections.emptySet;
import static javax.lang.model.element.Modifier.*;

class Renderer {
    static final String BUILDER_SUFFIX = "Builder";

    static final String BUILD_METHOD = "build";
    static final String INIT_METHOD = "init";
    static final Set<String> RESERVED_METHODS = new HashSet<String>(asList(BUILD_METHOD, INIT_METHOD));

    private static final String MYSELF = "myself";
    private static final String SELF_TYPE = "selfType";

    private final Converters converters;

    Renderer(Converters converters) {
        this.converters = converters;
    }

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

            for (Class<?> sourceType : getSourceTypes(property)) {
                builderBuilder.addMethod(
                    createSetter(concreteClass, builderType, typeVariableS, property, sourceType));
            }
        }

        for (TargetClass.PropertyGroup group : target.getPropertyGroups()) {
            for (Iterable<IProperty> properties : combinePropertiesWithConverters(group.getProperties())) {
                builderBuilder.addMethod(createGroupSetter(concreteClass, builderType, typeVariableS,
                                                           group.getGroupName(), properties));
            }
        }

        return builder(target.getPackageName(), builderBuilder.build()).build().toString();
    }

    private Iterable<Iterable<IProperty>> combinePropertiesWithConverters(Iterable<Property> properties) {
        List<List<Class<?>>> allSourceTypes = new ArrayList<List<Class<?>>>();
        for (Property property : properties) {
            final List<Class<?>> sourceTypes = new ArrayList<Class<?>>();
            sourceTypes.add((Class<?>) property.getTypeClass());
            addAll(sourceTypes, getSourceTypes(property));
            allSourceTypes.add(sourceTypes);
        }

        List<Iterable<IProperty>> result = new ArrayList<Iterable<IProperty>>();
        for (List<Class<?>> argumentTypes : cartesianProduct(allSourceTypes)) {
            List<IProperty> arguments = new ArrayList<IProperty>();
            for (int i = 0; i < argumentTypes.size(); i++) {
                Class<?> argumentType = argumentTypes.get(i);
                arguments.add(new ExtProperty(argumentType, get(properties, i)));
            }
            result.add(arguments);
        }
        return result;
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
        return buildSetter(concreteClass, builderType, typeVariableS, property, property.getTypeClass())
            .addStatement("this.$N = $N", property.getName(), property.getName())
            .addStatement("return $N", concreteClass ? "this" : MYSELF)
            .build();
    }

    private MethodSpec createSetter(boolean concreteClass, ClassName builderType, TypeVariableName typeVariableS,
                                    Property property, Class<?> sourceType) {
        ClassName converters = get(Converters.class);
        CodeBlock block = builder()
            .add("try {")
            .indent()
            .add("\n$T converters = $T.getInstance();", converters, converters)
            .add("\nreturn $N(converters.convert($N, $N.class));", property.getName(), property.getName(),
                 property.getQualifiedName())
            .unindent()
            .add("\n} catch(Exception e) {")
            .indent()
            .add("\nthrow new RuntimeException(e);")
            .unindent()
            .add("\n}\n")
            .build();
        return buildSetter(concreteClass, builderType, typeVariableS, property, sourceType)
            .addCode(block)
            .build();
    }

    private MethodSpec.Builder buildSetter(boolean concreteClass, ClassName builderType,
                                           TypeVariableName typeVariableS, Property property, Type parameterType) {
        return methodBuilder(property.getName())
            .addModifiers(PUBLIC)
            .addParameter(parameterType, property.getName())
            .returns(concreteClass ? builderType : typeVariableS);
    }

    private MethodSpec createGroupSetter(boolean concreteClass, ClassName builderType, TypeVariableName typeVariableS,
                                         String groupName, Iterable<? extends IProperty> properties) {
        Builder builder = methodBuilder(groupName)
            .addModifiers(PUBLIC)
            .returns(concreteClass ? builderType : typeVariableS);
        StringBuilder statement = new StringBuilder();
        for (IProperty property : properties) {
            builder.addParameter(property.getTypeClass(), property.getName());
            if (statement.length() > 0) {
                statement.append('.');
            }
            statement.append(property.getName()).append('(').append(property.getName()).append(')');
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

    private Iterable<Class<?>> getSourceTypes(Property property) {
        if (property.getTypeClass() instanceof Class) {
            return converters.getSourceTypes((Class) property.getTypeClass());
        } else {
            return emptySet();
        }
    }

    private static class ExtProperty implements IProperty {
        private final Class<?> sourceType;
        private final Property property;

        private ExtProperty(Class<?> sourceType, Property property) {
            this.sourceType = sourceType;
            this.property = property;
        }

        @Override
        public String getName() {
            return property.getName();
        }

        @Override
        public Type getTypeClass() {
            return sourceType;
        }
    }
}
