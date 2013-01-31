package business;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 29.01.13
 * Time: 21:00
 */
public class TaskTreeModel implements TreeModel {
  private MutableTaskNode root;
  private List<TreeModelListener> listeners;

  public TaskTreeModel(MutableTaskNode root) {
    listeners = new ArrayList<TreeModelListener>();
    this.root = root;
  }

  private void checkIndex(int index, int compareTo) {
    if(!(index >= 0 && index < compareTo))
      throw new ArrayIndexOutOfBoundsException(
          "The parameter is not >= 0 and not < "+compareTo);
  }

  public Object getRoot() {
    return root;
  }

  public Object getChild(Object parent, int index) {
    if(parent instanceof MutableTaskNode) {
      checkIndex(index, getChildCount(parent));
      return ((MutableTaskNode) parent).getChildAt(index);
    }
    return null;
  }

  public int getChildCount(Object parent) {
    return isLeaf(parent) ? 0 : ((MutableTaskNode) parent).getChildCount();
  }

  public boolean isLeaf(Object node) {
    return !(node instanceof MutableTaskNode) || ((MutableTaskNode) node).getChildCount() == 0;
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    if(!path.getLastPathComponent().equals(newValue)) {
      final TreeModelEvent event = new TreeModelEvent(newValue, path);
      for (TreeModelListener listener : listeners)
        listener.treeNodesChanged(event);
    }
  }

  public int getIndexOfChild(Object parent, Object child) {
    if(parent instanceof MutableTaskNode && child instanceof MutableTaskNode)
      return ((MutableTaskNode) parent).getIndex((MutableTaskNode) child);
    return -1;
  }

  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  public void add(TreePath parent, MutableTaskNode child){
    if(parent == null)
      parent = new TreePath(root);
    final MutableTaskNode parentNode = (MutableTaskNode) parent.getLastPathComponent();
    int index = getChildCount(parentNode);
    parentNode.insert(child, index);
    if(parentNode.getAllowsChildren()) {
      final TreeModelEvent event = new TreeModelEvent(
          this, parent, new int[]{index}, new Object[]{child});
      for (TreeModelListener listener : listeners)
        listener.treeNodesInserted(event);
    } else {
      final TreeModelEvent event = new TreeModelEvent(this, parent);
      for (TreeModelListener listener : listeners)
        listener.treeNodesChanged(event);
    }
  }

  public void remove(TreePath path) {
    final MutableTaskNode node = (MutableTaskNode) path.getLastPathComponent();
    final MutableTaskNode parent = (MutableTaskNode) node.getParent();
    int index = getIndexOfChild(parent, node);
    parent.remove(index);
    final TreeModelEvent event = new TreeModelEvent(
        this, path.getParentPath(), new int[]{index-1}, new Object[]{node});
    for (TreeModelListener listener : listeners)
      listener.treeNodesRemoved(event);
  }
}

