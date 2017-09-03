package com.josketres.builderator;

import org.junit.Test;
import test.classes.NormalJavaBean;

import static com.josketres.builderator.BuilderatorFacade.builderFor;
import static com.josketres.builderator.BuilderatorTest.renderNormalJavaBeanBuilder;

public class BuilderatorFacadeTest {
    @Test
    public void test_builderFor() throws Exception {
        renderNormalJavaBeanBuilder(builderFor(NormalJavaBean.class));
    }
}
