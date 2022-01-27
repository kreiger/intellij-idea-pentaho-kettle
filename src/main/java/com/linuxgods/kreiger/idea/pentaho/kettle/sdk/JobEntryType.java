package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.google.common.base.Suppliers;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URL;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class JobEntryType {
    public static final Logger LOGGER = LoggerFactory.getLogger(StepType.class);
    private final String id;
    private final String imagePath;
    private final String className;
    private final Supplier<Icon> icon;

    public JobEntryType(String id, String imagePath, String className, Function<JobEntryType, Icon> iconLoader) {
        this.id = requireNonNull(id);
        this.imagePath = requireNonNull(imagePath);
        this.className = className;
        this.icon = Suppliers.memoize(() -> iconLoader.apply(this));
    }

    public JobEntryType(Element element, Function<JobEntryType, Icon> iconLoader) {
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
        Element stepElement = new Element("job-entry");
        stepElement.setAttribute("id", getId());
        stepElement.setAttribute("image", getImagePath());
        if (null != getClassName()) stepElement.setAttribute("classname", getClassName());
        return stepElement;
    }

    public Icon getIcon() {
        return icon.get();
    }

    public static Icon loadIcon(ClassLoader classLoader, StepType stepType) {
        return loadIcon(classLoader, stepType.getImagePath(), stepType.getClassName());
    }
    public static Icon loadIcon(ClassLoader classLoader, JobEntryType jobEntryType) {
        return loadIcon(classLoader, jobEntryType.getImagePath(), jobEntryType.getClassName());
    }

    public static Icon loadIcon(ClassLoader classLoader, String imagePath, String className) {
        try {
            URL resource = classLoader.getResource(imagePath);
            if (null == resource) {
                LOGGER.warn("No resource for "+imagePath+" on "+ className);
                if (imagePath.startsWith("images/")) {
                    return loadIcon(classLoader, "ui/"+imagePath, className);
                } else if (!imagePath.contains("/")) {
                    return loadIcon(classLoader, "ui/images/"+imagePath, className);
                }
                return ImageUtil.MISSING_ENTRY_ICON;
            }
            return ImageUtil.graphIcon(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load "+ imagePath +" for "+ className, e);
        }
    }

}
