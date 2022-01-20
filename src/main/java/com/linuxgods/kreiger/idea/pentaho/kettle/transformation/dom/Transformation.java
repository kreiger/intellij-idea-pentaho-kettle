package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public interface Transformation extends DomElement {
    Order getOrder();
    List<Step> getSteps();
    Notepads getNotepads();

    @NotNull static Transformation getTransformation(Project project, VirtualFile file) {
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = Objects.requireNonNull(psiManager.findFile(file));
        return getTransformation((XmlFile) psiFile);
    }

    @NotNull
    static Transformation getTransformation(XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<Transformation> fileElement = domManager.getFileElement(xmlFile, Transformation.class);
        return Objects.requireNonNull(fileElement).getRootElement();
    }
}
