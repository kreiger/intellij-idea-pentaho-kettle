package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.KettleIcons;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.Stream;

import static com.intellij.openapi.roots.DependencyScope.RUNTIME;

public class PdiFacetType extends FacetType<PdiFacet, PdiFacetConfiguration> {
    public static final FacetTypeId<PdiFacet> ID = new FacetTypeId<>("PentahoKettle");
    public static final String NAME = "Pentaho Data Integration (Kettle)";
    public static final PdiFacetType INSTANCE = new PdiFacetType();

    public PdiFacetType() {
        super(ID, "PENTAHO_KETTLE", NAME);
    }

    @Override public PdiFacetConfiguration createDefaultConfiguration() {
        var configuration = new PdiFacetConfiguration();
        List<Sdk> sdks = ProjectJdkTable.getInstance().getSdksOfType(PdiSdkType.INSTANCE);
        if (sdks.size() > 0) {
            configuration.setSdk(sdks.get(0));
        }

        return configuration;
    }

    @Override
    public PdiFacet createFacet(@NotNull Module module, @NlsSafe String name, @NotNull PdiFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new PdiFacet(module, name, configuration, underlyingFacet);
    }

    @Override public @Nullable Icon getIcon() {
        return KettleIcons.KETTLE_ICON;
    }

    @Override public boolean isSuitableModuleType(ModuleType moduleType) {
        return true;
    }
}
