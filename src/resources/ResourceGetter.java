package resources;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:59
 */
public class ResourceGetter {
  private static final Logger LOGGER = Logger.getLogger(ResourceGetter.class.getName());

  public static ImageIcon getImage(String imageName) {
    ImageIcon image = null;

    try {
      image = loadImage(imageName, "");
    } catch (ResourceGetterException e) {
      LOGGER.info(e.getMessage());
    }

    return image;
  }

  private static ImageIcon loadImage(String imageURL, String alternativeText)
      throws ResourceGetterException {
    final ImageIcon image;

    final URL url = ResourceGetter.class.getResource(imageURL);
    if(url != null)
      image = new ImageIcon(url, alternativeText);
    else
      throw new ResourceGetterException("Could not find an URL for the path "+imageURL);

    return image;
  }

  public static ImageIcon getScaledImage(ImageIcon srcImg, Integer width, Integer height){
    if(srcImg == null) return null;
    if(width == null && height == null) return srcImg;

    if(width == null)
      width = (int) (height*(float)srcImg.getIconWidth()/srcImg.getIconHeight());
    else if(height == null)
      height = (int) (width*(float)srcImg.getIconHeight()/srcImg.getIconWidth());

    BufferedImage resizeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = resizeImage.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(srcImg.getImage(), 0, 0, width, height, null);
    g2.dispose();

    return new ImageIcon(resizeImage);
  }
}