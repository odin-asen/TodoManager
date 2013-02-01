package gui;

import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:44
 */
public class FramePosition {
  private final int posX;
  private final int posY;
  private final int width;
  private final int height;
  private final Rectangle bounds;

  private FramePosition(Dimension screenSize, float frameToScreenWidthRatio,
                        float frameToScreenHeightRatio) {
    if(screenSize == null)
      screenSize = new Dimension(1366,768);

    width = (int) (screenSize.width* Math.abs(frameToScreenWidthRatio));
    height = (int) (screenSize.height* Math.abs(frameToScreenHeightRatio));
    posX = (int) (screenSize.width*0.5f- width *0.5f);
    posY = (int) (screenSize.height*0.5f- height *0.5f);
    bounds = new Rectangle(posX,posY, width, height);
  }

  public static FramePosition createFramePosition(float frameToScreenWidthRatio,
                                                  float frameToScreenHeightRatio) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new FramePosition(screenSize, frameToScreenWidthRatio, frameToScreenHeightRatio);
  }

  public static FramePosition createFramePosition(float screenRatio) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new FramePosition(screenSize, screenRatio, screenRatio);
  }

  public int getPosX() {
    return posX;
  }

  public int getPosY() {
    return posY;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @SuppressWarnings("ALL")
  public String toString() {
    return "" +posX+"; "+
        "PosY: "+posY+"; "+
        "Width: "+ width +"; "+
        "Height: "+ height +"; ";
  }

  public Rectangle getBounds() {
    return bounds;
  }
}
