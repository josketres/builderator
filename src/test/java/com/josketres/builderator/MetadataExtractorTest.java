package com.josketres.builderator;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import test.classes.Address;
import test.classes.NormalJavaBean;

import java.util.Date;
import java.util.List;


public class MetadataExtractorTest {
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void shouldGenerateMetadataOfTargetClass() {
        MetadataExtractor gen = new MetadataExtractor(NormalJavaBean.class);
        TargetClass data = gen.getMetadata();

        softly.assertThat(data.getName()).isEqualTo(NormalJavaBean.class.getSimpleName());
        softly.assertThat(data.getQualifiedName()).isEqualTo(NormalJavaBean.class.getName());
        softly.assertThat(data.getPackageName()).isEqualTo(NormalJavaBean.class.getPackage().getName());
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
            property(String.class, "name", "setName", false)
        );
    }

    private static Property property(Class<?> clazz, String name, String setterName, boolean shouldBeImported) {
        Property property = new Property();
        property.setName(name);
        property.setType(clazz.getSimpleName());
        property.setQualifiedName(clazz.getName());
        property.setSetterName(setterName);
        property.setShouldBeImported(shouldBeImported);
        property.setTypeClass(clazz);
        return property;
    }
}
