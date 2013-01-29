package dto;

import static dto.TaskProperty.*;

/**
 * User: Timm Herrmann
 * Date: 11.12.12
 * Time: 00:45
 */
public class DTOTask {
  public String category;
  public String name;
  public Boolean permanent;
  public Long dueDate;
  public Priority priority;
  public Attribution attribution;
  public String description;
}
