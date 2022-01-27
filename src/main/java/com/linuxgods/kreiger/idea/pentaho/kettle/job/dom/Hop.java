package com.linuxgods.kreiger.idea.pentaho.kettle.job.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;

public interface Hop extends DomElement {
    @Required
    GenericDomValue<Entry> getFrom();
    @Required
    GenericDomValue<Entry> getTo();

    @Required
    GenericDomValue<String> getEnabled();
}
