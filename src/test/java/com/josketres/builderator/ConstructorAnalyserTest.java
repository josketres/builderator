package com.josketres.builderator;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;

public class ConstructorAnalyserTest {

    static class ConstructorWithOneParam {

        private final String name;

        public ConstructorWithOneParam(String name) {
            this.name = name;
        }
    }

    @Test
    public void test() throws Exception {


        Constructor<?>[] declaredConstructors = ConstructorWithOneParam.class.getDeclaredConstructors();
        assertEquals(declaredConstructors.length, 1);

        ConstructorSignature signature = new ConstructorAnalyser().getSignature(ConstructorWithOneParam.class);
        assertEquals("name", signature.getNames().get(0));
        assertEquals(String.class, signature.getClassTypes().get(0));
    }

}