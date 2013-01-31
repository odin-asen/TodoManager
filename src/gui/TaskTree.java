package gui;

import business.MutableTaskNode;
import business.Task;
import business.TaskTreeModel;
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
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 22:43
 */
public class TaskTree extends JPanel {
  /* constants */
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  public static final Color LIST_BACKGROUND = new Color(242, 241, 240);

  /* Action commands */
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
    final Task root = new Task();
    root.setName("Meine Tasks");
    model = new TaskTreeModel(new MutableTaskNode(root));
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
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.expand.all"));
    button = (JButton) toolBar.getComponent(1);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.collapse.all"));
  }

  /* initialise the list and fill with a new task list */
  public void setTasks(List<Task> tasks) {
    //TODO
  }

  /* add tasks to the current list */
  public void addTasks(List<Task> tasks) {
    //TODO
  }

  public void addTask(Task task) {
    addTask(task, false);
  }

  /* add tasks to the current list */
  public void addTask(Task task, boolean expand) {
    TreePath path = tree.getSelectionPath();

    model.add(path, new MutableTaskNode(task));
    listChanged = true;
    if(expand && path != null)
      tree.expandPath(path);
  }

  /* remove a task from the list */
  public void removeSelectionPaths() {
    final TreePath[] paths = tree.getSelectionPaths();
    for (TreePath path : paths) {
      model.remove(path);
    }
    listChanged = paths.length > 0;
  }

  public void removeAllPaths() {
    model.remove(null);
  }

  public void deselectAllTasks() {
    tree.setSelectionPath(null);
  }

  public void expandCollapseTrees(boolean expand) {
    if(expand) tree.expandRow(tree.getRowCount());
    else tree.collapseRow(tree.getRowCount());
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
  private static final String BUNDLE_GUI = "gui"; //NON-NLS

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
      final Task task = node.getUserObject();
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