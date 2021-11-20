package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.Nullable;

public class PdiFacetState {
    public String sdkName;

    public String getSdkName() {
        return sdkName;
    }

    public void setSdkName(String sdkName) {
        this.sdkName = sdkName;
    }

}
