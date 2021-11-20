package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.lang.xml.XMLLanguage;

public class TransformationLanguage extends XMLLanguage {
    public static TransformationLanguage INSTANCE = new TransformationLanguage();

    protected TransformationLanguage() {
        super(XMLLanguage.INSTANCE, "PentahoKettleTransformation");
    }
}
