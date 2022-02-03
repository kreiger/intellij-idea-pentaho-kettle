package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.google.common.base.Suppliers;
import com.intellij.codeInsight.daemon.DefaultGutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.NavigateAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet.findClasses;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class StepType {
    public static final Logger LOGGER = LoggerFactory.getLogger(StepType.class);
    private final String id;
    private final String imagePath;
    private final String className;
    private final Supplier<Icon> icon;

    public StepType(String id, String imagePath, String className, Function<StepType, Icon> iconLoader) {
        this.id = requireNonNull(id);
        this.imagePath = requireNonNull(imagePath);
        this.className = className;
        this.icon = Suppliers.memoize(() -> iconLoader.apply(this));
    }

    public StepType(Element element, Function<StepType, Icon> iconLoader) {
        this(element.getAttributeValue("id"), element.getAttributeValue("image"), element.getAttributeValue("classname"), iconLoader);
    }

    @NotNull private static Stream<? extends PsiMethod> getPublicMethods(PsiClass psiClass, String... methodNames) {
        Set<String> methodNamesSet = Set.of(methodNames);
        return Arrays.stream(psiClass.getMethods()).filter(psiMethod -> psiMethod.hasModifierProperty(PsiModifier.PUBLIC)).filter(psiMethod -> methodNamesSet.contains(psiMethod.getName()));
    }

    @NotNull public static Stream<NavigatablePsiElement> getTypePsiElements(PsiClass psiClass) {
        return Stream.concat(Stream.of(psiClass),
                getPublicMethods(psiClass, "getXML", "loadXML", "setDefault", "getStep"));
    }

    public String getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getClassName() {
        return className;
    }

    @NotNull Element createElement() {
        Element stepElement = new Element("step");
        stepElement.setAttribute("id", getId());
        stepElement.setAttribute("image", getImagePath());
        if (null != getClassName()) stepElement.setAttribute("classname", getClassName());
        return stepElement;
    }

    public Icon getIcon() {
        return icon.get();
    }

    public static Icon loadIcon(ClassLoader classLoader, StepType stepType) {
        return loadIcon(classLoader, stepType.getImagePath(), stepType.getClassName());
    }

    public static Icon loadIcon(ClassLoader classLoader, String imagePath, String className) {
        try {
            URL resource = classLoader.getResource(imagePath);
            if (null == resource) {
                LOGGER.warn("No resource for " + imagePath + " on " + className);
                if (imagePath.startsWith("images/")) {
                    return loadIcon(classLoader, "ui/" + imagePath, className);
                } else if (!imagePath.contains("/")) {
                    return loadIcon(classLoader, "ui/images/" + imagePath, className);
                }
                return ImageUtil.MISSING_ENTRY_ICON;
            }
            return ImageUtil.graphIcon(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + imagePath + " for " + className, e);
        }
    }

    @Override public String toString() {
        return "StepType{" + "id='" + id + '\'' + ", className='" + className + '\'' + ", imagePath='" + imagePath + '\'' + '}';
    }

    @NotNull
    public LineMarkerInfo<PsiElement> getPsiElementLineMarkerInfo(PsiElement element, List<NavigatablePsiElement> stepMetaClasses) {
        LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(element, element.getTextRange(), getIcon(), null, new DefaultGutterIconNavigationHandler<>(stepMetaClasses, getId()), GutterIconRenderer.Alignment.LEFT, this::getId);
        return NavigateAction.setNavigateAction(lineMarkerInfo, "Go To " + getId(), "GotoClass", getIcon());
    }

    @NotNull
    public Stream<NavigatablePsiElement> getTypePsiElements(@NotNull Project project, @NotNull GlobalSearchScope resolveScope) {
        return findClasses(getClassName(), project, resolveScope)
                .flatMap(StepType::getTypePsiElements);
    }

    @NotNull public LineMarkerInfo<PsiElement> getPsiElementLineMarkerInfo(PsiElement element, PdiFacet pdiFacet) {
        List<NavigatablePsiElement> stepMetaClasses = getTypePsiElements(pdiFacet.getModule().getProject(), element.getResolveScope())
                .collect(toList());
        return getPsiElementLineMarkerInfo(element, stepMetaClasses);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepType stepType = (StepType) o;
        return id.equals(stepType.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
