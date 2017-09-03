package com.josketres.builderator;

import com.josketres.builderator.BuilderatorRenderer.SourceWriter;
import org.junit.Test;
import test.classes.Address;
import test.classes.NormalJavaBean;

import java.util.HashMap;
import java.util.Map;

import static com.google.testing.compile.JavaFileObjects.forSourceString;

public class BuilderatorRendererTest {
    @Test
    public void test_render() throws Exception {
        final Map<Class<?>, String> sources = renderBuilderForNormalJavaBean();
        BuilderatorTest.test_builderFor(sources.get(NormalJavaBean.class));

        TestCompiler testCompiler = new TestCompiler();
        testCompiler.compile(forSourceString("test.classes.AddressBuilder", sources.get(Address.class)));
        testCompiler.assertCompilationSuccess();
    }

    private Map<Class<?>, String> renderBuilderForNormalJavaBean() {
        final Map<Class<?>, String> sources = new HashMap<Class<?>, String>();
        SourceWriter sourceWriter = new SourceWriter() {
            @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName,
                                              String builderSource) {
                sources.put(targetClass, builderSource);
            }
        };

        new BuilderatorRenderer().targetClass(NormalJavaBean.class, Address.class).render(sourceWriter);
        return sources;
    }
}