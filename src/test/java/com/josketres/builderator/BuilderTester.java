package com.josketres.builderator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec.Builder;

import java.util.concurrent.Callable;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.testing.compile.JavaFileObjects.forSourceString;
import static com.josketres.builderator.Renderer.BUILDER_SUFFIX;
import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static java.lang.String.format;
import static javax.lang.model.element.Modifier.PUBLIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class BuilderTester<T> {
    private static final String TARGET_PACKAGE = "test.classes";
    private static final String BUILDER_TESTER_CLASS = TARGET_PACKAGE + ".BuilderTester";

    private final Class<T> targetClass;
    private final String builderSourceCode;
    private final String builderClassName;

    public BuilderTester(Class<T> targetClass, String builderSourceCode) {
        this.targetClass = targetClass;
        this.builderSourceCode = builderSourceCode;
        this.builderClassName = targetClass.getSimpleName() + BUILDER_SUFFIX;
    }

    @SuppressWarnings("unchecked")
    public T test(String builderUsage, Object... args) throws Exception {
        builderUsage = nullToEmpty(builderUsage).trim();
        assertThat("builderSourceCode is null", builderSourceCode, is(notNullValue()));

        String builderQualifiedClassName = TARGET_PACKAGE + '.' + builderClassName;
        TestCompiler testCompiler = new TestCompiler();
        testCompiler.compile(forSourceString(builderQualifiedClassName, builderSourceCode),
                             forSourceString(BUILDER_TESTER_CLASS, generateBuilderTester(builderUsage, args)));
        testCompiler.assertCompilationSuccess();

        testCompiler.loadClass(builderQualifiedClassName);
        return ((Callable<T>) testCompiler.loadClass(BUILDER_TESTER_CLASS).newInstance()).call();
    }

    private String generateBuilderTester(String builderUsage, Object[] args) {
        Builder builder = classBuilder("BuilderTester")
            .addModifiers(PUBLIC)
            .addMethod(constructorBuilder().addModifiers(PUBLIC).build())
            .addSuperinterface(Callable.class)
            .addMethod(methodBuilder("call")
                           .addModifiers(PUBLIC)
                           .returns(targetClass)
                           .addStatement(format("%s builder = %s.a%s()", builderClassName, builderClassName,
                                                targetClass.getSimpleName()))
                           .addStatement("return builder" + builderUsage + ".build()", args)
                           .build());

        return JavaFile.builder(TARGET_PACKAGE, builder.build()).build().toString();
    }
}
