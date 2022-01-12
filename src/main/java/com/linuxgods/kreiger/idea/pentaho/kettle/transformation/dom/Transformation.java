package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Transformation extends DomElement {
    Order getOrder();
    List<Step> getSteps();
    Notepads getNotepads();

    @NotNull static Transformation getTransformation(Project project, VirtualFile file) {
        PsiManager psiManager = PsiManager.getInstance(project);
        DomManager domManager = DomManager.getDomManager(project);
        XmlFile xmlFile = (XmlFile) psiManager.findFile(file);
        DomFileElement<Transformation> fileElement = domManager.getFileElement(xmlFile, Transformation.class);
        return fileElement.getRootElement();
    }
}
