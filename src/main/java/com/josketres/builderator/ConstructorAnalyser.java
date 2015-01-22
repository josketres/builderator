package com.josketres.builderator;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

class ConstructorAnalyser {

    public ConstructorSignature getSignature(
            final Constructor<?> constructor)
            throws IOException {
        Class<?> declaringClass = constructor.getDeclaringClass();
        ClassLoader declaringClassLoader = declaringClass.getClassLoader();

        Type declaringType = Type.getType(declaringClass);
        String url = declaringType.getInternalName() + ".class";
        String constructorDescriptor = Type
                .getConstructorDescriptor(constructor);
        System.out.println(url);
        System.out.println(constructorDescriptor);

        InputStream classFileInputStream = declaringClassLoader
                .getResourceAsStream(url);
        if (classFileInputStream == null) {
            throw new IllegalArgumentException(
                    "The constructor's class loader cannot find the bytecode that defined the constructor's class (URL: "
                            + url + ")");
        }

        final ConstructorSignature ctrSignature = new ConstructorSignature();
        ClassVisitor classVisitor;
        try {
            classVisitor = new ClassVisitor(Opcodes.ASM4) {

                @Override
                public void visitSource(String source, String debug) {
                    System.out.println(source);
                }

                @Override
                public MethodVisitor visitMethod(int access, String name,
                                                 String desc, String signature, String[] exceptions) {
                    if ("<init>".equals(name)) {
                        return new MethodVisitor(Opcodes.ASM4) {

                            public void visitLocalVariable(String name,
                                                           String desc, String signature, Label start,
                                                           Label end, int index) {
                                if (!"this".equals(name)) {
                                    Class<?> argumentType = extractArgumentType(constructor, desc);
                                    ctrSignature.addArgument(desc, name, signature, argumentType);
                                }


                                System.out.println("index: " + index);
                                System.out.println("name: " + name);
                                System.out.println("signature: "
                                        + signature);
                                System.out.println("desc: " + desc);

                            }

                            private Class<?> extractArgumentType(
                                    final Constructor<?> constructor,
                                    String desc) {
                                Class<?> argumentType = null;
                                for (Class<?> type : constructor.getParameterTypes()) {
                                    String typeDesc = Type.getDescriptor(type);
                                    if (typeDesc.equals(desc)) {
                                        argumentType = type;
                                    }
                                }
                                return argumentType;
                            }

                            ;
                        };
                    }
                    return null;
                }
            };
            ClassReader classReader = new ClassReader(classFileInputStream);
            classReader.accept(classVisitor, 0);
        } finally {
            classFileInputStream.close();
        }

        return ctrSignature;
    }
}
