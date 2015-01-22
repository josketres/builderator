package com.josketres.builderator;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import test.classes.NormalJavaBean;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assert_;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BuilderatorTest {
    private static final int COMPILER_SUCCESS_CODE = 0;

    private static File root;

    private File packageFolder;

    @BeforeClass
    public static void setup() throws Exception {
        root = File.createTempFile("java", ".tmp");
        root.delete();
        root.mkdirs();
        root.deleteOnExit();
    }

    @Test
    public void test_compiles_without_error() throws Exception {

        assert_().about(JavaSourceSubjectFactory.javaSource())
                .that(JavaFileObjects.forSourceString("NormalJavaBeanBuilder",
                        Builderator.builderFor(NormalJavaBean.class)))
                .compilesWithoutError();
    }

    @Test
    public void test() throws Exception {
        File builder = createBuilder();
        File builderTester = createBuilderTester();
        assertCompilesWithoutErrors(builder, builderTester);
        assertBuilderCanBeUsed();
    }

    private void assertBuilderCanBeUsed() throws Exception {

        URLClassLoader classLoader = URLClassLoader
                .newInstance(new URL[]{root.toURI().toURL()});
        Class<?> cls = Class.forName(NormalJavaBean.class.getPackage()
                .getName() + ".BuilderTester", true, classLoader);
        @SuppressWarnings("unchecked")
        Callable<NormalJavaBean> instance = (Callable<NormalJavaBean>) cls
                .newInstance();
        NormalJavaBean constructed = instance.call();

        assertThat(constructed.getName(), is("builderTest"));
        assertThat(constructed.getAge(), is(18));
    }

    private File createBuilder() throws IOException {
        String source = Builderator.builderFor(NormalJavaBean.class);
        return createFile(source, "NormalJavaBeanBuilder",
                NormalJavaBean.class.getPackage().getName());
    }

    private File createBuilderTester() throws Exception {

        String source = "package "
                + NormalJavaBean.class.getPackage().getName()
                + ";"
                +
                " import java.util.Date;"
                +
                " public class BuilderTester implements java.util.concurrent.Callable<NormalJavaBean>  { "
                +
                " public BuilderTester(){ } "
                +
                "  @Override"
                +
                "  public NormalJavaBean call() { "
                +
                "    NormalJavaBeanBuilder builder = NormalJavaBeanBuilder.aNormalJavaBean();"
                +
                "    return builder.name(\"builderTest\")"
                +
                "      .age(18).date(new Date()).address(new Address()).build();"
                +
                "  } " +
                "}";

        return createFile(source, "BuilderTester", NormalJavaBean.class
                .getPackage().getName());
    }

    private File createFile(String source, String name, String packageName)
            throws IOException {
        String packageDirs = packageName.replace(".",
                System.getProperty("file.separator"));

        packageFolder = new File(root, packageDirs);
        packageFolder.mkdirs();

        File sourceFile = new File(packageFolder, name + ".java");
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
        return sourceFile;
    }

    private void assertCompilesWithoutErrors(File builder,
                                             File builderTester) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, System.out, System.err,
                builder.getPath(), builderTester.getPath());
        Assert.assertEquals(COMPILER_SUCCESS_CODE, result);
    }
}
