package resources;

import dto.TaskProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Logger;

import static dto.TaskProperty.Priority;
import static resources.ResourceList.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:59
 */
public class ResourceGetter {
  private static final Logger LOGGER = Logger.getLogger(ResourceGetter.class.getName());

  public static ImageIcon getImage(String imageName, String alternativeText) {
    ImageIcon image = null;

    try {
      image = loadImage(imageName, alternativeText);
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

  public static ImageIcon getTaskEventIcon(String eventString, Integer height) {
    final ImageIcon icon;
    if (TaskProperty.Attribution.BIRTHDAY.getDescription().equals(eventString))
      icon = getImage(IMAGE_ATTRIBUTION_BIRTHDAY, eventString);
    else if (TaskProperty.Attribution.FREE_TIME.getDescription().equals(eventString))
      icon = getImage(IMAGE_ATTRIBUTION_FREE_TIME, eventString);
    else if (TaskProperty.Attribution.WORK.getDescription().equals(eventString))
      icon = getImage(IMAGE_ATTRIBUTION_WORK, eventString);
    else return null;

    return getScaledImage(icon, null, height);
  }

  public static ImageIcon getTaskPriorityIcon(String  priorityString, Integer height) {
    final ImageIcon icon;
    if(Priority.LOWEST.getDescription().equals(priorityString))
      icon = getImage(IMAGE_PRIORITY_LOWEST, priorityString);
    else if(Priority.LOW.getDescription().equals(priorityString))
      icon = getImage(IMAGE_PRIORITY_LOW, priorityString);
    else if(Priority.MEDIUM.getDescription().equals(priorityString))
      icon = getImage(IMAGE_PRIORITY_MEDIUM, priorityString);
    else if(Priority.HIGH.getDescription().equals(priorityString))
      icon = getImage(IMAGE_PRIORITY_HIGH, priorityString);
    else if(Priority.HIGHEST.getDescription().equals(priorityString))
      icon = getImage(IMAGE_PRIORITY_HIGHEST, priorityString);
    else return null;

    return getScaledImage(icon ,null, height);
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