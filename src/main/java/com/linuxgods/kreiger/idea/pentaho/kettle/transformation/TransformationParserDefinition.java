package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class TransformationParserDefinition extends XMLParserDefinition {

    public static final IFileElementType KTR_FILE = new IFileElementType(TransformationLanguage.INSTANCE);


    @Override public @NotNull IFileElementType getFileNodeType() {
        return KTR_FILE;
    }

    @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new XmlFileImpl(viewProvider, KTR_FILE);
    }

    @Override public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.ANY;
    }
}
