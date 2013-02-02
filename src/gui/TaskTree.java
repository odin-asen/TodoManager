package gui;

import business.MutableTaskNode;
import business.Task;
import business.TaskTreeModel;
import dto.DTOTask;
import dto.TaskProperty;
import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static i18n.BundleStrings.COMPONENTS;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 22:43
 */
public class TaskTree extends JPanel {
  public static final Color LIST_BACKGROUND = new Color(242, 241, 240);

  private static final String AC_COLLAPSE_ALL = "collapse"; //NON-NLS
  private static final String AC_EXPAND_ALL = "expand"; //NON-NLS

  private JTree tree;
  private TaskTreeModel model;

  private JToolBar toolBar;

  private Boolean listChanged;

  /* Constructors */

  public TaskTree() {
    super(new BorderLayout());

    listChanged = false;

    /* create toolbar area at top */
    add(createToolBarArea(), BorderLayout.PAGE_START);

    /* create the selector panel with scroll pane */
    final JScrollPane scrollPane = new JScrollPane(getTreeComponent());
    add(scrollPane, BorderLayout.CENTER);
  }


  /* Methods */


  public void addTreeMouseListener(MouseListener listener) {
    tree.addMouseListener(listener);
  }

  public void addTreeSelectionListener(TreeSelectionListener listener) {
    tree.addTreeSelectionListener(listener);
  }

  /* create the component that contains all category trees */
  private JTree getTreeComponent() {
    if(tree != null)
      return tree;

    tree = new JTree();
    model = new TaskTreeModel(MutableTaskNode.getRootInstance());
    tree.setModel(model);
    tree.setBackground(LIST_BACKGROUND);
    tree.setCellRenderer(new TaskTreeCellRenderer());
    tree.setBackground(LIST_BACKGROUND);
    tree.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
    tree.setBorder(new LineBorder(Color.BLACK));
    tree.addTreeExpansionListener(new TreeExpansionListener() {
      public void treeExpanded(TreeExpansionEvent event) {
        final JTree tree = (JTree) event.getSource();
        tree.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
      }

      public void treeCollapsed(TreeExpansionEvent event) {
        final JTree tree = (JTree) event.getSource();
        tree.setMaximumSize(new Dimension(Integer.MAX_VALUE, 0));
      }
    });
    return tree;
  }

  private JPanel createToolBarArea() {
    JPanel toolbarAreaPanel = new JPanel(new BorderLayout());

    toolBar = new JToolBar();
    toolBar.setLayout(new FlowLayout(FlowLayout.LEADING));
    toolBar.setFloatable(false);
    final ActionListener expandCollapseAL = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(AC_COLLAPSE_ALL.equals(e.getActionCommand())) {
          expandCollapseTrees(false);
        } else if(AC_EXPAND_ALL.equals(e.getActionCommand())) {
          expandCollapseTrees(true);
        }
      }
    };
    toolBar.add(GUIUtilities.createButton(
        ResourceGetter.getImage(ResourceList.IMAGE_ARROWS_OUT, "exp"), //NON-NLS
        AC_EXPAND_ALL, expandCollapseAL));
    toolBar.add(GUIUtilities.createButton(
        ResourceGetter.getImage(ResourceList.IMAGE_ARROWS_IN, "col"), //NON-NLS
        AC_COLLAPSE_ALL, expandCollapseAL));

    toolbarAreaPanel.add(toolBar, BorderLayout.PAGE_START);

    return toolbarAreaPanel;
  }

  public void resetI18n() {
    JButton button = (JButton) toolBar.getComponent(0);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.expand.all"));
    button = (JButton) toolBar.getComponent(1);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.collapse.all"));
  }

  /* add tasks to the current list */
  public void addTasks(List<Task> tasks) {
    //TODO
  }

  /* add tasks to the current list */
  public MutableTaskNode addTask(boolean expand) {
    final Task task = new Task();
    final TreePath path = tree.getSelectionPath();
    final MutableTaskNode parentNode = path != null ?
        (MutableTaskNode) path.getLastPathComponent() : (MutableTaskNode) model.getRoot();
    model.add(path, new MutableTaskNode(task));
    listChanged = true;

    if(path == null)
      tree.setSelectionPath(tree.getPathForRow(0));
    else if(expand)
      tree.expandPath(path);
    tree.requestFocus();

    return parentNode;
  }

  /**
   * Removes all selected task and their sub tasks from the list.
   */
  public int removeSelectedTasks() {
    int removed = 0;
    final TreePath[] paths = tree.getSelectionPaths();
    if (paths != null) {
      for (TreePath path : paths)
        removed = removed + model.remove(path);
      listChanged = paths.length > 0;
    }
    return removed;
  }

  public void removeAllTasks() {
    model.remove(null);
  }

  public void deselectAllTasks() {
    tree.setSelectionPath(null);
  }

  public void expandCollapseTrees(boolean expand) {
    if(expand) tree.expandRow(tree.getRowCount());
    else tree.collapseRow(tree.getRowCount());
  }

  public List<MutableTaskNode> getSelectedNodes() {
    final List<MutableTaskNode> list = new ArrayList<MutableTaskNode>();
    final TreePath[] paths = tree.getSelectionPaths();
    if (paths != null) {
      for (TreePath path : paths) {
        list.add((MutableTaskNode) path.getLastPathComponent());
      }
    }
    return list;
  }

  public void changeSelectedNodes(DTOTask dtoTask) {
    final TreePath[] paths = tree.getSelectionPaths();
    if (paths != null) {
      for (TreePath path : paths) {
        changeSubTasks((MutableTaskNode) path.getLastPathComponent(), dtoTask);
      }
    }
  }

  private void changeSubTasks(MutableTaskNode node, DTOTask dtoTask) {
    final Enumeration<MutableTaskNode> children = node.children();
    while (children.hasMoreElements()) {
      changeSubTasks(children.nextElement(), dtoTask);
    }
    node.getTask().change(dtoTask);
  }

  public MutableTaskNode getTaskRoot() {
    return (MutableTaskNode) model.getRoot();
  }

  public void setRoot(MutableTaskNode root) {
    model = new TaskTreeModel(root);
    tree.setModel(model);
  }

  /* Getter and Setter */

  public Boolean hasListChanged() {
    return listChanged;
  }

  public void setListChanged(Boolean listChanged) {
    this.listChanged = listChanged != null ? listChanged : Boolean.valueOf(false);
  }

  /* Inner classes */
}

class TaskTreeCellRenderer extends DefaultTreeCellRenderer {
  public TaskTreeCellRenderer() {
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

    if(selected) {
      setBorder(BorderFactory.createDashedBorder(getForeground()));
      if(!hasFocus) {
        foreground = getTextNonSelectionColor();
        setBackground(getBackgroundNonSelectionColor());
        setOpaque(true);
      } else setOpaque(false);
    } else setOpaque(false);

    setForeground(foreground);
    setText(text);

    return this;
  }

  private Color getPriorityForeground(TaskProperty.Priority priority) {
    final Color colour;
    if(priority.equals(TaskProperty.Priority.LOWEST)) {
      colour = new Color(13, 246,0);
    } else if(priority.equals(TaskProperty.Priority.LOW)) {
      colour = new Color(0, 246, 237);
    } else if(priority.equals(TaskProperty.Priority.HIGH)) {
      colour = new Color(246, 136,0);
    } else if(priority.equals(TaskProperty.Priority.HIGHEST)) {
      colour = new Color(246,0,0);
    } else {
      colour = getForeground();
    }

    return colour;
  }
}