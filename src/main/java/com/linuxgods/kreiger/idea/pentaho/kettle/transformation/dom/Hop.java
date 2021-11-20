package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;

public interface Hop extends DomElement {
    GenericDomValue<Step> getFrom();
    GenericDomValue<Step> getTo();

}
