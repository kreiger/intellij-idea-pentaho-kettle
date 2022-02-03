package com.linuxgods.kreiger.idea.pentaho.kettle.job.dom;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.*;
import com.linuxgods.kreiger.idea.pentaho.kettle.KettleIcons;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.JobEntryType;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step.StepTypeConverter;
import org.apache.commons.lang.StringEscapeUtils;

import javax.swing.*;
import java.util.Optional;

public interface Entry extends DomElement {
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
    GenericDomValue<JobEntryType> getType();

    default Optional<Icon> getIcon() {
        if (isStart()) {
            return Optional.of(KettleIcons.START_ICON); // TODO Get from SDK
        }
        if (isDummy()) {
            return Optional.of(KettleIcons.DUMMY_ICON); // TODO Get from SDK
        }

        String type = getType().getStringValue();
        if ("SPECIAL".equals(type)) {
            return Optional.empty();
        }

        return PdiFacet.getInstance(getModule())
                .flatMap(pdiFacet -> pdiFacet.getJobEntryType(type))
                .map(JobEntryType::getIcon);
    }

    GenericDomValue<Integer> getXloc();

    GenericDomValue<Integer> getYloc();

    default boolean isDraw() {
        return "Y".equals(getDraw().getStringValue());
    }
    GenericDomValue<String> getDraw();

    default boolean isStart() {
        return "Y".equals(getStart().getStringValue());
    }

    GenericDomValue<String> getStart();

    default boolean isDummy() {
        return "Y".equals(getDummy().getStringValue());
    }
    GenericDomValue<String> getDummy();
}
