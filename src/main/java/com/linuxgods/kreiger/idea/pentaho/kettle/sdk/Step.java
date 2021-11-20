package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import org.jdom.Element;

import static java.util.Objects.requireNonNull;

public class Step {
    private final String id;
    private final String image;

    public Step(String id, String image) {
        this.id = requireNonNull(id);
        this.image = requireNonNull(image);
    }

    public Step(Element element) {
        this(element.getAttributeValue("id"), element.getAttributeValue("image"));
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }
}
