package gui;

import business.MutableTaskNode;
import business.Task;
import data.LoggingUtility;
import dto.ReflectionUtility;
import i18n.I18nSupport;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 27.01.13
 * Time: 17:13
 */
public class TaskNodeTable extends JComponent {
  private static final Dimension DIMENSION_MAX =
      new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  private JTable table;
  private TaskTableModel model;
  private MutableTaskNode currentNode;

  /* Constructors */

  public TaskNodeTable() {
    JScrollPane tableScrollPane = new JScrollPane(getTable(),
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    tableScrollPane.setMaximumSize(DIMENSION_MAX);
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(tableScrollPane);
  }

  /* Methods */

  public void setLayout(LayoutManager layout) {
    if(layout instanceof BoxLayout && ((BoxLayout) layout).getAxis() == BoxLayout.PAGE_AXIS)
      super.setLayout(layout);
    else throw new IllegalArgumentException(
        "TaskTable's layout must be instance of BoxLayout and have PAGE_AXIS");
  }

  public void showNode(MutableTaskNode node) {
    if(node != null) {
      currentNode = node;
      final Enumeration<MutableTaskNode> children = node.children();
      model.clear();
      while(children.hasMoreElements())
        model.add(children.nextElement().getTask());
    }
  }

  public void updateTable() {
    showNode(currentNode);
  }

  /* Getter and Setter */

  private JTable getTable() {
    if(table != null)
      return table;

    table = new JTable();
    model = new TaskTableModel();

    table.setModel(model);
    table.setMaximumSize(DIMENSION_MAX);

    return table;
  }
}

class TaskTableModel implements TableModel {
  private static final String GUI_BUNDLE = "gui"; //NON-NLS
  private static final List<Method> GETTER = ReflectionUtility.getSortedGetters(Task.class);
  private static final List<Method> SETTER = ReflectionUtility.getSortedSetters(Task.class);
  private static final Logger LOGGER =
      LoggingUtility.getLogger(TaskTableModel.class.getName());

  private List<String> columnNames;
  private int columnCount;
  private List<Task> tasks;
  private List<TableModelListener> listeners;

  TaskTableModel() {
    columnCount = GETTER.size(); //TODO später variabel gestalten, falls spalten nicht angezeigt werden
    tasks = new ArrayList<Task>();
    listeners = new ArrayList<TableModelListener>();
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

  private void checkIndices(int rowIndex, int columnIndex) {
    if(!(rowIndex >= 0 && rowIndex < tasks.size()))
      throw new ArrayIndexOutOfBoundsException("The first parameter is not >= 0 and not < "+tasks.size());
    if(!(columnIndex >= 0 && columnIndex < columnCount))
      throw new ArrayIndexOutOfBoundsException("The second parameter is not >= 0 and not < "+columnCount);
  }

  private void notifyListeners(TableModelEvent event) {
    for (TableModelListener listener : listeners)
      listener.tableChanged(event);
  }

  public int getRowCount() {
    return tasks.size();
  }

  public int getColumnCount() {
    return columnCount;
  }

  public String getColumnName(int columnIndex) {
    checkIndex(columnIndex, columnCount);
    return I18nSupport.getValue(GUI_BUNDLE, "text." + columnNames.get(columnIndex));
  }

  public Class<?> getColumnClass(int columnIndex) {
    checkIndex(columnIndex, columnCount);
    return GETTER.get(columnIndex).getReturnType();
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    checkIndices(rowIndex, columnIndex);
    return true;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    checkIndices(rowIndex, columnIndex);
    try {
      return GETTER.get(columnIndex).invoke(tasks.get(rowIndex));
    } catch (IllegalAccessException e) {
      LOGGER.severe("Illegal access on a task: " + e.getMessage());
    } catch (InvocationTargetException e) {
      LOGGER.severe("Could not invoke getter on a task: " + e.getMessage());
    }
    return null;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    checkIndices(rowIndex, columnIndex);
    try {
      SETTER.get(columnIndex).invoke(tasks.get(rowIndex), aValue);
      notifyListeners(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
    } catch (IllegalAccessException e) {
      LOGGER.severe("Illegal access on a task: " + e.getMessage());
    } catch (InvocationTargetException e) {
      LOGGER.severe("Could not invoke setter on a task: " + e.getMessage());
    }
  }

  public void addTableModelListener(TableModelListener l) {
    listeners.add(l);
  }

  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }

  public void add(Task task) {
    int index = tasks.size();
    tasks.add(task);
    notifyListeners(new TableModelEvent(this, index, index,
        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
  }

  public void clear() {
    if(!tasks.isEmpty()) {
      final int lastIndex = tasks.size()-1;
      tasks.clear();
      notifyListeners(new TableModelEvent(this, 0, lastIndex, TableModelEvent.ALL_COLUMNS,
          TableModelEvent.DELETE));
    }
  }
}