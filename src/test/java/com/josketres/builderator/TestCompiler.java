package com.josketres.builderator;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.System.getProperty;
import static org.junit.Assert.fail;

public class TestCompiler {

    private final JavaCompiler compiler;
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;
    private final MemoryFileManager fileManager;
    private final Map<String, Class<?>> classNameToClass = new HashMap<String, Class<?>>();
    private ClassLoader classLoader;

    public TestCompiler() {
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
        fileManager = new MemoryFileManager(compiler.getStandardFileManager(diagnosticCollector, Locale.getDefault(), Charsets.UTF_8));
    }

    public boolean compile(JavaFileObject... sources) {

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, // explicitly use the default because old versions of javac log some output on stderr
                fileManager,
                diagnosticCollector,
                ImmutableSet.<String>of(),
                ImmutableSet.<String>of(),
                Arrays.asList(sources));

        return task.call();
    }

    public void assertCompilationSuccess() {
        if (!diagnosticCollector.getDiagnostics().isEmpty()) {
            String lineSeparator = getProperty("line.separator");
            StringBuilder message = new StringBuilder("There was compilation failures :").append(lineSeparator);
            for (Diagnostic<? extends JavaFileObject> d : this.diagnosticCollector.getDiagnostics()) {
                message.append(d).append(lineSeparator);
            }
            fail(message.toString());
        }
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = classNameToClass.get(name);
        if (clazz == null) {
            if (classLoader == null) {
                classLoader = fileManager.getClassLoader(null);
            }
            clazz = classLoader.loadClass(name);
            classNameToClass.put(name, clazz);
        }
        return clazz;
    }
}

class MemoryJavaClassObject extends SimpleJavaFileObject {

    protected final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    public MemoryJavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    public byte[] getBytes() {
        return stream.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return stream;
    }

}

class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private Map<String, MemoryJavaClassObject> objects = new HashMap<String, MemoryJavaClassObject>();

    public MemoryFileManager(StandardJavaFileManager manager) {
        super(manager);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return new SecureClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] b = objects.get(name).getBytes();
                return super.defineClass(name, objects.get(name).getBytes(), 0, b.length);
            }
        };
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        System.out.println(location.toString());
        objects.put(name, new MemoryJavaClassObject(name, kind));
        return objects.get(name);
    }

}

class MemoryJavaFileObject extends SimpleJavaFileObject {

    private CharSequence content;

    protected MemoryJavaFileObject(String className, CharSequence content) {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }

}

