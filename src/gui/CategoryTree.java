package gui;

import business.TaskCategory;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 29.01.13
 * Time: 04:05
 */
public class CategoryTree extends JComponent {
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  public static final Color LIST_BACKGROUND = new Color(242, 241, 240);

  /* private fields */

  private SortedMap<String,JTree> treeHolder;
  private List<TaskCategory> categoryList;

  private JPanel categoryListPanel;
  private Boolean listChanged;

  /* Constructors */

  public CategoryTree() {
    setLayout(new BorderLayout());

    /* initialise non-JComponent fields */
    initNewList();

    /* create the selector panel with scroll pane */
    final JScrollPane scrollPane = new JScrollPane(getCategoryListPanel());
    add(scrollPane, BorderLayout.CENTER);
    TaskCategory parent = new TaskCategory("parent");
    TaskCategory other = new TaskCategory("other");
    TaskCategory child = new TaskCategory("child", parent);
    addCategory(parent, true);
    addCategory(child, true);
    addCategory(other, true);
    addCategory(new TaskCategory("foreveralone"), true);
    addCategory(new TaskCategory("childchild", child), true);
    addCategory(new TaskCategory("child", other), true);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(new CategoryTree());
    frame.pack();
    frame.setVisible(true);
  }

  /* Methods */

  /* add tasks to the current list */
  public void addCategory(TaskCategory category, boolean expand) {
    /* get the main category tree for this task */
    final JTree tree = getMainCategoryTree(category);
    final TaskCategoryTreeModel model = (TaskCategoryTreeModel) tree.getModel();

    /* add sub category nodes */
//    final String[] categories = TaskCategory.getCategories(task.getCategory());
//    final DefaultMutableTreeNode parentNode = insertSubCategories(tree, categories);

    /* insert the task node */
    model.setNode(category);
    categoryList.add(category);
    listChanged = true;
//    if(expand)
//      expandTree(tree, node.getLevel());
  }

  private JTree getMainCategoryTree(TaskCategory category) {
    final String rootCategory = category.getRoot().getName();
    JTree tree = findCategoryTree(rootCategory);
    if (tree != null)
      return tree;

    /* Add a new category tree */
    tree = new JTree();
    tree.setModel(new TaskCategoryTreeModel());
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

    final int index = treeHolder.add(rootCategory, tree);
    categoryListPanel.add(tree, index);

    return tree;
  }

  private JTree findCategoryTree(String category) {
    return treeHolder.get(category);
  }

  private void initNewList() {
    treeHolder = new SortedMap<String,JTree>();
    categoryList = new ArrayList<TaskCategory>();
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

  /* Getter and Setter */

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
      return Collections.binarySearch(sortedList, key);
    }
  }
}

class CategoryCellRenderer extends DefaultTreeCellRenderer {
//  private static final String BUNDLE_GUI = "gui"; //NON-NLS
//  private static final Border SELECTED_BORDER =
//      BorderFactory.createDashedBorder(Color.WHITE);
//
//  public Component getTreeCellRendererComponent(
//      JTree tree, Object value, boolean selected, boolean expanded,
//      boolean leaf, int row, boolean hasFocus) {
//    super.getTreeCellRendererComponent(tree, value, selected, expanded,
//        leaf, row, hasFocus);
//    if(value instanceof TaskTreeNode) {
//      final TaskTreeNode node = (TaskTreeNode) value;
//      final Task task = node.getTask();
//      setText(task.getName());
//      if(!selected)
//        setForeground(getPriorityForeground(task.getPriority()));
//    } else {
//      if(value.toString().isEmpty()) {
//        setText(I18nSupport.getValue(BUNDLE_GUI, "tree.node.no.category"));
//      }
//    }
//
//    if(selected) setBorder(SELECTED_BORDER);
//
//    return this;
//  }
//
//  private Color getPriorityForeground(TaskProperty.Priority priority) {
//    final Color colour;
//    if(priority.equals(TaskProperty.Priority.LOWEST)) {
//      colour = new Color(13, 246,0);
//    } else if(priority.equals(TaskProperty.Priority.LOW)) {
//      colour = new Color(0, 246, 237);
//    } else if(priority.equals(TaskProperty.Priority.HIGH)) {
//      colour = new Color(246, 136,0);
//    } else if(priority.equals(TaskProperty.Priority.HIGHEST)) {
//      colour = new Color(246,0,0);
//    } else {
//      colour = new Color(0,0,0);
//    }
//
//    return colour;
//  }
}

class TaskCategoryTreeModel implements TreeModel {
  private static final String GUI_BUNDLE = "gui"; //NON-NLS

  private TaskCategory root;
  private List<TreeModelListener> listeners;

  public TaskCategoryTreeModel() {
    listeners = new ArrayList<TreeModelListener>();
    this.root = null;
  }

  private void checkIndex(int index, int compareTo) {
    if(!(index >= 0 && index < compareTo))
      throw new ArrayIndexOutOfBoundsException("The parameter is not >= 0 and not < "+compareTo);
  }

  private void notifyListeners(TreeModelEvent event) {
    for (TreeModelListener listener : listeners)
      listener.treeNodesChanged(event);
  }

  public Object getRoot() {
    return root;
  }

  public Object getChild(Object parent, int index) {
    if(parent instanceof TaskCategory) {
      checkIndex(index, getChildCount(parent));
      return ((TaskCategory) parent).getChild(index);
    }
    return null;
  }

  public int getChildCount(Object parent) {
    return parent instanceof TaskCategory ? ((TaskCategory) parent).getChildCount() : 0;
  }

  public boolean isLeaf(Object node) {
    return !(node instanceof TaskCategory) || ((TaskCategory) node).getChildCount() == 0;
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    if(!path.getLastPathComponent().equals(newValue))
      notifyListeners(new TreeModelEvent(newValue, path));
  }

  public int getIndexOfChild(Object parent, Object child) {
    if(parent instanceof TaskCategory && child instanceof TaskCategory)
      return ((TaskCategory) parent).getChildIndex((TaskCategory) child);
    return -1;
  }

  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  public void setNode(TaskCategory category) {
    if(category.getParent() != null) {
      root = category.getRoot();
      notifyListeners(new TreeModelEvent(root, (TreePath) null));
    }
    if(root != null)
      System.out.println(root.getName()+": "+root.getChildCount());
  }
}