package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.IconManager;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractList;
import java.util.List;
import java.util.stream.Stream;

public class KettleIcons {
    public static final @NotNull Logger LOGGER = Logger.getInstance(KettleIcons.class);
    public static final @NotNull Icon KETTLE_ICON = IconManager.getInstance().getIcon("/ui/images/kettle_logo_small.svg", KettleIcons.class);

    public static Stream<StepType> loadStepsXml(InputStream resourceAsStream, URLClassLoader classLoader) {
        Document document = parseXmlResource(resourceAsStream);
        NodeList stepsElement = document.getElementsByTagName("step");
        return asList(stepsElement).stream()
                .flatMap(element -> {
                    String className = getSubTagText(element, "classname");
                    String iconPath = getIconPath(element);
                    String[] ids = element.getAttribute("id").split(",");
                    return Stream.of(ids).map(id -> new StepType(id, iconPath, className, path -> StepType.loadIcon(classLoader, path)));
                });
    }

    private static URL getIconURL(Element element) {
        String iconPath = getIconPath(element);
        URL iconUrl = KettleIcons.class.getResource("/" + iconPath);
        if (null == iconUrl) {
            LOGGER.warn("Kettle step resource not found: " + iconPath);
        }
        return iconUrl;
    }

    private static String getIconPath(Element element) {
        return getSubTagText(element, "iconfile");
    }

    private static String getSubTagText(Element element, String subTagName) {
        return element.getElementsByTagName(subTagName).item(0).getTextContent();
    }

    private static Document parseXmlResource(InputStream stream) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(stream);
            return document;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Element> asList(NodeList elements) {
        return new AbstractList<>() {
            @Override public Element get(int i) {
                return (Element) elements.item(i);
            }

            @Override public int size() {
                return elements.getLength();
            }
        };
    }

}
