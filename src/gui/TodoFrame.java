package gui;

import business.Converter;
import business.MutableTaskNode;
import business.Task;
import data.TodoFileIO;
import dto.DTOTask;
import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import static i18n.BundleStrings.*;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 11:17
 */
public class TodoFrame extends JFrame {
  private enum ItemType {RADIO_BUTTON, CHECK_BOX, NORMAL}

  /* Action Commands */
  private static final String AC_SAVE = "save list"; //NON-NLS
  private static final String AC_SAVE_AS = "save as"; //NON-NLS
  private static final String AC_OPEN_FILE = "open list"; //NON-NLS
  private static final String AC_CLOSE = "close"; //NON-NLS
  private static final String AC_ADD_TASK = "add task"; //NON-NLS
  private static final String AC_REMOVE_TASK = "remove task"; //NON-NLS
  private static final String AC_LANGUAGE_ENG = "english"; //NON-NLS
  private static final String AC_LANGUAGE_GER = "deutsch"; //NON-NLS

  /* constant fields */
  private static final String VERSION_NUMBER = "1.0";
  private static final int STATUS_BAR_HEIGHT = 20;

  private JMenuBar menuBar;
  private TaskTreeTable taskTreeTable;
  private TodoStatusBar statusBar;
  private JToolBar toolBar;

  private final ActionListener taskMenuAL;
  private final ActionListener settingsMenuAL;
  private final FileMenuItemListener fileMenuAL;

  private File currentFile;

  /* Constructors */

  public TodoFrame() {
    final FramePosition position = FramePosition.createFramePosition(0.8f);
    currentFile = null;
    taskMenuAL = new TaskActionListener(this);
    settingsMenuAL = new SettingsMenuItemListener();
    fileMenuAL = new FileMenuItemListener(this);

    setBounds(position.getBounds());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(fileMenuAL);
    setTitle(getTitleString());
    setIconImage(ResourceGetter.getImage(ResourceList.IMAGE_TODO_LIST).getImage());

    setJMenuBar(getCreateMenuBar());

    initComponents();
  }

  private String getTitleString() {
    String title = MessageFormat.format("{0} - {1} {2}",
        I18nSupport.getValue(TITLES, "application"),
        I18nSupport.getValue(MISC, "version"), VERSION_NUMBER);
    if (getCurrentFile().exists())
      title = title + " - " + getCurrentFile().getAbsolutePath();
    return title;
  }

  /* Methods */

  private JMenuBar getCreateMenuBar() {
    if (menuBar != null)
      return menuBar;

    final int ctrl = InputEvent.CTRL_DOWN_MASK;
    final int shift = InputEvent.SHIFT_DOWN_MASK;
    JMenu menu;
    JMenuItem item;

    menuBar = new JMenuBar();

    /* initialise the file menu */
    menu = new JMenu(I18nSupport.getValue(COMPONENTS, "text.file"));
    addMenuItem(menu, AC_OPEN_FILE, ResourceGetter.getImage(ResourceList.IMAGE_OPEN_FILE),
        KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrl), fileMenuAL);
    addMenuItem(menu, AC_SAVE, ResourceGetter.getImage(ResourceList.IMAGE_SAVE_FILE),
        KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl), fileMenuAL);
    addMenuItem(menu, AC_SAVE_AS, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl | shift), fileMenuAL);
    menu.addSeparator();
    addMenuItem(menu, AC_CLOSE, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl), fileMenuAL);
    menuBar.add(menu);

    /* initialise the settings menu */
    ButtonGroup languageGroup = new ButtonGroup();
    menu = new JMenu(I18nSupport.getValue(COMPONENTS, "text.settings"));
    JMenu pullRight = new JMenu(I18nSupport.getValue(COMPONENTS, "text.language"));
    item = addMenuItem(pullRight, AC_LANGUAGE_GER, null, settingsMenuAL, ItemType.RADIO_BUTTON);
    languageGroup.add(item);
    item = addMenuItem(pullRight, AC_LANGUAGE_ENG, null, settingsMenuAL, ItemType.RADIO_BUTTON);
    languageGroup.add(item);
    menu.add(pullRight);
    menuBar.add(menu);

    /* initialise the task menu */
    menu = new JMenu(I18nSupport.getValue(COMPONENTS, "text.task"));
    addMenuItem(menu, AC_ADD_TASK, ResourceGetter.getImage(ResourceList.IMAGE_PLUS_GREEN),
        KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), taskMenuAL);
    addMenuItem(menu, AC_REMOVE_TASK, ResourceGetter.getImage(ResourceList.IMAGE_MINUS_RED),
        KeyStroke.getKeyStroke(KeyEvent.VK_R, ctrl), taskMenuAL);
    menuBar.add(menu);

    return menuBar;
  }

  private JMenuItem addMenuItem(JMenu menu, String actionCommand, ImageIcon icon,
                                KeyStroke keyStroke, ActionListener actionListener) {
    final JMenuItem item = addMenuItem(menu, actionCommand, keyStroke, actionListener);
    item.setIcon(icon);
    return item;
  }

  private JMenuItem addMenuItem(JMenu menu, String actionCommand,
                                KeyStroke keyStroke, ActionListener actionListener) {
    return addMenuItem(menu, actionCommand, keyStroke, actionListener,
        ItemType.NORMAL);
  }

  private JMenuItem addMenuItem(JMenu menu, String actionCommand,
                                KeyStroke keyStroke, ActionListener actionListener,
                                ItemType type) {
    final JMenuItem item;
    if (ItemType.RADIO_BUTTON.equals(type))
      item = new JRadioButtonMenuItem();
    else if (ItemType.CHECK_BOX.equals(type))
      item = new JCheckBoxMenuItem();
    else item = new JMenuItem();

    item.setActionCommand(actionCommand);
    item.setAccelerator(keyStroke);
    item.addActionListener(actionListener);
    menu.add(item);

    return item;
  }

  private void initComponents() {
    getContentPane().setLayout(new BorderLayout());
    final JScrollPane scrollPane = new JScrollPane(getTableContainer(),
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    getContentPane().add(getStatusBar(), BorderLayout.PAGE_END);
    getContentPane().add(getToolBar(), BorderLayout.PAGE_START);

    resetI18n();
  }

  private void resetI18n() {
    /* reset frames i18n */
    setTitle(getTitleString());

    taskTreeTable.resetI18n();

    /* set the locale of the frame and of all components */
    for (Component component : getComponents()) {
      if (component instanceof JComponent)
        setDefaultLocale((JComponent) component);
    }
    setLocale(Locale.getDefault());
    revalidate();

    /* reset sub components i18n */
    resetMenuItemsI18n();
    resetToolBarI18n();
    statusBar.resetI18n();
  }

  private void setDefaultLocale(JComponent parent) {
    for (Component component : parent.getComponents()) {
      if (component instanceof JComponent)
        setDefaultLocale((JComponent) component);
      component.setLocale(Locale.getDefault());
      component.revalidate();
    }
  }

  private void resetToolBarI18n() {
    JButton button = (JButton) toolBar.getComponent(0);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.open.list"));
    button = (JButton) toolBar.getComponent(1);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.save.list"));
    /* component index 2 is a separator */
    button = (JButton) toolBar.getComponent(3);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.add.task"));
    button = (JButton) toolBar.getComponent(4);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.remove.task"));
    /* component index 5 is a separator */
    button = (JButton) toolBar.getComponent(6);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.expand.all.selected"));
    button = (JButton) toolBar.getComponent(7);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.collapse.all.selected"));
  }

  private void resetMenuItemsI18n() {
    /* reset the file menu */
    JMenu menu = menuBar.getMenu(0);
    menu.setText(I18nSupport.getValue(COMPONENTS, "text.file"));
    JMenuItem item = ((JMenuItem) menu.getMenuComponent(0));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.load.list"));
    item = ((JMenuItem) menu.getMenuComponent(1));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.save"));
    item = ((JMenuItem) menu.getMenuComponent(2));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.save.as"));
    /* item 3 is a separator */
    item = ((JMenuItem) menu.getMenuComponent(4));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.close"));

    /* reset the settings menu */
    menu = menuBar.getMenu(1);
    menu.setText(I18nSupport.getValue(COMPONENTS, "text.settings"));

    /* language popup menu section */
    JMenu language = (JMenu) menu.getMenuComponent(0);
    language.setText(I18nSupport.getValue(COMPONENTS, "text.language"));
    item = ((JMenuItem) language.getMenuComponent(0));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.german"));
    final Locale locale = Locale.getDefault();
    final boolean german = locale.equals(Locale.GERMAN) || locale.equals(Locale.GERMANY);
    if (german) item.setSelected(true);
    item = ((JMenuItem) language.getMenuComponent(1));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.english"));
    if (!german) item.setSelected(true);

    /* reset the task menu */
    menu = menuBar.getMenu(2);
    menu.setText(I18nSupport.getValue(COMPONENTS, "text.task"));
    item = ((JMenuItem) menu.getMenuComponent(0));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.add.task"));
    item = ((JMenuItem) menu.getMenuComponent(1));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.remove.task"));
  }

  private TaskTreeTable getTableContainer() {
    if (taskTreeTable != null)
      return taskTreeTable;
    taskTreeTable = new TaskTreeTable();
    taskTreeTable.setMaximumSize(taskTreeTable.getPreferredSize());
    return taskTreeTable;
  }

  private void updateGUI() {
    if (taskTreeTable.hasListChanged())
      setTitle(getTitleString() + "*");
    else setTitle(getTitleString());
  }

  /* Getter and Setter */

  public File getCurrentFile() {
    if (currentFile == null)
      return new File("");
    return currentFile;
  }

  public void setCurrentFile(File newFile) {
    currentFile = newFile != null ? newFile : new File("");
  }

  public TodoStatusBar getStatusBar() {
    if (statusBar != null)
      return statusBar;

    statusBar = new TodoStatusBar();
    statusBar.setPreferredSize(new Dimension(0, STATUS_BAR_HEIGHT));

    return statusBar;
  }

  public JToolBar getToolBar() {
    if (toolBar != null)
      return toolBar;

    toolBar = new JToolBar();
    toolBar.setLayout(new FlowLayout(FlowLayout.LEADING));
    toolBar.setFloatable(false);

    final ActionListener expandListener = new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        taskTreeTable.expandCollapseSelectedNodes(true);}};
    final ActionListener collapseListener = new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        taskTreeTable.expandCollapseSelectedNodes(false);}};
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_OPEN_FILE),
        AC_OPEN_FILE, fileMenuAL));
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_SAVE_FILE),
        AC_SAVE, fileMenuAL));
    toolBar.addSeparator();
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_PLUS_GREEN),
        AC_ADD_TASK, taskMenuAL));
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_MINUS_RED),
        AC_REMOVE_TASK, taskMenuAL));
    toolBar.addSeparator();
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_ARROWS_OUT),
        "", expandListener));
    toolBar.add(GUIUtilities.createButton(
        ResourceGetter.getImage(ResourceList.IMAGE_ARROWS_IN), "", collapseListener));

    return toolBar;
  }

  private boolean hasChanged() {
    return taskTreeTable.hasListChanged();
  }

  /* Inner Classes */

  private class FileMenuItemListener extends WindowAdapter implements ActionListener {
    private TodoFrame frame;

    private FileMenuItemListener(TodoFrame frame) {
      this.frame = frame;
    }

    public void windowClosing(WindowEvent e) {
      closeApplication();
    }

    public void actionPerformed(ActionEvent e) {
      if (AC_OPEN_FILE.equals(e.getActionCommand())) {
        openFile();
      } else if (AC_SAVE.equals(e.getActionCommand())) {
        saveFile();
      } else if (AC_SAVE_AS.equals(e.getActionCommand())) {
        saveFileAs();
      } else if (AC_CLOSE.equals(e.getActionCommand())) {
        closeApplication();
      }
      updateGUI();
    }

    private void closeApplication() {
      int result = JOptionPane.NO_OPTION;
      String[] strings = {I18nSupport.getValue(COMPONENTS, "text.close.and.save"),
          I18nSupport.getValue(COMPONENTS, "text.close.without.saving"),
          I18nSupport.getValue(COMPONENTS, "text.not.sure")};

      if (hasChanged()) {
        result = GUIUtilities.showConfirmDialog(frame, strings,
            I18nSupport.getValue(MESSAGES, "question.save.before.close"), "",
            JOptionPane.QUESTION_MESSAGE, 2);
      }

      if (result == JOptionPane.OK_OPTION) {
        saveFile();
        closeWindow();
      } else if (result == JOptionPane.NO_OPTION) {
        closeWindow();
      } else {
        setVisible(true);
      }
    }

    private void closeWindow() {
      /* Close window smooth */
      setVisible(false);
      dispose();
      System.exit(0);
    }

    private void saveFileAs() {
      final File selectedFile = GUIUtilities.getSaveFile(currentFile, frame);
      try {
        if (selectedFile != null)
          writeListToFile(selectedFile);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(frame, I18nSupport.getValue(MESSAGES,
            "could.not.save.list"), "", JOptionPane.ERROR_MESSAGE);
      }
    }

    private void saveFile() {
      try {
        if (getCurrentFile().exists()) {
          writeListToFile(currentFile);
        } else
          saveFileAs();
      } catch (IOException e) {
        JOptionPane.showMessageDialog(frame, I18nSupport.getValue(MESSAGES,
            "could.not.save.list"), "", JOptionPane.ERROR_MESSAGE);
      }
    }

    private void writeListToFile(File file) throws IOException {
      final List<DTOTask> tasks = Converter.toDTOList(taskTreeTable.getTaskRoot());
      TodoFileIO.writeTodoFile(tasks, file);
      setCurrentFile(file);
      taskTreeTable.setListChanged(false);
      statusBar.setText(I18nSupport.getValue(MESSAGES, "saved.file.0",
          file.getAbsolutePath()));
    }

    private void openFile() {
      final File selectedFile = GUIUtilities.getOpenFile(currentFile, frame);
      if (selectedFile != null) {
        try {
          final List<DTOTask> dtoTasks = TodoFileIO.readTodoFile(selectedFile);
          final MutableTaskNode root = Converter.fromDTOList(dtoTasks);
          taskTreeTable.setRoot(root);
          frame.setCurrentFile(selectedFile);
          taskTreeTable.setListChanged(false);
          statusBar.setText(I18nSupport.getValue(MESSAGES, "opened.file.0",
              selectedFile.getAbsolutePath()));
        } catch (IOException e) {
          JOptionPane.showMessageDialog(frame,
              I18nSupport.getValue(MESSAGES, "could.not.load.list"), "",
              JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(frame,
              I18nSupport.getValue(MESSAGES, "wrong.file.format"), "",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private class SettingsMenuItemListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      final String text = I18nSupport.getValue(MESSAGES, "changed.language");
      if (AC_LANGUAGE_ENG.equals(e.getActionCommand())) {
        Locale.setDefault(Locale.UK);
        resetI18n();
        statusBar.setText(text);
      } else if (AC_LANGUAGE_GER.equals(e.getActionCommand())) {
        Locale.setDefault(Locale.GERMANY);
        resetI18n();
        statusBar.setText(text);
      }
    }
  }

  private class TaskActionListener implements ActionListener {
    private TodoFrame frame;

    private TaskActionListener(TodoFrame frame) {
      this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {

      if (AC_ADD_TASK.equals(e.getActionCommand())) {
        addTask();
      } else if (AC_REMOVE_TASK.equals(e.getActionCommand())) {
        removeTasks();
      }
      updateGUI();
    }

    private void addTask() {
      final Task task = new Task();
      task.setName(I18nSupport.getValue(MISC, "default.task.name"));
      taskTreeTable.addTask(task);
      statusBar.setText(I18nSupport.getValue(MESSAGES, "added.task"));
    }

    private void removeTasks() {
      if(taskTreeTable.hasSelectedTasks()) {
        String text = "";
        String[] strings = {I18nSupport.getValue(COMPONENTS, "text.remove"),
          I18nSupport.getValue(COMPONENTS, "text.reset.selections"),
          I18nSupport.getValue(COMPONENTS, "text.not.sure")};
        int result = GUIUtilities.showConfirmDialog(frame, strings,
          I18nSupport.getValue(MESSAGES, "remove.selected.tasks.question"),
          I18nSupport.getValue(TITLES, "remove.tasks"),
          JOptionPane.QUESTION_MESSAGE, 2);
        if(result == JOptionPane.YES_OPTION) {
          int deleted = taskTreeTable.removeSelectedTasks();
          if(deleted != 0) {
            text = I18nSupport.getValue(MESSAGES, "removed.tasks.0",deleted);
          }
        } else if(result == JOptionPane.NO_OPTION)
          taskTreeTable.clearSelection();
        statusBar.setText(text);
      }
    }
  }
}

class GUIUtilities {
  private static final FileFilter FILE_FILTER = new FileNameExtensionFilter(
      I18nSupport.getValue(MISC, "file.filter.todo.description"), "todo"); //NON-NLS
  private static final String DOT_EXTENSION_TODO = ".todo"; //NON-NLS
  private static final String DEFAULT_LIST_NAME =
      I18nSupport.getValue(MISC, "default.file.name");

  /**
   * Opens an open file dialog at the specified file if it exists.
   * The method returns a File-Object of the selected file.
   *
   * @param startFile Opens the chooser on this file
   * @return The selected file if the approved option was pressed,
   *         otherwise null.
   */
  public static File getOpenFile(File startFile, Component parent) {
    final JFileChooser chooser;
    final int chooserResult;
    final File selectedFile;

    chooser = new JFileChooser();
    chooser.setFileFilter(FILE_FILTER);
    chooser.setMultiSelectionEnabled(false);
    if (startFile != null)
      chooser.setSelectedFile(startFile);
    else chooser.setSelectedFile(new File(DEFAULT_LIST_NAME));

    chooserResult = chooser.showOpenDialog(parent);
    if (JFileChooser.APPROVE_OPTION == chooserResult) {
      selectedFile = chooser.getSelectedFile();
    } else selectedFile = null;

    return selectedFile;
  }

  /**
   * Opens a save file dialog at the specified file if it exists.
   * The method returns a File-Object of the selected file.
   *
   * @param startFile Opens the chooser on this file
   * @return The selected file if the approved option was pressed,
   *         otherwise null.
   */
  public static File getSaveFile(File startFile, Component parent) {
    final JFileChooser chooser;
    final int chooserResult;
    File selectedFile;

    chooser = new JFileChooser();
    chooser.setFileFilter(FILE_FILTER);
    chooser.setMultiSelectionEnabled(false);
    if (startFile != null)
      chooser.setSelectedFile(startFile);
    else chooser.setSelectedFile(new File(DEFAULT_LIST_NAME));

    chooserResult = chooser.showSaveDialog(parent);
    if (JFileChooser.APPROVE_OPTION == chooserResult) {
      selectedFile = chooser.getSelectedFile();
      String path = selectedFile.getAbsolutePath();
      if (!path.toLowerCase().endsWith(DOT_EXTENSION_TODO)) {
        selectedFile = new File(path + DOT_EXTENSION_TODO);
      }
    } else selectedFile = null;

    return selectedFile;
  }

  @SuppressWarnings("MagicConstant")
  public static int showConfirmDialog(Component parentComponent, String[] strings,
                                      Object message, String title, int messageType,
                                      int selected) {
    return JOptionPane.showOptionDialog(parentComponent, message, title,
        JOptionPane.YES_NO_CANCEL_OPTION, messageType, null, strings, strings[selected]);
  }

  public static JButton createButton(Icon icon, String actionCommand,
                                     ActionListener actionListener) {
    JButton button = new JButton();
    button.setIcon(icon);
    button.setActionCommand(actionCommand);
    button.addActionListener(actionListener);

    return button;
  }

  public static GridBagConstraints createConstraints(int x, int y) {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.LINE_START;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0, 3, 0, 3);
    return constraints;
  }

  public static GridBagConstraints createConstraints(int x, int y, int width, int height) {
    final GridBagConstraints constraints = createConstraints(x, y);
    constraints.gridwidth = width;
    constraints.gridheight = height;
    return constraints;
  }

  @SuppressWarnings("UnusedDeclaration")
  public static GridBagConstraints createConstraints(int x, int y, int width, int height, int fill) {
    final GridBagConstraints constraints = createConstraints(x, y, width, height);
    constraints.fill = fill;
    return constraints;
  }

  @SuppressWarnings("UnusedDeclaration")
  public static GridBagConstraints createConstraints(int x, int y, int width, int height, int fill,
                                                     double weightx, double weighty) {
    final GridBagConstraints constraints = createConstraints(x, y, width, height, weightx, weighty);
    constraints.fill = fill;
    return constraints;
  }

  public static GridBagConstraints createConstraints(int x, int y, int width, int height,
                                                     double weightx, double weighty) {
    final GridBagConstraints constraints = createConstraints(x, y, width, height);
    constraints.weighty = weighty;
    constraints.weightx = weightx;
    return constraints;
  }
}
