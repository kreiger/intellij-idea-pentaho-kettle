package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.google.common.base.Suppliers;
import com.intellij.util.ImageLoader;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class StepType {
    public static final Logger LOGGER = LoggerFactory.getLogger(StepType.class);
    private final String id;
    private final String imagePath;
    private final String className;
    private final Supplier<Icon> icon;

    public StepType(String id, String imagePath, String className, Function<StepType, Icon> iconLoader) {
        this.id = requireNonNull(id);
        this.imagePath = requireNonNull(imagePath);
        this.className = className;
        this.icon = Suppliers.memoize(() -> iconLoader.apply(this));
    }

    public StepType(Element element, Function<StepType, Icon> iconLoader) {
        this(element.getAttributeValue("id"), element.getAttributeValue("image"), element.getAttributeValue("classname"), iconLoader);
    }

    public String getId() {
        return id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getClassName() {
        return className;
    }

    @NotNull Element createElement() {
        Element stepElement = new Element("step");
        stepElement.setAttribute("id", getId());
        stepElement.setAttribute("image", getImagePath());
        if (null != getClassName()) stepElement.setAttribute("classname", getClassName());
        return stepElement;
    }

    public Icon getIcon() {
        return icon.get();
    }

    public static Icon loadIcon(ClassLoader classLoader, StepType stepType) {
        return loadIcon(classLoader, stepType, stepType.getImagePath());
    }

    public static Icon loadIcon(ClassLoader classLoader, StepType stepType, String imagePath) {
        try {
            URL resource = classLoader.getResource(imagePath);
            if (null == resource) {
                LOGGER.warn("No resource for "+imagePath+" on "+stepType.getClassName());
                if (imagePath.startsWith("images/")) {
                    return loadIcon(classLoader, stepType, "ui/"+imagePath);
                } else if (!imagePath.contains("/")) {
                    return loadIcon(classLoader, stepType, "ui/images/"+imagePath);
                }
                return ImageUtil.MISSING_ENTRY_ICON;
            }
            Image image = ImageLoader.loadFromUrl(resource);
            return new ImageIcon(ImageUtil.drawOnBackground(Color.LIGHT_GRAY, 1, image));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load "+ imagePath +" for "+stepType.getClassName(), e);
        }
    }
}
