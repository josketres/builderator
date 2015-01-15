package com.josketres.builderator;

import com.josketres.builderator.model.TargetClass;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class Renderer {

    private final STGroup templates;

    public Renderer() {
        templates = new STGroupFile("builder.stg");
    }

    public String render(TargetClass targetClass) {
        ST st = templates.getInstanceOf("builder");
        st.add("data", targetClass);
        return st.render();
    }

}
