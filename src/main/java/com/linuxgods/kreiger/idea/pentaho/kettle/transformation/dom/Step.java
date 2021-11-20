package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.openapi.paths.PathReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.*;
import com.intellij.util.xml.converters.PathReferenceConverter;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.Objects;

public interface Step extends DomElement {
    @PropertyAccessor("name")
    @NameValue
    default String getName() {
        XmlTag xmlTag = Objects.requireNonNull(this.getXmlTag());
        return StringEscapeUtils.unescapeXml(Objects.requireNonNull(xmlTag.getSubTagText("name")));
    }

    Type getType();
    GUI getGUI();

    @Convert(PathReferenceConverter.class)
    GenericDomValue<PathReference> getFilename();

    interface Name extends DomElement {
        @NameValue
        default String getUntrimmedValue() {
            return StringEscapeUtils.unescapeXml(this.getXmlTag().getValue().getText());
        }
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