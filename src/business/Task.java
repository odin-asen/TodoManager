package business;

import dto.DTOTask;
import dto.TaskProperty;

import java.util.Calendar;

import static dto.TaskProperty.Priority;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 22:54
 *
 * The getter and setter of this class must not be changed because they will be used for
 * reflection purposes.
 */
public class Task {
  private TaskCategory category;
  private String name;
  private Boolean permanent;
  private Long dueDate;
  private TaskProperty.Attribution attribution;
  private Priority priority;
  private String description;

  /* Constructors */

  public Task() {
    this(new TaskCategory(""), "", "", false,
        Calendar.getInstance().getTimeInMillis(),
        TaskProperty.Attribution.NOTHING, Priority.MEDIUM);
  }

  public Task(TaskCategory category, String name, String description,
              boolean permanent, Long timeInMillis, TaskProperty.Attribution attribution,
              Priority priority) {
    this.category = category;
    this.name = name;
    this.description = description;
    this.permanent = permanent;
    this.dueDate = timeInMillis;
    this.attribution = attribution;
    this.priority = priority;
  }

  /* Methods */

  public String toString() {
    return toDTO(this).toString();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Task)) return false;

    Task task = (Task) o;

    return category.equals(task.category) &&
        description.equals(task.description) &&
        dueDate.equals(task.dueDate) &&
        attribution == task.attribution && name.equals(task.name) &&
        permanent.equals(task.permanent) && priority == task.priority;
  }

  public int hashCode() {
    int result = category.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + permanent.hashCode();
    result = 31 * result + dueDate.hashCode();
    result = 31 * result + attribution.hashCode();
    result = 31 * result + priority.hashCode();
    return result;
  }

  public static DTOTask toDTO(Task task) {
    final DTOTask dto = new DTOTask();
    dto.category = task.getCategory().toString();
    dto.description = task.getDescription();
    dto.dueDate = task.getDueDate();
    dto.attribution = task.getAttribution();
    dto.name = task.getName();
    dto.permanent = task.isPermanent();
    dto.priority = task.getPriority();
    return dto;
  }

  public static Task fromDTO(DTOTask dto) {
    final Task task = new Task();
    task.setCategory(dto.category);
    task.setDescription(dto.description);
    task.setDueDate(dto.dueDate);
    task.setAttribution(dto.attribution);
    task.setName(dto.name);
    task.setPermanent(dto.permanent);
    task.setPriority(dto.priority);
    return task;
  }


  /* Getter and Setter */

  public TaskCategory getCategory() {
    return category;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setCategory(TaskCategory category) {
    assert category != null;
    this.category = new TaskCategory(category.getName(), category.getParent());
  }

  public void setCategory(String toParse) {
    if(toParse != null)
      category = TaskCategory.parseCategory(toParse);
    else category = new TaskCategory("");
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name != null ? name : "";
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description != null ? description : "";
  }

  public Long getDueDate() {
    return dueDate;
  }

  public void setDueDate(Long timeInMillis) {
    if(timeInMillis != null)
      dueDate = timeInMillis;
    else dueDate = Calendar.getInstance().getTimeInMillis();
  }

  public Boolean isPermanent() {
    return permanent;
  }

  public void setPermanent(Boolean permanent) {
    this.permanent = permanent != null ? permanent : Boolean.valueOf(false);
  }

  public TaskProperty.Attribution getAttribution() {
    return attribution;
  }

  public void setAttribution(TaskProperty.Attribution attribution) {
    this.attribution = attribution != null ? attribution : TaskProperty.Attribution.NOTHING;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority != null ? priority : Priority.MEDIUM;
  }
}
