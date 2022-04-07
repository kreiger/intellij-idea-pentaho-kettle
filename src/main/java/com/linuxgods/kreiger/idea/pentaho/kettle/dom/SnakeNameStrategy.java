package com.linuxgods.kreiger.idea.pentaho.kettle.dom;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.NameUtilCore;
import com.intellij.util.xml.DomNameStrategy;
import org.jetbrains.annotations.NotNull;

public class SnakeNameStrategy extends DomNameStrategy {

    @Override
    public @NotNull String convertName(@NotNull String propertyName) {
        final String[] words = NameUtilCore.nameToWords(propertyName);
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtil.decapitalize(words[i]);
        }
        return StringUtil.join(words, "_");
    }

    @Override
    public String splitIntoWords(String xmlElementName) {
        return xmlElementName.replace('_', ' ');
    }
}
