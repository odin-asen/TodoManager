package business;

import dto.DTOTask;

import java.util.*;

/**
 * User: Timm Herrmann
 * Date: 31.01.13
 * Time: 02:32
 */
public class Converter {
  /**
   * Creates a node from a list and returns it. The node is a root and has therefore no parent.
   * The method will only work properly if the surpassed list was created by
   * {@link #toDTOList(MutableTaskNode)}.
   * @param taskList List to convert to a MutableTaskNode.
   * @return An object of MutableTaskNode that is a root.
   */
  public static MutableTaskNode fromDTOList(List<DTOTask> taskList) {
    assert taskList != null;
    final Map<String, MutableTaskNode> nodeMap = getMap(taskList);
    final MutableTaskNode root = MutableTaskNode.getRootInstance();

    for (DTOTask dtoTask : taskList) {
      final String parentID = dtoTask.parentID;
      final MutableTaskNode node;
      if (parentID == null) node = root;
      else node = nodeMap.get(parentID);
      node.insert(nodeMap.get(dtoTask.id), node.getChildCount());
    }

    return root;
  }

  private static Map<String, MutableTaskNode> getMap(List<DTOTask> taskList) {
    final Map<String, MutableTaskNode> map =
        new HashMap<String, MutableTaskNode>(taskList.size());
    for (DTOTask dtoTask : taskList)
      map.put(dtoTask.id, new MutableTaskNode(fromDTO(dtoTask)));
    //TODO fehler ausgeben, wenn mehrere tasks die gleiche id haben
    return map;
  }

  private static Task fromDTO(DTOTask dtoTask) {
    if(dtoTask == null)
      return null;

    final Task task = new Task();
    task.setAttribution(dtoTask.attribution);
    task.setDescription(dtoTask.description);
    task.setDueDate(dtoTask.dueDate);
    task.setName(dtoTask.name);
    task.setPermanent(dtoTask.permanent);
    task.setPriority(dtoTask.priority);
    return task;
  }

  public static DTOTask toDTO(Task task, String id, String parentID) {
    if(task == null)
      return null;
    final DTOTask dto = new DTOTask();
    dto.id = id;
    dto.parentID = parentID;
    dto.attribution = task.getAttribution();
    dto.description = task.getDescription();
    dto.dueDate = task.getDueDate();
    dto.name = task.getName();
    dto.permanent = task.isPermanent();
    dto.priority = task.getPriority();

    return dto;
  }

  public static List<DTOTask> toDTOList(MutableTaskNode root) {
    assert root != null;
    final List<DTOTask> taskList = new ArrayList<DTOTask>(root.countAllSubNodes());
    final Enumeration<MutableTaskNode> children = root.children();
    Integer id = 0;
    while (children.hasMoreElements()) {
      addChild(taskList, children.nextElement(), id.toString(), null);
      id++;
    }

    return taskList;
  }

  private static void addChild(List<DTOTask> taskList, MutableTaskNode child,
                               String childID, String parentID) {
    final Enumeration<MutableTaskNode> children = child.children();
    taskList.add(toDTO(child.getTask(), childID, parentID));
    Integer id = 0;
    while (children.hasMoreElements()) {
      addChild(taskList, children.nextElement(), childID+id.toString(), childID);
      id++;
    }
  }
}
