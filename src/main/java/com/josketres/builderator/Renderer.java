package com.josketres.builderator;

import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

class Renderer {

    public static final String BUILDER_SUFFIX = "Builder";

    public String render(final TargetClass target) {

        ClassName builderType = ClassName.get(target.getPackageName(), target.getName() + BUILDER_SUFFIX);

        TypeSpec.Builder builderBuilder = TypeSpec.classBuilder(target.getName() + BUILDER_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(MethodSpec.methodBuilder("a" + target.getName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(builderType)
                        .addStatement("return new $T()", builderType)
                        .build());

        for (Property property : target.getProperties()) {
            builderBuilder.addField(FieldSpec.builder(
                    property.getTypeClass(),
                    property.getName())
                    .addModifiers(Modifier.PRIVATE)
                    .build());
            MethodSpec setter = MethodSpec.methodBuilder(property.getName())
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(property.getTypeClass(), property.getName())
                    .returns(builderType)
                    .addStatement("this.$N = $N", property.getName(), property.getName())
                    .addStatement("return this")
                    .build();
            builderBuilder.addMethod(setter);
        }

        builderBuilder.addMethod(createBuildMethod(target));

        return JavaFile.builder(target.getPackageName(), builderBuilder.build()).build().toString();
    }

    private MethodSpec createBuildMethod(TargetClass target) {

        ClassName returnType = ClassName.get(target.getPackageName(), target.getName());
        MethodSpec.Builder method = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType);

        method.addStatement("$T $N = new $T()", returnType, "object", returnType);
        for (Property property : target.getProperties()) {
            method.addStatement("object.$N($N)", property.getSetterName(), property.getName());
        }

        method.addStatement("return object");
        return method.build();
    }
}
