package com.josketres.builderator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec.Builder;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;
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

class BuilderTester<T> {
    private static final String TARGET_PACKAGE = "test.classes";
    private static final String BUILDER_TESTER_CLASS = TARGET_PACKAGE + ".BuilderTester";

    private final Class<T> targetClass;
    private final String builderClassName;
    private final List<BuilderSource> builderSources = new ArrayList<BuilderSource>();

    public BuilderTester(Class<T> targetClass, String builderSourceCode) {
        this.targetClass = targetClass;
        this.builderClassName = targetClass.getSimpleName() + BUILDER_SUFFIX;
        addBuilderSource(targetClass, builderSourceCode);

        assertThat("builderSourceCode is null for " + targetClass.getName(), builderSourceCode, is(notNullValue()));
    }

    void addBuilderSource(Class<?> targetClass, String builderSourceCode) {
        String builderQualifiedClassName = targetClass.getName() + BUILDER_SUFFIX;
        builderSources.add(new BuilderSource(targetClass, builderQualifiedClassName, builderSourceCode));
    }

    @SuppressWarnings("unchecked")
    public T test(String builderUsage, Object... args) throws Exception {
        TestCompiler testCompiler = buildAndCompileAll(builderUsage, args);

        testCompiler.loadClass(builderSources.get(0).qualifiedClassName);
        return ((Callable<T>) testCompiler.loadClass(BUILDER_TESTER_CLASS).newInstance()).call();
    }

    TestCompiler testCompiler;
    public Class<?> loadBuilderClass(Class<?> targetClass) throws Exception {
        if (testCompiler == null) {
            testCompiler = buildAndCompileAll(null);
        }

        return testCompiler.loadClass(targetClass.getName() + BUILDER_SUFFIX);
    }

    public Object newBuilderInstance() throws Exception {
        Class<?> aClass = loadBuilderClass(targetClass);
        return aClass.getMethod(getFactoryMethod(targetClass)).invoke(null);
    }

    @SuppressWarnings("unchecked")
    private TestCompiler buildAndCompileAll(String builderUsage, Object... args) throws Exception {
        builderUsage = nullToEmpty(builderUsage).trim();

        TestCompiler testCompiler = new TestCompiler();
        JavaFileObject[] sources = new JavaFileObject[builderSources.size() + 1];
        for (int i = 0; i < builderSources.size(); i++) {
            BuilderSource builderSource = builderSources.get(i);
            sources[i] = forSourceString(builderSource.qualifiedClassName, builderSource.sourceCode);
        }
        sources[sources.length - 1] = forSourceString(BUILDER_TESTER_CLASS, generateBuilderTester(builderUsage, args));
        testCompiler.compile(sources);
        testCompiler.assertCompilationSuccess();

        return testCompiler;
    }

    String getFactoryMethod() {
        return getFactoryMethod(targetClass);
    }

    String getFactoryMethod(Class<?> clazz) {
        return "a" + clazz.getSimpleName();
    }

    private String generateBuilderTester(String builderUsage, Object[] args) {
        Builder builder = classBuilder("BuilderTester")
            .addModifiers(PUBLIC)
            .addMethod(constructorBuilder().addModifiers(PUBLIC).build())
            .addSuperinterface(Callable.class)
            .addMethod(methodBuilder("call")
                           .addModifiers(PUBLIC)
                           .returns(targetClass)
                           .addStatement(format("%s builder = %s.%s()", builderClassName, builderClassName,
                                                getFactoryMethod()))
                           .addStatement("return builder" + builderUsage + ".build()", args)
                           .build());

        return JavaFile.builder(TARGET_PACKAGE, builder.build()).build().toString();
    }

    private static class BuilderSource {
        private final Class<?> targetClass;
        private final String qualifiedClassName;
        private final String sourceCode;

        private BuilderSource(Class<?> targetClass, String qualifiedClassName, String sourceCode) {
            this.targetClass = targetClass;
            this.sourceCode = sourceCode;
            this.qualifiedClassName = qualifiedClassName;
        }
    }
}
