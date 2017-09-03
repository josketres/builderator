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
    public void test_render() throws Exception {
        Map<Class<?>, String> sources = renderBuilderForNormalJavaBean();
        testNormalJavaBeanBuilder(sources.get(NormalJavaBean.class));
        testAddressBuilder(sources.get(Address.class));
    }

    static void testNormalJavaBeanBuilder(String normalJavaBeanBuilderSource) throws Exception {
        BuilderTester<NormalJavaBean> tester = new BuilderTester<NormalJavaBean>(NormalJavaBean.class,
                                                                                 normalJavaBeanBuilderSource);
        NormalJavaBean constructed = tester
            .test(".name($S).age(18).date(new java.util.Date()).address(new Address())", "builderTest");
        assertThat(constructed.getName(), is("builderTest"));
        assertThat(constructed.getAge(), is(18));
    }

    private static void testAddressBuilder(String addressBuilderSource) throws Exception {
        BuilderTester<Address> tester = new BuilderTester<Address>(Address.class, addressBuilderSource);
        Address constructed = tester.test(".number(12).street($S)", "main street");
        assertThat(constructed.getNumber(), is(12));
        assertThat(constructed.getStreet(), is("main street"));
    }

    private Map<Class<?>, String> renderBuilderForNormalJavaBean() {
        final Map<Class<?>, String> sources = new HashMap<Class<?>, String>();
        SourceWriter sourceWriter = new SourceWriter() {
            @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName,
                                              String builderSource) {
                sources.put(targetClass, builderSource);
            }
        };

        new Builderator().targetClass(NormalJavaBean.class, Address.class).render(sourceWriter);
        return sources;
    }
}