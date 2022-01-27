package com.linuxgods.kreiger.idea.pentaho.kettle.job.dom;

import com.intellij.util.xml.DomElement;

import java.util.List;

public interface Hops extends DomElement {
    List<Hop> getHops();
}
