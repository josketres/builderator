package com.josketres.builderator;

import com.josketres.builderator.Builderator.SourceWriter;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import test.classes.Address;
import test.classes.NormalJavaBean;

import java.util.HashMap;
import java.util.Map;

public class BuilderatorTest {
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void render_singleClass() throws Exception {
        renderNormalJavaBeanBuilder(softly, renderBuildersFor(NormalJavaBean.class).get(NormalJavaBean.class));
    }

    @Test
    public void render_twoClasses() throws Exception {
        Map<Class<?>, String> sources = renderBuildersFor(NormalJavaBean.class, Address.class);
        renderNormalJavaBeanBuilder(softly, sources.get(NormalJavaBean.class));
        renderAddressBuilder(sources.get(Address.class));
    }

    static void renderNormalJavaBeanBuilder(JUnitSoftAssertions softly, String normalJavaBeanBuilderSource)
        throws Exception {
        BuilderTester<NormalJavaBean> tester = new BuilderTester<NormalJavaBean>(NormalJavaBean.class,
                                                                                 normalJavaBeanBuilderSource);
        NormalJavaBean constructed = tester
            .test(".name($S).age(18).date(new java.util.Date()).address(new Address())", "builderTest");
        softly.assertThat(constructed.getName()).isEqualTo("builderTest");
        softly.assertThat(constructed.getAge()).isEqualTo(18);
    }

    private void renderAddressBuilder(String addressBuilderSource) throws Exception {
        BuilderTester<Address> tester = new BuilderTester<Address>(Address.class, addressBuilderSource);
        Address constructed = tester.test(".number(12).street($S)", "main street");
        softly.assertThat(constructed.getNumber()).isEqualTo(12);
        softly.assertThat(constructed.getStreet()).isEqualTo("main street");
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