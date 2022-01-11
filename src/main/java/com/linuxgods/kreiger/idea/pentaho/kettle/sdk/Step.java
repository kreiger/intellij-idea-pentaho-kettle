package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import org.jdom.Element;

import static java.util.Objects.requireNonNull;

public class Step {
    private final String id;
    private final String image;
    private final String className;

    public Step(String id, String image, String className) {
        this.id = requireNonNull(id);
        this.image = requireNonNull(image);
        this.className = className;
    }

    public Step(Element element) {
        this(element.getAttributeValue("id"), element.getAttributeValue("image"), element.getAttributeValue("classname"));
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getClassName() {
        return className;
    }
}
