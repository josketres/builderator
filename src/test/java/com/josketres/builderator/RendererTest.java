package com.josketres.builderator;

import com.google.common.io.Files;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import test.classes.AbstractClass;
import test.classes.NormalJavaBean;
import test.classes.ParentBuilderClass;
import test.classes.pkg.ParentBuilderClassOtherPackage;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;

import static com.josketres.builderator.Renderer.BUILD_METHOD;
import static com.josketres.builderator.Renderer.getBuilderClassName;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isAbstract;

@RunWith(Theories.class)
public class RendererTest {
    private static final int COMPILER_SUCCESS_CODE = 0;
    private static final String PARENT_BUILDER_CLASS = ParentBuilderClass.class.getName();
    private static final String PARENT_BUILDER_CLASS_OTHER_PACKAGE = ParentBuilderClassOtherPackage.class.getName();

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Theory
    public void test_withParentClass_abstractClass(boolean otherPackage, boolean abstractModifier) throws IOException {
        test(true, otherPackage, false, abstractModifier);
    }

    @Theory
    public void test_withParentClass_concreteClass(boolean otherPackage, boolean abstractModifier) throws IOException {
        test(true, otherPackage, true, abstractModifier);
    }

    @Theory
    public void test_withoutParentClass_abstractClass(boolean abstractModifier) throws IOException {
        test(false, false, false, abstractModifier);
    }

    @Theory
    public void test_withoutParentClass_concreteClass(boolean abstractModifier) throws IOException {
        test(false, false, true, abstractModifier);
    }

    private void test(boolean withParentBuilderClass, boolean otherPackage, boolean concreteClass,
                      boolean abstractModifier) throws IOException {
        // prepare
        Class<?> targetClass = abstractModifier ? AbstractClass.class : NormalJavaBean.class;
        if (isAbstract(targetClass.getModifiers())) {
            concreteClass = false;
        }

        MetadataExtractor metadataExtractor = new MetadataExtractor(targetClass);

        String parentBuilderClassName = null;
        String parentBuilderClassIfAny = otherPackage ? PARENT_BUILDER_CLASS_OTHER_PACKAGE : PARENT_BUILDER_CLASS;
        if (withParentBuilderClass) {
            parentBuilderClassName = parentBuilderClassIfAny;
        }
        TargetClass metadata = metadataExtractor.getMetadata();

        // test
        String source = new Renderer(Converters.getInstance()).render(metadata, parentBuilderClassName, concreteClass);

        // verify
        softly.assertThat(compile(metadata, source)).isEqualTo(COMPILER_SUCCESS_CODE);

        String buildMethod = BUILD_METHOD + "()";
        String factoryMethod = Renderer.getFactoryMethod(metadata) + "()";
        String extendsParentClause = " extends " + parentBuilderClassIfAny;

        if (parentBuilderClassName == null) {
            softly.assertThat(source).doesNotContain(extendsParentClause);
        } else {
            softly.assertThat(source).contains(extendsParentClause);
        }

        if (concreteClass) {
            softly.assertThat(source).contains(buildMethod)
                  .contains(factoryMethod);
        } else {
            softly.assertThat(source).doesNotContain(buildMethod)
                  .doesNotContain(factoryMethod);
        }

        softly.assertThat(source)
              .contains(
                  format("public %sclass %s%s", abstractModifier ? "abstract " : "", getBuilderClassName(metadata),
                         concreteClass ? ' ' : '<'))
              .contains(format("%s %s(", concreteClass ? "public" : "protected", getBuilderClassName(metadata)));

        if (!abstractModifier) {
            softly.assertThat(source).contains("List<String> petNames;").contains("petNames(List<String> petNames)");
        }
    }

    private int compile(TargetClass targetClass, String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File packageFolder = temporaryFolder.newFolder(targetClass.getPackageName().split("."));
        File sourceFile = new File(packageFolder, targetClass.getName() + "Builder.java");
        Files.write(source.getBytes(), sourceFile);

        return compiler.run(null, System.out, System.err, sourceFile.getPath());
    }

}