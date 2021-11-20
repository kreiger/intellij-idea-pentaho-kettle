package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator;
import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.SdkPopupFactory;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkType;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.TransformationFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public class PdiFacetSdkSetupValidator implements ProjectSdkSetupValidator {
    @Override public boolean isApplicableFor(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getFileType() instanceof TransformationFileType;
    }

    @Override
    public @Nullable @NlsContexts.Label String getErrorMessage(@NotNull Project project, @NotNull VirtualFile file) {
        PdiFacet pdiFacet = PdiFacet.getInstance(project, file);
        return pdiFacet != null && pdiFacet.getConfiguration().getSdk() != null ? null : "No Pentaho Data Integration SDK defined for this module!";
    }

    @Override
    public @NotNull EditorNotificationPanel.ActionHandler getFixHandler(@NotNull Project project, @NotNull VirtualFile file) {
        return SdkPopupFactory.newBuilder()
                .withProject(project)
                .withSdkTypeFilter(type -> type instanceof PdiSdkType)
                .onSdkSelected(sdk -> {
                    WriteAction.run(() -> {
                        Module module = ModuleUtilCore.findModuleForFile(file, project);
                        FacetManager facetManager = FacetManager.getInstance(module);
                        PdiFacet pdiFacet = FacetManager.getInstance(module).getFacetByType(PdiFacetType.ID);
                        if (null == pdiFacet) {
                            pdiFacet = FacetUtil.addFacet(module, PdiFacetType.INSTANCE, PdiFacetType.NAME);
                        }
                        pdiFacet.getConfiguration().setSdk(sdk);
                        facetManager.facetConfigurationChanged(pdiFacet);
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        Stream.of(fileEditorManager.getOpenFiles())
                                .filter(virtualFile -> virtualFile.getFileType() instanceof TransformationFileType)
                                .forEach(virtualFile -> {
                                    fileEditorManager.closeFile(virtualFile);
                                    fileEditorManager.openFile(virtualFile, false);
                                });
                    });
                })
                .buildEditorNotificationPanelHandler();
    }
}
