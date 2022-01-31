package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.intellij.facet.ProjectFacetListener;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.JavaSyntheticLibrary;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.KettleIcons;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

public class PdiSdkAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider implements ProjectFacetListener<PdiFacet> {

    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        return getPdiSdks(project)
                .map(this::getSyntheticLibrary)
                .collect(toList());
    }

    @Override public @NotNull Collection<VirtualFile> getRootsToWatch(@NotNull Project project) {
        return getPdiSdks(project).map(Sdk::getHomeDirectory).collect(toList());
    }

    @NotNull
    private SyntheticLibrary getSyntheticLibrary(Sdk sdk) {
        return new PdiSyntheticLibrary(sdk);
    }

    @NotNull
    private Stream<Sdk> getPdiSdks(@NotNull Project project) {
        return Arrays.stream(ModuleManager.getInstance(project).getModules())
                .flatMap(module -> PdiFacet.getInstance(module).stream())
                .flatMap(pdiFacet -> pdiFacet.getSdk().stream())
                .distinct();
    }

    private static class PdiSyntheticLibrary extends JavaSyntheticLibrary implements ItemPresentation {
        private final String name;
        private final String location;

        public PdiSyntheticLibrary(Sdk sdk) {
            super(emptyList(), asList(sdk.getRootProvider().getFiles(OrderRootType.CLASSES)), emptySet(), null);
            this.name = sdk.getName();
            this.location = sdk.getHomePath();
        }

        @Override
        public @NlsSafe @Nullable String getPresentableText() {
            return name;
        }

        @Override public @NlsSafe @Nullable String getLocationString() {
            return location;
        }

        @Override
        public @Nullable Icon getIcon(boolean unused) {
            return KettleIcons.KETTLE_ICON;
        }
    }

    @Override public void facetConfigurationChanged(@NotNull PdiFacet facet) {
        Project project = facet.getModule().getProject();
        triggerExternalLibrariesViewUpdate(project);
    }

    private void triggerExternalLibrariesViewUpdate(Project project) {
        ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
    }
}
