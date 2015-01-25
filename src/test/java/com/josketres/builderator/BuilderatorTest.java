package com.josketres.builderator;

import com.google.common.collect.Lists;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import test.classes.NormalJavaBean;

import javax.lang.model.element.Modifier;
import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assert_;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BuilderatorTest {

    @Test
    public void test_builder_compiles_without_error() throws Exception {

        assert_().about(JavaSourceSubjectFactory.javaSource())
                .that(JavaFileObjects.forSourceString("test.classes.NormalJavaBeanBuilder",
                        Builderator.builderFor(NormalJavaBean.class)))
                .compilesWithoutError();
    }

    @Test
    public void test_builder_tester_compiles_without_error() throws Exception {

        assert_().about(JavaSourcesSubjectFactory.javaSources())
                .that(Lists.newArrayList(
                        JavaFileObjects.forSourceString("test.classes.NormalJavaBeanBuilder",
                                Builderator.builderFor(NormalJavaBean.class)),
                        JavaFileObjects.forSourceString("test.classes.BuilderTester",
                                createBuilderTesterSource())))
                .compilesWithoutError();
    }

    @Test
    public void test_compiles_and_can_be_used() throws Exception {

        TestCompiler testCompiler = new TestCompiler();
        testCompiler.compile(
                JavaFileObjects.forSourceString("test.classes.NormalJavaBeanBuilder", Builderator.builderFor(NormalJavaBean.class)),
                JavaFileObjects.forSourceString("test.classes.BuilderTester", createBuilderTesterSource()));

        testCompiler.loadClass("test.classes.NormalJavaBeanBuilder");
        Callable<NormalJavaBean> tester = (Callable<NormalJavaBean>) testCompiler.loadClass("test.classes.BuilderTester").newInstance();
        NormalJavaBean constructed = tester.call();
        assertThat(constructed.getName(), is("builderTest"));
        assertThat(constructed.getAge(), is(18));
    }

    private String createBuilderTesterSource() throws Exception {

        TypeSpec.Builder builder = TypeSpec.classBuilder("BuilderTester")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                .addSuperinterface(Callable.class)
                .addMethod(MethodSpec.methodBuilder("call")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(NormalJavaBean.class)
                        .addStatement("NormalJavaBeanBuilder builder = NormalJavaBeanBuilder.aNormalJavaBean()")
                        .addStatement("return builder.name($S).age(18).date(new java.util.Date()).address(new Address()).build()", "builderTest")
                        .build());

        return JavaFile.builder("test.classes", builder.build()).build().toString();
    }
}
