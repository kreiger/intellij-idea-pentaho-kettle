package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;

public interface Hop extends DomElement {
    @Required
    GenericDomValue<Step> getFrom();
    @Required
    GenericDomValue<Step> getTo();

    @Required()
    GenericDomValue<String> getEnabled();
}
