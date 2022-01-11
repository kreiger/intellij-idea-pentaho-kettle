package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkAdditionalData;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.Step;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;

public class PdiFacet extends Facet<PdiFacetConfiguration> {
    public PdiFacet(@NotNull Module module, @NotNull @NlsSafe String name, @NotNull PdiFacetConfiguration configuration, Facet underlyingFacet) {
        super(PdiFacetType.INSTANCE, module, name, configuration, underlyingFacet);
    }

    public static PdiFacet getInstance(@NotNull PsiElement element) {
        return PdiFacet.getInstance(element.getProject(), element.getContainingFile().getVirtualFile());
    }

    public static PdiFacet getInstance(@NotNull Module module) {
        return FacetManager.getInstance(module).getFacetByType(PdiFacetType.ID);
    }

    public static PdiFacet getInstance(@NotNull Project project, @NotNull VirtualFile file) {
        Module module = ModuleUtilCore.findModuleForFile(file, project);
        return getInstance(module);
    }

    public Optional<Image> getImage(String id) {
        return getSdkAdditionalData()
                .flatMap(sdkAdditionalData -> sdkAdditionalData.getImage(id))
                .map(image -> ImageUtil.drawOnBackground(Color.LIGHT_GRAY, 1, image));
    }

    @NotNull private Optional<PdiSdkAdditionalData> getSdkAdditionalData() {
        return Optional.of(getConfiguration())
                .map(PdiFacetConfiguration::getSdk)
                .map(sdk -> (PdiSdkAdditionalData) sdk.getSdkAdditionalData());
    }

    public Optional<Icon> getIcon(String type) {
        return getImage(type)
                .map(ImageIcon::new);
    }

    public Stream<PsiClass> findStepMetaClasses(String type, @NotNull GlobalSearchScope resolveScope) {
        return getSdkAdditionalData()
                .flatMap(sdkAdditionalData -> sdkAdditionalData.getStep(type))
                .map(Step::getClassName)
                .stream()
                .flatMap(className -> {
                    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(getModule().getProject());
                    return Stream.of(javaPsiFacade.findClasses(className, resolveScope));
                });
    }
}
