package com.josketres.builderator;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import test.classes.NormalJavaBean;

import static com.josketres.builderator.BuilderatorFacade.builderFor;
import static com.josketres.builderator.BuilderatorTest.renderNormalJavaBeanBuilder;

public class BuilderatorFacadeTest {
    @Rule public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void test_builderFor() throws Exception {
        renderNormalJavaBeanBuilder(softly, builderFor(NormalJavaBean.class));
    }
}
