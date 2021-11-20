package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.sql.psi.SqlLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SqlInjector implements MultiHostInjector {
    private static final Logger log = Logger.getInstance(SqlInjector.class);
    @Override public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement host) {
        if (!isPentahoKettleTransformation(host)) return;
        XmlText xmlText = (XmlText) host;
        XmlTag xmlTag = xmlText.getParentTag();
        if (xmlTag == null) return;
        String xmlTagName = xmlTag.getLocalName();
        if (!"sql".equals(xmlTagName)) return;

        registrar.startInjecting(SqlLanguage.INSTANCE)
                .addPlace(null, null, (PsiLanguageInjectionHost)host, TextRange.from(0, host.getTextLength()))
                .doneInjecting();

    }

    private boolean isPentahoKettleTransformation(@NotNull PsiElement context) {
        if (!context.isValid()) return false;
        PsiFile containingFile = context.getContainingFile();
        if (containingFile == null) return false;
        @NotNull FileType fileType = containingFile.getFileType();
        return fileType instanceof TransformationFileType;
    }

    @Override public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(XmlText.class);
    }
}
