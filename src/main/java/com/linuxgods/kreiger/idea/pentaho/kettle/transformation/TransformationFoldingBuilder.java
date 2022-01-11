package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.XmlTagPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.*;
import com.intellij.util.text.TextRangeUtil;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.patterns.XmlPatterns.xmlTag;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.*;

public class TransformationFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    private static final @NotNull Key<Integer> LONGEST_FROM = Key.create("KETTLE_TRANSFORMATION_LONGEST_FROM");
    private static final @NotNull Key<Integer> LONGEST_TO = Key.create("KETTLE_TRANSFORMATION_LONGEST_TO");
    private static final XmlTagPattern.Capture TRANSFORMATION_PATTERN = xmlTag().withLocalName("transformation");
    private static final XmlTagPattern.@NotNull Capture STEP_PATTERN =
            xmlTag().withLocalName("step")
            .withParent(TRANSFORMATION_PATTERN);
    private static final XmlTagPattern.@NotNull Capture HOP_PATTERN =
            xmlTag().withLocalName("hop")
            .withParent(xmlTag().withLocalName("order")
                    .withParent(TRANSFORMATION_PATTERN));

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (!root.getLanguage().equals(TransformationLanguage.INSTANCE)) {
            return FoldingDescriptor.EMPTY;
        }
        List<FoldingDescriptor> foldingDescriptors = new ArrayList<>();
        new XmlRecursiveElementVisitor() {
            public final FoldingGroup CHAR_ENTITY_REF_GROUP = FoldingGroup.newGroup("charEntityRef");

            @Override public void visitXmlToken(XmlToken token) {
                if (token.getTokenType() == XmlTokenType.XML_CHAR_ENTITY_REF) {
                    char c = XmlUtil.getCharFromEntityRef(token.getText());
                    foldingDescriptors.add(new FoldingDescriptor(token.getNode(), token.getTextRange(), CHAR_ENTITY_REF_GROUP, ""+c));
                }
            }

            /*

            @Override public void visitXmlText(XmlText text) {
                if (!text.getValue().equals(text.getText())) {
                    foldingDescriptors.add(new FoldingDescriptor(text, text.getTextRange()));                    
                }
            }

             */

            @Override public void visitXmlTag(XmlTag xmlTag) {
                if (xmlTag.getParentTag() == null) {
                    super.visitXmlTag(xmlTag);
                    return;
                }
                super.visitXmlTag(xmlTag);
                if (!foldable(xmlTag)) {
                    return;
                }

                if (STEP_PATTERN.accepts(xmlTag)) {
                    visitStep(xmlTag);
                    return;
                }
                if (HOP_PATTERN.accepts(xmlTag)) {
                    visitHop(xmlTag);
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

            private void visitHop(XmlTag xmlTag) {
                String hopPlaceholderText = getHopPlaceholderText(xmlTag);
                if (null != hopPlaceholderText) {
                    foldingDescriptors.add(new FoldingDescriptor(xmlTag.getNode(), xmlTag.getTextRange(), null, hopPlaceholderText));
                }
            }

            private void visitStep(XmlTag xmlTag) {
                String type = requireNonNullElse(xmlTag.getSubTagText("type"), "?");

                Set<String> excludedTagNames = Set.of("name", "type");
                FoldingGroup stepMetaGroup = FoldingGroup.newGroup("...default StepMeta...");
                FoldingGroup typeMetaGroup = FoldingGroup.newGroup("..."+type+"...");
                FoldingGroup previous = null;
                List<TextRange> currentTextRanges = new ArrayList<>();
                for (XmlTag tag : xmlTag.getSubTags()) {
                    String tagName = tag.getLocalName();
                    FoldingGroup current;
                    if (excludedTagNames.contains(tagName)) {
                        current = null;
                    } else {
                        Optional<Boolean> stepMetaTagDefault = getStepMetaTagDefault(tag);
                        if (stepMetaTagDefault.isPresent()) {
                            current = stepMetaTagDefault.get() ? stepMetaGroup : null;
                        } else {
                            current = typeMetaGroup;
                        }
                    }
                    if (current != previous) {
                        add(xmlTag, currentTextRanges, previous, foldingDescriptors);
                    }
                    if (current != null) {
                        currentTextRanges.add(tag.getTextRange());
                    }
                    previous = current;
                }
                add(xmlTag, currentTextRanges, previous, foldingDescriptors);
                return;
            }
        }.visitElement(root);
        return foldingDescriptors.toArray(FoldingDescriptor[]::new);
    }

    private void add(XmlTag xmlTag, List<TextRange> currentTextRanges, FoldingGroup group, List<FoldingDescriptor> foldingDescriptors) {
        if (!currentTextRanges.isEmpty()) {
            TextRange textRange = TextRangeUtil.getEnclosingTextRange(currentTextRanges);
            foldingDescriptors.add(new FoldingDescriptor(xmlTag.getNode(), textRange, group, group.toString()));
            currentTextRanges.clear();
        }
    }

    private Optional<Boolean> getStepMetaTagDefault(XmlTag tag) {
        String localName = tag.getLocalName();
        switch (localName) {
            case "description":
            case "custom_distribution":
            case "attributes":
            case "cluster_schema":
                return Optional.of(tag.isEmpty());
            case "distribute":
                return Optional.of("Y".equals(tag.getValue().getText()));
            case "copies":
                return Optional.of("1".equals(tag.getValue().getText()));
            case "partitioning":
                return Optional.of("none".equals(tag.getSubTagText("method")));
            case "GUI":
                return Optional.of("Y".equals(tag.getSubTagText("draw")));
            case "remotesteps":
                return Optional.of(Arrays.stream(tag.getSubTags())
                        .allMatch(subTag -> subTag.isEmpty() || isBlank(subTag)));
        }
        return Optional.empty();
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

        XmlTag xmlTag = (XmlTag) node;
        String localName = xmlTag.getLocalName();
        if (isBlank(xmlTag)) {
            return "<"+ localName +"/>";
        }

        if (xmlTag.getSubTagText("name") != null || xmlTag.getSubTagText("note") != null) {
            return "...";
        }
        return "<"+ localName +"...>";
    }

    private String getHopPlaceholderText(XmlTag hopTag) {
        String enabled = hopTag.getSubTagText("enabled");
        if (!"Y".equals(enabled)) {
            return "<hop disabled/>";
        }
        XmlTag orderTag = hopTag.getParentTag();
        if (null == orderTag) {
            return null;
        }
        int longestFromValueLength = getLongestValue(orderTag, "from", LONGEST_FROM);
        int longestToValueLength = getLongestValue(orderTag, "to", LONGEST_TO);
        XmlText from = getSubTagXmlText(hopTag, "from");
        XmlText to = getSubTagXmlText(hopTag, "to");
        if (null != from && null != to) {
            String fromValue = leftPad(from.getValue(), longestFromValueLength);
            String toValue = rightPad(to.getValue(), longestToValueLength);
            return "<hop> " + fromValue + "  ->  " + toValue+ " </hop>";
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
        if (!(node instanceof XmlTag)) return true;
        XmlTag xmlTag = (XmlTag) node;
        int subTagCount = xmlTag.getSubTags().length;
        if (Set.of("order","notepads").contains(xmlTag.getLocalName())) {
            return subTagCount == 0;
        }
        return subTagCount != 1;
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
