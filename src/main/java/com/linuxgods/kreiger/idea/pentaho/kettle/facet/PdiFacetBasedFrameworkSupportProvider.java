package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurable;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PdiFacetBasedFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<PdiFacet> {
    public PdiFacetBasedFrameworkSupportProvider() {
        super(PdiFacetType.INSTANCE);
    }

    @Override
    public void setupConfiguration(PdiFacet facet, ModifiableRootModel rootModel, FrameworkVersion version) {
        System.out.println("setupConfiguration");
        @NotNull PdiFacetConfiguration configuration = facet.getConfiguration();
        Sdk sdk = configuration.getSdk();
        if (sdk == null) {
            return;
        }
        System.out.println("setupConfiguration sdk: "+sdk.getName());
        String[] urls = sdk.getRootProvider().getUrls(OrderRootType.CLASSES);

        LibraryTable globalLibraries = LibraryTablesRegistrar.getInstance().getLibraryTable();
        Library library = globalLibraries.createLibrary(sdk.getName() + " facet library");
        Library.ModifiableModel modifiableModel = library.getModifiableModel();
        for (String url : urls) {
            modifiableModel.addRoot(url, OrderRootType.CLASSES);
        }
        modifiableModel.commit();
    }

    @Override
    protected void addSupport(@NotNull Module module, @NotNull ModifiableRootModel rootModel, FrameworkVersion version, @Nullable Library library) {
        System.out.println("addSupport");
        super.addSupport(module, rootModel, version, library);
    }

    @Override public boolean isSupportAlreadyAdded(@NotNull Module module) {
        System.out.println("isSupportAlreadyAdded(module)");
        return super.isSupportAlreadyAdded(module);
    }

    @Override public boolean isSupportAlreadyAdded(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
        System.out.println("isSupportAlreadyAdded(module, facetsProvider)");
        return super.isSupportAlreadyAdded(module, facetsProvider);
    }

    @Override public @NotNull FrameworkSupportConfigurable createConfigurable(@NotNull FrameworkSupportModel model) {
        System.out.println("createConfigurable(model)");
        return super.createConfigurable(model);
    }
}
