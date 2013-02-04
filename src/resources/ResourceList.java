package resources;

/**
 * User: Timm Herrmann
 * Date: 29.10.12
 * Time: 17:17
 */
@SuppressWarnings("HardCodedStringLiteral")
public interface ResourceList {
  String RESOURCES_ROOT = "";
  String PICTURES_ROOT = RESOURCES_ROOT + "icons/";

  String IMAGE_ATTRIBUTION_BIRTHDAY = PICTURES_ROOT + "birthday_cake.png";
  String IMAGE_ATTRIBUTION_FREE_TIME = PICTURES_ROOT + "tent.png";
  String IMAGE_ATTRIBUTION_WORK = PICTURES_ROOT + "industry.png";
  String IMAGE_PRIORITY_LOWEST = PICTURES_ROOT + "triangle_green.png";
  String IMAGE_PRIORITY_LOW = PICTURES_ROOT + "triangle_turquoise.png";
  String IMAGE_PRIORITY_MEDIUM = PICTURES_ROOT + "triangle_yellow.png";
  String IMAGE_PRIORITY_HIGH = PICTURES_ROOT + "triangle_orange.png";
  String IMAGE_PRIORITY_HIGHEST = PICTURES_ROOT + "triangle_red.png";
  String IMAGE_PLUS_GREEN = PICTURES_ROOT + "plus_green.png";
  String IMAGE_MINUS_RED = PICTURES_ROOT + "minus_red.png";
  String IMAGE_ARROWS_IN = PICTURES_ROOT + "arrows_in.png";
  String IMAGE_ARROWS_OUT = PICTURES_ROOT + "arrows_out.png";
  String IMAGE_TODO_LIST = PICTURES_ROOT + "todo_list.png";
  String IMAGE_EDIT = PICTURES_ROOT + "edit.png";
  String IMAGE_CLOSE = PICTURES_ROOT + "close.png";
  String IMAGE_CLOSE_ROLLOVER = PICTURES_ROOT + "close_dark.png";
  String IMAGE_OPEN_FILE = PICTURES_ROOT + "open_folder.png";
  String IMAGE_SAVE_FILE = PICTURES_ROOT + "flash_drive.png";
  String IMAGE_UPPER_TASK = PICTURES_ROOT + "todo_list_table.png";
  String IMAGE_SINGLE_TASK = PICTURES_ROOT + "tick.png";
}
