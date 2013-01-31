package dto;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import data.LoggingUtility;

import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 31.01.13
 * Time: 18:42
 */
public class DTOTaskConverter implements Converter {
  private static final Logger LOGGER =
      LoggingUtility.getLogger(DTOTaskConverter.class.getName());

  private static final String NAME_ID = "id";
  private static final String NAME_PARENT_ID = "parent";
  private static final String NAME_NAME = "name";
  private static final String NAME_PERMANENT = "permanent";
  private static final String NAME_DUE_DATE = "dueDate";
  private static final String NAME_PRIORITY = "priority";
  private static final String NAME_ATTRIBUTION = "attribution";
  private static final String NAME_DESCRIPTION = "description";

  private void addAttribute(HierarchicalStreamWriter writer, String name, Object value) {
    if(value != null)
      writer.addAttribute(name, value.toString());
  }

  public boolean canConvert(Class clazz) {
    return DTOTask.class == clazz;
  }

  public void marshal(Object value, HierarchicalStreamWriter writer,
                      MarshallingContext context) {
    DTOTask task = (DTOTask) value;
    addAttribute(writer, NAME_ID, task.id);
    addAttribute(writer, NAME_PARENT_ID, task.parentID);
    addAttribute(writer, NAME_NAME, task.name);
    addAttribute(writer, NAME_PERMANENT, task.permanent);
    addAttribute(writer, NAME_DUE_DATE, task.dueDate);
    addAttribute(writer, NAME_PRIORITY, task.priority);
    addAttribute(writer, NAME_ATTRIBUTION, task.attribution);
    if(task.description != null && !task.description.isEmpty()) {
      writer.startNode(NAME_DESCRIPTION);
      writer.setValue(task.description);
      writer.endNode();
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader,
                          UnmarshallingContext context) {
    DTOTask task = new DTOTask();
    String value = reader.getAttribute(NAME_ID);
    setID(value, task);
    value = reader.getAttribute(NAME_PARENT_ID);
    setParentID(value, task);
    value = reader.getAttribute(NAME_NAME);
    setName(value, task);
    value = reader.getAttribute(NAME_PERMANENT);
    setPermanent(value, task);
    value = reader.getAttribute(NAME_DUE_DATE);
    setDueDate(value, task);
    value = reader.getAttribute(NAME_PRIORITY);
    setPriority(value, task);
    value = reader.getAttribute(NAME_ATTRIBUTION);
    setAttribution(value, task);
    value = reader.getAttribute(NAME_DESCRIPTION);
    setDescription(value, task);
    return task;
  }

  private String getNotInterpretMessage(String valueName, String value) {
    return "Could not interpret "+valueName+". Set "+valueName+" to "+value;
  }

  private void setDescription(String value, DTOTask task) {
    if (value == null)
      task.description = "";
    else task.description = value;
  }

  private void setAttribution(String value, DTOTask task) {
    try {
      task.attribution = TaskProperty.Attribution.valueOf(value);
    } catch (IllegalArgumentException e) {
      task.attribution = TaskProperty.Attribution.NOTHING;
      LOGGER.warning(getNotInterpretMessage("attribution", task.attribution.toString()));
    }
  }

  private void setPriority(String value, DTOTask task) {
    try {
      task.priority = TaskProperty.Priority.valueOf(value);
    } catch (IllegalArgumentException e) {
      task.priority = TaskProperty.Priority.MEDIUM;
      LOGGER.warning(getNotInterpretMessage("priority", task.priority.toString()));
    }
  }

  private void setDueDate(String value, DTOTask task) {
    try {
      if (value == null)
        task.dueDate = 0L;
      else task.dueDate = Long.parseLong(value);
    } catch (NumberFormatException e) {
      LOGGER.warning(getNotInterpretMessage("due date", "0"));
      task.dueDate = 0L;
    }
  }

  private void setPermanent(String value, DTOTask task) {
    task.permanent = Boolean.parseBoolean(value);
  }

  private void setName(String value, DTOTask task) {
    if (value == null)
      task.name = "";
    else task.name = value;
  }

  private void setParentID(String value, DTOTask task) {
    if (value == null || value.isEmpty())
      task.parentID = null;
    else task.parentID = value;
  }

  private void setID(String value, DTOTask task) {
    if (value == null || value.isEmpty())
      task.id = null;
    else task.id = value;
  }
}
