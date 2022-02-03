package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.xml.*;
import com.intellij.util.xml.*;
import com.intellij.util.xml.converters.PathReferenceConverter;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step.FakeStepPsiElement;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step.StepTypeConverter;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public interface Step extends DomElement {

    default String getNameUntrimmed() {
        XmlTag xmlTag = getXmlTag();
        if (xmlTag == null) return null;
        String nameText = xmlTag.getSubTagText("name");
        if (nameText == null) return null;
        return StringEscapeUtils.unescapeXml(nameText);
    }

    @Required
    @NameValue
    GenericDomValue<String> getName();

    @Required
    @Convert(StepTypeConverter.class)
    @Referencing(value = StepTypeConverter.class)
    GenericDomValue<StepType> getType();

    @Required
    GUI getGUI();

    @Convert(PathReferenceConverter.class)
    GenericDomValue<PathReference> getFilename();

    default int getX() {
        return getGUI().getXloc().getValue();
    }

    default int getY() {
        return getGUI().getYloc().getValue();
    }

    default Optional<Icon> getIcon() {
        return PdiFacet.getInstance(getModule())
                .flatMap(pdiFacet -> pdiFacet.getStepType(getType().getStringValue()).map(StepType::getIcon));
    }

    @NotNull
    default FakePsiElement getFakePsiElement() {
        Step step = Step.this;
        return new FakeStepPsiElement(step);
    }

    interface Type extends DomElement {
        String getValue();
    }

}
/*
    <GUI>
      <xloc>64</xloc>
      <yloc>53</yloc>
      <draw>Y</draw>
    </GUI>
 */