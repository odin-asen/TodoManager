package business;

import data.LoggingUtility;
import dto.ReflectionUtility;
import gui.treeTable.AbstractTreeTableModel;
import gui.treeTable.TreeTableModel;
import i18n.I18nSupport;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import static i18n.BundleStrings.COMPONENTS;

/**
 * User: Timm Herrmann
 * Date: 02.02.13
 * Time: 16:24
 */
public class TaskTreeTableModel extends AbstractTreeTableModel {
  private static final List<Method> GETTER = ReflectionUtility.getSortedGetters(Task.class);
  private static final List<Method> SETTER = ReflectionUtility.getSortedSetters(Task.class);
  private static final Logger LOGGER =
      LoggingUtility.getLogger(TaskTreeTableModel.class.getName());

  private List<String> columnNames;

  public TaskTreeTableModel(MutableTaskNode rootNode) {
    super(rootNode);
    root = rootNode;
    initColumnNames();
  }

  private void initColumnNames() {
    List<Field> getterSetterFields = ReflectionUtility.getterSetterFields(Task.class);
    columnNames = new ArrayList<String>(getterSetterFields.size());
    for (Field field : getterSetterFields)
      columnNames.add(field.getName());
  }

  private void checkIndex(int index, int compareTo) {
    if(!(index >= 0 && index < compareTo))
      throw new ArrayIndexOutOfBoundsException("The parameter is not >= 0 and not < "+compareTo);
  }

  public void resetI18n() {
    initColumnNames();
  }

  public Object getChild(Object parent, int index) {
    return ((MutableTaskNode) parent).getChildAt(index);
  }

  public int getChildCount(Object parent) {
    if(parent instanceof MutableTaskNode)
      return ((MutableTaskNode) parent).getChildCount();
    else return -1;
  }

  public int getIndexOfChild(Object parent, Object child) {
    if(parent instanceof TreeNode && child instanceof TreeNode)
      return ((TreeNode) parent).getIndex((TreeNode) child);
    else return -1;
  }

  public int getColumnCount() {
    return columnNames.size();
  }

  public String getColumnName(int columnIndex) {
    checkIndex(columnIndex, columnNames.size());
    return I18nSupport.getValue(COMPONENTS, "text." + columnNames.get(columnIndex));
  }

  public Class<?> getColumnClass(int columnIndex) {
    if(columnIndex == 0)
      return TreeTableModel.class;

    checkIndex(columnIndex, columnNames.size());
    return GETTER.get(columnIndex).getReturnType();
  }

  public Object getValueAt(Object node, int columnIndex) {
    checkIndex(columnIndex, columnNames.size());
    try {
      return GETTER.get(columnIndex).invoke(((MutableTaskNode) node).getTask());
    } catch (IllegalAccessException e) {
      LOGGER.severe("Illegal access on a task: " + e.getMessage());
    } catch (InvocationTargetException e) {
      LOGGER.severe("Could not invoke getter on a task: " + e.getMessage());
    }
    return null;
  }

  public boolean isCellEditable(Object node, int columnIndex) {
    return true; /* Important to activate TreeExpandListener */
  }

  public void setValueAt(Object value, Object node, int columnIndex) {
    checkIndex(columnIndex, columnNames.size());
    try {
      final Class parameterClass = GETTER.get(columnIndex).getReturnType();
      if(value != null && value.getClass().equals(parameterClass))
        SETTER.get(columnIndex).invoke(((MutableTaskNode) node).getTask(), value);
    } catch (IllegalAccessException e) {
      LOGGER.severe("Illegal access on a task: " + e.getMessage());
    } catch (InvocationTargetException e) {
      LOGGER.severe("Could not invoke getter on a task: " + e.getMessage());
    }
  }

  public void add(TreePath parent, MutableTaskNode child){
    if(parent == null)
      parent = new TreePath(root);
    final MutableTaskNode parentNode = (MutableTaskNode) parent.getLastPathComponent();
    int index = getChildCount(parentNode);
    parentNode.insert(child, index);
    fireTreeNodesInserted(this, new Object[]{parent}, new int[]{index},
        new Object[]{child});
  }

  public int remove(TreePath path) {
    final int removed;

    if(path == null || root.equals(path.getLastPathComponent())) {
      removed = removeAll();
    } else {
      final MutableTaskNode node = (MutableTaskNode) path.getLastPathComponent();
      int index = getIndexOfChild(node.getParent(), node);
      removed = node.countAllSubNodes()+1;
      if(index != -1) {
        ((MutableTaskNode) node.getParent()).remove(index);
        fireTreeNodesRemoved(this, new Object[]{path.getParentPath()}, new int[]{index},
            new Object[]{node});
      }
    }
    return removed;
  }

  private int removeAll() {
    final MutableTaskNode taskRoot = ((MutableTaskNode) getRoot());
    int removed = taskRoot.countAllSubNodes();
    final TreePath rootPath = new TreePath(root);
    int[] indices = new int[removed];
    Object[] nodes = new Object[removed];

    final Enumeration<MutableTaskNode> children = taskRoot.children();
    for (int index = 0; children.hasMoreElements(); index++) {
      indices[index] = index;
      nodes[index] = children.nextElement();
      taskRoot.remove((MutableTreeNode) nodes[index]);
    }
    fireTreeNodesRemoved(this, new Object[]{rootPath}, indices, nodes);
    return removed;
  }
}
