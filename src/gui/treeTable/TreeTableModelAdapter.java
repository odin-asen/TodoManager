package gui.treeTable;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * User: Timm Herrmann
 * Date: 02.02.13
 * Time: 17:40
 */
public class TreeTableModelAdapter extends AbstractTableModel {
  JTree tree;
  AbstractTreeTableModel treeTableModel;

  public TreeTableModelAdapter(AbstractTreeTableModel treeTableModel, JTree tree) {
    this.tree = tree;
    this.treeTableModel = treeTableModel;

    tree.addTreeExpansionListener(new TreeExpansionListener() {
      public void treeExpanded(TreeExpansionEvent event) {
        fireTableDataChanged();
      }

      public void treeCollapsed(TreeExpansionEvent event) {
        fireTableDataChanged();
      }
    });
  }

  public int getColumnCount() {
    return treeTableModel.getColumnCount();
  }

  public String getColumnName(int column) {
    return treeTableModel.getColumnName(column);
  }

  public Class<?> getColumnClass(int column) {
    return treeTableModel.getColumnClass(column);
  }

  public int getRowCount() {
    return tree.getRowCount();
  }

  protected Object nodeForRow(int row) {
    TreePath treePath = tree.getPathForRow(row);
    return treePath.getLastPathComponent();
  }

  public Object getValueAt(int row, int column) {
    return treeTableModel.getValueAt(nodeForRow(row), column);
  }

  public boolean isCellEditable(int row, int column) {
    return treeTableModel.isCellEditable(nodeForRow(row), column);
  }

  public void setValueAt(Object value, int row, int column) {
    treeTableModel.setValueAt(value, nodeForRow(row), column);
  }
}