package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.intellij.openapi.projectRoots.ProjectJdkTable.Listener;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkType;
import org.jetbrains.annotations.NotNull;

public class PdiProjectJdkTableListener implements Listener {
    @NotNull private String getSdkFacetLibraryName(@NotNull Sdk sdk) {
        return sdk.getName() + " facet library";
    }

    @Override public void jdkAdded(@NotNull Sdk sdk) {
        if (!(sdk.getSdkType() instanceof PdiSdkType)) return;
        LibraryTable globalLibraries = LibraryTablesRegistrar.getInstance().getLibraryTable();
        Library library = globalLibraries.createLibrary(getSdkFacetLibraryName(sdk));
        String[] urls = sdk.getRootProvider().getUrls(OrderRootType.CLASSES);
        Library.ModifiableModel model = library.getModifiableModel();
        for (String url : urls) {
            model.addRoot(url, OrderRootType.CLASSES);
        }
        model.commit();
    }

    @Override public void jdkRemoved(@NotNull Sdk sdk) {
        if (!(sdk.getSdkType() instanceof PdiSdkType)) return;
        //ApplicationManager.getApplication().invokeLater(() -> {
        //    WriteAction.run(() -> {
                LibraryTable.ModifiableModel globalLibraries = LibraryTablesRegistrar.getInstance().getLibraryTable().getModifiableModel();
                String sdkFacetLibraryName = getSdkFacetLibraryName(sdk);
                Library library = globalLibraries.getLibraryByName(sdkFacetLibraryName);
                if (null != library) {
                    globalLibraries.removeLibrary(library);
                }
                globalLibraries.commit();
          //  });
        //}, ModalityState.NON_MODAL);
    }
}
