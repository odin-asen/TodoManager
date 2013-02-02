package gui;

import dto.TaskProperty;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 02.02.13
 * Time: 19:51
 * <p/>
 * Renders the combo box items for priority and attribution.
 */
public class AttributionPriorityRenderer extends DefaultListCellRenderer {
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    final JLabel label = new JLabel();
    if(value instanceof TaskProperty.Attribution) {
      final TaskProperty.Attribution attribution = (TaskProperty.Attribution) value;
      label.setText(attribution.getDescription());
      label.setIcon(attribution.getIcon(this.getPreferredSize().height));
    } else if(value instanceof TaskProperty.Priority) {
      final TaskProperty.Priority priority = (TaskProperty.Priority) value;
      label.setText(priority.getDescription());
      label.setIcon(priority.getIcon(this.getPreferredSize().height));
    } else {
      if(value != null)
        label.setText(value.toString());
    }

    label.setOpaque(true);
    label.setForeground(comp.getForeground());
    label.setBackground(comp.getBackground());

    return label;
  }
}
