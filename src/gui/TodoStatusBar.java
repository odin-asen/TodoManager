package gui;

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

import static i18n.BundleStrings.MISC;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 19:37
 */
public class TodoStatusBar extends JPanel implements Runnable {
  private static final Border LOWERED_BORDER = BorderFactory.createBevelBorder(BevelBorder.LOWERED);

  private static DateFormat format;
  private static Calendar calendar;

  private JPanel besideLabelPanel;
  private JLabel mainStatusLabel;
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

    new Thread(this).start();
  }

  /* Methods */
  private JPanel getBesideLabelPanel() {
    if(besideLabelPanel != null)
      return besideLabelPanel;

    besideLabelPanel = new JPanel();
    clockLabel = new JLabel();

    besideLabelPanel.setLayout(new BoxLayout(besideLabelPanel, BoxLayout.LINE_AXIS));
    addStatusLabel(besideLabelPanel, clockLabel);

    return besideLabelPanel;
  }

  private void addStatusLabel(JPanel panel, JLabel label) {
    label.setBorder(LOWERED_BORDER);
    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    panel.add(label);
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
  }
}