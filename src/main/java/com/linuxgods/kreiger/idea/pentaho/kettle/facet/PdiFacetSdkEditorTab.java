package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.SdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.util.NlsContexts;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.openapi.roots.ui.configuration.SdkComboBoxModel.createSdkComboBoxModel;

public class PdiFacetSdkEditorTab extends FacetEditorTab {
    private final Project project;
    private final SdkComboBox sdkComboBox;
    private final JPanel rootPanel;
    private final PdiFacetConfiguration configuration;
    private Sdk selectedSdk;

    public PdiFacetSdkEditorTab(FacetEditorContext editorContext, PdiFacetConfiguration configuration) {
        this.configuration = configuration;
        this.project = editorContext.getProject();
        selectedSdk = configuration.getSdk();
        this.rootPanel = new JPanel();
        this.sdkComboBox = createSdkComboBox();
        rootPanel.add(sdkComboBox);
    }

    @Override public @NotNull JComponent createComponent() {
        return rootPanel;
    }

    @NotNull private SdkComboBox createSdkComboBox() {

        ProjectSdksModel sdksModel = ProjectStructureConfigurable.getInstance(project).getProjectJdksModel();
        var sdkComboBoxModel = createSdkComboBoxModel(project, sdksModel,
                sdkTypeId -> sdkTypeId instanceof PdiSdkType);
        SdkComboBox sdkComboBox = new SdkComboBox(sdkComboBoxModel);
        if (null != selectedSdk) sdkComboBox.setSelectedSdk(selectedSdk);
        sdkComboBox.addActionListener(e -> selectedSdk = sdkComboBox.getSelectedSdk());
        return sdkComboBox;
    }

    @Override public boolean isModified() {
        return selectedSdk != configuration.getSdk();
    }

    @Override public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Pentaho Kettle SDK";
    }

    @Override public void apply() throws ConfigurationException {
        configuration.setSdk(selectedSdk);
    }
}
