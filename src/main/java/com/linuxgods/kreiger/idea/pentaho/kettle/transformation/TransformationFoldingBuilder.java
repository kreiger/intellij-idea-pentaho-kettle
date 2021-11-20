package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

public class TransformationFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    private static final @NotNull Key<Integer> LONGEST_FROM = Key.create("KETTLE_TRANSFORMATION_LONGEST_FROM");
    private static final @NotNull Key<Integer> LONGEST_TO = Key.create("KETTLE_TRANSFORMATION_LONGEST_TO");

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (!root.getLanguage().equals(TransformationLanguage.INSTANCE)) {
            return FoldingDescriptor.EMPTY;
        }
        List<FoldingDescriptor> foldingDescriptors = new ArrayList<>();
        new XmlRecursiveElementVisitor() {

            @Override public void visitXmlText(XmlText text) {
                if (!text.getValue().equals(text.getText())) {
                    foldingDescriptors.add(new FoldingDescriptor(text, text.getTextRange()));                    
                }
            }

            @Override public void visitXmlTag(XmlTag xmlTag) {
                if (xmlTag.getParentTag() == null) {
                    super.visitXmlTag(xmlTag);
                    return;
                }
                super.visitXmlTag(xmlTag);
                if (!foldable(xmlTag)) {
                    return;
                }

                XmlText from = getSubTagXmlText(xmlTag, "from");
                XmlText to = getSubTagXmlText(xmlTag, "to");
                if (null != from && null != to) {
                    foldingDescriptors.add(new FoldingDescriptor(xmlTag, xmlTag.getTextRange()));
                    return;
                }

                XmlTag[] nameTags = xmlTag.findSubTags("name", null);
                if (nameTags.length == 0) {
                    nameTags = xmlTag.findSubTags("note", null);
                }
                if (nameTags.length == 0) {
                    foldingDescriptors.add(new FoldingDescriptor(xmlTag, xmlTag.getTextRange()));
                    return;
                }
                XmlTag nameTag = nameTags[0];
                int nameEnd = nameTag.getTextRange().getEndOffset();
                XmlTag[] typeTags = xmlTag.findSubTags("type", null);
                XmlTag typeTag = typeTags.length == 0 ? nameTag : typeTags[0];
                int typeEnd = typeTag.getTextRange().getEndOffset();
                XmlTagChild nextSiblingInTag = (typeEnd > nameEnd ? typeTag : nameTag).getNextSiblingInTag();
                XmlTag[] subTags = xmlTag.getSubTags();
                XmlTag lastTag = subTags[subTags.length - 1];
                int endOffset = lastTag.getTextRange().getEndOffset();
                if (null == nextSiblingInTag) {
                    return;
                }
                int startOffset = nextSiblingInTag.getTextRange().getEndOffset();
                if (endOffset <= startOffset) {
                    return;
                }
                TextRange textRange = new TextRange(startOffset, endOffset);
                foldingDescriptors.add(new FoldingDescriptor(xmlTag, textRange));
            }
        }.visitElement(root);
        return foldingDescriptors.toArray(FoldingDescriptor[]::new);
    }

    private boolean foldable(XmlTag xmlTag) {
        if (xmlTag.isEmpty()) return false;
        return isBlank(xmlTag) || xmlTag.getSubTags().length > 0;
    }

    private boolean isBlank(XmlTag xmlTag) {
        for (PsiElement child : xmlTag.getChildren()) {
            if (child instanceof XmlText) {
                XmlText xmlText = (XmlText) child;
                if (!xmlText.getText().isBlank()) {
                    return false;
                }
            }
            if (child instanceof XmlTag) {
                return false;
            }
        }
        return true;
    }

    @Override public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        if (node instanceof XmlText) {
            return ((XmlText) node).getValue();
        }

        XmlTag xmlTag = (XmlTag) node;
        String localName = xmlTag.getLocalName();
        if (isBlank(xmlTag)) {
            return "<"+ localName +"/>";
        }

        if ("hop".equals(xmlTag.getLocalName())) {
            return getHopPlaceholderText(xmlTag);
        }

        if (xmlTag.getSubTagText("name") != null || xmlTag.getSubTagText("note") != null) {
            return "...";
        }
        return "<"+ localName +"...>";
    }

    @Nullable private String getHopPlaceholderText(XmlTag hopTag) {
        String enabled = hopTag.getSubTagText("enabled");
        if (!"Y".equals(enabled)) {
            return "<hop disabled/>";
        }
        XmlTag orderTag = hopTag.getParentTag();
        int longestFromValueLength = getLongestValue(orderTag, "from", LONGEST_FROM);
        int longestToValueLength = getLongestValue(orderTag, "to", LONGEST_TO);
        XmlText from = getSubTagXmlText(hopTag, "from");
        XmlText to = getSubTagXmlText(hopTag, "to");
        if (null != from && null != to) {
            String fromValue = leftPad(from.getValue(), longestFromValueLength);
            String toValue = rightPad(to.getValue(), longestToValueLength);
            return "<hop> <from> " + fromValue + " </from> <to> " + toValue+ " </to> </hop>";
        }
        return null;
    }

    private int getLongestValue(XmlTag orderTag, String name, @NotNull Key<Integer> key) {
        Integer longestFrom = orderTag.getUserData(key);
        if (null == longestFrom) {
            longestFrom = 0;
            for (XmlTag subTag : orderTag.getSubTags()) {
                XmlText from = getSubTagXmlText(subTag, name);
                if (null != from) {
                    longestFrom = Math.max(longestFrom, from.getValue().length());
                }
            }
            orderTag.putUserData(key, longestFrom);
        }
        return longestFrom;
    }

    @Override public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        if (node instanceof XmlText) return true;
        XmlTag xmlTag = (XmlTag) node;
        int length = xmlTag.getSubTags().length;
        return length != 1;
    }

    private XmlText getSubTagXmlText(XmlTag xmlTag, String qname) {
        XmlTag[] subTags = xmlTag.findSubTags(qname);
        if (subTags.length != 1) {
            return null;
        }
        return getXmlText(subTags[0]);
    }

    @Nullable private XmlText getXmlText(XmlTag xmlTag) {
        XmlText[] textElements = xmlTag.getValue().getTextElements();
        if (textElements.length != 1) {
            return null;
        }
        return textElements[0];
    }
}
