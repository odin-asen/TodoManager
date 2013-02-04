package gui;

import business.MutableTaskNode;
import business.Task;
import business.TaskTreeTableModel;
import com.toedter.calendar.JDateChooser;
import dto.DTOTask;
import dto.TaskProperty;
import gui.treeTable.TreeTableModel;
import gui.treeTable.TreeTableModelAdapter;
import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.Locale;

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
    TreeTableSelectionModel selectionModel = new TreeTableSelectionModel();
    treeRenderer.setSelectionModel(selectionModel); //For the tree
    setSelectionModel(selectionModel.getListSelectionModel()); //For the table

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

  public void doLayout() {
    getColumn(treeTableModel.getColumnName(treeTableModel.getTreeColumn())).setWidth(
        treeRenderer.getPreferredSize().width);
    for (int index = 0; index < getColumnCount()-1; index++) {
      if(index != treeTableModel.getTreeColumn()) {
        final TableColumn column = getColumn(treeTableModel.getColumnName(index));
        final Component component = getCellRenderer(0, index).getTableCellRendererComponent(
            this, getValueAt(0, index), false, false, 0, index);
        final int componentWidth = component.getPreferredSize().width +5;
        final int columnWidth = column.getPreferredWidth();
        column.setWidth(componentWidth > columnWidth ? componentWidth : columnWidth);
      }
    }
    final TableColumn lastColumn = getColumn(getColumnName(getColumnCount()-1));
    int remainingSpace = getWidth() -
        (getColumnModel().getTotalColumnWidth() - lastColumn.getWidth());
    lastColumn.setWidth(remainingSpace);
  }

  public void resetI18n() {
    treeTableModel.resetI18n();
    tableCellRenderer.resetI18n();
    treeTableModelAdapter.fireTableCellUpdated(TableModelEvent.HEADER_ROW,
        TableModelEvent.ALL_COLUMNS);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void changeSelectedTasks(DTOTask dtoTask) {
    final TreePath[] paths = treeRenderer.getSelectionPaths();
    if (paths != null) {
      for (TreePath path : paths) {
        ((MutableTaskNode) path.getLastPathComponent()).getTask().change(dtoTask);
      }
    }
  }

  public void deselectAllTasks() {
    treeRenderer.setSelectionPath(null);
  }

  @SuppressWarnings("UnusedDeclaration")
  public Task getTask(int index) {
    final TreePath path = treeRenderer.getPathForRow(index);
    if(path != null)
      return ((MutableTaskNode) path.getLastPathComponent()).getTask();
    else return null;
  }

  public void addTask(Task task) {
    task.setName("test");
    final int selectedRow = getSelectedRow();
    if(selectedRow >= 0) {
      final TreePath path = treeRenderer.getPathForRow(selectedRow);
      treeTableModel.add(path, new MutableTaskNode(task));
      treeRenderer.setSelectionRow(selectedRow);
      listChanged = true;
      updateUI();
    }
  }

  /**
   * Removes all selected task and their sub tasks from the list.
   */
  public int removeSelectedTasks() {
    int removed = 0;
    final int[] selectionRows = getSelectedRows();
    if (selectionRows != null) {
      for (int row : selectionRows) {
        final TreePath path = treeRenderer.getPathForRow(row);
        removed = removed + treeTableModel.remove(path);
      }
      listChanged = removed > 0;
      updateUI();
    }
    return removed;
  }

  public MutableTaskNode getTaskRoot() {
    return (MutableTaskNode) treeRenderer.getPathForRow(0).getLastPathComponent();
  }

  public void setRoot(MutableTaskNode root) {
    treeTableModel = new TaskTreeTableModel(root);
    if(!treeTableModel.equals(treeRenderer.getModel()))
      treeRenderer.setModel(treeTableModel);
    treeTableModelAdapter = new TreeTableModelAdapter(treeTableModel, treeRenderer);
    super.setModel(treeTableModelAdapter);
  }

  /* Getter and Setter */

  /**
   * Create the component that contains all category trees
   */
  private TaskTreeTableCellRenderer getTreeRenderer() {
    if(treeRenderer != null)
      return treeRenderer;

    treeRenderer = new TaskTreeTableCellRenderer(this, treeTableModel);
    return treeRenderer;
  }

  public boolean hasListChanged() {
    return listChanged;
  }

  public void setListChanged(boolean changed) {
    listChanged = changed;
  }

  public boolean hasSelectedTasks() {
    return treeRenderer.getSelectionCount() > 0;
  }

  /* Inner classes */

  class TreeTableSelectionModel extends DefaultTreeSelectionModel {
    private ListSelectionModel getListSelectionModel() {
      return listSelectionModel;
    }

    @SuppressWarnings("UnusedDeclaration")
    void addListSelectionListener(ListSelectionListener l) {
      listSelectionModel.addListSelectionListener(l);
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
    if (e instanceof MouseEvent) {
      int column = table.getSelectedColumn();
      MouseEvent me = (MouseEvent) e;
      MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(),
          me.getModifiers(), me.getX() - table.getCellRect(0, column, true).x,
          me.getY(), 2, me.isPopupTrigger());
      tree.dispatchEvent(newME);
    }
    return false;
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