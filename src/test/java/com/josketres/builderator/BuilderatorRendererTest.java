package com.josketres.builderator;

import org.junit.Test;
import test.classes.NormalJavaBean;

public class BuilderatorRendererTest {
    @Test
    public void test_builder_compiles_without_error() throws Exception {
        BuilderatorTest.test_builder_compiles_without_error(renderBuilderForNormalJavaBean());
    }

    @Test
    public void test_builder_tester_compiles_without_error() throws Exception {
        BuilderatorTest.test_builder_tester_compiles_without_error(renderBuilderForNormalJavaBean());
    }

    @Test
    public void test_compiles_and_can_be_used() throws Exception {
        BuilderatorTest.test_compiles_and_can_be_used(renderBuilderForNormalJavaBean());
    }

    private String renderBuilderForNormalJavaBean() {
        return new BuilderatorRenderer(NormalJavaBean.class).render();
    }
}