package business;

import i18n.BundleStrings;
import i18n.I18nSupport;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 29.01.13
 * Time: 21:53
 * <p/>
 * Represents a task node. A MutableTaskNode object has as user object that is of the class
 * {@link business.Task}. Additionally to the task properties a node can represent an upper
 * task or a simple task. The {@link javax.swing.tree.TreeNode#getAllowsChildren()} method
 * returns only true for a node that has children that have children themselves. To get all
 * children the method {@link javax.swing.tree.TreeNode#children()} can be called.
 */
public class MutableTaskNode implements MutableTreeNode {
  private boolean isUpperTask;
  private Task task;
  private MutableTreeNode parent;
  private List<MutableTaskNode> children;
  
  public MutableTaskNode(Task task) {
    isUpperTask = false;
    children = new ArrayList<MutableTaskNode>();
    setUserObject(task);
  }

  private void checkIndex(int index, int compareTo) {
    if(!(index >= 0 && index < compareTo))
      throw new ArrayIndexOutOfBoundsException(
          "The parameter is not >= 0 and not < "+compareTo);
  }

  public TreeNode getChildAt(int childIndex) {
    checkIndex(childIndex, getChildCount());
    return children.get(childIndex);
  }

  public int getChildCount() {
    return children.size();
  }

  public TreeNode getParent() {
    return parent;
  }

  public int getIndex(TreeNode node) {
    if (node instanceof MutableTaskNode)
      return children.indexOf(node);
    else return -1;
  }

  public boolean getAllowsChildren() {
    return isUpperTask;
  }

  public boolean isLeaf() {
    return false;
  }

  /**
   * Returns all children of this node. Children that have children themselves are first in
   * the sequence. Children without children are last in the sequence.
   * @return An Enumeration with MutableTaskNode objects.
   */
  public Enumeration<MutableTaskNode> children() {
    final List<MutableTaskNode> nodes =
        new ArrayList<MutableTaskNode>(children.size());
    for (MutableTaskNode child : children)
      nodes.add(child);
    return Collections.enumeration(nodes);
  }

  public void insert(MutableTreeNode child, int index) {
    if(child instanceof MutableTaskNode) {
      addChild((MutableTaskNode) child, index);
    }
  }

  private void addChild(MutableTaskNode child, int index) {
    children.add(index, child);
    child.setParent(this);
    if(!isUpperTask)
      isUpperTask = true;
  }

  public void remove(int index) {
    children.remove(index);
    isUpperTask = !(getChildCount() == 0);
  }

  public void remove(MutableTreeNode node) {
    if(node instanceof MutableTaskNode) {
      children.remove(node);
      isUpperTask = !(getChildCount() == 0);
    }
  }

  public void setUserObject(Object object) {
    this.task = object instanceof Task ? (Task) object : null;
  }

  public void removeFromParent() {
    if(parent != null)
      parent.remove(this);
  }

  public void setParent(MutableTreeNode newParent) {
    parent = newParent;
  }

  public Task getTask() {
    return task;
  }

  public String toString() {
    return "MutableTaskNode{" +
        "task=" + task +
        ", parent=" + parent +
        ", children=" + children.size() +
        '}';
  }

  public static MutableTaskNode getRootInstance() {
    final Task task = new Task();
    task.setName(I18nSupport.getValue(BundleStrings.COMPONENTS, "text.my.list"));
    return new MutableTaskNode(task);
  }

  public int countAllSubNodes() {
    int sum = getChildCount();
    for (MutableTaskNode node : children) {
      sum = getTotalChildCount(node, node.getChildCount() + sum);
    }
    return sum;
  }

  private int getTotalChildCount(MutableTaskNode subNode, int init) {
    for (MutableTaskNode node : subNode.children) {
      init = getTotalChildCount(node, node.getChildCount() + init);
    }
    return init;
  }
}