package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.XmlUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import one.util.streamex.StreamEx;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intellij.patterns.StandardPatterns.collection;
import static com.intellij.patterns.XmlPatterns.xmlTag;
import static java.util.Collections.emptySet;
import static java.util.Map.entry;
import static java.util.Objects.requireNonNullElse;
import static org.apache.commons.lang3.StringUtils.*;

public class TransformationFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    private static final @NotNull Key<Integer> LONGEST_FROM = Key.create("KETTLE_TRANSFORMATION_LONGEST_FROM");
    private static final @NotNull Key<Integer> LONGEST_TO = Key.create("KETTLE_TRANSFORMATION_LONGEST_TO");
    private static final XmlTagPattern.Capture TRANSFORMATION_PATTERN = xmlTag().withLocalName("transformation");
    private static final XmlTagPattern.@NotNull Capture STEP_PATTERN =
            xmlTag().withLocalName("step")
            .withParent(TRANSFORMATION_PATTERN);
    private static final XmlTagPattern.@NotNull Capture CONNECTION_PATTERN =
            xmlTag().withLocalName("connection")
            .withParent(TRANSFORMATION_PATTERN);
    private static final XmlTagPattern.@NotNull Capture INFO_PATTERN =
            xmlTag().withLocalName("info")
            .withParent(TRANSFORMATION_PATTERN);
    private static final XmlTagPattern.@NotNull Capture HOP_PATTERN =
            xmlTag().withLocalName("hop")
            .withParent(xmlTag().withLocalName("order")
                    .withParent(TRANSFORMATION_PATTERN));
    private static final XmlTagPattern.@NotNull Capture NOTEPAD_PATTERN =
            xmlTag().withLocalName("notepad")
            .withParent(xmlTag().withLocalName("notepads")
                    .withParent(TRANSFORMATION_PATTERN));
    private static final Map<String, String> INFO_DEFAULT_VALUES = Map.ofEntries(
            entry("trans_type", "Normal"),
            entry("trans_status", "0"),
            entry("size_rowset", "10000"),
            entry("sleep_time_empty", "50"),
            entry("sleep_time_full", "50"),
            entry("unique_connections", "N"),
            entry("feedback_shown", "Y"),
            entry("feedback_size", "50000"),
            entry("using_thread_priorities", "Y"),
            entry("capture_step_performance", "N"),
            entry("step_performance_capturing_delay", "1000"),
            entry("step_performance_capturing_size_limit", "100"),
            entry("created_user", "-"),
            entry("modified_user", "-"),
            entry("key_for_session_key", "H4sIAAAAAAAAAAMAAAAAAAAAAAA="),
            entry("is_key_private", "N"));


    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (!root.getLanguage().equals(TransformationLanguage.INSTANCE)) {
            return FoldingDescriptor.EMPTY;
        }
        List<FoldingDescriptor> foldingDescriptors = new ArrayList<>();
        Optional<PdiFacet> pdiFacet = PdiFacet.getInstance(root);
        new XmlRecursiveElementVisitor() {
            private final FoldingGroup TRANS_META_GROUP = FoldingGroup.newGroup("...TransMeta...");
            private final FoldingGroup TRANS_META_DEFAULT_GROUP = FoldingGroup.newGroup("...TransMeta default...");
            private final FoldingGroup CHAR_ENTITY_REF_GROUP = FoldingGroup.newGroup("charEntityRef");

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
                    foldSubTags(xmlTag, subTag -> getTransformationSubTagDefault(subTag)
                            .map(subTagDefault -> subTagDefault ? TRANS_META_DEFAULT_GROUP : TRANS_META_GROUP)
                            .map(group -> entry(group, group.toString()))
                            .orElse(null));
                    super.visitXmlTag(xmlTag);
                    return;
                }

                if (INFO_PATTERN.accepts(xmlTag)) {
                    visitInfoTag(xmlTag);
                    super.visitXmlTag(xmlTag);
                    return;
                } else if (NOTEPAD_PATTERN.accepts(xmlTag)) {
                    FoldingGroup noteGroup = FoldingGroup.newGroup(xmlTag.getLocalName());
                    foldSubTags(xmlTag,
                            tag -> "note".equals(tag.getLocalName()) ? null : Map.entry(noteGroup, "..."));
                    super.visitXmlTag(xmlTag);
                    return;
                } else if (STEP_PATTERN.accepts(xmlTag)) {
                    visitStep(xmlTag);
                    super.visitXmlTag(xmlTag);
                    return;
                } else if (HOP_PATTERN.accepts(xmlTag)) {
                    visitHop(xmlTag);
                    super.visitXmlTag(xmlTag);
                    return;
                } else
                super.visitXmlTag(xmlTag);
                if (!foldable(xmlTag)) {
                    return;
                }

                FoldingGroup group = FoldingGroup.newGroup(xmlTag.getName());

                String name = xmlTag.getSubTagText("name");
                if (name != null) {
                    foldSubTags(xmlTag, subTag -> {
                        if ("name".equals(subTag.getLocalName())) {
                            return null;
                        }
                        return entry(group, "...");
                    });
                    return;
                }

                foldingDescriptors.add(new FoldingDescriptor(xmlTag, xmlTag.getTextRange()));
            }

            private Optional<Boolean> getTransformationSubTagDefault(XmlTag subTag) {
                switch (subTag.getLocalName()) {
                    case "step_error_handling":
                    case "slave-step-copy-partition-distribution":
                        return Optional.of(isBlank(subTag));
                    case "slave_transformation":
                        return Optional.of("N".equals(subTag.getValue().getText()));
                    case "attributes":
                        return Optional.of(subTag.isEmpty());
                }
                return Optional.empty();
            }

            private void visitConnectionTag(XmlTag xmlTag) {
                FoldingGroup group = FoldingGroup.newGroup(xmlTag.getName());
                foldSubTags(xmlTag, subTag -> {
                    switch (subTag.getLocalName()) {
                        case "name":
                        case "server":
                        case "type":
                        case "access":
                        case "database":
                        case "port":
                        case "username":
                            return null;
                    }
                    return entry(group,  "...");
                });
            }

            private void visitInfoTag(XmlTag xmlTag) {
                foldSubTags(xmlTag, subTag -> {
                    String localName = subTag.getLocalName();
                    switch (localName) {
                        case "name":
                        case "directory":
                            return null;
                    }
                    FoldingGroup group = getInfoTagDefault(subTag)? TRANS_META_DEFAULT_GROUP : TRANS_META_GROUP;
                    return entry(group, group.toString());
                });
            }

            private boolean getInfoTagDefault(XmlTag tag) {
                String localName = tag.getLocalName();
                String defaultValue = INFO_DEFAULT_VALUES.get(localName);
                if (null != defaultValue) {
                    return defaultValue.equals(tag.getValue().getTrimmedText());
                }
                switch (localName) {
                    case "description":
                    case "extended_description":
                    case "trans_version":
                    case "shared_objects_file":
                        return tag.isEmpty();
                    case "parameters":
                    case "dependencies":
                    case "partitionschemas":
                    case "slaveservers":
                    case "clusterschemas":
                        return isBlank(tag);
                    case "log":
                        return Stream.of(tag.getSubTags())
                                .flatMap(t -> Stream.of("connection", "schema", "table").map(t::findFirstSubTag))
                                .filter(Objects::nonNull)
                                .allMatch(XmlTag::isEmpty);
                    case "maxdate":
                        return xmlTag("maxdate").withSubTags(xmlTags(
                                xmlTag("connection").with(empty()),
                                xmlTag("table").with(empty()),
                                xmlTag("field").with(empty()),
                                xmlTag("offset").with(xmlValueText("0.0")),
                                xmlTag("maxdiff").with(xmlValueText("0.0")))).accepts(tag);
    /*<maxdate>
      <connection/>
      <table/>
      <field/>
      <offset>0.0</offset>
      <maxdiff>0.0</maxdiff>
    </maxdate>*/
                }
                return true;
            }

            @NotNull private PatternCondition<XmlTag> xmlValueText(String text) {
                return new PatternCondition<XmlTag>("xmlValueText") {
                    @Override
                    public boolean accepts(@NotNull XmlTag xmlTag, ProcessingContext context) {
                        return text.equals(xmlTag.getValue().getText());
                    }
                };
            }

            @NotNull private PatternCondition<XmlTag> empty() {
                return new PatternCondition<>("empty") {
                    @Override
                    public boolean accepts(@NotNull XmlTag xmlTag, ProcessingContext context) {
                        return xmlTag.isEmpty();
                    }
                };
            }

            private XmlTagPattern.Capture xmlTag(String localName) {
                return XmlPatterns.xmlTag().withLocalName(localName);
            }

            @SafeVarargs @NotNull private CollectionPattern<XmlTag> xmlTags(ElementPattern<XmlTag>... xmlTagsPatterns) {
                return StandardPatterns.<XmlTag>collection().with(contents(xmlTagsPatterns));
            }

            @SafeVarargs @NotNull private <T> PatternCondition<Collection<T>> contents(ElementPattern<T>... elementPatterns) {
                return new PatternCondition<>("elements") {
                    @Override public boolean accepts(@NotNull Collection<T> ts, ProcessingContext context) {
                        Iterator<T> iterator = ts.iterator();
                        for (ElementPattern<T> elementPattern : elementPatterns) {
                            if (!iterator.hasNext() || !elementPattern.accepts(iterator.next())) {
                                return false;
                            }
                        }
                        return !iterator.hasNext();
                    }
                };
            }

            private void visitHop(XmlTag xmlTag) {
                String hopPlaceholderText = getHopPlaceholderText(xmlTag);
                if (null != hopPlaceholderText) {
                    foldingDescriptors.add(new FoldingDescriptor(xmlTag.getNode(), xmlTag.getTextRange(), null, hopPlaceholderText));
                }
            }

            private void visitStep(XmlTag xmlTag) {
                String type = requireNonNullElse(xmlTag.getSubTagText("type"), "?");
                String typeClass = pdiFacet.flatMap(facet -> facet.getClassName(type))
                        .map(className -> StringUtils.substringAfterLast(className, "."))
                        .orElse(type);
                foldSubTags(xmlTag, new Function<>() {
                    final Set<String> excludedTagNames = Set.of("name", "type");
                    final FoldingGroup stepMetaDefaultsGroup = FoldingGroup.newGroup("...StepMeta default...");
                    final FoldingGroup stepMetaGroup = FoldingGroup.newGroup("...StepMeta...");
                    final FoldingGroup typeMetaGroup = FoldingGroup.newGroup("..." + typeClass + "...");

                    @Override
                    public Map.Entry<FoldingGroup, String> apply(XmlTag tag) {
                        String tagName = tag.getLocalName();
                        if (excludedTagNames.contains(tagName)) {
                            return null;
                        }
                        FoldingGroup foldingGroup = getStepMetaTagDefault(tag)
                                .map(stepMetaTagDefault -> stepMetaTagDefault ? stepMetaDefaultsGroup : stepMetaGroup)
                                .orElse(typeMetaGroup);
                        return entry(foldingGroup, foldingGroup.toString());

                    }
                });
            }

            private void foldSubTags(XmlTag xmlTag, Function<XmlTag, Map.Entry<FoldingGroup, String>> f) {
                StreamEx.of(xmlTag.getSubTags())
                        .map(subTag -> Optional.ofNullable(f.apply(subTag))
                                .map(fold -> createFoldingDescriptor(subTag, subTag.getTextRange(), fold.getKey(), fold.getValue()))
                                .orElse(null))
                        .groupRuns((fd1, fd2) -> fd1 != null && fd2 != null && fd1.getGroup() == fd2.getGroup() && Objects.equals(fd1.getPlaceholderText(), fd2.getPlaceholderText()))
                        .filter(fd -> null != fd.get(0))
                        .map(fds -> mergeFoldingDescriptors(xmlTag, fds))
                        .forEach(foldingDescriptors::add);
            }

            @NotNull
            private FoldingDescriptor mergeFoldingDescriptors(XmlTag xmlTag, List<FoldingDescriptor> fds) {
                FoldingDescriptor first = fds.get(0);
                FoldingGroup group = first.getGroup();
                int startOffset = first.getRange().getStartOffset();
                int endOffset = fds.get(fds.size() - 1).getRange().getEndOffset();
                String placeholderText = first.getPlaceholderText();
                if (null == placeholderText && null != group) {
                    placeholderText = group.toString();
                }
                TextRange range = new TextRange(startOffset, endOffset);
                return createFoldingDescriptor(xmlTag, range, group, placeholderText);
            }

            @NotNull
            private FoldingDescriptor createFoldingDescriptor(XmlTag xmlTag, TextRange range, FoldingGroup group, String placeholderText) {
                return new FoldingDescriptor(xmlTag.getNode(), range, group, emptySet(), false, placeholderText, true);
            }

        }.visitElement(root);
        return foldingDescriptors.toArray(FoldingDescriptor[]::new);
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
        return isBlank(xmlTag) || xmlTag.getSubTags().length > 2;
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
            return "<hop> " + fromValue + "  \u27F6  " + toValue+ " </hop>";
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
