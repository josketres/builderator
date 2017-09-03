package com.josketres.builderator;

import com.josketres.builderator.Builderator.SourceWriter;
import org.junit.Test;
import test.classes.Address;
import test.classes.NormalJavaBean;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BuilderatorTest {
    @Test
    public void render_singleClass() throws Exception {
        renderNormalJavaBeanBuilder(renderBuildersFor(NormalJavaBean.class).get(NormalJavaBean.class));
    }

    @Test
    public void render_twoClasses() throws Exception {
        Map<Class<?>, String> sources = renderBuildersFor(NormalJavaBean.class, Address.class);
        renderNormalJavaBeanBuilder(sources.get(NormalJavaBean.class));
        renderAddressBuilder(sources.get(Address.class));
    }

    static void renderNormalJavaBeanBuilder(String normalJavaBeanBuilderSource) throws Exception {
        BuilderTester<NormalJavaBean> tester = new BuilderTester<NormalJavaBean>(NormalJavaBean.class,
                                                                                 normalJavaBeanBuilderSource);
        NormalJavaBean constructed = tester
            .test(".name($S).age(18).date(new java.util.Date()).address(new Address())", "builderTest");
        assertThat(constructed.getName(), is("builderTest"));
        assertThat(constructed.getAge(), is(18));
    }

    private static void renderAddressBuilder(String addressBuilderSource) throws Exception {
        BuilderTester<Address> tester = new BuilderTester<Address>(Address.class, addressBuilderSource);
        Address constructed = tester.test(".number(12).street($S)", "main street");
        assertThat(constructed.getNumber(), is(12));
        assertThat(constructed.getStreet(), is("main street"));
    }

    private Map<Class<?>, String> renderBuildersFor(Class<?>... targetClasses) {
        final Map<Class<?>, String> sources = new HashMap<Class<?>, String>();
        SourceWriter sourceWriter = new SourceWriter() {
            @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName,
                                              String builderSource) {
                sources.put(targetClass, builderSource);
            }
        };

        new Builderator().targetClass(targetClasses).render(sourceWriter);
        return sources;
    }
}