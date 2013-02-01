package gui;

import business.Task;
import dto.TaskProperty;
import i18n.I18nSupport;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static i18n.BundleStrings.*;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 19:37
 */
public class TodoStatusBar extends JPanel implements Runnable {
  private static final Dimension MINIMUM_DIMENSION = new Dimension(16,16);

  private static final Border LOWERED_BORDER = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

  private static DateFormat format;
  private static Calendar calendar;

  private JPanel besideLabelPanel;
  private JLabel mainStatusLabel;
  private JLabel attributionLabel;
  private JLabel taskLabel;
  private JLabel clockLabel;
  private Boolean running;


  /* Constructors */
  public TodoStatusBar() {
    mainStatusLabel = new JLabel();
    mainStatusLabel.setBorder(LOWERED_BORDER);
    running = true;

    setLayout(new BorderLayout());
    add(mainStatusLabel, BorderLayout.CENTER);
    add(getBesideLabelPanel(), BorderLayout.LINE_END);
    resetI18n();

    /* init labels */
    showTaskInformation(null);

    new Thread(this).start();
  }

  /* Methods */
  private JPanel getBesideLabelPanel() {
    if(besideLabelPanel != null)
      return besideLabelPanel;

    besideLabelPanel = new JPanel();
    clockLabel = new JLabel();
    attributionLabel = new JLabel();
    taskLabel = new JLabel();

    besideLabelPanel.setLayout(new BoxLayout(besideLabelPanel, BoxLayout.LINE_AXIS));
    addStatusLabel(besideLabelPanel, taskLabel);
    addStatusLabel(besideLabelPanel, attributionLabel);
    addStatusLabel(besideLabelPanel, clockLabel);

    return besideLabelPanel;
  }

  private void addStatusLabel(JPanel panel, JLabel label) {
    label.setBorder(LOWERED_BORDER);
    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    panel.add(label);
  }

  /**
   * Displays the task's information in the status bar.
   * The display can be removed by calling the method with
   * null as parameter
   * @param task Task whose information will be displayed.
   */
  public void showTaskInformation(Task task) {
    if(task == null)
      setTaskInformation(null, null, null, null);
    else {
      final DateFormat format = new SimpleDateFormat(I18nSupport.getValue(
          MISC, "format.due.date"), Locale.getDefault());
      final Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(task.getDueDate());
      setTaskInformation(task.getAttribution(),
          format.format(calendar.getTime()), task.getName(),
          I18nSupport.getValue(COMPONENTS, "tooltip.category.0", task.getName()));
      setText(I18nSupport.getValue(MESSAGES, "selected.task"));
    }
  }

  private void setTaskInformation(TaskProperty.Attribution attribution,
                                  String time, String taskText,
                                  String taskToolTip) {
    attributionLabel.setText(time);
    attributionLabel.setToolTipText(attribution != null ? attribution.getDescription() : null);
    attributionLabel.setIcon(attribution != null
        ? attribution.getIcon(attributionLabel.getSize().height) : null);
    attributionLabel.setPreferredSize(time == null || time.isEmpty()
        ? MINIMUM_DIMENSION : null);

    taskLabel.setText(taskText);
    taskLabel.setToolTipText(taskToolTip);
    taskLabel.setPreferredSize(taskText == null || taskText.isEmpty()
        ? MINIMUM_DIMENSION : null);

  }

  public void setText(String text) {
    mainStatusLabel.setText(text);
  }

  private void setTime(long millisSince1970) {
    calendar.setTimeInMillis(millisSince1970);
    clockLabel.setText(format.format(calendar.getTime()));
  }

  public void run() {
    long millis = System.currentTimeMillis();
    final long waitingTime = 1000L;
    while(running) {
      try {
        this.setTime(millis);
        Thread.sleep(waitingTime);
        millis = millis + waitingTime;
      } catch (InterruptedException ignored) {}
    }
  }

  /* Getter and Setter */

  public void resetI18n() {
    calendar = GregorianCalendar.getInstance(Locale.getDefault());
    format = new SimpleDateFormat(I18nSupport.getValue(MISC, "format.date"),
        Locale.getDefault());
    format.setCalendar(calendar);
    setTaskInformation(null,"","","");
  }
}