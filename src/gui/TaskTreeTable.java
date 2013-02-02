package gui;

import business.MutableTaskNode;
import business.Task;
import business.TaskTreeTableModel;
import com.toedter.calendar.JDateChooser;
import dto.TaskProperty;
import gui.treeTable.AbstractTreeTableModel;
import gui.treeTable.TreeTableModel;
import gui.treeTable.TreeTableModelAdapter;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.EventObject;

import static dto.TaskProperty.Attribution;
import static dto.TaskProperty.Priority;

/**
 * User: Timm Herrmann
 * Date: 27.01.13
 * Time: 17:13
 */
public class TaskTreeTable extends JTable {
  private TaskTreeTableCellRenderer treeRenderer;
  private AbstractTreeTableModel model;
  private TreeTableSelectionModel selectionModel;

  /* Constructors */

  public TaskTreeTable() {
    /* create JTree */
    getTreeRenderer();
    super.setModel(new TreeTableModelAdapter(model, treeRenderer));

    /* Simultaneous selection for the tree and the table */
    selectionModel = new TreeTableSelectionModel();
    treeRenderer.setSelectionModel(selectionModel); //For the tree
    setSelectionModel(selectionModel.getListSelectionModel()); //For the table

    /* Set renderer and editors */
    setDefaultRenderer(TreeTableModel.class, treeRenderer);

    setDefaultEditor(TreeTableModel.class, new TaskTreeTableCellEditor(treeRenderer, this));
    final TaskCellEditor editor = new TaskCellEditor(treeRenderer);
    setDefaultEditor(Priority.class, editor);
    setDefaultEditor(Attribution.class, editor);
    setDefaultEditor(Long.class, editor);
  }

  /* Methods */

  public void showNode(MutableTaskNode node) {
//    currentNode = node;
//    model.clear();
//    if(node != null) {
//      final Enumeration<MutableTaskNode> children = node.children();
//      while(children.hasMoreElements())
//        model.add(children.nextElement().getTask());
//    }
  }

  public void updateTable() {
//    showNode(currentNode);
  }

  public void resetI18n() {
//    model.resetI18n();
  }

  public void addTableMouseListener(MouseListener listener) {
    addMouseListener(listener);
  }

  /* Getter and Setter */

  /* create the component that contains all category trees */
  private TaskTreeTableCellRenderer getTreeRenderer() {
    if(treeRenderer != null)
      return treeRenderer;

    model = new TaskTreeTableModel(MutableTaskNode.getRootInstance());
    treeRenderer = new TaskTreeTableCellRenderer(this, model);
    return treeRenderer;
  }

  public TreeTableSelectionModel getTreeTableSelectionModel() {
    return selectionModel;
  }

  /* Inner classes */

  class TreeTableSelectionModel extends DefaultTreeSelectionModel {
    private ListSelectionModel getListSelectionModel() {
      return listSelectionModel;
    }

    void addListSelectionListener(ListSelectionListener l) {
      listSelectionModel.addListSelectionListener(l);
    }

    void removeListSelectionListener(ListSelectionListener l) {
      listSelectionModel.removeListSelectionListener(l);
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
    setOpaque(false);
    /* Set the row height for the JTabSetzen der Zeilenhoehe fuer die JTable
    // Muss explizit aufgerufen werden, weil treeTable noch
    // null ist, wenn super(model) setRowHeight aufruft!
    */
    setRowHeight(getRowHeight());
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    if(isSelected)
      setBackground(table.getSelectionBackground());
    else setBackground(table.getBackground());
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

class TaskTreeCellRenderer extends DefaultTreeCellRenderer {
  public TaskTreeCellRenderer() {
    setOpaque(false);
  }

  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    Color foreground = getForeground();
    String text = getText();

    if(value instanceof MutableTaskNode) {
      final MutableTaskNode node = (MutableTaskNode) value;
      final Task task = node.getTask();
      if(task != null) {
        text = task.getName();
        if (!selected)
          foreground = getPriorityForeground(task.getPriority());
      }
    }

    setForeground(foreground);
    setText(text);

    return this;
  }

  private Color getPriorityForeground(Priority priority) {
    final Color colour;
    if(priority.equals(Priority.LOWEST)) {
      colour = new Color(13, 246,0);
    } else if(priority.equals(Priority.LOW)) {
      colour = new Color(0, 246, 237);
    } else if(priority.equals(Priority.HIGH)) {
      colour = new Color(246, 136,0);
    } else if(priority.equals(Priority.HIGHEST)) {
      colour = new Color(246,0,0);
    } else {
      colour = getForeground();
    }

    return colour;
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
      int column = 0;
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
  private Object editorValue;
  private JComboBox<TaskProperty.Priority> comboPriority;
  private JComboBox<TaskProperty.Attribution> comboAttribution;
  private JDateChooser dateChooser;
  private JTree tree;

  TaskCellEditor(JTree tree) {
    this.tree = tree;
    initComboBoxes();
    dateChooser = new JDateChooser(new Date());
    dateChooser.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if(dateChooser.getDate() != null) {
          editorValue = dateChooser.getDate().getTime();
        } else editorValue = null;
      }
    });
    comboAttribution.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editorValue = comboAttribution.getSelectedItem();
      }
    });
    comboPriority.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        editorValue = comboPriority.getSelectedItem();
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
      dateChooser.setDate(new Date((Long) value));
      return dateChooser;
    } else return null;
  }

  public Object getCellEditorValue() {
    return editorValue;
  }
}