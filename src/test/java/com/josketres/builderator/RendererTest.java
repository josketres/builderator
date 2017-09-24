package com.josketres.builderator;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import test.classes.NormalJavaBean;
import test.classes.ParentBuilderClass;
import test.classes.pkg.ParentBuilderClassOtherPackage;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.josketres.builderator.Renderer.BUILD_METHOD;
import static com.josketres.builderator.Renderer.getBuilderClassName;
import static java.lang.String.format;

@RunWith(Theories.class)
public class RendererTest {
    private static final int COMPILER_SUCCESS_CODE = 0;
    private static final String PARENT_BUILDER_CLASS = ParentBuilderClass.class.getName();
    private static final String PARENT_BUILDER_CLASS_OTHER_PACKAGE = ParentBuilderClassOtherPackage.class.getName();

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
        MetadataExtractor metadataExtractor = new MetadataExtractor(NormalJavaBean.class);

        String parentBuilderClassName = null;
        String parentBuilderClassIfAny = otherPackage ? PARENT_BUILDER_CLASS_OTHER_PACKAGE : PARENT_BUILDER_CLASS;
        String extendsParentClause = " extends " + parentBuilderClassIfAny;
        if (withParentBuilderClass) {
            parentBuilderClassName = parentBuilderClassIfAny;
        }

        TargetClass targetClass = metadataExtractor.getMetadata();
        String source = new Renderer().render(targetClass, parentBuilderClassName, concreteClass);

        File root = File.createTempFile("java", null);
        root.delete();
        root.mkdirs();

        String packageDirs = targetClass.getPackageName().replace(".", System.getProperty("file.separator"));

        File packageFolder = new File(root, packageDirs);
        packageFolder.mkdirs();

        File sourceFile = new File(packageFolder,
                                   targetClass.getName().replace(".", System.getProperty("file.separator"))
                                   + "Builder.java");
        sourceFile.getParentFile().mkdirs();
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(sourceFile);
            fileWriter.append(source);
        } finally {
            if (fileWriter != null) {
                fileWriter.close();
            }
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, System.out, System.err, sourceFile.getPath());
        softly.assertThat(result).isEqualTo(COMPILER_SUCCESS_CODE);

        String buildMethod = BUILD_METHOD + "()";
        String factoryMethod = Renderer.getFactoryMethod(targetClass) + "()";

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
              .contains(format("protected %s(", getBuilderClassName(targetClass)));

    }
}