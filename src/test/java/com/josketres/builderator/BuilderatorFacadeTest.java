package com.josketres.builderator;

import com.google.testing.compile.JavaFileObjects;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;
import test.classes.NormalJavaBean;

import javax.lang.model.element.Modifier;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class BuilderatorFacadeTest {
    @Test
    public void test_builderFor() throws Exception {
        test_builderFor(BuilderatorFacade.builderFor(NormalJavaBean.class));
    }

    static void test_builderFor(String normalJavaBeanBuilderSource) throws Exception {

        String builderTesterSource = createBuilderTesterSource();
        assertThat("normalJavaBeanBuilderSource is null", normalJavaBeanBuilderSource, is(notNullValue()));

        TestCompiler testCompiler = new TestCompiler();
        testCompiler.compile(
            JavaFileObjects.forSourceString("test.classes.NormalJavaBeanBuilder",
                                            normalJavaBeanBuilderSource),
            JavaFileObjects.forSourceString("test.classes.BuilderTester",
                                            builderTesterSource));

        testCompiler.assertCompilationSuccess();
        
        testCompiler.loadClass("test.classes.NormalJavaBeanBuilder");
        Callable<NormalJavaBean> tester = (Callable<NormalJavaBean>) testCompiler.loadClass("test.classes.BuilderTester").newInstance();
        NormalJavaBean constructed = tester.call();
        assertThat(constructed.getName(), is("builderTest"));
        assertThat(constructed.getAge(), is(18));
    }

    private static String createBuilderTesterSource() throws Exception {

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
