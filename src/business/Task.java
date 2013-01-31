package business;

import dto.TaskProperty.Attribution;
import dto.TaskProperty.Priority;

import java.util.Calendar;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 22:54
 *
 * The getter and setter of this class must not be changed because they will be used for
 * reflection purposes.
 */
public class Task {
  private String name;
  private Boolean permanent;
  private Long dueDate;
  private Attribution attribution;
  private Priority priority;
  private String description;

  /* Constructors */

  public Task() {
    this("", false, Calendar.getInstance().getTimeInMillis(), Attribution.NOTHING,
        Priority.MEDIUM, "");
  }

  public Task(String name, Boolean permanent, Long timeInMillis, Attribution attribution,
              Priority priority, String description) {
    setName(name);
    setPermanent(permanent);
    setDueDate(timeInMillis);
    setAttribution(attribution);
    setPriority(priority);
    setDescription(description);
  }

  public Task(Task task) {
    this(task.getName(), task.isPermanent(), task.getDueDate(), task.getAttribution(),
        task.getPriority(), task.getDescription());
  }

  /* Methods */

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Task)) return false;

    Task task = (Task) o;

    return attribution == task.attribution && description.equals(task.description)
        && dueDate.equals(task.dueDate) && name.equals(task.name) &&
        permanent.equals(task.permanent) && priority == task.priority;
  }

  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + permanent.hashCode();
    result = 31 * result + dueDate.hashCode();
    result = 31 * result + attribution.hashCode();
    result = 31 * result + priority.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }

//  public static DTOTask toDTO(Task task) {
//    if(task == null)
//      return null;
//
//    final DTOTask dto = new DTOTask();
//    dto.children = task.children;
//    dto.description = task.getDescription();
//    dto.dueDate = task.getDueDate();
//    dto.attribution = task.getAttribution();
//    dto.name = task.getName();
//    dto.permanent = task.isPermanent();
//    dto.priority = task.getPriority();
//    return dto;
//  }
//
//  public static Task fromDTO(DTOTask dto) {
//    if(dto == null)
//      return null;
//
//    final Task task = new Task();
//    if(dto.children != null) {
//      for (Task child : dto.children)
//        task.addChild(child);
//    }
//    task.setParent(fromDTO(dto.parent));
//    task.setDescription(dto.description);
//    task.setDueDate(dto.dueDate);
//    task.setAttribution(dto.attribution);
//    task.setName(dto.name);
//    task.setPermanent(dto.permanent);
//    task.setPriority(dto.priority);
//
//    return task;
//  }

  /* Getter and Setter */

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name != null ? name : "";
  }

  public Long getDueDate() {
    return dueDate;
  }

  public void setDueDate(Long timeInMillis) {
    this.dueDate = timeInMillis != null ? timeInMillis : Long.MAX_VALUE;
  }

  public Boolean isPermanent() {
    return permanent;
  }

  public void setPermanent(Boolean permanent) {
    this.permanent = permanent != null ? permanent : false;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public void setAttribution(Attribution attribution) {
    this.attribution = attribution != null ? attribution : Attribution.NOTHING;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority != null ? priority : Priority.MEDIUM;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description != null ? description : "";
  }
}
