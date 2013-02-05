package dto;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 05.02.13
 * Time: 18:46
 */
public class Utility {
  private static final class PathLengthComparator implements Comparator<TreePath> {
    private boolean shortToLong;
    PathLengthComparator(boolean shortToLong) {
      this.shortToLong = shortToLong;
    }
    public int compare(TreePath path1, TreePath path2) {
      if(path1 == null && path2 == null) return 0;
      else if(path1 == null) return shortToLong ? -1 : 1;
      else if(path2 == null) return shortToLong ? 1 : -1;

      int result = path1.getPathCount() - path2.getPathCount();
      return shortToLong ? result : -result;
    }
  }

  public static List<TreePath> getLengthSortedList(boolean shortToLong, int[] rows,
                                                   JTree tree) {
    final List<TreePath> list = new ArrayList<TreePath>(rows.length);
    final Comparator<TreePath> comparator = new PathLengthComparator(shortToLong);
    /* iterate over the rows and add non null paths in the correct order to the list */
    for (int row : rows) {
      final TreePath path = tree.getPathForRow(row);
      if(path != null) {
        final int insertIndex = Collections.binarySearch(list, path, comparator);
        list.add(insertIndex < 0 ? (-insertIndex-1) : insertIndex, path);
      }
    }
    return list;
  }
}
