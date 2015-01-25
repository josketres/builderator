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

    static class ConstructorWithTwoParams {

        private final String name;
        private final String lastName;

        public ConstructorWithTwoParams(String name, String lastName) {
            this.name = name;
            this.lastName = lastName;
        }
    }

    @Test
    public void should_extract_signature_of_constructor_with_one_param() throws Exception {


        Constructor<?>[] declaredConstructors = ConstructorWithOneParam.class.getDeclaredConstructors();
        assertEquals(declaredConstructors.length, 1);

        ConstructorSignature signature = new ConstructorAnalyser().getSignature(ConstructorWithOneParam.class);
        assertEquals("name", signature.getNames().get(0));
        assertEquals(String.class, signature.getClassTypes().get(0));
    }

    @Test
    public void should_extract_signature_of_constructor_with_two_params() throws Exception {


        Constructor<?>[] declaredConstructors = ConstructorWithTwoParams.class.getDeclaredConstructors();
        assertEquals(declaredConstructors.length, 1);

        ConstructorSignature signature = new ConstructorAnalyser().getSignature(ConstructorWithTwoParams.class);
        assertEquals("name", signature.getNames().get(0));
        assertEquals(String.class, signature.getClassTypes().get(0));
        assertEquals("lastName", signature.getNames().get(1));
        assertEquals(String.class, signature.getClassTypes().get(1));
    }

}