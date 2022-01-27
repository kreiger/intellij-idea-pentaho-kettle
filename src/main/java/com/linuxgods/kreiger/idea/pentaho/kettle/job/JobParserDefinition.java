package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class JobParserDefinition extends XMLParserDefinition {

    public static final IFileElementType KJB_FILE = new IFileElementType(JobLanguage.INSTANCE);


    @Override public @NotNull IFileElementType getFileNodeType() {
        return KJB_FILE;
    }

    @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new XmlFileImpl(viewProvider, KJB_FILE);
    }
}
