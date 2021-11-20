package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;

import java.util.List;

public interface Order extends DomElement {
    List<Hop> getHops();
}
