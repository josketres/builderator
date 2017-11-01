package com.josketres.builderator;

import com.josketres.builderator.Builderator.SourceWriter;
import com.josketres.builderator.Converters.AbstractConverter;
import com.josketres.builderator.dsl.BuilderatorClassDSL;
import org.apache.commons.beanutils.ConversionException;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import test.classes.*;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Calendar.DAY_OF_MONTH;

@RunWith(Theories.class)
public class BuilderatorTest {
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void render_groupSetters() throws Exception {
        SingleSourceWriter sourceWriter = new SingleSourceWriter();

        new Builderator().targetClass(GroupSettersClass.class)
                         .groupSetters("validityDates", "beginValidity", "endValidity")
                         .render(sourceWriter);

        BuilderTester<GroupSettersClass> tester = new BuilderTester<GroupSettersClass>(GroupSettersClass.class,
                                                                                       sourceWriter.getSource());
        GroupSettersClass constructed = tester.test(".validityDates(new java.util.Date(1), new java.util.Date(2))");
        softly.assertThat(constructed.getBeginValidity()).isEqualTo(new Date(1));
        softly.assertThat(constructed.getEndValidity()).isEqualTo(new Date(2));
    }

    @Theory
    public void render_groupSetters_withConverter(boolean useConverterArg1, boolean useConverterArg2,
                                                  Position idPosition) throws Exception {
        SingleSourceWriter sourceWriter = new SingleSourceWriter();
        DateConverter converter = new DateConverter();

        BuilderatorClassDSL dsl = new Builderator().targetClass(GroupSettersClass.class);
        String arg1 = useConverterArg1 ?
            format("\"%s\"", converter.expectedString) :
            format("new java.util.Date(%d)", converter.getMilliSeconds());
        String arg2 = useConverterArg2 ?
            format("\"%s\"", converter.expectedString2) :
            format("new java.util.Date(%d)", converter.getMilliSeconds2());
        String groupCall = null;
        int id = 123;
        switch (idPosition) {
        case FIRST:
            dsl.groupSetters("group", "id", "beginValidity", "endValidity");
            groupCall = format(".group(%d, %s, %s)", id, arg1, arg2);
            break;
        case MIDDLE:
            dsl.groupSetters("group", "beginValidity", "id", "endValidity");
            groupCall = format(".group(%s, %d, %s)", arg1, id, arg2);
            break;
        case LAST:
            dsl.groupSetters("group", "beginValidity", "endValidity", "id");
            groupCall = format(".group(%s, %s, %d)", arg1, arg2, id);
        }

        dsl.converter(converter).render(sourceWriter);

        BuilderTester<GroupSettersClass> tester = new BuilderTester<GroupSettersClass>(GroupSettersClass.class,
                                                                                       sourceWriter.getSource());
        GroupSettersClass constructed = tester.test(groupCall);
        softly.assertThat(constructed.getBeginValidity()).isEqualTo(converter.expectedDate);
        softly.assertThat(constructed.getEndValidity()).isEqualTo(converter.expectedDate2);
        softly.assertThat(constructed.getId()).isEqualTo(id);
    }

    @Test
    public void render_defaultValue() throws Exception {
        SingleSourceWriter sourceWriter = new SingleSourceWriter();
        Date defaultValue = new Date(456);

        new Builderator().targetClass(GroupSettersClass.class)
                         .defaultValue("beginValidity", "new Date(456)")
                         .render(sourceWriter);

        BuilderTester<GroupSettersClass> tester = new BuilderTester<GroupSettersClass>(GroupSettersClass.class,
                                                                                       sourceWriter.getSource());
        GroupSettersClass constructed = tester.test(null);
        softly.assertThat(constructed.getBeginValidity()).isEqualTo(defaultValue);
        softly.assertThat(constructed.getEndValidity()).isNull();
    }

    @Theory
    public void render_singleClass_withConverter(boolean useConverter) throws Exception {
        SingleSourceWriter sourceWriter = new SingleSourceWriter();
        DateConverter converter = new DateConverter();

        new Builderator().targetClass(NormalJavaBean.class)
                         .converter(converter)
                         .render(sourceWriter);

        BuilderTester<NormalJavaBean> tester = new BuilderTester<NormalJavaBean>(NormalJavaBean.class,
                                                                                 sourceWriter.getSource());
        String builderUsage = useConverter ?
            format(".date(\"%s\")", converter.expectedString) :
            ".date(new java.util.Date(" + converter.getMilliSeconds() + "))";
        NormalJavaBean constructed = tester.test(builderUsage);
        softly.assertThat(constructed.getDate()).isEqualTo(converter.expectedDate);
    }

    @Test
    public void render_singleClass() throws Exception {
        renderNormalJavaBeanBuilder(softly, renderBuildersFor(NormalJavaBean.class).get(NormalJavaBean.class));
    }

    @Test
    public void render_singleClass_withNonObjectParent() throws Exception {
        renderIntermediateClassBuilder(softly, renderBuildersFor(IntermediateClass.class).get(IntermediateClass.class));
    }

    @Theory
    public void render_twoClasses(boolean splitTargetClasses) throws Exception {
        Map<Class<?>, String> sources = renderBuildersFor(splitTargetClasses, NormalJavaBean.class, Address.class);
        renderNormalJavaBeanBuilder(softly, sources.get(NormalJavaBean.class));
        renderAddressBuilder(sources.get(Address.class));
    }

    @Theory
    public void render_threeClasses_withInheritance_inReverseOrder(boolean splitTargetClasses) throws Exception {
        render_threeClasses_withInheritance(splitTargetClasses, ConcreteClass.class, IntermediateClass.class,
                                            BaseClass.class);
    }

    @Theory
    public void render_threeClasses_withInheritance_inOrder(boolean splitTargetClasses) throws Exception {
        render_threeClasses_withInheritance(splitTargetClasses, BaseClass.class, IntermediateClass.class,
                                            ConcreteClass.class);
    }

    @Theory
    public void render_threeClasses_withInheritance_disordered(boolean splitTargetClasses) throws Exception {
        render_threeClasses_withInheritance(splitTargetClasses, IntermediateClass.class, ConcreteClass.class,
                                            BaseClass.class);
    }

    public void render_threeClasses_withInheritance(boolean splitTargetClasses, Class<?>... targetClasses)
        throws Exception {
        Map<Class<?>, String> sources = renderBuildersFor(splitTargetClasses, targetClasses);
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

    static void renderIntermediateClassBuilder(JUnitSoftAssertions softly, String intermediateClassBuilderSource)
        throws Exception {
        BuilderTester<IntermediateClass> tester = new BuilderTester<IntermediateClass>(IntermediateClass.class,
                                                                                       intermediateClassBuilderSource);

        IntermediateClass constructed = tester.test(".name(\"name2\")");
        softly.assertThat(constructed.getName()).as("name").isEqualTo("name2");
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
        return renderBuildersFor(false, targetClasses);
    }

    private Map<Class<?>, String> renderBuildersFor(boolean splitTargetClasses, Class<?>... targetClasses) {
        final Map<Class<?>, String> sources = new HashMap<Class<?>, String>();
        SourceWriter sourceWriter = new SourceWriter() {
            @Override public void writeSource(Class<?> targetClass, String builderClassQualifiedName,
                                              String builderSource) {
                sources.put(targetClass, builderSource);
            }
        };

        Builderator builderator = new Builderator();
        if (splitTargetClasses && (targetClasses.length > 1)) {
            Class<?>[] moreClasses = new Class<?>[targetClasses.length - 1];
            arraycopy(targetClasses, 1, moreClasses, 0, targetClasses.length - 1);
            builderator.targetClass(targetClasses[0]).targetClass(moreClasses).render(sourceWriter);
        } else {
            builderator.targetClass(targetClasses).render(sourceWriter);
        }
        return sources;
    }

    private class DateConverter extends AbstractConverter<String, Date> {
        private final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        private final Date expectedDate;
        private final Date expectedDate2;
        private final String expectedString;
        private final String expectedString2;

        public DateConverter() throws ParseException {
            super(String.class, Date.class);

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(1));
            expectedString = formatter.format(calendar.getTime());
            expectedDate = formatter.parse(expectedString);

            calendar.add(DAY_OF_MONTH, 1);
            expectedString2 = formatter.format(calendar.getTime());
            expectedDate2 = formatter.parse(expectedString2);
        }

        @Override public Date convert(String date) throws ConversionException {
            try {
                return formatter.parse(date);
            } catch (ParseException e) {
                throw new ConversionException(e);
            }
        }

        public long getMilliSeconds() {
            return expectedDate.getTime();
        }

        public long getMilliSeconds2() {
            return expectedDate2.getTime();
        }
    }

    public enum Position {
        FIRST,
        MIDDLE,
        LAST;
    }
}