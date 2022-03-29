package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.JobEntryType;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.PdiSdkAdditionalData;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import org.jetbrains.annotations.NotNull;

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
        return findModuleForFile(project, file)
                .flatMap(PdiFacet::getInstance);
    }

    @NotNull
    public static Optional<Module> findModuleForFile(Project project, VirtualFile file) {
        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        if (projectFileIndex.isInLibrary(file)) {
            List<OrderEntry> orderEntries = projectFileIndex.getOrderEntriesForFile(file);
            if (orderEntries.size() != 1) {
                return Optional.empty();
            }
            return Optional.of(orderEntries.get(0).getOwnerModule());
        }
        return Optional.ofNullable(ModuleUtilCore.findModuleForFile(file, project));
    }

    @NotNull public Optional<PdiSdkAdditionalData> getSdkAdditionalData() {
        return getSdk()
                .map(sdk -> (PdiSdkAdditionalData) sdk.getSdkAdditionalData());
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
    public static Stream<PsiClass> findClasses(String className, @NotNull Project project, @NotNull GlobalSearchScope resolveScope) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return Stream.of(javaPsiFacade.findClasses(className, resolveScope));
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
        boolean initialized = (boolean) isInitializedMethod.invoke(null);
        if (!initialized) {
            initMethod.invoke(null);
        }
    }

    @NotNull public Optional<JobEntryType> getJobEntryType(String type) {
        return getSdkAdditionalData()
                .flatMap(sdkAdditionalData -> sdkAdditionalData.getJobEntryType(type));
    }

    @NotNull public Optional<StepType> getStepTypeByClassName(String metaClassName) {
        return getSdkAdditionalData()
                .flatMap(ad -> ad.getStepTypeByClassName(metaClassName));
    }

    public Optional<StepType> getStepType(PsiType returnType) {
        if (returnType == null) return Optional.empty();
        return Optional.ofNullable(returnType.accept(new StepTypeVisitor()));
    }

    private class StepTypeVisitor extends PsiTypeVisitorEx<StepType> {
        @Override public StepType visitClassType(@NotNull PsiClassType classType) {
            PsiClass resolved = classType.resolve();
            if (resolved == null) {
                return null;
            }
            String qualifiedName = resolved.getQualifiedName();
            if (qualifiedName == null) {
                return null;
            }
            return getStepTypeByClassName(qualifiedName)
                    .orElseGet(() -> {
                        for (PsiType parameter : classType.getParameters()) {
                            StepType stepType = parameter.accept(this);
                            if (null != stepType) return stepType;
                        }
                        return null;
                    });
        }
    }
}
