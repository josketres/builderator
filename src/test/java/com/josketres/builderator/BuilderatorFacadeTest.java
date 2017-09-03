package com.josketres.builderator;

import org.junit.Test;
import test.classes.NormalJavaBean;

import static com.josketres.builderator.BuilderatorFacade.builderFor;
import static com.josketres.builderator.BuilderatorTest.testNormalJavaBeanBuilder;

public class BuilderatorFacadeTest {
    @Test
    public void test_builderFor() throws Exception {
        testNormalJavaBeanBuilder(builderFor(NormalJavaBean.class));
    }
}
