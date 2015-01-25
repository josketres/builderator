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

public class TestCompiler {

    private final JavaCompiler compiler;
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector;
    private final MemoryFileManager fileManager;

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

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return fileManager.getClassLoader(null).loadClass(name);
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

