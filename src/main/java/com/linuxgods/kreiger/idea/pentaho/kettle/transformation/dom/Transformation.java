package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.*;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Notepads;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface Transformation extends DomElement {
    Order getOrder();
    List<Step> getSteps();
    Notepads getNotepads();

    @PropertyAccessor({"step_error_handling", "error"})
    List<StepError> getStepErrors();

    static Optional<Transformation> getTransformation(Project project, VirtualFile file) {
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = Objects.requireNonNull(psiManager.findFile(file));
        return getTransformation((XmlFile) psiFile);
    }

    static Optional<Transformation> getTransformation(XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<Transformation> fileElement = domManager.getFileElement(xmlFile, Transformation.class);
        return Optional.ofNullable(fileElement).map(DomFileElement::getRootElement);
    }
}
