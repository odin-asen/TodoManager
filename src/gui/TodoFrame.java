package gui;

import business.Task;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static i18n.BundleStrings.*;

/**
 * User: Timm Herrmann
 * Date: 04.12.12
 * Time: 11:17
 */
public class TodoFrame extends JFrame {
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
    setIconImage(ResourceGetter.getImage(
        ResourceList.IMAGE_TODO_LIST, "todo").getImage()); //NON-NLS

    setJMenuBar(getCreateMenuBar());

    initComponents();
  }

  private String getTitleString() {
    String title = MessageFormat.format("{0} - {1} {2}",
        I18nSupport.getValue(TITLES, "application"),
        I18nSupport.getValue(MISC, "version"), VERSION_NUMBER);
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
    menu = new JMenu(I18nSupport.getValue(COMPONENTS, "text.file"));
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
    createSelectorCalendarPanel();
//    getContentPane().add(createSelectorCalendarPanel(), BorderLayout.LINE_START);
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
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.open.list"));
    button = (JButton) toolBar.getComponent(1);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.save.list"));
    /* component index 2 is a separator */
    button = (JButton) toolBar.getComponent(3);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.add.task"));
    button = (JButton) toolBar.getComponent(4);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.edit.task"));
    button = (JButton) toolBar.getComponent(5);
    button.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.remove.task"));
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
    if(german) item.setSelected(true);
    item = ((JMenuItem) language.getMenuComponent(1));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.english"));
    if(!german) item.setSelected(true);

    /* reset the task menu */
    menu = menuBar.getMenu(2);
    menu.setText(I18nSupport.getValue(COMPONENTS, "text.task"));
    item = ((JMenuItem) menu.getMenuComponent(0));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.add.task"));
    item = ((JMenuItem) menu.getMenuComponent(1));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.edit.task"));
    item = ((JMenuItem) menu.getMenuComponent(2));
    item.setText(I18nSupport.getValue(COMPONENTS, "text.remove.task"));
  }

  private void resetSelectorCalendarI18n() {
    taskTreeTable.resetI18n();
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
//        if (!taskTree.getSelectedNodes().isEmpty()) {
//          changeDueDateDialog(calendar.getDate());
//        }TODO
      }
    };
     /* Each button of the day panel gets a mouse listener that changes the due dates
      * after a button was pressed. */
    for (Component c : calendar.getDayChooser().getDayPanel().getComponents()) {
      if(c instanceof JButton)
        c.addMouseListener(ml);
    }

    panel.add(calendar);
    return panel;
  }

//    taskTree.addTreeSelectionListener(new TreeSelectionListener() {
//      public void valueChanged(TreeSelectionEvent e) {
//        final TreePath path = e.getPath();
//        final Object object = path.getLastPathComponent();
//        if (e.isAddedPath()) {
//          if (object instanceof MutableTaskNode) {
//            final MutableTaskNode node = (MutableTaskNode) object;
//            statusBar.showTaskInformation(node.getTask());
//          } else {
//            statusBar.setText("");
//            statusBar.showTaskInformation(null);
//          }
//        }
//      }
//    });TODO zum tasktree hinzufügen
//    taskTree.addTreeMouseListener(new MouseAdapter() {
//      public void mouseClicked(MouseEvent e) {
//        final JTree tree = (JTree) e.getSource();
//        if (e.getClickCount() == 1) {
//          final TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
//          if (path != null) {
//            final Object object = path.getLastPathComponent();
//            if (object instanceof MutableTaskNode)
//              taskTreeTable.showNode((MutableTaskNode) object);
//          }
//        }
//      }
//    });

  private TaskTreeTable getTableContainer() {
    if(taskTreeTable != null)
      return taskTreeTable;
    taskTreeTable = new TaskTreeTable();
    taskTreeTable.setMaximumSize(taskTreeTable.getPreferredSize());
    taskTreeTable.addTableMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
//        taskTree.deselectAllTasks(); //TODO ?
      }
    });
    return taskTreeTable;
  }

  private void updateGUI() {
//    if(taskTree.hasListChanged())
//      setTitle(getTitleString()+"*");
//    else setTitle(getTitleString());TODO hasListChanged
  }

  private void changeDueDateDialog(Date newDate) {
    int result;
    String[] strings = {I18nSupport.getValue(COMPONENTS, "text.change"),
        I18nSupport.getValue(COMPONENTS, "text.do.not.change"),
        I18nSupport.getValue(COMPONENTS, "text.not.sure")};
    final DateFormat format = new SimpleDateFormat(I18nSupport.getValue(
        MISC, "format.due.date"), Locale.getDefault());
    String dateString = format.format(newDate);
    result = GUIUtilities.showConfirmDialog(this, strings,
        I18nSupport.getValue(MESSAGES, "question.change.selected.due.dates.to.0",
            dateString), "", JOptionPane.QUESTION_MESSAGE, 2);
    if (result == JOptionPane.OK_OPTION) {
      final DTOTask dtoTask = new DTOTask();
      dtoTask.dueDate = newDate.getTime();
//      taskTree.changeSelectedNodes(dtoTask); TODO selection
      taskTreeTable.updateTable();
    } else if(result == JOptionPane.NO_OPTION)               ;
//      taskTree.deselectAllTasks();
  }

  /* Getter and Setter */

  public File getCurrentFile() {
    if(currentFile == null)
      return new File("");
    return currentFile;
  }

  public void setCurrentFile(File newFile) {
    currentFile = newFile != null ? newFile : new File("");
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

  private boolean hasChanged() {
    return true;//taskTree.hasListChanged();
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
      if(AC_OPEN_FILE.equals(e.getActionCommand())) {
        openFile();
      } else if(AC_SAVE.equals(e.getActionCommand())) {
        saveFile();
      } else if(AC_SAVE_AS.equals(e.getActionCommand())) {
        saveFileAs();
      } else if(AC_CLOSE.equals(e.getActionCommand())) {
        closeApplication();
      }
      updateGUI();
    }

    private void closeApplication() {
      int result = JOptionPane.NO_OPTION;
      String[] strings = {I18nSupport.getValue(COMPONENTS, "text.close.and.save"),
          I18nSupport.getValue(COMPONENTS, "text.close.without.saving"),
          I18nSupport.getValue(COMPONENTS, "text.not.sure")};

      if(hasChanged()) {
        result = GUIUtilities.showConfirmDialog(frame, strings,
          I18nSupport.getValue(MESSAGES, "question.save.before.close"), "",
          JOptionPane.QUESTION_MESSAGE, 2);
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
      final File selectedFile = GUIUtilities.getSaveFile(currentFile, frame);
      try {
        if(selectedFile != null)
          writeListToFile(selectedFile);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(frame, I18nSupport.getValue(MESSAGES,
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
          JOptionPane.showMessageDialog(frame, I18nSupport.getValue(MESSAGES,
              "could.not.save.list"), "", JOptionPane.ERROR_MESSAGE);
      }
    }

    private void writeListToFile(File file) throws IOException {
//      final List<DTOTask> tasks = Converter.toDTOList(taskTree.getTaskRoot());
//      TodoFileIO.writeTodoFile(tasks, file);
//      setCurrentFile(file);
//      taskTree.setListChanged(false);
//      statusBar.setText(I18nSupport.getValue(MESSAGES, "saved.file.0",
//          file.getAbsolutePath())); TODO
    }

    private void openFile() {
//      final File selectedFile = GUIUtilities.getOpenFile(currentFile, frame);
//      if(selectedFile != null) {
//        try {
//          final List<DTOTask> dtoTasks = TodoFileIO.readTodoFile(selectedFile);
//          final MutableTaskNode root = Converter.fromDTOList(dtoTasks);
//          taskTree.setRoot(root);
//          taskTreeTable.showNode(root);
//          frame.setCurrentFile(selectedFile);
//          taskTree.setListChanged(false);
//          statusBar.setText(I18nSupport.getValue(MESSAGES, "opened.file.0",
//              selectedFile.getAbsolutePath()));
//        } catch (IOException e) {
//          JOptionPane.showMessageDialog(frame,
//              I18nSupport.getValue(MESSAGES, "could.not.load.list"), "",
//              JOptionPane.ERROR_MESSAGE);
//        } catch (Exception e) {
//          JOptionPane.showMessageDialog(frame,
//              I18nSupport.getValue(MESSAGES, "wrong.file.format"), "",
//              JOptionPane.ERROR_MESSAGE);
//        }
//   - }TODO
    }
  }

  private class SettingsMenuItemListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      final String text = I18nSupport.getValue(MESSAGES, "changed.language");
      if(AC_LANGUAGE_ENG.equals(e.getActionCommand())) {
        Locale.setDefault(Locale.UK);
        resetI18n();
        statusBar.setText(text);
      } else if(AC_LANGUAGE_GER.equals(e.getActionCommand())) {
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

      if(AC_ADD_TASK.equals(e.getActionCommand())) {
        addTask();
      } else if(AC_EDIT_TASK.equals(e.getActionCommand())) {
        editTask();
      } else if(AC_REMOVE_TASK.equals(e.getActionCommand())) {
        removeTasks();
      }
      updateGUI();
    }

    private void editTask() {
//      final List<MutableTaskNode> tasks = taskTree.getSelectedNodes();
//      String text = "";
//      if(tasks.size() > 1) {
//        String[] strings = {I18nSupport.getValue(COMPONENTS, "text.yes.edit.all"),
//            I18nSupport.getValue(COMPONENTS, "text.only.the.first"),
//            I18nSupport.getValue(COMPONENTS, "text.cancel"),
//            I18nSupport.getValue(COMPONENTS, "text.not.sure")};
//        int result = GUIUtilities.showConfirmDialog(frame, strings,
//            I18nSupport.getValue(MESSAGES, "edit.all.selected.tasks.question"),
//            I18nSupport.getValue(TITLES, "edit.tasks"),
//            JOptionPane.QUESTION_MESSAGE, 4);
//        if(result == 1) {
//          //TODO Alles editieren
//          text = I18nSupport.getValue(MESSAGES, "changed.task");
//        } else if(result == 2) {
//          //TODO nur das erste editieren
//          text = I18nSupport.getValue(MESSAGES, "changed.task");
//        } else if(result == 3) {
//          taskTree.deselectAllTasks();
//        }
//      }
//      statusBar.setText(text); TODO
    }

    private void addTask() {  //TODO add Task
//      final MutableTaskNode node = taskTree.addTask(true);
//      taskTreeTable.showNode(node);
//      statusBar.setText(I18nSupport.getValue(MESSAGES, "added.task"));
//      statusBar.showTaskInformation(node.getTask());
    }

    private void removeTasks() {
//      final List<MutableTaskNode> tasks = taskTree.getSelectedNodes();
//      String text = "";
//      if(tasks.size() > 0) {
//        String[] strings = {I18nSupport.getValue(COMPONENTS, "text.remove"),
//          I18nSupport.getValue(COMPONENTS, "text.reset.selections"),
//          I18nSupport.getValue(COMPONENTS, "text.not.sure")};
//        int result = GUIUtilities.showConfirmDialog(frame, strings,
//          I18nSupport.getValue(MESSAGES, "remove.selected.tasks.question"),
//          I18nSupport.getValue(TITLES, "remove.tasks"),
//          JOptionPane.QUESTION_MESSAGE, 2);
//        if(result == JOptionPane.YES_OPTION) {
//          int deleted = taskTree.removeSelectedTasks();
//          if(deleted != 0) {
//            taskTreeTable.showNode(null);
//            text = I18nSupport.getValue(MESSAGES, "removed.tasks.0",deleted);
//          }
//        } else if(result == JOptionPane.NO_OPTION)
//          taskTree.deselectAllTasks();
//        statusBar.setText(text);
//      }
    }
  }
}

class GUIUtilities {
  private static final FileFilter FILE_FILTER = new FileNameExtensionFilter(
      I18nSupport.getValue(MISC, "file.filter.todo.description"), "todo"); //NON-NLS
  private static final String DOT_EXTENSION_TODO = ".todo"; //NON-NLS
  private static final String DEFAULT_LIST_NAME =
      I18nSupport.getValue(MISC, "file.default.name");

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
  private Color notifyColour;

  private Task unchangedTask;
  private JTextArea descriptionArea;
  /* unused for the HCI&GUI project but will be implemented afterwards */
//  private JCheckBox permanentCheckBox;
  private JLabel nameLabel;
  private JTextField nameField;
//  private JLabel categoryLabel;
//  private JTextField categoryField;
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
//        frame.closeTask((TaskEditorPanel) thisPanel, getUnchangedTask());
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
      nameField.setText(I18nSupport.getValue(MISC, "default.task.name"));

    add(nameLabel, GUIUtilities.createConstraints(0, row));
    add(nameField, GUIUtilities.createConstraints(1, row, 2, 1,
        GridBagConstraints.HORIZONTAL));
    row++;

    /* Category part */
//    categoryLabel = new JLabel();
//    categoryField = new JTextField(task.getCategory().toString());
//
//    dim = categoryLabel.getPreferredSize();
//    categoryLabel.setMaximumSize(new Dimension(dim.width + 5, dim.height));
//    categoryField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
//        categoryField.getPreferredSize().height));
//    categoryField.addKeyListener(fieldKeyListener);
//
//    add(categoryLabel, GUIUtilities.createConstraints(0, row));
//    add(categoryField, GUIUtilities.createConstraints(1, row, 2, 1,
//        GridBagConstraints.HORIZONTAL));
//    row++;

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
    attrComboBox.setRenderer(new AttributionPriorityRenderer());

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
    priorityComboBox.setRenderer(new AttributionPriorityRenderer());

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
//    descriptionArea = new JTextArea(task.getDescription());
//    JScrollPane descriptionPane = new JScrollPane(descriptionArea);
//
//    descriptionPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//    descriptionPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//    dim = descriptionPane.getPreferredSize();
//    descriptionPane.setPreferredSize(new Dimension(dim.width, dim.height*2));
//    descriptionPane.setMinimumSize(descriptionPane.getPreferredSize());
//    descriptionPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
//    add(descriptionPane, GUIUtilities.createConstraints(0, row, 3, 1,
//        GridBagConstraints.BOTH, 1.0, 1.0));
  }

  public Task getUnchangedTask() {
    return unchangedTask;
  }

  public Task getUpdatedTask() {
    final long dueDate;
    if(dueDateChooser.getCalendar() != null)
      dueDate = dueDateChooser.getCalendar().getTimeInMillis();
    else dueDate = System.currentTimeMillis();

    return null;
//    new Task(unchangedTask.getParent(), nameField.getText(),
//        descriptionArea.getText(), unchangedTask.isPermanent(),
//        /* unused for the HCI&GUI project but will be implemented afterwards */
//        //permanentCheckBox.isSelected(),
//        dueDate,
//        (TaskProperty.Attribution) attrComboBox.getSelectedItem(),
//        (TaskProperty.Priority) priorityComboBox.getSelectedItem());
  }

  public void resetI18n() {
    /* Close Button */
    closeButton.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.close"));

    /* Name part */
    nameLabel.setText(I18nSupport.getValue(COMPONENTS, "text.name.colon"));
    nameField.setToolTipText(I18nSupport.getValue(COMPONENTS, "tooltip.task.name.not.empty"));

    /* Category part */
//    categoryLabel.setText(I18nSupport.getValue(BUNDLE_GUI, "label.text.category"));
//    categoryField.setToolTipText(I18nSupport.getValue(BUNDLE_GUI, "tooltip.text.category.format"));

    /* Due date part */
    dueDateLabel.setText(I18nSupport.getValue(COMPONENTS, "text.due.date.colon"));
    dueDateChooser.setLocale(Locale.getDefault());

    /* Attribution part */
    attributionLabel.setText(I18nSupport.getValue(COMPONENTS, "text.attribution.colon"));

    /* Priority part */
    priorityLabel.setText(I18nSupport.getValue(COMPONENTS, "text.priority.colon"));

    /* unused for the HCI&GUI project but will be implemented afterwards */
    /* Permanent part */
//    permanentCheckBox.setText(I18nSupport.getValue(BUNDLE_GUI, "checkbox.text.permanent"));

    /* Description part */
    descriptionArea.setToolTipText(
        I18nSupport.getValue(COMPONENTS, "tooltip.enter.description"));
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
//        TodoFrame.getTodoFrame().closeTask((TaskEditorPanel) thisPanel, getUnchangedTask());
      }
    }
  }
}
