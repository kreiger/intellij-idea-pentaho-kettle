package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;

public class PdiFacetConfiguration implements FacetConfiguration, PersistentStateComponent<PdiFacetState> {
    private PdiFacetState state = new PdiFacetState();

    public PdiFacetConfiguration() {
    }

    public PdiFacetConfiguration(Sdk sdk) {
        setSdk(sdk);
    }

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[] { new PdiFacetSdkEditorTab(editorContext, this)};
    }

    public Sdk getSdk() {
        PdiFacetState state = getState();
        String sdkName = state.getSdkName();
        if (sdkName == null) {
            return null;
        }

        ProjectJdkTable sdksTable = ProjectJdkTable.getInstance();
        Sdk sdk = sdksTable.findJdk(sdkName);

        return sdk;
    }

    public void setSdk(Sdk sdk) {
        getState().setSdkName(sdk == null ? null : sdk.getName());
    }


    @Override @NotNull public PdiFacetState getState() {
        if (state == null) {
            state = new PdiFacetState();
        }
        return state;
    }

    @Override public void loadState(@NotNull PdiFacetState state) {
        this.state = state;
    }
}
