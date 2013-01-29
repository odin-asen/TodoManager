package gui;

import business.Task;
import business.TaskCategory;
import data.LoggingUtility;
import dto.TaskProperty;
import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 22:43
 */
public class TaskTreePanel extends JPanel {
  /* constants */
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  public static final Color LIST_BACKGROUND = new Color(242, 241, 240);

  private static final String SORT_ATTRIBUTION = I18nSupport.getValue(BUNDLE_GUI, "order.by.attribution");
  private static final String SORT_CATEGORY = I18nSupport.getValue(BUNDLE_GUI, "order.by.category");
  private static final String SORT_PRIORITY = I18nSupport.getValue(BUNDLE_GUI, "order.by.priority");

  /* Action commands */
  private static final String AC_COLLAPSE_ALL = "collapse"; //NON-NLS
  private static final String AC_EXPAND_ALL = "expand"; //NON-NLS
  private static final String NODE_CATEGORY_CATEGORY = "getCategory"; //NON-NLS
  private static final String NODE_CATEGORY_ATTR = "getAttribution"; //NON-NLS
  private static final String NODE_CATEGORY_PRIO = "getPriority"; //NON-NLS

  /* private fields */
  private SortedMap<String,JTree> treeHolder;
  private List<Task> taskList;
  private String nodeCategory;

  private JPanel categoryListPanel;
  private JToolBar toolBar;

  private Boolean listChanged;

  /* Constructors */

  public TaskTreePanel() {
    super(new BorderLayout());

    nodeCategory = NODE_CATEGORY_CATEGORY;
    /* initialise non-JComponent fields */
    initNewList();

    /* create toolbar area at top */
    add(createToolBarArea(), BorderLayout.PAGE_START);

    /* create the selector panel with scroll pane */
    final JScrollPane scrollPane = new JScrollPane(getCategoryListPanel());
    add(scrollPane, BorderLayout.CENTER);
  }


  /* Methods */

  private void initNewList() {
    treeHolder = new SortedMap<String,JTree>();
    taskList = new ArrayList<Task>();
    listChanged = false;
    removeCategoryTrees();
  }

  private void removeCategoryTrees() {
    JPanel panel = getCategoryListPanel();
    for (Component component : panel.getComponents()) {
      if(component instanceof JTree)
        panel.remove(component);
    }
  }

  /* create the panel that contains all category trees */
  private JPanel getCategoryListPanel() {
    if(categoryListPanel != null)
      return categoryListPanel;

    categoryListPanel = new JPanel();
    categoryListPanel.setLayout(new BoxLayout(categoryListPanel, BoxLayout.PAGE_AXIS));
    categoryListPanel.add(Box.createGlue());
    categoryListPanel.setBackground(LIST_BACKGROUND);

    return categoryListPanel;
  }

  /* Returns the tree for the main category or creates a new if it does not exist */
  private JTree getMainCategoryTree(String categoryRoot) {
    JTree tree = findCategoryTree(categoryRoot);
    if (tree != null)
      return tree;

    /* Add a new category tree */
    tree = new JTree(new DefaultMutableTreeNode(categoryRoot));
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        final TreePath path = e.getPath();
        final Object object = path.getLastPathComponent();
        if(e.isAddedPath()) {
          if(object instanceof TaskTreeNode) {
            final TaskTreeNode node = (TaskTreeNode) object;
            TodoFrame.getTodoFrame().getStatusBar().showTaskInformation(node.getTask());
          } else {
            TodoFrame.getTodoFrame().getStatusBar().setText("");
            TodoFrame.getTodoFrame().getStatusBar().showTaskInformation(null);
          }
        }
      }
    });
    tree.addMouseListener(new TreeMouseListener(tree));
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

    final int index = treeHolder.add(categoryRoot, tree);
    categoryListPanel.add(tree, index);

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

//    toolbarAreaPanel.add(createViewChangerPanel(), BorderLayout.CENTER);
    toolbarAreaPanel.add(toolBar, BorderLayout.PAGE_START);

    return toolbarAreaPanel;
  }

  private JPanel createViewChangerPanel() {
    final JPanel viewPanel = new JPanel();
    final JLabel viewLabel = new JLabel(I18nSupport.getValue(BUNDLE_GUI,"label.text.view"));
    JComboBox<String> viewComboBox = new JComboBox<String>();

    viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.X_AXIS));
    viewPanel.add(viewLabel);
    viewPanel.add(Box.createHorizontalStrut(6));
    viewPanel.add(viewComboBox);
    viewComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED)
          changeSortCriteria(e.getItem());
      }
    });
    viewComboBox.addItem(SORT_ATTRIBUTION);
    viewComboBox.addItem(SORT_CATEGORY);
    viewComboBox.addItem(SORT_PRIORITY);
    viewComboBox.setSelectedIndex(1);

    return viewPanel;
  }

  private void changeSortCriteria(Object criteria) {
    if(criteria.equals(SORT_ATTRIBUTION)) {
      nodeCategory = NODE_CATEGORY_ATTR;
      refreshList();
    } else if(criteria.equals(SORT_CATEGORY)) {
      nodeCategory = NODE_CATEGORY_CATEGORY;
      refreshList();
    } else if(criteria.equals(SORT_PRIORITY)) {
      nodeCategory = NODE_CATEGORY_PRIO;
      refreshList();
    }
  }

  private void refreshList() {
    final List<Task> currentTasks = new ArrayList<Task>(taskList.size());
    for (Task task : taskList)
      currentTasks.add(task);
    setTasks(currentTasks);
  }

  public void resetI18n() {
    JButton button = (JButton) toolBar.getComponent(0);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.expand.all"));
    button = (JButton) toolBar.getComponent(1);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.collapse.all"));
  }

  private JTree findCategoryTree(String category) {
    return treeHolder.get(category);
  }

  /* initialise the list and fill with a new task list */
  public void setTasks(List<Task> tasks) {
    if(tasks != null) {
      initNewList();
      for (Task task : tasks)
        addTask(task);
    }
  }

  /* add tasks to the current list */
  public void addTasks(List<Task> tasks) {
    if(tasks != null) {
      for (Task task : tasks) {
        addTask(task);
      }
    }
  }

  public void addTask(Task task) {
    addTask(task, false);
  }

  /* add tasks to the current list */
  public void addTask(Task task, boolean expand) {
    /* get the main category tree for this task */
    final String rootCategory = ObjectCategoriser.getCategory(task, nodeCategory);
    final JTree tree = getMainCategoryTree(rootCategory);
    final DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());

    /* add sub category nodes */
    final String[] categories = TaskCategory.getCategories(task.getCategory());
    final DefaultMutableTreeNode parentNode = insertSubCategories(tree, categories);

    /* insert the task node */
    final DefaultMutableTreeNode node = new TaskTreeNode(task);
    model.insertNodeInto(node, parentNode, getSortedIndex(parentNode, node));
    taskList.add(task);
    listChanged = true;
    if(expand)
      expandTree(tree, node.getLevel());
  }

  /**
   * Adds new category nodes if they don't exist.
   * @param categories Categories to be inserted
   * @return Returns the last category node that was added or found.
   */
  private DefaultMutableTreeNode insertSubCategories(JTree tree, String[] categories) {
    final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) model.getRoot();
    DefaultMutableTreeNode node;
    for (int level = 1; level < categories.length; level++) {
      /* get the next sub category node and add a new if it does not exist */
      final String category = categories[level];
      node = getSubCategory(parentNode, category, level);
      if(node == null) {
        node = new DefaultMutableTreeNode(category);
        node.setAllowsChildren(true);
        model.insertNodeInto(node, parentNode, getSortedIndex(parentNode, node));
      }
      parentNode = node;
    }
    return parentNode;
  }

  /* Gets the index for the node in the order of the node.toString value */
  /* Categories are ordered before tasks */
  private int getSortedIndex(MutableTreeNode parentNode,
                             MutableTreeNode node) {
    int index = 0;
    final boolean nodeIsTask = node instanceof TaskTreeNode;

    final Collator collator = Collator.getInstance();
    for (int i = 0; i < parentNode.getChildCount(); i++) {
      final TreeNode child = parentNode.getChildAt(i);
      final boolean childIsTask = child instanceof TaskTreeNode;
      if(!nodeIsTask && childIsTask)
        return index;
      if (!nodeIsTask || childIsTask) {
        if((collator.compare(child.toString(),node.toString()) >= 0))
          return index;
      }
      index++;
    }

    return index;
  }

  /* remove a task from the list */
  public void removeTask(Task task) {
    final String category = task.getCategory().getRoot().getName();
    final JTree tree = treeHolder.get(category);
    if(tree != null) {
      final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
      final TaskTreeNode node = (TaskTreeNode) findTaskNode(root, task);
      if(node != null) {
        ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
        taskList.remove(task);
        listChanged = true;
      }
    }
  }

  /* find a parents task node */
  private TreeNode findTaskNode(TreeNode parent, Task task) {
    for (int index = 0; index < parent.getChildCount(); index++) {
      final TreeNode node = parent.getChildAt(index);
      if (node instanceof TaskTreeNode) {
        if(task.equals(((TaskTreeNode) node).getTask()))
          return node;
      } else {
        if(!node.isLeaf()) {
          final TreeNode foundTask = findTaskNode(node, task);
          if(foundTask != null)
            return foundTask;
        }
      }
    }
    return null;
  }

  /**
   * Get the sub category node a specified top category.
   * @return Returns null if the node doesn't exists
   */
  private DefaultMutableTreeNode getSubCategory(DefaultMutableTreeNode topCategory,
                                                String subCategory, int level) {
    for (int index = 0; index < topCategory.getChildCount(); index++) {
      final DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) topCategory.getChildAt(index);
      if (node.getAllowsChildren() && subCategory.equals(node.toString())
          && node.getLevel() == level)
        return node;
    }
    return null;
  }

  /**
   * Removes one selected node specified by the path.
   * @return Returns the number of removed tasks.
   */
  private int removeSelectedNode(JTree tree, TreePath path) {
    final int removed;
    final DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());
    final DefaultMutableTreeNode node;
    if(path != null)
        node = (DefaultMutableTreeNode) path.getLastPathComponent();
    else node = (DefaultMutableTreeNode) tree.getModel().getRoot();

    /* remove task from list if node is a task */
    if(node instanceof TaskTreeNode) {
      final TaskTreeNode taskNode = (TaskTreeNode) node;
      taskList.remove(taskNode.getTask());
      removed=1;
    } else {
      removed = removeAllTasks(tree, node);
    }

    /* remove node if it is not root */
    if(node.getParent() != null)
      model.removeNodeFromParent(node);

    listChanged = true;
    return removed;
  }

  /**
   * Removes all tasks and sub tasks and sub categories from this node.
   * @param category Category whom tasks has to be deleted
   * @return Number of deleted tasks.
   */
  private int removeAllTasks(JTree tree, DefaultMutableTreeNode category) {
    int removed = 0;
    final DefaultTreeModel model = ((DefaultTreeModel) tree.getModel());
    while (category.getChildCount() > 0) {
      final MutableTreeNode node = (MutableTreeNode) category.getChildAt(0);
      if(node instanceof TaskTreeNode) {
        if(taskList.remove(((TaskTreeNode) node).getTask()))
          removed++;
      } else {
        if(!node.isLeaf()) {
          removed = removed + removeAllTasks(tree, (DefaultMutableTreeNode) node);
        }
      }
      model.removeNodeFromParent(node);
    }
    return removed;
  }

  /* removes a tree from the list */
  private void removeTree(JTree tree) {
    treeHolder.remove(tree.getModel().getRoot().toString());
    categoryListPanel.remove(tree);
    categoryListPanel.updateUI();
  }

  /**
   * Deletes the selected tasks and returns the number of them.
   * @return Number of deleted nodes.
   */
  public int removeSelectedNodes() {
    int deleted = 0;
    final List<JTree> emptyTrees = new ArrayList<JTree>();

    for (JTree tree : treeHolder.values()) {
      final TreePath[] paths = tree.getSelectionPaths();
      if (paths != null) {
        for (int index = 0; index < paths.length; index++) {
          final TreePath path = paths[index];
          if(path.getParentPath() != null) {
            deleted = deleted + removeSelectedNode(tree, path);
          } else {
            /* path is root path, so delete sub tasks */
            deleted = deleted + removeSelectedNode(tree, null);
            emptyTrees.add(tree);
            /* since the root may be selected first and all nodes
             * are deleted, the inner loop has to stop
             */
            index = paths.length;
          }
        }
      }
    }

    /* remove all empty trees */
    for (JTree tree : emptyTrees) {
      removeTree(tree);
    }

    return deleted;
  }

  public void deselectAllTasks() {
    for (JTree tree : treeHolder.values()) {
      tree.setSelectionPath(null);
    }
  }

  public void expandCollapseTrees(boolean expand) {
    for (JTree tree : treeHolder.values()) {
      if(expand) expandTree(tree);
      else collapseTree(tree);
    }
  }

  private void collapseTree(JTree tree) {
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.collapseRow(i);
    }
  }

  private void expandTree(JTree tree) {
    expandTree(tree, tree.getRowCount());
  }

  private void expandTree(JTree tree, int maxLevel) {
    for (int i = 0; i < tree.getRowCount() && i < maxLevel; i++) {
      tree.expandRow(i);
    }
  }

  public boolean selectedNodeExists() {
    for (JTree tree : treeHolder.values()) {
      final TreePath[] paths = tree.getSelectionPaths();
      if (paths != null) return true;
    }

    return false;
  }

  public void changeTask(Task unchangedTask, Task updatedTask) {
    removeTask(unchangedTask);
    addTask(updatedTask, true);
  }

  public List<Task> getSelectedTasks() {
    final List<Task> tasks = new ArrayList<Task>();
    for (JTree tree : treeHolder.values()) {
      final TreePath[] paths = tree.getSelectionPaths();
      if(paths != null) {
        for (TreePath path : paths) {
          final DefaultMutableTreeNode node =
              (DefaultMutableTreeNode) path.getLastPathComponent();
          if(node instanceof TaskTreeNode)
            tasks.add(((TaskTreeNode) node).getTask());
        }
      }
    }
    return tasks;
  }

  private void expandCollapseSelectedNode() {
    //TODO
  }

  /* Getter and Setter */

  public List<Task> getTaskList() {
    return taskList;
  }

  public Boolean hasListChanged() {
    return listChanged;
  }

  public void setListChanged(Boolean listChanged) {
    this.listChanged = listChanged != null ? listChanged : Boolean.valueOf(false);
  }

  /* Inner classes */

  private class SortedMap<K extends Comparable<? super K>,V> {
    private Map<K,V> map;
    private List<K> sortedList;

    private SortedMap() {
      map = new Hashtable<K,V>();
      sortedList = new ArrayList<K>();
    }

    /**
     * @return Returns the index where the value was sorted in.
     */
    public int add(K key, V value) {
      map.put(key, value);
      final int index = getKeyIndex(key);
      if(index < 0) {
        sortedList.add(-index - 1, key);
        return -index-1;
      }
      return index;
    }

    public V get(K key) {
      return map.get(key);
    }

    public void remove(K key) {
      map.remove(key);
      sortedList.remove(key);
    }

    public Collection<V> values() {
      return map.values();
    }

    /**
     * Calls {@link Collections#binarySearch} for the sorted list of this class.
     * @return Returns the index like described in {@link Collections#binarySearch}
     */
    private int getKeyIndex(K key) {
      return Collections.binarySearch(sortedList,key);
    }
  }
}

class TaskTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  private static final Border SELECTED_BORDER =
      BorderFactory.createDashedBorder(Color.WHITE);

  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, selected, expanded,
        leaf, row, hasFocus);
    if(value instanceof TaskTreeNode) {
      final TaskTreeNode node = (TaskTreeNode) value;
      final Task task = node.getTask();
      setText(task.getName());
      if(!selected)
        setForeground(getPriorityForeground(task.getPriority()));
    } else {
      if(value.toString().isEmpty()) {
        setText(I18nSupport.getValue(BUNDLE_GUI, "tree.node.no.category"));
      }
    }

    if(selected) setBorder(SELECTED_BORDER);

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
      colour = new Color(0,0,0);
    }

    return colour;
  }
}

class TaskTreeNode extends DefaultMutableTreeNode {
  TaskTreeNode(Task task) {
    super();
    setUserObject(task);
  }

  public boolean getAllowsChildren() {
    return false;
  }

  public boolean isRoot() {
    return false;
  }

  public boolean isLeaf() {
    return true;
  }

  public void setUserObject(Object task) {
    if(task instanceof Task)
      super.setUserObject(task);
  }

  public Object getUserObject() {
    return super.getUserObject() instanceof Task ? super.getUserObject() : null;
  }

  public Task getTask() {
    return (Task) getUserObject();
  }
}

class TreeMouseListener extends MouseAdapter {
  private JTree tree;
  public TreeMouseListener(JTree tree) {
    this.tree = tree;
  }

  public void mouseClicked(MouseEvent e) {
    if(e.getClickCount() == 2) {
      final TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
      if(path != null) {
        final Object object = path.getLastPathComponent();
        tree.setSelectionPath(path);
        if(object instanceof TaskTreeNode) {
          TodoFrame.getTodoFrame().openTask(((TaskTreeNode) object).getTask(), false);
        }
      }
    }
  }
}

class ObjectCategoriser {
  private static final Logger LOGGER = LoggingUtility.getLogger(ObjectCategoriser.class.getName());

  public static String getCategory(Object object, String getterMethodName) {
    try {
      final Object o = object.getClass().getMethod(getterMethodName).invoke(object);
      return o != null ? o.toString() : null;
    } catch (NoSuchMethodException e) {
      LOGGER.warning("Method " + getterMethodName + " could not be found!");
    } catch (InvocationTargetException e) {
      LOGGER.warning("Method invocation failed: " + e.getMessage());
    } catch (IllegalAccessException e) {
      LOGGER.warning("No access to method: "+e.getMessage());
    }
    return null;
  }
}