package dto;

import business.Task;

import java.util.List;

import static dto.TaskProperty.Attribution;
import static dto.TaskProperty.Priority;

/**
 * User: Timm Herrmann
 * Date: 11.12.12
 * Time: 00:45
 */
public class DTOTask {
  public DTOTask parent;
  public List<Task> children;
  public String name;
  public Boolean permanent;
  public Long dueDate;
  public Priority priority;
  public Attribution attribution;
  public String description;
}
