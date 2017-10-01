package com.josketres.builderator;

import com.google.common.io.Files;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
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

@RunWith(Theories.class)
public class RendererTest {
    private static final int COMPILER_SUCCESS_CODE = 0;
    private static final String PARENT_BUILDER_CLASS = ParentBuilderClass.class.getName();
    private static final String PARENT_BUILDER_CLASS_OTHER_PACKAGE = ParentBuilderClassOtherPackage.class.getName();

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Theory
    public void test_withParentClass_abstractClass(boolean otherPackage) throws IOException {
        test(true, otherPackage, false);
    }

    @Theory
    public void test_withParentClass_concreteClass(boolean otherPackage) throws IOException {
        test(true, otherPackage, true);
    }

    @Test
    public void test_withoutParentClass_abstractClass() throws IOException {
        test(false, false, false);
    }

    @Test
    public void test_withoutParentClass_concreteClass() throws IOException {
        test(false, false, true);
    }

    private void test(boolean withParentBuilderClass, boolean otherPackage, boolean concreteClass) throws IOException {
        // prepare
        MetadataExtractor metadataExtractor = new MetadataExtractor(NormalJavaBean.class);

        String parentBuilderClassName = null;
        String parentBuilderClassIfAny = otherPackage ? PARENT_BUILDER_CLASS_OTHER_PACKAGE : PARENT_BUILDER_CLASS;
        if (withParentBuilderClass) {
            parentBuilderClassName = parentBuilderClassIfAny;
        }
        TargetClass targetClass = metadataExtractor.getMetadata();

        // test
        String source = new Renderer().render(targetClass, parentBuilderClassName, concreteClass);

        // verify
        softly.assertThat(compile(targetClass, source)).isEqualTo(COMPILER_SUCCESS_CODE);

        String buildMethod = BUILD_METHOD + "()";
        String factoryMethod = Renderer.getFactoryMethod(targetClass) + "()";
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
              .contains(format("public class %s%s", getBuilderClassName(targetClass), concreteClass ? ' ' : '<'))
              .contains(format("%s %s(", concreteClass ? "public" : "protected", getBuilderClassName(targetClass)))
              .contains("List<String> petNames;").contains("petNames(List<String> petNames)");
    }

    private int compile(TargetClass targetClass, String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File packageFolder = temporaryFolder.newFolder(targetClass.getPackageName().split("."));
        File sourceFile = new File(packageFolder, targetClass.getName() + "Builder.java");
        Files.write(source.getBytes(), sourceFile);

        return compiler.run(null, System.out, System.err, sourceFile.getPath());
    }

}