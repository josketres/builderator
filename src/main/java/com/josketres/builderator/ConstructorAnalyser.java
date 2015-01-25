package com.josketres.builderator;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

class ConstructorAnalyser {

    public ConstructorSignature getSignature(final Class<?> targetClass) throws IOException {

        ClassLoader classLoader = targetClass.getClassLoader();
        Type type = Type.getType(targetClass);
        String url = type.getInternalName() + ".class";

        InputStream classFileInputStream = classLoader.getResourceAsStream(url);
        if (classFileInputStream == null) {
            throw new IllegalArgumentException("The constructor's class loader cannot " +
                    "find the bytecode that defined the constructor's class (URL: " + url + ")");
        }

        final ConstructorSignature ctrSignature = new ConstructorSignature();
        try {
            ClassReader classReader = new ClassReader(classFileInputStream);
            classReader.accept(new ConstructorVisitorDelegate(new MethodVisitor(Opcodes.ASM4) {

                @Override
                public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                    if (!"this".equals(name)) {
                        Constructor firstConstructor = targetClass.getDeclaredConstructors()[0];
                        Class<?> argumentType = extractArgumentType(firstConstructor, desc);
                        ctrSignature.addArgument(desc, name, signature, argumentType);
                    }
                }

                private Class<?> extractArgumentType(final Constructor<?> constructor, String desc) {
                    Class<?> argumentType = null;
                    for (Class<?> type : constructor.getParameterTypes()) {
                        String typeDesc = Type.getDescriptor(type);
                        if (typeDesc.equals(desc)) {
                            argumentType = type;
                        }
                    }
                    return argumentType;
                }
            }), 0);
        } finally {
            classFileInputStream.close();
        }

        return ctrSignature;
    }

    private static class ConstructorVisitorDelegate extends ClassVisitor {
        private final MethodVisitor constructorMethodVisitor;

        public ConstructorVisitorDelegate(MethodVisitor constructorMethodVisitor) {
            super(Opcodes.ASM4);
            this.constructorMethodVisitor = constructorMethodVisitor;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("<init>".equals(name)) {
                return constructorMethodVisitor;
            } else {
                return null;
            }
        }
    }
}
