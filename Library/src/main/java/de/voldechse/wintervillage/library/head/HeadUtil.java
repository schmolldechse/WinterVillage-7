package de.voldechse.wintervillage.library.head;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class HeadUtil {

    private static Rectangle head = new Rectangle(8, 8, 8, 8);
    private static Rectangle helmet = new Rectangle(40, 8, 8, 8);

    public static BufferedImage getHead(URL skinUrl) throws IOException {
        BufferedImage skin = ImageIO.read(skinUrl);
        BufferedImage headImage = getSkinPart(skin, head);

        Graphics graphics = headImage.createGraphics();
        graphics.drawImage(getSkinPart(skin, helmet), 0, 0, null);
        graphics.dispose();

        return headImage;
    }

    private static BufferedImage getSkinPart(BufferedImage skin, Rectangle head) {
        return skin.getSubimage(head.x, head.y, head.width, head.height);
    }
}
