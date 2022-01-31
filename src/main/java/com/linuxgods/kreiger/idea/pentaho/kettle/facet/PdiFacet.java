package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.JobEntryType;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkAdditionalData;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Image;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import java.util.stream.Stream;

public class PdiFacet extends Facet<PdiFacetConfiguration> {
    public PdiFacet(@NotNull Module module, @NotNull @NlsSafe String name, @NotNull PdiFacetConfiguration configuration, Facet underlyingFacet) {
        super(PdiFacetType.INSTANCE, module, name, configuration, underlyingFacet);
    }

    public static Optional<PdiFacet> getInstance(@NotNull PsiElement element) {
        if (!element.isValid()) return Optional.empty();
        return PdiFacet.getInstance(element.getProject(), element.getContainingFile().getVirtualFile());
    }

    public static Optional<PdiFacet> getInstance(Module module) {
        if (module == null) return Optional.empty();
        return Optional.ofNullable(FacetManager.getInstance(module).getFacetByType(PdiFacetType.ID));
    }

    public static Optional<PdiFacet> getInstance(Project project, VirtualFile file) {
        if (project == null || file == null) return Optional.empty();
        return Optional.ofNullable(ModuleUtilCore.findModuleForFile(file, project))
                .flatMap(PdiFacet::getInstance);
    }

    @NotNull
    private static Stream<? extends PsiMethod> getPublicMethods(PsiClass psiClass, String... methodNames) {
        Set<String> methodNamesSet = Set.of(methodNames);
        return Arrays.stream(psiClass.getMethods())
                .filter(psiMethod -> psiMethod.hasModifierProperty(PsiModifier.PUBLIC))
                .filter(psiMethod -> methodNamesSet.contains(psiMethod.getName()));
    }

    @NotNull public Optional<PdiSdkAdditionalData> getSdkAdditionalData() {
        return getSdk()
                .map(sdk -> (PdiSdkAdditionalData) sdk.getSdkAdditionalData());
    }

    public Optional<Icon> getIcon(String type) {
        return getStepType(type).map(StepType::getIcon);
    }

    public Stream<PsiClass> findStepMetaClasses(String type, @NotNull GlobalSearchScope resolveScope) {
        return getStepTypeClassName(type)
                .stream()
                .flatMap(className -> {
                    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(getModule().getProject());
                    return Stream.of(javaPsiFacade.findClasses(className, resolveScope));
                });
    }

    @NotNull public Optional<String> getStepTypeClassName(String type) {
        return getStepType(type)
                .map(StepType::getClassName);
    }

    @NotNull public Optional<StepType> getStepType(String type) {
        return getSdkAdditionalData()
                .flatMap(sdkAdditionalData -> sdkAdditionalData.getStepType(type));
    }


    public Stream<StepType> getStepTypes() {
        return getSdkAdditionalData().stream()
                .flatMap(PdiSdkAdditionalData::getStepTypes);

    }

    public Optional<Sdk> getSdk() {
        return Optional.of(getConfiguration())
                .map(PdiFacetConfiguration::getSdk);
    }

    @NotNull
    public Stream<NavigatablePsiElement> getTypePsiElements(String type, @NotNull GlobalSearchScope resolveScope) {
        return findStepMetaClasses(type, resolveScope)
                .flatMap(psiClass -> Stream.<NavigatablePsiElement>concat(Stream.of(psiClass), getPublicMethods(psiClass, "getXML", "loadXML")));
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Optional<PdiSdkAdditionalData> sdkAdditionalData = getSdkAdditionalData();
        if (sdkAdditionalData.isPresent()) {
            return sdkAdditionalData.get().loadClass(className);
        }
        throw new ClassNotFoundException();
    }

    public synchronized void initializeKettleEnvironment() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> kettleClientEnvironmentClass = loadClass("org.pentaho.di.core.KettleClientEnvironment");
        Method isInitializedMethod = kettleClientEnvironmentClass.getMethod("isInitialized");
        Method initMethod = kettleClientEnvironmentClass.getMethod("init");
        boolean initialized = (boolean)isInitializedMethod.invoke(null);
        if (!initialized) {
            initMethod.invoke(null);
        }
    }

    @NotNull public Optional<JobEntryType> getJobEntryType(String type) {
        return getSdkAdditionalData()
                .flatMap(sdkAdditionalData -> sdkAdditionalData.getJobEntryType(type));
    }
}
