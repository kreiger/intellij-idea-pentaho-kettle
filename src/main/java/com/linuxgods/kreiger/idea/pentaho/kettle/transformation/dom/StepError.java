package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.NameStrategy;
import com.linuxgods.kreiger.idea.pentaho.kettle.dom.SnakeNameStrategy;

@NameStrategy(SnakeNameStrategy.class)
public interface StepError extends DomElement {
    GenericDomValue<String> getSourceStep();
    GenericDomValue<String> getTargetStep();
    GenericDomValue<String> getIsEnabled();
    default boolean isEnabled() {
        return "Y".equals(getIsEnabled().getStringValue());
    };
}
