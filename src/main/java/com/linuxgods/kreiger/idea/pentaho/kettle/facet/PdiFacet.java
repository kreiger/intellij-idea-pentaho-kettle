package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class PdiFacet extends Facet<PdiFacetConfiguration> {
    public PdiFacet(@NotNull Module module, @NotNull @NlsSafe String name, @NotNull PdiFacetConfiguration configuration, Facet underlyingFacet) {
        super(PdiFacetType.INSTANCE, module, name, configuration, underlyingFacet);
    }

    public static PdiFacet getInstance(@NotNull Module module) {
        return FacetManager.getInstance(module).getFacetByType(PdiFacetType.ID);
    }

    public static PdiFacet getInstance(@NotNull Project project, @NotNull VirtualFile file) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        return getInstance(module);
    }
}
