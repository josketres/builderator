package com.josketres.builderator;

import org.junit.Assert;
import org.junit.Test;

public class ConstructorAnalyserTest {

    class ConstructorWithOneParam {

        private final String name;

        public ConstructorWithOneParam(String name) {
            this.name = name;
        }
    }

    @Test
    public void test() throws Exception {

        ConstructorSignature signature = new ConstructorAnalyser().getSignature(ConstructorWithOneParam.class.getConstructors()[0]);
        Assert.assertEquals(signature.getNames().get(1), "name");
        Assert.assertEquals(signature.getClassTypes().get(1), String.class);
    }

}