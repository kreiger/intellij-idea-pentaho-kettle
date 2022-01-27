package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.ui.JBColor;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.ImageLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ImageUtil {
    public static final Color SPOON_STEP_BORDER_COLOR = new Color(0x3a, 0x64, 0x81);
    public static final Image MISSING_ENTRY_IMAGE = loadMissingEntry();
    public static final ImageIcon MISSING_ENTRY_ICON = new ImageIcon(MISSING_ENTRY_IMAGE);

    @NotNull public static Image drawOnBackground(Color color, float alpha, Image image) {
        //if (true) return image;
        image = com.intellij.util.ui.ImageUtil.resize(image, 32, ScaleContext.create());
        BufferedImage background = com.intellij.util.ui.ImageUtil.createImage(34, 34, TYPE_INT_RGB);
        Graphics2D g2 = background.createGraphics();
        g2.setColor(color);
        Composite composite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.fillRect(0, 0, background.getWidth(), background.getHeight());
        g2.setColor(SPOON_STEP_BORDER_COLOR);
        g2.drawRoundRect(0, 0, background.getWidth() - 1, background.getHeight() - 1, 5, 5);
        g2.setComposite(composite);
        g2.drawImage(image, 1, 1, 32, 32, null);
        g2.dispose();
        background = com.intellij.util.ui.ImageUtil.createRoundedImage(background, 7);
        return background;
    }

    public static Image loadMissingEntry() {
        Image image = ImageLoader.loadFromUrl(ImageUtil.class.getResource("/ui/images/missing_entry.svg"));
        return drawOnBackground(Color.RED, 0.2f, image);
    }

    @NotNull public static Image graphImage(Image image) {
        return drawOnBackground(Color.WHITE, 1, image);
    }

    @NotNull public static ImageIcon graphIcon(Image image) {
        return new ImageIcon(graphImage(image));
    }

    @NotNull public static ImageIcon graphIcon(URL resource) {
        return graphIcon(ImageLoader.loadFromUrl(resource));
    }
}
