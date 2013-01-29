package gui;

import business.Task;
import business.TaskCategory;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import data.TodoFileIO;
import dto.DTOTask;
import dto.TaskProperty;
import i18n.I18nSupport;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 11:17
 */
public class TodoFrame extends JFrame {
  private static TodoFrame todoFrame;
  private JCalendar calendar;

  private enum ItemType {RADIO_BUTTON, CHECK_BOX, NORMAL}

  /* Action Commands */
  private static final String AC_SAVE = "save list"; //NON-NLS
  private static final String AC_SAVE_AS = "save as"; //NON-NLS
  private static final String AC_OPEN_FILE = "open list"; //NON-NLS
  private static final String AC_CLOSE = "close"; //NON-NLS
  private static final String AC_ADD_TASK = "add task"; //NON-NLS
  private static final String AC_REMOVE_TASK = "remove task"; //NON-NLS
  private static final String AC_EDIT_TASK = "edit task"; //NON-NLS
  private static final String AC_LANGUAGE_ENG = "english"; //NON-NLS
  private static final String AC_LANGUAGE_GER = "deutsch"; //NON-NLS

  /* constant fields */
  private static final String VERSION_NUMBER = "0.1";
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  private static final String BUNDLE_MESSAGES = "messages"; //NON-NLS
  private static final int STATUS_BAR_HEIGHT = 20;

  private JMenuBar menuBar;
  private TaskTreePanel taskTreePanel;
  private TaskTable taskTable;
  private TodoStatusBar statusBar;
  private JToolBar toolBar;

  private final ActionListener taskMenuAL;
  private final ActionListener settingsMenuAL;
  private final FileMenuItemListener fileMenuAL;

  private File currentFile;

  /* Constructors */

  public static TodoFrame getTodoFrame() {
    if(todoFrame == null) {
      todoFrame = new TodoFrame();
    }

    return todoFrame;
  }

  private TodoFrame() {
    final FramePosition position = FramePosition.createFramePosition(0.8f);
    currentFile = null;
    taskMenuAL = new TaskActionListener();
    settingsMenuAL = new SettingsMenuItemListener();
    fileMenuAL = new FileMenuItemListener();

    setBounds(position.getBounds());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(fileMenuAL);
    setTitle(getTitleString());
    setIconImage(ResourceGetter.getImage(
        ResourceList.IMAGE_TODO_LIST, "todo").getImage()); //NON-NLS

    setJMenuBar(getCreateMenuBar());

    initComponents();
  }

  private String getTitleString() {
    String title = MessageFormat.format("{0} - {1} {2}",
        I18nSupport.getValue(BUNDLE_GUI, "application.title"),
        I18nSupport.getValue(BUNDLE_GUI, "version"), VERSION_NUMBER);
    if(getCurrentFile().exists())
      title = title + " - "+getCurrentFile().getAbsolutePath();
    return title;
  }

  /* Methods */

  private JMenuBar getCreateMenuBar() {
    if(menuBar != null)
      return menuBar;

    final int ctrl = InputEvent.CTRL_DOWN_MASK;
    final int shift = InputEvent.SHIFT_DOWN_MASK;
    JMenu menu;
    JMenuItem item;

    menuBar = new JMenuBar();

    /* initialise the file menu */
    menu = new JMenu(I18nSupport.getValue(BUNDLE_GUI,"menu.text.file"));
    addMenuItem(menu, AC_OPEN_FILE, ResourceGetter.getImage(ResourceList.IMAGE_OPEN_FILE, ""),
        KeyStroke.getKeyStroke(KeyEvent.VK_O,ctrl), fileMenuAL);
    addMenuItem(menu, AC_SAVE, ResourceGetter.getImage(ResourceList.IMAGE_SAVE_FILE, ""),
        KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl), fileMenuAL);
    addMenuItem(menu, AC_SAVE_AS, KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl|shift), fileMenuAL);
    menu.addSeparator();
    addMenuItem(menu, AC_CLOSE, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl), fileMenuAL);
    menuBar.add(menu);

    /* initialise the settings menu */
    ButtonGroup languageGroup = new ButtonGroup();
    menu = new JMenu(I18nSupport.getValue(BUNDLE_GUI,"menu.text.settings"));
    JMenu pullRight = new JMenu(I18nSupport.getValue(BUNDLE_GUI,"menu.text.language"));
    item = addMenuItem(pullRight, AC_LANGUAGE_GER, null, settingsMenuAL, ItemType.RADIO_BUTTON);
    languageGroup.add(item);
    item = addMenuItem(pullRight, AC_LANGUAGE_ENG, null, settingsMenuAL, ItemType.RADIO_BUTTON);
    languageGroup.add(item);
    menu.add(pullRight);
    menuBar.add(menu);

    /* initialise the task menu */
    menu = new JMenu(I18nSupport.getValue(BUNDLE_GUI,"menu.text.task"));
    addMenuItem(menu, AC_ADD_TASK, ResourceGetter.getImage(ResourceList.IMAGE_PLUS_GREEN, ""),
        KeyStroke.getKeyStroke(KeyEvent.VK_A,ctrl), taskMenuAL);
    addMenuItem(menu, AC_EDIT_TASK, ResourceGetter.getImage(ResourceList.IMAGE_EDIT, ""),
        KeyStroke.getKeyStroke(KeyEvent.VK_E,ctrl), taskMenuAL);
    addMenuItem(menu, AC_REMOVE_TASK, ResourceGetter.getImage(ResourceList.IMAGE_MINUS_RED, ""),
        KeyStroke.getKeyStroke(KeyEvent.VK_R,ctrl), taskMenuAL);
    menuBar.add(menu);

    return menuBar;
  }

  private JMenuItem addMenuItem(JMenu menu, String actionCommand, ImageIcon icon,
                                KeyStroke keyStroke, ActionListener actionListener) {
    final JMenuItem item = addMenuItem(menu,actionCommand,keyStroke, actionListener);
    item.setIcon(icon);
    return item;
  }

  private JMenuItem addMenuItem(JMenu menu, String actionCommand,
                           KeyStroke keyStroke, ActionListener actionListener) {
    return addMenuItem(menu,actionCommand,keyStroke,actionListener,
        ItemType.NORMAL);
  }

  private JMenuItem addMenuItem(JMenu menu, String actionCommand,
                           KeyStroke keyStroke, ActionListener actionListener,
                           ItemType type) {
    final JMenuItem item;
    if(ItemType.RADIO_BUTTON.equals(type))
      item = new JRadioButtonMenuItem();
    else if(ItemType.CHECK_BOX.equals(type))
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
    getContentPane().add(createSelectorCalendarPanel(), BorderLayout.LINE_START);
    getContentPane().add(getTableContainer(), BorderLayout.CENTER);
    getContentPane().add(getStatusBar(), BorderLayout.PAGE_END);
    getContentPane().add(getToolBar(), BorderLayout.PAGE_START);

    resetI18n();
  }

  private void resetI18n() {
    /* reset frames i18n */
    setTitle(getTitleString());

    /* reset all open task editor panels */ //TODO für table umstellen
//    taskTable.setLocale(Locale.getDefault());
//    for (Component c : taskTable.getComponents()) {
//      final TaskEditorPanel editor = (TaskEditorPanel) c;
//      editor.resetI18n();
//    }

    /* set the locale of the frame and of all components */
    for (Component component : getComponents()) {
      if(component instanceof JComponent)
        setDefaultLocale((JComponent) component);
    }
    setLocale(Locale.getDefault());
    revalidate();

    /* reset sub components i18n */
    resetSelectorCalendarI18n();
    resetMenuItemsI18n();
    resetToolBarI18n();
    statusBar.resetI18n();
  }

  private void setDefaultLocale(JComponent parent) {
    for (Component component : parent.getComponents()) {
      if(component instanceof JComponent)
        setDefaultLocale((JComponent) component);
      component.setLocale(Locale.getDefault());
      component.revalidate();
    }
  }

  private void resetToolBarI18n() {
    JButton button = (JButton) toolBar.getComponent(0);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.open.list"));
    button = (JButton) toolBar.getComponent(1);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.save.list"));
    /* component index 2 is a separator */
    button = (JButton) toolBar.getComponent(3);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.add.task"));
    button = (JButton) toolBar.getComponent(4);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.edit.task"));
    button = (JButton) toolBar.getComponent(5);
    button.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.remove.task"));
  }

  private void resetMenuItemsI18n() {
    /* reset the file menu */
    JMenu menu = menuBar.getMenu(0);
    menu.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.text.file"));
    JMenuItem item = ((JMenuItem) menu.getMenuComponent(0));
    item.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.item.text.load.list"));
    item = ((JMenuItem) menu.getMenuComponent(1));
    item.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.item.text.save"));
    item = ((JMenuItem) menu.getMenuComponent(2));
    item.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.item.text.save.as"));
    //item 3 is a separator
    item = ((JMenuItem) menu.getMenuComponent(4));
    item.setText(I18nSupport.getValue(BUNDLE_GUI, "menu.item.text.close"));


    /* reset the settings menu */
    menu = menuBar.getMenu(1);
    menu.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.text.settings"));

    /* language popup menu section */
    JMenu language = (JMenu) menu.getMenuComponent(0);
    language.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.text.language"));
    item = ((JMenuItem) language.getMenuComponent(0));
    item.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.item.text.german"));
    final Locale locale = Locale.getDefault();
    final boolean german = locale.equals(Locale.GERMAN) || locale.equals(Locale.GERMANY);
    if(german) item.setSelected(true);
    item = ((JMenuItem) language.getMenuComponent(1));
    item.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.item.text.english"));
    if(!german) item.setSelected(true);

    /* reset the task menu */
    menu = menuBar.getMenu(2);
    menu.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.text.task"));
    item = ((JMenuItem) menu.getMenuComponent(0));
    item.setText(I18nSupport.getValue(BUNDLE_GUI,"menu.item.text.add.task"));
    item = ((JMenuItem) menu.getMenuComponent(1));
    item.setText(I18nSupport.getValue(BUNDLE_GUI, "menu.item.text.edit.task"));
    item = ((JMenuItem) menu.getMenuComponent(2));
    item.setText(I18nSupport.getValue(BUNDLE_GUI, "menu.item.text.remove.task"));
  }

  private void resetSelectorCalendarI18n() {
    taskTreePanel.resetI18n();
    /* reset calendar */
    calendar.setLocale(Locale.getDefault());
  }

  private JPanel createSelectorCalendarPanel() {
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    calendar = new JCalendar();
    calendar.setMaximumSize(calendar.getPreferredSize());
    calendar.getDayChooser().setMaxDayCharacters(2);

    /* Provide the change of due date for all selected tasks when the mouse is pressed */
    MouseListener ml = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (taskTreePanel.selectedNodeExists()) {
          changeDueDateDialog(calendar.getDate());
        }
      }
    };
     /* Each button of the day panel gets a mouse listener that changes the due dates
      * after a button was pressed. */
    for (Component c : calendar.getDayChooser().getDayPanel().getComponents()) {
      if(c instanceof JButton)
        c.addMouseListener(ml);
    }

    panel.add(getTaskTreePanel());
    panel.add(calendar);
    return panel;
  }

  private TaskTreePanel getTaskTreePanel() {
    if(taskTreePanel != null)
      return taskTreePanel;

    taskTreePanel = new TaskTreePanel();

    return taskTreePanel;
  }

  private JComponent getTableContainer() {
    if(taskTable != null)
      return taskTable;
    taskTable = new TaskTable();
    taskTable.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    return taskTable;
  }

  private void updateGUI() {
    if(taskTreePanel.hasListChanged())
      setTitle(getTitleString()+"*");
    else setTitle(getTitleString());
  }

  public void openTask(Task task, boolean addNew) {
    final TaskEditorPanel openPanel = findOpenEditor(task);
    if(openPanel == null) {
      int columns = taskTable.getComponentCount()+1;
      if(columns > 3)
        columns = 3;
      taskTable.setLayout(new GridLayout(0,columns));
      taskTable.add(new TaskEditorPanel(task, addNew));
    } else {
      openPanel.notifyYourself();
      statusBar.setText(I18nSupport.getValue(BUNDLE_MESSAGES, "task.already.open"));
    }
  }

  private TaskEditorPanel findOpenEditor(Task task) {
    for (Component component : taskTable.getComponents()) {
      final TaskEditorPanel editor = (TaskEditorPanel) component;
      if(editor.getUnchangedTask().equals(task))
        return editor;
    }
    return null;
  }

  public void closeTask(TaskEditorPanel panel, Task unchangedTask) {
    final Task updatedTask = panel.getUpdatedTask();
    if(!updatedTask.getName().isEmpty()) {
      if(!panel.isCreatedNew()) {
        if(!updatedTask.equals(unchangedTask))
          taskTreePanel.changeTask(unchangedTask, updatedTask);
      } else taskTreePanel.addTask(updatedTask, true);
      removeEditor(panel);
    } else {
      JOptionPane.showMessageDialog(this,
          I18nSupport.getValue(BUNDLE_MESSAGES, "enter.a.task.name"),
          I18nSupport.getValue(BUNDLE_GUI, "dialog.title.missing.value"),
          JOptionPane.PLAIN_MESSAGE);
    }
    updateGUI();
  }

  private void removeEditor(TaskEditorPanel panel) {
    for (int index = 0; index < taskTable.getComponentCount(); index++) {
      final TaskEditorPanel editor = (TaskEditorPanel) taskTable.getComponent(index);
      if(editor.equals(panel)) {
        taskTable.remove(editor);
        taskTable.updateUI();
        index = taskTable.getComponentCount();
      }
    }
  }

  private void changeDueDateDialog(Date newDate) {
    int result;
    String[] strings = {I18nSupport.getValue(BUNDLE_GUI, "dialog.button.change"),
        I18nSupport.getValue(BUNDLE_GUI, "dialog.button.do.not.change"),
        I18nSupport.getValue(BUNDLE_GUI, "dialog.button.not.sure")};
    final DateFormat format = new SimpleDateFormat(I18nSupport.getValue(
        BUNDLE_GUI, "format.due.date"), Locale.getDefault());
    String dateString = format.format(newDate);
    result = GUIUtilities.showConfirmDialog(todoFrame, strings,
        I18nSupport.getValue(BUNDLE_MESSAGES, "question.change.selected.due.dates.to.0",
            dateString), "", JOptionPane.QUESTION_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      final List<Task> tasks = taskTreePanel.getSelectedTasks();
      changeTasksDueDate(newDate, tasks);
    } else if(result == JOptionPane.NO_OPTION)
      taskTreePanel.deselectAllTasks();
  }

  private void changeTasksDueDate(Date newDate, List<Task> tasks) {
    for (Task task : tasks) {
      final Task newTask = new Task(task.getCategory(), task.getName(),
          task.getDescription(), task.isPermanent(), newDate.getTime(),
          task.getAttribution(), task.getPriority());
      taskTreePanel.changeTask(task, newTask);
    }
  }

  /* Getter and Setter */

  public File getCurrentFile() {
    if(currentFile == null)
      return new File("");
    return currentFile;
  }

  public void setCurrentFile(File newFile, List<Task> toLoad) {
    currentFile = newFile != null ? newFile : new File("");

    if(toLoad != null)
      taskTreePanel.setTasks(toLoad);
  }

  public TodoStatusBar getStatusBar() {
    if(statusBar != null)
      return statusBar;

    statusBar = new TodoStatusBar();
    statusBar.setPreferredSize(new Dimension(0, STATUS_BAR_HEIGHT));

    return statusBar;
  }

  public JToolBar getToolBar() {
    if(toolBar != null)
      return toolBar;

    toolBar = new JToolBar();
    toolBar.setLayout(new FlowLayout(FlowLayout.LEADING));
    toolBar.setFloatable(false);

    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_OPEN_FILE, "open"), AC_OPEN_FILE, fileMenuAL)); //NON-NLS
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_SAVE_FILE, "save"), AC_SAVE, fileMenuAL)); //NON-NLS
    toolBar.addSeparator();
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_PLUS_GREEN, "add"), AC_ADD_TASK, taskMenuAL)); //NON-NLS
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_EDIT, "edt"), AC_EDIT_TASK, taskMenuAL)); //NON-NLS
    toolBar.add(GUIUtilities.createButton(ResourceGetter.getImage(ResourceList.IMAGE_MINUS_RED, "rmv"), AC_REMOVE_TASK, taskMenuAL)); //NON-NLS

    return toolBar;
  }

  private List<Task> getTaskList() {
    return taskTreePanel.getTaskList();
  }

  private boolean hasChanged() {
    return taskTreePanel.hasListChanged();
  }

  /* Inner Classes */

  private class FileMenuItemListener extends WindowAdapter implements ActionListener {
    public void windowClosing(WindowEvent e) {
      closeApplication();
    }

    public void actionPerformed(ActionEvent e) {
      if(AC_OPEN_FILE.equals(e.getActionCommand())) {
        openFile();
      } else if(AC_SAVE.equals(e.getActionCommand())) {
        saveFile();
      } else if(AC_SAVE_AS.equals(e.getActionCommand())) {
        saveFileAs();
      } else if(AC_CLOSE.equals(e.getActionCommand())) {
        closeApplication();
      }
      TodoFrame.getTodoFrame().updateGUI();
    }

    private void closeApplication() {
      int result = JOptionPane.NO_OPTION;
      String[] strings = {I18nSupport.getValue(BUNDLE_GUI, "dialog.button.close.and.save"),
          I18nSupport.getValue(BUNDLE_GUI, "dialog.button.close.without.saving"),
          I18nSupport.getValue(BUNDLE_GUI, "dialog.button.not.sure")};

      if(todoFrame.hasChanged()) {
        result = GUIUtilities.showConfirmDialog(todoFrame, strings,
          I18nSupport.getValue(BUNDLE_MESSAGES, "question.save.before.close"), "",
          JOptionPane.QUESTION_MESSAGE);
      }

      if(result == JOptionPane.OK_OPTION) {
        saveFile();
        closeWindow();
      } else if(result == JOptionPane.NO_OPTION) {
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

      final File selectedFile = GUIUtilities.getSaveFile(currentFile, todoFrame);
      try {
        if(selectedFile != null)
          writeListToFile(selectedFile);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(todoFrame, I18nSupport.getValue(BUNDLE_MESSAGES,
            "could.not.save.list"), "", JOptionPane.ERROR_MESSAGE);
      }
    }

    private void saveFile() {
      try {
        if(getCurrentFile().exists()) {
          writeListToFile(currentFile);
        } else
          saveFileAs();
      } catch (IOException e) {
          JOptionPane.showMessageDialog(TodoFrame.getTodoFrame(), I18nSupport.getValue(BUNDLE_MESSAGES,
              "could.not.save.list"), "", JOptionPane.ERROR_MESSAGE);
      }
    }

    private void writeListToFile(File file) throws IOException {

      final List<Task> tasks = todoFrame.getTaskList();
      final List<DTOTask> dtoTasks = new ArrayList<DTOTask>(tasks.size());
      for (Task task : tasks)
        dtoTasks.add(Task.toDTO(task));
      TodoFileIO.writeTodoFile(dtoTasks, file);
      todoFrame.setCurrentFile(file, null);
      todoFrame.getTaskTreePanel().setListChanged(false);
      statusBar.setText(I18nSupport.getValue(BUNDLE_GUI, "status.saved.file.0",
          file.getAbsolutePath()));
    }

    private void openFile() {
      final File selectedFile = GUIUtilities.getOpenFile(currentFile, todoFrame);
      if(selectedFile != null) {
        try {
          final List<DTOTask> dtoTasks = TodoFileIO.readTodoFile(selectedFile);
          final List<Task> tasks = new ArrayList<Task>(dtoTasks.size());
          for (DTOTask dtoTask : dtoTasks)
            tasks.add(Task.fromDTO(dtoTask));
          todoFrame.setCurrentFile(selectedFile, tasks);
          todoFrame.getTaskTreePanel().setListChanged(false);
          statusBar.setText(I18nSupport.getValue(BUNDLE_GUI, "status.opened.file.0",
              selectedFile.getAbsolutePath()));
        } catch (IOException e) {
          JOptionPane.showMessageDialog(todoFrame, I18nSupport.getValue(BUNDLE_MESSAGES,
              "could.not.load.list"), "", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(todoFrame, I18nSupport.getValue(BUNDLE_MESSAGES,
              "wrong.file.format"), "", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private class SettingsMenuItemListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(AC_LANGUAGE_ENG.equals(e.getActionCommand())) {
        Locale.setDefault(Locale.UK);
        resetI18n();
        statusBar.setText(I18nSupport.getValue(BUNDLE_GUI, "status.changed.language"));
      } else if(AC_LANGUAGE_GER.equals(e.getActionCommand())) {
        Locale.setDefault(Locale.GERMANY);
        resetI18n();
        statusBar.setText(I18nSupport.getValue(BUNDLE_GUI, "status.changed.language"));
      }
    }
  }

  private class TaskActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {

      if(AC_ADD_TASK.equals(e.getActionCommand())) {
        addTask();
      } else if(AC_EDIT_TASK.equals(e.getActionCommand())) {
        editTask();
      } else if(AC_REMOVE_TASK.equals(e.getActionCommand())) {
        removeTasks();
      }
      todoFrame.updateGUI();
    }

    private void editTask() {
      final List<Task> tasks = taskTreePanel.getSelectedTasks();
      if(!tasks.isEmpty()) {
        final Task task = tasks.get(tasks.size()-1);
        if(task != null) {
          todoFrame.openTask(task, false);
          statusBar.showTaskInformation(task);
        } else statusBar.setText(I18nSupport.getValue(BUNDLE_MESSAGES, "no.task.selected"));
      }
    }

    private void addTask() {
      Task task = new Task();
      task.setDueDate(System.currentTimeMillis());
      todoFrame.openTask(task, true);
      statusBar.setText(I18nSupport.getValue(BUNDLE_GUI, "status.text.added.task"));
      statusBar.showTaskInformation(task);
    }

    private void removeTasks() {
      if(!taskTreePanel.selectedNodeExists())
        return;

      String text = "";
      String[] strings = {I18nSupport.getValue(BUNDLE_GUI, "dialog.button.remove"),
          I18nSupport.getValue(BUNDLE_GUI, "dialog.button.reset.selections"),
          I18nSupport.getValue(BUNDLE_GUI, "dialog.button.not.sure")};
      int result = GUIUtilities.showConfirmDialog(todoFrame, strings,
          I18nSupport.getValue(BUNDLE_MESSAGES, "remove.selected.tasks.question"),
          I18nSupport.getValue(BUNDLE_GUI, "dialog.title.remove.tasks"),
          JOptionPane.QUESTION_MESSAGE);
      if(result == JOptionPane.YES_OPTION) {
        int deleted = taskTreePanel.removeSelectedNodes();
        if(deleted != 0)
          text = I18nSupport.getValue(BUNDLE_GUI,
              "status.text.removed.tasks.0",deleted);
      } else if(result == JOptionPane.NO_OPTION)
        taskTreePanel.deselectAllTasks();
      statusBar.setText(text);
    }
  }
}

class GUIUtilities {
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  private static final FileFilter FILE_FILTER = new FileNameExtensionFilter(
      I18nSupport.getValue(BUNDLE_GUI, "file.filter.todo.description"), "todo"); //NON-NLS
  private static final String DOT_EXTENSION_TODO = ".todo"; //NON-NLS
  private static final String DEFAULT_LIST_NAME =
      I18nSupport.getValue(BUNDLE_GUI, "file.default.name");

  /**
   * Opens an open file dialog at the specified file if it exists.
   * The method returns a File-Object of the selected file.
   * @param startFile Opens the chooser on this file
   * @return The selected file if the approved option was pressed,
   * otherwise null.
   */
  public static File getOpenFile(File startFile, Component parent) {
    final JFileChooser chooser;
    final int chooserResult;
    final File selectedFile;

    chooser = new JFileChooser();
    chooser.setFileFilter(FILE_FILTER);
    chooser.setMultiSelectionEnabled(false);
    if(startFile != null)
      chooser.setSelectedFile(startFile);
    else chooser.setSelectedFile(new File(DEFAULT_LIST_NAME));

    chooserResult = chooser.showOpenDialog(parent);
    if(JFileChooser.APPROVE_OPTION == chooserResult) {
      selectedFile = chooser.getSelectedFile();
    } else selectedFile = null;

    return selectedFile;
  }

  /**
   * Opens a save file dialog at the specified file if it exists.
   * The method returns a File-Object of the selected file.
   * @param startFile Opens the chooser on this file
   * @return The selected file if the approved option was pressed,
   * otherwise null.
   */
  public static File getSaveFile(File startFile, Component parent) {
    final JFileChooser chooser;
    final int chooserResult;
    File selectedFile;

    chooser = new JFileChooser();
    chooser.setFileFilter(FILE_FILTER);
    chooser.setMultiSelectionEnabled(false);
    if(startFile != null)
      chooser.setSelectedFile(startFile);
    else chooser.setSelectedFile(new File(DEFAULT_LIST_NAME));

    chooserResult = chooser.showSaveDialog(parent);
    if(JFileChooser.APPROVE_OPTION == chooserResult) {
      selectedFile = chooser.getSelectedFile();
      String path = selectedFile.getAbsolutePath();
      if(!path.toLowerCase().endsWith(DOT_EXTENSION_TODO)) {
        selectedFile = new File(path + DOT_EXTENSION_TODO);
      }
    } else selectedFile = null;

    return selectedFile;
  }

  @SuppressWarnings("MagicConstant")
  public static int showConfirmDialog(Component parentComponent, String[] strings,
                                      Object message, String title,
                                      int jOptionPaneMessageType) {
    return JOptionPane.showOptionDialog(parentComponent, message, title,
        JOptionPane.YES_NO_CANCEL_OPTION, jOptionPaneMessageType,
        null, strings, strings[2]);
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
    constraints.insets = new Insets(0,3,0,3);
    return constraints;
  }

  public static GridBagConstraints createConstraints(int x, int y, int width, int height) {
    final GridBagConstraints constraints = createConstraints(x,y);
    constraints.gridwidth = width;
    constraints.gridheight = height;
    return constraints;
  }

  public static GridBagConstraints createConstraints(int x, int y, int width, int height, int fill) {
    final GridBagConstraints constraints = createConstraints(x,y,width,height);
    constraints.fill = fill;
    return constraints;
  }

  public static GridBagConstraints createConstraints(int x, int y, int width, int height, int fill,
                                               double  weightx, double weighty) {
    final GridBagConstraints constraints = createConstraints(x,y,width,height, weightx, weighty);
    constraints.fill = fill;
    return constraints;
  }

  public static GridBagConstraints createConstraints(int x, int y, int width, int height,
                                               double  weightx, double weighty) {
    final GridBagConstraints constraints = createConstraints(x,y,width,height);
    constraints.weighty = weighty;
    constraints.weightx = weightx;
    return constraints;
  }
}

class TaskEditorPanel extends JPanel {
  private static final String BUNDLE_GUI = "gui"; //NON-NLS
  private Color notifyColour;

  private Task unchangedTask;
  private JTextArea descriptionArea;
  /* unused for the HCI&GUI project but will be implemented afterwards */
//  private JCheckBox permanentCheckBox;
  private JLabel nameLabel;
  private JTextField nameField;
  private JLabel categoryLabel;
  private JTextField categoryField;
  private JLabel dueDateLabel;
  private JDateChooser dueDateChooser;
  private JLabel attributionLabel;
  private JComboBox<TaskProperty.Attribution> attrComboBox;
  private JLabel priorityLabel;
  private JComboBox<TaskProperty.Priority> priorityComboBox;
  private boolean createNew;
  private boolean twinkle;
  private JButton closeButton;

  TaskEditorPanel(Task unchangedTask, boolean createNew) {
    super(new GridBagLayout());
    twinkle = false;
    notifyColour = new Color(255,0,0,160);
    this.createNew = createNew;
    this.unchangedTask = unchangedTask;
    setFocusable(true);
    setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    initComponents(unchangedTask);
    resetI18n();
  }

  private void initComponents(Task task) {
    int row = 0;

    /* Close Button */
    ImageIcon icon = ResourceGetter.getImage(ResourceList.IMAGE_CLOSE,
        "close"); //NON-NLS
    closeButton = new JButton(icon);
    closeButton.setRolloverIcon(ResourceGetter.getImage(ResourceList.IMAGE_CLOSE_ROLLOVER,
        "close")); //NON-NLS
    closeButton.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    closeButton.setMaximumSize(closeButton.getPreferredSize());
    closeButton.setMinimumSize(closeButton.getPreferredSize());
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Component thisPanel = (Component) e.getSource();
        while(!(thisPanel instanceof TaskEditorPanel) && thisPanel != null)
          thisPanel = thisPanel.getParent();
        TodoFrame.getTodoFrame().closeTask((TaskEditorPanel) thisPanel, getUnchangedTask());
      }
    });

    final GridBagConstraints c = GUIUtilities.createConstraints(0, row);
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    add(closeButton, c);
    row++;

    final KeyListener fieldKeyListener = new FieldKeyListener();
    /* Name part */
    nameLabel = new JLabel();
    nameField = new JTextField(task.getName());

    Dimension dim = nameLabel.getPreferredSize();
    nameLabel.setMaximumSize(new Dimension(dim.width + 5, dim.height));
    nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        nameField.getPreferredSize().height));
    nameField.addKeyListener(fieldKeyListener);
    if(task.getName().isEmpty())
      nameField.setText(I18nSupport.getValue(BUNDLE_GUI, "default.task.name"));

    add(nameLabel, GUIUtilities.createConstraints(0, row));
    add(nameField, GUIUtilities.createConstraints(1, row, 2, 1,
        GridBagConstraints.HORIZONTAL));
    row++;

    /* Category part */
    categoryLabel = new JLabel();
    categoryField = new JTextField(task.getCategory().toString());

    dim = categoryLabel.getPreferredSize();
    categoryLabel.setMaximumSize(new Dimension(dim.width + 5, dim.height));
    categoryField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        categoryField.getPreferredSize().height));
    categoryField.addKeyListener(fieldKeyListener);

    add(categoryLabel, GUIUtilities.createConstraints(0, row));
    add(categoryField, GUIUtilities.createConstraints(1, row, 2, 1,
        GridBagConstraints.HORIZONTAL));
    row++;

    /* Due date part */
    dueDateLabel = new JLabel();
    dueDateChooser = new JDateChooser();

    dim = dueDateLabel.getPreferredSize();
    dueDateLabel.setMaximumSize(new Dimension(dim.width + 5, dim.height));

    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(task.getDueDate());
    dueDateChooser.setDate(calendar.getTime());
    dueDateChooser.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        dueDateChooser.getPreferredSize().height));
    dueDateChooser.addKeyListener(fieldKeyListener);

    add(dueDateLabel, GUIUtilities.createConstraints(0, row));
    add(dueDateChooser, GUIUtilities.createConstraints(1, row, 2, 1,
        GridBagConstraints.HORIZONTAL));
    row++;

    /* Attribution part */
    attributionLabel = new JLabel();
    attrComboBox = new JComboBox<TaskProperty.Attribution>();
    for (TaskProperty.Attribution attribution : TaskProperty.Attribution.values()) {
      attrComboBox.insertItemAt(attribution, attrComboBox.getItemCount());
      if(attribution.equals(task.getAttribution()))
        attrComboBox.setSelectedItem(attribution);
    }

    dim = attributionLabel.getPreferredSize();
    attributionLabel.setMaximumSize(new Dimension(dim.width + 5, dim.height));
    attrComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        attrComboBox.getPreferredSize().height));
    attrComboBox.setRenderer(new gui.TaskEditorPanel.EditorComboBoxRenderer());

    add(attributionLabel, GUIUtilities.createConstraints(0, row));
    add(attrComboBox, GUIUtilities.createConstraints(1, row, 2, 1,
        GridBagConstraints.HORIZONTAL));
    row++;

    /* Priority part */
    priorityLabel = new JLabel();
    priorityComboBox = new JComboBox<TaskProperty.Priority>();
    for (TaskProperty.Priority priority : TaskProperty.Priority.values()) {
      priorityComboBox.insertItemAt(priority, priorityComboBox.getItemCount());
      if(priority.equals(task.getPriority()))
        priorityComboBox.setSelectedItem(priority);
    }

    dim = priorityLabel.getPreferredSize();
    priorityLabel.setMaximumSize(new Dimension(dim.width + 5, dim.height));
    priorityComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        priorityComboBox.getPreferredSize().height));
    priorityComboBox.setRenderer(new EditorComboBoxRenderer());

    add(priorityLabel, GUIUtilities.createConstraints(0, row));
    add(priorityComboBox, GUIUtilities.createConstraints(1, row, 2, 1,
        GridBagConstraints.HORIZONTAL));
    row++;

    /* unused for the HCI&GUI project but will be implemented afterwards */
    /* Permanent part */
//    permanentCheckBox = new JCheckBox();
//    permanentCheckBox.setSelected(task.isPermanent());
//    permanentCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,
//        permanentCheckBox.getPreferredSize().height));
//
//    add(permanentCheckBox, createConstraints(0, row, 1, 1, GridBagConstraints.HORIZONTAL));
//    row++;

    /* Description part */
    descriptionArea = new JTextArea(task.getDescription());
    JScrollPane descriptionPane = new JScrollPane(descriptionArea);

    descriptionPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    descriptionPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    dim = descriptionPane.getPreferredSize();
    descriptionPane.setPreferredSize(new Dimension(dim.width, dim.height*2));
    descriptionPane.setMinimumSize(descriptionPane.getPreferredSize());
    descriptionPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    add(descriptionPane, GUIUtilities.createConstraints(0, row, 3, 1,
        GridBagConstraints.BOTH, 1.0, 1.0));
  }

  public Task getUnchangedTask() {
    return unchangedTask;
  }

  public Task getUpdatedTask() {
    final long dueDate;
    if(dueDateChooser.getCalendar() != null)
      dueDate = dueDateChooser.getCalendar().getTimeInMillis();
    else dueDate = System.currentTimeMillis();

    return new Task(TaskCategory.parseCategory(categoryField.getText()),nameField.getText(),
        descriptionArea.getText(), false,
        /* unused for the HCI&GUI project but will be implemented afterwards */
        //permanentCheckBox.isSelected(),
        dueDate,
        (TaskProperty.Attribution) attrComboBox.getSelectedItem(),
        (TaskProperty.Priority) priorityComboBox.getSelectedItem());
  }

  public void resetI18n() {
    /* Close Button */
    closeButton.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.close"));

    /* Name part */
    nameLabel.setText(I18nSupport.getValue(BUNDLE_GUI, "label.text.name"));
    nameField.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.task.name.not.empty"));

    /* Category part */
    categoryLabel.setText(I18nSupport.getValue(BUNDLE_GUI, "label.text.category"));
    categoryField.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.category.format"));

    /* Due date part */
    dueDateLabel.setText(I18nSupport.getValue(BUNDLE_GUI, "label.text.due.date"));
    dueDateChooser.setLocale(Locale.getDefault());

    /* Attribution part */
    attributionLabel.setText(I18nSupport.getValue(BUNDLE_GUI, "label.text.attribution"));

    /* Priority part */
    priorityLabel.setText(I18nSupport.getValue(BUNDLE_GUI, "label.text.priority"));

    /* unused for the HCI&GUI project but will be implemented afterwards */
    /* Permanent part */
//    permanentCheckBox.setText(I18nSupport.getValue(BUNDLE_GUI, "checkbox.text.permanent"));

    /* Description part */
    descriptionArea.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.enter.description"));
  }

  public boolean isCreatedNew() {
    return createNew;
  }

  public void paint(Graphics g) {
    super.paint(g);
    if(twinkle) {
      g.setColor(notifyColour);
      g.fillRect(0,0,getWidth(),getHeight());
    }
  }

  public void notifyYourself() {
    if(!twinkle) {
      new Thread(new Runnable() {
        public void run() {
          changeNotifyColour();
        }
      }).start();
    }
  }
  /* let the panel twinkle in transparent red */
  private void changeNotifyColour() {
    final Color colourOn = new Color(255,0,0,160);
    final Color colourOff = new Color(255,255,255,0);

    twinkle = true;
    notifyColour = colourOn;
    repaint();
    try {Thread.sleep(330L);}
    catch (InterruptedException ignored) {}
    notifyColour = colourOff;
    repaint();
    try {Thread.sleep(330L);}
    catch (InterruptedException ignored) {}
    notifyColour = colourOn;
    repaint();
    try {Thread.sleep(330L);}
    catch (InterruptedException ignored) {}
    notifyColour = colourOff;
    repaint();
    twinkle = false;
  }

  /* Inner classes */

  private class FieldKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      if(e.getKeyCode() == KeyEvent.VK_ENTER) {
        Component thisPanel = e.getComponent();
        while(!(thisPanel instanceof TaskEditorPanel) && thisPanel != null)
          thisPanel = thisPanel.getParent();
        TodoFrame.getTodoFrame().closeTask((TaskEditorPanel) thisPanel, getUnchangedTask());
      }
    }
  }

  /* Renders the combo box items for priority and attribution */
  private class EditorComboBoxRenderer extends DefaultListCellRenderer {
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
      } else label.setText(value.toString());

      label.setOpaque(true);
      label.setForeground(comp.getForeground());
      label.setBackground(comp.getBackground());

      return label;
    }
  }
}
