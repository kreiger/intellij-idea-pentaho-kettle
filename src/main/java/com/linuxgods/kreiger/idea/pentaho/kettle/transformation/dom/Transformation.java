package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.*;
import com.linuxgods.kreiger.idea.pentaho.kettle.dom.SnakeNameStrategy;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Notepads;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@NameStrategy(SnakeNameStrategy.class)
public interface Transformation extends DomElement {
    Order getOrder();
    List<Step> getSteps();
    Notepads getNotepads();
    StepErrorHandling getStepErrorHandling();

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
    @NotNull
    default Optional<StepError> findStepError(Step from, Step to) {
        return getStepErrorHandling().getErrors().stream()
                .filter(StepError::isEnabled)
                .filter(stepError -> from.getName().getRawText().equals(stepError.getSourceStep().getRawText()))
                .filter(stepError -> to.getName().getRawText().equals(stepError.getTargetStep().getRawText()))
                .findFirst();
    }

}
