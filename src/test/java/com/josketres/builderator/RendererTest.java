package com.josketres.builderator;

import org.junit.Assert;
import org.junit.Test;
import test.classes.NormalJavaBean;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RendererTest {
    private static final int COMPILER_SUCCESS_CODE = 0;

    @Test
    public void test() throws IOException {
        MetadataExtractor generator = new MetadataExtractor(NormalJavaBean.class);
        String source = new Renderer().render(generator.getMetadata());

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
        Assert.assertEquals(COMPILER_SUCCESS_CODE, result);

    }

}