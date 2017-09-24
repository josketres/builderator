package com.josketres.builderator;

import com.josketres.builderator.Builderator.SourceWriter;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import test.classes.*;

import java.lang.reflect.Method;
import java.util.*;

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

    @Test
    public void render_threeClasses_withInheritance_inReverseOrder() throws Exception {
        render_threeClasses_withInheritance(ConcreteClass.class, IntermediateClass.class, BaseClass.class);
    }

    @Test
    public void render_threeClasses_withInheritance_inOrder() throws Exception {
        render_threeClasses_withInheritance(BaseClass.class, IntermediateClass.class, ConcreteClass.class);
    }

    @Test
    public void render_threeClasses_withInheritance_disordered() throws Exception {
        render_threeClasses_withInheritance(IntermediateClass.class, ConcreteClass.class, BaseClass.class);
    }

    public void render_threeClasses_withInheritance(Class<?>... targetClasses) throws Exception {
        Map<Class<?>, String> sources = renderBuildersFor(targetClasses);
        BuilderTester<ConcreteClass> testerConcreteClass = renderConcreteClassBuilder(sources);

        Class<?> baseClassBuilder = testerConcreteClass.loadBuilderClass(BaseClass.class);
        assertDeclaredSettersAreExactly(baseClassBuilder, testerConcreteClass, "id");

        Class<?> intermediateClassBuilder = testerConcreteClass.loadBuilderClass(IntermediateClass.class);
        assertSuperClass(intermediateClassBuilder, baseClassBuilder);
        assertDeclaredSettersAreExactly(intermediateClassBuilder, testerConcreteClass, "name");

        Object concreteClassBuilder = testerConcreteClass.newBuilderInstance();
        assertSuperClass(concreteClassBuilder, intermediateClassBuilder);
        assertDeclaredSettersAreExactly(concreteClassBuilder, testerConcreteClass, "value");
    }

    private void assertSuperClass(Object object, Class<?> expectedSuperClass) {
        assertSuperClass((object == null) ? null : object.getClass(), expectedSuperClass);
    }

    private void assertSuperClass(Class<?> clazz, Class<?> expectedSuperClass) {
        softly.assertThat(clazz).isNotNull();
        if (softly.wasSuccess()) {
            // TODO move this to assertj
            Class<?> actualSuperClass = clazz.getSuperclass();
            softly.assertThat(actualSuperClass)
                  .overridingErrorMessage("%n" +
                                          "Expecting:%n" +
                                          "  <%s's super class>%n" +
                                          "to be:%n" +
                                          "  <%s>%n" +
                                          "but was:%n" +
                                          "  <%s>%n",
                                          clazz.getName(),
                                          expectedSuperClass.getName() + " (ClassLoader " + expectedSuperClass.getClassLoader() + ")",
                                          actualSuperClass.getName() + " (ClassLoader " + actualSuperClass.getClassLoader() + ")")
                  .isEqualTo(expectedSuperClass);
        }
    }

    private void assertDeclaredSettersAreExactly(Object object, BuilderTester<?> tester,
                                                 String... expectedDeclaredSetters) {
        assertDeclaredSettersAreExactly((object == null) ? null : object.getClass(), tester, expectedDeclaredSetters);
    }

    private void assertDeclaredSettersAreExactly(Class<?> clazz, BuilderTester<?> tester,
                                                 String... expectedDeclaredSetters) {
        Set<String> actualDeclaredSetters = new TreeSet<String>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Renderer.RESERVED_METHODS.contains(method.getName()) && !tester.getFactoryMethod()
                                                                          .equals(method.getName())) {
                actualDeclaredSetters.add(method.getName());
            }
        }
        Arrays.sort(expectedDeclaredSetters);
        softly.assertThat(actualDeclaredSetters)
              .overridingErrorMessage("%n" +
                                      "Expecting:%n" +
                                      "  <%s>%n" +
                                      "to contain exactly declared setters:%n" +
                                      "  <%s>%n" +
                                      "but was:%n" +
                                      "  <%s>%n",
                                      clazz, Arrays.toString(expectedDeclaredSetters),
                                      actualDeclaredSetters)
              .containsExactly(expectedDeclaredSetters);
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

    private BuilderTester<ConcreteClass> renderConcreteClassBuilder(Map<Class<?>, String> builderSource) throws Exception {
        BuilderTester<ConcreteClass> tester = new BuilderTester<ConcreteClass>(ConcreteClass.class, builderSource.get(ConcreteClass.class));
        tester.addBuilderSource(BaseClass.class, builderSource.get(BaseClass.class));
        tester.addBuilderSource(IntermediateClass.class, builderSource.get(IntermediateClass.class));

        ConcreteClass constructed = tester.test(".id(15).name(\"name15\").value(200)");
        softly.assertThat(constructed.getId()).as("id").isEqualTo(15);
        softly.assertThat(constructed.getName()).as("name").isEqualTo("name15");
        softly.assertThat(constructed.getValue()).as("value").isEqualTo(200);

        return tester;
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