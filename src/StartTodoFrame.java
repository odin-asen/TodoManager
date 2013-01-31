import data.LoggingUtility;
import gui.TodoFrame;

import javax.swing.*;
import java.util.logging.Level;

/**
* User: Timm Herrmann
* Date: 04.12.12
* Time: 11:11
*/
public class StartTodoFrame {
  static {
    loadLaF();
    LoggingUtility.setFirstTimeLoggingFile("todomanager.log");
  }

  public static void main(String[] args) {
    if(args.length == 1)
      LoggingUtility.setLevel(Level.parse(args[0]));
    new TodoFrame().setVisible(true);
  }

  public static void loadLaF() {
    try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
    catch(Exception ignored){}
  }
}