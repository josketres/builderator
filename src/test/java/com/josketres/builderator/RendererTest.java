package com.josketres.builderator;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import test.classes.NormalJavaBean;
import test.classes.ParentBuilderClass;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.josketres.builderator.Renderer.BUILD_METHOD;
import static com.josketres.builderator.Renderer.simpleName;

public class RendererTest {
    private static final int COMPILER_SUCCESS_CODE = 0;
    private static final String PARENT_BUILDER_CLASS = ParentBuilderClass.class.getName();

    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void test_withParentClass_abstractClass() throws IOException {
        test(true, false);
    }

    @Test
    public void test_withParentClass_concreteClass() throws IOException {
        test(true, true);
    }

    @Test
    public void test_withoutParentClass_abstractClass() throws IOException {
        test(false, false);
    }

    @Test
    public void test_withoutParentClass_concreteClass() throws IOException {
        test(false, true);
    }

    private void test(boolean withParentBuilderClass, boolean concreteClass) throws IOException {
        MetadataExtractor metadataExtractor = new MetadataExtractor(NormalJavaBean.class);
        String parentBuilderClassName = withParentBuilderClass ? PARENT_BUILDER_CLASS : null;
        TargetClass targetClass = metadataExtractor.getMetadata();
        String source = new Renderer().render(targetClass, parentBuilderClassName, concreteClass);

        File root = File.createTempFile("java", null);
        root.delete();
        root.mkdirs();

        String packageDirs = NormalJavaBean.class.getPackage().getName().replace(".", System.getProperty("file.separator"));

        File packageFolder = new File(root, packageDirs);
        packageFolder.mkdirs();

        File sourceFile = new File(packageFolder,
                NormalJavaBean.class.getName().replace(".", System.getProperty("file.separator")) + "Builder.java");
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

        String extendsParentClause = "extends " + simpleName(PARENT_BUILDER_CLASS);
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
    }
}