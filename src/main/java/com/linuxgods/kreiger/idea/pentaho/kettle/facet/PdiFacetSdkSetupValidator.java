package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator;
import com.intellij.facet.FacetManager;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ui.configuration.SdkPopupFactory;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.linuxgods.kreiger.idea.pentaho.kettle.PentahoKettleFileType;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PdiFacetSdkSetupValidator implements ProjectSdkSetupValidator {
    @Override public boolean isApplicableFor(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getFileType() instanceof PentahoKettleFileType;
    }

    @Override
    public @Nullable @NlsContexts.Label String getErrorMessage(@NotNull Project project, @NotNull VirtualFile file) {
        Optional<Sdk> sdk = PdiFacet.getInstance(project, file).flatMap(PdiFacet::getSdk);
        return sdk.isPresent() ? null : "No Pentaho Data Integration SDK defined for this module!" ;
    }

    @Override
    public @NotNull EditorNotificationPanel.ActionHandler getFixHandler(@NotNull Project project, @NotNull VirtualFile file) {
        return SdkPopupFactory.newBuilder()
                .withProject(project)
                .withSdkTypeFilter(type -> type instanceof PdiSdkType)
                .registerNewSdk()
                .onSdkSelected(sdk -> {
                    WriteAction.run(() -> {
                        PdiFacet.findModuleForFile(project, file).ifPresent(module -> {
                            FacetManager facetManager = FacetManager.getInstance(module);
                            PdiFacet pdiFacet = FacetManager.getInstance(module).getFacetByType(PdiFacetType.ID);
                            if (null == pdiFacet) {
                                pdiFacet = FacetUtil.addFacet(module, PdiFacetType.INSTANCE, PdiFacetType.NAME);
                            }
                            pdiFacet.getConfiguration().setSdk(sdk);
                            facetManager.facetConfigurationChanged(pdiFacet);
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            Stream.of(fileEditorManager.getOpenFiles())
                                    .filter(virtualFile -> virtualFile.getFileType() instanceof PentahoKettleFileType)
                                    .forEach(virtualFile -> {
                                        fileEditorManager.closeFile(virtualFile);
                                        fileEditorManager.openFile(virtualFile, false);
                                    });
                        });
                    });
                })
                .buildEditorNotificationPanelHandler();
    }
}
