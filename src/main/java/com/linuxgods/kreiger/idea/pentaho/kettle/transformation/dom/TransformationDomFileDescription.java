package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomFileDescription;

public class TransformationDomFileDescription extends DomFileDescription<Transformation> {
    public TransformationDomFileDescription() {
        super(Transformation.class, "transformation");
    }

}
