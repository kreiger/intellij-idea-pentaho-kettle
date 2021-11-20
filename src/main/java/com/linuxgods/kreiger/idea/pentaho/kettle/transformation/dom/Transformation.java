package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.*;

import java.util.List;

public interface Transformation extends DomElement {
    Order getOrder();
    List<Step> getSteps();
    Notepads getNotepads();
}
