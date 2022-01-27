package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.intellij.lang.xml.XMLLanguage;

public class JobLanguage extends XMLLanguage {
    public static JobLanguage INSTANCE = new JobLanguage();

    protected JobLanguage() {
        super(XMLLanguage.INSTANCE, "PentahoKettleJob");
    }
}
