package com.linuxgods.kreiger.idea.pentaho.kettle.job.dom;

import com.intellij.util.xml.DomFileDescription;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;

public class JobDomFileDescription extends DomFileDescription<Job> {
    public JobDomFileDescription() {
        super(Job.class, "job");
    }

}
