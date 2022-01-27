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
        System.out.println("createDefaultConfiguration");
        var configuration = new PdiFacetConfiguration();
        List<Sdk> sdks = ProjectJdkTable.getInstance().getSdksOfType(PdiSdkType.INSTANCE);
        if (sdks.size() > 0) {
            configuration.setSdk(sdks.get(0));
        }

        return configuration;
    }

    @Override
    public PdiFacet createFacet(@NotNull Module module, @NlsSafe String name, @NotNull PdiFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        System.out.println("createFacet");
        Sdk sdk = configuration.getSdk();
        if (null != sdk) {
            addSdkLibrary(module, sdk);
        }
        return new PdiFacet(module, name, configuration, underlyingFacet);
    }

    private void addSdkLibrary(@NotNull Module module, Sdk sdk) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            application.runWriteAction(() -> {
                LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable();
                Library finalLibrary = addLibrary(libraryTable, sdk);
                application.invokeLater(() -> application.runWriteAction(() -> {
                    if (!libraryExists(module, finalLibrary)) {
                        ModuleRootModificationUtil.addDependency(module, finalLibrary, RUNTIME, false);
                    }
                }));
            });
        });
    }

    @NotNull private Library addLibrary(LibraryTable libraryTable, Sdk sdk) {
        Library library = libraryTable.getLibraryByName(getLibraryName(sdk));
        if (null == library) {
            library = createLibrary(libraryTable, sdk);
        }
        return library;
    }

    private boolean libraryExists(@NotNull Module module, Library library) {
        return Stream.of(ModuleRootManager.getInstance(module).getOrderEntries())
                .filter(orderEntry -> orderEntry instanceof LibraryOrderEntry)
                .map(orderEntry -> (LibraryOrderEntry) orderEntry)
                .anyMatch(libraryOrderEntry -> library.equals(libraryOrderEntry.getLibrary()));
    }

    @NotNull private Library createLibrary(LibraryTable libraryTable, Sdk sdk) {
        String libraryName = getLibraryName(sdk);
        Library library = libraryTable.createLibrary(libraryName);
        Library.ModifiableModel libraryModel = library.getModifiableModel();
        libraryModel.setName(libraryName);
        for (VirtualFile virtualFile : sdk.getRootProvider().getFiles(OrderRootType.CLASSES)) {
            libraryModel.addRoot(virtualFile, OrderRootType.CLASSES);
        }
        libraryModel.commit();
        return library;
    }

    @NlsSafe @NotNull private String getLibraryName(Sdk sdk) {
        return sdk.getName();
    }

    @Override public @Nullable Icon getIcon() {
        return KettleIcons.KETTLE_ICON;
    }

    @Override public boolean isSuitableModuleType(ModuleType moduleType) {
        return true;
    }
}
