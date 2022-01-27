package com.linuxgods.kreiger.idea.pentaho.kettle.job.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface Job extends DomElement {
    Hops getHops();
    Entries getEntries();

    @NotNull static Job getJob(Project project, VirtualFile file) {
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = Objects.requireNonNull(psiManager.findFile(file));
        return getJob((XmlFile) psiFile);
    }

    @NotNull
    static Job getJob(XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<Job> fileElement = domManager.getFileElement(xmlFile, Job.class);
        return Objects.requireNonNull(fileElement).getRootElement();
    }

}
