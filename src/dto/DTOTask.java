package dto;

import static dto.TaskProperty.Attribution;
import static dto.TaskProperty.Priority;

/**
 * User: Timm Herrmann
 * Date: 11.12.12
 * Time: 00:45
 */
public class DTOTask {
  public String id;
  public String parentID;
  public String name;
  public Boolean permanent;
  public Long dueDate;
  public Priority priority;
  public Attribution attribution;
  public String description;

  @Override
  public String toString() {
    return "DTOTask{" +
        "id='" + id + '\'' +
        ", parentID='" + parentID + '\'' +
        ", name='" + name + '\'' +
        ", permanent=" + permanent +
        ", dueDate=" + dueDate +
        ", priority=" + priority +
        ", attribution=" + attribution +
        ", description='" + description + '\'' +
        '}';
  }
}