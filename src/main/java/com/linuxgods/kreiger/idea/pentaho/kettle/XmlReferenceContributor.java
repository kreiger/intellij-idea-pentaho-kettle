package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.patterns.XmlPatterns;
import com.intellij.patterns.XmlTagPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class XmlReferenceContributor extends PsiReferenceContributor {

    //public static final PsiElementPattern.Capture<XmlToken> XML_TEXT_PATTERN = PlatformPatterns.psiElement(XmlToken.class).withElementType(XmlTokenType.XML_DATA_CHARACTERS);
    public static final XmlTagPattern.Capture XML_TEXT_PATTERN = XmlPatterns.xmlTag();

    @Override public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        //registrar.registerReferenceProvider(XML_TEXT_PATTERN, new StepReferenceProvider(element -> GlobalSearchScope.fileScope(element.getContainingFile())));
        registrar.registerReferenceProvider(XML_TEXT_PATTERN, new TransformationReferenceProvider());
        //registrar.registerReferenceProvider(XML_TEXT_PATTERN, new StepTypeReferenceProvider());
    }
}
