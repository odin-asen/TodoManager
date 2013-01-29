package dto;

import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;

/**
* User: Timm Herrmann
* Date: 11.12.12
* Time: 00:49
*/
public interface TaskProperty {
  String BUNDLE_GUI = "gui"; //NON-NLS

  public enum Attribution {
    NOTHING(null),
    BIRTHDAY(ResourceGetter.getImage(ResourceList.IMAGE_ATTRIBUTION_BIRTHDAY,"")),
    FREE_TIME(ResourceGetter.getImage(ResourceList.IMAGE_ATTRIBUTION_FREE_TIME,"")),
    WORK(ResourceGetter.getImage(ResourceList.IMAGE_ATTRIBUTION_WORK,""));

    private ImageIcon icon;

    Attribution(ImageIcon icon) {
      this.icon = icon;
    }

    public String getDescription() {
      final String description;
      if(this.equals(BIRTHDAY)) description = I18nSupport.getValue(BUNDLE_GUI, "task.attribution.birthday");
      else if(this.equals(FREE_TIME)) description = I18nSupport.getValue(BUNDLE_GUI, "task.attribution.free.time");
      else if(this.equals(NOTHING)) description = I18nSupport.getValue(BUNDLE_GUI, "task.attribution.nothing");
      else if(this.equals(WORK)) description = I18nSupport.getValue(BUNDLE_GUI, "task.attribution.work");
      else description = "";
      return description;
    }

    public ImageIcon getIcon(Integer height) {
      return ResourceGetter.getScaledImage(icon, null, height);
    }
  }

  public enum Priority {
    LOWEST(ResourceGetter.getImage(ResourceList.IMAGE_PRIORITY_LOWEST,"")),
    LOW(ResourceGetter.getImage(ResourceList.IMAGE_PRIORITY_LOW,"")),
    MEDIUM(ResourceGetter.getImage(ResourceList.IMAGE_PRIORITY_MEDIUM,"")),
    HIGH(ResourceGetter.getImage(ResourceList.IMAGE_PRIORITY_HIGH,"")),
    HIGHEST(ResourceGetter.getImage(ResourceList.IMAGE_PRIORITY_HIGHEST,""));

    private ImageIcon icon;

    Priority(ImageIcon icon) {
      this.icon = icon;
    }

    public String getDescription() {
      final String description;
      if(this.equals(LOWEST)) description = I18nSupport.getValue(BUNDLE_GUI, "task.priority.lowest");
      else if(this.equals(LOW)) description = I18nSupport.getValue(BUNDLE_GUI, "task.priority.low");
      else if(this.equals(MEDIUM)) description = I18nSupport.getValue(BUNDLE_GUI, "task.priority.medium");
      else if(this.equals(HIGH)) description = I18nSupport.getValue(BUNDLE_GUI, "task.priority.high");
      else if(this.equals(HIGHEST)) description = I18nSupport.getValue(BUNDLE_GUI, "task.priority.highest");
      else description = "";
      return description;
    }

    public ImageIcon getIcon(Integer height) {
      return ResourceGetter.getScaledImage(icon, null, height);
    }
  }
}
