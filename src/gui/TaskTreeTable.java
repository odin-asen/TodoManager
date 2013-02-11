package gui;

import business.MutableTaskNode;
import business.Task;
import business.TaskTreeTableModel;
import com.toedter.calendar.JDateChooser;
import dto.DTOTask;
import dto.TaskProperty;
import dto.Utility;
import gui.treeTable.TreeTableModel;
import gui.treeTable.TreeTableModelAdapter;
import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static dto.TaskProperty.Attribution;
import static dto.TaskProperty.Priority;
import static i18n.BundleStrings.MISC;

/**
 * User: Timm Herrmann
 * Date: 27.01.13
 * Time: 17:13
 */
public class TaskTreeTable extends JTable {
  private static final int DEFAULT_ROW_HEIGHT = 20;
  private TaskTreeTableCellRenderer treeRenderer;
  private TaskTreeTableModel treeTableModel;
  private boolean listChanged;
  private TreeTableModelAdapter treeTableModelAdapter;
  private TaskTableCellRenderer tableCellRenderer;

  /* Constructors */

  public TaskTreeTable() {
    listChanged = false;

    /* create JTree */
    getTreeRenderer();
    setRoot(MutableTaskNode.getRootInstance());

    /* simultaneous selection for the tree and the table */
    setSelectionModel(new ListToTreeSelectionModelWrapper(treeRenderer.getSelectionModel()));

    /* set renderer and editor for the tree */
    setDefaultRenderer(TreeTableModel.class, treeRenderer);
    setDefaultEditor(TreeTableModel.class, new TaskTreeTableCellEditor(treeRenderer, this));

    /* set remaining renderer and editors */
    tableCellRenderer = new TaskTableCellRenderer();
    setDefaultRenderer(Long.class, tableCellRenderer);
    setDefaultRenderer(Attribution.class, tableCellRenderer);
    setDefaultRenderer(Priority.class, tableCellRenderer);

    final TaskCellEditor editor = new TaskCellEditor();
    setDefaultEditor(Priority.class, editor);
    setDefaultEditor(Attribution.class, editor);
    setDefaultEditor(Long.class, editor);

    /* header moving is not allowed (makes less problems) */
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setResizingAllowed(false);

    /* set a default row height that fits for the date chooser and pictures */
    setRowHeight(DEFAULT_ROW_HEIGHT);
    treeRenderer.setRowHeight(DEFAULT_ROW_HEIGHT);
  }

  /* Methods */

  /**
   * Sets the widths for the columns. The tree column has always the width of the tree.
   * Every other column gets the width that is needed to show the whole content except for
   * the last (index=columnCount-1) column. As the last column should be for task
   * descriptions the column will be fit to the remaining space. It also has a minimum size
   * which is tableWidth/columnCount.
   */
  public void doLayout() {
    getColumn(treeTableModel.getColumnName(
        treeTableModel.getTreeColumn())).setWidth(treeRenderer.getPreferredSize().width);
    fitColumnsToComponents(false);
  }

  private void fitColumnsToComponents(boolean boundToMaxWidth) {
    final int maxWidth = getWidth()/getColumnCount();
    int rowSpan[] = getVisibleRowSpan();
    for (int index = 0; index < getColumnCount()-1; index++) {
      if(index != treeTableModel.getTreeColumn()) {
        final TableColumn column = getColumn(treeTableModel.getColumnName(index));
        final int componentWidth = calcBiggestNeededWidth(rowSpan[0], rowSpan[1], index);
        final int columnWidth = column.getPreferredWidth();
        if(boundToMaxWidth && (componentWidth > maxWidth || columnWidth > maxWidth))
          setColumnWidth(column, maxWidth);
        else setColumnWidth(column, componentWidth > columnWidth ? componentWidth
                                                                 : columnWidth);
      }
    }

    /* set the description column */
    setLastColumnWidth();
  }

  private int calcBiggestNeededWidth(int startRow, int endRow, int column) {
    int biggestWidth = 0;
    for (int row = startRow; row < endRow; row++) {
      final Component component = getCellRenderer(row, column).getTableCellRendererComponent(
          this, getValueAt(row, column), false, false, row, column);
      final int componentWidth = component.getPreferredSize().width + 5;
      biggestWidth = biggestWidth < componentWidth ? componentWidth : biggestWidth;
    }
    return biggestWidth;
  }

  private int[] getVisibleRowSpan() {
    final int[] rowSpan = new int[2];
    final int maxVisibleRows = getHeight()/getRowHeight()+1;
    final int visibleRows = maxVisibleRows > getRowCount() ? getRowCount() : maxVisibleRows;
    rowSpan[0] = getVisibleRect().y/getRowHeight();
    rowSpan[1] = rowSpan[0]+visibleRows;
    return rowSpan;
  }

  private void setColumnWidth(TableColumn column, int width) {
    if(column.getWidth() != width)
      column.setWidth(width);
  }

  private void setLastColumnWidth() {
    final TableColumn lastColumn = getColumn(getColumnName(getColumnCount()-1));
    int remainingSpace = getWidth() -
        (getColumnModel().getTotalColumnWidth() - lastColumn.getWidth());
    int minWidth =  getWidth()/getColumnCount();
    setColumnWidth(lastColumn, minWidth > remainingSpace ? minWidth : remainingSpace);
  }

  public void resetI18n() {
    treeTableModel.resetI18n();
    tableCellRenderer.resetI18n();
    treeTableModelAdapter.fireTableCellUpdated(TableModelEvent.HEADER_ROW,
        TableModelEvent.ALL_COLUMNS);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void changeSelectedTasks(DTOTask dtoTask) {
    final int[] selectedRows = getSelectedRows();
    for (int row : selectedRows) {
      final TreePath path = treeRenderer.getPathForRow(row);
      ((MutableTaskNode) path.getLastPathComponent()).getTask().change(dtoTask);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public Task getTask(int index) {
    final TreePath path = treeRenderer.getPathForRow(index);
    if(path != null)
      return ((MutableTaskNode) path.getLastPathComponent()).getTask();
    else return null;
  }

  public void addTask(Task task) {
    final boolean expand = treeTableModel.getChildCount(treeTableModel.getRoot()) == 0;
    final int selectedRow = getSelectedRow();
    treeTableModel.add(treeRenderer.getPathForRow(selectedRow), new MutableTaskNode(task));
    getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
    listChanged = true;
    if(expand)
      treeRenderer.expandPath(new TreePath(treeTableModel.getRoot()));
    updateUI();
  }

  /**
   * Removes all selected task and their sub tasks from the list.
   */
  public int removeSelectedTasks() {
    final MutableTaskNode root = ((MutableTaskNode) treeTableModel.getRoot());
    final int totalTasksBefore = root.countAllSubNodes();
    /* The rows of the table must be used because the tree does not seem to
     * react to selections properly.
     */
    final int[] selectionRows = getSelectedRows();

    for (int row : selectionRows)
      treeTableModel.remove(treeRenderer.getPathForRow(row));

    listChanged = selectionRows.length > 0;
    clearSelection();
    updateUI();
    return totalTasksBefore-root.countAllSubNodes();
  }

  public MutableTaskNode getTaskRoot() {
    return (MutableTaskNode) treeTableModel.getRoot();
  }

  public void setRoot(MutableTaskNode root) {
    treeTableModel = new TaskTreeTableModel(root);
    if(!treeTableModel.equals(treeRenderer.getModel()))
      treeRenderer.setModel(treeTableModel);
    treeTableModelAdapter = new TreeTableModelAdapter(treeTableModel, treeRenderer);
    super.setModel(treeTableModelAdapter);
  }

  /**
   * Collapses or expands all selected nodes, so that all sub nodes will collapse or expand
   * their children, so on and so far. The previous selected nodes that are still visible after a
   * collapse action will remain to be selected. If no node is selected all direct children of the
   * root will do the chosen action.
   * @param expand If true, all selected nodes will be expanded. If false, all selected nodes will
   *               be collapsed.
   */
  public void expandCollapseSelectedNodes(boolean expand) {
    final int[] rows = getSelectedRows();
    final List<TreePath> sortedPaths;
    if(rows.length == 0)
      sortedPaths = getRootChildrenPaths();
    else sortedPaths = Utility.getLengthSortedList(expand, rows, treeRenderer);
    final TreePath[] selectedPaths = new TreePath[sortedPaths.size()];
    for (int index = 0; index < sortedPaths.size(); index++) {
      final TreePath path = sortedPaths.get(index);
      if(expand)
        expandPathComplete(path);
      else collapsePathComplete(path);
      selectedPaths[index] = treeRenderer.getRowForPath(path) != -1 ? path : null;
    }
    setSelectionPaths(selectedPaths);
  }

  private List<TreePath> getRootChildrenPaths() {
    final List<TreePath> paths = new ArrayList<TreePath>();
    final TreePath rootPath = new TreePath(treeTableModel.getRoot());
    for (int index = 0; index < treeRenderer.getRowCount(); index++) {
      final TreePath path = treeRenderer.getPathForRow(index);
      if(path != null && rootPath.equals(path.getParentPath()))
        paths.add(path);
    }
    return paths;
  }

  private int expandPathComplete(TreePath path) {
    int nextRow = treeRenderer.getRowForPath(path)+1;
    treeRenderer.expandPath(path);
    TreePath nextPath = treeRenderer.getPathForRow(nextRow);
    while (path.isDescendant(nextPath)) {
      nextRow = expandPathComplete(nextPath);
      nextPath = treeRenderer.getPathForRow(nextRow);
    }
    return nextRow;
  }

  private int collapsePathComplete(TreePath path) {
    int nextRow = treeRenderer.getRowForPath(path)+1;
    TreePath nextPath = treeRenderer.getPathForRow(nextRow);
    while (path.isDescendant(nextPath)) {
      nextRow = collapsePathComplete(nextPath);
      nextPath = treeRenderer.getPathForRow(nextRow);
    }
    treeRenderer.collapsePath(path);
    return nextRow;
  }

  private void setSelectionPaths(TreePath[] paths) {
    clearSelection();
    for (TreePath path : paths) {
      final int row = treeRenderer.getRowForPath(path);
      getSelectionModel().addSelectionInterval(row,row);
    }
  }

  private TreePath[] getPathForRange(int index0, int index1) {
    if(index0 < 0 || index1 < 0)
      return new TreePath[0];

    final TreePath[] paths = new TreePath[Math.abs(index0-index1)+1];
    for (int index = 0, row = index0; index < paths.length; index++) {
      paths[index] = treeRenderer.getPathForRow(row);
      if(index0 < index1)
        row++;
      else row--;
    }
    return paths;
  }

  /* Getter and Setter */

  /**
   * Create the component that contains all category trees
   */
  private TaskTreeTableCellRenderer getTreeRenderer() {
    if(treeRenderer != null)
      return treeRenderer;

    treeRenderer = new TaskTreeTableCellRenderer(this, treeTableModel);
    treeRenderer.setRootVisible(false);
    return treeRenderer;
  }

  public boolean hasListChanged() {
    return listChanged;
  }

  public void setListChanged(boolean changed) {
    listChanged = changed;
  }

  public boolean hasSelectedTasks() {
    return getSelectedRow() != -1;
  }

  /* Inner classes */

  private class ListToTreeSelectionModelWrapper extends DefaultListSelectionModel {
    private TreeSelectionModel treeSelectionModel;
    private ListToTreeSelectionModelWrapper(TreeSelectionModel treeSelectionModel) {
      this.treeSelectionModel = treeSelectionModel;
    }

    public void addSelectionInterval(int index0, int index1) {
      super.addSelectionInterval(index0, index1);
      treeSelectionModel.addSelectionPaths(getPathForRange(index0, index1));
    }

    public void removeSelectionInterval(int index0, int index1) {
      super.removeSelectionInterval(index0, index1);
      treeSelectionModel.removeSelectionPaths(getPathForRange(index0, index1));
    }

    public void setSelectionInterval(int index0, int index1) {
      super.setSelectionInterval(index0, index1);
      treeSelectionModel.setSelectionPaths(getPathForRange(index0, index1));
    }

    public void setSelectionMode(int selectionMode) {
      super.setSelectionMode(selectionMode);
    }
  }
}

class TaskTreeTableCellRenderer extends JTree implements TableCellRenderer {
  /**
   * The last row that was rendered.
   */
  protected int rowToPaint;

  private TaskTreeTable treeTable;

  public TaskTreeTableCellRenderer(TaskTreeTable treeTable, TreeModel model) {
    super(model);
    this.treeTable = treeTable;
    setCellRenderer(new TaskTreeCellRenderer());
    /* Set the row height for the JTable */
    /* Must be called because treeTable is still null */
    /* when super(model) calls setRowHeight! */
    setRowHeight(getRowHeight());
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    if(isSelected) {
      setBackground(table.getSelectionBackground());
    } else {
      setBackground(table.getBackground());
    }
    rowToPaint = row;
    return this;
  }

  public void paint(Graphics g) {
    g.translate(0, -rowToPaint*getRowHeight());
    super.paint(g);
  }

  /**
   * Tree has to have the same height as the table.
   */
  public void setBounds(int x, int y, int w, int h) {
    super.setBounds(x, 0, w, treeTable.getHeight());
  }

  /**
   * Tree und Table have to have the same height
   */
  public void setRowHeight(int rowHeight) {
    if (rowHeight > 0) {
      super.setRowHeight(rowHeight);
      if (treeTable != null && treeTable.getRowHeight() != rowHeight) {
        treeTable.setRowHeight(getRowHeight());
      }
    }
  }
}

class TaskTableCellRenderer extends JLabel implements TableCellRenderer {
  private DateFormat dateFormat;

  public TaskTableCellRenderer() {
    setOpaque(true);
    resetI18n();
  }

  public void resetI18n() {
    dateFormat = new SimpleDateFormat(
        I18nSupport.getValue(MISC, "format.due.date"), Locale.getDefault());
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    if(isSelected) {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    } else {
      setBackground(table.getBackground());
      setForeground(table.getForeground());
    }
    if(value instanceof Long) {
      setText(dateFormat.format(new Date((Long) value)));
      setIcon(null);
    } else if(value instanceof Attribution) {
      setText(((Attribution) value).getDescription());
      setIcon(((Attribution) value).getIcon(table.getRowHeight()));
    } else if(value instanceof Priority) {
      setText(((Priority) value).getDescription());
      setIcon(((Priority) value).getIcon(table.getRowHeight()));
    }
    return this;
  }
}

class TaskTreeCellRenderer extends DefaultTreeCellRenderer {
  final Icon upperTaskIcon = ResourceGetter.getImage(ResourceList.IMAGE_UPPER_TASK);
  final Icon defaultTaskIcon = ResourceGetter.getImage(ResourceList.IMAGE_SINGLE_TASK);
  public TaskTreeCellRenderer() {
    setOpaque(false);
  }

  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    if(leaf) setIcon(defaultTaskIcon);
    else setIcon(upperTaskIcon);
    setForeground(getForeground());
    setText(null);

    return this;
  }
}

class TaskTreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
  private JTree tree;
  private JTable table;
  public TaskTreeTableCellEditor(JTree tree, JTable table) {
    this.tree = tree;
    this.table = table;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int row, int column) {
    return tree;
  }

  public boolean isCellEditable(EventObject e) {
    if(e instanceof MouseEvent) {
      for(int counter = 0; counter < table.getColumnCount(); counter++) {
        if(table.getColumnClass(counter) == TreeTableModel.class) {
          dispatchMouseEvent((MouseEvent) e, counter);
          counter = table.getColumnCount();
        }
      }
    }
    return false;
  }

  private void dispatchMouseEvent(MouseEvent me, int column) {
    int transX = me.getX()-table.getCellRect(0, column, true).x;
    MouseEvent newMouseEvent = new MouseEvent(tree, me.getID(), me.getWhen(),
        me.getModifiers(), transX, me.getY(), me.getClickCount(), me.isPopupTrigger());
    tree.dispatchEvent(newMouseEvent);
    table.revalidate();
  }

  public Object getCellEditorValue() {
    return null;
  }
}

class TaskCellEditor extends AbstractCellEditor implements TableCellEditor {
  private static final String PROPERTY_DATE = "date";
  private Object editorValue;
  private JComboBox<TaskProperty.Priority> comboPriority;
  private JComboBox<TaskProperty.Attribution> comboAttribution;
  private JDateChooser dateChooser;

  TaskCellEditor() {
    initComboBoxes();
    dateChooser = new JDateChooser(new Date());
    dateChooser.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROPERTY_DATE) && dateChooser.getDate() != null) {
          if(!evt.getOldValue().equals(evt.getNewValue())) {
            editorValue = dateChooser.getDate().getTime();
            stopCellEditing();
          }
        }
      }
    });
    comboAttribution.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        editorValue = comboAttribution.getSelectedItem();
        stopCellEditing();
      }
    });
    comboPriority.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        editorValue = comboPriority.getSelectedItem();
        stopCellEditing();
      }
    });
  }

  private void initComboBoxes() {
    comboAttribution = new JComboBox<TaskProperty.Attribution>();
    for (Attribution attribution : Attribution.values())
      comboAttribution.insertItemAt(attribution, comboAttribution.getItemCount());

    comboPriority = new JComboBox<TaskProperty.Priority>();
    for (Priority priority : Priority.values())
      comboPriority.insertItemAt(priority, comboPriority.getItemCount());

    comboAttribution.setRenderer(new AttributionPriorityRenderer());
    comboPriority.setRenderer(new AttributionPriorityRenderer());
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected, int row, int column) {
    if(value instanceof Priority) {
      comboPriority.setSelectedItem(value);
      return comboPriority;
    } else if (value instanceof Attribution) {
      comboAttribution.setSelectedItem(value);
      return comboAttribution;
    } else if (value instanceof Long) {
      if(!dateChooser.getLocale().equals(Locale.getDefault()))
        dateChooser.setLocale(Locale.getDefault());
      dateChooser.setDate(new Date((Long) value));
      return dateChooser;
    } else return null;
  }

  public boolean stopCellEditing() {
    return super.stopCellEditing() && editorValue != null;
  }

  public Object getCellEditorValue() {
    return editorValue;
  }
}