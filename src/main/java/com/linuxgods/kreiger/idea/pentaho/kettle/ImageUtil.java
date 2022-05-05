package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.ui.scale.ScaleContext;
import com.intellij.util.SVGLoader;
import com.intellij.util.ui.JBImageIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ImageUtil {
    public static final Color SPOON_STEP_BORDER_COLOR = new Color(0x3a, 0x64, 0x81);
    public static final ImageIcon MISSING_ENTRY_ICON = loadMissingEntry();

    @NotNull
    public static Image drawOnBackground(Color color, Image image) {
        BufferedImage background = com.intellij.util.ui.ImageUtil.createImage(34, 34, TYPE_INT_RGB);
        Graphics2D g = background.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, background.getWidth(), background.getHeight());
        g.drawImage(image, 1, 1, 32, 32, null);
        return background;
    }

    public static ImageIcon loadMissingEntry() {
        Image image;
        try {
            image = loadSVG(ImageUtil.class.getResource("/ui/images/missing_entry.svg"), 32, 32);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JBImageIcon(drawOnBackground(Color.RED, image));
    }

    @NotNull
    public static Image graphImage(Image image) {
        return drawOnBackground(Color.WHITE, image);
    }

    @NotNull
    public static ImageIcon graphIcon(Image image) {
        return new JBImageIcon(graphImage(image));
    }

    public static Icon graphIcon(URL url) {
        try {
            return graphIcon(loadSVG(url, 32, 32));
        } catch (IOException e) {
            return MISSING_ENTRY_ICON;
        }
    }

    @Nullable
    private static BufferedImage loadSVG(URL url, int width, int height) throws IOException {
        BufferedImage image;
        image = (BufferedImage) SVGLoader.load(url, url.openStream(), ScaleContext.create(), width, height);
        image = (BufferedImage) com.intellij.util.ui.ImageUtil.ensureHiDPI(image, ScaleContext.create());
        return image;
    }


}
