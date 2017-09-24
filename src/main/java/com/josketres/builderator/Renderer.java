package com.josketres.builderator;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.Set;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.ParameterizedTypeName.get;
import static com.squareup.javapoet.TypeVariableName.get;
import static java.lang.String.format;
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
        ClassName targetType = get(target.getPackageName(), target.getName());
        String builderClassName = getBuilderClassName(target);
        ClassName builderType = get(target.getPackageName(), builderClassName);

        // Generates something like "BaseClassBuilder<T, S extends BaseClassBuilder<T, S>>"
        TypeVariableName typeVariableT = get("T", targetType);
        TypeVariableName typeVariableS = get("S", get(format("%s<T,S>", builderClassName)));
        ParameterizedTypeName myselfType = get(get(Class.class), typeVariableS);

        TypeSpec.Builder builderBuilder = TypeSpec.classBuilder(builderClassName).addModifiers(PUBLIC);

        MethodSpec.Builder constructorBuilder = constructorBuilder().addModifiers(PROTECTED);
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

        builderBuilder.addMethod(createInitMethod(target, get(target.getPackageName(), target.getName()), (parentBuilderClass != null)));
        if (concreteClass) {
            builderBuilder.addModifiers(PUBLIC);

            builderBuilder.addMethod(methodBuilder(getFactoryMethod(target))
                                         .addModifiers(PUBLIC, Modifier.STATIC)
                                         .returns(builderType)
                                         .addStatement("return new $T()", builderType)
                                         .build());
            builderBuilder.addMethod(createBuildMethod(target, get(target.getPackageName(), target.getName())));
        } else {
            // no Modifier => package private
            builderBuilder.addTypeVariable(typeVariableT).addTypeVariable(typeVariableS);
        }

        for (Property property : target.getProperties()) {
            builderBuilder.addField(FieldSpec.builder(
                    property.getTypeClass(),
                    property.getName())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            MethodSpec setter = methodBuilder(property.getName())
                    .addModifiers(PUBLIC)
                    .addParameter(property.getTypeClass(), property.getName())
                    .returns(concreteClass ? builderType : typeVariableS)
                    .addStatement("this.$N = $N", property.getName(), property.getName())
                    .addStatement("return $N", concreteClass ? "this" : MYSELF)
                    .build();
            builderBuilder.addMethod(setter);
        }

        String s = JavaFile.builder(target.getPackageName(), builderBuilder.build()).build().toString();
        return s;
    }

    static String getFactoryMethod(TargetClass target) {
        return "a" + target.getName();
    }

    static String getBuilderClassName(TargetClass target) {
        return target.getName() + BUILDER_SUFFIX;
    }

    private MethodSpec createBuildMethod(TargetClass target, TypeName objectType) {

        MethodSpec.Builder method = methodBuilder(BUILD_METHOD)
                .addModifiers(PUBLIC)
                .returns(objectType);

        method.addStatement("$T $N = new $T()", objectType, "object", objectType);
        method.addStatement("$N($N)", INIT_METHOD, "object");
        method.addStatement("return object");
        return method.build();
    }

    private MethodSpec createInitMethod(TargetClass target, TypeName objectType, boolean parentBuilderClass) {
        MethodSpec.Builder method = methodBuilder(INIT_METHOD)
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

    static String simpleName(String parentBuilderClassName) {
        int beginIndex = parentBuilderClassName.lastIndexOf('.');
        if (beginIndex >= 0)  {
            parentBuilderClassName = parentBuilderClassName.substring(beginIndex + 1);
        }
        return parentBuilderClassName;
    }
}
