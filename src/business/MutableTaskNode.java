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
  private TwoHalfList<MutableTaskNode> children;
  
  public MutableTaskNode(Task task) {
    isUpperTask = false;
    children = new TwoHalfList<MutableTaskNode>();
    setUserObject(task);
  }

  private void checkIndex(int index, int compareTo) {
    if(!(index >= 0 && index < compareTo))
      throw new ArrayIndexOutOfBoundsException(
          "The parameter is not >= 0 and not < "+compareTo);
  }

  public TreeNode getChildAt(int childIndex) {
    checkIndex(childIndex, getChildCount());
    return children.getFirstElement(childIndex);
  }

  public int getChildCount() {
    return children.getFirstSize();
  }

  public TreeNode getParent() {
    return parent;
  }

  public int getIndex(TreeNode node) {
    if (node instanceof MutableTaskNode)
      return children.getFirstIndex((MutableTaskNode) node);
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
    return Collections.enumeration(children.getList());
  }

  public void insert(MutableTreeNode child, int index) {
    if(child instanceof MutableTaskNode) {
      addChild((MutableTaskNode) child, index);
      if(!isUpperTask)
        isUpperTask = true;
    }
  }

  private void addChild(MutableTaskNode child, int index) {
    if(child.getChildCount() == 0)
      children.addToSecond(children.getSecondSize(), child);
    else {
      if(index > children.getFirstSize())
        index = children.getFirstSize();
      children.addToFirst(index, child);
    }
    child.setParent(this);
  }

  public void remove(int index) {
    children.removeFromFirst(index);
    isUpperTask = !(getChildCount() == 0);
  }

  public void remove(MutableTreeNode node) {
    if(node instanceof MutableTaskNode) {
      children.removeFromFirst((MutableTaskNode) node);
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
        ", children=" + children.getSize() +
        '}';
  }

  public int getTotalChildCount() {
    return children.getSize();
  }

  public static MutableTaskNode getRootInstance() {
    final Task task = new Task();
    task.setName(I18nSupport.getValue(BundleStrings.COMPONENTS, "text.my.list"));
    return new MutableTaskNode(task);
  }

  public int countAllSubNodes() {
    int sum = getTotalChildCount();
    for (MutableTaskNode node : children.getList()) {
      sum = getTotalChildCount(node, node.getTotalChildCount() + sum);
    }
    return sum;
  }

  private int getTotalChildCount(MutableTaskNode subNode, int init) {
    for (MutableTaskNode node : subNode.children.getList()) {
      init = getTotalChildCount(node, node.getTotalChildCount() + init);
    }
    return init;
  }
}

class TwoHalfList<T> {
  private static final String INDEX_ASSERT_MESSAGE = "Index is out of range";
  private static final String INVARIANT_ASSERT_MESSAGE = "Invariant not satisfied: firstSize + secondSize != list.size()";
  private String assertMessage;
  
  private List<T> list;

  private int firstSize;
  private int secondSize;

  public TwoHalfList() {
    list = new ArrayList<T>();
    firstSize = 0;
    secondSize = 0;
    assert invariant() : getAssertMessage();
  }

  private boolean isIndexValid(int index, int limit) {
    if(0 <= index && index < limit)
      return true;
    else {
      setAssertMessage(INDEX_ASSERT_MESSAGE);
      return false;
    }
  }

  private boolean invariant() {
    if(firstSize+secondSize == list.size())
      return true;
    else {
      setAssertMessage(INVARIANT_ASSERT_MESSAGE);
      return false;
    }
  }

  private void setAssertMessage(String message) {
    assertMessage = message;
  }
  
  private String getAssertMessage() {
    return assertMessage;
  }
  
  public void addToFirst(int index, T element) {
    assert invariant() && isIndexValid(index, firstSize+1) : getAssertMessage();
    list.add(index, element);
    firstSize++;
    assert invariant() : getAssertMessage();
  }
  
  public void addToSecond(int index, T element) {
    assert invariant() && isIndexValid(index, secondSize+1) : getAssertMessage();
    list.add(firstSize+index, element);
    secondSize++;
    assert invariant() : getAssertMessage();
  }
  
  public void removeFromFirst(int index) {
    assert invariant() && isIndexValid(index, firstSize) : getAssertMessage();
    if(list.remove(index) != null)
      firstSize--;
    assert invariant() : getAssertMessage();
  }
  
  public boolean removeFromFirst(T element) {
    assert invariant() : getAssertMessage();

    if(isIndexValid(list.indexOf(element), firstSize)) {
      if(list.remove(element)) {
        firstSize--;
        return true;
      }
    }
    assert invariant() : getAssertMessage();
    return false;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void removeFromSecond(int index) {
    assert invariant() && isIndexValid(index, secondSize) : getAssertMessage();
    if(list.remove(index+firstSize) != null)
      secondSize--;
    assert invariant() : getAssertMessage();
  }

  @SuppressWarnings("UnusedDeclaration")
  public boolean removeFromSecond(T element) {
    assert invariant() : getAssertMessage();
    boolean removed = false;

    if(isIndexValid(list.indexOf(element)-secondSize, secondSize)) {
      if(list.remove(element)) {
        secondSize--;
        removed = true;
      }
    }

    assert invariant() : getAssertMessage();
    return removed;
  }

  public T getFirstElement(int index) {
    assert invariant() && isIndexValid(index, firstSize);
    return list.get(index);
  }

  @SuppressWarnings("UnusedDeclaration")
  public T getSecondElement(int index) {
    assert invariant() && isIndexValid(index, secondSize);
    return list.get(index+firstSize);
  }

  public int getSize() {
    assert invariant() : getAssertMessage();
    return firstSize+secondSize;
  }

  public int getFirstSize() {
    assert invariant() : getAssertMessage();
    return firstSize;
  }

  @SuppressWarnings("UnusedDeclaration")
  public int getSecondSize() {
    assert invariant() : getAssertMessage();
    return secondSize;
  }

  public int getFirstIndex(T element) {
    assert invariant() : getAssertMessage();
    int index = list.indexOf(element);
    return isIndexValid(index, firstSize) ? index : -1;
  }

  @SuppressWarnings("UnusedDeclaration")
  public int getSecondIndex(T element) {
    assert invariant() : getAssertMessage();
    int index = list.indexOf(element);
    return isIndexValid(index-secondSize, secondSize) ? index : -1;
  }

  public List<T> getList() {
    assert invariant() : getAssertMessage();
    return list;
  }
}
