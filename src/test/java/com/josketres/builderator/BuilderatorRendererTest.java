package com.josketres.builderator;

import org.junit.Test;
import test.classes.NormalJavaBean;

public class BuilderatorRendererTest {
    @Test
    public void test_render() throws Exception {
        BuilderatorTest.test_builderFor(renderBuilderForNormalJavaBean());
    }

    private String renderBuilderForNormalJavaBean() {
        return new BuilderatorRenderer(NormalJavaBean.class).render();
    }
}