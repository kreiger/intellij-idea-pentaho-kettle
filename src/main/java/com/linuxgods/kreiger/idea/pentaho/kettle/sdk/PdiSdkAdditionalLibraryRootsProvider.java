package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.JavaSyntheticLibrary;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.SyntheticLibrary;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

public class PdiSdkAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdiSdkAdditionalLibraryRootsProvider.class);

    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        return getPdiSdks(project)
                .map(this::getSyntheticLibrary)
                .collect(toList());
    }

    @Override
    public @NotNull Collection<VirtualFile> getRootsToWatch(@NotNull Project project) {
        return getPdiSdks(project).map(Sdk::getHomeDirectory).collect(toList());
    }

    @NotNull
    private SyntheticLibrary getSyntheticLibrary(Sdk sdk) {
        List<VirtualFile> jars = asList(sdk.getRootProvider().getFiles(OrderRootType.CLASSES));
        LOGGER.warn("Synthetic library: "+jars);
        return new PdiSyntheticLibrary(sdk);
    }

    @NotNull
    private Stream<Sdk> getPdiSdks(@NotNull Project project) {
        return Arrays.stream(ModuleManager.getInstance(project).getModules())
                .flatMap(module -> PdiFacet.getInstance(module).stream())
                .flatMap(facet -> facet.getSdk().stream())
                .distinct()
                .peek(sdk -> LOGGER.warn("SDK: {}", sdk));
    }

    private static class PdiSyntheticLibrary extends JavaSyntheticLibrary implements ItemPresentation {
        private final String name;

        public PdiSyntheticLibrary(Sdk sdk) {
            super(emptyList(), asList(sdk.getRootProvider().getFiles(OrderRootType.CLASSES)), emptySet(), null);
            this.name = sdk.getName();
        }

        @Override
        public @NlsSafe @Nullable String getPresentableText() {
            return name;
        }

        @Override
        public @Nullable Icon getIcon(boolean unused) {
            return KettleIcons.KETTLE_ICON;
        }
    }
}
