package business;

import java.util.*;

/**
 * User: Timm Herrmann
 * Date: 10.12.12
 * Time: 00:02
 *
 * This class is immutable!
 */
public class TaskCategory {
  public static final String CATEGORY_SEPARATOR = ".";

  private String name;
  private TaskCategory parent;
  private List<TaskCategory> children;

  /* Constructors */

  public TaskCategory(String name) {
    children = new ArrayList<TaskCategory>();
    setName(name);
    setParent(null);
  }

  public TaskCategory(String name, TaskCategory parent) {
    children = new ArrayList<TaskCategory>();
    setName(name);
    setParent(parent);
  }

  private TaskCategory(TaskCategory category) {
    children = new ArrayList<TaskCategory>();
    setName(category.getName());
    setParent(category.getParent());
  }

  /* Methods */

  public void addChild(TaskCategory child) {
    children.add(child);
  }

  public void removeChild(TaskCategory child) {
    children.remove(child);
  }

  public TaskCategory getChild(int index) {
    return new TaskCategory(children.get(index));
  }

  public static TaskCategory getRoot(TaskCategory category) {
    if(category != null) {
      if (category.getParent() != null)
        return getRoot(category.getParent());
      else return category;
    } else return null;
  }

  public TaskCategory getRoot() {
    return getRoot(this);
  }

  public String toString() {
    StringBuilder builder = new StringBuilder(name);
    TaskCategory category = getParent();
    while(category != null) {
      builder.insert(0,category.getName()+ CATEGORY_SEPARATOR);
      category = category.getParent();
    }
    return builder.toString();
  }

  /**
   * @return Returns a TaskCategory for the surpassed String. The String
   * should have the format of a String generated
   * by TaskCategory#toString()
   */
  public static TaskCategory parseCategory(String toParse) {
    TaskCategory parent = new TaskCategory("");
    final StringTokenizer tokenizer = new StringTokenizer(toParse, CATEGORY_SEPARATOR);

    if(tokenizer.hasMoreElements())
      parent = new TaskCategory(tokenizer.nextToken(), null);
    while (tokenizer.hasMoreTokens()) {
      parent = new TaskCategory(tokenizer.nextToken(), parent);
    }

    return parent;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TaskCategory)) return false;

    TaskCategory that = (TaskCategory) o;

    return this.toString().equals(that.toString());
  }

  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }

  public static String[] getCategories(TaskCategory category) {
    final List<String> hierarchy = new Stack<String>();
    hierarchy.add(category.getName());

    TaskCategory parent = category.getParent();
    while(parent != null) {
      hierarchy.add(parent.getName());
      parent = parent.getParent();
    }

    Collections.reverse(hierarchy);
    String[] strings = new String[hierarchy.size()];

    return hierarchy.toArray(strings);
  }

  public int getChildCount() {
    return children.size();
  }

  public int getChildIndex(TaskCategory child) {
    return children.indexOf(child);
  }

  /* Getter and Setter */

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name != null ? name : "";
  }

  public TaskCategory getParent() {
    return parent != null ? new TaskCategory(parent.getName(), parent.getParent()) : null;
  }

  public void setParent(TaskCategory parent) {
    if(this.parent != null) this.parent.removeChild(this);
    this.parent = parent == null
        ? null : new TaskCategory(parent.getName(), parent.getParent());
    if(this.parent != null) this.parent.addChild(this);
  }
}