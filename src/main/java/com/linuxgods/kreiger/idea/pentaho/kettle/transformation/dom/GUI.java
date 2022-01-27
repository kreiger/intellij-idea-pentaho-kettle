package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;

public interface GUI extends DomElement {
    GenericDomValue<Integer> getXloc();

    GenericDomValue<Integer> getYloc();

}
