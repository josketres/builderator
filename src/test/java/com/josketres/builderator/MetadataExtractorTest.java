package com.josketres.builderator;

import com.google.common.reflect.TypeToken;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import test.classes.*;

import java.util.Date;
import java.util.List;

import static com.google.common.reflect.TypeToken.of;

@RunWith(Theories.class)
public class MetadataExtractorTest {
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void shouldGenerateMetadataOfTargetClass() {
        MetadataExtractor gen = new MetadataExtractor(NormalJavaBean.class);
        TargetClass data = gen.getMetadata();

        softly.assertThat(data.getName()).isEqualTo(NormalJavaBean.class.getSimpleName());
        softly.assertThat(data.getQualifiedName()).isEqualTo(NormalJavaBean.class.getName());
        softly.assertThat(data.getPackageName()).isEqualTo(NormalJavaBean.class.getPackage().getName());
        softly.assertThat(data.getSuperClasses()).containsExactly(Object.class.getName());
    }

    @Test
    public void shouldGenerateMetadataOfTargetClassWithSuperClasses() {
        TargetClass intermediateClass = new MetadataExtractor(IntermediateClass.class).getMetadata();
        TargetClass concreteClass = new MetadataExtractor(ConcreteClass.class).getMetadata();

        softly.assertThat(intermediateClass.getSuperClasses()).containsExactly(
            BaseClass.class.getName(), Object.class.getName());
        softly.assertThat(concreteClass.getSuperClasses()).containsExactly(
            IntermediateClass.class.getName(), BaseClass.class.getName(), Object.class.getName());
    }

    @Test
    public void shouldGeneratePropertiesMetadata() throws Exception {
        MetadataExtractor gen = new MetadataExtractor(NormalJavaBean.class);
        TargetClass data = gen.getMetadata();

        List<Property> properties = data.getProperties();

        softly.assertThat(properties).usingFieldByFieldElementComparator().containsExactly(
            property(Address.class, "address", "setAddress", false),
            property(int.class, "age", "setAge", false),
            property(Date.class, "date", "setDate", false),
            property(String.class, "name", "setName", false),
            property(new TypeToken<List<String>>() {
            }, "petNames", "setPetNames", false)
        );
    }

    @Theory
    public void shouldGeneratePropertiesMetadataWithOnlyDeclaredFields(boolean concreteClass) throws Exception {
        MetadataExtractor gen = new MetadataExtractor(concreteClass ? ConcreteClass.class : IntermediateClass.class);
        TargetClass data = gen.getMetadata();

        List<Property> properties = data.getProperties();

        if (concreteClass) {
            softly.assertThat(properties).usingFieldByFieldElementComparator().containsExactly(
                property(long.class, "value", "setValue", false));
        } else {
            softly.assertThat(properties).usingFieldByFieldElementComparator().containsExactly(
                property(String.class, "name", "setName", false));
        }
    }

    private static Property property(Class<?> clazz, String name, String setterName, boolean shouldBeImported) {
        return property(of(clazz), name, setterName, shouldBeImported);
    }

    private static Property property(TypeToken<?> typeToken, String name, String setterName, boolean shouldBeImported) {
        Property property = new Property(typeToken);
        property.setName(name);
        property.setSetterName(setterName);
        property.setShouldBeImported(shouldBeImported);
        return property;
    }
}
